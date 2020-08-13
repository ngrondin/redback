<rb-tab
	#<%=id%>="tab"
	[id]="'<%=id%>'"
	[label]="'<%=config.label%>'"
	style="<%=config.inlineStyle%>"
	[active]="<%=parents.tabsection%>.isTabVisible(<%=id%>)"
	[hidden]="!<%=id%>.active"
	(initialised)="<%=parents.tabsection%>.register(<%=id%>, <%=config.isdefault != null ? config.isdefault : false%>)">
	#content#
</rb-tab>
