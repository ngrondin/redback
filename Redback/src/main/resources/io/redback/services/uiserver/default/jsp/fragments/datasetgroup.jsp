<rb-datasetgroup 
	#<%=id%>="datasetgroup" <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
} %>
	[active]="<%=(typeof parents.tab !== 'undefined' ? parents.tab + ".active" : "true")%>">
	#content#
</rb-datasetgroup>
