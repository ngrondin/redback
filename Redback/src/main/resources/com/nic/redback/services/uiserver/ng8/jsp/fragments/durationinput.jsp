<% 
var showExpr = (config.get("show") != null ? config.getString('show') : 'true').replaceAll('object', dataset + '.selectedObject').replaceAll('relatedObject', dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-duration-input
	style="<%=config.getString('inlineStyle')%>"
	*ngIf="<%=showExpr%>"
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
	[editable]="<%=canWrite%>">
</rb-duration-input>
