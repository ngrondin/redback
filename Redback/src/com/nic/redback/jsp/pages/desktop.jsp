<html>
<head>
	<title><%=config.getString('label')%></title>
	<link rel="shortcut icon" href="../resource/favicon.ico" />
	<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.min.css"/>
	<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
	<link rel="stylesheet" href="../resource/desktop.css" type="text/css" />
	<link rel="stylesheet" href="../resource/mdPickers.css" type="text/css" />
	<!--  <link rel="stylesheet" href="https://rawgit.com/indrimuska/angular-moment-picker/master/dist/angular-moment-picker.min.css">-->	
	<script src = "../resource/lodash.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-animate.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-aria.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-messages.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.js"></script>
	<script src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyBc0KUFKS6XuCL2PRiFv9XATkMFJah6x88'></script>
	<!--<script src = "../resource/angular-simple-logger.js"></script>-->
	<script src = "http://cdn.rawgit.com/nmccready/angular-simple-logger/master/dist/angular-simple-logger.js"></script>	
	<!--<script src = "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.6/moment-with-locales.js"></script>-->
	<!--<script src = "https://rawgit.com/indrimuska/angular-moment-picker/master/dist/angular-moment-picker.min.js"></script>-->
	<script src = "../resource/angular-google-maps.js"></script>
	<script src = "../resource/moment.js"></script>
	<script src = "../resource/mdPickers.js"></script>
	<script src = "../resource/rbobject.js"></script>
	<script src = "../resource/rbcore.js"></script>
	<script src = "../resource/desktopmodule.js"></script>
</head>
<body
	ng-app="desktopmodule" 
	data-ng-init="page = '../view/<%=config.getString('defaultview')%>'">
	<md-toolbar 
		class="md-hue-2">
		<div 
			class="md-toolbar-tools">
			<md-button 
				class="md-icon-button" 
				aria-label="Settings" 
				ng-disabled="true">
				<md-icon>settings</md-icon>
			</md-button>
			<h3 flex="" md-truncate=""><%=config.getString('label')%></h3>
		</div>
	</md-toolbar>
	<div
		class="hsection"
		style="flex:1 1 auto">
		<md-sidenav 
			class="md-sidenav-left" 
			md-component-id="left" 
			md-is-locked-open="true" 
			md-whiteframe="4">
			#menu#
		</md-sidenav>		
		<ng-include  id="mainview" src="page" class="rb-include"></ng-include>
	</div>
</body>
</html>
	