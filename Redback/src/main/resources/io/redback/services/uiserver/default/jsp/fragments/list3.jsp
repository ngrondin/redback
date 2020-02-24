<rb-list <%
if(config.get('view') != null) { %>
	[view]="'<%=config.getString('view')%>'" <%
}
if(typeof dataset != 'undefined') { %>
	[list]="<%=dataset%>.list"
	[(selectedObject)]="<%=dataset%>.selectedObject"
	[isLoading]="<%=dataset%>.isLoading" <%
}
if(config.get('headerattribute') != null) { %>
	[headerattribute]="'<%=config.getString('headerattribute')%>'" <%
}
if(config.get('subheadattribute') != null) { %>
	[subheadattribute]="'<%=config.getString('subheadattribute')%>'" <%
}
if(config.get('supptextattribute') != null) { %>
	[supptextattribute]="'<%=config.getString('supptextattribute')%>'" <%
}
if(config.get('sidetextattribute') != null) { %>
	[sidetextattribute]="'<%=config.getString('sidetextattribute')%>'" <%
}
if(config.get('iconattribute') != null) { %>
	[iconattribute]="'<%=config.getString('iconattribute')%>'" <%
}
if(config.get('colorattribute') != null) { %>
	[colorattribute]="'<%=config.getString('colorattribute')%>'" <%
}
if(config.get('iconmap') != null) { %>
	[iconmap]="<%=utils.convertDataEntityToAttributeString(config.getObject('iconmap'))%>" <%
}
if(config.get('colormap') != null) { %>
	[colormap]="<%=utils.convertDataEntityToAttributeString(config.getObject('colormap'))%>" <%
} %>>
</rb-list>
