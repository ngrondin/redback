<rb-related-input
	style="<%=config.getString('inlineStyle')%>"
	*ngIf="(<%=config.getString('show')%>)"
	[label]="'<%=config.getString("label")%>'"<%
if(config.get('icon') == null) {%>
	[icon]="'description'"<%	
} else if(config.getString('icon').indexOf(':') >= 0) {%>
	[icon]="'<%=config.getString('icon')%>'"<%
} else {%>
	[icon]="'<%=config.getString('icon')%>'"<%
}%>	
	[size]="<%=(config.get("size") == null ? 50 : config.getString("size")) %>"
	[object]="<%=dataset%>.selectedObject"
	[attribute]="'<%=config.getString('attribute')%>'"
	[displayattribute]="'<%=config.getString('displayattribute')%>'"<%
if(config.get("parentattribute")) { %>
	[parentattribute]="<%=config.getString('parentattribute')%>"<% 
}
if(config.get("childattribute")) { %>
	[childattribute]="<%=config.getString('childattribute')%>"<% 
} %>
	[editable]="<%=canWrite%>"
	>
</rb-related-input>
