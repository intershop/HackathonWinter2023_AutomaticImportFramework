package com.zamro.component.capi.syndication.modules;

public interface GoogleSeaCategoryModule
{
    public static final String CATEGORY_TAG_NAME = "category";
    public static final String CATEGORY_SUB_POSTFIX = "_sub";
    public static final String CATEGORY_MAIN_POSTFIX = "_main";
    public static final String CATEGORY_LINK_POSTFIX= "_link";

    public void setTagName(final String tagName);

    public String getTagName();

    public void setValue(final String value);

    public String getValue();

}
