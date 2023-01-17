package com.zamro.component.capi.syndication.modules;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;

public class GoogleSeaModuleGenerator implements ModuleGenerator
{

    private static final Namespace NAMESPACE = Namespace.getNamespace(GoogleSeaProductModule.NS_PREFIX,
                    GoogleSeaProductModule.URI);
    private static final Set<Namespace> NAMESPACES = Collections.singleton(NAMESPACE);

    @Override
    public void generate(final Module module, final Element element)
    {
        final GoogleSeaProductModule myModule = (GoogleSeaProductModule)module;
        addIfNotBlank(element, myModule.getCategoryPath(), GoogleSeaProductModule.CATEGORY_PATH_TAG_NAME);
        addIfNotBlank(element, myModule.getDiscount(), GoogleSeaProductModule.DISCOUNT_TAG_NAME);
        addIfNotBlank(element, myModule.getMasterId(), GoogleSeaProductModule.MASTER_ID_TAG_NAME);
        addIfNotBlank(element, myModule.getMasterName(), GoogleSeaProductModule.MASTER_NAME_TAG_NAME);
        addIfNotBlank(element, myModule.getMasterLink(), GoogleSeaProductModule.MASTER_LINK_TAG_NAME);
        addSeaCategoriesIfNotNull(element, myModule.getCategories());

    }

    private void addSeaCategoriesIfNotNull(final Element element, List<GoogleSeaCategoryModule> categories)
    {
        if (categories != null)
        {
            categories.forEach(cat -> add(element, cat.getValue(), cat.getTagName()));
        }
    }

    private void addIfNotNull(final Element element, final Object value, final String tag)
    {
        if (value != null)
        {
            add(element, String.valueOf(value), tag);
        }
    }

    private void addIfNotBlank(final Element parent, final String value, final String tag)
    {
        if (value != null && !"".equals(value.trim()))
        {
            add(parent, value, tag);
        }
    }

    private void add(final Element parent, final String value, final String tag)
    {
        final Element child = new Element(tag, NAMESPACE);
        child.setText(value);
        add(parent, child);
    }

    private void add(final Element parent, final Element child)
    {
        parent.addContent(child);
    }

    @Override
    public String getNamespaceUri()
    {
        return GoogleSeaProductModule.URI;
    }

    @Override
    public Set<?> getNamespaces()
    {
        return NAMESPACES;
    }

}