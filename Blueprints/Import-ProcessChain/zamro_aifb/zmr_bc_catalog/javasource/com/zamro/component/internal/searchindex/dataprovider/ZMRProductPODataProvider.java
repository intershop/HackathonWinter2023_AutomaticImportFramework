package com.zamro.component.internal.searchindex.dataprovider;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.component.image.internal.orm.ImageReference;
import com.intershop.component.mvc.internal.searchindex.dataprovider.ProductPODataProvider;
import com.intershop.component.search.capi.Attribute;

/**
 * @author Juergen Nuetzel
 * 
 *         Adds further features (isMastered flag) to the ProductPO data provider
 *
 */
public class ZMRProductPODataProvider extends ProductPODataProvider
{

    // definition of product attributes

    public static final String IS_MASTERED = "isMastered";

    @Override
    protected void getProductData(Product product, Map<String, Object> data)
    {
        super.getProductData(product, data);

        Iterator<ImageReference> it = product.createImageReferencesIterator();

        if(it.hasNext()){
            data.put("hasImage", "1");
            while(it.hasNext()){
                ImageReference img = it.next();
                if((img.getImageView().getID().equals("front")) && (img.getImageType().getID().equals("M"))) {
                    data.put("imageURL", img.getImageType().getPrefix()+img.getImageBaseName());
                    break;
                }
            }
        } else {
            data.put("hasImage", "0");
            data.put("imageURL", "https://cdn.zamro.nl/M/not_available.png");
        }

        for (Attribute a : getAttributes())
        {
            String attributeName = a.getName();
            Object attributeValue = null;
            if (IS_MASTERED.equalsIgnoreCase(attributeName))
            {
                attributeValue = product.isMastered() ? "1" : "0";
            }

            if (attributeValue != null)
            {
                data.put(attributeName, attributeValue);
            }
        }
    }

    @Override
    public List<Attribute> getSelectableSourceAttributes()
    {
        List<Attribute> selectableAttributes = super.getSelectableSourceAttributes();

        String[] selectableAtts = { IS_MASTERED, "0", };

        for (int i = 0; i < selectableAtts.length; i += 2)
        {
            Attribute a = new Attribute();
            a.setName(selectableAtts[i]);
            a.setDisplayName(selectableAtts[i]);
            a.setDataType(Integer.valueOf(selectableAtts[i + 1]));
            selectableAttributes.add(a);

        }
        return selectableAttributes;
    }

}
