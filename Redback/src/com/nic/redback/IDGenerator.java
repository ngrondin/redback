package com.nic.redback;

import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONObject;

public class IDGenerator extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");

	public IDGenerator(JSONObject c) 
	{
		super(c);
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		Payload response = new Payload();
		String id = "";
		try
		{
			String idName = payload.getString();
			JSONObject idConfig = getIDConfig(idName);
			if(idConfig.getString("type").equals("sequence"))
			{
				int next = Integer.parseInt(idConfig.getString("next"));
				id = "" + next;
				if(idConfig.containsKey("pad"))
					while(id.length() < Integer.parseInt(idConfig.getString("pad")))
						id = "0" + id;
				if(idConfig.containsKey("prefix"))
					id = idConfig.getString("prefix") + id;
				next++;
				firebus.publish(configService, new Payload("{object:rbid_config, data:{_id:" + idConfig.getString("_id") + ", next:\"" + next + "\"}}"));
			}
			response.setData(id);
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		return response;
	}

	public ServiceInformation getServiceInformation() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected JSONObject getIDConfig(String name) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		JSONObject idConfig = null;
		JSONObject configList = request(configService, "{object:rbid_config,filter:{name:" + name + "}}");
		if(configList.getList("result").size() > 0)
			idConfig = configList.getObject("result.0");
		return idConfig;
	}
	
}
