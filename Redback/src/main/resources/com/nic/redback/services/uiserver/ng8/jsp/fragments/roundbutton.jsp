<% var action = config.get("action") != null ? config.getString('action') : 'noAction';
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {
%><button 
	class="md-fab md-mini md-primary rb-button" 
	(click)="action('<%=action%>', '<%=config.getString('param')%>');"
	*ngIf="<%=config.getString('show')%>"><%=config.getString('label')%>
	<!-- <mat-icon class="md-hue-3" ><%=(config.get("icon") == null ? "" : config.getString("icon")) %></mat-icon> -->
	</button><%
} %> 	