<%
//var resourcePrefix = version != null ? ('../../resource/' + version) : '../resource'; 
var versionOrDefault = version != null ? version : 'default';
%>  
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title><%=config.tabname != null ? config.tabname : config.label%></title>
  <meta name="viewport" content="width=device-width">
  <link rel="icon" type="image/x-icon" href="<%=config.favicon != null ? config.favicon : config.logo%>">
  <link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" rel="stylesheet" />
  <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=<%=global.googlekey%>&libraries=places"></script>
  <link href="https://cdn.quilljs.com/1.0.0/quill.snow.css" rel="stylesheet" />
  <script src="https://cdn.quilljs.com/1.0.0/quill.js"></script>  
  <link href="/<%=servicemap.ui%>/resource/styles.css" rel="stylesheet">
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
    uiservice="<%=servicemap.ui%>"
    objectservice="<%=servicemap.object%>"
    fileservice="<%=servicemap.file%>"
    domainservice="<%=servicemap.domain%>"
    reportservice="<%=servicemap.report%>"
    processservice="<%=servicemap.process%>"
    userpreferenceservice="<%=servicemap.userpreference%>"
    chatservice="<%=servicemap.chat%>"
    aiservice="<%=servicemap.ai%>"
    clientservice="<%=servicemap.client%>"
    usecsforapi="<%=useclientforapi%>">
  </app-root>
  
  <script src="/<%=servicemap.ui%>/resource/runtime.js" type="module"></script>
  <script src="/<%=servicemap.ui%>/resource/polyfills.js" type="module"></script>
  <script src="/<%=servicemap.ui%>/resource/main.js" type="module"></script>

</body>
</html>
