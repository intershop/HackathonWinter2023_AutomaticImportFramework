package com.zamro.component.capi.catalog;

import com.intershop.beehive.businessobject.capi.BusinessObject;
import com.intershop.beehive.businessobject.capi.BusinessObjectExtension;
import com.intershop.component.image.capi.ImageBO;

/**
 * This extension provides functions for rendering menu structures.
 * 
 * @author Juergen Nuetzel
 * 
 */
public interface CatalogCategoryBOImageExtension<B extends BusinessObject> extends BusinessObjectExtension<B>
{

    /**
     * The ID of the created extensions which can be used to get them from the
     * business object later.
     */
    public static final String EXTENSION_ID = "ZMRImage";

    /**
     * Returns fully qualified ImageBO for the categoryImage.
     * 
     * @param the imageType
     * @return the ImageBO.
     */
    public ImageBO getImageBO(String imageType);
}