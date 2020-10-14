<% 
var showExpr = (config.show != null ? config.show : 'true').split('object').join(parents.dataset + '.selectedObject').split('relatedObject').join(parents.dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = parents.dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = parents.dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-address-input
	class="rb-input-margin"
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>"
	[label]="'<%=config.label%>'"<%
if(config.icon == null) {%>
	[icon]="'description'"<%	
} else if(config.icon.indexOf(':') >= 0) {%>
	[icon]="'<%=config.icon%>'"<%
} else {%>
	[icon]="'<%=config.icon%>'"<%
}
if(config.size != null) { %>	
	[size]="<%=config.size%>" <%
} 
if(parents.dataset != null) { %>	
	[object]="<%=parents.dataset%>.selectedObject" <%
} 
if(config.attribute != null) { %>	
	[attribute]="'<%=config.attribute%>'" <%
}
if(config.centerattribute != null) { %>	
	[centerattribute]="'<%=config.centerattribute%>'" <%
}%>	
	[editable]="<%=canWrite%>">
</rb-address-input>
