<rb-datasetgroup 
	#<%=id%>="datasetgroup" <% 
if(config.get('inlineStyle') != null) {%>
	style="<%=config.getString('inlineStyle')%>"<%
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-datasetgroup>
