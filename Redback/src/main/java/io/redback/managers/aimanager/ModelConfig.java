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
		return config.getString("url");
	}
	
	public String getAPIKey() {
		return config.getString("apikey");
	}
}
