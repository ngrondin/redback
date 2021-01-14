<rb-gantt <%
if(typeof parents.datasetgroup != 'undefined' && config.series != null) { %>
	[datasetgroup]="<%=parents.datasetgroup%>" <%
} else if(typeof parents.dataset != 'undefined') { %>
	[dataset]="<%=parents.dataset%>" <%
} %>	
	[lanes]="<%=utils.convertDataEntityToAttributeString(config.lanes)%>"
	[series]="<%=utils.convertDataEntityToAttributeString(config.series)%>"
	(openModal)="openModal($event)">
</rb-gantt>
