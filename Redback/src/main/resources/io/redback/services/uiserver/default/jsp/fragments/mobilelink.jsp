<md-button
	class=" rb-mobile-link"
	ng-click="resolveIntent('<%=config.intent%>')"><%
if(config.icon != null) {	%>
	<md-icon><%=config.icon %></md-icon><%
} %>
	<span style="flex:1 0 auto; text-align:left;margin-left:10px;"><%=config.label %></span>
	<md-icon>keyboard_arrow_right</md-icon>	
</md-button>
