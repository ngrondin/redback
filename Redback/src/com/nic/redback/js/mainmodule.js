	var regexIso8601 = /^(\d{4}|\+\d{6})(?:-(\d{2})(?:-(\d{2})(?:T(\d{2}):(\d{2}):(\d{2})\.(\d{1,})(Z|([\-+])(\d{2}):(\d{2}))?)?)?)?$/;
	
	var objectMaster = [];

	function processResponseJSONList(input) {
		var outputList = [];
		if (Array.isArray(input)) {
			for(var i = 0; i < input.length; i++) {
				outputList.push(processResponseJSONObject(input[i]));
			}
		}		
		return outputList;
	}
	
	function processResponseJSONObject(input) {
		var obj = null;
		if (typeof input == "object") {
			convertResponseJSONToObject(input);

			for (var i = 0; i < objectMaster.length; i++) {
				if(objectMaster[i].objectname == input.objectname  &&  objectMaster[i].uid == input.uid) {
					obj = objectMaster[i];
				}
			}
			
			if(obj != null) {
				for (var key in input.data) {
					var inputval = input.data[key];
					if(obj.data.hasOwnProperty(key)  &&  !(obj.hasOwnProperty("updatedattributes")  &&  obj.updatedattributes.includes(key))  &&  obj.data[key] != inputval) {
						obj.data[key] = inputval;
						if(obj.hasOwnProperty("related")  &&  obj.related.hasOwnProperty(key)) {
							var relatedobj = processResponseJSONObject(input.related[key]);
							obj.related[key] = relatedobj;
						}
					}
				}
			} else {
				obj = input;
				objectMaster.push(obj);
				if(obj.hasOwnProperty("related")) {
					for (var key in obj.related) {
						var relatedObj = processResponseJSONObject(obj.related[key]);
						obj.related[key] = relatedObj;
					}
				}
			}
		}
		return obj;
	};
	
	function convertResponseJSONToObject(input) {
		if (typeof input == "object") {
			for (var key in input) {
				if (!input.hasOwnProperty(key)) continue;

				var value = input[key];
				var match;
				if (typeof value === "string") {
					if(match = value.match(regexIso8601)) {
						var milliseconds = Date.parse(match[0])
						if (!isNaN(milliseconds)) {
							input[key] = new Date(milliseconds);
						}
					}
					else if(value == 'true')
						input[key] = true;
					else if(value == 'false')
						input[key] = false;
				} else if (typeof value === "object") {
					convertResponseJSONToObject(value);
				}
			}
		}
	};
			
	function getFilterFromRelationship(object, relationship)
	{
		var filter = {};
		for (var key in relationship) {
			var value = relationship[key];
			if(value.startsWith("{{") &&  value.endsWith("}}")) {
				var parentEval = value.replace('{{', 'object.data.').replace('}}', '').replace('object.data.uid', 'object.uid');
				value = eval(parentEval);
			}
			filter[key] = value;
		}
		return filter;
	};
	
	var mainModule = angular.module("mainmodule", ['ngMaterial']);	

	//mainModule.config(function($compileProvider) {  $compileProvider.preAssignBindingsEnabled(true);});
	
	
	
	mainModule.controller('form', function accountsCtl($scope,$attrs,$http) {
		$scope.objectName = $attrs.rbObject;
		$scope.object = null;
		$scope.dynamicSearchText = "";
		$scope.relatedObjectList = {};
		
		$scope.setObject = function(object) {
			$scope.object = object;
			for (var key in object.related) {
				$scope.relatedObjectList[key] = [object.related[key]];
				$scope.updatedAttributes = {};
			}
		}

		
		$scope.loadRelatedObjectList = function(attributeName, searchText)
		{
			var relationship = $scope.object.validation[attributeName].relatedobject.relationship;
			var relatedObjectName = $scope.object.validation[attributeName].relatedobject.name;
			var filter = getFilterFromRelationship($scope.object, relationship)
			if(searchText != null)
				filter._any = '*' + searchText + '*';

			var req = {action:"list", object:relatedObjectName, filter:filter};
			return $http.post("../../rbos", req)
				.then(function(response) {
					var responseList = processResponseJSONList(response.data.list);
					if(!$scope.relatedObjectList.hasOwnProperty(attributeName))
						$scope.relatedObjectList[attributeName] = [];
					$scope.relatedObjectList[attributeName] = responseList;
					return responseList;
				});
		};
		
		$scope.relatedObjectHasChanged = function(attributeName)
		{
			var newRelatedObject = $scope.object.related[attributeName];
			var newLinkValue = '';
			if(newRelatedObject != null)
			{
				var relatedObjectLinkAttribute = $scope.object.validation[attributeName].relatedobject.linkattribute;
				if(relatedObjectLinkAttribute == 'uid')
					newLinkValue = $scope.object.related[attributeName].uid;
				else
					newLinkValue = $scope.object.related[attributeName].data[relatedObjectLinkAttribute];
			}
			if($scope.object.data[attributeName] != newLinkValue)
			{
				$scope.object.data[attributeName] = newLinkValue;
				$scope.attributeHasChanged(attributeName);
			}
		}

		
		$scope.attributeHasChanged = function(attributeName)
		{
			if (!$scope.object.hasOwnProperty('updatedattributes')) 
				$scope.object.updatedattributes = [];
			if(!$scope.object.updatedattributes.includes(attributeName))
				$scope.object.updatedattributes.push(attributeName);
		}
		
		
		$scope.$on('ObjectSelected', function($event, object){
			if(object.objectname == $scope.objectName)
				$scope.setObject(object);
		});

		

		$scope.save = function(){
			if($scope.object != null) {
				var req = {action:"update", object:$scope.objectName, uid:$scope.object.uid, data:{}, options:{addrelated:"true", addvalidation:"true"}};
				for(i = 0; i < $scope.object.updatedattributes.length; i++)
				{
					var attributeName = $scope.object.updatedattributes[i];
					req.data[attributeName] = $scope.object.data[attributeName];
				}
				$http.post("../../rbos", req)
				.success(function(response) {
					var responseObject = processResponseJSONObject(response);
					$scope.object.data = responseObject.data;
					$scope.object.related = responseObject.related;
					$scope.object.validation = responseObject.validation;
					$scope.object.updatedattributes = [];
					$scope.setObject($scope.object);
				})
				.error(function(error, status) {
					alert('save error');
				});
			}
		};		


		$scope.create = function(){
			var req = {action:"create", object:$scope.objectName, options:{addrelated:"true", addvalidation:"true"}};
			$http.post("../../rbos", req)
			.success(function(response) {
				var responseObject = processResponseJSONObject(response);
				$scope.setObject(responseObject);
				$scope.$emit('refresh', $scope.objectName);
			})
			.error(function(error, status) {
				alert('create error');
			});
		};		

		$scope.objectfunction = function(functionName){
			var req = {action:"execute", object:$scope.objectName, uid:$scope.object.uid, "function":functionName};
			$http.post("../../rbos", req)
			.success(function(response) {
				var responseObject = processResponseJSONObject(response);
				$scope.setObject(responseObject);
				$scope.$emit('refresh', $scope.objectName);
			})
			.error(function(error, status) {
				alert('execute error');
			});
		};	
	 });
	 


	 
	 
	mainModule.controller('list', function accountsCtl($scope,$attrs,$http) {
		$scope.list = null;
		$scope.objectName = $attrs.rbObject;
		$scope.searchText = "";

		if($attrs.rbMaster != null)
			$scope.masterConfig = JSON.parse($attrs.rbMaster.replace(/'/g, '"'));
		else
			$scope.masterConfig = null;
			
		if($attrs.rbInitialFilter != null)
			$scope.filter = JSON.parse($attrs.rbInitialFilter.replace(/'/g, '"'));
		else
			$scope.filter == null;


		$scope.search = function(searchText) {
			if($scope.filter == null)
				$scope.filter = {};
			$scope.filter.$multi = '*' + $scope.searchText + '*';
			$scope.load();
		}



		$scope.load = function() {
			if($scope.filter != null) {
				var req = {action:"list", object:$scope.objectName, filter:$scope.filter, options:{addrelated:"true", addvalidation:"true"}};
				$http.post("../../rbos", req)
				.success(function(response) {
					var responseList = processResponseJSONList(response.list);
					$scope.list = responseList;
				})
				.error(function(error, status) {
					alert('load error');
				});
			}
		}


		
		$scope.$on('ObjectSelected', function($event, object){
			if($scope.masterConfig != null && object.objectname == $scope.masterConfig.objectname)
			{
				$scope.searchText = "";
				$scope.filter = getFilterFromRelationship(object, $scope.masterConfig.relationship)
				$scope.load();
			}
		});

		$scope.load();
	 });	 



	 
	mainModule.controller('layout', function accountsCtl($scope,$attrs,$http) {

		$scope.$on('ObjectSelectedEmit', function($event, object){
			$scope.$broadcast('ObjectSelected', object);
		});
	 });	 