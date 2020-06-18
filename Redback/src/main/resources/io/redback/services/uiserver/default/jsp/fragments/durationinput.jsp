<% 
var showExpr = (config.get("show") != null ? config.getString('show') : 'true').replace(/object/g, dataset + '.selectedObject').replace(/relatedObject/g, dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-duration-input
	class="rb-input-margin"
	style="<%=config.getString('inlineStyle')%>"
	*ngIf="<%=showExpr%>"
	[label]="'<%=config.getString("label")%>'"<%
if(config.get('icon') == null) {%>
	[icon]="'timelapse'"<%	
} else if(config.getString('icon').indexOf(':') >= 0) {%>
	[icon]="'<%=config.getString('icon')%>'"<%
} else {%>
	[icon]="'<%=config.getString('icon')%>'"<%
}
if(config.get('size') != null) { %>	
	[size]="<%=(config.get("size") == null ? 20 : config.getString("size")) %>" <%
} 
if(dataset != null) { %>	
	[object]="<%=dataset%>.selectedObject" <%
} 
if(config.get('attribute') != null) { %>	
	[attribute]="'<%=(config.get("attribute") == null ? 20 : config.getString("attribute")) %>'" <%
}%>	
	[editable]="<%=canWrite%>">
</rb-duration-input>
