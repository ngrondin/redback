<rb-filelist <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(config.downloadonselect != null) { %>
	[downloadOnSelect]="<%=config.downloadonselect%>" <%
}
if(typeof fileset != 'undefined') { %>
	[list]="<%=fileset%>.list"
	[selectedFile]="<%=fileset%>.selectedFile"
	(selected)="<%=fileset%>.select($event)" <%
} %> >
</rb-filelist>
