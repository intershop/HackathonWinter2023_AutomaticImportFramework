package com.zamro.adapter.ac_search_solr_ext.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.intershop.adapter.search_solr.internal.SolrConstants;
import com.intershop.beehive.businessobject.capi.BusinessObjectRepository;
import com.intershop.beehive.core.capi.app.AppContextUtil;
import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.domain.AttributeDefinitionConstants;
import com.intershop.beehive.core.capi.domain.AttributeHelper;
import com.intershop.beehive.core.capi.domain.AttributeValue;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.domain.DomainMgr;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.beehive.xcs.capi.product.ProductMgr;
import com.intershop.beehive.xcs.capi.productvariation.VariableVariationAttribute;
import com.intershop.component.application.capi.ApplicationBO;
import com.intershop.component.mvc.capi.filter.CatalogFilter;
import com.intershop.component.mvc.capi.filter.CatalogFilterMgr;
import com.intershop.component.mvc.capi.filter.CatalogFilterViewingHelper;
import com.intershop.component.mvc.capi.searchindex.ProductIndexAttributes;
import com.intershop.component.search.capi.Attribute;
import com.intershop.component.search.capi.SearchIndexQuery;
import com.intershop.component.search.capi.SearchIndexQuery.AttributeCondition;
import com.intershop.component.search.capi.SearchResultMapper;
import com.intershop.component.search.internal.SearchIndexBaseImpl;

/**
 * resolves a product using the index domain and the product uuid returned from
 * search result resolver.
 * 
 * It is a modified copy of ResolveProduct from com.intershop.adapter.search_solr.internal 
 * 
 */
public class ZMRResolveProduct extends SearchResultMapper
{
    private ProductMgr pMgr;
    private Domain repositoryDomain;

    /**
     * Creates an instance of ResolveProduct.
     */
    public ZMRResolveProduct()
    {
        super();
        pMgr = (ProductMgr)NamingMgr.getInstance().lookupManager(ProductMgr.REGISTRY_NAME);
    }

    /**
     * Resolves a Product instance for a given uuid.
     * 
     * @param productUUID
     *            a product uuid
     * @return a Product instance for the uuid
     * @exception NoSuchElementException
     *                in case no product was found for given productUUID
     */
    @Override
    public Object resolve(Object productUUID) throws NoSuchElementException
    {
        if(repositoryDomain == null)
        {
            repositoryDomain = getRepositoryDomain();
            if(repositoryDomain == null)
            {
                repositoryDomain = getSearchIndex().getDomain(); 
            }
        }
        
        SearchIndexQuery query = getSearchResult().getQuery();
        Product result = null;
        String sku = null; // in case of a single product result we store the trimmed search term
        
        // ENFINITY-19126: assume search term is SKU
        if (getSearchResult().getPageable().getElementCount() == 1 && 
            query != null && query.getQueryTerm() != null)
        {
            try
            {
                sku=query.getQueryTerm().trim().toUpperCase();
                if (sku != null){
                  if (sku.length() != 0&&sku.startsWith("\""))
                  {
                    sku = sku.substring(1, sku.length());
                  }
                  if (sku.length() != 0&&sku.endsWith("\""))
                  {
                    sku = sku.substring(0, sku.length() - 1);
                  }                
                }
                result = pMgr.getProductBySKU(sku, repositoryDomain);
                if (null != result)
                {
                    // search term is SKU
                    if(checkOnlineCondition(query, result) && checkCatalogFilterCondition(query, result))
                    {
                        return result;
                    }
                }
            }
            catch(SystemException ex)
            {
            }
        }
        
        result = pMgr.resolveProductFromID((String)productUUID, repositoryDomain);
        if (result == null)
        {
            if(getSearchIndex() instanceof SearchIndexBaseImpl && ((SearchIndexBaseImpl)getSearchIndex()).isShared())
            {
                Logger.error(this, "SearchIndex '{}' is inconsistent or shared incorrectly. Product with uuid='{}' not found. ", 
                                this.getSearchIndex().getIndexID(), productUUID);
            }
            else
            {
                Logger.error(this, "SearchIndex '{}' is inconsistent. Product with uuid='{}' not found in DB.", 
                                this.getSearchIndex().getIndexID(), productUUID);
            }

            throw new NoSuchElementException();
        }
        
        if(result.isProductMaster())
        {
            
            //check for conditions matching a variable variation attribute 
            //result.createVariableVariationAttributesIterator();
            Map<String, VariableVariationAttribute> vaMap = getVariableVariationAttributeMap(result);
            
            
            query = getSearchResult().getQuery();
            if(query.getConditions() != null && query.getConditions().size() > 0)
            {
                Map<String, AttributeCondition> vaConditions = new HashMap<String, AttributeCondition>(); 
                List<AttributeCondition> conditions = query.getConditions(); 
                for(AttributeCondition condition:conditions)
                {
                    if(vaMap.containsKey(condition.getAttributeName()))
                    {
                        vaConditions.put(condition.getAttributeName(), condition); 
                    }
                }
                
                /*if(vaConditions.size()>0 || (sku != null)) // modified for Zamro
                {
                    Iterator<Product> variations = result.createMasteredProductsIterator(); 
                    
                    
                    Product productMatch = null;
                    int maxMatchCount = 0; 
                    while(variations.hasNext())
                    {
                        Product varProduct = variations.next();
                        // this if branch added for Zamro
                        if (sku != null && varProduct.getManufacturerSKU() != null && varProduct.getManufacturerSKU().equals(sku))                            
                        {
                            return varProduct; 
                        }
                        if (sku != null && varProduct.getAttribute("SupplierSKU") != null && varProduct.getAttribute("SupplierSKU").equals(sku))                                  
                        {
                            return varProduct; 
                        }
                        if (sku != null && varProduct.getAttribute("EANCode") != null && varProduct.getAttribute("EANCode").equals(sku))                            
                        {
                            return varProduct; 
                        }
                        if(checkOnlineCondition(query, varProduct) && checkCatalogFilterCondition(query, varProduct))
                        {
                            //find a variation matching the conditions
                            int matchCount=0; 
                            for(Map.Entry<String, AttributeCondition> vaCondition:vaConditions.entrySet())
                            {
                                //match condition to attribute value
                                
                                if(isVariationProductMatchingCondition(varProduct, vaCondition))
                                {
                                    matchCount++; 
                                    if(matchCount > maxMatchCount)
                                    {
                                        maxMatchCount = matchCount; 
                                        productMatch = varProduct; 
                                    }
                                }
                            }
                            if(maxMatchCount == vaConditions.size() && productMatch != null)
                            {
                                return productMatch; 
                            }
                        }
                    }
                    if(productMatch != null)
                    {
                        return productMatch; 
                    }
                }
                else
                {
                    //no variation conditions, return master
                    return result; 
                }*/
                return result;
            }
        }

        return result;
    }

