package com.zamro.component.capi.syndication.modules;

import com.sun.syndication.feed.module.ModuleImpl;

public class GoogleMerchantProductModuleImpl extends ModuleImpl implements GoogleMerchantProductModule
{

    private static final long serialVersionUID = 3387352269727404587L;
    private String _brand;
    private String _gtin;
    private String _mpn;
    private String _imageLink;
    private String _price;
    private String _salePrice;
    private String _salePriceEffectiveDate;
    private String _condition;
    private String _availability;
    private String _id;
    private String _mobileLink;
    private String _productType;
    private String _googleProductCategory;
    private String _masterLink;
    private String _masterName;
    
    private GoogleMerchantShippingModule _shipping;

    public GoogleMerchantProductModuleImpl()
    {
        super(GoogleMerchantProductModule.class, URI);
    }

    @Override
    public String getImageLink()
    {
        return _imageLink;
    }

    @Override
    public void setImageLink(final String imageLink)
    {
        _imageLink = imageLink;
    }

    @Override
    public String getPrice()
    {
        return _price;
    }

    @Override
    public void setPrice(final String price)
    {
        _price = price;
    }

    @Override
    public String getCondition()
    {
        return _condition;
    }

    @Override
    public void setCondition(final String condition)
    {
        _condition = condition;
    }

    @Override
    public String getId()
    {
        return _id;
    }

    @Override
    public void setId(final String id)
    {
        _id = id;
    }

    @Override
    public void copyFrom(final Object other)
    {
        if (!(other instanceof GoogleMerchantProductModule))
        {
            throw new IllegalArgumentException(
                            "Expected other to be of class " + GoogleMerchantProductModule.class.getSimpleName()
                                            + " but was " + other.getClass().getSimpleName());
        }
        final GoogleMerchantProductModule otherModule = (GoogleMerchantProductModule)other;
        setImageLink(otherModule.getImageLink());
        setPrice(otherModule.getPrice());
        setCondition(otherModule.getCondition());
        setId(otherModule.getId());
        setAvailability(otherModule.getAvailability());
        setMobileLink(otherModule.getMobileLink());
        setSalePrice(otherModule.getSalePrice());
        setSalePriceEffectiveDate(otherModule.getSalePriceEffectiveDate());
        setShipping(otherModule.getShipping());
        setBrand(otherModule.getBrand());
        setGtin(otherModule.getGtin());
        setMpn(otherModule.getMpn());
        setProductType(otherModule.getProductType());
        setGoogleProductCategory(otherModule.getGoogleProductCategory());
    }

    @Override
    public Class<?> getInterface()
    {
        return GoogleMerchantProductModule.class;
    }

    @Override
    public String getAvailability()
    {
        return _availability;
    }

    @Override
    public void setAvailability(final String availability)
    {
        _availability = availability;
    }

    @Override
    public void setMobileLink(String mobileLink)
    {
        _mobileLink = mobileLink;
    }

    @Override
    public String getMobileLink()
    {
        return _mobileLink;
    }

    @Override
    public String getSalePrice()
    {
        return _salePrice;
    }

    @Override
    public void setSalePrice(String salePrice)
    {
        _salePrice = salePrice;
    }

    @Override
    public String getSalePriceEffectiveDate()
    {
        return _salePriceEffectiveDate;
    }

    @Override
    public void setSalePriceEffectiveDate(String salePriceEffectiveDate)
    {
        _salePriceEffectiveDate = salePriceEffectiveDate;
    }

    @Override
    public GoogleMerchantShippingModule getShipping()
    {
        return _shipping;
    }

    @Override
    public void setShipping(GoogleMerchantShippingModule shipping)
    {
        _shipping = shipping;
    }

    @Override
    public void setBrand(String brand)
    {
        _brand = brand;
    }

    @Override
    public String getBrand()
    {
        return _brand;
    }

    @Override
    public void setGtin(String gtin)
    {
        _gtin = gtin;
    }

    @Override
    public String getGtin()
    {
        return _gtin;
    }

    @Override
    public void setMpn(String mpn)
    {
        _mpn = mpn;
    }

    @Override
    public String getMpn()
    {
        return _mpn;
    }

    @Override
    public void setProductType(String productType)
    {
        _productType = productType;
    }

    @Override
    public String getProductType()
    {
        return _productType;
    }

    @Override
    public void setGoogleProductCategory(String googleProductCategory)
    {
        _googleProductCategory = googleProductCategory;
    }

    @Override
    public String getGoogleProductCategory()
    {
        return _googleProductCategory;
    }
}