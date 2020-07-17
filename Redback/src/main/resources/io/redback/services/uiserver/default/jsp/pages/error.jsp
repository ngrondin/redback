<%
var resourcePrefix = version != null ? ('../../resource/' + version) : '../resource'; 
var versionOrDefault = version != null ? version : 'default';
%>  
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title><%=config.label%></title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="<%=config.logo%>">
  <link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC78ZWKE3Shecj9QgDf84TW9kk7r5NrVPE&libraries=places"></script>
</head>
<body>
	<div class="loginbox" ng-app="loginmodule" ng-controller="form">
		<h2>Redback Error</h2>
		#errormessage#
	</div>
</body>
</html>