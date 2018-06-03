<rb-match-scheduler
<% if(config.getObject('config') != null) { %>
	rb-config="<%=com.nic.redback.utils.StringUtils.convertJSONToAttributeString(com.nic.redback.services.UIServer.convertFilter(config.getObject('config')))%>"<% 
} %>>
	<div
		class="rb-sched-toolbar">
		<div
			class="rb-sched-toolbar-item ">
			<span>Span</span>
		</div>
		<md-select 
			ng-model="spanDays" 
			class="rb-sched-toolbar-item"
			ng-change="spanChanged()"
			aria-label="Span">
			<md-option ng-value="1">1 Day</md-option>
			<md-option ng-value="3">3 Days</md-option>
			<md-option ng-value="7">1 Week</md-option>
		</md-select>
		<div style="width:50px"></div>
		<div
			class="rb-sched-toolbar-item ">
			<span>Zoom</span>
		</div>
		<md-select 
			ng-model="scale" 
			class="rb-sched-toolbar-item"
			ng-change="zoomChanged()"
			aria-label="Zoom">
			<md-option ng-value="12000">6 Hours</md-option>
			<md-option ng-value="50000">1 Day</md-option>
			<md-option ng-value="150000">3 Days</md-option>
		</md-select>			
	</div>
	<div
		class="rb-sched-demand">
		<div
			class="rb-sched-list">
			<div
				class="rb-sched-demand-header">
			</div>
			<div
				class="rb-sched-demand-header"
				ng-repeat="group in data.demandgroups">{{group.label}}</div>
		</div>
		<div
			class="rb-sched-demand-container">
			<div
				class="rb-sched-demand-canvas"
				style="width:{{(width)}}px"
				rb-drop-target="">
				<div
					class="rb-sched-demand-line">
					<div
						class="rb-sched-marker"
						ng-repeat="marker in data.markers"
						style="left:{{(marker.position)}}px">{{marker.label}}</div>
				</div>
				<div
					class="rb-sched-demand-line"
					ng-repeat="group in data.demandgroups">
					<div
						class="rb-sched-demand-block"
						ng-repeat="demand in group.unmatched"
						style="width:{{(demand.finish - demand.start)}}px;left:{{(demand.start)}}px"
						rb-draggable="demand">
					</div>
					<div
						class="rb-sched-demand-block-matched"
						ng-repeat="demand in group.matched"
						style="width:{{(demand.finish - demand.start)}}px;left:{{(demand.start)}}px">
					</div>
					<div
						class="rb-sched-marker"
						ng-repeat="marker in data.markers"
						style="left:{{(marker.position)}}px">
					</div>
				</div>
			</div>
		</div>
	</div>
	<div
		class="rb-sched-offer">
		<div
			class="rb-sched-list">
			<div
				class="rb-sched-offer-header"
				ng-repeat="group in data.offergroups">{{group.label}}</div>
		</div>
		<div
			class="rb-sched-offer-container"
			scroll>
			<div
				class="rb-sched-offer-canvas"
				style="width:{{(width)}}px">
				<div
					class="rb-sched-offer-line"
					ng-repeat="group in data.offergroups">
					<div
						class="rb-sched-offer-block"
						ng-class="{'rb-sched-offer-block-active':offer.canoffer, 'rb-sched-offer-block-inactive':!offer.canoffer}"
						ng-repeat="offer in group.offers"
						style="width:{{(offer.finish - offer.start)}}px;left:{{(offer.start)}}px"
						rb-drop-target="offer">
					</div>
					<div
						class="rb-sched-offer-block-matcheddemand"
						ng-repeat="demand in group.matcheddemands"
						style="width:{{(demand.finish - demand.start)}}px;left:{{(demand.start)}}px"
						rb-draggable="demand">
					</div>
					<div
						class="rb-sched-marker"
						ng-repeat="marker in data.markers"
						style="left:{{(marker.position)}}px">
					</div>

				</div>
			</div>
		</div>
	</div>
</rb-match-scheduler>
