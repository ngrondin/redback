<% 
var showExpr = (config.show != null ? config.show : 'true').split('object').join(parents.dataset + '.selectedObject').split('relatedObject').join(parents.dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = parents.dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = parents.dataset + '.relatedObject != null && (' + showExpr + ')';
var action = config.action != null ? config.action : 'noAction';
if(((action == 'create'  ||  action == 'save')  &&  canWrite) || ((!(action == 'create')  &&  !(action == 'save'))  &&  canExecute)) {
%><button 
	mat-button
	class="mat-mini-fab mat-primary rb-button" 
	(click)="<%=parents.dataset%>.action('<%=action%>', '<%=config.param%>');"
	*ngIf="<%=showExpr%>"><%=config.label%>
	<mat-icon class="md-hue-3" ><%=(config.icon == null ? "" : config.icon) %></mat-icon>
</button><%
} %>
