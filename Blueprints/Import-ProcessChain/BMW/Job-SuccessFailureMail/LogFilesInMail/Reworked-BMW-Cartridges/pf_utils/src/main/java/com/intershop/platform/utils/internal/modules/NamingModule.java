package com.intershop.platform.utils.internal.modules;

import javax.annotation.Generated;
import javax.inject.Singleton;

import com.intershop.beehive.core.capi.naming.AbstractNamingModule;
import com.intershop.platform.utils.capi.template.TemplateToolsProvider;
import com.intershop.platform.utils.internal.template.TemplateToolsProviderImpl;

/**
 * This is the naming module of the pf_utils cartridge.
 * 
 * @author a.seitz@intershop.de
 * @version 1.0, 07/25/2016
 * 
 *          Basic functionality.
 * 
 */
@Generated(value = { "com.intershop.beehive.objectgraph.guice.capi.naming.NamingMgrMigrationTool" })
public class NamingModule extends AbstractNamingModule
{
    /**
     * Binding the managers.
     */
    @Override
    protected void configure()
    {
        // Provider
        bindProvider(TemplateToolsProvider.class).to(TemplateToolsProviderImpl.class).in(Singleton.class);
    }
}
