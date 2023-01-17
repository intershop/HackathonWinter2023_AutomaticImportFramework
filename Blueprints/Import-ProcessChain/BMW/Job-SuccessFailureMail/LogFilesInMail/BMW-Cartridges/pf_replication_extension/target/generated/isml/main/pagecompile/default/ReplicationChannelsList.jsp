<%@  page buffer="none" import="java.util.*,java.io.*,com.intershop.beehive.core.internal.template.*,com.intershop.beehive.core.internal.template.isml.*,com.intershop.beehive.core.capi.log.*,com.intershop.beehive.core.capi.resource.*,com.intershop.beehive.core.capi.util.UUIDMgr,com.intershop.beehive.core.capi.util.XMLHelper,com.intershop.beehive.foundation.util.*,com.intershop.beehive.core.internal.url.*,com.intershop.beehive.core.internal.resource.*,com.intershop.beehive.core.internal.wsrp.*,com.intershop.beehive.core.capi.pipeline.PipelineDictionary,com.intershop.beehive.core.capi.naming.NamingMgr,com.intershop.beehive.core.capi.pagecache.PageCacheMgr,com.intershop.beehive.core.capi.request.SessionMgr,com.intershop.beehive.core.internal.request.SessionMgrImpl,com.intershop.beehive.core.pipelet.PipelineConstants" extends="com.intershop.beehive.core.internal.template.AbstractTemplate" %><% 
boolean _boolean_result=false;
TemplateExecutionConfig context = getTemplateExecutionConfig();
createTemplatePageConfig(context.getServletRequest());
printHeader(out);
 %><% %><%@ page contentType="text/html;charset=utf-8" %><%setEncodingType("text/html"); %><% {out.flush();processLocalIncludeByServer((com.intershop.beehive.core.capi.request.ServletResponse)response,"inc/Modules", null, "2");} %><table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
