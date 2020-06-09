<rb-graph
	class="rb-input-margin"
	style="<%=config.getString('inlineStyle')%>"
	[type]="'<%=config.getString("graphtype")%>'"
	[label]="'<%=config.getString("label")%>'"
	[series]="<%=utils.convertDataEntityToAttributeString(config.getObject('series'))%>"
	[categories]="<%=utils.convertDataEntityToAttributeString(config.getObject('categories'))%>"
	[value]="<%=utils.convertDataEntityToAttributeString(config.getObject('value'))%>"
	[min]="'<%=config.getNumber("min")%>'"
	[max]="'<%=config.getNumber("max")%>'" <%
if(aggregateset != null) { %>	
	[aggregates]="<%=aggregateset%>.aggregates" <%
} %> >
</rb-graph>
