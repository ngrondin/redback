<div 
	class="tabsection"
	style="<%=config.getString('inlineStyle')%>">
	<div
		class="tabheadersection">
		<div
			class="tabheader"
			ng-repeat="tab in rb_tabs">			
			<md-button 
				class="md-primary"
				ng-click="$parent.rb_selected_tab = tab">
				{{tab}}
			</md-button>
		</div>
	</div>
	<md-divider></md-divider>
	<div
		class="tabcontentsection">	
		#content#
	</div>
</dov>