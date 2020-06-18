<rb-filedrop <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(typeof fileset != 'undefined') { %>
	[uploader]="<%=fileset%>.uploader"
	(dropped)="<%=fileset%>.upload($event)" <%
} %> >
</rb-filedrop>
