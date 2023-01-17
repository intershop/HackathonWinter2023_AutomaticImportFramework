package com.intershop.component.processstatistics.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * Holds statistics informations about file imports.
 * <p/>
 * Objects will be marshalled as JSON string and stored to ProcessStatisticsPO.
 * 
 * @author Torsten Herrmann
 */
@JsonPropertyOrder({ "File", "Domain", "ImportMode", "Elements", "BeforeStats", "AfterStats" })
public class FileImportStatistics implements Serializable
{

    /** serialization ID */
    private static final long serialVersionUID = -3909042110298649690L;

    /** import file name */
    private String filename;

    /** domain name */
    private String domain;

    /** import mode */
    private String importMode;

    /** element count */
    private long elements;

    /** import statistics before file import */
    private ProductImportStatistics beforeImportStats;

    /** import statistics after file import */
    private ProductImportStatistics afterImportStats;
    
    /** type of imported object */
    private String objectType;

    /**
     * Returns object type of related file
     * 
     * @return object type
     */
    public String setObjectTypeFromFilename(String aFileName)
    {
        String type = null;
        Map<String, String> filenameMatches = new HashMap<>();
        filenameMatches.put("^product_.*_part_.*", "Parts");
        filenameMatches.put("^product_.*_prod_.*", "Products");
        filenameMatches.put("^pricelist_.*", "Prices");
        filenameMatches.put("^vehicle_typs_.*", "Types");
        filenameMatches.put("^catalog_.*", "Catalogs");
        
        if (StringUtils.isNoneBlank(aFileName))
        {
            for (Map.Entry<String, String> check : filenameMatches.entrySet())
            {
                if (aFileName.matches(check.getKey()))
                {
                    type = check.getValue();
                    break;
                }
            }
        }
        return type;
    }
    
    @JsonProperty("objectType")
    public String getObjectType()
    {
        return objectType;
    }
    
    public void setObjectType(String value)
    {
        this.objectType = value;
    }

    @JsonProperty("File")
    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    @JsonProperty("Domain")
    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    @JsonProperty("ImportMode")
    public String getImportMode()
    {
        return importMode;
    }

    public void setImportMode(String importMode)
    {
        this.importMode = importMode;
    }

    @JsonProperty("BeforeStats")
    public ProductImportStatistics getBeforeImportStats()
    {
        return beforeImportStats;
    }

    public void setBeforeImportStats(ProductImportStatistics importStats)
    {
        this.beforeImportStats = importStats;
    }

    @JsonProperty("AfterStats")
    public ProductImportStatistics getAfterImportStats()
    {
        return afterImportStats;
    }

    public void setAfterImportStats(ProductImportStatistics afterImportStats)
    {
        this.afterImportStats = afterImportStats;
    }

    @JsonProperty("Elements")
    public long getElements()
    {
        return elements;
    }

    public void setElements(long elements)
    {
        this.elements = elements;
    }

}
