<md-list-item 
	class="rb-menu-link"
	ng-show="menutoggle.group<%=config.getString('group')%>"
	ng-click="navigate('<%=config.getString('view')%>', '<%=config.getString('label')%>')">
	<md-icon md-svg-icon="<%=config.getString('icon')%>"></md-icon>
	<span 
		ng-show="largemenu" 
		flex>
		<%=config.getString('label')%>
	</span>
</md-list-item>				