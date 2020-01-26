<% var action = config.get("action") != null ? config.getString('action') : 'noAction';
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {
%><md-button 
	class="md-fab md-mini md-primary rb-button" 
	ng-click="action('<%=action%>', '<%=config.getString('param')%>');"
	ng-show="<%=config.getString('show')%>"><%=config.getString('label')%>
	<md-icon class="md-hue-3" ><%=(config.get("icon") == null ? "" : config.getString("icon")) %></md-icon>
	</md-button><%
} %> 	