<rb-table <%
if(typeof parents.dataset != 'undefined') { %>
	[list]="<%=parents.dataset%>.list"
	[(selectedObject)]="<%=parents.dataset%>.selectedObject" <%
} %>	
	[columns]="<%=utils.convertDataEntityToAttributeString(config.columns)%>"
	(deleteSelected)="<%=parents.dataset%>.action('delete')"
	(filterSort)="<%=parents.dataset%>.filterSort($event)">
</rb-table>
