<rb-tab-section 
	#<%=id%>
	style="<%=config.inlineStyle%>" <%
if(typeof parents.tab != 'undefined') { %>
	[tab]="<%=parents.tab%>" <%
} %> >	
	#content#
</rb-tab-section>
