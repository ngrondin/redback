<div 
	class="tabsection"
	ng-controller="tab"
	style="<%=config.getString('inlineStyle')%>">
	<div
		class="tabheadersection">
		<div
			class="tabheader"
			ng-class="{ 'tabheader-active': tab == selected_tab }"			
			ng-repeat="tab in tabs">			
			<md-button 
				class="md-primary"
				ng-click="selectTab(tab)">
				{{tab}}
			</md-button>
		</div>
	</div>
	<md-divider></md-divider>
	<div
		class="tabcontentsection">	
		#content#
	</div>
</div>