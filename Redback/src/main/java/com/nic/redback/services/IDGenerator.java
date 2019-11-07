package com.nic.redback.services;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;

public abstract class IDGenerator extends DataService
{
	private Logger logger = Logger.getLogger("com.nic.redback");

	public IDGenerator(DataMap c, Firebus f) 
	{
		super(c, f);
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
		catch(RedbackException e)
		{
			throw new FunctionErrorException(e.getMessage());
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
