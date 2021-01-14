<button 
	mat-button
	class="mat-mini-fab mat-primary rb-button" 
	[dataset]="<%=parents.dataset%>"
	[show]="'<%=rbutils.encode(config.show)%>'">
	<%=config.label%>
	<mat-icon class="md-hue-3" ><%=(config.icon == null ? "" : config.icon) %></mat-icon>
</button><%
} %>
