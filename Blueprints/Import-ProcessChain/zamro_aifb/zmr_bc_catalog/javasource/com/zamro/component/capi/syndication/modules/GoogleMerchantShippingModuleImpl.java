package com.zamro.component.capi.syndication.modules;

import com.sun.syndication.feed.module.ModuleImpl;

public class GoogleMerchantShippingModuleImpl extends ModuleImpl implements GoogleMerchantShippingModule
{

    private static final long serialVersionUID = 4983263174291527458L;
    private String _price;
    private String _service;
    private String _country;

    public GoogleMerchantShippingModuleImpl()
    {
        super(GoogleMerchantShippingModule.class, GoogleMerchantProductModule.URI);
    }

    @Override
    public String getPrice()
    {
        return _price;
    }

    @Override
    public void setPrice(String price)
    {
        _price = price;
    }

    @Override
    public String getService()
    {
        return _service;
    }

    @Override
    public void setService(String service)
    {
        _service = service;
    }

    @Override
    public String getCountry()
    {
        return _country;
    }

    @Override
    public void setCountry(String country)
    {
        _country = country;
    }

    @Override
    public Class<?> getInterface()
    {
        return GoogleMerchantShippingModule.class;
    }

    @Override
    public void copyFrom(Object other)
    {
        if (!(other instanceof GoogleMerchantShippingModule))
        {
            throw new IllegalArgumentException(
                            "Expected other to be of class " + GoogleMerchantShippingModule.class.getSimpleName()
                                            + " but was " + other.getClass().getSimpleName());
        }
        final GoogleMerchantShippingModule otherModule = (GoogleMerchantShippingModule)other;
        setPrice(otherModule.getPrice());
        setCountry(otherModule.getCountry());
        setService(otherModule.getService());
    }

}
