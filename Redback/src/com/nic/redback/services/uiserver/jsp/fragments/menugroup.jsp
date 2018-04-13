<md-list-item 
	class="rb-menu-line"
	ng-click="menutoggle.group<%=config.getString('_id')%> = !menutoggle.group<%=config.getString('_id')%>">
	<md-icon md-svg-icon="<%=config.getString('icon')%>"></md-icon>
	<span 
		class="menugroup" 
		flex 
		ng-show="largemenu">
		<%=config.getString('label')%>
	</span>
	<md-icon 
		ng-show="largemenu  &&  !menutoggle.group<%=config.getString('_id')%>">
		expand_more
	</md-icon>
	<md-icon 
		ng-show="largemenu  &&  menutoggle.group<%=config.getString('_id')%>">
		expand_less
	</md-icon>
</md-list-item>
<md-divider></md-divider>
#content#
<md-divider 
	ng-show="menutoggle.group<%=config.getString('_id')%>">
</md-divider>
	