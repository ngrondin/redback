package com.nic.redback.services.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.services.ConfigDevelopmentServer;

public class RedbackConfigDevServer extends ConfigDevelopmentServer
{
	protected String devPath;

	public RedbackConfigDevServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		if(config.containsKey("devpath"))
			devPath = config.getString("devpath");
	}

	protected DataMap getDevConfig(String service, String category, String name) throws RedbackException
	{
		try
		{
			File file = new File(devPath + "\\" + service + "\\" + category + "\\" + name + ".json");
			if(file.exists())
			{
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;
				StringBuilder sb = new StringBuilder();
				while((line = br.readLine()) != null)
					sb.append(line + "\n");
				br.close();
				return new DataMap(sb.toString());
			}
			else
			{
				error("The requested configuration in not found");
			}
		}
		catch(DataException | IOException e)
		{
			error("Error getting the configuration", e);
		}
		return null;
	}
	
	protected DataList getDevConfigList(String service, String category, DataMap filter) throws RedbackException
	{
		List<String> filePaths = new ArrayList<String>();
		DataList list = new DataList();
		String root = devPath + "\\" + service + "\\" + category;
		File file = new File(root);
		if(file.isDirectory())
		{
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++)
				filePaths.add(files[i].getPath());
		}

		for(int i = 0; i < filePaths.size(); i++)
		{
			String resource = filePaths.get(i);
			if(resource.endsWith(".json"))
			{
				String name = resource.substring(root.length() + 1, resource.length() - 5);
				DataMap cfg = getDevConfig(service, category, name);
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
		}
		return list;
	}
	
	protected void putDevConfig(String service, String category, String name, DataMap obj) throws RedbackException
	{
		try
		{
			if(devPath != null)
			{
				FileOutputStream fos = new FileOutputStream(devPath + "\\" + service + "\\" + category + "\\" + name + ".json");
				fos.write(obj.toString().getBytes());
				fos.close();
			}
		}
		catch(IOException e)
		{
			error("Error putting the configuration", e);
		}
	}
}
