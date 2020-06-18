<rb-search
	style="<%=config.inlineStyle%>"
	[icon]="'<%=config.icon%>'"
	[size]="<%=(config.size == null ? 12 : config.size) %>"<%
if(config.filter != null) { %>
	[filterconfig]="<%=utils.convertDataMapToAttributeString(config.filter)%>"<%
} 
if(config.sort != null) { %>
	[sortconfig]="<%=utils.convertDataMapToAttributeString(config.sort)%>"<%
} %>

	(search)="<%=dataset%>.search($event)"
	(filter)="<%=dataset%>.filter($event)"
	(sort)="<%=dataset%>.sort($event)">
</rb-search>
