<rb-tab-content
	label="<%=config.getString('label')%>"
	style="<%=config.getString('inlineStyle')%>"
	ng-init="tabs.push('<%=config.getString('label')%>')"
	ng-show="selected_tab == '<%=config.getString('label')%>'">
	#content#
</rb-tab-content>