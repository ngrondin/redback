<rb-tab-section 
	#<%=id%>="tabsection"
	style="<%=config.inlineStyle%>"
	[active]="<%=(typeof tab !== 'undefined' ? tab + ".active" : "true")%>">
	<div
		class="rb-tab-header-section">
		<div
			class="rb-tab-header"
			*ngFor="let tab of <%=id%>.tabs"
			[ngClass]="<%=id%>.isTabVisible(tab) ? 'rb-tabheader-active' : ''">			
			<button
				mat-button
				class="rb-tab-button" 
				(click)="<%=id%>.select(tab)">
				{{tab.label}}
			</button>
		</div>
	</div>
	<mat-divider></mat-divider>
	<div
		class="rb-tab-content-section">	
		#content#
	</div>
</rb-tab-section>
