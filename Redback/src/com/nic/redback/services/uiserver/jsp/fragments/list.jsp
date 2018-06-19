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
			ng-class="{ 'list-item-active': item == selected }"
			ng-repeat="item in list" 
			ng-click="select(item)">
			<div 
				class="md-list-item-text" 
				layout=row><%
if(config.get('initials') != null) {%>
				<div
					class="rb-list-item-circle">
					<span class="rb-list-item-initials"><%=config.getString('initials')%></span>
				</div><%
}%>
				<div
					layout="column">
					<h3><%=config.getString('line1')%></h3>
					<h4><%=config.getString('line2')%></h4>
				</div>
			</div>
		</md-list-item>
	</md-list>
</rb-list-scroll>