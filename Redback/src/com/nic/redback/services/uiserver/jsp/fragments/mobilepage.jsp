<div
	ng-controller="mobilepage" 
	class="rb-mobile-page"
	ng-init="pages.push('<%=config.getString('name')%>')"
	ng-show="topPage == '<%=config.getString('name')%>'">
	#content#
</div>