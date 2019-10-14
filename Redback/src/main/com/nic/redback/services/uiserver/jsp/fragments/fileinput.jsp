<rb-file-input
	rb-attribute="<%=config.getString('attribute')%>"
	ng-show="<%=config.getString('show')%>">
	<md-button
		class="rb-button md-button md-primary md-raised" 
		style="width:80px">
		<md-icon>photo_camera</md-icon>
		<input 
			type="file" 
			accept="image/*" 
			capture="camera" />
	</md-button>
	<img
		ng-repeat="fileuid in fileuids"
		src="../../rbfs?action=get&uid={{fileuid}}"/
		rb-touch-start="touchStart"
		rb-touch-move="touchMove">
</rb-file-input>