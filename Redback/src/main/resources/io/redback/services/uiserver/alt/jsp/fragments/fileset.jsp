<rb-fileset 
	#<%=id%>="fileset" <% 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}
if(typeof dataset != 'undefined') { %>
	[relatedObject]="<%=dataset%>.selectedObject" <%
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-fileset>
