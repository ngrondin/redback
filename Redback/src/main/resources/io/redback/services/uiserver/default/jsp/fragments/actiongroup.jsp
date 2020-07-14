<mat-menu
	#<%=id%>="matMenu">
<% 
var list = config.actions; 
var noShow = "";
for(var i = 0; i < list.length; i++) { 
	var actionConfig = list[i];
	var showExpr = (actionConfig.show != null ? actionConfig.show : 'true').split('object').join(parents.dataset + '.selectedObject').split('relatedObject').join(parents.dataset + '.relatedObject');
	if(showExpr.indexOf('.selectedObject.') > -1) showExpr = parents.dataset + '.selectedObject != null && (' + showExpr + ')';
	if(showExpr.indexOf('.relatedObject.') > -1) showExpr = parents.dataset + '.relatedObject != null && (' + showExpr + ')';
	if(i > 0)
		noShow = noShow + " && ";
	noShow = noShow + "!(" + showExpr + ")"
	var action = actionConfig.action != null ? actionConfig.action : 'noAction'	
	if(((action == 'create'  ||  action == 'save')  &&  canWrite) || ((action != 'create'  &&  action != 'save')  &&  canExecute)) {
	%>
	<button
		mat-menu-item
		(click)="<%=parents.dataset%>.action('<%=action%>', '<%=actionConfig.param%>');"
		*ngIf="<%=showExpr%>">
		<%=actionConfig.label %>
	</button><%
	}
}        		
%>
	<button
		mat-menu-item
		*ngIf="<%=noShow%>"
		[disabled]="true">
		No actions available
	</button>
</mat-menu> <%
if(config.round == true) { %>
<button
	mat-icon-button 
	matTooltip="Geenral actions for this record"
    [matTooltipShowDelay]="1000"
	class="mat-mini-fab mat-primary rb-button"
	[matMenuTriggerFor]="<%=id%>">
	<mat-icon>reorder</mat-icon>
</button><%
} else { %>
<div>
	<button
		mat-stroked-button 
		matTooltip="Geenral actions for this record"
	    [matTooltipShowDelay]="1000"
		class="mat-primary rb-button"
		[matMenuTriggerFor]="<%=id%>">
		Actions
	</button>
</div><%
} %>
