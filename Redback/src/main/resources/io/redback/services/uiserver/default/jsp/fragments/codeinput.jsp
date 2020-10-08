<% 
var showExpr = (config.show != null ? config.show : 'true').split('object').join(parents.dataset + '.selectedObject').split('relatedObject').join(parents.dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = parents.dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = parents.dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-code-input
	class="rb-input-margin"
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>"<%
if(config.size != null) { %>	
	[size]="<%=config.size%>" <%
} 
if(config.mode != null) { %>	
	[mode]="'<%=config.mode%>'" <%
} else {%>	
	[mode]="'javascript'" <%
} 
if(parents.dataset != null) { %>	
	[object]="<%=parents.dataset%>.selectedObject" <%
} 
if(config.attribute != null) { %>	
	[attribute]="'<%=(config.attribute == null ? 20 : config.attribute) %>'" <%
}
if(config.rows != null) { %>	
	[rows]="<%=config.rows%>" <%
} %>	
	[editable]="<%=canWrite%>"
	>
</rb-code-input>
