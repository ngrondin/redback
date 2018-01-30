<ui-gmap-google-map 
	center='map.center' 
	zoom='map.zoom'>
	<ui-gmap-marker 
		ng-repeat="listitem in list" 
		coords="listitem.data.geometry.coords" 
		idkey="listitem.uid"
		click="selectObject(listitem)">
	</ui-gmap-marker>
</ui-gmap-google-map>