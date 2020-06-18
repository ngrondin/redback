<rb-fileset 
	#<%=id%>="fileset" <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(typeof dataset != 'undefined') { %>
	[relatedObject]="<%=dataset%>.selectedObject" <%
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-fileset>
