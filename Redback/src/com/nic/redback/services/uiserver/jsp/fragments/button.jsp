<% var action = config.get("action") != null ? config.getString('action') : 'noAction';
if(((action.equals('create')  ||  action.equals('save'))  &&  canWrite) || ((!action.equals('create')  &&  !action.equals('save'))  &&  canExecute)) {	
%><md-button 
	class="md-primary md-raised" 
	ng-click="<%=config.getString('action')%>('<%=config.getString('param')%>');"
	ng-show="<%=config.getString('show')%>"><%=config.getString('label')%></md-button><%
} %> 	