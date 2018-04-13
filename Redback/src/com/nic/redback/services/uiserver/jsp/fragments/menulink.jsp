<md-list-item 
	ng-show="menutoggle.group<%=config.getString('group')%>"
	ng-click="page = '../view/<%=config.getString('view')%>'">
	<md-icon md-svg-icon="<%=config.getString('icon')%>"></md-icon>
	<span 
		class="menuitem"
		ng-show="largemenu" 
		flex>
		<%=config.getString('label')%>
	</span>
</md-list-item>				