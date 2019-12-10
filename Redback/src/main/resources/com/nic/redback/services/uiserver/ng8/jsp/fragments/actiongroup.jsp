<mat-menu
	#<%=id%>="matMenu">
<% var list = config.getList('actions'); 
for(var i = 0; i < list.size(); i++) { 
	var actionConfig = list.getObject(i);
	var action = actionConfig.get("action") != null ? actionConfig.getString('action') : 'noAction'	
	if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {
	%>
		<button
			mat-menu-item
			(click)="<%=dataset%>.action('<%=action%>', '<%=actionConfig.getString('param')%>');"
			*ngIf="<%=dataset%>.selectedObject != null ? (<%=actionConfig.getString('show').replace('object.', dataset + '.selectedObject.')%>) : false"><%=actionConfig.getString('label') %>
		</button><%
	}
}        		
%>	
</mat-menu>
<button
	mat-icon-button 
	class="md-fab md-mini md-primary rb-form-button"
	[matMenuTriggerFor]="<%=id%>">
	<mat-icon>reorder</mat-icon>
</button>
