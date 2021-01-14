<rb-filelist <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(config.downloadonselect != null) { %>
	[downloadOnSelect]="<%=config.downloadonselect%>" <%
}
if(typeof parents.fileset != 'undefined') { %>
	[fileset]="<%=parents.fileset%>" <%
} %> >
</rb-filelist>
