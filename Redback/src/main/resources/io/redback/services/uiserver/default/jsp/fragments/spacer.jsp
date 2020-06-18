<div
	style="<%
if(config.size!= null && config.size.length > 0) {
	%>width:<%=config.size%>px;height:<%=config.size%>px;<%
} else {
	%>flex-grow:1<%
}
	%>">
</div>