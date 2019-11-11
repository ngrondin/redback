<rb-list-scroll 
	style="<%=config.getString('inlineStyle')%>">
	<!-- <md-progress-linear 
		md-mode="indeterminate" 
		ng-show="loading">
	</md-progress-linear> -->
	<mat-list>
		<mat-list-item 
			*ngFor="let item of list" 
			(click)="select(item)">
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
		</mat-list-item>
	</mat-list>
</rb-list-scroll>