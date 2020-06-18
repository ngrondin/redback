<rb-datasetgroup 
	#<%=id%>="datasetgroup" <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
} %>
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	#content#
</rb-datasetgroup>
