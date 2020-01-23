<md-menu md-offset="0 60">
	<md-button 
		class="md-fab md-mini md-primary rb-form-button"
		ng-click="$mdOpenMenu($event)">
		<md-icon>reorder</md-icon>
	</md-button>
	<md-menu-content>
<% var list = config.getList('actions'); 
for(var i = 0; i < list.size(); i++) { 
	var actionConfig = list.getObject(i);
	var action = actionConfig.get("action") != null ? actionConfig.getString('action') : 'noAction'	
	if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {
	%>
        		<md-menu-item>
        			<md-button
				ng-click="action('<%=action%>', '<%=actionConfig.getString('param')%>');"
				ng-disabled="!(<%=actionConfig.getString('show')%>)"><%=actionConfig.getString('label') %></md-button>
        		</md-menu-item><%
	}
}        		
%>
	</md-menu-content>	
</md-menu>