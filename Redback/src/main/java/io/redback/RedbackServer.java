package io.redback;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.DiscoveryAgent;
import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.interfaces.BusFunction;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamProvider;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.services.Service;
import io.redback.utils.Watchdog;

public class RedbackServer implements Consumer
{
	private Logger logger = Logger.getLogger("io.redback.RedbackServer");
	protected ArrayList<BusFunction> services;
	protected static ArrayList<Logger> loggers;
	protected Firebus firebus;
	
	public RedbackServer(DataMap config)
	{
		List<Logger> loggers = new ArrayList<Logger>();
		DataList loggerConfigs = config.getList("loggers");
		if(loggerConfigs != null) {
			for(int i = 0; i < loggerConfigs.size(); i++)
			{
				try
				{
					DataMap loggerConfig = loggerConfigs.getObject(i);
					Logger logger = Logger.getLogger(loggerConfig.getString("name"));
					Formatter formatter = (Formatter)Class.forName(loggerConfig.getString("formatter")).newInstance();
					Handler handler = null;
					if(loggerConfig.containsKey("filename")) 
						handler = new FileHandler(loggerConfig.getString("filename"));
					else
						handler = new ConsoleHandler();
					handler.setFormatter(formatter);
					handler.setLevel(Level.parse(loggerConfig.getString("level")));
					logger.addHandler(handler);
					logger.setUseParentHandlers(false);
					logger.setLevel(Level.parse(loggerConfig.getString("level")));
					loggers.add(logger);
				}
				catch(Exception e)
				{
					logger.severe("General error when configuring loggers : " + e.getMessage());
				}
			}
		}
		
		firebus = new Firebus(config.getString("network"), config.getString("password"));
		if(config.containsKey("threads"))
			firebus.setThreadCount(config.getNumber("threads").intValue());
		firebus.registerConsumer("_rb_config_cache_clear", this, 1);
		
		DataList knownAddresses = config.getList("knownaddresses");
		if(knownAddresses != null)
		{
			logger.fine("Adding known addresses to firebus");
			for(int i = 0; i < knownAddresses.size(); i++)
			{
				String address = knownAddresses.getObject(i).getString("address");
				int port = Integer.parseInt(knownAddresses.getObject(i).getString("port"));
				logger.fine("Adding known address " + address + ":" + port);
				firebus.addKnownNodeAddress(address, port);
			}
		}
		
		DataList discoveryAgents = config.getList("discoveryagents");
		if(discoveryAgents != null)
		{
			logger.fine("Adding discovery agents to firebus");
			for(int i = 0; i < discoveryAgents.size(); i++)
			{
				DataMap discoveryAgent = discoveryAgents.getObject(i);  
				String className = discoveryAgent.getString("class");
				try
				{
					logger.fine("Instantiating discovery agent " + className);
					Class<?> c = Class.forName(className);
					DiscoveryAgent agent = (DiscoveryAgent)c.newInstance();
					firebus.addDiscoveryAgent(agent);
				} 
				catch (Exception e) 
				{
					logger.severe("Error instantiating discoveryAgent " + className + " : " + e.getMessage());
				}
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
						cons = c.getConstructor(new Class[]{String.class, DataMap.class, Firebus.class});
					} catch (NoSuchMethodException e) {}
					if(cons != null) 
					{
						service = (BusFunction)cons.newInstance(new Object[]{name, deploymentConfig, firebus});
					}
					else
					{
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
								cons = c.getConstructor(new Class[]{String.class, DataMap.class});
								
							} catch (NoSuchMethodException e) {}
							if(cons != null)
							{
								service = (BusFunction)cons.newInstance(new Object[]{name, deploymentConfig});
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
						}
					}
					
					if(service != null)
					{
						if(service instanceof ServiceProvider)
							firebus.registerServiceProvider(name, ((ServiceProvider)service), 10);
						if(service instanceof StreamProvider)
							firebus.registerStreamProvider(name, ((StreamProvider)service), 10);
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
		
		new Watchdog();
	}

	public void consume(Payload payload) {
		for(int i = 0; i < services.size(); i++) {
			BusFunction service = services.get(i);
			if(service instanceof Service)
				((Service)service).clearCaches();
		}		
	}

	
	public static void main(String[] args)
	{
		if(args.length > 0)
		{			
			try
			{
				Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
				DataMap config = null;
				String configString = null;
				Properties props = null;
				for(int i = 0; i < args.length; i++) 
				{
					if(args[i].endsWith(".json"))
					{
						configString = new String(Files.readAllBytes(Paths.get(args[i])));
					}
					else if(args[i].endsWith("properties"))
					{
						props = new Properties();
						props.load(new FileInputStream(args[i]));
					}
				}
				
				if(configString != null) 
				{
					int pos1 = -1;
					while((pos1 = configString.indexOf("{{")) != -1)
					{
						int pos2 = configString.indexOf("}}", pos1);
						String key = configString.substring(pos1 + 2, pos2);
						String val = props != null && props.getProperty(key) != null ? props.getProperty(key) : System.getenv(key);
						if(val == null) val = "";
						configString = configString.substring(0, pos1) + val + configString.substring(pos2 + 2);
					}
					config = new DataMap(configString);
					new RedbackServer(config);
					System.out.println("Redback server started");
				}
				else
				{
					System.out.println("No config file provided");
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

}
