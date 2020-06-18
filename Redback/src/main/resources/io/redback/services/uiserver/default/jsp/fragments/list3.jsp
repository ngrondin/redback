<rb-list <%
if(config.view != null) { %>
	[view]="'<%=config.view%>'" <%
}
if(typeof dataset != 'undefined') { %>
	[list]="<%=dataset%>.list"
	[(selectedObject)]="<%=dataset%>.selectedObject"
	[isLoading]="<%=dataset%>.isLoading" <%
}
if(config.headerattribute != null) { %>
	[headerattribute]="'<%=config.headerattribute%>'" <%
}
if(config.subheadattribute != null) { %>
	[subheadattribute]="'<%=config.subheadattribute%>'" <%
}
if(config.supptextattribute != null) { %>
	[supptextattribute]="'<%=config.supptextattribute%>'" <%
}
if(config.sidetextattribute != null) { %>
	[sidetextattribute]="'<%=config.sidetextattribute%>'" <%
}
if(config.iconattribute != null) { %>
	[iconattribute]="'<%=config.iconattribute%>'" <%
}
if(config.colorattribute != null) { %>
	[colorattribute]="'<%=config.colorattribute%>'" <%
}
if(config.iconmap != null) { %>
	[iconmap]="<%=utils.convertDataEntityToAttributeString(config.iconmap)%>" <%
}
if(config.colormap != null) { %>
	[colormap]="<%=utils.convertDataEntityToAttributeString(config.colormap)%>" <%
} %>>
</rb-list>
