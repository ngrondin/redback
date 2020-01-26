<% var action = config.get("action") != null ? config.getString('action') : 'noAction';
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {	
%><md-button 
	class="rb-button md-button md-primary md-raised" 
	ng-click="action('<%=action%>', '<%=config.getString('param')%>');"
	ng-show="<%=config.getString('show')%>"><%
if(config.get("icon") != null) {	%>
	<md-icon><%=config.getString("icon") %></md-icon><%
}
if(config.get("label") != null) {%>
	<%=config.getString('label')%><%
}%>
</md-button><%
} %> 	