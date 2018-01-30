<div
	class="tabcontent"
	label="<%=config.getString('label')%>"
	style="<%=config.getString('inlineStyle')%>"
	ng-init="rb_tabs == null ? rb_tabs = ['<%=config.getString('label')%>'] : rb_tabs.push('<%=config.getString('label')%>')"
	ng-show="rb_selected_tab == '<%=config.getString('label')%>'">
	#content#
</div>