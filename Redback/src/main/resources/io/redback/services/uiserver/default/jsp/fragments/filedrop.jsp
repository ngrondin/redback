<rb-filedrop <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(typeof parents.fileset != 'undefined') { %>
	[fileset]="<%=parents.fileset%>" <%
} %> >
	#content#
</rb-filedrop>
