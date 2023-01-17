<%@  page buffer="none" import="java.util.*,java.io.*,com.intershop.beehive.core.internal.template.*,com.intershop.beehive.core.internal.template.isml.*,com.intershop.beehive.core.capi.log.*,com.intershop.beehive.core.capi.resource.*,com.intershop.beehive.core.capi.util.UUIDMgr,com.intershop.beehive.core.capi.util.XMLHelper,com.intershop.beehive.foundation.util.*,com.intershop.beehive.core.internal.url.*,com.intershop.beehive.core.internal.resource.*,com.intershop.beehive.core.internal.wsrp.*,com.intershop.beehive.core.capi.pipeline.PipelineDictionary,com.intershop.beehive.core.capi.naming.NamingMgr,com.intershop.beehive.core.capi.pagecache.PageCacheMgr,com.intershop.beehive.core.capi.request.SessionMgr,com.intershop.beehive.core.internal.request.SessionMgrImpl,com.intershop.beehive.core.pipelet.PipelineConstants" extends="com.intershop.beehive.core.internal.template.AbstractTemplate" %><% 
boolean _boolean_result=false;
TemplateExecutionConfig context = getTemplateExecutionConfig();
createTemplatePageConfig(context.getServletRequest());
printHeader(out);
 %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<% %><%@ page contentType="text/html;charset=utf-8" %><%%><%@ page session="false"%><%setEncodingType("text/html"); %><subject><% {String value = null;try{value=context.getFormattedValue(getObject("Subject"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {4}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %></subject><html>
	<head><% insertIntershopSignature(request,(com.intershop.beehive.core.capi.request.ServletResponse)response); %><style type="text/css">
body {
font: 100% Verdana, Arial, Helvetica, sans-serif;
color: #000;
background: #fff;
}
body p { 
font-size: 12px;
}
p {
font-size: 12px;
}
p.additional {
margin-top: 20px;
font-size: 10px;
color: #333;
}
span.bold {
font-weight: bold;
}
</style>
</head>
<body>
<p><% {String value = null;try{value=context.getFormattedValue(getObject("Message"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {31}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %></p>
<p>
<span class="bold">Host name:</span> <% {String value = null;try{value=context.getFormattedValue(getObject("Process:HostName"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {34}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br />
</p><% _boolean_result=false;try {_boolean_result=((Boolean)((disableErrorMessages().isDefined(getObject("LogInformation"))))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",36,e);}if (_boolean_result) { %><p>
Some sub tasks returned warnings and errors. Please check the attached log files:
</p>
----------------------
<% while (loop("LogInformation","logInfo",null)) { %><p><% 
						String[] logEntry = ((String)getObject("logInfo")).split("\\|");
						getPipelineDictionary().put("jobFileName",logEntry[0].substring(logEntry[0].lastIndexOf("\\")+1));
						getPipelineDictionary().put("jobName",logEntry[1]);
						getPipelineDictionary().put("jobDescription",logEntry[2]);
						getPipelineDictionary().put("jobDomain",logEntry[3]);
					%><span class="bold">Job name: </span><% {String value = null;try{value=context.getFormattedValue(getObject("jobName"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {51}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br />
<span class="bold">Job description: </span><% {String value = null;try{value=context.getFormattedValue(getObject("jobDescription"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {52}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><br />
<span class="bold">Job execution domain: </span> <% {String value = null;try{value=context.getFormattedValue(getObject("jobDomain"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {53}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %> <br />
<span class="bold">Log File Name: </span> <% {String value = null;try{value=context.getFormattedValue(getObject("jobFileName"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {54}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %> <br />
Please find the original import file compressed on eserver path: /share/sites/<% {String value = null;try{value=context.getFormattedValue(getObject("jobDomain"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {55}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>-Site/units/<% {String value = null;try{value=context.getFormattedValue(getObject("jobDomain"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {55}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>/impex/archive.
<br /><br />
----------------------
</p><% } %><p><br/>
Please note:<br/>
Before restarting the import process chain, please be aware of the parameter <b>CleanupUploadFolder</b> for the job configuration <b>ProcessImportPreProcess</b> within domain <b>root</b>.<br/>
The valid options for this parameter when starting the process chain are <b>true</b> and <b>false</b>.<br /><br />
1. Import process chain starting with parameter set to <b>true</b><br />
All import files which are not processed due to import errors on the previous import process will be deleted before starting the new import process. Choose this option if new files are available and these files are a <b>REPLACEMENT</b> of the erroneous imports.<br /><br />
2. Import process chain starting with parameter set to <b>false</b><br />
All import files which are not processed due to import errors on the previous import process will be imported before importing new import files. Choose this option if the import of these files is neccessary to get consistent data. 
</p><% } %><p>
Yours sincerely,<br/>
The Intershop support team
</p>
<p class="additional">
*** This is an automatically generated email, please do not reply ***
</p>
</body>
</html><% printFooter(out); %>