	var module = angular.module("desktopmodule", ['ngMaterial', 'uiGmapgoogle-maps', 'mdPickers']);	

	/***********************************/
	/** Input Directive			 	  **/
	/***********************************/
	
	module.directive('rbInput', function($compile) {
		return {
			restrict:'A',
			scope:true,
			controller: function($scope, $attrs, $http, $compile) {
				$scope.element = null;
				$scope.attributeName = $attrs.rbAttribute;
				$scope.inputValue = '';
				
				$scope.keyProcessor = function(event) {
					handled = false;
					if(event.keyCode == 13  ||  event.keyCode == 9) {
					} 
				};
				$scope.change = function(event) {
					$scope.object.data[$scope.attributeName] = $scope.inputValue;			
					$scope.object.attributeHasChanged($scope.attributeName, $http);
				};
				$scope.$watch('object.data.' + $scope.attributeName, function(newValue, oldValue) {
					$scope.inputValue = newValue;
				});
			},
			link: function($scope, $element, $attrs) {
				$scope.element = $element;
				$element.bind('keydown', $scope.keyProcessor);
				$element.bind('change', $scope.change);
			}
		};
	});	
	
	/***********************************/
	/** Related Input Direcive	 	  **/
	/***********************************/
	
	module.directive('rbRelatedInput', ['$compile', function($compile) {
		return {
			restrict:'A',
			scope:true,
			controller: function($scope, $attrs, $http, $compile, $mdPanel) {
				$scope.mdPanel = $mdPanel;
				$scope.mdPanelRef = null;
				$scope.element = null;
				$scope.inputValue = '';
				$scope.attributeName = $attrs.rbAttribute;
				$scope.displayAttributeName = $attrs.rbDisplayAttribute;
				$scope.parentAttributeName = $attrs.rbParentAttribute;
				$scope.childAttributeName = $attrs.rbChildAttribute;
				$scope.listConfig = {
					parents:[], 
					list:[], 
					hierachical:($scope.parentAttributeName != null  &&  $scope.childAttributeName != null),
					selected:null,
					highlightedIndex:-1,
					loading:false,
					open:false,
					inputScope:$scope,
					loadList:function(){
						this.inputScope.loadList();
					}
				};
				$scope.openDropDown = function(event) {
					if($scope.listConfig.open == false)
					{
						var config = {
							attachTo: angular.element(document.body),
							template: '<div>' +
										'<md-list>' +
											'<md-list-item ng-repeat="listitem in listConfig.parents" ng-click="selectItem(listitem)" class="list-item-parent">' +
												'<p style="white-space: nowrap">{{listitem.data.' + $scope.displayAttributeName + '}}</p>' +
												'<md-button ng-click="colapseItem(listitem)" class="md-icon-button md-primary"><md-icon ng-show="listConfig.hierachical">expand_less</md-icon></md-button>' +
											'</md-list-item>' +
											'<md-progress-linear md-mode="indeterminate" ng-show="listConfig.loading"></md-progress-linear>' +
											'<md-list-item ng-repeat="listitem in listConfig.list" ng-click="selectItem(listitem)" ng-class="{\'list-item-highlighted\': listConfig.highlightedIndex===$index}">' +
												'<p style="white-space: nowrap">{{listitem.data.' + $scope.displayAttributeName + '}}</p>' +
												'<md-button ng-click="expandItem(listitem)" class="md-icon-button md-primary"><md-icon ng-show="listConfig.hierachical">expand_more</md-icon></md-button>' +
											'</md-list-item>' +
										'</md-list>' +
										'</div>',
							controller: function($scope, mdPanelRef, listConfig) { 
								$scope.listConfig = listConfig;
								$scope.mdPanelRef = mdPanelRef;
								$scope.selectItem = function(listitem) {
									$scope.listConfig.selected = listitem;
									$scope.mdPanelRef.close();
								};
								$scope.expandItem = function(listitem) {
									$scope.listConfig.parents.push(listitem);
									$scope.listConfig.list = [];
									$scope.listConfig.loadList();
								};
								$scope.colapseItem = function(listitem) {
									while($scope.listConfig.parents.pop().uid != listitem.uid);
									$scope.listConfig.loadList();
								};
							},
							position: $scope.mdPanel.newPanelPosition().relativeTo($scope.element).addPanelPosition($scope.mdPanel.xPosition.ALIGN_START, $scope.mdPanel.yPosition.BELOW),
							locals: {
								'listConfig' : $scope.listConfig
							},
							panelClass: 'rb-dropdown-panel',
							openFrom: event,
							clickOutsideToClose: true,
							escapeToClose: true,
							focusOnOpen: false,
							zIndex: 2,
							onRemoving: $scope.dropDownClosed
						};
						$scope.listConfig.open = true;
						$scope.listConfig.parents = [];
						//$scope.element.val('');
						$scope.inputValue = '';
						$scope.mdPanel.open(config).then(function(rez) {$scope.mdPanelRef = rez;});
						$scope.loadList();
					}
				};
				$scope.loadList = function() {
					if($scope.listConfig.open == true) {
						var filter = {$multi:'*' + $scope.inputValue + '*'};
						if($scope.parentAttributeName != null  &&  $scope.childAttributeName != null) {
							var lastParentKey = null;
							if($scope.listConfig.parents.length > 0) {
								var lastParent = $scope.listConfig.parents[$scope.listConfig.parents.length - 1];
								lastParentKey = $scope.childAttributeName == 'uid' ? lastParent.uid : lastParent.data[$scope.childAttributeName];
							} 
							filter[$scope.parentAttributeName] = lastParentKey;
						}
						var req = {action:"list", object:$scope.object.objectname, uid:$scope.object.uid, attribute:$scope.attributeName, filter:filter, options:{addrelated:true}};
						$scope.listConfig.loading = true;
						$http.post("../../rbos", req)
							.success(function(response) {
								var responseList = processResponseJSON(response);
								$scope.listConfig.loading = false;
								if(responseList != null) 
									$scope.listConfig.list = responseList;
							});
					}
				};	
				$scope.keyProcessor = function(event) {
					handled = false;
					if(event.keyCode == 38) {
							$scope.listConfig.highlightedIndex--;
							handled = true;
					} else if(event.keyCode == 40) {
							$scope.listConfig.highlightedIndex++;
							handled = true;
					} else if(event.keyCode == 13  ||  event.keyCode == 9) {
							if($scope.listConfig.highlightedIndex > -1  &&  $scope.listConfig.highlightedIndex < $scope.listConfig.list.length)
								$scope.listConfig.selected = $scope.listConfig.list[$scope.listConfig.highlightedIndex];
							$scope.mdPanelRef.close();
							handled = true;
					} else {
						setTimeout(function() {$scope.loadList()}, 100);
					}					
					if (handled) {
						$scope.$apply();
						event.preventDefault();
						event.stopImmediatePropagation();
						event.Handled = true;
					}							
				};
				$scope.dropDownClosed = function() {
					if($scope.listConfig.selected != null) {
						$scope.object.related[$scope.attributeName] = $scope.listConfig.selected;
						$scope.object.attributeHasChanged($scope.attributeName, $http);
					}
					$scope.listConfig.open = false;
					$scope.listConfig.selected = null;
					$scope.listConfig.highlightedIndex = -1;
					$scope.listConfig.list = [];
					$scope.inputValue = $scope.object.related[$scope.attributeName].data[$scope.displayAttributeName];
				};
				$scope.$watch('object.related.' + $scope.attributeName + '.data.' + $scope.displayAttributeName, function(newValue, oldValue) {
					$scope.inputValue = newValue;
				});
			},
			link: function($scope, $element, $attrs) {
				$scope.element = $element;
				$element.bind('focus', $scope.openDropDown);
				$element.bind('keydown', $scope.keyProcessor);
			}
		};
	}]);	


	/***********************************/
	/** Date Input Direcive 	 	  **/
	/***********************************/
	
	module.directive('rbDatetimeInput', function() {
		return {
			restrict:'A',
			scope:true,
			controller: function($scope, $attrs, $http, $compile, $mdPanel) {
				$scope.element = null;
				$scope.formattedDateTime = '';
				$scope.attributeName = $attrs.rbAttribute;
				$scope.format = $attrs.rbFormat;
				$scope.timepicker = {
					time:null,
					mdPanelRef:null,
					open:false,
					views:((($scope.format.includes('YY') ||  $scope.format.includes('MM')  ||  $scope.format.includes('DD')) ? 1 : 0) | ($scope.format.includes('HH') ? 2 : 0) | ($scope.format.includes('mm') ? 4 : 0)),
					currentView:0,
					pickFinalised:false,
					switchView:function(){
						this.currentView = this.currentView + 1;	
						if((1 << this.currentView) <= this.views) {
							while(((1 << this.currentView) & this.views) == 0)
								this.currentView = this.currentView + 1;	
						} else {
							this.pickFinalised = true;
							this.mdPanelRef.close();
						}
					}
				};
				$scope.openDropDown = function(event) {
					if($scope.timepicker.open == false)
					{
						var config = {
							attachTo: angular.element(document.body),
							position: $mdPanel.newPanelPosition().relativeTo($scope.element).addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW),
							template:'<md-content>' +
                                    '<div class="mdp-clock-switch-container" ng-switch="timepicker.currentView" layout layout-align="center center">' +
										'<mdp-calendar date="timepicker.time" selectevent="timepicker.switchView()" auto-switch="1" ng-switch-when="0"></mdp-calendar>' +
                                        '<mdp-clock time="timepicker.time" type="hours" auto-switch="1" ng-switch-when="1"></mdp-clock>' +
                                        '<mdp-clock time="timepicker.time" type="minutes" auto-switch="1" ng-switch-when="2"></mdp-clock>' +
                                        '<div ng-switch-when="4">Can close</div>' +
                                    '</div>' +
                                '</md-content>',
							controller: function($scope, mdPanelRef, timepicker) { 
								$scope.timepicker = timepicker;
								$scope.mdPanelRef = mdPanelRef;
								$scope.time = timepicker.time;
							},
							locals: {
								'timepicker' : $scope.timepicker,
							},
							panelClass: 'rb-dropdown-panel',
							openFrom: event,
							clickOutsideToClose: true,
							escapeToClose: true,
							focusOnOpen: false,
							zIndex: 2,
							onRemoving: $scope.dropDownClosed
						};
						$scope.timepicker.open = true;
						$scope.timepicker.currentView = -1;
						$scope.timepicker.switchView();
						$scope.timepicker.pickFinalised = false;
						if($scope.timepicker.time == null) {
							$scope.timepicker.time = moment();
							$scope.timepicker.time.set({second:0,millisecond:0});
						}
						$mdPanel.open(config).then(function(rez) {$scope.timepicker.mdPanelRef = rez;});
					}
				};
				$scope.keyProcessor = function(event) {
					handled = false;	
					if(event.keyCode == 13  ||  event.keyCode == 9) {
						$scope.timepicker.mdPanelRef.close();
					}						
				};
				$scope.dropDownClosed = function() {
					$scope.timepicker.open = false;
					if($scope.timepicker.pickFinalised) {
						$scope.object.data[$scope.attributeName] = $scope.timepicker.time.toISOString();
						$scope.object.attributeHasChanged($scope.attributeName, $http);
					}
				};
				$scope.$watch('object.data.' + $scope.attributeName, function(newValue, oldValue) {
					if(newValue == null) {
						$scope.timepicker.time = null;
						$scope.formattedDateTime = '';
					} else {
						$scope.timepicker.time = moment(newValue);
						$scope.formattedDateTime = $scope.timepicker.time.format($scope.format);
					}
				});
			},
			link: function($scope, $element, $attrs) {
				$scope.element = $element;
				$element.bind('focus', $scope.openDropDown);
				$element.bind('keydown', $scope.keyProcessor);
			}
		};
	});			
	
	
	/***********************************/
	/** Root Controller			 	  **/
	/***********************************/
	
	module.controller('desktoproot', function rootCtl($scope,$attrs,$http) {
		
	});
	
	  
	/***********************************/
	/** Form Controller			 	  **/
	/***********************************/

	
	module.controller('form', function formCtl($scope,$attrs,$http) {
		$scope.objectName = $attrs.rbObject;
		$scope.object = null;
		$scope.dynamicSearchText = "";
		
		
		$scope.setObject = function(object) {
			$scope.object = object;
		}

		
		$scope.save = function(){
			if($scope.object != null) {
				$scope.object.save($http);
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

	 
	 
	module.controller('list', function listCtl($scope,$attrs,$http,$element) {
		$scope.objectName = $attrs.rbObject;
		$scope.list = [];
		$scope.selectedObject = null;
		$scope.relatedConfig = null;
		$scope.relatedObject = null;
		$scope.searchText = "";
		$scope.baseFilter = {};
		$scope.searchText = null;
		$scope.element = $element;
		$scope.visible = false;
		$scope.loading = false;

		if($attrs.rbRelated != null  &&  $attrs.rbRelated.length > 0) {
			$scope.relatedConfig = JSON.parse($attrs.rbRelated.replace(/'/g, '"'));
			$scope.relationshipFilter = {uid:-1};
		}
			
		if($attrs.rbBaseFilter != null  &&  $attrs.rbBaseFilter.length > 0)
			$scope.baseFilter = JSON.parse($attrs.rbBaseFilter.replace(/'/g, '"'));


		$scope.search = function(searchText) {
			$scope.clearList();		
			$scope.searchText = searchText;
			$scope.load();
		}
		
		$scope.getBaseAndRelationshipFilter = function() {
			var filter = {};
			for (var key in $scope.baseFilter)
				filter[key] = $scope.baseFilter[key];
			if($scope.relatedConfig != null  &&  $scope.relatedObject != null) {
				var relationshipFilter = getFilterFromRelationship($scope.relatedObject, $scope.relatedConfig.relationship)
				for (var key in relationshipFilter)
					filter[key] = relationshipFilter[key];
			}
			return filter;
		}
		
		$scope.getFullFilter = function() {
			var filter = $scope.getBaseAndRelationshipFilter();
			if($scope.searchText != null  &&  $scope.searchText != '') {
				filter['$multi'] = '*' + $scope.searchText + '*';
			}
			return filter;
		}

		$scope.load = function() {
			if($scope.visible  &&  $scope.list.length == 0  &&  ($scope.relatedConfig == null ||  ($scope.relatedConfig != null  &&  $scope.relatedObject != null))) {
				var req = {action:"list", object:$scope.objectName, filter:$scope.getFullFilter(), options:{addrelated:true, addvalidation:true}};
				$scope.loading = true;
				$http.post("../../rbos", req)
				.success(function(response) {
					var responseList = processResponseJSON(response);
					$scope.loading = false;
					if(responseList != null) 
						$scope.list = responseList;
				})
				.error(function(error, status) {
					$scope.loading = false;
					alert(error.error);
				});
			}
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
				alert(error.error);
			});
		};		
		
		
		$scope.save = function() {
			for(var i = 0; i < $scope.list.length; i++) {
				$scope.list[i].save($http);
			}		
		}		
		
		$scope.selectObject = function(object) {
			if(object != null && $scope.list.includes(object)) {
				$scope.selectedObject = object;
				$scope.$emit('objectSelectedEmit', object);
			}
		}

		$scope.clearList = function() {
			$scope.list = [];
			$scope.selectedObject = null;
			$scope.$emit('nullObjectSelectedEmit', $scope.objectName);
		}		
		
		$scope.$on('objectSelected', function($event, object){
			if($scope.relatedConfig != null && object.objectname == $scope.relatedConfig.objectname) {
				$scope.clearList();
				$scope.relatedObject = object;
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
		
		$scope.$watch(function() {
			return $scope.element.prop('offsetParent') != null;
		}, function(newValue, oldValue) {
			$scope.visible = newValue;
			if(newValue == true)
				$scope.load();
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
	/** Tab Controller	    		 **/
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
			$scope.$parent.selectedObject.attributeHasChanged('geometry', $http);
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
	 
	 

	/***********************************/
	/** Workflow Actions Controller	  **/
	/***********************************/

	module.controller('workflowactions', function wfactionCtl($scope,$attrs,$http) {
		$scope.notification = {};
		
		$scope.openMenu = function($mdOpenMenu, ev) {
			$http.post("../../rbpm", {action:'getnotifications', filter:{'data.objectname':$scope.$parent.objectName, 'data.uid':$scope.$parent.object.uid}})
			.success(function(response) {
				if(response.result.length == 0) {
					$scope.notification = {message:"No actions"};
					$mdOpenMenu(ev);
				} else if(response.result.length > 0) {
					$scope.notification = response.result[0];
					$mdOpenMenu(ev);
				}
			})
			.error(function(error, status) {
				alert(error.error);
			});	
		}
		
		$scope.processAction = function(action) {
			$http.post("../../rbpm", {action:'processaction', processaction:action, pid:$scope.notification.pid})
			.success(function(response) {
				$scope.$parent.object.refresh($http);
			})
			.error(function(error, status) {
				alert(error.error);
			});	
		}

	});