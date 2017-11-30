	var regexIso8601 = /^(\d{4}|\+\d{6})(?:-(\d{2})(?:-(\d{2})(?:T(\d{2}):(\d{2}):(\d{2})\.(\d{1,})(Z|([\-+])(\d{2}):(\d{2}))?)?)?)?$/;
	
	var objectMaster = [];

	function processResponseJSON(input) {
		if (typeof input == "object") {
			if(input.hasOwnProperty('list')) {
				var outputList = [];
				for(var i = 0; i < input.list.length; i++) 
					outputList.push(processResponseJSONObject(input.list[i]));
				return outputList;
			} else if (input.hasOwnProperty('objectname') && input.hasOwnProperty('uid')) {
				return processResponseJSONObject(input);
			} else if (input.hasOwnProperty('scripterror')) {
				alert('Script Error: ' + input.scripterror);
			} else if (input.hasOwnProperty('generalerror')) {
				alert('General Error: ' + input.generalerror);
			}
		} 
		return null;
	}
	
	function processResponseJSONObject(input) {
		var obj = null;
		if (typeof input == "object") {
			if(input.hasOwnProperty('objectname') && input.hasOwnProperty('uid')) {
			
				for (var key in input.data) {
					var match;
					if (typeof input.data[key] === "string") {
						if(match = input.data[key].match(regexIso8601)) {
							var milliseconds = Date.parse(match[0])
							if (!isNaN(milliseconds)) {
								input.data[key] = new Date(milliseconds);
							}
						}
					}
					if(input.hasOwnProperty('validation')  &&  input.validation[key].hasOwnProperty('editable')) {
						if(input.validation[key].editable == 'true')
							input.validation[key].editable = true;
						else 
							input.validation[key].editable = false;
					}
				}

				for (var i = 0; i < objectMaster.length; i++) {
					if(objectMaster[i].objectname == input.objectname  &&  objectMaster[i].uid == input.uid) {
						obj = objectMaster[i];
					}
				}
				
				if(obj != null) {
					for (var key in input.data) {
						if(obj.data.hasOwnProperty(key)) {
							if(obj.hasOwnProperty("updatedattributes")  &&  obj.updatedattributes.includes(key)) {
								if(obj.data[key] == input.data[key]) {
									obj.updatedattributes.pop(key);
								}
							} else {
								if(obj.data[key] != input.data[key]) {
									obj.data[key] = input.data[key];
									obj.validation[key] = input.validation[key];
									if(obj.hasOwnProperty("related")  &&  obj.related.hasOwnProperty(key)) {
										obj.related[key] = processResponseJSONObject(input.related[key]);
									}
								}
							}						
						}
					}
				} else {
					obj = convertToRBObject(input);
					objectMaster.push(obj);
					if(obj.hasOwnProperty("related")) {
						for (var key in obj.related) {
							obj.related[key] = processResponseJSONObject(obj.related[key]);
						}
					}
				}
			} 
		}
		return obj;
	};
	
			
	function getFilterFromRelationship(object, relationship)
	{
		var filter = {};
		if(object != null  &&  relationship != null) {
			for (var key in relationship) {
				var value = relationship[key];
				if(value.startsWith("{{") &&  value.endsWith("}}")) {
					var parentEval = value.replace('{{', 'object.data.').replace('}}', '').replace('object.data.uid', 'object.uid');
					value = eval(parentEval);
				}
				filter[key] = value;
			}
		}
		return filter;
	};
	
	function navigate(view)
	{
		mainview.src = 'a';
	};
	

	