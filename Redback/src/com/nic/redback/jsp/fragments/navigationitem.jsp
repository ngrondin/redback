<md-list-item 
	ng-show="menutoggle.group<%=config.getString('groupid')%>"
	ng-click="page = '../view/<%=config.getString('view')%>'">
	<md-icon>explore</md-icon>
	<span class="menuitem" flex><%=config.getString('label')%></span>
</md-list-item>				