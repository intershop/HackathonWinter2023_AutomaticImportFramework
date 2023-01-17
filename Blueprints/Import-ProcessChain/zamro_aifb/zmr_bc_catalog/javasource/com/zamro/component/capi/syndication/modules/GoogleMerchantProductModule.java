package com.zamro.component.capi.syndication.modules;

import com.sun.syndication.feed.module.Module;

public interface GoogleMerchantProductModule extends Module
{

    public static final String PRICE_TAG_NAME = "price";
    public static final String BRAND_TAG_NAME = "brand";
    public static final String GTIN_TAG_NAME = "gtin";
    public static final String MPN_TAG_NAME = "mpn";
    public static final String SALE_PRICE_TAG_NAME = "sale_price";
    public static final String SALE_PRICE_EFFECTIVE_TAG_NAME = "sale_price_effective_date";
    public static final String ID_TAG_NAME = "id";
    public static final String IMAGE_LINK_TAG_NAME = "image_link";
    public static final String MOBILE_LINK_TAG_NAME = "mobile_link";
    public static final String SHIPPING_TAG_NAME = "shipping";
    public static final String PRODUCT_TYPE_TAG_NAME = "product_type";
    public static final String GOOGLE_PRODUCT_CATEGORY_TAG_NAME = "google_product_category";

    public static final String CONDITION_TAG_NAME = "condition";
    public static final String AVAILABILITY_TAG_NAME = "availability";

    public final static String NS_PREFIX = "g";
    public final static String URI = "http://base.google.com/ns/1.0";

    public String getImageLink();

    public void setImageLink(final String imageLink);

    public String getPrice();

    public void setPrice(final String price);

    public String getSalePrice();

    public void setSalePrice(final String salePrice);

    public String getSalePriceEffectiveDate();

    public void setSalePriceEffectiveDate(final String salePriceEffectiveDate);

    public String getCondition();

    public void setCondition(final String condition);

    public String getId();

    public void setId(final String id);

    public void setAvailability(final String value);

    public String getAvailability();

    public void setMobileLink(final String mobileLink);

    public String getMobileLink();

    public GoogleMerchantShippingModule getShipping();

    public void setShipping(GoogleMerchantShippingModule shipping);

    public void setBrand(final String brand);

    public String getBrand();

    public void setGtin(final String gtin);

    public String getGtin();

    public void setMpn(final String mpn);

    public String getMpn();

    public void setProductType(final String productType);

    public String getProductType();

    public void setGoogleProductCategory(final String googleProductCategory);

    public String getGoogleProductCategory();
    

}
