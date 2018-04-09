<md-list-item 
	ng-show="menutoggle.group<%=config.getString('group')%>"
	ng-click="page = '../view/<%=config.getString('view')%>'">
	<md-icon md-font-icon="desktopicon-<%=config.getString('icon')%>"></md-icon>
	<span class="menuitem" flex><%=config.getString('label')%></span>
</md-list-item>				