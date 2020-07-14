<% 
var showExpr = (config.show != null ? config.show : 'true').split('object').join(parents.dataset + '.selectedObject').split('relatedObject').join(parents.dataset + '.relatedObject');
if(showExpr.indexOf('.selectedObject.') > -1) showExpr = parents.dataset + '.selectedObject != null && (' + showExpr + ')';
if(showExpr.indexOf('.relatedObject.') > -1) showExpr = parents.dataset + '.relatedObject != null && (' + showExpr + ')';
%><rb-dynamicform
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>"<%
if(typeof parents.dataset != 'undefined') { %>	
	[list]="<%=parents.dataset%>.list"
	[isLoading]="<%=parents.dataset%>.isLoading" <%
} 
if(config.valueattribute != null) { %>	
	[valueattribute]="'<%=config.valueattribute%>'" <%
} 
if(config.typeattribute != null) { %>	
	[typeattribute]="'<%=config.typeattribute%>'" <%
} 
if(config.optionsattribute != null) { %>	
	[optionsattribute]="'<%=config.optionsattribute%>'" <%
} 
if(config.titleattribute != null) { %>	
	[titleattribute]="'<%=config.titleattribute%>'" <%
} 
if(config.detailattribute != null) { %>	
	[detailattribute]="'<%=config.detailattribute%>'" <%
}
if(config.labelattribute != null) { %>	
	[labelattribute]="'<%=config.labelattribute%>'" <%
}
if(config.orderattribute != null) { %>	
	[orderattribute]="'<%=config.orderattribute%>'" <%
}
if(config.categoryattribute != null) { %>	
	[categoryattribute]="'<%=config.categoryattribute%>'" <%
}
if(config.categoryorderattribute != null) { %>	
	[categoryorderattribute]="'<%=config.categoryorderattribute%>'" <%
}
if(config.dependencyattribute != null) { %>	
	[dependencyattribute]="'<%=config.dependencyattribute%>'" <%
}
if(config.dependencyvalueattribute != null) { %>	
	[dependencyvalueattribute]="'<%=config.dependencyvalueattribute%>'" <%
} %>
	[editable]="<%=canWrite%>" >
</rb-dynamicform>
