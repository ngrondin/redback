	var module = angular.module("mobilemodule", ['ngMaterial', 'mdPickers', 'ngAnimate']);	

	module.directive('rbTouchStart', function($compile) {
		return {
			restrict:'A',
			link: function($scope, $element, $attrs) {
				$element.bind('touchstart', function(e) {eval('$scope.' + $attrs.rbTouchStart + '(e, $scope)')});
			}
		};
	});	

	module.directive('rbTouchMove', function($compile) {
		return {
			restrict:'A',
			link: function($scope, $element, $attrs) {
				$element.bind('touchmove', function(e) {eval('$scope.' + $attrs.rbTouchMove + '(e, $scope)')});
			}
		};
	});	
	
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
			controller: function($scope, $attrs, $http, $compile, $mdDialog) {
				$scope.mdDialog = $mdDialog;
				$scope.mdDialogRef = null;
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
					if($scope.listConfig.open == false) {
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
							controller: function($scope, $mdDialog, listConfig) { 
								$scope.listConfig = listConfig;
								$scope.mdDialog = $mdDialog;
								$scope.selectItem = function(listitem) {
									$scope.listConfig.selected = listitem;
									$scope.mdDialog.hide();
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
							fullscreen: false,
							locals: {
								'listConfig' : $scope.listConfig
							},
							panelClass: 'rb-dropdown-panel',
							openFrom: event,
							clickOutsideToClose: true,
							escapeToClose: true,
							focusOnOpen: true,
							zIndex: 20,
							onRemoving: $scope.dropDownClosed
						};
						$scope.listConfig.open = true;
						$scope.listConfig.parents = [];
						//$scope.element.blur();
						$scope.inputValue = '';
						$scope.mdDialog.show(config).then(function(rez) {$scope.mdDialogRef = rez;});
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
				$scope.element.prop('readOnly', true);
				$element.bind('click', $scope.openDropDown);
				$element.bind('focus', function(event) {event.preventDefault();});
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
			controller: function($scope, $attrs, $http, $compile, $mdDialog) {
				$scope.element = null;
				$scope.formattedDateTime = '';
				$scope.attributeName = $attrs.rbAttribute;
				$scope.format = $attrs.rbFormat;
				$scope.timepicker = {
					time:null,
					mdDialog:null,
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
							$mdDialog.hide();
						}
					}
				};
				$scope.openDropDown = function(event) {
					if($scope.timepicker.open == false)
					{
						var config = {
							attachTo: angular.element(document.body),
							template:'<md-content>' +
                                    '<div class="mdp-clock-switch-container" ng-switch="timepicker.currentView" layout layout-align="center center">' +
										'<mdp-calendar date="timepicker.time" selectevent="timepicker.switchView()" auto-switch="1" ng-switch-when="0"></mdp-calendar>' +
                                        '<mdp-clock time="timepicker.time" type="hours" auto-switch="1" ng-switch-when="1"></mdp-clock>' +
                                        '<mdp-clock time="timepicker.time" type="minutes" auto-switch="1" ng-switch-when="2"></mdp-clock>' +
                                        '<div ng-switch-when="4">Can close</div>' +
                                    '</div>' +
                                '</md-content>',
							controller: function($scope, $mdDialog, timepicker) { 
								$scope.timepicker = timepicker;
								$scope.mdDialog = $mdDialog;
								$scope.time = timepicker.time;
							},
							locals: {
								'timepicker' : $scope.timepicker,
							},
							fullScreen: false,
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
						$mdDialog.show(config).then(function(rez) {$scope.timepicker.mdPanelRef = rez;});
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
				$scope.element.prop('readOnly', true);
				$element.bind('click', $scope.openDropDown);
				$element.bind('focus', function(event) {event.preventDefault();});
			}
		};
	});			
	
	
	/***********************************/
	/** File Input Directive	 	  **/
	/***********************************/
	
	module.directive('rbFileInput', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $compile) {
				$scope.element = null;
				$scope.inputElement = null;
				$scope.fileuids = [];
				$scope.currentFile = null;
				$scope.touchX = null;
				$scope.touchFileUID = null;
				$scope.attributeName = $attrs.rbAttribute;
				
				$scope.fileSelected = function() {
					$scope.currentFile = $scope.inputElement.files[0];
					var fr = new FileReader();
					fr.onload = $scope.uploadFile;
					fr.readAsArrayBuffer($scope.currentFile);
				}
				
				$scope.uploadFile = function(e) {
					$http({
						url:'../../rbfs', 
						method: 'POST',
						data: new Uint8Array(e.target.result),
						headers:{'Content-Type': $scope.currentFile.type, 'rb-filename': $scope.currentFile.name},
						transformRequest: []
					})
					.success(function(response) {
						var currentVal = $scope.object.data[$scope.attributeName];
						var newUID = response.uid;
						$scope.object.data[$scope.attributeName] = currentVal + (currentVal.length != 0 ? ',' : '') + newUID;
						$scope.object.attributeHasChanged($scope.attributeName, $http);
						$scope.currentFile = null;
					})
					.error(function(error, status) {
						alert(error.error);
					});
				}
				
				$scope.touchStart = function(e, scp) {
					$scope.touchX = e.touches[0].clientX;
					$scope.touchFileUID = scp.fileuid;
				}
				
				$scope.touchMove = function(e, scp) {
					if($scope.touchFileUID = scp.fileuid) {
						var pos = e.touches[0].clientX;
						if(pos > $scope.touchX + 200) {
							var index = $scope.fileuids.indexOf($scope.touchFileUID);
							if(index > -1)
							{
								$scope.fileuids.splice(index, 1);
								$scope.object.data[$scope.attributeName] = $scope.fileuids.toString();
								$scope.object.attributeHasChanged($scope.attributeName, $http);
							}
						}
					}
				}
				
				$scope.$watch('object.data.' + $scope.attributeName, function(newValue, oldValue) {
					if(newValue != null  &&  newValue.length > 0) {
						$scope.fileuids = newValue.split(',');
					} else {
						$scope.fileuids = [];
					}
				});
			},
			link: function($scope, $element, $attrs) {
				$scope.element = $element;
				$scope.inputElement = $scope.element[0].querySelector('input');
				$element.bind('change', $scope.fileSelected);
			}
		};
	});	
	
	
	/***********************************/
	/** Root Controller			 	  **/
	/***********************************/
	
	module.controller('mobileroot', function rootCtl($scope,$attrs,$http,$element) {
		$scope.largemenu = true;
		$scope.menuwidth = 300;
		$scope.page = null;
		$scope.pageLabel = 'Welcome';
		
		$scope.action = function(action, param) {
			
		}
		
		$scope.touchMove = function(event) {
			//if ($scope.maybePreventPullToRefresh) {
			//	$scope.maybePreventPullToRefresh = false;
            //    event.preventDefault();
			//}
		}

		$scope.touchStart = function(event) {
			//$scope.maybePreventPullToRefresh = (window.pageYOffset === 0);
		}
		
		$scope.getInitials = function(str)
		{
			var initials = '';
			var isWordStart = true;
			for(var i = 0; i < str.length; i++) {
				var c = str.charAt(i);
				if(isWordStart) {
					initials = initials + c;
					isWordStart = false;
				}
				if(c == ' ')
					isWordStart = true;
			}
			return initials;
		}

		$element.bind('touchmove', $scope.touchMove);
		$element.bind('touchstart', $scope.touchStart);
		
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
						} else if(action == 'back') {
							$scope.$parent.action(action, param);
						}else {
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
				
				/*$scope.intent = function(target) {
					$scope.$parent.intent(target);
				};*/

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
				
				$scope.action = function(action, param){
					if(action == 'save') {
						$scope.save();
					} else if(action == 'create') {
						$scope.create();
					} else {
						$scope.$parent.action(action, param);
					}
				};
				
				$scope.select = function(object) {
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
			}			
		};
	});		

	
	/***********************************/
	/** Mobile Layout Directive			 **/
	/***********************************/

	module.directive('rbMobileLayout', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $compile) {
				$scope.pages = [];
				$scope.topPage = null;
				$scope.pageStack = [];
				$scope.rootPageName = $attrs.rbRootPage;
				
				$scope.registerPage = function(pageScope) {
					//$scope.pages.push(pageScope);
					pageScope.element.addClass('rb-mobile-page-out');
					if($scope.pageStack.length == 0  &&  pageScope.name == $scope.rootPageName) {
						$scope.pushPage(pageScope);
					}
				}
				
				$scope.pushPage = function(pageScope) {
					$scope.pageStack.push(pageScope);
					$scope.topPage = pageScope;
					pageScope.stackIndex = $scope.pageStack.length;
					pageScope.element.removeClass('rb-mobile-page-out');
					pageScope.element.addClass('rb-mobile-page-in');
				}
				
				$scope.popPage = function() {
					var pageScope = $scope.pageStack.pop();
					$scope.topPage = $scope.pageStack[$scope.pageStack.length - 1];
					pageScope.stackIndex = 0;
					pageScope.element.removeClass('rb-mobile-page-in');
					pageScope.element.addClass('rb-mobile-page-out');
				}
				
				$scope.resolveIntent = function(target) {
					$scope.$broadcast('intent', target);
				};
				
				$scope.action = function(action, param){
					if(action == 'back') {
						$scope.popPage($scope);
					} else {
						$scope.$parent.action(action, param);
					}
				}
				
				$scope.$on('objectSelectedEmit', function($event, object){
					if(!$event.defaultPrevented) {
						$scope.$broadcast('objectSelected', object);
						$scope.$broadcast('intent', object.objectname);
						$event.defaultPrevented = true;
					}
				});
			}
		};
	});	
	 
	 
	/***********************************/
	/** Mobile Page directive			 **/
	/***********************************/

	 
	module.directive('rbMobilePage', function($compile) {
		return {
			restrict:'E',
			scope:true,
			controller: function($scope, $attrs, $http, $element, $compile) {
				$scope.element = $element;
				$scope.name = $attrs.rbPageName;
				$scope.stackIndex = 0;
				$scope.intent = $attrs.rbIntent;
				
				$scope.$on('intent', function($event, targetIntent){
					if($scope.intent != null  &&  $scope.intent == targetIntent) {
						$scope.pushPage($scope);
					}
				});		
				
				$scope.registerPage($scope);
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

	function rbProcessActionButtonController($scope, $attrs, $http, $element, $compile, $mdDialog) {
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
				clickOutsideToClose: true
			};
			$mdDialog.show(config).then(function(rez) {$scope.timepicker.mdPanelRef = rez;});
		}
		
		$scope.processAction = function(action) {
			$mdDialog.hide();
			$http.post("../../rbpm", {action:'processaction', processaction:action, pid:$scope.notification.pid})
			.success(function(response) {
				if(response.rbobjectupdate != null) {
					for(var i = 0; i < response.rbobjectupdate.length; i++) {
						var obj = findExistingObject(response.rbobjectupdate[i].objectname, response.rbobjectupdate[i].uid);
						if(obj != null)
							obj.refresh($http);
					}
				}
			})
			.error(function(error, status) {
				alert(error.error);
			});	
		}
	}			

	

