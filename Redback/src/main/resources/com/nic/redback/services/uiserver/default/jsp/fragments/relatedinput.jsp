<% 
var showExpr = (config.get("show") != null ? config.getString('show') : 'true').replaceAll('object', dataset + '.selectedObject').replaceAll('relatedObject', dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-related-input
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
}%>	
	[size]="<%=(config.get("size") == null ? 20 : config.getString("size")) %>"
	[object]="<%=dataset%>.selectedObject"
	[attribute]="'<%=config.getString('attribute')%>'"
	[displayattribute]="'<%=config.getString('displayattribute')%>'"<%
if(config.get("parentattribute")) { %>
	[parentattribute]="'<%=config.getString('parentattribute')%>'"<% 
}
if(config.get("childattribute")) { %>
	[childattribute]="'<%=config.getString('childattribute')%>'"<% 
} %>
	[editable]="<%=canWrite%>">
</rb-related-input>
