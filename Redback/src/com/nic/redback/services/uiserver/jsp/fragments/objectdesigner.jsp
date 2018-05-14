<div
	class="rb-hsection">
	<div
		class="rb-vsection">
		<div
			class="rb-hsection"
			style="flex:0 0 auto;">
			<md-button
				class="md-fab md-mini md-primary"
				ng-click="createObject()">+
			</md-button>
			<md-input-container 
				class="md-block rb-search-container" >
				<md-icon class="md-hue-3"> search </md-icon>
				<input 
					ng-model="searchText" 
					aria-label="Search"
					size="15">
			</md-input-container>		
		</div>
		<div
			class="rb-listscroll">
			<md-list 
				flex="">
				<md-list-item 
					class="md-2-line" 
					ng-class="{ 'list-item-active': item == selectedObject }"
					ng-repeat="listitem in list | filter:searchText" 
					ng-click="selectObject(listitem)">
					<div 
						class="md-list-item-text" 
						layout="column">
						<h3><b>{{listitem.name}}</b></h3>
					</div>
				</md-list-item>
			</md-list>
		</div>
	</div>
	<div
		class="rb-vsection">
		<!-- Header -->
		<div
			class="rb-hsection"
			style="flex:0 0 auto">
				<md-input-container>
					<label>Name</label>
					<input type="text" ng-model="config.name">
				</md-input-container><br>
				<md-input-container>
					<label>Group</label>
					<input type="text" ng-model="config.group">
				</md-input-container><br>
				<md-input-container>
					<label>UID Generator</label>
					<input type="text" ng-model="config.uidgenerator">
				</md-input-container><br>
				<md-button
					class="md-primary"
					ng-click="save()">
					Save
					<md-icon ng-show="saveok == true" class="md-hue-3" >done</md-icon>
				</md-button>
		</div>
		<div
			class="rb-hsection"
			style="flex:0 0 auto">
				<md-input-container>
					<label>Collection</label>
					<input type="text" ng-model="config.collection">
				</md-input-container>
				<md-input-container>
					<label>UID DB Key</label>
					<input type="text" ng-model="config.uiddbkey" >
				</md-input-container>
				<md-input-container>
					<label>Domain DB Key</label>
					<input type="text" ng-model="config.domaindbkey" >
				</md-input-container>
		</div>
		<div
			class="rb-hsection"
			style="flex:0 0 auto">
			<md-button
				class="md-primary"
				ng-click="selectedView='attributes'">Attributes
			</md-button>
			<md-button
				class="md-primary"
				ng-click="selectedView='scripts'">Scripts
			</md-button>
		</div>
		<div
			class="rb-hsection"
			style="flex:1 1 auto"
			ng-show="selectedView == 'attributes'">
			<div
				class="rb-vsection">
				<md-button
					class="md-primary"
					ng-click="addAttribute()">+
				</md-button>
				<div
					class="rb-listscroll">
					<md-list 
						flex="">
						<md-list-item 
							class="md-2-line" 
							ng-class="{ 'list-item-active': item == selectedObject }"
							ng-repeat="listitem in config.attributes" 
							ng-click="selectAttribute(listitem)">
							<div 
								class="md-list-item-text" 
								layout="column">
								<h3>{{listitem.name}}</h3>
							</div>
						</md-list-item>
					</md-list>
				</div>
			</div>
			<div
				class="rb-vsection"
				style="overflow:auto">
				<div
					class="rb-hsection"
					style="flex: 0 0 auto;">
					<md-input-container>
						<label>Name</label>
						<input type="text" ng-model="selectedAttribute.name">
					</md-input-container>
					<md-input-container>
						<label>DB Key</label>
						<input type="text" ng-model="selectedAttribute.dbkey">
					</md-input-container>
					<md-input-container>
						<label>Editable</label>
						<input type="text" ng-model="selectedAttribute.editable">
					</md-input-container>
					<md-input-container>
						<label>Default</label>
						<input type="text" ng-model="selectedAttribute.default">
					</md-input-container>
					<md-button
						class="md-icon-button  md-raised"
						ng-click="removeAttribute()">-
					</md-button>
				</div>
				<div
					class="rb-hsection"
					style="flex: 0 0 auto;">
					<md-input-container>
						<label>Related Object</label>
						<input type="text" ng-model="selectedAttribute.relatedobject.name">
					</md-input-container>
					<md-input-container>
						<label>Related Link Attribute</label>
						<input type="text" ng-model="selectedAttribute.relatedobject.linkattribute">
					</md-input-container>
					<div
						class="rb-vsection">
						<div
							class="rb-hsection">
							<md-button
								class="md-icon-button  md-raised"
								ng-click="addRelatedFilter()">+
							</md-button>
							Related Filter
						</div>
						<div ng-repeat="filter in listfilter">
							<md-button
								class="md-icon-button  md-raised"
								ng-click="removeRelatedFilter(filter)">-
							</md-button>
							<md-input-container>
								<label>Related Column</label>
								<input type="text" size="12" ng-model="filter.name" ng-change="writeListFilter()">
							</md-input-container>
							<md-input-container>
								<label>Filter Value</label>
								<input type="text" ng-model="filter.value" ng-change="writeListFilter()">
							</md-input-container>		
						</div>
					</div>
				</div>
				<div
					class="rb-hsection"
					style="flex: 1 0 auto;">
					<md-input-container>
						<label>On Update</label>
						<textarea ui-codemirror ui-codemirror-opts="{lineNumbers: true, mode: 'application/x-javascript'}" class="rb-textarea" ng-model="selectedAttribute.scripts.onupdate" cols="80" rows="4"></textarea>
					</md-input-container>
				</div>
				<div
					class="rb-hsection"
					style="flex: 1 0 auto;">
					<md-input-container>
						<label>After Update</label>
						<textarea ui-codemirror ui-codemirror-opts="{lineNumbers: true, mode: 'application/x-javascript'}" class="rb-textarea" ng-model="selectedAttribute.scripts.afterupdate" cols="80" rows="4"></textarea>
					</md-input-container>
				</div>
			</div>
		</div>
		<div
			class="rb-vsection"
			style="overflow:auto"
			ng-show="selectedView == 'scripts'">
			<div
				class="rb-hsection"
				style="flex: 0 0 auto">
				<md-button
					class="md-icon-button  md-raised"
					ng-click="addScript()">+
				</md-button>
				Object Scripts
			</div>
			<div 
				class="rb-hsection"
				style="flex: 1 0 auto;align-items:flex-start;"
				ng-repeat="scriptitem in scripts">
				<md-button
					class="md-icon-button  md-raised"
					ng-click="removeScript(scriptitem)">-
				</md-button>
				<md-input-container>
					<label>Event</label>
					<input type="text" size="12" ng-model="scriptitem.name" ng-change="writeScripts()">
				</md-input-container>
				<md-input-container>
					<label>Source</label>
					<textarea ui-codemirror ui-codemirror-opts="{lineNumbers: true, mode: 'application/x-javascript'}" class="rb-textarea" ng-model="scriptitem.value" cols="80" rows="4"  ng-change="writeScripts()"></textarea>
				</md-input-container>
			</div>
		</div>
		
	</div>
</div>	
