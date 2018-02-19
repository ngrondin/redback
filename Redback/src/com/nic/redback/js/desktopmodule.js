	var module = angular.module("desktopmodule", ['ngMaterial', 'uiGmapgoogle-maps']);	

	
	/***********************************/
	/** Form Controller			 	  **/
	/***********************************/

	
	module.controller('form', function formCtl($scope,$attrs,$http) {
		$scope.objectName = $attrs.rbObject;
		$scope.object = null;
		$scope.dynamicSearchText = "";
		
		
		$scope.setObject = function(object) {
			$scope.object = object;
			if(object != null) {
				for (var key in object.related) {
					if(!object.validation[key].hasOwnProperty('listofvalues'))
						object.validation[key].listofvalues = [object.related[key]];
				}
			}
		}

		
		$scope.loadRelatedObjectList = function(attributeName, searchText)
		{
			if($scope.object != null) {
				var req = {action:"list", object:$scope.objectName, uid:$scope.object.uid, filter:{}, attribute:attributeName};
				if(searchText != null)
					req.filter.$multi = '*' + searchText + '*';
				return $http.post("../../rbos", req)
					.then(function(response) {
						var responseList = processResponseJSON(response.data);
						if(responseList != null) {
							$scope.object.validation[attributeName].listofvalues = responseList;
							return responseList;
						}
					});
			}
		};
				
		
		$scope.save = function(){
			if($scope.object != null) {
				if($scope.object.isUpdated()) {
					$http.post("../../rbos", $scope.object.getUpdateRequestMessage())
					.success(function(response) {
						var responseObject = processResponseJSON(response);
						if(responseObject != null) 
							$scope.setObject(responseObject);
					})
					.error(function(error, status) {
						alert('save error');
					});
				}
				$scope.$emit('saveRelatedEmit', $scope.object);
			}
		};		


		$scope.create = function(){
			$scope.$emit('createObjectEmit', $scope.objectName);
		};		

		$scope.objectfunction = function(functionName){
			var req = {action:"execute", object:$scope.objectName, uid:$scope.object.uid, "function":functionName, options:{addrelated:true, addvalidation:true}};
			$http.post("../../rbos", req)
			.success(function(response) {
				var responseObject = processResponseJSON(response);
				if(responseObject != null) {
					$scope.setObject(responseObject);
					$scope.$emit('refreshRelatedEmit', $scope.object);
				}
			})
			.error(function(error, status) {
				alert('execute error');
			});
		};

		
		$scope.$on('objectSelected', function($event, object){
			if(object != null  &&  object.objectname == $scope.objectName)
				$scope.setObject(object);
		});


		$scope.$on('nullObjectSelected', function($event, objectName){
			if(objectName == $scope.objectName)
				$scope.setObject(null);
		});

	 });
	 

	/***********************************/
	/** List Controller				  **/
	/***********************************/

	 
	 
	module.controller('list', function listCtl($scope,$attrs,$http) {
		$scope.objectName = $attrs.rbObject;
		$scope.list = null;
		$scope.selectedObject = null;
		$scope.relatedConfig = null;
		$scope.searchText = "";
		$scope.relationshipFilter = {};
		$scope.searchFilter = {};
		$scope.baseFilter = {};

		if($attrs.rbRelated != null  &&  $attrs.rbRelated.length > 0) {
			$scope.relatedConfig = JSON.parse($attrs.rbRelated.replace(/'/g, '"'));
			$scope.relationshipFilter = {uid:-1};
		}
			
		if($attrs.rbInitialFilter != null  &&  $attrs.rbInitialFilter.length > 0)
			$scope.baseFilter = JSON.parse($attrs.rbInitialFilter.replace(/'/g, '"'));


		$scope.search = function(searchText) {
			if(searchText == null || searchText == 0)
				$scope.searchFilter = {};
			else
				$scope.searchFilter.$multi = '*' + $scope.searchText + '*';
			$scope.load();
		}
		
		$scope.getBaseAndRelationshipFilter = function() {
			var filter = {};
			for (var key in $scope.baseFilter)
				filter[key] = $scope.baseFilter[key];
			for (var key in $scope.relationshipFilter)
				filter[key] = $scope.relationshipFilter[key];
			return filter;
		}
		
		$scope.getFullFilter = function() {
			var filter = $scope.getBaseAndRelationshipFilter();
			for (var key in $scope.searchFilter)
				filter[key] = $scope.searchFilter[key];
			return filter;
		}

		$scope.load = function() {
			var req = {action:"list", object:$scope.objectName, filter:$scope.getFullFilter(), options:{addrelated:true, addvalidation:true}};
			$http.post("../../rbos", req)
			.success(function(response) {
				var responseList = processResponseJSON(response);
				if(responseList != null) 
					$scope.list = responseList;
			})
			.error(function(error, status) {
				alert('load error');
			});
		}
		

		$scope.create = function(){
			var req = {action:"create", object:$scope.objectName, data:$scope.getBaseAndRelationshipFilter(), options:{addrelated:true, addvalidation:true}};
			$http.post("../../rbos", req)
			.success(function(response) {
				var responseObject = processResponseJSON(response);
				if(responseObject != null) {
					$scope.list.push(responseObject);
					$scope.selectObject(responseObject);
					$scope.$emit('refreshRelatedEmit', responseObject);
				}
			})
			.error(function(error, status) {
				alert('create error');
			});
		};		
		
		
		$scope.save = function() {
			for(var i = 0; i < $scope.list.length; i++) {
				if($scope.list[i].isUpdated()) {
					$http.post("../../rbos", $scope.list[i].getUpdateRequestMessage())
					.success(function(response) {
						processResponseJSONObject(response);
					})
					.error(function(error, status) {
						alert('save error');
					});						
				}
			}		
		}		
		
		$scope.selectObject = function(object) {
			if(object != null && $scope.list.includes(object)) {
				$scope.selectedObject = object;
				$scope.$emit('objectSelectedEmit', object);
			}
		}

		$scope.clearSelectedObject = function() {
			$scope.selectedObject = null;
			$scope.$emit('nullObjectSelectedEmit', $scope.objectName);
		}		
		
		$scope.$on('objectSelected', function($event, object){
			if($scope.relatedConfig != null && object.objectname == $scope.relatedConfig.objectname) {
				$scope.clearSelectedObject();
				$scope.relationshipFilter = getFilterFromRelationship(object, $scope.relatedConfig.relationship)
				$scope.load();
			}
		});

		$scope.$on('createObject', function($event, name){
			if(name == $scope.objectName) {
				$scope.create();
			}
		});

		$scope.$on('saveRelated', function($event, object){
			if($scope.relatedConfig != null && object.objectname == $scope.relatedConfig.objectname) {
				$scope.save();
			}
		});

		$scope.$on('refreshRelated', function($event, object){
			if($scope.relatedConfig != null && object.objectname == $scope.relatedConfig.objectname) {
				$scope.load();
			}
		});
		
		$scope.load();
	 });	 

	
	/***********************************/
	/** Layout Controller			 **/
	/***********************************/

	 
	module.controller('layout', function layoutCtl($scope,$attrs,$http) {

		$scope.$on('objectSelectedEmit', function($event, object){
			if(!$event.defaultPrevented) {
				$scope.$broadcast('objectSelected', object);
				$event.defaultPrevented = true;
			}
		});
		
		$scope.$on('nullObjectSelectedEmit', function($event, name){
			if(!$event.defaultPrevented) {
				$scope.$broadcast('nullObjectSelected', name);
				$event.defaultPrevented = true;
			}
		});

		$scope.$on('createObjectEmit', function($event, name){
			if(!$event.defaultPrevented) {
				$scope.$broadcast('createObject', name);
				$event.defaultPrevented = true;
			}
		});

		$scope.$on('saveRelatedEmit', function($event, object){
			if(!$event.defaultPrevented) {
				$scope.$broadcast('saveRelated', object);
				$event.defaultPrevented = true;
			}
		});

		$scope.$on('refreshRelatedEmit', function($event, object){
			if(!$event.defaultPrevented) {
				$scope.$broadcast('refreshRelated', object);
				$event.defaultPrevented = true;
			}
		});

		
	 });	 
	 
	/***********************************/
	/** Layout Controller			 **/
	/***********************************/

	 
	module.controller('tab', function tabCtl($scope,$attrs,$http) {
		$scope.tabs = [];
		$scope.selected_tab = null;
		
		$scope.selectTab = function(tab) {
			$scope.selected_tab = tab;
		}

	});
	
	
	/***********************************/
	/** Map Controller		    	  **/
	/***********************************/

	 
	module.controller('map', function mapCtl($scope,$attrs,$http,$compile) {

		$scope.mapcontrol = {};
		$scope.center = { latitude: -34, longitude: 150 }; 
		$scope.zoom = 8;
		$scope.markeroptions = {
			draggable: true,
			label: 'Pout'
		};
	
		$scope.createObjectAtPosition = function(position) {
			alert('allo');
		}
		
		$scope.setSelectedObjectPosition = function(position) {
			$scope.$parent.selectedObject.data.geometry = {
				type: 'point',
				coords: position
			}
			$scope.$parent.selectedObject.attributeHasChanged('geometry');
			$scope.hideContextMenu();
		}

		$scope.markerHasMoved = function(marker, eventName, model, args) {
			$scope.setSelectedObjectPosition({latitude:marker.position.lat(), longitude:marker.position.lng()});
		}
		
		$scope.markerSelected = function(marker, eventName, model, args) {
			model.$parent.$parent.selectObject(model.$parent.object);
		}
		
		$scope.mapClicked = function(map, eventName, args) {
			$scope.hideContextMenu();
		}

		$scope.mapDragStarted = function(map, eventName, args) {
			$scope.hideContextMenu();
		}
		
		$scope.showContextMenu = function(map, eventName, args) {
			var clickLatLng = args[0].latLng;
			$scope.hideContextMenu();
			
			var scale = Math.pow(2, map.getZoom());
			var nw = new google.maps.LatLng(map.getBounds().getNorthEast().lat(), map.getBounds().getSouthWest().lng());
			var worldCoordinateNW = map.getProjection().fromLatLngToPoint(nw);
			var worldCoordinate = map.getProjection().fromLatLngToPoint(clickLatLng);
			var clickedPosition = new google.maps.Point(Math.floor((worldCoordinate.x - worldCoordinateNW.x) * scale), Math.floor((worldCoordinate.y - worldCoordinateNW.y) * scale));	

			var html = '<div id="contextmenu" class="contextmenu"><md-list>';
			if($scope.$parent.selectedObject != null)
				html = html + '<md-list-item ng-click="setSelectedObjectPosition({latitude:' + clickLatLng.lat() + ', longitude:' + clickLatLng.lng() + '})"><div style="white-space:nowrap">Set location here</div></md-list-item>';
			html = html + '<md-list-item ng-click="createObjectAtPosition({latitude:' + clickLatLng.lat() + ', longitude:' + clickLatLng.lng() + '})"><div style="white-space:nowrap">Create new location here</div></md-list-item>';
			html = html + '</md-list></div>';
			var contextmenuDivFactory = $compile(html);
			var contextmenuDiv = contextmenuDivFactory($scope);
			angular.element(map.getDiv()).append(contextmenuDiv);

			if((map.getDiv().offsetWidth - clickedPosition.x) < contextmenuDiv[0].offsetWidth)
				clickedPosition.x = clickedPosition.x - contextmenuDiv[0].offsetWidth;
			if((map.getDiv().offsetHeight - clickedPosition.y) < contextmenuDiv[0].offsetHeight)
				clickedPosition.y = clickedPosition.y - contextmenuDiv[0].offsetHeight;

			contextmenuDiv[0].style.left = (clickedPosition.x + 'px');
			contextmenuDiv[0].style.top = (clickedPosition.y + 'px');
			contextmenuDiv[0].style.visibility = 'visible';
		}
		
		$scope.hideContextMenu = function() {
			var existingContextMenu = document.getElementById('contextmenu');
			if(existingContextMenu != null)
				existingContextMenu.remove();		
		}

	 });	 	 