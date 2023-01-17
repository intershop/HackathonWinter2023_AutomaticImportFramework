package com.zamro.adapter.ac_search_solr_ext.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.solr.common.SolrDocument;

import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.foundation.quantity.Money;
import com.intershop.beehive.foundation.quantity.Quantity;
import com.intershop.beehive.xcs.common.ProductRef;

/**
 * @author Joost van der Land
 *
 * Class resembling a SOLR product document
 *
 */
public class ZMRSOLRProduct
{
    
    public final static String IMAGE_LINK_NOT_AV    = "https://cdn.zamro.nl/M/not_available.png";
    
    private SolrDocument _delegate;
    private String UUID;
    private String masterUUID;
    private boolean isOnline;
    private Money listPrice;
    private Money salePrice;
    private int numberOfVariants;
    private String imageUrl;
    private boolean isMastered;
    private boolean isProductMaster;
    private boolean isRetailSet;
    private String masterName;
    private String masterSKU;
    private String SKU;
    private String name;
    private String manufacturerName;
    private String unitDescription;
    private List<String> categoryPath;
    private ProductRef ref;
    private Quantity stepQuantity;
    private Quantity minOrderQuantity;
    private Quantity maxOrderQuantity;
    //private ProductBO prodBO;
    //private PriceRecordBO salesPriceForCalculation;
    private Money grossPrice;
    private Money grossListPrice;
    private String EANCode;
    private Domain domain;
    private HashMap<String, String> customAttrs;
    private Integer BUP_Value;
    private String BUP_UOM;
    private Double BUP_Conversion;
    private String label_our_choice;

    public ZMRSOLRProduct(SolrDocument solrDoc) {
        this._delegate = solrDoc;
        this.customAttrs = new HashMap<String, String>();
    }
    
    /*
     * Business Logic methods. Makes it easier for templates....
     */
    
    public String getRootCategoryName() {
        if(categoryPath == null || categoryPath.size() == 0) return null;
        
        return categoryPath.get(0);
    }
    
    /**
     * @return the lowest leaf to which this product is assigned
     */
    public String getLeafCategoryName() {
        if(categoryPath == null || categoryPath.size() == 0) return null;
        
        return categoryPath.get(categoryPath.size() -1);
    }
    
    /**
     * Can be used to determine if a striketrough price should be shown
     * @return
     */
    public boolean isSalepriceLowerThenListPrice() {
        if(listPrice == null || salePrice == null || !listPrice.isAvailable() || !salePrice.isAvailable()) return false;
        return listPrice.getValue().compareTo(salePrice.getValue()) > 0;
        
    }
    
    public boolean isEndOfLife() {
        return false;
    }
    
    /**
     * Used in templates for cache control
     * @return
     */
    public Collection<String> getCacheKeys() {
        List<String> keys = new ArrayList<String>();
        if(SKU != null) keys.add(SKU);
        if(masterSKU != null) keys.add(masterSKU);
        if(UUID != null) keys.add(UUID);
        if(masterUUID != null) keys.add(masterUUID);
        return keys;
    }
    
    /**
     * Convience method to get any unmapped field from to original SOLR document
     * @param field
     * @return
     */
    public String getField(String field)
    {
        return (String) _delegate.get(field);
    }
    
    public Money getGrossPriceRoundedUpWithMinQuantity(String locale){
        BigDecimal priceValue = this.getSalePrice().getValue();
        priceValue = priceValue.multiply(this.getMinOrderQuantity().getValue());
        BigDecimal taxRate = new BigDecimal(21);
        if(locale.equalsIgnoreCase("nl_NL") || locale.equalsIgnoreCase("nl_BE")){
            taxRate = new BigDecimal(1.21);
        }else if(locale.equalsIgnoreCase("de_DE") || locale.equalsIgnoreCase("de_AT")){
            taxRate = new BigDecimal(1.19);
        }
        priceValue = priceValue.multiply(taxRate);
        return new Money(this.getSalePrice().getCurrencyMnemonic(), priceValue.setScale(2, RoundingMode.CEILING));
    }


    /*
     * property getters / setters
     */
    
    public boolean isMastered()
    {
        return isMastered;
    }
    
    public void isMastered(boolean isMastered) {
        this.isMastered = isMastered;
    }
    
