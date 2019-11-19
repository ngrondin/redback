<rb-input
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
	[object]="<%=datasetname%>.selectedObject"
	[attribute]="'<%=config.getString('attribute')%>'"
	[editable]="<%=canWrite%>"
	>
</rb-input>
