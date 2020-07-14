<rb-fileset 
	#<%=id%>="fileset" <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(typeof parents.dataset != 'undefined') { %>
	[relatedObject]="<%=parents.dataset%>.selectedObject" <%
} %>
	[active]="<%=(typeof parents.tab !== 'undefined' ? parents.tab + ".active" : "true")%>">
	#content#
</rb-fileset>
