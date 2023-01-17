<%@  page buffer="none" import="java.util.*,java.io.*,com.intershop.beehive.core.internal.template.*,com.intershop.beehive.core.internal.template.isml.*,com.intershop.beehive.core.capi.log.*,com.intershop.beehive.core.capi.resource.*,com.intershop.beehive.core.capi.util.UUIDMgr,com.intershop.beehive.core.capi.util.XMLHelper,com.intershop.beehive.foundation.util.*,com.intershop.beehive.core.internal.url.*,com.intershop.beehive.core.internal.resource.*,com.intershop.beehive.core.internal.wsrp.*,com.intershop.beehive.core.capi.pipeline.PipelineDictionary,com.intershop.beehive.core.capi.naming.NamingMgr,com.intershop.beehive.core.capi.pagecache.PageCacheMgr,com.intershop.beehive.core.capi.request.SessionMgr,com.intershop.beehive.core.internal.request.SessionMgrImpl,com.intershop.beehive.core.pipelet.PipelineConstants" extends="com.intershop.beehive.core.internal.template.AbstractTemplate" %><% 
boolean _boolean_result=false;
TemplateExecutionConfig context = getTemplateExecutionConfig();
createTemplatePageConfig(context.getServletRequest());
printHeader(out);
 %><% %><%@ page contentType="text/html;charset=utf-8" %><%setEncodingType("text/html"); %>	<td width="50%">
		<table border="0" cellspacing="0" cellpadding="0" width="100%">
			<tr>
				<td class="overview_arrow">
					<img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/arrow_right_down.gif" width="9" height="9" alt="" border="0"/>
				</td>
				<td class="overview_subtitle" nowrap="nowrap" width="100%">
					<a href="<%=context.getFormattedValue(url(true,(new URLPipelineAction(context.getFormattedValue("ViewReplicationChannels-ReplicationChannelsList",null)))),null)%>" class="overview_subtitle"><% {out.write(localizeISText("replicationchannels.smc.extension.overview.title.replicationchannels","",null,null,null,null,null,null,null,null,null,null,null));} %></a>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="overview_title_description" valign="top">
					<% {out.write(localizeISText("replicationchannels.smc.extension.overview.description.replicationchannels","",null,null,null,null,null,null,null,null,null,null,null));} %>
					<br />&nbsp;
				</td>
			</tr>
			<tr>
				<td colspan="2" class="overview_line">
					<img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/space.gif" width="2" height="1" alt="" border="0"/>
				</td>
			</tr>
		</table>
	</td>
	<% processOpenTag(response, pageContext, "seq-next", new TagParameter[] {
new TagParameter("name","count")}, 24); %>
	<% _boolean_result=false;try {_boolean_result=((Boolean)((( ((Number) getObject("count")).doubleValue() ==((Number)(new Double(0))).doubleValue()) ? Boolean.TRUE : Boolean.FALSE))).booleanValue();} catch (Exception e) {Logger.debug(this,"Boolean expression in line {} could not be evaluated. False returned. Consider using the 'isDefined' ISML function.",25,e);}if (_boolean_result) { %>
		</tr>
		<tr>
	<% } else { %>
		<td>
			<img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/space.gif" width="12" height="1" alt="" border="0"/>
		</td>
		<td class="overview_vertical_line">
			<img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/space.gif" width="1" height="2" alt="" border="0"/>
		</td>
		<td>
			<img src="<%=context.getFormattedValue(context.webRoot(),null)%>/images/space.gif" width="12" height="1" alt="" border="0"/>
		</td>
	<% } %>
<% printFooter(out); %>