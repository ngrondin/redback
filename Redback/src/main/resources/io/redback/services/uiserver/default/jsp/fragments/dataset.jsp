<rb-dataset 
	#<%=id%> <% 
if(config.inlineStyle != null) {%>
	style="<%=config.inlineStyle%>"<%
}
if(config.object != null) { %>
	[object]="'<%=config.object%>'"
	[fetchAll]="<%=config.fetchall%>"<% 
}
if(config.basefilter != null) { %>
	[baseFilter]="<%=utils.convertDataMapToAttributeString(config.basefilter)%>"<% 
}
if(config.basesort != null) { %>
	[baseSort]="<%=utils.convertDataMapToAttributeString(config.basesort)%>"<% 
}  
if(typeof parents.dataset == 'undefined' && typeof parents.datasetgroup == 'undefined') { %>
	[dataTarget]="currentTarget.dataTarget" <%
} else if(config.master != null) { %>
	[relatedFilter]="<%=utils.convertDataMapToAttributeString(config.master.relationship)%>" 
	[dataset]="<%=parents.dataset%>"<% 
}  
if(typeof parents.datasetgroup != 'undefined' && config.name != null) { %>
	[datasetgroup]="<%=parents.datasetgroup%>" <%
} 
if(typeof parents.tab != 'undefined') { %>
	[activator]="<%=parents.tab%>" <%
} %>
	[name]="'<%=config.name%>'"
	(openModal)="openModal($event)">
	#content#
</rb-dataset>