<td class="breadcrumb">
<a href="<%=context.getFormattedValue(url(true,(new URLPipelineAction(context.getFormattedValue("ViewSMCExtensions-Start",null)))),null)%>" class="breadcrumb"><% {out.write(localizeISText("smc.extension.link.extensions","",null,null,null,null,null,null,null,null,null,null,null));} %></a>&nbsp;&gt;&nbsp;
<% {out.write(localizeISText("replicationchannels.smc.extension.link.replicationchannels","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
</tr>
</table>
<div><img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/space.gif" width="1" height="7" alt="" border="0"/></div>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td>
<!-- Main Content -->
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr>
<td width="100%" class="table_title aldi"><% {out.write(localizeISText("replicationchannels.list.title.replicationchannels","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
</tr><% _boolean_result=false;try {_boolean_result=((Boolean)(((((Boolean) (disableErrorMessages().isDefined(getObject("setTakesPart")))).booleanValue() || ((Boolean) (disableErrorMessages().isDefined(getObject("resetTakesPart")))).booleanValue()) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",20,e);}if (_boolean_result) { %><% _boolean_result=false;try {_boolean_result=((Boolean)(((((Boolean) (disableErrorMessages().isDefined(getObject("Clipboard")))).booleanValue() && ((Boolean) (hasLoopElements("Clipboard:ObjectUUIDs") ? Boolean.TRUE : Boolean.FALSE)).booleanValue()) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",21,e);}if (_boolean_result) { %><tr>
<td class="w e s" colspan="1"><% URLPipelineAction action1 = new URLPipelineAction(context.getFormattedValue(url(true,(new URLPipelineAction(context.getFormattedValue("ViewReplicationChannels-Dispatch",null)))),null));String site1 = null;String serverGroup1 = null;String actionValue1 = context.getFormattedValue(url(true,(new URLPipelineAction(context.getFormattedValue("ViewReplicationChannels-Dispatch",null)))),null);if (site1 == null){  site1 = action1.getDomain();  if (site1 == null)  {      site1 = com.intershop.beehive.core.capi.request.Request.getCurrent().getRequestSite().getDomainName();  }}if (serverGroup1 == null){  serverGroup1 = action1.getServerGroup();  if (serverGroup1 == null)  {      serverGroup1 = com.intershop.beehive.core.capi.request.Request.getCurrent().getRequestSite().getServerGroup();  }}out.print("<form");out.print(" method=\"");out.print("post");out.print("\"");out.print(" name=\"");out.print("commit");out.print("\"");out.print(" action=\"");out.print(context.getFormattedValue(url(true,(new URLPipelineAction(context.getFormattedValue("ViewReplicationChannels-Dispatch",null)))),null));out.print("\"");out.print(">");out.print(context.prepareWACSRFTag(actionValue1, site1, serverGroup1,true)); %><table border="0" cellpadding="4" cellspacing="0" width="100%" class="confirm_box">
<tr>
<td class="error_icon e"><img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/confirmation.gif" width="16" height="15" alt="" border="0"/></td>
<td class="confirm" width="100%"><% _boolean_result=false;try {_boolean_result=((Boolean)((disableErrorMessages().isDefined(getObject("setTakesPart"))))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",29,e);}if (_boolean_result) { %><% {out.write(localizeISText("replicationchannels.list.message.confirmation.SetTakesPart",null,null,null,null,null,null,null,null,null,null,null,null));} %><% } else { %><% {out.write(localizeISText("replicationchannels.list.message.confirmation.ResetTakesPart",null,null,null,null,null,null,null,null,null,null,null,null));} %><% } %><br/><% while (loop("Clipboard:ObjectUUIDs","ReplicationChannelUUID",null)) { %><% {Object temp_obj = (getObject("ReplicationChannelHelperCreator:getByUUID(ReplicationChannelUUID)")); getPipelineDictionary().put("ReplicationChannelHelper", temp_obj);} %>
&nbsp;&nbsp;<% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannelHelper:ReplicationChannel:RepositoryDomain:DomainName"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {37}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><% _boolean_result=false;try {_boolean_result=((Boolean)((hasNext("ReplicationChannelUUID") ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",37,e);}if (_boolean_result) { %><br/><% } %><% } %></td>
<td nowrap="nowrap">
<table border="0" cellspacing="0" cellpadding="0">
<tr><% _boolean_result=false;try {_boolean_result=((Boolean)((disableErrorMessages().isDefined(getObject("setTakesPart"))))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",43,e);}if (_boolean_result) { %><td><input type="submit" name="setTakesPartCommit" value="<%=context.getFormattedValue(localizeText(context.getFormattedValue("smc.OK.button",null)),null)%>" class="button"/></td><% } else { %><td><input type="submit" name="resetTakesPartCommit" value="<%=context.getFormattedValue(localizeText(context.getFormattedValue("smc.OK.button",null)),null)%>" class="button"/></td><% } %><td>&nbsp;</td>
<td><input type="submit" name="setTakesPartCancel" value="<%=context.getFormattedValue(localizeText(context.getFormattedValue("smc.Cancel.button",null)),null)%>" class="button"/></td>
</tr>
</table>
</td>
</tr>
</table><% out.print("</form>"); %></td>
</tr><% } else { %><tr><td>
<table border="0" cellspacing="0" cellpadding="4" width="100%" class="error_box w e s">
<tr>
<td class="error_icon top e"><img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/error.gif" width="16" height="15" alt="" border="0"/></td>
<td class="error top" width="100%"><% {out.write(localizeISText("replicationchannels.list.message.noselection","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
</tr>
</table>
</td></tr><% } %><% } %><tr>
<td class="table_title_description w e"><% {out.write(localizeISText("replicationchannels.list.description.replicationchannels",null,null,null,null,null,null,null,null,null,null,null,null));} %></td>
</tr>
</table>
<form action="<%=context.getFormattedValue(url(true,(new URLPipelineAction(context.getFormattedValue("ViewReplicationChannels-Dispatch",null)))),null)%>" method="post" name="filterForm">
<table border="0" cellpadding="5" cellspacing="0" width="100%" class="infobox n w e">
<tr>
<td class="infobox_title" nowrap="nowrap"><% {out.write(localizeISText("replicationchannels.list.description.replicationchannels.filter.infobox_title","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
</tr>
</table>
<table border="0" cellpadding="5" cellspacing="0" width="100%" class="infobox w e">
<tr>
<td class="infobox_item" nowrap="nowrap" align="right"><% {out.write(localizeISText("replicationchannels.list.form.filter.organization.label","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
<td class="infobox_item" nowrap="nowrap">
<select name="ReplicationChannelsOrganizationFilter" class="select">
<option value=""><% {out.write(localizeISText("replicationchannels.list.form.filter.organization.option.all","",null,null,null,null,null,null,null,null,null,null,null));} %></option><% while (loop("Organizations","Organization",null)) { %><option value="<% {String value = null;try{value=context.getFormattedValue(getObject("Organization"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {92}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>" <% _boolean_result=false;try {_boolean_result=((Boolean)((((context.getFormattedValue(getObject("ReplicationChannelsOrganizationFilter"),null).equals(context.getFormattedValue(getObject("Organization"),null)))) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",92,e);}if (_boolean_result) { %>selected="selected"<% } %>><% {String value = null;try{value=context.getFormattedValue(getObject("Organization"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {92}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %></option><% } %></select>
</td>
<td class="infobox_item" nowrap="nowrap">&nbsp;</td>
<td class="infobox_item" nowrap="nowrap" align="right"><% {out.write(localizeISText("replicationchannels.list.form.filter.marketchannel.label","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
<td class="infobox_item" nowrap="nowrap">
<select name="ReplicationChannelsMarketChannelFilter" class="select">
<option value=""><% {out.write(localizeISText("replicationchannels.list.form.filter.marketchannel.option.all","",null,null,null,null,null,null,null,null,null,null,null));} %></option><% while (loop("MarketChannels","MarketChannel",null)) { %><option value="<% {String value = null;try{value=context.getFormattedValue(getObject("MarketChannel"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {102}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>" <% _boolean_result=false;try {_boolean_result=((Boolean)((((context.getFormattedValue(getObject("ReplicationChannelsMarketChannelFilter"),null).equals(context.getFormattedValue(getObject("MarketChannel"),null)))) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",102,e);}if (_boolean_result) { %>selected="selected"<% } %>><% {String value = null;try{value=context.getFormattedValue(getObject("MarketChannel"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {102}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %></option><% } %><option value="NULL" <% _boolean_result=false;try {_boolean_result=((Boolean)((((context.getFormattedValue(getObject("ReplicationChannelsMarketChannelFilter"),null).equals(context.getFormattedValue("NULL",null)))) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",105,e);}if (_boolean_result) { %>selected="selected"<% } %>><% {out.write(localizeISText("replicationchannels.list.form.filter.marketchannel.option.without_marketchannel","",null,null,null,null,null,null,null,null,null,null,null));} %></option>
</select>
</td>
<td class="infobox_item" width="100%">&nbsp;</td>
</tr>
</table>
<table border="0" cellpadding="5" cellspacing="0" width="100%" class="infobox w e s">
<tr>
<td class="infobox_item" nowrap="nowrap" align="right"><% {out.write(localizeISText("replicationchannels.list.form.filter.takes_part.label","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
<td class="infobox_item" nowrap="nowrap">
<select name="ReplicationChannelsTakesPartFilter" class="select">
<option value="" ><% {out.write(localizeISText("replicationchannels.list.form.filter.takes_part.option.all","",null,null,null,null,null,null,null,null,null,null,null));} %></option>
<option value="YES" <% _boolean_result=false;try {_boolean_result=((Boolean)((((context.getFormattedValue(getObject("ReplicationChannelsTakesPartFilter"),null).equals(context.getFormattedValue("YES",null)))) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",118,e);}if (_boolean_result) { %>selected="selected"<% } %>><% {out.write(localizeISText("replicationchannels.list.form.filter.takes_part.option.yes","",null,null,null,null,null,null,null,null,null,null,null));} %></option>
<option value="NO" <% _boolean_result=false;try {_boolean_result=((Boolean)((((context.getFormattedValue(getObject("ReplicationChannelsTakesPartFilter"),null).equals(context.getFormattedValue("NO",null)))) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",119,e);}if (_boolean_result) { %>selected="selected"<% } %>><% {out.write(localizeISText("replicationchannels.list.form.filter.takes_part.option.no","",null,null,null,null,null,null,null,null,null,null,null));} %></option>
</select>
</td>
<td class="infobox_item" width="100%">&nbsp;</td>
<td class="infobox_item" nowrap="nowrap" align="right">
<table border="0" cellspacing="4" cellpadding="0">
<tr>
<td class="button">
<input type="submit" name="filter" value="<%=context.getFormattedValue(localizeText(context.getFormattedValue("smc.Apply.button",null)),null)%>" class="button"/>
</td>
</tr>
</table>
</td>
</tr>
</table>
</form>
<form action="<%=context.getFormattedValue(url(true,(new URLPipelineAction(context.getFormattedValue("ViewReplicationChannels-Dispatch",null)))),null)%>" method="post" name="ReplicationChannelsList">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td>
<table width="100%" border="0" cellspacing="0" cellpadding="0"><% _boolean_result=false;try {_boolean_result=((Boolean)((((Boolean) (hasLoopElements("ReplicationChannels") ? Boolean.TRUE : Boolean.FALSE)).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",140,e);}if (_boolean_result) { %><tr>
<td class="table_detail w e s" colspan="7"><% {out.write(localizeISText("replicationchannels.list.noentries","",null,null,null,null,null,null,null,null,null,null,null));} %></td>
</tr><% } else { %><tr>
<td class="table_header w e s center" nowrap="nowrap" width="70" valign="middle">
<div id="A">
<table border="0" cellspacing="0" cellpadding="0" class="table_header center" width="75">
<tr>
<td nowrap="nowrap">
<a href="javascript:selectAll('ReplicationChannelsList','SelectedObjectUUID','A','B');" class="tableheader"><% {out.write(localizeISText("smc.SelectAll.link4","",null,null,null,null,null,null,null,null,null,null,null));} %></a>
</td>
</tr>
</table>
</div>
<div id="B" style="display:none">
<table border="0" cellspacing="0" cellpadding="0" class="table_header center" width="75">
<tr>
<td nowrap="nowrap">
<a href="javascript:selectAll('ReplicationChannelsList','SelectedObjectUUID','A','B');" class="tableheader"><% {out.write(localizeISText("smc.ClearAll.link4","",null,null,null,null,null,null,null,null,null,null,null));} %></a>
</td>
</tr>
</table>
</div>
</td>
<td class="table_header e s" nowrap="nowrap" valign="middle"><% {out.write(localizeISText("replicationchannels.list.table.header.Organization",null,null,null,null,null,null,null,null,null,null,null,null));} %></td>
<td class="table_header e s" nowrap="nowrap" valign="middle"><% {out.write(localizeISText("replicationchannels.list.table.header.ChannelID",null,null,null,null,null,null,null,null,null,null,null,null));} %></td>
<td class="table_header e s" nowrap="nowrap" valign="middle"><% {out.write(localizeISText("replicationchannels.list.table.header.ChannelName",null,null,null,null,null,null,null,null,null,null,null,null));} %></td>
<td class="table_header e s" nowrap="nowrap" valign="middle"><% {out.write(localizeISText("replicationchannels.list.table.header.MarketChannel",null,null,null,null,null,null,null,null,null,null,null,null));} %></td>
<td class="table_header e s" nowrap="nowrap" valign="middle"><% {out.write(localizeISText("replicationchannels.list.table.header.TakesPart",null,null,null,null,null,null,null,null,null,null,null,null));} %></td>
</tr><% while (loop("ReplicationChannels","ReplicationChannel",null)) { %><% {Object temp_obj = (getObject("ReplicationChannelHelperCreator:get(ReplicationChannel)")); getPipelineDictionary().put("ReplicationChannelHelper", temp_obj);} %><% {Object temp_obj = (getObject("ReplicationChannelHelper:MarketChannel")); getPipelineDictionary().put("AssignedMarketChannel", temp_obj);} %><% {Object temp_obj = (((((Boolean) ((((Boolean) (disableErrorMessages().isDefined(getObject("AssignedMarketChannel")))).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue() || ((Boolean) ((((context.getFormattedValue(getObject("AssignedMarketChannel:RepositoryDomain:Site:TakesPartInReplication"),null).equals(context.getFormattedValue("true",null)))) ? Boolean.TRUE : Boolean.FALSE))).booleanValue()) ? Boolean.TRUE : Boolean.FALSE)); getPipelineDictionary().put("AssignedMarketChannelTakesPartInReplication", temp_obj);} %> 
<% {Object temp_obj = (getObject("ReplicationChannelHelper:Organization")); getPipelineDictionary().put("ReplicationChannelOrganization", temp_obj);} %><% {Object temp_obj = (getObject("ReplicationChannelHelper:isMarketChannel")); getPipelineDictionary().put("IsMarketChannel", temp_obj);} %><% {Object temp_obj = ((((context.getFormattedValue(pad(context.getFormattedValue(getObject("ReplicationChannel:Id"),null),((Number)(new Double(7))).intValue()),null).equals(context.getFormattedValue("Preview",null)))) ? Boolean.TRUE : Boolean.FALSE)); getPipelineDictionary().put("IsPreviewChannel", temp_obj);} %><% {Object temp_obj = ((( ((Number) getObject("ReplicationChannelOrganization:TypeCode")).doubleValue() ==((Number)(new Double(20))).doubleValue()) ? Boolean.TRUE : Boolean.FALSE)); getPipelineDictionary().put("IsCOMChannel", temp_obj);} %><tr>
<td class="w e s center">
<input type="checkbox"
name="SelectedObjectUUID"
value="<% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannel:UUID"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {184}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>"
<% _boolean_result=false;try {_boolean_result=((Boolean)(getObject("Clipboard:contains(ReplicationChannel:UUID)"))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",185,e);}if (_boolean_result) { %>checked="checked"<% } %><% _boolean_result=false;try {_boolean_result=((Boolean)((((Boolean) (((((Boolean) getObject("IsMarketChannel")).booleanValue() || ((Boolean) getObject("IsPreviewChannel")).booleanValue() || ((Boolean) getObject("IsCOMChannel")).booleanValue()) ? Boolean.TRUE : Boolean.FALSE))).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",186,e);}if (_boolean_result) { %>disabled="disabled"<% } %>
/>
<input type="hidden" name="ObjectUUID" value="<% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannel:UUID"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {188}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>"/>
</td>
<td class="table_detail e s"><% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannelOrganization:ID"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {190}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %></td>
<td class="table_detail e s"><% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannel:Id"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {191}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %></td>
<td class="table_detail e s"><% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannel:DisplayName"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {192}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %></td>
<td class="table_detail e s"><% _boolean_result=false;try {_boolean_result=((Boolean)(getObject("IsMarketChannel"))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",194,e);}if (_boolean_result) { %><% {out.write(localizeISText("replicationchannels.list.table.column.MarketChannel.self",null,null,null,null,null,null,null,null,null,null,null,null));} %><% } else {_boolean_result=false;try {_boolean_result=((Boolean)((disableErrorMessages().isDefined(getObject("AssignedMarketChannel"))))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",196,e);}if (_boolean_result) { %><% {String value = null;try{value=context.getFormattedValue(getObject("AssignedMarketChannel:Id"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {197}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %><% _boolean_result=false;try {_boolean_result=((Boolean)((((Boolean) getObject("AssignedMarketChannelTakesPartInReplication")).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",198,e);}if (_boolean_result) { %>
(*)
<% } %><% } else {_boolean_result=false;try {_boolean_result=((Boolean)((((Boolean) getObject("IsCOMChannel")).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",201,e);}if (_boolean_result) { %> 
<!-- This dealer channel is not assigned to a Market Channel --><% } else { %> 
<!-- This is a COM channel --><% }}} %></td>
<td class="table_detail e s"><% _boolean_result=false;try {_boolean_result=((Boolean)(getObject("ReplicationChannelHelper:isTakesPartInReplication"))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",208,e);}if (_boolean_result) { %><% {out.write(localizeISText("replicationchannels.list.table.column.TakesPart.yes",null,null,null,null,null,null,null,null,null,null,null,null));} %><% _boolean_result=false;try {_boolean_result=((Boolean)((((Boolean) getObject("AssignedMarketChannelTakesPartInReplication")).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",210,e);}if (_boolean_result) { %>
(*)
<% } %><% } else { %><% {out.write(localizeISText("replicationchannels.list.table.column.TakesPart.no",null,null,null,null,null,null,null,null,null,null,null,null));} %><% } %></td>
</tr><% } %><% } %></table>
<table width="100%" border="0" cellspacing="0" cellpadding="0" class="w e s">
<tr>
<td align="right">
<table cellpadding="0" cellspacing="4" border="0">
<tr>
<td class="button">
<input type="hidden" name="ReplicationChannelsOrganizationFilter" value="<% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannelsOrganizationFilter"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {227}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>"/>
<input type="hidden" name="ReplicationChannelsMarketChannelFilter" value="<% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannelsMarketChannelFilter"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {228}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>"/>
<input type="hidden" name="ReplicationChannelsTakesPartFilter" value="<% {String value = null;try{value=context.getFormattedValue(getObject("ReplicationChannelsTakesPartFilter"),null,null);}catch(Exception e){value=null;Logger.error(this,"ISPRINT has an invalid expression. Returning empty string. Line: {229}",e);}if (value==null) value="";value = encodeString(value);out.write(value);} %>"/>
<input type="submit" name="setTakesPart" value="<%=context.getFormattedValue(localizeText(context.getFormattedValue("replicationchannels.list.SetTakesPart.button",null)),null)%>" class="button" <% _boolean_result=false;try {_boolean_result=((Boolean)((((Boolean) (hasLoopElements("ReplicationChannels") ? Boolean.TRUE : Boolean.FALSE)).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",231,e);}if (_boolean_result) { %>disabled="disabled"<% } %>/>
<input type="submit" name="resetTakesPart" value="<%=context.getFormattedValue(localizeText(context.getFormattedValue("replicationchannels.list.ResetTakesPart.button",null)),null)%>" class="button" <% _boolean_result=false;try {_boolean_result=((Boolean)((((Boolean) (hasLoopElements("ReplicationChannels") ? Boolean.TRUE : Boolean.FALSE)).booleanValue() ? Boolean.FALSE : Boolean.TRUE) )).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",232,e);}if (_boolean_result) { %>disabled="disabled"<% } %>/>
</td>
</tr>
</table>
</td>
</tr>
</table>
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr>
<td>
<div><img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/space.gif" width="1" height="6" alt="" border="0"/></div><% processOpenTag(response, pageContext, "pagingbar", new TagParameter[] {
new TagParameter("variablepagesize","true"),
new TagParameter("pageable","ReplicationChannels")}, 243); %></td>
</tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td><img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/space.gif" width="1" height="6" alt="" border="0"/></td>
</tr>
</table>
</td>
</tr>
</table>
</form>
</td>
</tr>
</table><% printFooter(out); %>