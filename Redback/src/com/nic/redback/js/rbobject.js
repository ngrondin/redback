	function convertToRBObject(input) {
		var obj = input;
		obj.updatedattributes = [];
		
		
		obj.isUpdated = function() {
			if(this.updatedattributes.length > 0)
				return true;
			else
				return false;
		};
		
		
		obj.getUpdateRequestMessage = function(http) {
			var req = {action:"update", object:this.objectname, uid:this.uid, data:{}, options:{addrelated:true, addvalidation:true}};
			for(i = 0; i < this.updatedattributes.length; i++) {
				var attributeName = this.updatedattributes[i];
				req.data[attributeName] = this.data[attributeName];
			}
			return req;
		};

		
		obj.attributeHasChanged = function(attributeName, $http) {
			if(this.data.hasOwnProperty(attributeName)) {
				if(this.related[attributeName] != null) {
					var newRelatedObject = this.related[attributeName];
					var newLinkValue = '';
					var relatedObjectLinkAttribute = this.validation[attributeName].related.link;
					if(relatedObjectLinkAttribute == 'uid')
						newLinkValue = this.related[attributeName].uid;
					else
						newLinkValue = this.related[attributeName].data[relatedObjectLinkAttribute];
					if(this.data[attributeName] != newLinkValue)
					{
						this.data[attributeName] = newLinkValue;
					}
				} 
				if(!this.updatedattributes.includes(attributeName))
					this.updatedattributes.push(attributeName);
				//if(this.validation[attributeName].updatescript)
					this.save($http);
			}
		}
		
		/*
		obj.dateChange = function(attrName, newValue) {
			if(!this.data[attrName].isSame(newValue)) {
				this.attributeHasChanged(attrName);
			}
		}*/
		
		obj.save = function($http) {
			if(this.isUpdated()) {
				$http.post("../../rbos", this.getUpdateRequestMessage())
				.success(function(response) {
					var responseObject = processResponseJSON(response);
				})
				.error(function(error, status) {
					alert('save error');
				});
				this.updatedattributes = [];
			}
		}
		
		return obj;
	};