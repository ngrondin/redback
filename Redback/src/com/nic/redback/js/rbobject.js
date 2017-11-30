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
			var req = {action:"update", object:this.objectname, uid:this.uid, data:{}, options:{addrelated:"true", addvalidation:"true"}};
			for(i = 0; i < this.updatedattributes.length; i++) {
				var attributeName = this.updatedattributes[i];
				req.data[attributeName] = this.data[attributeName];
			}
			return req;
		};
		
		
		obj.getRelatedObjectListRequestMessage = function(attributeName, searchText) {
			var relationship = this.validation[attributeName].relatedobject.relationship;
			var relatedObjectName = this.validation[attributeName].relatedobject.name;
			var filter = getFilterFromRelationship(this, relationship)
			if(searchText != null)
				filter.$multi = '*' + searchText + '*';
			var req = {action:"list", object:relatedObjectName, filter:filter};
			return req;
		}
		
		obj.attributeHasChanged = function(attributeName) {
			if(this.data.hasOwnProperty(attributeName)) {
				if(!this.updatedattributes.includes(attributeName))
					this.updatedattributes.push(attributeName);
			}
		}
		
		
		obj.relatedObjectHasChanged = function(attributeName) {
			if(this.data.hasOwnProperty(attributeName)) {
				var newRelatedObject = this.related[attributeName];
				var newLinkValue = '';
				if(newRelatedObject != null)
				{
					var relatedObjectLinkAttribute = this.validation[attributeName].relatedobject.linkattribute;
					if(relatedObjectLinkAttribute == 'uid')
						newLinkValue = this.related[attributeName].uid;
					else
						newLinkValue = this.related[attributeName].data[relatedObjectLinkAttribute];
				}
				if(this.data[attributeName] != newLinkValue)
				{
					this.data[attributeName] = newLinkValue;
					this.attributeHasChanged(attributeName);
				}
			}
		}
		
		return obj;
	};