<% 
var showExpr = (config.get("show") != null ? config.getString('show') : 'true').replace(/object/g, dataset + '.selectedObject').replace(/relatedObject/g, dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-switch-input
	class="rb-input-margin"
	style="<%=config.getString('inlineStyle')%>"
	*ngIf="<%=showExpr%>"
	[label]="'<%=config.getString("label")%>'"<%
if(config.get('icon') == null) {%>
	[icon]="'description'"<%	
} else if(config.getString('icon').indexOf(':') >= 0) {%>
	[icon]="'<%=config.getString('icon')%>'"<%
} else {%>
	[icon]="'<%=config.getString('icon')%>'"<%
}
if(dataset != null) { %>	
	[object]="<%=dataset%>.selectedObject" <%
} 
if(config.get('attribute') != null) { %>	
	[attribute]="'<%=(config.get("attribute") == null ? 20 : config.getString("attribute")) %>'" <%
} %>
	[editable]="<%=canWrite%>" >
</rb-switch-input>
