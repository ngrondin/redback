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
	[object]="<%=parents.dataset%>.objectname"
	(search)="<%=parents.dataset%>.search($event)"
	(filterSort)="<%=parents.dataset%>.filterSort($event)">
</rb-search>
