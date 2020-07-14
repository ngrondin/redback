<rb-filelist <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(config.downloadonselect != null) { %>
	[downloadOnSelect]="<%=config.downloadonselect%>" <%
}
if(typeof parents.fileset != 'undefined') { %>
	[list]="<%=parents.fileset%>.list"
	[selectedFile]="<%=parents.fileset%>.selectedFile"
	(selected)="<%=parents.fileset%>.select($event)" <%
} %> >
</rb-filelist>
