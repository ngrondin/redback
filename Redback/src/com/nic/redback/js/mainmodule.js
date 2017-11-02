	var regexIso8601 = /^(\d{4}|\+\d{6})(?:-(\d{2})(?:-(\d{2})(?:T(\d{2}):(\d{2}):(\d{2})\.(\d{1,})(Z|([\-+])(\d{2}):(\d{2}))?)?)?)?$/;

	function processResponseJSON(input) {
		// Ignore things that aren't objects.
		if (typeof input !== "object") return input;

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
				processResponseJSON(value);
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
					processResponseJSON(response);
					if(!$scope.relatedObjectList.hasOwnProperty(attributeName))
						$scope.relatedObjectList[attributeName] = [];
					$scope.relatedObjectList[attributeName] = response.data.list;
					return response.data.list;
				});
		};
		
		$scope.relatedObjectHasChanged = function(attributeName)
		{
			var newRelatedObject = $scope.object.related[attributeName];
			var newLinkValue = '';
			if(newRelatedObject != null)
			{
				var relatedObjectValueAttribute = $scope.object.validation[attributeName].relatedobject.valueattribute;
				if(relatedObjectValueAttribute == 'uid')
					newLinkValue = $scope.object.related[attributeName].uid;
				else
					newLinkValue = $scope.object.related[attributeName].data[relatedObjectValueAttribute];
			}
			if($scope.object.data[attributeName] != newLinkValue)
			{
				$scope.object.data[attributeName] = newLinkValue;
			}
		}

		
		$scope.$on('ObjectSelected', function($event, object){
			if(object.objectname == $scope.objectName)
				$scope.setObject(object);
		});

		

		$scope.save = function(){
			if($scope.object != null) {
				var req = {action:"update", object:$scope.objectName, uid:$scope.object.uid, data:$scope.object.data, options:{addrelated:"true", addvalidation:"true"}};
				$http.post("../../rbos", req)
				.success(function(response) {
					processResponseJSON(response);
					$scope.setObject(response);
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
				processResponseJSON(response);
				$scope.setObject(response);
				$scope.$emit('refresh', $scope.objectName);
			})
			.error(function(error, status) {
				alert('create error');
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
			$scope.filter._any = '*' + $scope.searchText + '*';
			$scope.load();
		}



		$scope.load = function() {
			if($scope.filter != null) {
				var req = {action:"list", object:$scope.objectName, filter:$scope.filter, options:{addrelated:"true", addvalidation:"true"}};
				$http.post("../../rbos", req)
				.success(function(response) {
					processResponseJSON(response);
					$scope.list = response.list;
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