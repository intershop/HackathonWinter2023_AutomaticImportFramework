package com.zamro.component.internal.catalog;

import com.intershop.beehive.businessobject.capi.AbstractBusinessObjectExtensionFactory;
import com.intershop.component.catalog.capi.CatalogCategoryBO;

/**
 * Factory for the catalog category menu extension.
 * 
 * @author Juergen Nuetzel
 * 
 */
public class CatalogCategoryBOImageExtensionFactory extends AbstractBusinessObjectExtensionFactory<CatalogCategoryBO>
{
    /**
     * Creates a new CatalogCategoryBOMenuExtension object.
     * 
     * @param CatalogCategoryBO
     *            The catalog category business object.
     * @return The catalog category menu extension impl.
     */
    public CatalogCategoryBOImageExtensionImpl createExtension(CatalogCategoryBO catalogCategoryBO)
    {
        return new CatalogCategoryBOImageExtensionImpl(catalogCategoryBO);
    }

    /**
     * Gets the extended type.
     * 
     * @return the extended type
     */
    public Class<CatalogCategoryBO> getExtendedType()
    {
        return CatalogCategoryBO.class;
    }
}