	var module = angular.module("configuratormodule", ['ngMaterial', 'ui.codemirror']);

	module.controller('processdesigner', function processdesigner($scope,$attrs,$http,$element) {
		$scope.nodeTypes = [
			{id:"interaction", name:"Interaction"},
			{id:"script", name:"Script"},
			{id:"action", name:"Process Action"},
			{id:"rbobjectupdate", name:"Object Update"},
			{id:"rbobjectexecute", name:"Object Execute"}
		];
		$scope.assigneeTypes = [
			{id:"user", name:"User"},
			{id:"group", name:"Group"},
			{id:"process", name:"Process"},
			{id:"rules", name:"Rules"}
		];
		$scope.notificationMethods = [
			{id:"email", name:"Email"},
			{id:"rbprocessnotification", name:"Process Notification"}
		]
		
		$scope.list = [];
		$scope.config = {};
		$scope.selectedNode = null;

		
		$scope.load = function() {
			var req = {action:"listprocesses"};
			$http.post("../../rbcf", req)
			.success(function(response) {
				$scope.list = response.result;
			})
			.error(function(error, status) {
				alert('load error');
			});
		}
		
		$scope.selectProcess = function(listitem) {
			var req = {action:"getprocess", name:listitem.name, version:listitem.version};
			$http.post("../../rbcf", req)
			.success(function(response) {
				$scope.config = response;
			})
			.error(function(error, status) {
				alert('load error');
			});	
		}
		
		$scope.save = function() {
			var req = {action:"updateprocess", config:$scope.config};
			$http.post("../../rbcf", req)
			.success(function(response) {
				alert("saved");
			})
			.error(function(error, status) {
				alert('save error');
			});	
		}
		
		$scope.addNode = function() {
			if($scope.config != null) {
				var nextId = 0;
				for(var i = 0; i < $scope.config.nodes.length; i++) {
					if($scope.config.nodes[i].id > nextId) nextId = $scope.config.nodes[i].id;
				}
				nextId += 1;
				$scope.config.nodes.push({id:nextId + '', type:null, name:null, position:{x:0, y:0}});
			}
		}

		$scope.removeNode = function() {
			if($scope.config != null  &&  $scope.selectedNode != null) {
				var index = -1;
				for(var i = 0; i < $scope.config.nodes.length; i++)
					if($scope.config.nodes[i] == $scope.selectedNode)
						index = i;
				if(i > -1) {
					$scope.config.nodes.splice(index, 1);
					$scope.$broadcast('drawConnectors');
				}
			}
		}

		$scope.addAssignee = function() {
			if($scope.config != null  &&  $scope.selectedNode != null) {
				if($scope.selectedNode.assignees == null)
					$scope.selectedNode.assignees = [];
				$scope.selectedNode.assignees.push({type:null, id:null});
			}
		}

		$scope.removeAssignee = function(assignee) {
			if($scope.config != null  &&  $scope.selectedNode != null) {
				$scope.selectedNode.assignees.pop(assignee);
			}
		}

		$scope.addAction = function() {
			if($scope.config != null  &&  $scope.selectedNode != null) {
				if($scope.selectedNode.actions == null)
					$scope.selectedNode.actions = [];
				$scope.selectedNode.actions.push({action:null, description:null, nextnode:null});
			}
		}

		$scope.removeAction = function(action) {
			if($scope.config != null  &&  $scope.selectedNode != null) {
				$scope.selectedNode.actions.pop(action);
			}
		}
		
		$scope.load();
	});
	
	
	module.controller('processcanvas', function processcanvas($scope,$attrs,$http,$element) {
		$scope.element = $element;
		$scope.canvasWidth = 0;
		$scope.canvasHeight = 0;
		$scope.dragging = false;
		$scope.draggingNode = null;
		$scope.dragOffset = null;
		$scope.nodeDivs = {};
		
		$scope.canvas = document.getElementById("canvas");
		$scope.ctx = canvas.getContext("2d");
		
		$scope.getNode = function(id){
			for(var i = 0; i < $scope.config.nodes.length; i++) 
				if($scope.config.nodes[i].id == id)
					return $scope.config.nodes[i];
		}
		
		$scope.drawConnectors = function() {
			$scope.ctx.clearRect(0, 0, $scope.canvas.width, $scope.canvas.height);
			$scope.ctx.beginPath();
			for(var i = 0; i < $scope.config.nodes.length; i++) {
				$scope.drawNodeConnectors($scope.config.nodes[i]);
			}
		}
		
		$scope.drawNodeConnectors = function(node) {
			if(node.type == 'interaction') {
				if(node.actions != null) {
					for(var i = 0; i < node.actions.length; i++) {
						if(node.actions[i].nextnode != null) {
							var toNode = $scope.getNode(node.actions[i].nextnode);
							if(toNode != null)
								$scope.drawConnector(node,toNode, node.actions[i].action);
						}
					}
				}
			} else {
				if(node.nextnode != null) {
					var toNode = $scope.getNode(node.nextnode);
					if(toNode != null)
						$scope.drawConnector(node, toNode, '');
				}
			}
		}
		
		$scope.drawConnector = function(fromNode, toNode, label) {
			var startX = fromNode.position.x + $scope.nodeDivs[fromNode.id][0].offsetWidth;
			var startY = fromNode.position.y + ($scope.nodeDivs[fromNode.id][0].offsetHeight / 2);
			var endX = toNode.position.x;
			var endY = toNode.position.y + ($scope.nodeDivs[toNode.id][0].offsetHeight / 2);
			if(startX < endX) {
				var midX = startX + 20; 
				if(endX < startX + 40) midX = (startX + endX) / 2;
				$scope.ctx.moveTo(startX, startY);
				$scope.ctx.lineTo(midX, startY);
				$scope.ctx.stroke();			
				$scope.ctx.lineTo(midX, endY);
				$scope.ctx.stroke();			
				$scope.ctx.lineTo(endX, endY);
				$scope.ctx.stroke();	
				$scope.ctx.font = "12px Arial";
				$scope.ctx.fillText(label,midX + 3,endY - 2);				
			} else {
				var midX1 = startX + 20;
				var midX2 = endX - 20;
				var midY = startY - 40; //(startY + endY) / 2;
				if(endY < startY) midY = endY - 40;
				$scope.ctx.moveTo(startX, startY);
				$scope.ctx.lineTo(midX1, startY);
				$scope.ctx.stroke();			
				$scope.ctx.lineTo(midX1, midY);
				$scope.ctx.stroke();			
				$scope.ctx.lineTo(midX2, midY);
				$scope.ctx.stroke();			
				$scope.ctx.lineTo(midX2, endY);
				$scope.ctx.stroke();			
				$scope.ctx.lineTo(endX, endY);
				$scope.ctx.stroke();			
				$scope.ctx.font = "12px Arial";
				$scope.ctx.fillText(label,midX1 - 30,midY - 2);				
			}
		}
		
		$scope.mouseup = function(event) {
			$scope.dragging = false;
			$scope.draggingNode = null;
			$scope.dragOffset = null;
		}
		
		$scope.mousemove = function(event) {
			if($scope.dragging) {
				$scope.draggingNode.position.x += (event.screenX - $scope.dragOffset.x);
				$scope.draggingNode.position.y += (event.screenY - $scope.dragOffset.y);
				$scope.dragOffset.x = event.screenX;
				$scope.dragOffset.y = event.screenY;
				$scope.updateCanvasSize();
				$scope.drawConnectors();
			}
		}
		
		$scope.updateCanvasSize = function() {
			var h  = 0;
			var w = 0;
			for(var i = 0; i < $scope.config.nodes.length; i++) {
				var nodeId = $scope.config.nodes[i].id;
				x = $scope.config.nodes[i].position.x + $scope.nodeDivs[nodeId][0].offsetWidth;
				y = $scope.config.nodes[i].position.y + $scope.nodeDivs[nodeId][0].offsetHeight;
				if(x > w) w = x;
				if(y > h) h = y;
			}	
			w += 40;
			h += 40;
			$scope.canvas.height = h;
			$scope.canvas.width = w;
			$scope.element.css('height', h + 'px');
			$scope.element.css('width', w + 'px');
		}		
		
		$scope.$on('drawConnectors', function($event) {
			$scope.drawConnectors();
		});
		
	});

	
	module.controller('processnode', function processnode($scope,$attrs,$http,$element) {
		$scope.element = $element;
		$scope.$parent.$parent.nodeDivs[$scope.node.id] = $element;
		
		$scope.mousedown = function(event) {
			$scope.$parent.$parent.dragging = true;
			$scope.$parent.$parent.draggingNode = $scope.node;
			$scope.$parent.$parent.dragOffset = {x: event.screenX, y: event.screenY};
			$scope.$parent.$parent.$parent.selectedNode = $scope.node;
		}
		
		if($scope.$last) {
			$scope.$parent.$parent.updateCanvasSize();
			$scope.$parent.$parent.drawConnectors();
		}
	});
	
	/****************************************************/
	
	module.controller('objectdesigner', function objectdesigner($scope,$attrs,$http,$element,$timeout) {
		$scope.list = [];
		$scope.config = {};
		$scope.selectedAttribute = null;
		$scope.listfilter = [];
		$scope.selectedView = null;
		$scope.scripts = [];
		$scope.saveok = false;

		
		$scope.load = function() {
			var req = {action:"listobjects"};
			$http.post("../../rbcf", req)
			.success(function(response) {
				$scope.list = response.result;
			})
			.error(function(error, status) {
				alert('load error');
			});
		}
		
		$scope.save = function() {
			if($scope.config != null) {
				$scope.cleanupConfig();
				var req = {action:"updateobject", config:$scope.config};
				$http.post("../../rbcf", req)
				.success(function(response) {
					$scope.saveok = true;
					$timeout(function(){$scope.saveok = false;}, 2000);
				})
				.error(function(error, status) {
					alert('save error');
				});	
			}
		}
		
		$scope.selectObject = function(listitem) {
			$scope.selectedAttribute = null;		
			$scope.listfilter = [];
			$scope.scripts = [];
			var req = {action:"getobject", _id:listitem._id};
			$http.post("../../rbcf", req)
			.success(function(response) {
				$scope.config = response;
				$scope.readScripts();
			})
			.error(function(error, status) {
				alert('load error');
			});	
		}
		
		$scope.selectAttribute = function(listitem) {
			$scope.selectedAttribute = listitem;
			$scope.readListFilter();
		}
		
		$scope.readListFilter = function() {
			$scope.listfilter = [];
			if($scope.selectedAttribute != null  &&  $scope.selectedAttribute.relatedobject != null  &&  $scope.selectedAttribute.relatedobject.listfilter != null) {
				for(var key in $scope.selectedAttribute.relatedobject.listfilter)
					$scope.listfilter.push({name:key, value:$scope.selectedAttribute.relatedobject.listfilter[key]});
			}
		}

		$scope.readScripts = function() {
			$scope.scripts = [];
			if($scope.config != null  &&  $scope.config.scripts != null) {
				for(var key in $scope.config.scripts)
					$scope.scripts.push({name:key, value:$scope.config.scripts[key]});
			} 
		}

		$scope.createObject = function() {
			var req = {action:"createobject"};
			$http.post("../../rbcf", req)
			.success(function(response) {
				$scope.config = response;
				$scope.list.push(response);
			})
			.error(function(error, status) {
				alert('load error');
			});	
		}

		$scope.addAttribute = function() {
			if($scope.config != null) {
				if($scope.config.attributes == null)
					$scope.config.attributes = [];
				$scope.config.attributes.push({ name:null});
			}
		}

		$scope.removeAttribute = function() {
			if($scope.config != null  &&  $scope.selectedAttribute != null) {
				var index = -1;
				for(var i = 0; i < $scope.config.attributes.length; i++)
					if($scope.config.attributes[i] == $scope.selectedAttribute)
						index = i;
				if(i > -1) {
					$scope.config.attributes.splice(index, 1);
				}
			}
		}

		$scope.addRelatedFilter = function() {
			if($scope.config != null  &&  $scope.selectedAttribute != null  &&  $scope.selectedAttribute.relatedobject != null) {
				$scope.listfilter.push({name:'', value:''});
			}
		}

		$scope.removeRelatedFilter = function(filter) {
			if($scope.config != null  &&  $scope.selectedAttribute != null  &&  $scope.selectedAttribute.relatedobject != null) {
				var index = -1;
				for(var i = 0; i < $scope.listfilter.length; i++)
					if($scope.listfilter[i] == filter)
						index = i;
				if(i > -1) {
					$scope.listfilter.splice(index, 1);
				}
				$scope.writeListFilter();
			}
		}
		
		$scope.writeListFilter = function() {
			if($scope.selectedAttribute != null  &&  $scope.selectedAttribute.relatedobject != null) {
				$scope.selectedAttribute.relatedobject.listfilter = {};
				for(var i = 0; i < $scope.listfilter.length; i++)
					$scope.selectedAttribute.relatedobject.listfilter[$scope.listfilter[i].name] = $scope.listfilter[i].value; 
			}			
		}

		$scope.addScript = function() {
			if($scope.config != null) {
				if($scope.config.scripts == null) {
					$scope.config.scripts = {};
					$scope.scripts = [];
				}
				$scope.scripts.push({name:'', value:''});
			}
		}
		
		$scope.removeScript = function(scriptitem) {
			if($scope.config != null  &&  $scope.scripts != null) {
				var index = -1;
				for(var i = 0; i < $scope.scripts.length; i++)
					if($scope.scripts[i] == scriptitem)
						index = i;
				if(i > -1) {
					$scope.scripts.splice(index, 1);
				}
				$scope.writeScripts();
			}
		}

		$scope.writeScripts = function() {
			if($scope.config != null  &&  $scope.config.scripts != null) {
				$scope.config.scripts = {};
				for(var i = 0; i < $scope.scripts.length; i++)
					$scope.config.scripts[$scope.scripts[i].name] = $scope.scripts[i].value; 
			}			
		}

		$scope.cleanupConfig = function() {
			if($scope.config != null) {
				if($scope.config.attributes != null) {
					for(var i = 0; i < $scope.config.attributes.length; i++) {
						var attribute = $scope.config.attributes[i];
						if(attribute.relatedobject != null  &&  (attribute.relatedobject.name == ''  ||  attribute.relatedobject.name == null))
							delete attribute.relatedobject;
						if(attribute.scripts != null) {
							for(var key in attribute.scripts)
								if(attribute.scripts[key] == ''  ||  attribute.scripts[key] == null)
									delete attribute.scripts[key];
							if(Object.getOwnPropertyNames(attribute.scripts).length === 0)
								delete attribute.scripts;
						}
					}
				}
				if($scope.config.scripts != null) {
					if(Object.getOwnPropertyNames($scope.config.scripts).length === 0)
						delete $scope.config.scripts;
				}
			}
		}

		$scope.load();
	});	