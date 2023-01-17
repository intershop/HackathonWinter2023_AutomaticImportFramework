package com.zamro.component.capi.automation;

import java.io.File;
import java.util.List;

/**
 * A business logic level interface to pull import files from a remote system
 * and keep references to the pulled files in order to provide them filtered on
 * import type and import domain name.
 * 
 * @author JCMeyer
 */
public interface AutomatedImportChainFileCollector
{
    /**
     * Sets a {@link FileTransferSessionFactory} used to create the underlying
     * file transfer session. Usually set via component framework.
     * 
     * @param sessionFactory
     */
    public void setFileTransferSessionFactory(FileTransferSessionFactory sessionFactory);

    /**
     * Sets the optional import folders pattern defining the folders to collect
     * import files from. If no folder pattern is provided, all folders will be
     * processed - pattern <code>'*'</code>. The folders are looked up relative
     * to the implicit or actively defined import base folder.
     * <p>
     * Example: Providing a pattern <code>'Zamro*'</code> will collect from folders <code>'Zamro'</code>
     * and <code>'Zamro-ZamroNL'</code> while ignoring a folder <code>'Foo'</code>.
     * <p>
     * See also: {@link #setImportBaseFolder(String)}
     * 
     * @param importFoldersPattern
     */
    public void setImportFoldersPattern(String importFoldersPattern);

    /**
     * Sets the optional import base folder. If a base folder is defined, the
     * remote working directory set after login to the remote system will be
     * changed to the provided base folder. 
     * 
     * @param importBaseFolder
     */
    public void setImportBaseFolder(String importBaseFolder);
    
    /**
     * Pulls files from a remote location using a {@link FileTransferSession}
     * created through the previously provided {@link FileTransferSessionFactory}.
     * <p>
     * See also: {@link #setFileTransferSessionFactory(FileTransferSessionFactory)}
     * 
     * @throws Exception
     */
    public void pullFilesFromRemoteLocation() throws Exception;

    /**
     * Returns the previously pulled import files, filtered on the given import
     * type and import domain name. If no or no successful pull operation has
     * been executed before or no import files were available remotely, an empty
     * list will be returned.
     * 
     * @param importType
     * @param importDomainName
     * @return the previously pulled import files, might be empty
     */
    public List<File> getPulledFiles(String importType, String importDomainName);

    /**
     * Returns a pipe separated list of lower case business values of all available
     * import types.
     * <p>
     * Example: <code>product|catalog|pricelist<code>
     *
     * @return a pipe separated list of lower case business values
     */
    public String getPipeSeparatedLowerCaseImportTypes();

    /**
     * Returns a pipe separated list of lower case business values of all available
     * import modes.
     * <p>
     * Example: <code>update|replace|ignore<code>
     *
     * @return a pipe separated list of lower case business values
     */
    public String getPipeSeparatedLowerCaseImportModes();
}
