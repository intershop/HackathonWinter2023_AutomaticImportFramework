package com.zamro.component.capi.syndication.modules;

import java.util.List;

import com.sun.syndication.feed.module.ModuleImpl;

public class GoogleSeaProductModuleImpl extends ModuleImpl implements GoogleSeaProductModule
{

    private static final long serialVersionUID = 3362189547689538564L;
    private String _priceExVat;
    private String _discount;
    private String _deliveryTime;
    private String _material;
    private String _size;
    private String _unitSize;
    private String _categoryPath;
    private String _masterId;
    private String _masterName;
    private String _masterLink;
    
    
    private List<GoogleSeaCategoryModule> _categories;

    public GoogleSeaProductModuleImpl()
    {
        super(GoogleSeaProductModule.class, URI);
    }

    @Override
    public Class<?> getInterface()
    {
        return GoogleSeaProductModule.class;
    }

    @Override
    public void copyFrom(Object obj)
    {

    }

    @Override
    public void setPriceExVat(String priceExVat)
    {
        _priceExVat = priceExVat;
    }

    @Override
    public String getPriceExVat()
    {
        return _priceExVat;
    }

    @Override
    public void setDiscount(String discount)
    {
        _discount = discount;
    }

    @Override
    public String getDiscount()
    {
        return _discount;
    }

    @Override
    public void setDeliveryTime(String deliveryTime)
    {
        _deliveryTime = deliveryTime;
    }

    @Override
    public String getDeliveryTime()
    {
        return _deliveryTime;
    }

    @Override
    public void setMaterial(String material)
    {
        _material = material;
    }

    @Override
    public String getMaterial()
    {
        return _material;
    }

    @Override
    public void setSize(String size)
    {
        _size = size;
    }

    @Override
    public String getSize()
    {
        return _size;
    }

    @Override
    public void setUnitSize(String unitSize)
    {
        _unitSize = unitSize;
    }

    @Override
    public String getUnitSize()
    {
        return _unitSize;
    }

    @Override
    public void setCategoryPath(String categoryPath)
    {
        _categoryPath = categoryPath;
    }

    @Override
    public String getCategoryPath()
    {
        return _categoryPath;
    }

    @Override
    public void setCategories(List<GoogleSeaCategoryModule> categories)
    {
        _categories = categories;
    }

    @Override
    public List<GoogleSeaCategoryModule> getCategories()
    {
        return _categories;
    }

    @Override
    public void setMasterId(String masterId)
    {
        _masterId = masterId;
    }

    @Override
    public String getMasterId()
    {
        return _masterId;
    }    
    
    @Override
    public String getMasterName()
    {
        return _masterName;
    }


    @Override
    public void setMasterName(String masterName)
    {
        _masterName = masterName;
    }

    @Override
    public String getMasterLink()
    {
        return _masterLink;
    }
    
    @Override
    public void setMasterLink(String masterLink)
    {
        _masterLink = masterLink;
        
    }
}
