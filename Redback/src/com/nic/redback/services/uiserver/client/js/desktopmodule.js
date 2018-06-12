	var module = angular.module("desktopmodule", ['ngMaterial', 'uiGmapgoogle-maps', 'mdPickers']);	

	/***********************************/
	/** Drag Directive			 	  **/
	/***********************************/
	
	module.directive('rbDraggable', ['$rootScope', function($rootScope) {
        return {
            restrict: 'A',
            link: function($scope, $element, $attrs, $controller) {
                angular.element($element).attr("draggable", "true");
				$scope.rbDragObjectName = $attrs.rbDraggable;
                $element.bind("dragstart", function(e) {
					var dragObject = eval('$scope.' + $scope.rbDragObjectName);
                    $scope.$emit("rbDragStart", dragObject, e);
                });

                $element.bind("dragend", function(e) {
					var dragObject = eval('$scope.' + $scope.rbDragObjectName);
                    $scope.$emit("rbDragEnd", dragObject, e);
                });
            }
        }
    }]);
	
	module.directive('rbDropTarget', ['$rootScope', function($rootScope) {
        return {
            restrict: 'A',
            link: function($scope, $element, $attrs, $controller) {
				$scope.rbDropObjectName = $attrs.rbDropTarget;
                $element.bind("dragover", function(e) {
					if (e.preventDefault) 
						e.preventDefault(); 
					e.dataTransfer.dropEffect = 'move';
					return false;
                });

                $element.bind("drop", function(e) {
					if (e.preventDefault) 
						e.preventDefault(); 
					if (e.stopPropagation)
						e.stopPropagation();
					var dropObject = null;
					if($scope.rbDropObjectName != null  &&  $scope.rbDropObjectName != '')
						dropObject = eval('$scope.' + $scope.rbDropObjectName);
                    $scope.$emit("rbDragDrop", dropObject, e);
                });
            }
        }
    }]);
	
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
					if($scope.object.related[$scope.attributeName] != null)
						$scope.inputValue = $scope.object.related[$scope.attributeName].data[$scope.displayAttributeName];
					else
						$scope.inputValue = '';
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
		$scope.largemenu = true;
		$scope.menuwidth = 300;
		$scope.page = null;
		$scope.pageLabel = 'Welcome';
		
		$scope.toggleMenu = function() {
			if($scope.largemenu) {
				$scope.largemenu = false;
				$scope.menuwidth = 56;
			} else {
				$scope.largemenu = true;
				$scope.menuwidth = null;
			}
		}
		
		$scope.navigate = function(view, label) {
			$scope.page = '../view/' + view;
			$scope.pageLabel = label;
		}
		
		$scope.today = function() {
			return (new Date());			
		}		
	}).config(function($mdIconProvider) {
	    $mdIconProvider
	       .iconSet('wms', '../resource/wms.svg', 24);
	});
	
	  
	/***********************************/
	/** Form Directive			 	  **/
	/***********************************/
	
	module.directive('rbForm', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $compile) {
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

				$scope.action = function(action, param){
					if($scope.object != null) {
						if(action == 'save') {
							$scope.save();
						} else if(action == 'create') {
							$scope.create();
						} else {
							var req = {action:"execute", object:$scope.objectName, uid:$scope.object.uid, 'function':action, options:{addrelated:true, addvalidation:true}};
							$http.post("../../rbos", req)
							.success(function(response) {
								var responseObject = processResponseJSON(response);
								if(responseObject != null) {
									$scope.setObject(responseObject);
									$scope.$emit('refreshRelatedEmit', $scope.object);
								}
							})
							.error(function(error, status) {
								alert(error.error);
							});
						}
					}
				};

				
				$scope.$on('objectSelected', function($event, object){
					if(object != null  &&  object.objectname == $scope.objectName)
						$scope.setObject(object);
				});


				$scope.$on('nullObjectSelected', function($event, objectName){
					if(objectName == $scope.objectName)
						$scope.setObject(null);
				});		
			}
		};
	});	
	


	/***********************************/
	/** Dataset Directive				  **/
	/***********************************/

	module.directive('rbDataset', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $element, $compile) {
				$scope.objectName = $attrs.rbObject;
				$scope.list = [];
				$scope.selected = null;
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

				$scope.today = function() {
					return (new Date());			
				}
				
				$scope.search = function(searchText) {
					$scope.clearList();		
					$scope.searchText = searchText;
					$scope.load();
				}
				
				$scope.getBaseAndRelationshipFilter = function() {
					var filter = {};
					for (var key in $scope.baseFilter) {
						var value = $scope.baseFilter[key];
						if(typeof value == "string"  &&  value.startsWith("{{") &&  value.endsWith("}}")) {
							var evalExpr = 'value = ' + value.replace('{{', '').replace('}}', '');
							eval(evalExpr);
						}
						filter[key] = value;
					}
					if($scope.relatedConfig != null  &&  $scope.relatedObject != null) {
						for (var key in $scope.relatedConfig.relationship) {
							var value = $scope.relatedConfig.relationship[key];
							if(typeof value == "string"  &&  value.startsWith("{{") &&  value.endsWith("}}")) {
								var evalExpr = 'value = ' + value.replace('{{', '$scope.relatedObject.data.').replace('}}', '').replace('.data.uid', '.uid');
								eval(evalExpr);
							}
							filter[key] = value;
						}
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
							$scope.select(responseObject);
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

				$scope.action = function(action, param){
					if(action == 'save') {
						$scope.save();
					} else if(action == 'create') {
						$scope.create();
					} 
				};
				
				$scope.select = function(object) {
					if(object != null && $scope.list.includes(object)) {
						$scope.selected = object;
						$scope.$emit('objectSelectedEmit', object);
					}
				}

				$scope.clearList = function() {
					$scope.list = [];
					$scope.selected = null;
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
			}			
		};
	});		

	
	/***********************************/
	/** Processset Directive		  **/
	/***********************************/

	module.directive('rbProcessset', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: rbProcessSetController		
		};
	});		
	
	function rbProcessSetController($scope, $attrs, $http, $element, $compile) {
		$scope.list = [];
		$scope.selected = null;
		$scope.searchText = "";
		$scope.baseFilter = {};
		$scope.searchText = null;
		$scope.element = $element;
		$scope.visible = false;
		$scope.loading = false;
		$scope.viewmap = {};
			
		if($attrs.rbBaseFilter != null  &&  $attrs.rbBaseFilter.length > 0)
			$scope.baseFilter = JSON.parse($attrs.rbBaseFilter.replace(/'/g, '"'));

		if($attrs.rbViewMap != null  &&  $attrs.rbViewMap.length > 0)
			$scope.viewmap = JSON.parse($attrs.rbViewMap.replace(/'/g, '"'));

		$scope.search = function(searchText) {
			$scope.clearList();		
			$scope.searchText = searchText;
			$scope.load();
		}
		
		$scope.getBaseFilter = function() {
			var filter = {};
			for (var key in $scope.baseFilter) {
				var value = $scope.baseFilter[key];
				if(typeof value == "string"  &&  value.startsWith("{{") &&  value.endsWith("}}")) {
					var evalExpr = 'value = ' + value.replace('{{', '').replace('}}', '');
					eval(evalExpr);
				}
				filter[key] = value;
			}
			return filter;
		}
		
		$scope.getFullFilter = function() {
			var filter = $scope.getBaseFilter();
			if($scope.searchText != null  &&  $scope.searchText != '') {
				filter['$multi'] = '*' + $scope.searchText + '*';
			}
			return filter;
		}

		$scope.load = function() {
			if($scope.visible  &&  $scope.list.length == 0) {
				var req = {action:"getnotifications", filter:$scope.getFullFilter(), viewdata:['objectname', 'uid']};
				$scope.loading = true;
				$http.post("../../rbpm", req)
				.success(function(response) {
					$scope.loading = false;
					if(response != null) {
						$scope.list = response.result;
						$scope.loadLinkedObjects();
					}
				})
				.error(function(error, status) {
					$scope.loading = false;
					alert(error.error);
				});
			}
		}	

		$scope.loadLinkedObjects = function() {
			if($scope.visible  &&  $scope.list.length != 0) {
				for(var i = 0; i < $scope.list.length; i++) {
					var obj = findExistingObject($scope.list[i].data.objectname, $scope.list[i].data.uid);
					if(obj == null) {
						var req = {action:"get", object:$scope.list[i].data.objectname, uid:$scope.list[i].data.uid,  options:{addrelated:true, addvalidation:true}};
						$http.post("../../rbos", req)
						.success(function(response) {
							var obj = processResponseJSON(response);
							for(var j = 0; j < $scope.list.length; j++)
								if($scope.list[j].data.objectname == obj.objectname  &&  $scope.list[j].data.uid == obj.uid)
									$scope.list[j].object = obj;
						})
						.error(function(error, status) {
						});
					} else {
						$scope.list[i].object = obj;
					}
				}
			}
		}
	
		$scope.select = function(process) {
			if(process != null && $scope.list.includes(process)) {
				$scope.selected = process;
				$scope.$broadcast('loadView', "processobject", $scope.viewmap[process.object.objectname]);
				$scope.$broadcast('objectSelected', process.object);
			}
		}

		$scope.clearList = function() {
			$scope.list = [];
			$scope.selected = null;
		}		
		
		$scope.$on('processActionEmit', function($event, process, pid, action){
			$scope.clearList();
			$scope.load();
		});
				
		$scope.$watch(function() {
			return $scope.element.prop('offsetParent') != null;
		}, function(newValue, oldValue) {
			$scope.visible = newValue;
			if(newValue == true)
				$scope.load();
		});
		
		$scope.load();
	}	
	
	/***********************************/
	/** Layout Directive			 **/
	/***********************************/

	
	module.directive('rbLayout', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $compile) {
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
			}			
		};
	});		
	 
	 
	/***********************************/
	/** View Loader Directive		 **/
	/***********************************/
	 
	module.directive('rbViewLoader', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: rbViewLoaderController		
		};
	});	
	
	function rbViewLoaderController($scope, $attrs, $http, $element, $compile) {
		$scope.name = $attrs.rbName;
		$scope.view = null;
		$scope.waitingSelectedObject = null;

		$scope.$on('loadView', function($event, loadername, viewname){
			if(loadername != null  &&  loadername == $scope.name)
				$scope.view = '../view/' + viewname;
		});
		
		$scope.$on('objectSelected', function($event, object){
			$scope.waitingSelectedObject = object;
		});

		$scope.$on("$includeContentLoaded", function(event, templateName){
			 if($scope.waitingSelectedObject != null) {
				$scope.$broadcast('objectSelected', $scope.waitingSelectedObject);
				$scope.waitingSelectedObject = null;
			 }
		});
	}
	
	/***********************************/
	/** Tab Section Directive	 	 **/
	/***********************************/

	module.directive('rbTabSection', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $compile) {
				$scope.tabs = [];
				$scope.selected_tab = null;
				
				$scope.selectTab = function(tab) {
					$scope.selected_tab = tab;
				}			
			}			
		};
	});		
	
	
	/***********************************/
	/** Map Controller		    	  **/
	/***********************************/

	module.directive('rbMap', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $element, $compile) {
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
					$scope.$parent.selected.data.geometry = {
						type: 'point',
						coords: position
					}
					$scope.$parent.selected.attributeHasChanged('geometry', $http);
					$scope.hideContextMenu();
				}

				$scope.markerHasMoved = function(marker, eventName, model, args) {
					$scope.setSelectedObjectPosition({latitude:marker.position.lat(), longitude:marker.position.lng()});
				}
				
				$scope.markerSelected = function(marker, eventName, model, args) {
					//$scope.$emit('objectSelectedEmit', model.$parent.object);
					$scope.select(model.$parent.object);
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
					if($scope.$parent.selected != null)
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
			}			
		};
	});		
	 

	/***********************************/
	/** Workflow Actions Controller	  **/
	/***********************************/

	module.directive('rbProcessActionsButton', function($compile) {
		return {
			restrict:'C',
			scope:true,
			controller: rbProcessActionButtonController
		};
	});		

	function rbProcessActionButtonController($scope, $attrs, $http, $element, $compile, $mdPanel) {
		$scope.notification = {};
		
		$scope.activate = function(ev) {
			if($scope.$parent.object != null) {
				$http.post("../../rbpm", {action:'getnotifications', filter:{'data.objectname':$scope.$parent.objectName, 'data.uid':$scope.$parent.object.uid}})
				.success(function(response) {
					if(response.result.length == 0) {
						$scope.notification = {message:"No actions"};
						$scope.openDialog();
					} else if(response.result.length > 0) {
						$scope.notification = response.result[0];
						$scope.openDialog();
					}
				})
				.error(function(error, status) {
					alert(error.error);
				});	
			}
		}
		
		$scope.openDialog = function() {
			var config = {
				attachTo: angular.element(document.body),
				template:'<md-list>' +
						'<md-list-item>{{notification.message}}</md-list-item>' +
						'<md-divider></md-divider>' +
						'<md-list-item ng-repeat="item in notification.actions" ng-click="processAction(item.action)">{{item.description}}</md-list-item>' + 
						'</md-list>',
				scope:$scope,
				preserveScope:true,
				position: $mdPanel.newPanelPosition().relativeTo($element).addPanelPosition($mdPanel.xPosition.ALIGN_END, $mdPanel.yPosition.BELOW),
				panelClass: 'rb-dropdown-panel',
				clickOutsideToClose: true
			};
			$mdPanel.open(config).then(function(rez) {$scope.mdPanelRef = rez;});
		}
		
		$scope.processAction = function(action) {
			$scope.mdPanelRef.close();
			$http.post("../../rbpm", {action:'processaction', processaction:action, pid:$scope.notification.pid})
			.success(function(response) {
				if(response.rbobjectupdate != null) {
					for(var i = 0; i < response.rbobjectupdate.length; i++) {
						var obj = findExistingObject(response.rbobjectupdate[i].objectname, response.rbobjectupdate[i].uid);
						if(obj != null)
							obj.refresh($http);
					}
				}
				$scope.$emit('processActionEmit', $scope.notification.process, $scope.notification.pid, action);
			})
			.error(function(error, status) {
				alert(error.error);
			});	
		}
	}	
	
	
	/***********************************/
	/** Match Scheduler Controller	  **/
	/***********************************/

	module.directive('rbMatchScheduler', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $element, $compile) {
				$scope.config = JSON.parse($attrs.rbConfig.replace(/'/g, '"'));		
				$scope.spanDays = 3;
				$scope.scale = 50000;
				$scope.startDate = new Date();
				$scope.startMS = $scope.startDate.getTime();
				$scope.endMS = $scope.startMS + ($scope.spanDays * 86400000);
				$scope.width = ($scope.spanDays * 86400000) / $scope.scale;
				$scope.scrollLeft = 0;
				$scope.dragging = null;
				$scope.draggingOffset = 0;
				$scope.offerContainer = angular.element($element[0].querySelector('.rb-sched-offer-container'));
				$scope.demandContainer = angular.element($element[0].querySelector('.rb-sched-demand-container'));
				$scope.rbobjects = {
						demand:[],
						offer:[]
				}
				
				$scope.offerContainer.bind('scroll', function() {
					$scope.scrollLeft = $scope.offerContainer.prop('scrollLeft');
					$scope.demandContainer.prop('scrollLeft', $scope.scrollLeft);
				});
				
				$scope.loadOffer = function() {
					var filter = $scope.config.offer.filter;
					filter[$scope.config.offer.start] = {'$lt': (new Date($scope.endMS)).toISOString()};
					filter[$scope.config.offer.finish] = {'$gt': (new Date($scope.startMS)).toISOString()};
					var req = {action:"list", object:$scope.config.offer.object, filter:filter};
					$http.post("../../rbos", req)
					.success(function(response) {
						var responseList = processResponseJSON(response);
						if(responseList != null) 
							$scope.rbobjects.offer = responseList;
						$scope.transform();
					})
					.error(function(error, status) {
						alert(error.error);
					});			
				};
				
				$scope.loadDemand = function() {
					var filter = $scope.config.demand.filter;
					filter['$or'] = [{}, {}, {}] ;
					filter['$or'][0][$scope.config.demand.start] = null;
					filter['$or'][1][$scope.config.demand.finish] = null;
					filter['$or'][2][$scope.config.demand.start] = {$lt: (new Date($scope.endMS)).toISOString()};
					var req = {action:"list", object:$scope.config.demand.object, filter:filter};
					$http.post("../../rbos", req)
					.success(function(response) {
						var responseList = processResponseJSON(response);
						if(responseList != null) 
							$scope.rbobjects.demand = responseList;
						$scope.transform();
					})
					.error(function(error, status) {
						alert(error.error);
					});
				}
				
				$scope.transform = function() {
					$scope.data = {
						demandgroups:[],
						offergroups:[],
						markers:[]
					};
					for(var i = 0; i < $scope.rbobjects.offer.length; i++) {
						var rbo = $scope.rbobjects.offer[i];
						var groupKey = $scope.config.offer.group == 'uid' ? rbo.uid : rbo.data[$scope.config.offer.group];
						var groupLabel = rbo.data[$scope.config.offer.grouplabel];
						var group = null;
						for(var j = 0; j < $scope.data.offergroups.length; j++)
							if($scope.data.offergroups[j].key == groupKey)
								group = $scope.data.offergroups[j];
						if(group == null) {
							group = {
								key:groupKey,
								label:groupLabel,
								offers:[],
								matcheddemands:[]
							}				
							$scope.data.offergroups.push(group);
						}
						var offer = {
							rbo:rbo,
							start:$scope.dateToX(rbo.data[$scope.config.offer.start]),
							finish:$scope.dateToX(rbo.data[$scope.config.offer.finish]),
							capabilities:($scope.config.offer.capabilities != null &&  rbo.data[$scope.config.offer.capabilities] != null) ? rbo.data[$scope.config.offer.capabilities].split(',') : null,
							canoffer:true
						};
						if(offer.start < 0)
							offer.start = 0;
						if(offer.finish > $scope.width)
							offer.finish = $scope.width;
						group.offers.push(offer);
					}	

					for(var i = 0; i < $scope.rbobjects.demand.length; i++) {
						var rbo = $scope.rbobjects.demand[i];
						var groupKey = $scope.config.demand.group == 'uid' ? rbo.uid : rbo.data[$scope.config.demand.group];
						var groupLabel = rbo.data[$scope.config.demand.grouplabel];
						var group = null;
						for(var j = 0; j < $scope.data.demandgroups.length; j++)
							if($scope.data.demandgroups[j].key == groupKey)
								group = $scope.data.demandgroups[j];
						if(group == null) {
							group = {
								key:groupKey,
								label:groupLabel,
								unmatched:[],
								matched:[]
							}				
							$scope.data.demandgroups.push(group);
						}
						var demand = {
							rbo:rbo,
							start:$scope.dateToX(rbo.data[$scope.config.demand.start]),
							finish:$scope.dateToX(rbo.data[$scope.config.demand.finish]),
							requirements:($scope.config.demand.requirements != null &&  rbo.data[$scope.config.demand.requirements] != null) ? rbo.data[$scope.config.demand.requirements].split(',') : null
						};
						if(demand.start < 0) {
							demand.finish = (demand.finish - demand.start) == 0 ? 100 : (demand.finish - demand.start);
							demand.start = 0;
						}
						var matched = false;
						var demandlink = rbo.data[$scope.config.demand.link];
						if(demandlink != null) {
							var offergroup = null;
							for(var j = 0; j < $scope.data.offergroups.length; j++) {
								for(var k = 0; k < $scope.data.offergroups[j].offers.length; k++) {
									var offerlink = $scope.config.offer.link == 'uid' ? $scope.data.offergroups[j].offers[k].rbo.uid : $scope.data.offergroups[j].offers[k].rbo.data[$scope.config.offer.link];
									if(offerlink == demandlink) {
										$scope.data.offergroups[j].matcheddemands.push(demand);
										group.matched.push(demand);
										matched = true;
									}
								}
							}
						}
						if(!matched)
							group.unmatched.push(demand);
					}
					
					var d = new Date($scope.startMS + 86399999);
					d.setHours(0,0,0,0);
					var markerPos = $scope.dateToX(d);
					var markerPeriod = (86400000 / $scope.scale);
					while(markerPos < $scope.width) {
						var marker = {
							position:markerPos,
							label:$scope.XToDate(markerPos).toLocaleDateString('en-GB', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
						};
						markerPos = markerPos + markerPeriod;
						$scope.data.markers.push(marker);
					}			
				};
				
				$scope.dateToX = function(dateStr) {
					var x = ((new Date(dateStr)).getTime() - $scope.startMS) / $scope.scale;
					return x;
				};
				
				$scope.XToDate = function(x) {
					var date = new Date((x * $scope.scale) + $scope.startMS);
					return date;
				};
				
				$scope.markOfferings = function(demand) {
					for(var j = 0; j < $scope.data.offergroups.length; j++) 
						for(var k = 0; k < $scope.data.offergroups[j].offers.length; k++)
							if(demand != null)
								$scope.data.offergroups[j].offers[k].canoffer = $scope.canMatch(demand, $scope.data.offergroups[j].offers[k]);
							else
								$scope.data.offergroups[j].offers[k].canoffer = true;
				}
				
				$scope.canMatch = function(demand, offer) {
					var matches = true;
					if(demand.requirements != null  &&  demand.requirements.length > 0) {
						if(offer.capabilities != null  &&  offer.capabilities.length > 0) {
							for(var i = 0; i < demand.requirements.length; i++)
								if(!offer.capabilities.includes(demand.requirements[i]))
									matches = false;
						} else {
							matches = false;
						}
					} 
					return matches;
				}
				
				$scope.spanChanged = function() {
					$scope.endMS = $scope.startMS + ($scope.spanDays * 86400000);
					$scope.width = ($scope.spanDays * 86400000) / $scope.scale;
					$scope.loadOffer();
					$scope.loadDemand();
				}
				
				$scope.zoomChanged = function() {
					$scope.width = ($scope.spanDays * 86400000) / $scope.scale;
					$scope.transform();
				}

				$scope.$on('rbDragStart', function($event, object, e){
					$scope.dragging = object;
					$scope.draggingOffset = e.clientX;
					$scope.markOfferings(object);
					$scope.$apply();
				});

				$scope.$on('rbDragDrop', function($event, object, e){
					var update = {};
					if(object != null) {
						if($scope.canMatch($scope.dragging, object)) {
							var offerlink = $scope.config.offer.link == 'uid' ? object.rbo.uid : object.rbo.data[$scope.config.offer.link];
							$scope.dragging.rbo.data[$scope.config.demand.link] = offerlink;
							update[$scope.config.demand.link] = offerlink;
						} else {
							$scope.markOfferings(null);
							$scope.$apply();
							return;
						}
					} else {
						$scope.dragging.rbo.data[$scope.config.demand.link] = null;
						update[$scope.config.demand.link] = null;
					}
					var diffX = e.clientX - $scope.draggingOffset;
					var newStartDate = $scope.XToDate($scope.dragging.start + diffX).toISOString();
					var newFinishDate = $scope.XToDate($scope.dragging.finish + diffX).toISOString();
					$scope.dragging.rbo.data[$scope.config.demand.star] = newStartDate;
					$scope.dragging.rbo.data[$scope.config.demand.finish] = newFinishDate;
					update[$scope.config.demand.start] = newStartDate;
					update[$scope.config.demand.finish] = newFinishDate;
					var req = {action:"update", object:$scope.config.demand.object, uid:$scope.dragging.rbo.uid, data:update, options:{addrelated:true, addvalidation:true}};
					$http.post("../../rbos", req)
					.success(function(response) {
						processResponseJSON(response);
						$scope.transform();
					})
					.error(function(error, status) {
						alert(error.error);
					});			
					$scope.dragging = null;
					$scope.markOfferings(null);
					$scope.$apply();
				});

				$scope.loadOffer();
				$scope.loadDemand();
			}			
		};
	});			