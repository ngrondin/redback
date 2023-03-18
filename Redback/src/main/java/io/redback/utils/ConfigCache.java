package io.redback.utils;

import io.firebus.data.DataMap;
import io.redback.client.ConfigClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class ConfigCache<T> {
	protected Cache<T> cache = new Cache<T>(300000);
	protected ConfigClient configClient;
	protected String service;
	protected String category;
	protected ConfigFactory<T> configFactory;
	
	public interface ConfigFactory<T> {
		public T createConfig(DataMap map) throws Exception ;
	}
	

	public ConfigCache(ConfigClient cc, String s, String cat, ConfigFactory<T> cf) 
	{
		configClient = cc;
		configFactory = cf;	
		service = s;
		category = cat;
	}
	
	private T createConfig(DataMap m) throws RedbackException {
		T config = null;
		if(configFactory != null) {
			try {
				config = configFactory.createConfig(m);
			} catch(Exception e) {
				throw new RedbackException("Error creating config object", e);
			}
		}
		return config;
	}
	
	private T getFromClient(Session session, String name, String domain) throws RedbackException
	{
		T config = null;
		String key = domain != null ? name + "." + domain : name;
		CacheEntry<T> ce = cache.getEntry(key);
		if(ce != null) {
			config = ce.get();
		} else {
			DataMap configMap = null;
			if(domain == null)
				configMap = configClient.getConfig(session, service, category, name);
			else
				configMap = configClient.getDomainConfig(session, service, category, name, domain);
			if(configMap != null) {
				config = createConfig(configMap);
			}
			cache.put(key, config);
		}
		return config;
	}

	public T get(Session session, String name) throws RedbackException
	{
		return get(session, name, null);
	}
	
	public T get(Session session, String name, String onlyInDomain) throws RedbackException
	{
		T config = null;
		if(onlyInDomain == null) {
			for(String domain : session.getUserProfile().getDomains()) {
				if(!domain.equals("*")) {
					config = getFromClient(session, name, domain);
					if(config != null) break;
				}
			} 
			
			if(config == null) {
				config = getFromClient(session, name, null);
			} 
		} else {
			config = getFromClient(session, name, onlyInDomain);
		}
		
		return config;
	}
	
	public void clear(String name, String domain) {
		String key = service + "." + category + "." + name;
		if(domain != null)
			key = key + "." + domain;
		cache.clear(key);
	}
	
	public void clear() {
		cache.clear();
	}
}
