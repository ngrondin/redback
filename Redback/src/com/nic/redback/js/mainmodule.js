	var regexIso8601 = /^(\d{4}|\+\d{6})(?:-(\d{2})(?:-(\d{2})(?:T(\d{2}):(\d{2}):(\d{2})\.(\d{1,})(Z|([\-+])(\d{2}):(\d{2}))?)?)?)?$/;

	function convertDateStringsToDates(input) {
		// Ignore things that aren't objects.
		if (typeof input !== "object") return input;

		for (var key in input) {
			if (!input.hasOwnProperty(key)) continue;

			var value = input[key];
			var match;
			// Check for string properties which look like dates.
			if (typeof value === "string" && (match = value.match(regexIso8601))) {
				var milliseconds = Date.parse(match[0])
				if (!isNaN(milliseconds)) {
					input[key] = new Date(milliseconds);
				}
			} else if (typeof value === "object") {
				// Recurse into object
				convertDateStringsToDates(value);
			}
		}
	};
	
	function getFilterFromRelationship(object, relationship)
	{
		var filter = {};
		for (var key in relationship) {
			var value = relationship[key];
			if(value.startsWith("{{") &&  value.endsWith("}}")) {
				var parentEval = 'object.' + value.substring(2, value.length - 2);
				value = eval(parentEval);
			}
			filter[key] = value;
		}
		return filter;
	};
	
	var mainModule = angular.module("mainmodule", ['ngMaterial']);	
	
	
	
	mainModule.controller('form', function accountsCtl($scope,$attrs,$http) {
		$scope.objectName = $attrs.rbObject;
		$scope.object = null;
		$scope.dynamicSearchText = "";
		
		$scope.setObject = function(object) {
			$scope.object = object;
		}

		
		$scope.loadRelatedObjectList = function(attributeName)
		{
			var relationship = $scope.object.validation[attributeName].relatedobject.relationship;
			var relatedObjectName = $scope.object.validation[attributeName].relatedobject.name;
			var filter = getFilterFromRelationship($scope.object, relationship)
			filter._any = '*' + $scope.dynamicSearchText + '*';

			var req = {action:"list", object:relatedObjectName, filter:filter};
			return $http.post("../../rbos", req)
				.then(function(response) {
					return response.data.list;
				});
		};
		
		$scope.relatedObjectHasChanged = function(attributeName)
		{
			var newSelectedItem = $scope.object.related[attributeName];
			var newId = '';
			if(newSelectedItem != null)
				newId = $scope.object.related[attributeName].id;
			if($scope.object.data[attributeName] != newId)
			{
				$scope.object.data[attributeName] = newId;
			}
		}
		
		$scope.$on('ObjectSelected', function($event, object){
			if(object.objectname == $scope.objectName)
				$scope.setObject(object);
		});

		
		$scope.save = function() {
			var req = {action:"update", object:$scope.objectName, id:$scope.object.id, data:$scope.object.data, options:{addrelated:"true"}};
			$http.post("../../rbos", req)
			.success(function(response) {
				convertDateStringsToDates(response);
				$scope.object = response;
			})
			.error(function(error, status) {
				alert('save error');
			});
		}		
	 });
	 
	 
	 
	mainModule.controller('list', function accountsCtl($scope,$attrs,$http) {
		$scope.list = null;
		$scope.objectName = $attrs.rbObject;
		if($attrs.rbMaster != null)
			$scope.masterConfig = JSON.parse($attrs.rbMaster.replace(/'/g, '"'));
		else
			$scope.masterConfig = null;
			
		if($attrs.rbInitialFilter != null)
			$scope.filter = JSON.parse($attrs.rbInitialFilter.replace(/'/g, '"'));
		else
			$scope.filter == null;

		$scope.load = function() {
			if($scope.filter != null) {
				var req = {action:"list", object:$scope.objectName, filter:$scope.filter, options:{addrelated:"true"}};
				$http.post("../../rbos", req)
				.success(function(response) {
					convertDateStringsToDates(response);
					$scope.list = response.list;
				})
				.error(function(error, status) {
					alert('load error');
				});
			}
		}
		
		$scope.selectItem = function(object) {
			$scope.$emit('ObjectSelectedEmit', object);
		}
		
		$scope.$on('ObjectSelected', function($event, object){
			if($scope.masterConfig != null && object.objectname == $scope.masterConfig.objectname)
			{
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