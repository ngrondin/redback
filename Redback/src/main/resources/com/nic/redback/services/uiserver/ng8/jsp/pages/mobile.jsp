<html>
<head>
	<title><%=config.getString('label')%></title>
	<link rel="shortcut icon" href="../resource/favicon.ico" />
	<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.min.css"/>
	<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
	<link rel="stylesheet" href="../resource/mdPickers.css" type="text/css" />
	<link rel="stylesheet" href="../resource/mobile.css" type="text/css" />
	<script src = "../resource/lodash.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-animate.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-aria.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-messages.js"></script>
	<script src = "https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.js"></script>
	<script src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyBc0KUFKS6XuCL2PRiFv9XATkMFJah6x88'></script>
	<script src = "http://cdn.rawgit.com/nmccready/angular-simple-logger/master/dist/angular-simple-logger.js"></script>	
	<script src = "../resource/angular-google-maps.js"></script>
	<script src = "../resource/moment.js"></script>
	<script src = "../resource/mdPickers.js"></script>
	<script src = "../resource/rbobject.js"></script>
	<script src = "../resource/rbcore.js"></script>
	<script src = "../resource/mobilemodule.js"></script>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name=”apple-mobile-web-app-capable” content=”yes”>
</head>
<body
	ng-app="mobilemodule" 
	ng-controller="mobileroot"
	data-ng-init="page = '../view/<%=config.getString('defaultview')%>'">
	<div  id="mainview" ng-include="page" class="rb-include">
	</div>
</body>
</html>
	