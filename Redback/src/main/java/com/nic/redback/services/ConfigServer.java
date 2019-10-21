package com.nic.redback.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackService;

public class ConfigServer extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	private String devpath;
	private String classpath;

	public ConfigServer(DataMap c) 
	{
		super(c);
		if(config.containsKey("devpath"))
			devpath = config.getString("devpath");
		classpath = "/com/nic/redback/config";
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		logger.finer("Config service start");
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String service = request.getString("service");
			String category = request.getString("category");
			String name = request.getString("name");
			DataMap filter = request.getObject("filter");
			
			if(action.equals("get"))
			{
				response.setData(getConfig(service, category, name).toString());
			} 
			else if(action.equals("list"))
			{
				response.setData("{\"result\":" + getConfigList(service, category, filter).toString() + "}");
			}
			else 
			{
				throw new FunctionErrorException("Action '" + action + "' is unknown");
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
		logger.finer("Config service finish");
		return response;
	}
	
	protected DataMap getConfig(String service, String category, String name) throws DataException, FunctionErrorException, IOException
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
    		InputStream is = getClass().getResourceAsStream(classpath + "/" + service + "/" + category + "/" + name + ".json");
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
			throw new FunctionErrorException("The requested configuration in not found");
		}
	}
	
	protected DataList getConfigList(String service, String category, DataMap filter) throws DataException, FunctionErrorException, IOException
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
    		InputStream is = getClass().getResourceAsStream(root);
    		if(is != null)
    		{
    			BufferedReader br = new BufferedReader(new InputStreamReader(is));
    			String resource = null;
    			while((resource = br.readLine()) != null)
    				if(resource.endsWith(".json"))
    					names.add(resource.substring(0, resource.length() - 5));
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

	public ServiceInformation getServiceInformation() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
