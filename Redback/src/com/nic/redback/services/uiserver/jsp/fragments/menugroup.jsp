<md-list-item 
	ng-click="menutoggle.group<%=config.getString('_id')%> = !menutoggle.group<%=config.getString('_id')%>">
	<md-icon md-font-icon="desktopicon-<%=config.getString('icon')%>" class="desktopicon-design-tool"></md-icon>
	<span class="menugroup" flex><%=config.getString('label')%></span>
	<md-icon ng-show="!menutoggle.group<%=config.getString('_id')%>">expand_more</md-icon>
	<md-icon ng-show="menutoggle.group<%=config.getString('_id')%>">expand_less</md-icon>
</md-list-item>
<md-divider></md-divider>
#content#
<md-divider ng-show="menutoggle.group<%=config.getString('_id')%>"></md-divider>
	