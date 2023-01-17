package com.intershop.component.processstatistics.internal;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class TestStatisticsToJson
{

    @Test
    public void test() throws Exception
    {
        writeJson(create());
    }
    
    private ProductImportStatistics create()
    {
        ProductImportStatistics stats = new ProductImportStatistics();
        stats.setProducts(10000);
        stats.setProductsOnline(9000);
        stats.setParts(5000);
        stats.setPartsOnline(4000);
        stats.setTypes(1500);
        stats.setTypesOnline(1400);
        stats.setCategoriesSearchIndex(3000);
        stats.setProductsSearchIndex(15000);
        stats.setProductsWithCategories(9000);
        stats.setProductsWithoutCategories(1000);
        return stats;
    }

    @Test
    public void testFileStats() throws Exception
    {
        FileImportStatistics stats = new FileImportStatistics();
        stats.setDomain("BMW-COM");
        stats.setFilename("test_product_import.xml");
        stats.setImportMode("UPDATE");
        stats.setElements(5000);
        stats.setBeforeImportStats(create());
        stats.setAfterImportStats(create());
        writeJson(stats);
    }
    
    private void writeJson(Object obj) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, obj);
    }
}
