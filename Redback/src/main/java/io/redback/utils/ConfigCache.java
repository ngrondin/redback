package io.redback.utils;

import io.firebus.data.DataMap;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidConfigException;
import io.redback.security.Session;

public class ConfigCache<T> {
	protected Cache<T> cache = new Cache<T>(300000);
	protected ConfigurationClient configClient;
	protected DataClient dataClient;
	protected CollectionConfig collection;
	protected String service;
	protected String category;
	protected ConfigFactory<T> configFactory;
	
	public interface ConfigFactory<T> {
		public T createConfig(DataMap map) throws Exception ;
	}
	

	public ConfigCache(ConfigurationClient cc, DataClient dc, String s, String cat, CollectionConfig col, ConfigFactory<T> cf) 
	{
		configClient = cc;
		dataClient = dc;
		collection = col;
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
	
	private T getForDomain(String name, String domain) throws RedbackException
	{
		T config = null;
		String key = name + "." + domain;
		CacheEntry<T> ce = cache.getEntry(key);
		if(ce != null) {
			config = ce.get();
		} else {
			DataMap reqkey = new DataMap("domain", domain, "name", name);
			DataMap res = dataClient.getData(collection.getName(), collection.convertObjectToSpecific(reqkey), null);
			if(res.containsKey("result") && res.getList("result").size() > 0) {
				config = createConfig(collection.convertObjectToCanonical(res.getList("result").getObject(0)));
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
			if(dataClient != null && collection != null) {
				for(String domain : session.getUserProfile().getDomains()) {
					if(!domain.equals("*")) {
						config = getForDomain(name, domain);
						if(config != null) break;
					}
				} 
			}
			
			if(config == null) {
				CacheEntry<T> ce = cache.getEntry(name);
				if(ce != null) {
					config = ce.get();
				} else {
					DataMap req = new DataMap();
					req.put("action", "get");
					req.put("service", service);
					req.put("category", category);
					req.put("name", name);
					try {
						config = createConfig(configClient.getConfig(session, service, category, name));
					} catch(Exception e) { }
					cache.put(name, config);
				}
			} 
		} else {
			config = getForDomain(name, onlyInDomain);
		}
		
		if(config != null) {
			return config;
		} else {
			throw new RedbackInvalidConfigException("config " + name + " not found");
		}
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
