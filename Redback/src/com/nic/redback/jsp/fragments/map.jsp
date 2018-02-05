<div
	class="rb-map"
	ng-controller="map">
	<ui-gmap-google-map 
		center='center' 
		zoom='zoom'>
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
			events="events">
		</ui-gmap-marker>
	</ui-gmap-google-map>
</div>