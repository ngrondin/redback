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
	[object]="<%=dataset%>.objectname"
	(search)="<%=dataset%>.search($event)"
	(filterSort)="<%=dataset%>.filterSort($event)">
</rb-search>
