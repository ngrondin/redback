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
</head>
<body oncontextmenu="return false;">
  <app-root 
    type="<%=config.page%>"
    apptitle="<%=config.label%>"
    logo="<%=config.logo%>"
    version="<%=versionOrDefault%>"
    username="<%=session.getUserProfile().getUsername()%>"
    userdisplay="<%=session.getUserProfile().getAttribute('fullname')%>"
    objects="<%=utils.convertDataMapToAttributeString(config.objects)%>"
    initialview="<%=config.defaultview%>"
    menuview="<%=config.name%>"
    iconsets="<%=utils.convertDataEntityToAttributeString(config.iconsets)%>"
    uiservice="<%=uiservicepath%>"
    objectservice="<%=objectservicepath%>"
    fileservice="<%=fileservicepath%>"
    processservice="<%=processservicepath%>"
    signalservice="<%=signalservicepath%>"
    chatservice="<%=chatservicepath%>">
  </app-root>

  <script src="<%=resourcePrefix%>/runtime-es2015.js" type="module"></script>
  <script src="<%=resourcePrefix%>/runtime-es5.js" nomodule defer></script>
  <script src="<%=resourcePrefix%>/polyfills-es5.js" nomodule defer></script>
  <script src="<%=resourcePrefix%>/polyfills-es2015.js" type="module"></script>
  <script src="<%=resourcePrefix%>/styles-es2015.js" type="module"></script>
  <script src="<%=resourcePrefix%>/styles-es5.js" nomodule defer></script>
  <script src="<%=resourcePrefix%>/main-es2015.js" type="module"></script>
  <script src="<%=resourcePrefix%>/main-es5.js" nomodule defer></script></body>

</body>
</html>