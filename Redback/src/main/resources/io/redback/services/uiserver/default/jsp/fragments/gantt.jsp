<rb-gantt
	[lists]="<%=parents.datasetgroup%>.lists"
	[lanes]="<%=utils.convertDataEntityToAttributeString(config.lanes)%>"
	[series]="<%=utils.convertDataEntityToAttributeString(config.series)%>"
	(openModal)="openModal($event)"
	[(selectedObject)]="<%=parents.datasetgroup%>.selectedObject">
</rb-gantt>
