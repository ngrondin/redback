<% 
var showExpr = (config.show != null ? config.show : 'true').split('object').join(dataset + '.selectedObject').split('relatedObject').join(dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
var action = config.action != null ? config.action : 'noAction';
if(((action == 'create'  ||  action == 'save')  &&  canWrite) || ((!(action == 'create')  &&  !(action == 'save'))  &&  canExecute)) {
%><div>
	<button 
		mat-stroked-button
		class="mat-primary rb-button" 
		(click)="<%=dataset%>.action('<%=action%>', '<%=config.param%>');"
		*ngIf="<%=showExpr%>">
		<%=config.label%>
	</button>
</div><%
} %>
