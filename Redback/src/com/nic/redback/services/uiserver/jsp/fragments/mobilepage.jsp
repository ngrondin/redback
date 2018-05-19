<div
	class="mobilepage"
	style="<%=config.getString('inlineStyle')%>"
	ng-init="pages.push('<%=config.getString('name')%>')"
	ng-show="top_page == '<%=config.getString('name')%>'">
	#content#
</div>