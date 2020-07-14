<% 
var showExpr = (config.show != null ? config.show : 'true').split('object').join(parents.dataset + '.selectedObject').split('relatedObject').join(parents.dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = parents.dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = parents.dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-log
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>"<%
if(config.size != null) { %>	
	[size]="<%=(config.size == null ? 20 : config.size) %>" <%
} 
if(typeof parents.dataset != 'undefined') { %>	
	[list]="<%=parents.dataset%>.list" <%
} 
if(config.userattribute != null) { %>	
	[userattribute]="'<%=config.userattribute%>'" <%
} 
if(config.dateattribute != null) { %>	
	[dateattribute]="'<%=config.dateattribute%>'" <%
} 
if(config.entryattribute != null) { %>	
	[entryattribute]="'<%=config.entryattribute%>'" <%
} 
if(config.categoryattribute != null) { %>	
	[categoryattribute]="'<%=config.categoryattribute%>'" <%
} %>
	[editable]="<%=canWrite%>" <%
if(typeof parents.dataset != 'undefined') { %>
	(posted)="<%=parents.dataset%>.action('create', $event)" <%
} %> >
</rb-log>
