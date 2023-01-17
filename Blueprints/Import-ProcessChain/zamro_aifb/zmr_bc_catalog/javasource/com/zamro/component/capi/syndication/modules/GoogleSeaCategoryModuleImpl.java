package com.zamro.component.capi.syndication.modules;

import com.sun.syndication.feed.module.ModuleImpl;

public class GoogleSeaCategoryModuleImpl extends ModuleImpl implements GoogleSeaCategoryModule
{
    private static final long serialVersionUID = 5317016165257060579L;
    private String _value;
    private String _tagName;

    public GoogleSeaCategoryModuleImpl()
    {
        super(GoogleSeaCategoryModule.class, GoogleSeaProductModule.URI);
    }

    public GoogleSeaCategoryModuleImpl(String tagName, String value)
    {
        super(GoogleSeaCategoryModule.class, GoogleSeaProductModule.URI);
        setTagName(tagName);
        setValue(value);
    }

    @Override
    public void setTagName(final String tagName)
    {
        _tagName = tagName;
    }

    @Override
    public String getTagName()
    {
        return _tagName;
    }

    @Override
    public void setValue(final String value)
    {
        _value = value;
    }

    @Override
    public String getValue()
    {
        return _value;
    }

    @Override
    public Class<?> getInterface()
    {
        return GoogleSeaCategoryModule.class;
    }

    @Override
    public void copyFrom(Object other)
    {
        if (!(other instanceof GoogleSeaCategoryModule))
        {
            throw new IllegalArgumentException(
                            "Expected other to be of class " + GoogleSeaCategoryModule.class.getSimpleName()
                                            + " but was " + other.getClass().getSimpleName());
        }

        final GoogleSeaCategoryModule otherModule = (GoogleSeaCategoryModule)other;
        setValue(otherModule.getValue());
        setTagName(otherModule.getTagName());

    }
}