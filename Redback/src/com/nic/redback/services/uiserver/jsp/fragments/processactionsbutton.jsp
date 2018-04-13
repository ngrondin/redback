<md-menu
	ng-controller="workflowactions">
	<md-button 
		aria-label="Workflow actions" 
		class="md-fab md-mini md-primary rb-form-button" 
		ng-click="openMenu($mdOpenMenu, $event)">
		<md-icon md-svg-icon="wms:network"></md-icon>
	</md-button>
	<md-menu-content>
        		<md-menu-item>
        			<span>{{notification.message}}</span>        			
        		</md-menu-item>
        		<md-divider></md-divider>
        		<md-menu-item
        			ng-repeat="item in notification.actions">
			<md-button
				ng-click="processAction(item.action)">
				{{item.description}}
			</md-button>
		</md-menu-item>
	</md-menu-content>	
</md-menu>