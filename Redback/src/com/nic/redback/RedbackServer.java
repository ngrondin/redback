package com.nic.redback;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.logging.FirebusSimpleFormatter;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class RedbackServer
{
	protected JSONObject config;
	protected Firebus firebus;
	protected ArrayList<RedbackService> services;
	
	public RedbackServer(JSONObject c)
	{
		config = c;
		
		JSONObject firebusConfig = config.getObject("firebus");
		if(firebusConfig != null)
		{
			firebus = new Firebus(firebusConfig.getString("name"), firebusConfig.getString("password"));
		}
		
		services = new ArrayList<RedbackService>();
		JSONList list = config.getList("services");
		for(int i = 0; i < list.size(); i++)
		{
			JSONObject serviceConfig = list.getObject(i);
			String type = serviceConfig.getString("type");
			String name = serviceConfig.getString("name");
			JSONObject config = serviceConfig.getObject("config");
			RedbackService service = RedbackService.instantiate(type, config);
			services.add(service);
			if(firebus != null)
			{
				service.setFirebus(firebus);
				firebus.registerServiceProvider(name, service, 10);
			}
		}
	}

	public static void main(String[] args)
	{
		if(args.length > 0)
		{			
			try
			{
				Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
				Logger logger = Logger.getLogger("com.nic.redback");
				FileHandler fh = new FileHandler("RedbackServer.log");
				fh.setFormatter(new FirebusSimpleFormatter());
				fh.setLevel(Level.FINEST);
				logger.addHandler(fh);
				logger.setLevel(Level.FINEST);

				JSONObject config = new JSONObject(new FileInputStream(args[0]));
				new RedbackServer(config);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}
}
