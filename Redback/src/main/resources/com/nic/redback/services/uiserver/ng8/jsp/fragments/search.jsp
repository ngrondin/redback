<rb-search
	style="<%=config.getString('inlineStyle')%>"
	[icon]="'<%=config.getString('icon')%>'"
	[size]="<%=(config.get("size") == null ? 50 : config.getString("size")) %>"
	>
</rb-search>
