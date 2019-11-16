package com.nic.redback;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.interfaces.BusFunction;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class RedbackServer
{
	private Logger logger = Logger.getLogger("com.nic.redback.RedbackServer");
	protected ArrayList<BusFunction> services;
	protected static ArrayList<Logger> loggers;
	protected Firebus firebus;
	
	public RedbackServer(DataMap config)
	{
		firebus = new Firebus(config.getString("network"), config.getString("password"));
		DataList knownAddresses = config.getList("knownaddresses");
		if(knownAddresses != null)
		{
			for(int i = 0; i < knownAddresses.size(); i++)
			{
				String address = knownAddresses.getObject(i).getString("address");
				int port = Integer.parseInt(knownAddresses.getObject(i).getString("port"));
				logger.fine("Adding known address " + address + ":" + port);
				firebus.addKnownNodeAddress(address, port);
			}
		}
		
		List<Logger> loggers = new ArrayList<Logger>();
		DataList loggerConfigs = config.getList("loggers");
		for(int i = 0; i < loggerConfigs.size(); i++)
		{
			try
			{
				DataMap loggerConfig = loggerConfigs.getObject(i);
				Logger logger = Logger.getLogger(loggerConfig.getString("name"));
				Formatter formatter = (Formatter)Class.forName(loggerConfig.getString("formatter")).newInstance();
				FileHandler fileHandler = new FileHandler(loggerConfig.getString("filename"));
				fileHandler.setFormatter(formatter);
				fileHandler.setLevel(Level.parse(loggerConfig.getString("level")));
				logger.addHandler(fileHandler);
				logger.setUseParentHandlers(false);
				logger.setLevel(Level.parse(loggerConfig.getString("level")));
				loggers.add(logger);
			}
			catch(Exception e)
			{
				logger.severe("General error when configuring loggers : " + e.getMessage());
			}
		}
		
		logger.fine("Adding services to container");
		services = new ArrayList<BusFunction>();
		DataList serviceConfigs = config.getList("services");
		for(int i = 0; i < serviceConfigs.size(); i++)
		{
			DataMap serviceConfig = serviceConfigs.getObject(i); 
			String className = serviceConfig.getString("class");
			String name = serviceConfig.getString("name");
			DataMap deploymentConfig = serviceConfig.getObject("config");
			if(className != null && name != null)
			{
				try
				{
					logger.fine("Instantiating service " + name);
					Class<?> c = Class.forName(className);
					Constructor<?> cons = null;
					BusFunction service = null;
					try {
						cons = c.getConstructor(new Class[]{DataMap.class, Firebus.class});
					} catch (NoSuchMethodException e) {}
					if(cons != null) 
					{
						service = (BusFunction)cons.newInstance(new Object[]{deploymentConfig, firebus});
					}
					else
					{
						try {
							cons = c.getConstructor(new Class[]{DataMap.class});
						} catch (NoSuchMethodException e) {}
						if(cons != null)
						{
							service = (BusFunction)cons.newInstance(new Object[]{deploymentConfig});
						}
						else
						{
							try {
								cons = c.getConstructor();
							} catch (NoSuchMethodException e) {}
							if(cons != null)
							{
								service = (BusFunction)cons.newInstance();
							}
							else
							{
								logger.severe("No appropriate constructor can be found for class " + className + " ");
							}
						}
					}
					
					if(service != null)
					{
						if(service instanceof ServiceProvider)
							firebus.registerServiceProvider(name, ((ServiceProvider)service), 10);
						if(service instanceof Consumer)
							firebus.registerConsumer(name, ((Consumer)service), 10);
						services.add(service);
					}
				}
				catch(ClassNotFoundException e)
				{
					logger.severe("Class " + className + " cannot be found in the classpath");
				}
				catch(InvocationTargetException | IllegalAccessException | InstantiationException e)
				{
					logger.severe("Error invoking the constructor for class " + className + " : " + e.getCause().getMessage());
				}					
			}
			else
			{
				logger.severe("No class or name provided for service");
			}
		}
	}

/*	
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
	*/
	
	public static void main(String[] args)
	{
		if(args.length > 0)
		{			
			try
			{
				Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
				DataMap config = new DataMap(new FileInputStream(args[0]));
				new RedbackServer(config);
				System.out.println("Redback server started");
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}
}
