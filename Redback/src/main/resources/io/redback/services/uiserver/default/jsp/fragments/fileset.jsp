<rb-fileset 
	#<%=id%>="fileset" <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(typeof parents.dataset != 'undefined') { %>
	[dataset]="<%=parents.dataset%>" <%
} %>
	[active]="<%=(typeof parents.tab !== 'undefined' ? parents.tab + ".active" : "true")%>">
	#content#
</rb-fileset>
