package com.zamro.component.capi.syndication.modules;

import java.util.Collections;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;

public class GoogleMerchantModuleGenerator implements ModuleGenerator
{

    private static final Namespace NAMESPACE = Namespace.getNamespace(GoogleMerchantProductModule.NS_PREFIX,
                    GoogleMerchantProductModule.URI);
    private static final Set<Namespace> NAMESPACES = Collections.singleton(NAMESPACE);

    @Override
    public void generate(final Module module, final Element element)
    {

        final GoogleMerchantProductModule myModule = (GoogleMerchantProductModule)module;
        addIfNotBlank(element, myModule.getCondition(), GoogleMerchantProductModule.CONDITION_TAG_NAME);
        addIfNotBlank(element, myModule.getImageLink(), GoogleMerchantProductModule.IMAGE_LINK_TAG_NAME);
        addIfNotBlank(element, myModule.getAvailability(), GoogleMerchantProductModule.AVAILABILITY_TAG_NAME);
        addIfNotBlank(element, myModule.getMobileLink(), GoogleMerchantProductModule.MOBILE_LINK_TAG_NAME);
        addIfNotBlank(element, myModule.getPrice(), GoogleMerchantProductModule.PRICE_TAG_NAME);
        addIfNotBlank(element, myModule.getSalePrice(), GoogleMerchantProductModule.SALE_PRICE_TAG_NAME);
        addIfNotBlank(element, myModule.getSalePriceEffectiveDate(),
                        GoogleMerchantProductModule.SALE_PRICE_EFFECTIVE_TAG_NAME);
        addIfNotBlank(element, myModule.getBrand(), GoogleMerchantProductModule.BRAND_TAG_NAME);
        addIfNotBlank(element, myModule.getGtin(), GoogleMerchantProductModule.GTIN_TAG_NAME);
        addIfNotBlank(element, myModule.getMpn(), GoogleMerchantProductModule.MPN_TAG_NAME);
        addIfNotBlank(element, myModule.getId(), GoogleMerchantProductModule.ID_TAG_NAME);
        addIfNotBlank(element, myModule.getProductType(), GoogleMerchantProductModule.PRODUCT_TYPE_TAG_NAME);
        addIfNotBlank(element, myModule.getGoogleProductCategory(),
                        GoogleMerchantProductModule.GOOGLE_PRODUCT_CATEGORY_TAG_NAME);
        addShippingIfNotNull(element, myModule.getShipping(), GoogleMerchantProductModule.SHIPPING_TAG_NAME);
    }

    private void addShippingIfNotNull(Element parent, GoogleMerchantShippingModule shipping, String tag)
    {
        if (shipping != null)
        {
            final Element shippingElement = new Element(tag, NAMESPACE);
            addIfNotBlank(shippingElement, shipping.getPrice(), GoogleMerchantShippingModule.SHIPPING_PRICE_TAG_NAME);
            addIfNotBlank(shippingElement, shipping.getCountry(),
                            GoogleMerchantShippingModule.SHIPPING_COUNTRY_TAG_NAME);
            addIfNotBlank(shippingElement, shipping.getService(),
                            GoogleMerchantShippingModule.SHIPPING_SERVICE_TAG_NAME);
            add(parent, shippingElement);
        }

    }

    private void addIfNotNull(final Element element, final Object value, final String tag)
    {
        if (value != null)
        {
            add(element, String.valueOf(value), tag);
        }
    }

    private void addIfNotBlank(final Element parent, final String value, final String tag)
    {
        if (value != null && !"".equals(value.trim()))
        {
            add(parent, value, tag);
        }
    }

    private void add(final Element parent, final String value, final String tag)
    {
        final Element child = new Element(tag, NAMESPACE);
        child.setText(value);
        add(parent, child);
    }

    private void add(final Element parent, final Element child)
    {
        parent.addContent(child);
    }

    @Override
    public String getNamespaceUri()
    {
        return GoogleMerchantProductModule.URI;
    }

    @Override
    public Set<?> getNamespaces()
    {
        return NAMESPACES;
    }

}
