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
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class RedbackServer extends StandaloneContainer
{
	protected ArrayList<RedbackService> services;
	protected static ArrayList<Logger> loggers;
	
	public RedbackServer(JSONObject config)
	{
		super(config);
		try
		{
			loggers = new ArrayList<Logger>();
			JSONList list = config.getList("loggers");
			for(int i = 0; i < list.size(); i++)
			{
				JSONObject loggerJSON = list.getObject(i);
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
					if(service instanceof ServiceProvider)
						firebus.registerServiceProvider(name, service, 10);
					if(service instanceof Consumer)
						firebus.registerConsumer(name, (Consumer)service, 10);
				}
			}
		}
		catch(Exception e)
		{
			System.err.println("Error initialising logger: " + e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		if(args.length > 0)
		{			
			try
			{
				Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
				/*
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

				Logger psLogger = Logger.getLogger("com.nici.redback.services.processserver");
				FileHandler psfh = new FileHandler("PorcessServer.log");
				psfh.setFormatter(new ProcessLogger());
				psfh.setLevel(Level.FINE);
				psLogger.addHandler(psfh);
				psLogger.setLevel(Level.FINE);
				System.out.println(System.identityHashCode(psLogger));
				System.out.println(psLogger.getHandlers().length);
*/
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
