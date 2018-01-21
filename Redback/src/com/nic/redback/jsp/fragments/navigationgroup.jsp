<md-list-item 
	ng-click="menutoggle.group<%=config.getString('groupid')%> = !menutoggle.group<%=config.getString('groupid')%>">
	<md-icon>collections</md-icon>
	<span class="menugroup\" flex><%=config.getString('label')%></span>
	<md-icon ng-show="!menutoggle.group<%=config.getString('groupid')%>">expand_more</md-icon>
	<md-icon ng-show="menutoggle.group<%=config.getString('groupid')%>">expand_less</md-icon>
</md-list-item>
<md-divider></md-divider>
#content#
<md-divider ng-show="menutoggle.group<%=config.getString('groupid')%>"></md-divider>
	