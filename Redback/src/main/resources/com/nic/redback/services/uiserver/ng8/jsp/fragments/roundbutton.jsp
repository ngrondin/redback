<% 
var show = config.get("show") != null ? config.getString('show') : 'true';
show = show.replace('object.', dataset + '.selectedObject.');
show = show.replace('relatedObject.', dataset + '.relatedObject.');
var action = config.get("action") != null ? config.getString('action') : 'noAction';
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {
%><button 
	mat-button
	class="md-fab md-mini md-primary rb-button" 
	(click)="<%=dataset%>.action('<%=action%>', '<%=config.getString('param')%>');"
	*ngIf="<%=dataset%>.selectedObject != null ? (<%=show%>) : false"><%=config.getString('label')%>
	<mat-icon class="md-hue-3" ><%=(config.get("icon") == null ? "" : config.getString("icon")) %></mat-icon>
</button><%
} %> 	