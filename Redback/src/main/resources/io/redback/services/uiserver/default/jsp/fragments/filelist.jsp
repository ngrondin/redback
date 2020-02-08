<rb-filelist <% 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}
if(config.get('downloadonselect') != null) { %>
	[downloadOnSelect]="<%=config.getBoolean('downloadonselect')%>" <%
}
if(typeof fileset != 'undefined') { %>
	[list]="<%=fileset%>.list"
	[selectedFile]="<%=fileset%>.selectedFile"
	(selected)="<%=fileset%>.select($event)" <%
} %> >
</rb-filelist>
