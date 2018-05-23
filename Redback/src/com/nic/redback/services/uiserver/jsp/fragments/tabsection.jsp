<div 
	class="rb-tabsection"
	ng-controller="tab"
	style="<%=config.getString('inlineStyle')%>">
	<div
		class="rb-tabheadersection">
		<div
			class="rb-tabheader"
			ng-class="{ 'rb-tabheader-active': tab == selected_tab }"			
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
		class="rb-tabcontentsection">	
		#content#
	</div>
</div>