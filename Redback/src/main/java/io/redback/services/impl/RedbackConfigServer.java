package io.redback.services.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidConfigException;
import io.redback.services.ConfigServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.FileWatcher;
import io.redback.utils.FileWatcher.FileWatcherListener;

public class RedbackConfigServer extends ConfigServer implements FileWatcherListener
{
	private DataClient dataClient;
	private String devpath;
	private String classpath;
	private Map<String, Map<String, CollectionConfig>> domainConfigCollections;
	
	public RedbackConfigServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		if(config.containsKey("dataservice")) {
			dataClient = new DataClient(firebus, config.getString("dataservice"));
		}
		if(config.containsKey("devpath") && config.getString("devpath").length() > 0) 
		{
			devpath = config.getString("devpath");
			try 
			{
				new FileWatcher(devpath, this);
			} 
			catch (IOException e) 
			{
				Logger.severe("rb.config.init", "Exception trying to watch the filesystem", e);
			}
		}
		classpath = "io/redback/config";	
		domainConfigCollections = new HashMap<String, Map<String, CollectionConfig>>();
		if(config.containsKey("domainconfigcollections")) {
			for(String service : config.getObject("domainconfigcollections").keySet()) {
				domainConfigCollections.put(service, new HashMap<String, CollectionConfig>());
				DataMap serviceMap = config.getObject("domainconfigcollections").getObject(service);
				for(String category : serviceMap.keySet()) {
					DataMap configMap = serviceMap.getObject(category);
					domainConfigCollections.get(service).put(category, new CollectionConfig(configMap));
				}
			}
		}
	}

	
	protected DataMap getConfig(String service, String category, String name) throws RedbackException
	{
		try
		{
			Reader reader = null;
			if(devpath != null)
			{
				File file = new File(devpath + File.separator + service + File.separator + category + File.separator + name + ".json");
				if(file.exists())
					reader = new FileReader(file);
			} 
			else 
			{
	    		InputStream is = getClass().getResourceAsStream("/" + classpath + "/" + service + "/" + category + "/" + name + ".json");
	    		if(is != null)
	    			reader = new InputStreamReader(is);
			}
	
			if(reader != null)
			{
				BufferedReader br = new BufferedReader(reader);
				String line = null;
				StringBuilder sb = new StringBuilder();
				while((line = br.readLine()) != null)
					sb.append(line + "\n");
				return new DataMap(sb.toString());
			}
			else
			{
				return null;
			}				

		}
		catch(IOException | DataException e)
		{
			throw new RedbackInvalidConfigException("Error getting config " + service + "/" + category + "/" + name, e);
		}
	}
	
	protected DataMap getDomainConfig(String service, String category, String name, String domain) throws RedbackException
	{
		if(dataClient != null) 
		{
			if(domainConfigCollections.containsKey(service) && domainConfigCollections.get(service).containsKey(category)) 
			{
				CollectionConfig collectionConfig = domainConfigCollections.get(service).get(category);
				DataMap reqkey = new DataMap("domain", domain, "name", name);
				DataMap res = dataClient.getData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(reqkey), null);
				if(res.containsKey("result") && res.getList("result").size() > 0) 
				{
					return res.getList("result").getObject(0);
				}
			}
		}
		return null;
	}
	
	
	protected DataList getConfigList(String service, String category, DataMap filter) throws RedbackException
	{
		try
		{
			List<String> names = new ArrayList<String>();
			DataList list = new DataList();
			String root = null;
			if(devpath != null)
			{
				root = devpath + File.separator + service + File.separator + category;
				File rootDir = new File(root);
				if(rootDir.isDirectory())
					for(File file: rootDir.listFiles()) 
						if(file.getName().endsWith(".json"))
	    					names.add(file.getName().substring(0, file.getName().length() - 5));
			}
			else
			{
				root = classpath + "/" + service + "/" + category;
				URL pathUrl = Thread.currentThread().getContextClassLoader().getResource(root);
				if(pathUrl != null) {
					if(pathUrl.getProtocol().equals("file"))
					{
						File dir = new File(pathUrl.toURI());
						for(File file: dir.listFiles()) 
		    				if(file.getName().endsWith(".json"))
		    					names.add(file.getName().substring(0, file.getName().length() - 5));
					}
					else if(pathUrl.getProtocol().equals("jar"))
					{
						String jarPath = pathUrl.getPath().substring(5, pathUrl.getPath().indexOf("!")); 
				        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
				        Enumeration<JarEntry> entries = jar.entries(); 
				        while(entries.hasMoreElements()) 
				        {
				          String name = entries.nextElement().getName();
				          if (name.startsWith(root)) 
				          { 
				            String resource = name.substring(root.length() + 1);
				            if(resource.endsWith(".json"))
				            	names.add(resource.substring(0, resource.length() - 5));
				          }
				        }
				        jar.close();
					}
				}
			}		
	
			for(int i = 0; i < names.size(); i++)
			{
				DataMap cfg = getConfig(service, category, names.get(i));
				boolean addToList = true;
				if(filter != null)
				{
					Iterator<String> it = filter.keySet().iterator();
					while(it.hasNext())
					{
						String key = it.next();
						if(!(cfg.containsKey(key) && cfg.get(key).equals(filter.get(key))))
							addToList = false;									
					}
				}
				if(addToList)
					list.add(cfg);					
			}
			return list;
		}
		catch(Exception e)
		{
			throw new RedbackException("Error getting a config list", e);
		}
	}

	
	protected DataList getDomainConfigList(String service, String category, DataMap filter) throws RedbackException
	{
		if(dataClient != null) 
		{
			if(domainConfigCollections.containsKey(service) && domainConfigCollections.get(service).containsKey(category)) 
			{
				CollectionConfig collectionConfig = domainConfigCollections.get(service).get(category);
				DataMap res = dataClient.getData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(filter), null);
				if(res.containsKey("result")) 
				{
					return res.getList("result");
				}
			}
		}
		return null;
	}

	public void fileModified(File file, int o) 
	{
		firebus.publish("_rb_config_cache_clear", new Payload());
	}

}
