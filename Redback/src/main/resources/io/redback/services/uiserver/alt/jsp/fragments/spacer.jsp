<div
	style="<%
if(config.containsKey('size') && config.getString('size').length > 0) {
	%>width:<%=config.getString('size')%>px;height:<%=config.getString('size')%>px;<%
} else {
	%>flex-grow:1<%
}
	%>">
</div>