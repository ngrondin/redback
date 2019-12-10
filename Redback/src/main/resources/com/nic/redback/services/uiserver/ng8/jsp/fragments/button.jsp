<% var action = config.get("action") != null ? config.getString('action') : 'noAction';
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {	
%><button
 	mat-button
	class="rb-button md-button md-primary md-raised" 
	(click)="<%=dataset%>.action('<%=action%>', '<%=config.getString('param')%>');"
	*ngIf="<%=config.getString('show')%>"><%
if(config.get("icon") != null) {	%>
	<mat-icon><%=config.getString("icon") %></mat-icon><%
}
if(config.get("label") != null) {%>
	<%=config.getString('label')%><%
}%>
</button><%
} %> 	