/**
 * 
 */
package com.zamro.component.internal.price;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.intershop.beehive.businessobject.capi.BusinessObjectExtension;
import com.intershop.component.product.capi.ProductBO;
import com.intershop.component.product.pricing.orm.internal.ProductBOPricingExtensionFactory;
import com.intershop.component.product.pricing.orm.internal.ProductBOPricingExtensionImpl;

/**
 * @author developer
 *
 */
public class ZMRProductBOPricingExtensionFactory extends ProductBOPricingExtensionFactory
{
    @Inject
    private Injector injector;
    @Override
    public BusinessObjectExtension<ProductBO> createExtension(ProductBO object) {
        ProductBOPricingExtensionImpl extensionImpl = new ZMRProductBOPricingExtensionImpl("Pricing", object);
        if (this.injector != null) {
            this.injector.injectMembers(extensionImpl);
        }

        return extensionImpl;
    }

}
