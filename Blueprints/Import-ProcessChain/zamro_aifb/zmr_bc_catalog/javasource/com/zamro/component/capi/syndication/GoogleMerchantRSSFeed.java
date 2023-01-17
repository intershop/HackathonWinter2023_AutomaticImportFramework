package com.zamro.component.capi.syndication;

import java.util.stream.Collectors;

import com.intershop.beehive.core.capi.currency.Currency;
import com.intershop.beehive.core.capi.currency.CurrencyMgr;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.localization.LocaleMgr;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.internal.url.URLPipelineAction;
import com.intershop.beehive.foundation.quantity.Money;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.component.catalog.capi.CatalogCategoryPath;
import com.intershop.component.foundation.capi.syndication.Syndication;
import com.intershop.component.image.capi.ImageBO;
import com.intershop.component.marketing.capi.syndication.SyndFeedMarshaller;
import com.intershop.component.marketing.internal.syndication.URLRewriteHelper;
import com.intershop.component.product.capi.ProductBO;
import com.intershop.component.product.pricing.capi.PriceRecordBO;
import com.intershop.component.product.pricing.orm.internal.ProductBOPricingExtensionImpl;
import com.intershop.sellside.appbase.b2c.capi.product.ProductBOImageExtension;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.zamro.component.capi.syndication.modules.GoogleMerchantProductModule;
import com.zamro.component.capi.syndication.modules.GoogleMerchantProductModuleImpl;
import com.zamro.component.capi.syndication.modules.GoogleMerchantShippingModule;
import com.zamro.component.capi.syndication.modules.GoogleMerchantShippingModuleImpl;;

public class GoogleMerchantRSSFeed extends SyndFeedMarshaller
{
    LocaleInformation locale;
    Currency currency;
    Domain site;

    @Override
    public void init()
    {
        super.init();

        Syndication syndication = this.getSyndication();
        this.site = syndication.getDomain().getSite();

        LocaleMgr localeMgr = (LocaleMgr)NamingMgr.getInstance().lookupManager("LocaleMgr");
        String localeID = syndication.getString("LocaleID");
        this.locale = (localeID != null) ? localeMgr.getLocaleBy(localeID) : localeMgr.getLeadLocale();

        CurrencyMgr currencyMgr = (CurrencyMgr)NamingMgr.getInstance().lookupManager("CurrencyMgr");
        String currencyId = syndication.getString("CurrencyMnemonic");
        this.currency = (currencyId != null) ? currencyMgr.getCurrency(currencyId) : currencyMgr.getLeadCurrency();

        String viewingPipeline = syndication.getTarget().getParameters().get("viewingPipeline").toString();
        this.action = new URLPipelineAction(viewingPipeline, "WFS", this.site.getDomainName(),
                        this.locale.getLocaleID(), currency.getMnemonic(),
                        site.getDefaultApplication().getUrlIdentifier());
    }

    @Override
    protected void updateSyndFeed(SyndFeed feed)
    {
        feed.getModules().add(new GoogleMerchantProductModuleImpl());
    }

    @Override
    protected SyndEntry createSyndEntry(Product p)
    {
        ProductBO productBO = URLRewriteHelper.getProductBObyProduct(p);

        SyndEntry entry = super.createSyndEntry(p);
        entry.setUri(p.getSKU());

        final GoogleMerchantProductModule data = new GoogleMerchantProductModuleImpl();

        data.setId(p.getSKU());
        data.setMobileLink(entry.getLink());

        // hardcoded to in stock for now
        data.setAvailability("in stock");

        // hardcoded to new
        data.setCondition("new");

        data.setBrand(productBO.getManufacturerName());
        data.setMpn(productBO.getManufacturerSKU());
        data.setGtin(productBO.getString("EANCode"));

        // hardcoded to Bouwmaterialen > Verbruiksartikelen voor de bouw (id: 503739) for now
        data.setGoogleProductCategory("503739");

        setImage(data, productBO);
        setPrices(data, productBO);
        setShipping(data, productBO);

        CatalogCategoryPath catalogPath = productBO.getDefaultCatalogCategoryBO().getCatalogCategoryPath()
                        .getReversePath();

        String path = catalogPath.entrySet().stream().map(cat -> cat.getValue().getDisplayName(locale))
                        .collect(Collectors.joining(" > "));
        data.setProductType(path);

        entry.getModules().add(data);
        return entry;
    }

    private void setShipping(GoogleMerchantProductModule data, ProductBO productBO)
    {
        GoogleMerchantShippingModule shipping = new GoogleMerchantShippingModuleImpl();
        shipping.setCountry(locale.getCountry());

        // shipping costs are hardcoded to 0 for now...
        Money shippingPrice = Money.getZeroMoney(currency.getMnemonic());
        shipping.setPrice(shippingPrice.getValue().toString() + " " + shippingPrice.getCurrencyMnemonic());
        data.setShipping(shipping);

    }

    private void setImage(GoogleMerchantProductModule data, ProductBO productBO)
    {
        ProductBOImageExtension ext = (ProductBOImageExtension)productBO.getExtension("Image");

        ImageBO mainImage = ext.getImageContainerBO().getImageBO("L", "front");
        if (mainImage != null)
        {

            data.setImageLink(mainImage.getEffectiveUrl());
        }
    }

    private void setPrices(GoogleMerchantProductModule data, ProductBO productBO)
    {

        ProductBOPricingExtensionImpl priceBO = (ProductBOPricingExtensionImpl)productBO.getExtension("Pricing");
        PriceRecordBO listPrice = priceBO.getPrice("ListPrice", this.currency);
        
        if(listPrice.getPrice() == null || listPrice.getPrice() != null || !listPrice.getPrice().isAvailable()) {
            // no price found, return
            return;
        }
        data.setPrice(listPrice.getPrice().getValue().toString() + " " + listPrice.getPrice().getCurrencyMnemonic());

        // check if there is a salePrice, add it if found
        PriceRecordBO salePrice = priceBO.getPrice("SalePrice", this.currency);
        if (salePrice != null && salePrice.getPrice() != null && listPrice.isPriceGreaterThan(salePrice))
        {
            data.setSalePrice(salePrice.getPrice().getValue().toString() + " "
                            + salePrice.getPrice().getCurrencyMnemonic());
        }
    }
}