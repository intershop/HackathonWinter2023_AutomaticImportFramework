package com.zamro.component.capi.syndication.modules;

public interface GoogleMerchantShippingModule
{

    public static final String SHIPPING_PRICE_TAG_NAME = "price";
    public static final String SHIPPING_SERVICE_TAG_NAME = "service";
    public static final String SHIPPING_COUNTRY_TAG_NAME = "country";

    public String getPrice();

    public void setPrice(final String price);

    public String getService();

    public void setService(final String service);

    public String getCountry();

    public void setCountry(final String country);

}
