package com.zamro.component.capi.syndication.modules;

import java.util.List;

import com.sun.syndication.feed.module.Module;

public interface GoogleSeaProductModule extends Module
{

    public final static String NS_PREFIX = "h";
    public final static String URI = "http://hidde.debruijn.com/ns/1.0";

    public static final String PRICE_EX_VAT_TAG_NAME = "price_excl_vat";
    public static final String DISCOUNT_TAG_NAME = "discount";
    public static final String DELIVERY_TIME_TAG_NAME = "delivery_time";
    public static final String MATERIAL_TAG_NAME = "material";
    public static final String SIZE_TAG_NAME = "size";
    public static final String UNIT_SIZE_TAG_NAME = "unit_size";
    public static final String CATEGORY_PATH_TAG_NAME = "category_path";
    public static final String MASTER_ID_TAG_NAME = "master_id";
    public static final String MASTER_NAME_TAG_NAME = "master_name";
    public static final String MASTER_LINK_TAG_NAME = "master_link";

    public void setPriceExVat(final String priceExVat);

    public String getPriceExVat();

    public void setDiscount(final String discount);

    public String getDiscount();

    public void setDeliveryTime(final String deliveryTime);

    public String getDeliveryTime();

    public void setMaterial(final String material);

    public String getMaterial();

    public void setSize(final String size);

    public String getSize();

    public void setUnitSize(final String unitSize);

    public String getUnitSize();

    public void setCategoryPath(final String categoryPath);

    public String getCategoryPath();

    public void setCategories(final List<GoogleSeaCategoryModule> categories);

    public List<GoogleSeaCategoryModule> getCategories();
    
    public void setMasterId(final String masterId);
    
    public String getMasterId();
    
    public void setMasterName(final String masterName);
    
    public String getMasterName();
    
    public void setMasterLink(final String masterLink);
    
    public String getMasterLink();
    
}
