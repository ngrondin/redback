<% 
var showExpr = (config.get("show") != null ? config.getString('show') : 'true').replaceAll('object', dataset + '.selectedObject').replaceAll('relatedObject', dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
var action = config.get("action") != null ? config.getString('action') : 'noAction';
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {
%><button 
	mat-button
	class="mat-mini-fab mat-primary rb-button" 
	(click)="<%=dataset%>.action('<%=action%>', '<%=config.getString('param')%>');"
	*ngIf="<%=showExpr%>"><%=config.getString('label')%>
	<mat-icon class="md-hue-3" ><%=(config.get("icon") == null ? "" : config.getString("icon")) %></mat-icon>
</button><%
} %>
