<%@  page buffer="none" import="java.util.*,java.io.*,com.intershop.beehive.core.internal.template.*,com.intershop.beehive.core.internal.template.isml.*,com.intershop.beehive.core.capi.log.*,com.intershop.beehive.core.capi.resource.*,com.intershop.beehive.core.capi.util.UUIDMgr,com.intershop.beehive.core.capi.util.XMLHelper,com.intershop.beehive.foundation.util.*,com.intershop.beehive.core.internal.url.*,com.intershop.beehive.core.internal.resource.*,com.intershop.beehive.core.internal.wsrp.*,com.intershop.beehive.core.capi.pipeline.PipelineDictionary,com.intershop.beehive.core.capi.naming.NamingMgr,com.intershop.beehive.core.capi.pagecache.PageCacheMgr,com.intershop.beehive.core.capi.request.SessionMgr,com.intershop.beehive.core.internal.request.SessionMgrImpl,com.intershop.beehive.core.pipelet.PipelineConstants" extends="com.intershop.beehive.core.internal.template.AbstractTemplate" %><% 
boolean _boolean_result=false;
TemplateExecutionConfig context = getTemplateExecutionConfig();
createTemplatePageConfig(context.getServletRequest());
printHeader(out);
 %><% %><%@ page contentType="text/html;charset=utf-8" %><%setEncodingType("text/html"); %><%@page import="com.intershop.beehive.core.capi.naming.NamingMgr,
				com.intershop.platform.utils.capi.template.TemplateToolsProvider,
				com.intershop.platform.utils.capi.template.TemplateTools" %><%
    String templateToolsName = (String)getObject("name");
	if (templateToolsName == null)
		throw new RuntimeException("No name is given to get an instance of template tools");
	
	TemplateToolsProvider templateToolsProvider = null;
	try {
	    templateToolsProvider = NamingMgr.getProvider(TemplateToolsProvider.class);
	} catch(ClassCastException cce) {
	    Logger.error(this, "Your provider implementation of {} cannot be casted to TemplateToolsProvider",
	                    TemplateToolsProvider.REGISTRY_NAME);
	}
	if (templateToolsProvider == null)
		throw new RuntimeException("Impossible to locate or instantiate provider instance for key " + TemplateToolsProvider.REGISTRY_NAME);

	TemplateTools templateTools = templateToolsProvider.lookupTemplateTools(templateToolsName);
	if (templateTools == null)
		throw new RuntimeException("Impossible to locate or instantiate template tools instance for name " + templateToolsName);

	String templateToolsKey = (String)getObject("key");
	if ((templateToolsKey != null) && (templateToolsKey.trim().length() > 0))
		getPipelineDictionary().put(templateToolsKey, templateTools);
	else
		getPipelineDictionary().put("TemplateTools", templateTools);
%><% printFooter(out); %>