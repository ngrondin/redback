<%
var resourcePrefix = version != null ? ('../../resource/' + version) : '../resource'; 
var versionOrDefault = version != null ? version : 'default';
%>  
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title><%=config.getString('label')%></title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta http-equiv="refresh" content="0; url = '<%=config.getString('mobileapp')%>://login?token=<%=session.getToken()%>&expiry=<%=session.expiry.toString()%>&username=<%=session.getUserProfile().getUsername()%>&objectservice=<%=objectservicepath%>&processservice=<%=processservicepath%>&fileservice=<%=fileservicepath%>'" />
  <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
  <meta http-equiv="Pragma" content="no-cache" />
  <meta http-equiv="Expires" content="0" />  
  <link rel="icon" type="image/x-icon" href="<%=config.getString('logo')%>">
  <link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <script>
    window.onload = function() {
        window.location = '<%=config.getString('mobileapp')%>://login?token=<%=session.getToken()%>&expiry=<%=session.expiry.toString()%>&username=<%=session.getUserProfile().getUsername()%>&objectservice=<%=objectservicepath%>&processservice=<%=processservicepath%>&fileservice=<%=fileservicepath%>';
    }
  </script>  
</head>
<body oncontextmenu="return false;">
	<a href="<%=config.getString('mobileapp')%>://login?token=<%=session.getToken()%>&expiry=<%=session.expiry.toString()%>&username=<%=session.getUserProfile().getUsername()%>&objectservice=<%=objectservicepath%>&processservice=<%=processservicepath%>&fileservice=<%=fileservicepath%>">Manual Redirect</a>
</body>
</html>