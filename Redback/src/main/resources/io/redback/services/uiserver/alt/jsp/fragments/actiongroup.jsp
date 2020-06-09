<mat-menu
	#<%=id%>="matMenu">
<% 
var list = config.getList('actions'); 
var noShow = "";
for(var i = 0; i < list.size(); i++) { 
	var actionConfig = list.getObject(i);
	var showExpr = (actionConfig.get("show") != null ? actionConfig.getString('show') : 'true').replaceAll('object', dataset + '.selectedObject').replaceAll('relatedObject', dataset + '.relatedObject');
	if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
	if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
	if(i > 0)
		noShow = noShow + " && ";
	noShow = noShow + "!(" + showExpr + ")"
	var action = actionConfig.get("action") != null ? actionConfig.getString('action') : 'noAction'	
	if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {
	%>
	<button
		mat-menu-item
		(click)="<%=dataset%>.action('<%=action%>', '<%=actionConfig.getString('param')%>');"
		*ngIf="<%=showExpr%>">
		<%=actionConfig.getString('label') %>
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
if(config.getBoolean("round") == true) { %>
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
