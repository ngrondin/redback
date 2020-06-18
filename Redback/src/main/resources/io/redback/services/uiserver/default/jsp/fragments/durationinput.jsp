<% 
var showExpr = (config.show != null ? config.show : 'true').replace(/object/g, dataset + '.selectedObject').replace(/relatedObject/g, dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-duration-input
	class="rb-input-margin"
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>"
	[label]="'<%=config.label%>'"<%
if(config.icon == null) {%>
	[icon]="'timelapse'"<%	
} else if(config.icon.indexOf(':') >= 0) {%>
	[icon]="'<%=config.icon%>'"<%
} else {%>
	[icon]="'<%=config.icon%>'"<%
}
if(config.size != null) { %>	
	[size]="<%=(config.size == null ? 20 : config.size) %>" <%
} 
if(dataset != null) { %>	
	[object]="<%=dataset%>.selectedObject" <%
} 
if(config.attribute != null) { %>	
	[attribute]="'<%=(config.attribute == null ? 20 : config.attribute) %>'" <%
}%>	
	[editable]="<%=canWrite%>">
</rb-duration-input>
