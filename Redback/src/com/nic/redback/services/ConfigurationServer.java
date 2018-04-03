package com.nic.redback.services;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;

public class ConfigurationServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String processServiceName;
	protected String objectServiceName;
	protected String idGenServiceName;

	public ConfigurationServer(JSONObject c)
	{
		super(c);
		processServiceName = config.getString("processservice");
		objectServiceName = config.getString("objectservice");
		idGenServiceName = config.getString("idgeneratorservice");
	}

	public void setFirebus(Firebus fb)
	{
		super.setFirebus(fb);
	}
	
	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		logger.info("Configuration service start");
		Payload response = new Payload();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String action = request.getString("action");
			JSONObject responseData = null;
			
			if(action != null)
			{
				if(action.equals("createobject"))
				{
					Payload idResp = firebus.requestService(idGenServiceName, new Payload("rbobjectid")); 
					String Id = idResp.getString();
					JSONObject newObject = new JSONObject("{_id:\"" + Id + "\"}");
					firebus.publish(configService, new Payload("{object:rbo_config,data:" + newObject.toString() + "}"));
					responseData = newObject;
				}
				else if(action.equals("getobject"))
				{
					String id = request.getString("_id");
					if(id != null)
					{
						JSONObject result = request(configService, new JSONObject("{object:rbo_config, filter:{_id:\"" + id + "\"}}"));
						if(result.getList("result").size() > 0)
							responseData = result.getObject("result.0");
					}
					else
					{
						throw new FunctionErrorException("A 'getobject' action requires a '_id' attribute");
					}
				}
				else if(action.equals("listobjects"))
				{
					JSONObject result = request(configService, new JSONObject("{object:rbo_config, filter:{}}"));
					JSONList resultList = result.getList("result");
					JSONList list = new JSONList();
					for(int i = 0; i < resultList.size(); i++)
					{
						list.add(new JSONObject("{_id:\"" + resultList.getObject(i).getString("_id") + "\", name:\"" + resultList.getObject(i).getString("name") + "\"}"));
					}
					responseData = new JSONObject();
					responseData.put("result", list);
				}
				else if(action.equals("updateobject"))
				{
					JSONObject cfg = request.getObject("config");
					if(cfg != null)
					{
						firebus.publish(configService, new Payload("{object:rbo_config,data:" + cfg.toString() + ", operation:replace}"));
						responseData = new JSONObject("{result:\"ok\"}");
						firebus.publish(objectServiceName, new Payload("refreshconfig"));
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
						JSONObject result = request(configService, new JSONObject("{object:rbpm_config, filter:{name:" + name + ", version:" + version +"}}"));
						if(result.getList("result").size() > 0)
							responseData = result.getObject("result.0");
					}
					else
					{
						throw new FunctionErrorException("A 'getprocess' action requires a 'name' and 'version' attribute");
					}
				}
				else if(action.equals("listprocesses"))
				{
					JSONObject result = request(configService, new JSONObject("{object:rbpm_config, filter:{}}"));
					JSONList resultList = result.getList("result");
					HashMap<String, Integer> processVersions = new HashMap<String, Integer>();
					for(int i = 0; i < resultList.size(); i++)
					{
						String processName = resultList.getObject(i).getString("name");
						Integer processVersion = resultList.getObject(i).getNumber("version").intValue();
						if(!processVersions.containsKey(processName)  ||  processVersion > processVersions.get(processName))
							processVersions.put(processName, processVersion);
					}
					JSONList list = new JSONList();
					Iterator<String> it = processVersions.keySet().iterator();
					while(it.hasNext())
					{
						String processName = it.next();
						list.add(new JSONObject("{name:\"" + processName + "\", version:\"" + processVersions.get(processName) + "\"}"));
					}
					responseData = new JSONObject();
					responseData.put("result", list);
				}
				else if(action.equals("updateprocess"))
				{
					JSONObject cfg = request.getObject("config");
					if(cfg != null)
					{
						firebus.publish(configService, new Payload("{object:rbpm_config,data:" + cfg.toString() + ", operation:replace}"));
						responseData = new JSONObject("{result:\"ok\"}");
						firebus.publish(processServiceName, new Payload("refreshconfig"));
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
		catch(FunctionTimeoutException | JSONException | RedbackException e)
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

	protected String buildErrorMessage(Exception e)
	{
		String msg = "";
		Throwable t = e;
		while(t != null)
		{
			if(msg.length() > 0)
				msg += " : ";
			msg += t.getMessage();
			t = t.getCause();
		}
		return msg;
	}
	
	protected String getStackTrace(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = sw.toString(); 
		return sStackTrace;
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	

	
}
