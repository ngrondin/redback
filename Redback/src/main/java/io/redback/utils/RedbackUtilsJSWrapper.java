package io.redback.utils;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataMap;
import io.redback.services.impl.RedbackUIServer;

public class RedbackUtilsJSWrapper 
{
	public RedbackUtilsJSWrapper() {
	}
	
	public String convertDataEntityToAttributeString(DataEntity entity) {
		return StringUtils.convertDataEntityToAttributeString(entity);
	}
	
	public String convertDataMapToAttributeString(DataMap map) {
		return StringUtils.convertDataEntityToAttributeString(map);
	}

	public DataMap convertFilterForClient(DataMap map) {
		return RedbackUIServer.convertFilter(map);
	}
}
