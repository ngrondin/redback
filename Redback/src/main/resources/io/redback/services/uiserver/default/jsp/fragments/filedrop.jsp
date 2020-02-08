<rb-filedrop <% 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
}
if(typeof fileset != 'undefined') { %>
	[uploader]="<%=fileset%>.uploader"
	(dropped)="<%=fileset%>.upload($event)" <%
} %> >
</rb-filedrop>
