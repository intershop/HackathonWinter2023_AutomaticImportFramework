package com.zamro.component.internal.automation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.log.Logger;
import com.zamro.component.capi.automation.AutomatedImportChainFileCollector;
import com.zamro.component.capi.automation.FileTransferSession;
import com.zamro.component.capi.automation.FileTransferSessionFactory;

/**
 * A basic implementation of interface {@link AutomatedImportChainFileCollector}.
 * 
 * @author JCMeyer
 */
public class BasicAutomatedImportChainFileCollector implements AutomatedImportChainFileCollector
{
    /**
     * A prefix added to remote files successfully pulled to the local system.
     */
    protected static final String FILE_PREFIX_LOADED = "_LOADED_";

    /**
     * A custom file name comparator working only on the individual part of an import file name.
     */
    protected final PrefixFilteringLexicographicFileNameComparator fileNameComparator = new PrefixFilteringLexicographicFileNameComparator();

    /**
     * Defines the import types known by this file collector as also by pipeline 'ProcessAutomatedImport'.
     */
    protected enum ImportTypes
    {
        PRODUCT               ("Product"),
        CATALOG               ("Catalog"),
        PRICELIST             ("PriceList"),
        SOLRCONFIG            ("SolrConfig"),
        PRODUCTATTRIBUTEGROUP ("ProductAttributeGroup");
        
        private String businessValue;
        
        ImportTypes(String businessValue)
        {
            this.businessValue = businessValue;
        }
        
        /**
         * Helper method returning the business value of the current import type.
         * 
         * @return the business value
         */
        public String getBusinessValue() {
            return businessValue;
        }
        
        /**
         * Helper method checking if the current import type defines a regular XML import as offered by the import framework.
         * 
         * @return <code>true</code> if the given import type defines a regular XML import, otherwise <code>false</code>
         */
        public boolean isImportFileImportType()
        {
            return EnumSet.of(CATALOG, PRODUCT, PRICELIST, PRODUCTATTRIBUTEGROUP).contains(this);
        }
        
        /**
         * Helper method checking if the current import type defines a Solr configuration import.
         * 
         * @return <code>true</code> if the given import type defines a Solr configuration import, otherwise <code>false</code>
         */
        public boolean isSolrConfigImportType() {
            return EnumSet.of(SOLRCONFIG).contains(this);
        }
    }

    /**
     * While the {@link File} class allows sorting out of the box this custom {@link Comparator} implementation ensures
     * that only the individual part of the file name following the import mode key words is used for name comparison.
     */
    protected class PrefixFilteringLexicographicFileNameComparator implements Comparator<File>
    {
        protected Pattern prefixFilterRegex = null;
        
        /**
         * Constructor compiling the regular expression used for filtering the individual name parts. 
         */
        public PrefixFilteringLexicographicFileNameComparator()
        {
            this.prefixFilterRegex = Pattern.compile("_(" + getPipeSeparatedLowerCaseImportModes() + ")");
        }
        
        @Override
        public int compare(File a, File b)
        {
            String aName = prefixFilterRegex.split(a.getName(), 2)[1]; 
            String bName = prefixFilterRegex.split(b.getName(), 2)[1];
            return aName.compareTo(bName);
        }
    }

    protected Map<String, List<File>> importFileMap = new HashMap<>();

    protected FileTransferSessionFactory sessionFactory = null;

    protected String importFoldersPattern = "*";

    protected String importBaseFolder = null;

