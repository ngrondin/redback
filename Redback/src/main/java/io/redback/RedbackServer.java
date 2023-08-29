package io.redback;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.firebus.DiscoveryAgent;
import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.interfaces.BusFunction;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.Logger;
import io.redback.services.common.Provider;
import io.redback.utils.Watchdog;

public class RedbackServer 
{
	protected Map<String, BusFunction> services;
	protected static ArrayList<Logger> loggers;
	protected Firebus firebus;
	
	public RedbackServer(DataMap config)
	{
		long start = System.currentTimeMillis();
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.OFF);
		DataMap loggingConfig = config.getObject("logging");
		if(loggingConfig != null) {
			if(loggingConfig.containsKey("level"))
				Logger.setLevel(Logger.getLevelFromString(loggingConfig.getString("level")));
			if(loggingConfig.containsKey("format"))
				Logger.setFormatter(loggingConfig.getString("format"));
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
			Logger.fine("Adding known addresses to firebus");
			for(int i = 0; i < knownAddresses.size(); i++)
			{
				String address = knownAddresses.getObject(i).getString("address");
				int port = Integer.parseInt(knownAddresses.getObject(i).getString("port"));
				Logger.fine("Adding known address " + address + ":" + port);
				firebus.addKnownNodeAddress(address, port);
			}
		}
		
		DataList discoveryAgents = config.getList("discoveryagents");
		if(discoveryAgents != null)
		{
			Logger.fine("Adding discovery agents to firebus");
			for(int i = 0; i < discoveryAgents.size(); i++)
			{
				DataMap discoveryAgent = discoveryAgents.getObject(i);  
				String className = discoveryAgent.getString("class");
				try
				{
					Logger.fine("Instantiating discovery agent " + className);
					Class<?> c = Class.forName(className);
					Constructor<?> agentConstructor = c.getDeclaredConstructor();
					DiscoveryAgent agent = (DiscoveryAgent)agentConstructor.newInstance();
					if(discoveryAgent.containsKey("config")) 
						agent.setConfig(discoveryAgent.getObject("config"));
					firebus.addDiscoveryAgent(agent);
				} 
				catch (Exception e) 
				{
					Logger.severe("Error instantiating discoveryAgent " + className + " : " + e.getMessage());
				}
			}
		}		

		Logger.fine("Adding services to container");
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
					Logger.fine("Instantiating service " + name);
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
											Logger.severe("No appropriate constructor can be found for class " + className + " ");
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
					Logger.severe("Class " + className + " cannot be found in the classpath");
				}
				catch(InvocationTargetException | IllegalAccessException | InstantiationException e)
				{
					Logger.severe("Error invoking the constructor for class " + className + " : " + e.getCause().getMessage());
				}					
			}
			else
			{
				Logger.severe("No class or name provided for service");
			}
		}
		
		configureAllServices();
		startAllServices();
		new Watchdog(firebus);
		long end = System.currentTimeMillis();
		Logger.info("Redback server started in " + (end - start) + "ms");
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
		try {
			String configString = null;
			Properties props = null;
			if(args.length > 0) {
				for(int i = 0; i < args.length; i++)  {
					if(args[i].endsWith(".json")) {
						configString = new String(Files.readAllBytes(Paths.get(args[i])));
					} else if(args[i].endsWith("properties")) {
						props = new Properties();
						props.load(new FileInputStream(args[i]));
					}
				}		
			} 
			
			if(configString == null) {
				Path configPath = Paths.get("redback.json");
				if(Files.exists(configPath)) {
					configString = new String(Files.readAllBytes(configPath));
				} else {
					InputStream in = RedbackServer.class.getClassLoader().getResourceAsStream("redback.json");
					if(in != null) {
						configString = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
					}
				}
			}
			
			if(props == null) {
				Path propsPath = Paths.get("redback.properties");
				if(Files.exists(propsPath)) {
					props = new Properties();
					props.load(Files.newInputStream(propsPath));
				} else {
					InputStream in = RedbackServer.class.getClassLoader().getResourceAsStream("redback.properties");
					if(in != null) {
						props = new Properties();
						props.load(in);
					}
				}
			}
			
			if(configString != null) {
					int pos1 = -1;
					while((pos1 = configString.indexOf("{{")) != -1) {
						int pos2 = configString.indexOf("}}", pos1);
						String key = configString.substring(pos1 + 2, pos2);
						String val = props != null && props.getProperty(key) != null ? props.getProperty(key) : System.getenv(key);
						if(val == null) val = "";
						configString = configString.substring(0, pos1) + val + configString.substring(pos2 + 2);
					}
					new RedbackServer(new DataMap(configString));
	
			} else {
				Logger.severe("No config file provided");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		

	}

}
