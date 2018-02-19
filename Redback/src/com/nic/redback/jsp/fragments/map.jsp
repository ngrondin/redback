<div
	id="testid"
	class="rb-map"
	ng-controller="map"
	oncontextmenu="return false">
 	<ui-gmap-google-map 
		center='center' 
		zoom='zoom'
		events="{click:mapClicked, rightclick:showContextMenu, dragstart:mapDragStarted}"
		control="mapcontrol"
		options="{}">
		<ui-gmap-marker 
			ng-repeat="object in list" 
			coords="object.data.geometry.coords" 
			idkey="object.uid"
			options="{draggable:<%
				if(canWrite) {		
					%>object.validation.geometry.editable<%
				} else {
					%>false<%
				}			
				%>}"
			events="{dragend:markerHasMoved, click:markerSelected}">
		</ui-gmap-marker>
	</ui-gmap-google-map>
</div>