    @Override
    public void setFileTransferSessionFactory(FileTransferSessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void setImportFoldersPattern(String importFoldersPattern)
    {
        this.importFoldersPattern = importFoldersPattern;
    }

    @Override
    public void setImportBaseFolder(String importBaseFolder)
    {
        this.importBaseFolder = importBaseFolder;
    }

    @Override
    public void pullFilesFromRemoteLocation() throws Exception
    {
        Logger.debug(this, "Pulling files from remote location using folder pattern '{}'.", importFoldersPattern);
        try (FileTransferSession session = sessionFactory.createFileTransferSession(importBaseFolder))
        {
            importFileMap.clear();
            for (String domainFolderName : session.getFolderElements(importFoldersPattern))
            {
                processFolder(session, importFileMap, domainFolderName);
            }
        }
    }

    @Override
    public List<File> getPulledFiles(String importType, String importDomainName)
    {
        List<File> files = importFileMap.get(calculateImportFilesKey(importType, importDomainName));
        if (null == files) {
            files = Collections.emptyList();
        }
        else
        {
            Collections.sort(files, fileNameComparator);
        }
        
        StringBuilder fileNamesToBeLogged = new StringBuilder(" ");
        files.forEach(f -> fileNamesToBeLogged.append(f.getName() + ' '));
        Logger.debug(this, "Handing out files requested for import type '{}' and import domain '{}': [{}].", importType, importDomainName, fileNamesToBeLogged.toString());
        
        return files; 
    }

    @Override
    public String getPipeSeparatedLowerCaseImportTypes()
    {
        return String.join("|", Arrays.stream(ImportTypes.values()).map(ImportTypes::getBusinessValue).collect(Collectors.toList())).toLowerCase();
    }

    @Override
    public String getPipeSeparatedLowerCaseImportModes()
    {
        return "update|replace|ignore|delete|initial|omit";
    }

    /**
     * Fetches all files matching {@link ImportTypes} related naming conventions from the folder defined through the
     * given folder name and collects these files into the given import files map. The given folder name needs to match
     * a valid import {@link Domain} known at the target system - otherwise the operation will fail.
     * 
     * @param session the {@link FileTransferSession} to be used for fetching the files
     * @param importFileMap the map to fetched files will be collected into
     * @param domainFolderName the name of the folder to be processed
     * @throws Exception
     */
    protected void processFolder(FileTransferSession session, Map<String, List<File>> importFileMap, String domainFolderName) throws Exception
    {
        Logger.debug(this, "Processing folder '{}'.", domainFolderName);
        try
        {
            for (ImportTypes importType : ImportTypes.values())
            {
                String targetFolderPath = null;
                
                List<String> sourceFileNames = session.getFolderElements(domainFolderName + '/' + importType.getBusinessValue().toLowerCase() + "_*");
                
                if (! sourceFileNames.isEmpty() && importType.isImportFileImportType())
                {
                    targetFolderPath = ensureImportFileTargetFolderPath(domainFolderName);
                }
                
                for (String sourceFileName : sourceFileNames)
                {
                    String targetFileName = null;
                    if (importType.isSolrConfigImportType())
                    {
                        SolrConfigCalculator solrConfigCalculator = new SolrConfigCalculator(domainFolderName);
                        targetFileName = solrConfigCalculator.calculateTargetFileName(sourceFileName); 
                        targetFolderPath = solrConfigCalculator.ensureTargetFolderPath(sourceFileName, targetFileName);
                    }
                    else
                    {
                        targetFileName = sourceFileName;
                    }
                    
                    String sourceFilePath = domainFolderName + '/' + sourceFileName;
                    String targetFilePath = targetFolderPath + '/' + targetFileName;
                    
                    Logger.debug(this, "Copying file '{}' => '{}' ...", sourceFilePath, targetFilePath);
                    session.copyFile(sourceFilePath, targetFilePath);
                    session.prefixFile(domainFolderName, sourceFileName, FILE_PREFIX_LOADED);
                    
                    addToImportFiles(importFileMap, importType, domainFolderName, targetFolderPath + '/' + sourceFileName);
                }
            }
        }
        catch (IllegalArgumentException iae)
        {
            Logger.error(this, "Error processing domain folder named '{}'. {}", domainFolderName, iae);
        }
    }

    /**
     * Helper method adding the provided target file path to the given import files map using a combination of import
     * type and import domain name as key object. The import files map will be read by {@link #getPulledFiles(String, String)}. 
     * <p>
     * See also: {@link #calculateImportFilesKey(String, String)}
     * 
     * @param importFiles the map to add the given target file path to
     * @param importType the import type to be part of the key
     * @param importDomainName the import domain name to be part of the key
     * @param targetPath the target file path to be added
     */
    protected void addToImportFiles(Map<String, List<File>> importFiles, ImportTypes importType, String importDomainName, String targetPath)
    {
        String key = calculateImportFilesKey(importType.getBusinessValue(), importDomainName);
        if (null == importFiles.get(key))
        {
            importFiles.put(key, new ArrayList<File>());
        }
        importFiles.get(key).add(new File(targetPath));
    }

    /**
     * Helper method calculating a unique key for putting fetched files into the import files map or to read files from
     * the map filtering by import type and import domain name.
     * <p>
     * See also: {@link #getPulledFiles(String, String)}
     * 
     * @param importType
     * @param importDomainName
     * @return a unique import files Key
     */
    protected String calculateImportFilesKey(String importType, String importDomainName)
    {
        String importFilesKey = importType + "$$" + importDomainName;
        return importFilesKey;
    }

    /**
     * Helper method calculating and ensuring an absolute import file target folder path from a given domain name. The
     * domain name is checked for validity.
     * 
     * @param domainName
     * 
     * @return the target folder path
     */
    protected String ensureImportFileTargetFolderPath(String domainName) throws IllegalArgumentException
    {
        String unitImpexDirectory = FileUtils.getUnitImpexDirectory(domainName);
        if (null == unitImpexDirectory)
        {
            throw new IllegalArgumentException("The given domain name doesn't specify a valid unit impex directory.");
        }
        File importFileTargetFolder = new File(unitImpexDirectory + "/src/catalog");
        if (! importFileTargetFolder.exists())
        {
            importFileTargetFolder.mkdirs();
        }
        return importFileTargetFolder.getAbsolutePath();
    }

    /**
     * This helper class encapsulates all Solr configuration specific import related calculations. It was introduced to
     * minimize the amount of recurring calculations as also to keep the regular import code free from larger amounts
     * of Solr specific logic.
     *
     */
    protected class SolrConfigCalculator
    {
        protected static final String ISH_CONFIG_XML = "ISH-Config.xml";
        
        protected static final String SCHEMA_XML = "schema.xml";
        
        protected final String solrConfigStr = ImportTypes.SOLRCONFIG.getBusinessValue().toLowerCase();
        
        protected String activeUnitBaseFolder = null;
        
        /**
         * Constructs a new calculator for the given domain name. The domain name is checked for validity.
         * 
         * @param domainName the domain context the calculator will work on
         */
        public SolrConfigCalculator(String domainName) throws IllegalArgumentException
        {
            activeUnitBaseFolder = FileUtils.getActiveUnitBaseDirectory(domainName);
            if (null == activeUnitBaseFolder)
            {
                throw new IllegalArgumentException("The given domain name doesn't specify a valid active unit base directory.");
            }
        }
        
        /**
         * Calculates and ensures the configuration file target folder path based on the given file name and target file
         * name. 
         * 
         * @param sourceFileName the original file name as found on the remote system
         * @param targetFileName the target file name as interpreted by the Solr functionality
         * @return the target folder path
         */
        public String ensureTargetFolderPath(String sourceFileName, String targetFileName ) throws IllegalArgumentException
        {
            File targetFolder = null;
            if (ISH_CONFIG_XML.equals(targetFileName)) {
                String indexFolderName = sourceFileName.substring((solrConfigStr + "_ISH-Config-").length()).replaceFirst("\\.xml$", "");
                targetFolder = new File(activeUnitBaseFolder + "/indexes/" + indexFolderName);
            }
            else if (SCHEMA_XML.equals(targetFileName)) {
                String indexFolderName = sourceFileName.substring((solrConfigStr + "_schema-").length()).replaceFirst("\\.xml$", "");
                targetFolder = new File(activeUnitBaseFolder + "/indexes/" + indexFolderName + "/conf");
            }
            else {
                throw new IllegalArgumentException("The given target file name needs to match '" + ISH_CONFIG_XML + "' or '" + SCHEMA_XML + "'.");
            }
            if (! targetFolder.exists())
            {
                targetFolder.mkdirs();
            }
            return targetFolder.getAbsolutePath();
        }
        
        /**
         * Calculates the target file name as interpreted by the Solr functionality based on the given source file name.
         * 
         * @param sourceFileName the original file name as found on the remote system
         * @return the calculated target file name or <code>null</code> in case of invalid input
         */
        public String calculateTargetFileName(String sourceFileName)
        {
            String targetFileName = null;
            
            if (sourceFileName.startsWith(solrConfigStr + "_ISH-Config-")) {
                targetFileName = ISH_CONFIG_XML;
            }
            else if (sourceFileName.startsWith(solrConfigStr + "_schema-")) {
                targetFileName = SCHEMA_XML;
            }
            
            return targetFileName;
        }
    }

}
