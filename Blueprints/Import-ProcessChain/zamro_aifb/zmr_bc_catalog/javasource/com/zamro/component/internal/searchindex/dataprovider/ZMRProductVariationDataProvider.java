package com.zamro.component.internal.searchindex.dataprovider;
import java.util.Iterator;
import java.util.Map;

import com.intershop.beehive.core.capi.domain.AttributeDefinitionConstants;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.component.mvc.internal.searchindex.dataprovider.ProductVariationDataProvider;
import com.intershop.component.search.capi.Attribute;
/**
 * @author Juergen Nuetzel
 * 
 *         Modification for Zamro: We do not skip the variations. We handle them like simple products
 *
 */
public class ZMRProductVariationDataProvider extends ProductVariationDataProvider
{
    /**
     * get the product variation data
     */
    @Override
    protected void getProductData(Product p, Map<String, Object> data)
    {
        if (p.isProductMaster())
        {
            for (Attribute a : this.getAttributes())
            {
                if (isMultipleAttributeType(a))
                {
                    this.setMode(MODE_MULTI);
                }
                else
                {
                    this.setMode(MODE_APPEND);
                }
                // add the product master attribute value to data for
                // non-variation attributes
                getExtensibleObjectData(p, a, data);
            }
            // Collect the variation attribute value
            Iterator<?> variations = p.createMasteredProductsIterator();
            int variationsOnline = 0;
            
            while(variations.hasNext()){
                Product varProduct = (Product)variations.next();
                if (!varProduct.isOnline()){
                    continue;                    
                }else{
                    variationsOnline++;
                    for (Attribute a : this.getAttributes()){
                        if (isMultipleAttributeType(a)){
                            this.setMode(MODE_MULTI);
                        }
                        else{
                            this.setMode(MODE_APPEND);
                        }
                        getExtensibleObjectData(varProduct, a, data);
                    }
                }
            }
            
            if (p.getProductVariationsCount() == 1 || variationsOnline == 1) {
                addData("useOnSearch"  , 0  , data);
            } else {
                addData("useOnSearch"  , 1  , data);
            }
            
        }
        // Modification for Zamro: We do not skip the variations. We handle them like simple products
        // else if (p.isMastered())
        // {
        // which variable variation attributes are defined?
        // Map<String, VariableVariationAttribute> vaMap = getVariableVariationAttributeMap(p.getProductMaster());
        // do some of the configured index attributes match the variation
        // attributes?
        // if yes the var atts are indexed with the master.
        // for(Attribute a : this.getAttributes())
        // {
        // if (vaMap.containsKey(a.getName()))
        // {
        // it is a variation attribute for this variation
        // data.put(OMIT_OBJECT_KEY, p.getUUID());
        // break;
        // }
        // }
        // }
        else
        {
            addData("useOnSearch"  , 1  , data);
            // indexed as a custom attribute
            getExtensibleObjectData(p, this.getAttributes(), data);
            // add existing combinations for simple products, too
            for (Attribute a : this.getAttributes())
            {
                Attribute a2 = getCombinedVariationAttribute(a);
                if (a2 != null)
                {
                    Object value1 = data.get(a.getName());
                    Object value2 = data.get(a2.getName());
                    if (value1 != null && value2 != null)
                    {
                        addData(a.getName(), value2 + "_" + value1, data, MODE_MULTI);
                        addData(a2.getName(), value1 + "_" + value2, data, MODE_MULTI);
                    }
                }
            }
            
            /*
             * Information from the Master
             */
            /**
             * Removed! not in use and causing time consuption 
             */
            /**if(p.isMastered() && p.getProductMaster() != null) {
                ProductBO productMasterBO = productRepository.getProductBOBySKU(p.getProductMaster().getSKU());
                /*
                 * defensive programmig against screwed up products....
                 * 
                 *
                if(productMasterBO != null) {
                    addData("masterSKU"  , productMasterBO.getSKU()  , data, MODE_REPLACE);
                    addData("masterName" , productMasterBO.getName() , data, MODE_REPLACE);
                }
            }
             **/
            
        }
    }
    /*
     * get the associated attribute definition from the index configuration
     */
    private Attribute getCombinedVariationAttribute(Attribute a)
    {
        if (a != null && a.get("CombinedVariationAttributeName") != null)
        {
            return this.context.getSearchIndex().getConfiguration()
                            .getAttributeByName(a.get("CombinedVariationAttributeName").toString());
        }
        return null;
    }
    private boolean isMultipleAttributeType(Attribute a)
    {
        if (a.getDataType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_STRING
                        || a.getDataType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_DOUBLE
                        || a.getDataType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_INT
                        || a.getDataType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_BOOLEAN
                        || a.getDataType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_DATE
                        || a.getDataType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_DECIMAL
                        || a.getDataType() == AttributeDefinitionConstants.ATTRIBUTE_TYPE_MULTIPLE_LONG)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
