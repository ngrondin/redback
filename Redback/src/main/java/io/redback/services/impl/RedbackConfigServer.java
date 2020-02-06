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
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.services.ConfigServer;
import io.redback.utils.FileWatcher;
import io.redback.utils.FileWatcher.FileWatcherListener;

public class RedbackConfigServer extends ConfigServer implements FileWatcherListener
{
	private Logger logger = Logger.getLogger("io.redback");
	private String devpath;
	private String classpath;
	
	public RedbackConfigServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		if(config.containsKey("devpath") && config.getString("devpath").length() > 0) 
		{
			devpath = config.getString("devpath");
			try 
			{
				new FileWatcher(devpath, this);
			} 
			catch (IOException e) 
			{
				logger.severe("Exception trying to watch the filesystem: " + e.getMessage());
			}
		}
		classpath = "io/redback/config";		
	}

	
	protected DataMap getConfig(String service, String category, String name) throws RedbackException
	{
		try
		{
			Reader reader = null;
			if(devpath != null)
			{
				File file = new File(devpath + "\\" + service + "\\" + category + "\\" + name + ".json");
				if(file.exists())
					reader = new FileReader(file);
			}
			
			if(reader == null)
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
				error("The requested configuration '" + service + "/" + category + "/" + name + "' is not found");
				return null;
			}
		}
		catch(IOException | DataException e)
		{
			error("Error getting a config", e);
			return null;
		}
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
				root = devpath + "\\" + service + "\\" + category;
				File file = new File(root);
				if(file.isDirectory())
				{
					File[] files = file.listFiles();
					for(int i = 0; i < files.length; i++)
	    				if(files[i].getName().endsWith(".json"))
	    					names.add(files[i].getName().substring(0, files[i].getName().length() - 5));
				}
			}
			else
			{
				root = classpath + "/" + service + "/" + category;
				URL pathUrl = Thread.currentThread().getContextClassLoader().getResource(root);
				if(pathUrl.getProtocol().equals("file"))
				{
					File[] files = new File(pathUrl.toURI()).listFiles();
					for(int i = 0; i < files.length; i++)
	    				if(files[i].getName().endsWith(".json"))
	    					names.add(files[i].getName().substring(0, files[i].getName().length() - 5));
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
			error("Error getting a config list", e);
			return null;

		}
	}


	public void fileModified(File file, int o) 
	{
		firebus.publish("_rb_config_cache_clear", new Payload());
	}

	
}
