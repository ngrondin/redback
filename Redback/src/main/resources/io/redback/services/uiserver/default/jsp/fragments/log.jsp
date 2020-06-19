<% 
var showExpr = (config.show != null ? config.show : 'true').split('object').join(dataset + '.selectedObject').split('relatedObject').join(dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-log
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>"<%
if(config.size != null) { %>	
	[size]="<%=(config.size == null ? 20 : config.size) %>" <%
} 
if(typeof dataset != 'undefined') { %>	
	[list]="<%=dataset%>.list" <%
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
if(typeof dataset != 'undefined') { %>
	(posted)="<%=dataset%>.action('create', $event)" <%
} %> >
</rb-log>