    public boolean isOnline()
    {
        return isOnline;
    }
    public void setOnline(boolean isOnline)
    {
        this.isOnline = isOnline;
    }
    public Money getListPrice()
    {
        return listPrice;
    }
    public void setListPrice(Money listPrice)
    {
        this.listPrice = listPrice;
    }
    public Money getSalePrice()
    {
        return salePrice;
    }
    public void setSalePrice(Money salePrice)
    {
        this.salePrice = salePrice;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getSKU()
    {
        return SKU;
    }

    public void setSKU(String sKU)
    {
        SKU = sKU;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getNumberOfVariants()
    {
        return numberOfVariants;
    }

    public void setNumberOfVariants(int numberOfVariants)
    {
        this.numberOfVariants = numberOfVariants;
    }

    public String getManufacturerName()
    {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName)
    {
        this.manufacturerName = manufacturerName;
    }

    public String getMasterSKU()
    {
        return masterSKU;
    }

    public void setMasterSKU(String masterSKU)
    {
        this.masterSKU = masterSKU;
    }

    public List<String> getCategoryPath()
    {
        return categoryPath;
    }

    public void setCategoryPath(List<String> categoryPath)
    {
        this.categoryPath = categoryPath;
    }

    public String getMasterName()
    {
        return masterName;
    }

    public void setMasterName(String masterName)
    {
        this.masterName = masterName;
    }

    public String getUnitDescription()
    {
        return unitDescription;
    }

    public void setUnitDescription(String unitDescription)
    {
        this.unitDescription = unitDescription;
    }

    public boolean isProductMaster()
    {
        return isProductMaster;
    }

    public void isProductMaster(boolean isProductMaster)
    {
        this.isProductMaster = isProductMaster;
    }

    public void setProductRef(ProductRef ref)
    {
        this.ref = ref;        
    }
    public ProductRef getProductRef() {
        return this.ref;
    }

    public Quantity getStepQuantity()
    {
        return stepQuantity;
    }

    public void setStepQuantity(Quantity stepQuantity)
    {
        this.stepQuantity = stepQuantity;
    }

    public Quantity getMinOrderQuantity()
    {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(Quantity minOrderQuantity)
    {
        this.minOrderQuantity = minOrderQuantity;
    }

    public Quantity getMaxOrderQuantity()
    {
        return maxOrderQuantity;
    }

    public void setMaxOrderQuantity(Quantity maxOrderQuantity)
    {
        this.maxOrderQuantity = maxOrderQuantity;
    }

    public boolean isRetailSet()
    {
        return isRetailSet;
    }

    public void isRetailSet(boolean isRetailSet)
    {
        this.isRetailSet = isRetailSet;
    }

    public String getUUID()
    {
        return UUID;
    }

    public void setUUID(String uUID)
    {
        UUID = uUID;
    }

    public String getMasterUUID()
    {
        return masterUUID;
    }

    public void setMasterUUID(String masterUUID)
    {
        this.masterUUID = masterUUID;
    }
    /*
    public void setProductBO()
    {
        ApplicationBO applicationBO = AppContextUtil.getCurrentAppContext().getVariable(ApplicationBO.CURRENT);
        ProductBORepository boRepository = applicationBO.getRepository("ProductBORepository");
        this.prodBO = boRepository.getProductBOBySKU(this.SKU);
          
    }
    public ProductBO getProductBO()
    {
        return this.prodBO;
    }
    
   public void setSalesPriceForCalculation(){
        @SuppressWarnings("deprecation")
        CurrencyMgr currencyMgr = (CurrencyMgr)NamingMgr.getInstance().lookupManager("CurrencyMgr");
        ProductBOPricingExtensionImpl priceBO = (ProductBOPricingExtensionImpl)this.prodBO.getExtension("Pricing");
        String currencyMnemonic = "EUR";
        if(this.salePrice != null && this.salePrice.isAvailable() && this.salePrice.getCurrencyMnemonic() != null){
            currencyMnemonic = this.salePrice.getCurrencyMnemonic();
        }
        this.salesPriceForCalculation = priceBO.getPrice("SalePrice", currencyMgr.getCurrency(currencyMnemonic));  
   }
    
   public PriceRecordBO getSalesPriceForCalculation(){
        return this.salesPriceForCalculation;
    }*/
   public void setGrossPrice(Double taxRateTemp){
       BigDecimal taxRate = BigDecimal.valueOf(taxRateTemp).add(BigDecimal.valueOf(1));
       if(null != salePrice){
           BigDecimal grossSalePriceValue = salePrice.getValue();
           // Only if it's a net price, apply the tax
           grossSalePriceValue = grossSalePriceValue.multiply(taxRate);
           this.grossPrice= new Money(salePrice.getCurrencyMnemonic(), grossSalePriceValue.setScale(2, RoundingMode.CEILING)); 
       }
       if(null != listPrice){
           BigDecimal grossListPriceValue = listPrice.getValue();
           // Only if it's a net price, apply the tax
           grossListPriceValue = grossListPriceValue.multiply(taxRate);
           this.grossListPrice = new Money(listPrice.getCurrencyMnemonic(), grossListPriceValue.setScale(2, RoundingMode.CEILING));
       }
   }
   
   public Money getGrossPrice(){
       return this.grossPrice;
   }
   
   
   
   public Money getGrossListPrice(){
       return this.grossListPrice;
   }

   public String getEANCode(){
       return EANCode;
   }
    
   public void setEANCode(String eANCode){
       EANCode = eANCode;
   }
   public void setDomain(Domain domain){
       this.domain=domain;
   }
   public Domain getDomain(){
       return this.domain;
   }

   public HashMap<String, String> getCustomAttrs(){
       return customAttrs;
   }

   public void setCustomAttrs(HashMap<String, String> customAttrs){
       this.customAttrs = customAttrs;
   }

    public Integer getBUP_Value()
    {
        return BUP_Value;
    }

    public void setBUP_Value(Integer bUP_Value)
    {
        BUP_Value = bUP_Value;
    }

    public String getBUP_UOM()
    {
        return BUP_UOM;
    }

    public void setBUP_UOM(String bUP_UOM)
    {
        BUP_UOM = bUP_UOM;
    }

    public Double getBUP_Conversion()
    {
        return BUP_Conversion;
    }

    public void setBUP_Conversion(Double bUP_Conversion)
    {
        BUP_Conversion = bUP_Conversion;
    }

    public String getLabel_our_choice()
    {
        return label_our_choice;
    }

    public void setLabel_our_choice(String label_our_choice)
    {
        this.label_our_choice = label_our_choice;
    }

}