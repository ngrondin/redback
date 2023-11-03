package io.redback.managers.aimanager;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class ModelConfig {
	protected AIManager aiManager;
	protected DataMap config;
	
	public ModelConfig(AIManager aim, DataMap cfg) throws RedbackException
	{
		aiManager = aim;
		config = cfg;
	}
	
	public String getType() {
		return config.getString("type");
	}
	
	public String getUrl() {
		String url = config.getString("url");
		if(!url.startsWith("http") && aiManager.urlMap != null) {
			String val = aiManager.urlMap.getString(url);
			if(val != null) {
				url = val;
			}
		}
		return url;
	}
	
	public String getAPIKey() {
		return config.getString("apikey");
	}
}
