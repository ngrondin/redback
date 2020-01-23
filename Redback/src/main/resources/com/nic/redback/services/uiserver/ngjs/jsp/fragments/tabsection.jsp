<rb-tab-section 
	style="<%=config.getString('inlineStyle')%>">
	<rb-tab-header-section>
		<rb-tab-header
			ng-class="{ 'rb-tabheader-active': tab == selected_tab }"			
			ng-repeat="tab in tabs">			
			<md-button 
				class="md-primary"
				ng-click="selectTab(tab)">
				{{tab}}
			</md-button>
		</rb-tab-header>
	</rb-tab-header-section>
	<md-divider></md-divider>
	<rb-tab-content-section
		class="">	
		#content#
	</rb-tab-content-section>
</rb-tab-section>