package io.redback.utils;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.ConfigClient;
import io.redback.exceptions.RedbackConfigNotFoundException;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class ConfigCache<T> {
	protected Cache<T> cache;
	protected Cache<List<T>> listCache;
	protected ConfigClient configClient;
	protected String service;
	protected String category;
	protected ConfigFactory<T> configFactory;
	
	public interface ConfigFactory<T> {
		public T createConfig(DataMap map) throws Exception ;
	}
	

	public ConfigCache(ConfigClient cc, String s, String cat, long timeout, ConfigFactory<T> cf) 
	{
		configClient = cc;
		configFactory = cf;	
		service = s;
		category = cat;
		cache = new Cache<T>(timeout);
		listCache = new Cache<List<T>>(timeout);
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
	
	private T getFromCacheOrClient(Session session, String name, String domain) throws RedbackException
	{
		String key = domain != null ? name + "." + domain : name;
		CacheEntry<T> ce = cache.getEntry(key);
		if(ce != null) {
			return ce.get();
		} else {
			DataMap configMap = null;
			T config = null;
			if(domain == null)
				configMap = configClient.getConfig(session, service, category, name);
			else
				configMap = configClient.getDomainConfig(session, service, category, name, domain);
			if(configMap != null) {
				config = createConfig(configMap);
			}
			cache.put(key, config);
			return config;
		}
	}
	
	private List<T> listFromCacheOrClient(Session session, DataMap filter, boolean fromDomains) throws RedbackException
	{
		String listKey = (fromDomains ? "dom." : "") + filter.toString(true);
		CacheEntry<List<T>> listCE = listCache.getEntry(listKey);
		if(listCE != null) {
			return listCE.get();
		} else {
			List<T> list = new ArrayList<T>();
			DataMap res = null;
			if(!fromDomains)
				res = configClient.listConfigs(session, service, category, filter);
			else
				res = configClient.listDomainConfigs(session, service, category, filter);
			if(res != null && res.containsKey("result")) {
				DataList resList = res.getList("result");
				for(int i = 0; i < resList.size(); i++) {
					DataMap configMap = resList.getObject(i);
					String name = configMap.getString("name");
					String domain = configMap.getString("domain");
					String key = domain != null ? name + "." + domain : name;
					CacheEntry<T> ce = cache.getEntry(key);
					if(ce != null) {
						if(ce.get() != null)
							list.add(ce.get());
					} else {
						T config = createConfig(configMap);
						cache.put(key, config);
						list.add(config);
					}
				}
			} 
			listCache.put(listKey, list);
			return list;				
		}
	}

	public T get(Session session, String name) throws RedbackException
	{
		return get(session, name, true);
	}
	
	public T get(Session session, String name, boolean exceptionOnNotFound) throws RedbackException
	{
		T config = null;
		String domainLock = session.getDomainLock();
		if(domainLock != null) {
			if(session.getUserProfile().hasDomain(domainLock))
				config = getFromCacheOrClient(session, name, domainLock);
		} else if(session.getUserProfile().hasAllDomains()) {
			List<T> list = listFromCacheOrClient(session, new DataMap("name", name), true);
			if(list.size() > 0) 
				config = list.get(0);
		} else {
			for(String domain : session.getUserProfile().getDomains()) {
				config = getFromCacheOrClient(session, name, domain);
				if(config != null) 
					break;
			} 
		}

		if(config == null) {
			config = getFromCacheOrClient(session, name, null);
		} 
		
		if(config == null && exceptionOnNotFound)
			throw new RedbackConfigNotFoundException("Config not found for " + service + "." + category + "." + name);
		else
			return config;
	}
	
	public List<T> list(Session session, DataMap filter) throws RedbackException 
	{
		List<T> list = new ArrayList<T>();
		DataMap domFilter = (DataMap)filter.getCopy();
		String domainLock = session.getDomainLock();
		if(domainLock != null) {
			if(session.getUserProfile().hasDomain(domainLock))
				domFilter.put("domain", domainLock);
		} else if(session.getUserProfile().hasAllDomains()) {
			//don't restrict domains
		} else {
			DataList domList = new DataList();
			for(String domain : session.getUserProfile().getDomains())
				domList.add(domain);
			domFilter.put("domain", new DataMap("$in", domList));
		}
		list.addAll(listFromCacheOrClient(session, domFilter, true));
		
		list.addAll(listFromCacheOrClient(session, filter, false));
		return list;
	}
	
	public void clear(String name, String domain) {
		String key = domain != null ? name + "." + domain : name;
		cache.clear(key);
	}
	
	public void clear() {
		cache.clear();
	}
}
