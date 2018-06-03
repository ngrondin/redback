<rb-list-scroll 
	style="<%=config.getString('inlineStyle')%>">
	<md-progress-linear 
		md-mode="indeterminate" 
		ng-show="loading">
	</md-progress-linear>
	<md-list 
		flex="">
		<md-list-item 
			class="md-2-line" 
			ng-class="{ 'list-item-active': item == selectedObject }"
			ng-repeat="item in list" 
			ng-click="selectObject(item)">
			<div 
				class="md-list-item-text" 
				layout="column">
				<h3><%=config.getString('line1')%></h3>
				<h4><%=config.getString('line2')%></h4>
			</div>
		</md-list-item>
	</md-list>
</rb-list-scroll>