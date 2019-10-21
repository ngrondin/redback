package com.nic.redback;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.standalone.StandaloneContainer;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.services.AccessManager;
import com.nic.redback.services.ConfigDevelopmentServer;
import com.nic.redback.services.ConfigServer;
import com.nic.redback.services.FileServer;
import com.nic.redback.services.IDGenerator;
import com.nic.redback.services.ObjectServer;
import com.nic.redback.services.ProcessServer;
import com.nic.redback.services.UIServer;

public class RedbackServer extends StandaloneContainer
{
	protected ArrayList<RedbackService> services;
	protected static ArrayList<Logger> loggers;
	
	public RedbackServer(DataMap config)
	{
		super(config);
		try
		{
			loggers = new ArrayList<Logger>();
			DataList list = config.getList("loggers");
			for(int i = 0; i < list.size(); i++)
			{
				DataMap loggerJSON = list.getObject(i);
				Logger logger = Logger.getLogger(loggerJSON.getString("name"));
				Formatter formatter = (Formatter)Class.forName(loggerJSON.getString("formatter")).newInstance();
				FileHandler fileHandler = new FileHandler(loggerJSON.getString("filename"));
				fileHandler.setFormatter(formatter);
				fileHandler.setLevel(Level.parse(loggerJSON.getString("level")));
				logger.addHandler(fileHandler);
				logger.setUseParentHandlers(false);
				logger.setLevel(Level.parse(loggerJSON.getString("level")));
				loggers.add(logger);
			}

			services = new ArrayList<RedbackService>();
			list = config.getList("services");
			for(int i = 0; i < list.size(); i++)
			{
				DataMap serviceDeployment = list.getObject(i);
				String type = serviceDeployment.getString("type");
				String name = serviceDeployment.getString("name");
				DataMap serviceDeploymentConfig = serviceDeployment.getObject("config");
				RedbackService service = instantiateService(type, serviceDeploymentConfig);
				if(service != null)
				{
					service.setName(name);
					services.add(service);
					if(firebus != null)
					{
						service.setFirebus(firebus);
						if(service instanceof ServiceProvider)
							firebus.registerServiceProvider(name, service, 10);
						if(service instanceof Consumer)
							firebus.registerConsumer(name, (Consumer)service, 10);
					}
				}
				else
				{
					System.err.println("Did not find a class for service " + name + " of type " + type);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	
	public RedbackService instantiateService(String type, DataMap config)
	{
		if(type.equalsIgnoreCase("objectservice"))
		{
			return new ObjectServer(config);
		}
		else if(type.equalsIgnoreCase("uiservice"))
		{
			return new UIServer(config);
		}
		else if(type.equalsIgnoreCase("processservice"))
		{
			return new ProcessServer(config);
		}
		else if(type.equalsIgnoreCase("idgeneratorservice"))
		{
			return new IDGenerator(config);
		}
		else if(type.equalsIgnoreCase("accessmanagementservice"))
		{
			return new AccessManager(config);
		}
		else if(type.equalsIgnoreCase("fileservice"))
		{
			return new FileServer(config);
		}
		else if(type.equalsIgnoreCase("configservice"))
		{
			return new ConfigServer(config);
		}
		else if(type.equalsIgnoreCase("configdevelopmentservice"))
		{
			return new ConfigDevelopmentServer(config);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		if(args.length > 0)
		{			
			try
			{
				Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
				DataMap config = new DataMap(new FileInputStream(args[0]));
				new RedbackServer(config);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}
}
