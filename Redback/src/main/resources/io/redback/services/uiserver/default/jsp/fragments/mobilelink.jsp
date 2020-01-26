<md-button
	class=" rb-mobile-link"
	ng-click="resolveIntent('<%=config.getString('intent')%>')"><%
if(config.get("icon") != null) {	%>
	<md-icon><%=config.getString("icon") %></md-icon><%
} %>
	<span style="flex:1 0 auto; text-align:left;margin-left:10px;"><%=config.getString("label") %></span>
	<md-icon>keyboard_arrow_right</md-icon>	
</md-button>