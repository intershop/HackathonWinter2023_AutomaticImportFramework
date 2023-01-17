<%@  page buffer="none" import="java.util.*,java.io.*,com.intershop.beehive.core.internal.template.*,com.intershop.beehive.core.internal.template.isml.*,com.intershop.beehive.core.capi.log.*,com.intershop.beehive.core.capi.resource.*,com.intershop.beehive.core.capi.util.UUIDMgr,com.intershop.beehive.core.capi.util.XMLHelper,com.intershop.beehive.foundation.util.*,com.intershop.beehive.core.internal.url.*,com.intershop.beehive.core.internal.resource.*,com.intershop.beehive.core.internal.wsrp.*,com.intershop.beehive.core.capi.pipeline.PipelineDictionary,com.intershop.beehive.core.capi.naming.NamingMgr,com.intershop.beehive.core.capi.pagecache.PageCacheMgr,com.intershop.beehive.core.capi.request.SessionMgr,com.intershop.beehive.core.internal.request.SessionMgrImpl,com.intershop.beehive.core.pipelet.PipelineConstants" extends="com.intershop.beehive.core.internal.template.AbstractTemplate" %><% 
boolean _boolean_result=false;
TemplateExecutionConfig context = getTemplateExecutionConfig();
createTemplatePageConfig(context.getServletRequest());
printHeader(out);
 %><%@ page import="java.net.InetAddress" %>
<% %><%@ page contentType="text/html;charset=utf-8" %><%setEncodingType("text/html"); %>
<subject>Product Threshold Failure (<%out.print(java.net.InetAddress.getLocalHost().getHostName());%>)</subject>

Threre are problems with product counts for replication
<hr/>

<% _boolean_result=false;try {_boolean_result=((Boolean)((hasLoopElements("OrganizationThresholdFailure") ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",8,e);}if (_boolean_result) { %>
	<% while (loop("OrganizationThresholdFailure","tf",null)) { %>
		Brand: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:Repository:RepositoryDomain:DomainName"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {10}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		Threshold: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:Threshold"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {11}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		Source System: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:SourceCount"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {12}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		Target System: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:TargetCount"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {13}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		<br/>
		<strong>
		Replication job is NOW DISABLED:<br/>
		Job: <% {String value = null;try{value=context.getFormattedValue(getObject("DisableJob"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {17}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		Domain: <% {String value = null;try{value=context.getFormattedValue(getObject("DisableJobDomain"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {18}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>
		</strong>
		<hr/>
	<% } %>
<% } %>

<% _boolean_result=false;try {_boolean_result=((Boolean)((((((Boolean) (disableErrorMessages().isDefined(getObject("ThresholdFailures")))).booleanValue() && ((Boolean) (hasLoopElements("ThresholdFailures") ? Boolean.TRUE : Boolean.FALSE)).booleanValue()) ? Boolean.TRUE : Boolean.FALSE)))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",24,e);}if (_boolean_result) { %>
	'TakesPartInReplication' was switched OFF in following cases:<br/>
	<% while (loop("ThresholdFailures","tf",null)) { %>
		Channel: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:Repository:RepositoryDomain:DomainName"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {27}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		Threshold: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:Threshold"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {28}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		Source System: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:SourceCount"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {29}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		Target System: <% {String value = null;try{value=context.getFormattedValue(getObject("tf:TargetCount"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {30}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br/>
		<hr/>
	<% } %>
<% } %>

This e-mail was sent by scheduled job.<br/>
<% printFooter(out); %>