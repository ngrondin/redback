<% var action = config.getString('action');
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {	
%><md-button 
	class="md-primary md-raised" 
	ng-click="<%=config.getString('action')%>('<%=config.getString('param')%>');"
	ng-show="<%=config.getString('show')%>"><%=config.getString('label')%></md-button><%
} %> 	