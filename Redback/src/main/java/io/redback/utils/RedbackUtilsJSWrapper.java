package io.redback.utils;

import io.firebus.utils.DataMap;
import io.redback.services.impl.RedbackUIServer;

public class RedbackUtilsJSWrapper 
{
	public RedbackUtilsJSWrapper() {
	}
	

	public String convertDataMapToAttributeString(DataMap map) {
		return StringUtils.convertJSONToAttributeString(map);
	}

	public DataMap convertFilterForClient(DataMap map) {
		return RedbackUIServer.convertFilter(map);
	}
}
