<%
//var resourcePrefix = version != null ? ('../../resource/' + version) : '../resource'; 
var versionOrDefault = version != null ? version : 'default';
%>  
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title><%=config.label%></title>
  <meta name="viewport" content="width=device-width">
  <link rel="icon" type="image/x-icon" href="<%=config.logo%>">
  <link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=<%=global.googlekey%>&libraries=places"></script>
  <link href="https://cdn.quilljs.com/1.0.0/quill.snow.css" rel="stylesheet" />
  <script src="https://cdn.quilljs.com/1.0.0/quill.js"></script>  
  <link href="/<%=deployment.uiservicepath%>/resource/styles.css" rel="stylesheet">
  <script>
    var googlekey = "<%=global.googlekey%>";
  </script>
</head>
<body oncontextmenu="return false;">
  <app-root 
  	name="<%=config.name%>"
    version="<%=versionOrDefault%>"
    username="<%=session.getUserProfile().getUsername()%>"
    userdisplay="<%=session.getUserProfile().getAttribute('fullname')%>"
    uiservice="<%=deployment.uiservicepath%>"
    objectservice="<%=deployment.objectservicepath%>"
    fileservice="<%=deployment.fileservicepath%>"
    domainservice="<%=deployment.domainservicepath%>"
    reportservice="<%=deployment.reportservicepath%>"
    processservice="<%=deployment.processservicepath%>"
    userpreferenceservice="<%=deployment.userpreferenceservicepath%>"
    chatservice="<%=deployment.chatservicepath%>"
    clientservice="<%=deployment.clientservicepath%>"
    usecsforapi="<%=deployment.useclientforapi%>">
  </app-root>
  
  <script src="/<%=deployment.uiservicepath%>/resource/runtime.js" type="module"></script>
  <script src="/<%=deployment.uiservicepath%>/resource/polyfills.js" type="module"></script>
  <script src="/<%=deployment.uiservicepath%>/resource/main.js" type="module"></script>

</body>
</html>
