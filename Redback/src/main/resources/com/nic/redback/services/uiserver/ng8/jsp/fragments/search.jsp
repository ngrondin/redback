<rb-search
	style="<%=config.getString('inlineStyle')%>"
	[icon]="'<%=config.getString('icon')%>'"
	[size]="<%=(config.get("size") == null ? 50 : config.getString("size")) %>"<%
if(config.get('filter') != null) { %>
	[filterconfig]="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(config.getObject('filter'))%>"<%
} %>
	(search)="<%=dataset%>.search($event)"
	(filter)="<%=dataset%>.filter($event)">
</rb-search>
