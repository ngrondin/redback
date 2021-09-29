package io.redback;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import io.firebus.logging.FirebusConsoleHandler;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.services.common.Provider;
import io.redback.utils.Watchdog;

public class RedbackServer 
{
	private Logger logger = Logger.getLogger("io.redback.RedbackServer");
	protected Map<String, BusFunction> services;
	protected static ArrayList<Logger> loggers;
	protected Firebus firebus;
	
	public RedbackServer(DataMap config)
	{
		long start = System.currentTimeMillis();
		List<Logger> loggers = new ArrayList<Logger>();
		DataList loggerConfigs = config.getList("loggers");
		if(loggerConfigs != null) {
			for(int i = 0; i < loggerConfigs.size(); i++)
			{
				try
				{
					DataMap loggerConfig = loggerConfigs.getObject(i);
					Logger logger = Logger.getLogger(loggerConfig.getString("name"));
					Constructor<?> formatterConsuctor = Class.forName(loggerConfig.getString("formatter")).getDeclaredConstructor();
					Formatter formatter = (Formatter)formatterConsuctor.newInstance();
					Handler handler = null;
					if(loggerConfig.containsKey("filename")) 
						handler = new FileHandler(loggerConfig.getString("filename"));
					else 
						handler = new FirebusConsoleHandler();
					
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
			firebus.setServiceThreadCount(config.getNumber("threads").intValue());
		if(config.containsKey("servicethreads"))
			firebus.setServiceThreadCount(config.getNumber("servicethreads").intValue());
		if(config.containsKey("streamthreads"))
			firebus.setStreamThreadCount(config.getNumber("streamthreads").intValue());
		if(config.containsKey("messagethreads"))
			firebus.setMessagingThreadCount(config.getNumber("messagethreads").intValue());
		
		firebus.registerConsumer("_rb_config_cache_clear", new Consumer() {
			public void consume(Payload payload) {
				configureAllServices();
			}
		}, 1);
		
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
					Constructor<?> agentConstructor = c.getDeclaredConstructor();
					DiscoveryAgent agent = (DiscoveryAgent)agentConstructor.newInstance();
					if(discoveryAgent.containsKey("config")) 
						agent.setConfig(discoveryAgent.getObject("config"));
					firebus.addDiscoveryAgent(agent);
				} 
				catch (Exception e) 
				{
					logger.severe("Error instantiating discoveryAgent " + className + " : " + e.getMessage());
				}
			}
		}		

		logger.fine("Adding services to container");
		services = new HashMap<String, BusFunction>();
		DataList serviceConfigs = config.getList("services");
		for(int i = 0; i < serviceConfigs.size(); i++)
		{
			DataMap serviceConfig = serviceConfigs.getObject(i); 
			String className = serviceConfig.getString("class");
			String name = serviceConfig.getString("name");
			int concurrent = serviceConfig.containsKey("concurrent") ? serviceConfig.getNumber("concurrent").intValue() : 10;
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
							cons = c.getConstructor(new Class[] {String.class, DataMap.class, RedbackServer.class});
						} catch(NoSuchMethodException e) {}
						if(cons != null) {
							service = (BusFunction)cons.newInstance(new Object[]{name, deploymentConfig, this});
						} else {
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
					}
					
					if(service != null)
					{
						if(service instanceof ServiceProvider)
							firebus.registerServiceProvider(name, ((ServiceProvider)service), concurrent);
						if(service instanceof StreamProvider)
							firebus.registerStreamProvider(name, ((StreamProvider)service), concurrent);
						if(service instanceof Consumer)
							firebus.registerConsumer(name, ((Consumer)service), concurrent);
						services.put(name, service);
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
		
		configureAllServices();
		startAllServices();
		new Watchdog(firebus);
		long end = System.currentTimeMillis();
		Logger.getLogger("io.redback").info("Redback server started in " + (end - start) + "ms");
	}
	
	protected void configureAllServices() {
		for(String name: services.keySet()) {
			BusFunction func = services.get(name);
			if(func instanceof Provider)
				((Provider)func).configure();
		}		
	}

	protected void startAllServices() {
		for(String name: services.keySet()) {
			BusFunction func = services.get(name);
			if(func instanceof Provider)
				((Provider)func).start();
		}		
	}
	
	public Firebus getFirebus() {
		return firebus;
	}
	
	public DataMap getStatus() {
		DataMap status = new DataMap();
		for(String name: services.keySet()) {
			BusFunction func = services.get(name);
			if(func instanceof Provider) {
				DataMap subStatus = ((Provider)func).getStatus();
				if(subStatus != null) {
					status.put(name, subStatus);
				}
			}
		}		
		return status;
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
				}
				else
				{
					Logger.getLogger("io.redback").severe("No config file provided");
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

}
