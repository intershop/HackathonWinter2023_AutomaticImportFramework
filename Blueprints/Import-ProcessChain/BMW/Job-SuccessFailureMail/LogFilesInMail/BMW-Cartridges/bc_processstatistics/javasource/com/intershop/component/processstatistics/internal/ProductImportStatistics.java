package com.intershop.component.processstatistics.internal;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Holds statistics informations about imports.
 * <p/>
 * Objects will be marshalled as JSON string and stored to ProcessStatisticsPO.
 * 
 * @author Torsten Herrmann
 */
public class ProductImportStatistics implements Serializable
{

    /** serialization ID */
    private static final long serialVersionUID = 7070012180108792648L;

    /** count of products */
    private long products;

    /** count of online products */
    private long productsOnline;

    /** count of parts */
    private long parts;

    /** count of online parts */
    private long partsOnline;

    /** count of types */
    private long types;

    /** count of online types */
    private long typesOnline;

    /** count of products in search index */
    private long productsSearchIndex;
    
    /** count of categories in search index */
    private long categoriesSearchIndex;

    /** count of products with category assignments */
    private long productsWithCategories;

    /** count of products without category assignments */
    private long productsWithoutCategories;

    @JsonProperty("ProdWithCats")
    public long getProductsWithCategories()
    {
        return productsWithCategories;
    }

    public void setProductsWithCategories(long productsWithCategories)
    {
        this.productsWithCategories = productsWithCategories;
    }

    @JsonProperty("ProdWithoutCats")
    public long getProductsWithoutCategories()
    {
        return productsWithoutCategories;
    }

    public void setProductsWithoutCategories(long productsWithoutCategories)
    {
        this.productsWithoutCategories = productsWithoutCategories;
    }

    @JsonProperty("Products")
    public long getProducts()
    {
        return products;
    }

    public void setProducts(long products)
    {
        this.products = products;
    }

    @JsonProperty("ProductsOnline")
    public long getProductsOnline()
    {
        return productsOnline;
    }

    public void setProductsOnline(long productsOnline)
    {
        this.productsOnline = productsOnline;
    }

    @JsonProperty("Parts")
    public long getParts()
    {
        return parts;
    }

    public void setParts(long parts)
    {
        this.parts = parts;
    }

    @JsonProperty("PartsOnline")
    public long getPartsOnline()
    {
        return partsOnline;
    }

    public void setPartsOnline(long partsOnline)
    {
        this.partsOnline = partsOnline;
    }

    @JsonProperty("Types")
    public long getTypes()
    {
        return types;
    }

    public void setTypes(long types)
    {
        this.types = types;
    }

    @JsonProperty("TypesOnline")
    public long getTypesOnline()
    {
        return typesOnline;
    }

    public void setTypesOnline(long typesOnline)
    {
        this.typesOnline = typesOnline;
    }

    @JsonProperty("ProductsIndex")
    public long getProductsSearchIndex()
    {
        return productsSearchIndex;
    }

    public void setProductsSearchIndex(long productsSearchIndex)
    {
        this.productsSearchIndex = productsSearchIndex;
    }

    @JsonProperty("CategoriesIndex")
    public long getCategoriesSearchIndex()
    {
        return categoriesSearchIndex;
    }

    public void setCategoriesSearchIndex(long categoriesSearchIndex)
    {
        this.categoriesSearchIndex = categoriesSearchIndex;
    }

}
