<rb-gantt <%
if(typeof parents.datasetgroup != 'undefined' && config.series != null) { %>
	[lists]="<%=parents.datasetgroup%>.lists"
	[(selectedObject)]="<%=parents.datasetgroup%>.selectedObject" <%
} else if(typeof parents.dataset != 'undefined') { %>
	[list]="<%=parents.dataset%>.list"
	[(selectedObject)]="<%=parents.dataset%>.selectedObject" <%
} %>	
	[lanes]="<%=utils.convertDataEntityToAttributeString(config.lanes)%>"
	[series]="<%=utils.convertDataEntityToAttributeString(config.series)%>"
	(openModal)="openModal($event)">
</rb-gantt>
