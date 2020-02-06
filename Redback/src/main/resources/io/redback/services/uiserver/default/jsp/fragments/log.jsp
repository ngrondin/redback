<% 
var showExpr = (config.get("show") != null ? config.getString('show') : 'true').replaceAll('object', dataset + '.selectedObject').replaceAll('relatedObject', dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-log
	style="<%=config.getString('inlineStyle')%>"
	*ngIf="<%=showExpr%>"<%
if(config.get('size') != null) { %>	
	[size]="<%=(config.get("size") == null ? 20 : config.getString("size")) %>" <%
} 
if(typeof dataset != 'undefined') { %>	
	[list]="<%=dataset%>.list" <%
} 
if(config.get('userattribute') != null) { %>	
	[userattribute]="'<%=config.getString("userattribute")%>'" <%
} 
if(config.get('dateattribute') != null) { %>	
	[dateattribute]="'<%=config.getString("dateattribute")%>'" <%
} 
if(config.get('entryattribute') != null) { %>	
	[entryattribute]="'<%=config.getString("entryattribute")%>'" <%
} 
if(config.get('categoryattribute') != null) { %>	
	[categoryattribute]="'<%=config.getString("categoryattribute")%>'" <%
} %>
	[editable]="<%=canWrite%>" <%
if(typeof dataset != 'undefined') { %>
	(posted)="<%=dataset%>.action('create', $event)" <%
} %> >
</rb-log>
