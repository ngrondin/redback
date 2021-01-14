<rb-tab-section 
	#<%=id%>
	style="<%=config.inlineStyle%>" <%
if(typeof parents.tab != 'undefined') { %>
	[activator]="<%=parents.tab%>" <%
} %> >	
	#content#
</rb-tab-section>
