<md-switch 
	ng-model="object.data.<%=config.getString('attribute')%>" 
	aria-label="Switch 6" 
	class="rb-switch md-primary" 
	md-no-ink="" 
	ng-change="object.attributeHasChanged('<%=config.getString('attribute')%>')" 
	ng-hide="!(<%=config.getString('show')%>)">
	<%=config.getString('label')%>
</md-switch>