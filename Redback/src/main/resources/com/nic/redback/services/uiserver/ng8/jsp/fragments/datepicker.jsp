<rb-datetime-input
	class="rb-input-margin"
	style="<%=config.getString('inlineStyle')%>"
	*ngIf="(<%=config.getString('show')%>)"
	[label]="'<%=config.getString("label")%>'"<%
if(config.get('icon') == null) {%>
	[icon]="'calendar_today'"<%	
} else if(config.getString('icon').indexOf(':') >= 0) {%>
	[icon]="'<%=config.getString('icon')%>'"<%
} else {%>
	[icon]="'<%=config.getString('icon')%>'"<%
}%>	
	[format]="'<%=(config.get("format") == null ? "YYYY-MM-DD HH:mm" : config.getString("format")) %>'"
	[size]="<%=(config.get("size") == null ? 14 : config.getString("size")) %>"
	[object]="<%=dataset%>.selectedObject"
	[attribute]="'<%=config.getString('attribute')%>'"
	[editable]="<%=canWrite%>" >
</rb-datetime-input>
