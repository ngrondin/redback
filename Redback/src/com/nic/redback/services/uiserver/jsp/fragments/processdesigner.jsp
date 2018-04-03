<div
	class="rb-hsection">
	<div
		class="rb-vsection">
		<md-list 
			flex="">
			<md-list-item 
				class="md-2-line" 
				ng-class="{ 'list-item-active': item == selectedObject }"
				ng-repeat="listitem in list" 
				ng-click="selectProcess(listitem)">
				<div 
					class="md-list-item-text" 
					layout="column">
					<h3><b>{{listitem.name}}</b></h3>
					<h4>Version: {{listitem.version}}</h4>
				</div>
			</md-list-item>
		</md-list>
	</div>
	<div
		class="rb-vsection">
		<!-- Workflow Header -->
		<div
			class="rb-hsection">
				<md-input-container>
					<label>Name</label>
					<input type="text" ng-model="config.name">
				</md-input-container><br>
				<md-input-container>
					<label>Version</label>
					<input type="text" ng-model="config.version" size="3">
				</md-input-container><br>
				<md-input-container>
					<label>Start Node</label>
					<md-select ng-model="config.startnode">
						<md-option ng-repeat="node in config.nodes" ng-value="node.id">{{node.name}}</md-option>
					</md-select>
				</md-input-container><br>
				<md-button
					class="md-primary"
					ng-click="save()">Save
				</md-button>
		</div>
		<!-- Layout Canvas -->		
		<div
			class="rb-wfconfig-canvas-container"
			style="width:100%;height:400px">
			<div 
				class="rb-wfconfig-canvas"
				ng-controller="processcanvas" 
				ng-mouseup="mouseup($event)"
				ng-mousemove="mousemove($event)">
				<canvas id="canvas"></canvas>
				<div
					class="rb-wfconfig-node"
					style="top:{{node.position.y}}px;left:{{node.position.x}}px;"
					ng-repeat="node in config.nodes"
					ng-controller="processnode"
					ng-mousedown="mousedown($event)">
					<div
						class="rb-wfconfig-node-name">
						{{node.name}}
					</div>
					<div
						class="rb-wfconfig-node-type">
						{{node.type}}
					</div>
				</div>
			</div>
		</div>
		<div style="display:flex;flex-direction:column">
			<!-- Node Buttons -->
			<div style="display:flex;flex-direction:row">
				<md-button
					class="md-icon-button  md-raised"
					ng-click="addNode()">+
				</md-button>
				<md-button
					class="md-icon-button  md-raised"
					ng-click="removeNode()">-
				</md-button>
			</div>
			<div style="display:flex;flex-direction:row">
				<div style="">
				</div>
				<!-- Node Header -->
				<div style="display:flex;flex-direction:column">
					<md-input-container>
						<label>Id</label>
						<input type="text" ng-model="selectedNode.id">
					</md-input-container>
					<md-input-container>
						<label>Name</label>
						<input type="text" ng-model="selectedNode.name">
					</md-input-container>
					<md-input-container>
						<label>Type</label>
						<md-select ng-model="selectedNode.type">
							<md-option ng-repeat="type in nodeTypes" ng-value="type.id">{{type.name}}</md-option>
						</md-select>
					</md-input-container><br>
				</div>
				<!-- Script Node -->
				<div style="display:flex;flex-direction:column;overflow:auto" ng-show="selectedNode.type == 'script'">
					<label>Source</label>
					<textarea ui-codemirror ui-codemirror-opts="{lineNumbers: true, mode: 'application/x-javascript'}" rows="8" cols="80" ng-model="selectedNode.source" style="width:800px;"></textarea>
					<md-input-container>
						<label>Next Node</label>
						<md-select ng-model="selectedNode.nextnode">
							<md-option ng-value=""></md-option>
							<md-option ng-repeat="node in config.nodes" ng-value="node.id">{{node.name}}</md-option>
						</md-select>
					</md-input-container><br>					
				</div>		
				<!-- Action Node -->
				<div style="display:flex;flex-direction:column" ng-show="selectedNode.type == 'action'">
					<md-input-container>
						<label>Interaction</label>
						<input type="text" ng-model="selectedNode.interaction">
					</md-input-container>
					<md-input-container>
						<label>Action</label>
						<input type="text" ng-model="selectedNode.action">
					</md-input-container>
					<md-input-container>
						<label>Next Node</label>
						<md-select ng-model="selectedNode.nextnode">
							<md-option ng-value=""></md-option>
							<md-option ng-repeat="node in config.nodes" ng-value="node.id">{{node.name}}</md-option>
						</md-select>
					</md-input-container>					
				</div>	
				<!-- Interaction Node -->
				<div style="display:flex;flex-direction:column" ng-show="selectedNode.type == 'interaction'">
					<md-input-container>
						<label>Iteraction code</label>
						<input type="text" ng-model="selectedNode.notification.code">
					</md-input-container>
					<md-input-container>
						<label>Message</label>
						<input type="text" ng-model="selectedNode.notification.message">
					</md-input-container>
					<md-input-container>
						<label>Method</label>
						<md-select ng-model="selectedNode.notification.method">
							<md-option ng-value=""></md-option>
							<md-option ng-repeat="method in notificationMethods" ng-value="method.id">{{method.name}}</md-option>
						</md-select>
					</md-input-container>					
				</div>	
				<!-- Interaction Node -->
				<div style="display:flex;flex-direction:column" ng-show="selectedNode.type == 'interaction'">
					<div style="display:flex;flex-direction:row;align-items:center;">
						<md-button
							class="md-icon-button  md-raised"
							ng-click="addAssignee()">+
						</md-button>
						Assignments
					</div>
					<div ng-repeat="assignee in selectedNode.assignees">
						<md-button
							class="md-icon-button  md-raised"
							ng-click="removeAssignee(assignee)">-
						</md-button>
						<md-input-container>
							<label>Type</label>
							<md-select ng-model="assignee.type">
								<md-option ng-value=""></md-option>
								<md-option ng-repeat="type in assigneeTypes" ng-value="type.id">{{type.name}}</md-option>
							</md-select>
						</md-input-container>			
						<md-input-container>
							<label>Assignee</label>
							<input type="text" size="12" ng-model="assignee.id">
						</md-input-container>
					</div>
				</div>	
				<div style="display:flex;flex-direction:column" ng-show="selectedNode.type == 'interaction'">
					<div style="display:flex;flex-direction:row;align-items:center;">
						<md-button
							class="md-icon-button  md-raised"
							ng-click="addAction()">+
						</md-button>
						Actions
					</div>
					<div ng-repeat="action in selectedNode.actions">
						<md-button
							class="md-icon-button  md-raised"
							ng-click="removeAction(action)">-
						</md-button>
						<md-input-container>
							<label>Action</label>
							<input type="text" size="12" ng-model="action.action">
						</md-input-container>
						<md-input-container>
							<label>Description</label>
							<input type="text" ng-model="action.description">
						</md-input-container>
						<md-input-container>
							<label>Next Node</label>
							<md-select ng-model="action.nextnode">
								<md-option ng-value=""></md-option>
								<md-option ng-repeat="node in config.nodes" ng-value="node.id">{{node.name}}</md-option>
							</md-select>
						</md-input-container>			
					</div>
				</div>	
				<!-- Redback Object Update Node -->
				<div style="display:flex;flex-direction:column" ng-show="selectedNode.type == 'rbobjectupdate'">
					<md-input-container>
						<label>Object Name</label>
						<input type="text" ng-model="selectedNode.object">
					</md-input-container>
					<md-input-container>
						<label>Object UID</label>
						<input type="text" ng-model="selectedNode.uid">
					</md-input-container>
					<md-input-container>
						<label>Data</label>
						<textarea rows="4" cols="80" ng-model="selectedNode.data"></textarea>
					</md-input-container>
					<md-input-container>
						<label>Next Node</label>
						<md-select ng-model="selectedNode.nextnode">
							<md-option ng-value=""></md-option>
							<md-option ng-repeat="node in config.nodes" ng-value="node.id">{{node.name}}</md-option>
						</md-select>
					</md-input-container>					
				</div>	
				<!-- Redback Object Execute Node -->
				<div style="display:flex;flex-direction:column" ng-show="selectedNode.type == 'rbobjectexecute'">
					<md-input-container>
						<label>Object Name</label>
						<input type="text" ng-model="selectedNode.object">
					</md-input-container>
					<md-input-container>
						<label>Object UID</label>
						<input type="text" ng-model="selectedNode.uid">
					</md-input-container>
					<md-input-container>
						<label>Function</label>
						<input type="text" ng-model="selectedNode.function">
					</md-input-container>
					<md-input-container>
						<label>Data</label>
						<textarea rows="4" cols="80" ng-model="selectedNode.data"></textarea>
					</md-input-container>
					<md-input-container>
						<label>Next Node</label>
						<md-select ng-model="selectedNode.nextnode">
							<md-option ng-value=""></md-option>
							<md-option ng-repeat="node in config.nodes" ng-value="node.id">{{node.name}}</md-option>
						</md-select>
					</md-input-container>					
				</div>	
			</div>
		</div>
	</div>
</div>	
