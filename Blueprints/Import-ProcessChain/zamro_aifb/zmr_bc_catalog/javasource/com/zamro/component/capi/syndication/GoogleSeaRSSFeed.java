package com.zamro.component.capi.syndication;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.internal.template.AbstractTemplate;
import com.intershop.beehive.core.internal.url.URLCompositionAdapter;
import com.intershop.beehive.core.internal.url.URLParameterSet;
import com.intershop.beehive.core.internal.url.URLPipelineAction;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.component.catalog.capi.CatalogCategoryBO;
import com.intershop.component.catalog.capi.CatalogCategoryPath;
import com.intershop.component.marketing.internal.syndication.URLRewriteHelper;
import com.intershop.component.product.capi.ProductBO;
import com.intershop.component.product.pricing.capi.PriceRecordBO;
import com.intershop.component.product.pricing.orm.internal.ProductBOPricingExtensionImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.zamro.component.capi.syndication.modules.GoogleSeaCategoryModule;
import com.zamro.component.capi.syndication.modules.GoogleSeaCategoryModuleImpl;
import com.zamro.component.capi.syndication.modules.GoogleSeaProductModule;
import com.zamro.component.capi.syndication.modules.GoogleSeaProductModuleImpl;

public class GoogleSeaRSSFeed extends GoogleMerchantRSSFeed
{
    private URLPipelineAction catAction;
    private URLCompositionAdapter catAdapter;
    private String protocol;
    private String host;
    private String port;
    
    @Override
    public void init()
    {
        super.init();
        
        /*
         * Setup for URL rewriting for catalog's
         */
        catAction = new URLPipelineAction("ViewStandardCatalog-Browse", "WFS", this.site.getDomainName(),
                        this.locale.getLocaleID(), currency.getMnemonic(),
                        site.getDefaultApplication().getUrlIdentifier());
        
        catAdapter = URLCompositionAdapter.getURLCompositionAdapter(getSyndication().getDomain().getSite().getDefaultApplication(), true);
        protocol = "http";
        URL standardURL = catAdapter.getWebServerURL(protocol);
        host = standardURL.getHost();
        port = (standardURL.getPort() != -1 ? String.valueOf(standardURL.getPort()) : String.valueOf(catAdapter.getPort()));
    }
    
    @Override
    protected void updateSyndFeed(SyndFeed feed)
    {
        feed.getModules().add(new GoogleSeaProductModuleImpl());
    }

    @Override
    protected SyndEntry createSyndEntry(Product p)
    {
        ProductBO productBO = URLRewriteHelper.getProductBObyProduct(p);
        
        SyndEntry entry = super.createSyndEntry(p);

        final GoogleSeaProductModule data = new GoogleSeaProductModuleImpl();

        CatalogCategoryPath catalogPath = productBO.getDefaultCatalogCategoryBO().getCatalogCategoryPath()
                        .getReversePath();
        String path = catalogPath.entrySet().stream().map(cat -> cat.getValue().getDisplayName(locale))
                        .collect(Collectors.joining(" > "));
        data.setCategoryPath(path);

        setSeaCategories(data, catalogPath);

        setDiscount(data, productBO);

        if(p.isMastered()) {
            Product master = p.getProductMaster();
            SyndEntry masterEntry = super.createSyndEntry(master);
            data.setMasterId(master.getSKU());
            data.setMasterName(masterEntry.getTitle());
            data.setMasterLink(masterEntry.getLink());
        }
        
        entry.getModules().add(data);

        return entry;
    }

    /**
     * Sets the discount (between list price and saleprice) as percentage
     * 
     * @param data
     * @param productBO
     */
    private void setDiscount(GoogleSeaProductModule data, ProductBO productBO)
    {

        ProductBOPricingExtensionImpl priceBO = (ProductBOPricingExtensionImpl)productBO.getExtension("Pricing");
        PriceRecordBO listPrice = priceBO.getPrice("ListPrice", this.currency);
        
        if(listPrice == null || listPrice.getPrice() == null || !listPrice.getPrice().isAvailable()) {
            // no list price found
            return;
        }
        
        // check if there is a salePrice that is less than the list price and calculate the percentage discount
        PriceRecordBO salePrice = priceBO.getPrice("SalePrice", this.currency);
        if (salePrice != null && salePrice.getPrice() != null && salePrice.getPrice().isAvailable() && listPrice.isPriceGreaterThan(salePrice))
        {
            // discount = (1 - ( salePrice / listPrice)) * 100
            BigDecimal fraction = salePrice.getPrice().getValue().divide(listPrice.getPrice().getValue(), 4,
                            BigDecimal.ROUND_HALF_UP);
            BigDecimal perc = new BigDecimal(1).subtract(fraction).multiply(new BigDecimal(100));
            data.setDiscount(perc.toPlainString());
        }
    }

    /**
     * Sets the Main catagory tag and sets a Sub (SubSub, SubSubSub, etc) category tag for each sub-catagory
     * 
     * 
     * @param data
     * @param catalogPath
     */
    private void setSeaCategories(GoogleSeaProductModule data, CatalogCategoryPath catalogPath)
    {
        // generate a category tag for each category
        List<GoogleSeaCategoryModule> categories = new ArrayList<GoogleSeaCategoryModule>();

        Iterator<CatalogCategoryBO> it = catalogPath.values().iterator();
        boolean first = true;
        String subPostFix = "";
        while(it.hasNext())
        {
            String catTagName;
            CatalogCategoryBO categoryBO = it.next();
            if (first)
            {
                catTagName = GoogleSeaCategoryModule.CATEGORY_TAG_NAME + GoogleSeaCategoryModule.CATEGORY_MAIN_POSTFIX;
                first = false;
            }
            else
            {
                subPostFix += GoogleSeaCategoryModule.CATEGORY_SUB_POSTFIX;
                catTagName = GoogleSeaCategoryModule.CATEGORY_TAG_NAME + subPostFix;
            }
            
            GoogleSeaCategoryModule catName = new GoogleSeaCategoryModuleImpl(catTagName,
                            categoryBO.getDisplayName(locale));
            
            
            GoogleSeaCategoryModule catLink = new GoogleSeaCategoryModuleImpl(
                            catTagName + GoogleSeaCategoryModule.CATEGORY_LINK_POSTFIX, createCatalogURL(categoryBO));

            categories.add(catName);
            categories.add(catLink);

        }

        data.setCategories(categories);
    }
    
    private String createCatalogURL(CatalogCategoryBO c) {
        URLParameterSet params = new URLParameterSet();
        params.addURLParameter("CategoryDomainName", c.getDomain().getDomainName());
        params.addURLParameter("CategoryName", c.getName());
        PipelineDictionary dict = AbstractTemplate.getTemplateExecutionConfig().getPipelineDictionary();
        dict.put("CategoryBO", c);
        URLRewriteHelper.setDictionaryValue("CategoryBO", c);   
        String url = catAdapter.createURL(false, protocol, host, port, catAction, params);
        URLRewriteHelper.removeDictionaryValue("CategoryBO");
        return url;
    }
}
