package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public abstract class IDGenerator extends Service implements ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");

	public IDGenerator(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		logger.finer("ID generator service start");
		Payload response = new Payload();
		String idName = payload.getString();
		try
		{
			String id = getNextId(idName);
			response.setData(id);
		}
		catch(Exception e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}
		logger.finer("ID generator service finish");
		return response;
	}

	public ServiceInformation getServiceInformation() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected abstract String getNextId(String name) throws RedbackException;

}