    private Domain getRepositoryDomain()
    {
        ApplicationBO applicationBO = AppContextUtil.getCurrentAppContext().getVariable(ApplicationBO.CURRENT);
        BusinessObjectRepository boRepository = applicationBO.getRepository("ProductBORepository");
        String repositoryID = boRepository.getRepositoryID();
        Domain repositoryDomain = NamingMgr.getManager(DomainMgr.class).getDomainByUUID(repositoryID); 
        return repositoryDomain; 
    }

    /**
     * checks if a product is satisfying the online flag configuration of the index 
     * and the condition of the query
     * 
     * @param query
     * @param product
     * @return <code>true</code> if the conditions are met and the product can be returned as a resolved product  
     */
    private boolean checkOnlineCondition(SearchIndexQuery query, Product product)
    {
        if(getSearchIndex().getConfiguration().getAttributeByName(ProductIndexAttributes.ONLINE) != null)
        {
            //index can contain offline and online products
            if(query.getCondition(ProductIndexAttributes.ONLINE) != null)
            {
                AttributeCondition ac = query.getCondition(ProductIndexAttributes.ONLINE); 
                if("1".equals(ac.getAttributeValue()) && product.isOnline())
                {
                        return true;
                }
                else if("0".equals(ac.getAttributeValue()) && !product.isOnline())
                {
                    return true;
                }
            }
            else
            {
                //no online condition, return regardless of online/offline
                return true; 
            }
        }
        else
        {
            //only online products
            if(product.isOnline())
            {
                return true; 
            }
        }
        
        return false;
    }

    private boolean isVariationProductMatchingCondition(Product varProduct,
                    Entry<String, AttributeCondition> vaCondition)
    {
        List<String> values = vaCondition.getValue().getValues();
        if (values.size() > 1)
        {
            // TODO multi value condition

            return false;
        }
        else if (values.size() == 1)
        {
            String sourceAttributeName = vaCondition.getKey();
            // check source attribute name
            Attribute a = getSearchIndex().getConfiguration().getAttributeByName(vaCondition.getKey());

            if (a != null && a.get("SourceAttributeName") != null)
            {
                sourceAttributeName = a.get("SourceAttributeName").toString();
            }

            AttributeValue av = varProduct.getAttributeValue(sourceAttributeName);
            if (av == null)
            {
                return false;
            }
            if (av.getType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_STRING)
            {
                Iterator<String> valueIt = (Iterator<String>)av.getObject();
                while(valueIt.hasNext())
                {
                    String avStrValue = valueIt.next();
                    if (avStrValue.equals(values.get(0)))
                    {
                        return true;
                    }
                }
            }
            else
            {
                String avStrValue = AttributeHelper.toStringValue(av);
                if (avStrValue.equals(values.get(0)))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private Map<String, VariableVariationAttribute> getVariableVariationAttributeMap(Product p)
    {
        Iterator<?> varAttIt = p.createVariableVariationAttributesIterator();
        Map<String, VariableVariationAttribute> vaMap = new HashMap<String, VariableVariationAttribute>();
        while(varAttIt.hasNext())
        {
            VariableVariationAttribute va = (VariableVariationAttribute)varAttIt.next();
            vaMap.put(va.getName(), va);
        }
        return vaMap;
    }
    
    /*
     * checks catalog filter condition for a given product
     */
    private boolean checkCatalogFilterCondition(SearchIndexQuery query, Product product)
    {
        if (getSearchIndex().getConfiguration().getAttributeByName(SolrConstants.CATALOG_FILTER_IDs) != null)
        {
            AttributeCondition catalogFilterCondition = query.getCondition(SolrConstants.CATALOG_FILTER_IDs);
            if (catalogFilterCondition != null)
            {
                CatalogFilterMgr filterMgr = NamingMgr.getManager(CatalogFilterMgr.class);
                Iterator<String> catalogFilterIDs = catalogFilterCondition.getValues().iterator();
                boolean visible = false;
                while(catalogFilterIDs.hasNext())
                {
                    String filterID = catalogFilterIDs.next();
                    CatalogFilter filter = filterMgr.getCatalogFilter(filterID);
                    if (new CatalogFilterViewingHelper(filter).isCurrentlyVisible(product))
                    {
                        visible = true;
                        // break on first founded catalog view where the product
                        // is visible because if a product is included at least
                        // in 1 view - it is visible
                        break;
                    }
                }

                return visible;
            }
        }

        return true;
    }

}
