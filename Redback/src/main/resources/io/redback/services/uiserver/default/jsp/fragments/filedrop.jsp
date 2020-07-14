<rb-filedrop <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(typeof parents.fileset != 'undefined') { %>
	[uploader]="<%=parents.fileset%>.uploader"
	(dropped)="<%=parents.fileset%>.upload($event)" <%
} %> >
</rb-filedrop>
