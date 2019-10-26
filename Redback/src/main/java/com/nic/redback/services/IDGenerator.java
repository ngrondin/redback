package com.nic.redback.services;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackDataService;
import com.nic.redback.RedbackException;

public class IDGenerator extends RedbackDataService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	private DataMap configCache;

	public IDGenerator(Firebus f, DataMap c) 
	{
		super(f, c);
		configCache = new DataMap();
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		logger.finer("ID generator service start");
		//TODO: Add handling of next batch
		Payload response = new Payload();
		String id = "";
		try
		{
			String idName = payload.getString();
			DataMap idConfig = configCache.getObject(idName);
			if(idConfig == null)
			{
				idConfig = getConfig("rbid", "key", idName);
				configCache.put(idName, idConfig);
			}
			
			if(idConfig != null)
			{
				if(idConfig.getString("type").equals("sequence"))
				{
					int next = idConfig.containsKey("next") ? idConfig.getNumber("next").intValue() : 0;
					int reserved = idConfig.containsKey("reserved") ? idConfig.getNumber("reserved").intValue() : 0;
					if(next >= reserved)
					{
						DataMap seq = getData("rbid_sequence", "{_id:\"" + idName + "\"}");
						if(seq.getList("result").size() > 0)
						{
							next = seq.getNumber("result.0.next").intValue();
							reserved = next + 10;
						}
						else
						{
							next = 0;
							reserved = 10;
						}
						idConfig.put("reserved", reserved);
						publishData("rbid_sequence", "{_id:\"" + idName + "\", next:" + reserved + "}");
					}
					id = "" + next;
					if(idConfig.containsKey("pad"))
						while(id.length() < Integer.parseInt(idConfig.getString("pad")))
							id = "0" + id;
					if(idConfig.containsKey("prefix"))
						id = idConfig.getString("prefix") + id;
					next++;
					idConfig.put("next", next);
				}
				response.setData(id);
			}
			else
			{
				throw new FunctionErrorException("No configuration exists for id '" + idName + "'");
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
		logger.finer("ID generator service finish");
		return response;
	}

	public ServiceInformation getServiceInformation() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected DataMap getIDConfig(String name) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		DataMap idConfig = null;
		DataMap configList = request(configService, "{object:rbid_config,filter:{name:" + name + "}}");
		if(configList.getList("result").size() > 0)
			idConfig = configList.getObject("result.0");
		return idConfig;
	}
	
}
