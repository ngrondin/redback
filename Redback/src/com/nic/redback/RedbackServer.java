package com.nic.redback;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nic.firebus.logging.FirebusSimpleFormatter;
import com.nic.firebus.standalone.StandaloneContainer;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class RedbackServer extends StandaloneContainer
{
	protected ArrayList<RedbackService> services;
	
	public RedbackServer(JSONObject config)
	{
		super(config);
		
		services = new ArrayList<RedbackService>();
		JSONList list = config.getList("services");
		for(int i = 0; i < list.size(); i++)
		{
			JSONObject deploymentConfig = list.getObject(i);
			String type = deploymentConfig.getString("type");
			String name = deploymentConfig.getString("name");
			JSONObject serviceConfig = deploymentConfig.getObject("config");
			RedbackService service = RedbackService.instantiate(type, serviceConfig);
			service.setName(name);
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
				
				Logger rbLogger = Logger.getLogger("com.nic.redback");
				FileHandler rbfh = new FileHandler("RedbackServer.log");
				rbfh.setFormatter(new FirebusSimpleFormatter());
				rbfh.setLevel(Level.FINEST);
				rbLogger.addHandler(rbfh);
				rbLogger.setLevel(Level.FINEST);
				
				Logger fbLogger = Logger.getLogger("com.nic.firebus");
				FileHandler fbfh = new FileHandler("Firebus.log");
				fbfh.setFormatter(new FirebusSimpleFormatter());
				fbfh.setLevel(Level.INFO);
				fbLogger.addHandler(fbfh);
				fbLogger.setLevel(Level.INFO);

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
