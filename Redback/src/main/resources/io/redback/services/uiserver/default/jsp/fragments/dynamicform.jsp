<% 
var showExpr = (config.get("show") != null ? config.getString('show') : 'true').replaceAll('object', dataset + '.selectedObject').replaceAll('relatedObject', dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-dynamicform
	style="<%=config.getString('inlineStyle')%>"
	*ngIf="<%=showExpr%>"<%
if(typeof dataset != 'undefined') { %>	
	[list]="<%=dataset%>.list" <%
} 
if(config.getString('valueattribute') != null) { %>	
	[valueattribute]="'<%=config.getString("valueattribute")%>'" <%
} 
if(config.getString('typeattribute') != null) { %>	
	[typeattribute]="'<%=config.getString("typeattribute")%>'" <%
} 
if(config.getString('optionsattribute') != null) { %>	
	[optionsattribute]="'<%=config.getString("optionsattribute")%>'" <%
} 
if(config.getString('titleattribute') != null) { %>	
	[titleattribute]="'<%=config.getString("titleattribute")%>'" <%
} 
if(config.getString('detailattribute') != null) { %>	
	[detailattribute]="'<%=config.getString("detailattribute")%>'" <%
}
if(config.getString('labelattribute') != null) { %>	
	[labelattribute]="'<%=config.getString("labelattribute")%>'" <%
}
if(config.getString('orderattribute') != null) { %>	
	[orderattribute]="'<%=config.getString("orderattribute")%>'" <%
}
if(config.getString('categoryattribute') != null) { %>	
	[categoryattribute]="'<%=config.getString("categoryattribute")%>'" <%
}
if(config.getString('categoryorderattribute') != null) { %>	
	[categoryorderattribute]="'<%=config.getString("categoryorderattribute")%>'" <%
}
if(config.getString('dependencyattribute') != null) { %>	
	[dependencyattribute]="'<%=config.getString("dependencyattribute")%>'" <%
}
if(config.getString('dependencyvalueattribute') != null) { %>	
	[dependencyvalueattribute]="'<%=config.getString("dependencyvalueattribute")%>'" <%
} %>
	[editable]="<%=canWrite%>" >
</rb-dynamicform>
