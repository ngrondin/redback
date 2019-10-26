package com.nic.redback.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackAuthenticatedService;
import com.nic.redback.security.Session;

public class ConfigDevelopmentServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String devPath;

	public ConfigDevelopmentServer(Firebus f, DataMap c)
	{
		super(f, c);
		if(config.containsKey("devpath"))
			devPath = config.getString("devpath");
/*		
		try
		{
		String configPath = "/com/nic/redback/wms/config";
		InputStream in1 = getClass().getResourceAsStream(configPath);
        BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
    	String service = null;
    	while ((service = br1.readLine()) != null) 
    	{
    		InputStream in2 = getClass().getResourceAsStream(configPath + "/" + service);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
        	String category = null;
        	while ((category = br2.readLine()) != null) 
        	{
        		InputStream in3 = getClass().getResourceAsStream(configPath + "/" + service + "/" + category);
                BufferedReader br3 = new BufferedReader(new InputStreamReader(in3));
            	String file = null;
            	while ((file = br3.readLine()) != null) 
            	{
            		if(file.endsWith(".json"))
            		{
            			String name = file.substring(0, file.length() - 5);
                		InputStream in4 = getClass().getResourceAsStream(configPath + "/" + service + "/" + category + "/" + file);
                        BufferedReader br4 = new BufferedReader(new InputStreamReader(in4));
                    	String line = null;
                    	StringBuilder sb = new StringBuilder();
                    	while ((line = br4.readLine()) != null) 
                    		sb.append(line + "\n");
                    	in4.close();
                    	DataMap cfg = new DataMap(sb.toString());
                    	if(!configs.containsKey(service))
                    		configs.put(service, new HashMap<String, Map<String, DataMap>>());
                    	if(!configs.get(service).containsKey(category))
                    		configs.get(service).put(category, new HashMap<String, DataMap>());
                    	configs.get(service).get(category).put(name, cfg);
            		}
            	}
            	in3.close();
        	}
        	in2.close();
    	}
    	in1.close();
		}
		catch(Exception e)
		{
			logger.severe(buildErrorMessage(e));
		}
		*/
	}

	/*
	public void setFirebus(Firebus fb)
	{
		super.setFirebus(fb);
	}
	*/
	
	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		logger.info("Configuration service start");
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			DataMap responseData = null;
			
			if(action != null)
			{
				if(action.equals("createobject"))
				{
					String name = request.getString("name");
					DataMap newObject = new DataMap("{name:\"" + name + "\"}");
					putDevConfig("rbo", "object", name, newObject);
					responseData = newObject;
				}
				else if(action.equals("getobject"))
				{
					String name = request.getString("name");
					if(name != null)
						responseData = getDevConfig("rbo", "object", name);
					else
						throw new FunctionErrorException("A 'getobject' action requires a '_id' attribute");
				}
				else if(action.equals("listobjects"))
				{
					DataList list = getDevConfigList("rbo", "object", null);
					responseData = new DataMap();
					responseData.put("result", list);
				}
				else if(action.equals("updateobject"))
				{
					DataMap cfg = request.getObject("config");
					if(cfg != null)
					{
						String name = cfg.getString("name");
						putDevConfig("rbo", "object", name, cfg);
						responseData = new DataMap("{result:\"ok\"}");
					}
					else
					{
						throw new FunctionErrorException("An 'updateobject' action requires a 'config' attribute");
					}
				}				
				else if(action.equals("getprocess"))
				{
					String name = request.getString("name");
					String version = request.getString("version");
					if(name != null  &&  version != null)
					{
						responseData = getDevConfig("rbpm", "process", name);
					}
					else
					{
						throw new FunctionErrorException("A 'getprocess' action requires a 'name' and 'version' attribute");
					}
				}
				else if(action.equals("listprocesses"))
				{
					HashMap<String, Integer> processVersions = new HashMap<String, Integer>();
					DataList list = getDevConfigList("rbpm", "process", null);
					responseData = new DataMap();
					responseData.put("result", list);
				}
				else if(action.equals("updateprocess"))
				{
					DataMap cfg = request.getObject("config");
					if(cfg != null)
					{
						String name = cfg.getString("name");
						this.putDevConfig("rbpm", "process", name, cfg);
						responseData = new DataMap("{result:\"ok\"}");
					}
					else
					{
						throw new FunctionErrorException("An 'updateprocess' action requires a 'config' attribute");
					}
				}
				else
				{
					throw new FunctionErrorException("The '" + action + "' action is not valid as an object request");
				}
			}
			else
			{
				throw new FunctionErrorException("Requests must have at least an 'action' attribute");
			}					
				
			response.setData(responseData.toString());
		}
		catch(DataException | IOException e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}		

		logger.info("Configuration service finish");
		return response;	
	}

	public Payload unAuthenticatedService(Session session, Payload payload)	throws FunctionErrorException
	{
		throw new FunctionErrorException("All requests need to be authenticated");
	}
	
	protected DataMap getDevConfig(String service, String category, String name) throws DataException, FunctionErrorException, IOException
	{
		File file = new File(devPath + "\\" + service + "\\" + category + "\\" + name + ".json");
		if(file.exists())
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
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
	
	protected DataList getDevConfigList(String service, String category, DataMap filter) throws DataException, FunctionErrorException, IOException
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
	
	private void putDevConfig(String service, String category, String name, DataMap obj) throws IOException
	{
		if(devPath != null)
		{
			FileOutputStream fos = new FileOutputStream(devPath + "\\" + service + "\\" + category + "\\" + name + ".json");
			fos.write(obj.toString().getBytes());
			fos.close();
		}		
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	

	
}
