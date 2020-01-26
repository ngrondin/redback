<div
	style="<%
if(config.containsKey('grow')) {
	%>flex-grow:1<%
} else {
	%>width:<%=config.getString('size')%>px;height:<%=config.getString('size')%>px;<%
}
	%>">
</div>