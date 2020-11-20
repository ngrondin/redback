<rb-graph
	class="rb-input-margin"
	style="<%=config.inlineStyle%>"
	[type]="'<%=config.graphtype%>'"
	[label]="'<%=config.label%>'"
	[series]="<%=utils.convertDataEntityToAttributeString(config.series)%>"
	[categories]="<%=utils.convertDataEntityToAttributeString(config.categories)%>"
	[value]="<%=utils.convertDataEntityToAttributeString(config.value)%>"
	[min]="'<%=config.min%>'"
	[max]="'<%=config.max%>'" <%
if(parents.aggregateset != null) { %>	
	[aggregates]="<%=parents.aggregateset%>.aggregates"  
	(selectDimensions)="<%=parents.aggregateset%>.selectDimensions($event)"<%
} %>>
</rb-graph>
