package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.services.IDGenerator;

public class RedbackIDGenerator extends IDGenerator
{
	private DataMap configCache;
	protected DataClient dataClient;
	protected ConfigurationClient configClient;


	public RedbackIDGenerator(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		configCache = new DataMap();
		dataClient = new DataClient(firebus, config.getString("dataservice"));
		configClient = new ConfigurationClient(firebus, config.getString("configservice"));

	}
	
	protected String getNextId(String name) throws RedbackException
	{
		try
		{
			String id = null;
			DataMap idConfig = configCache.getObject(name);
			if(idConfig == null)
			{
				idConfig = configClient.getConfig("rbid", "key", name);
				configCache.put(name, idConfig);
			}
			
			if(idConfig != null)
			{
				if(idConfig.getString("type").equals("sequence"))
				{
					synchronized(idConfig) 
					{
						int next = idConfig.containsKey("next") ? idConfig.getNumber("next").intValue() : 0;
						int reserved = idConfig.containsKey("reserved") ? idConfig.getNumber("reserved").intValue() : 0;
						if(next >= reserved)
						{
							DataMap seq = dataClient.getData("rbid_sequence", new DataMap("_id", name));
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
							dataClient.putData("rbid_sequence", new DataMap("_id", name), new DataMap("next", reserved));
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
				}
				return id;
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			error("Error getting next id", e);
			return null;
		}
	}
	
	
	public void clearCaches()
	{
		configCache.clear();
	}


}
