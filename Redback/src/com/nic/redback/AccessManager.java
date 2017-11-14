package com.nic.redback;

import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONObject;

public class AccessManager extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String dataService;
	
	public AccessManager(JSONObject c) 
	{
		super(c);
		dataService = config.getString("dataservice");
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		Payload responsePayload = null;
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String action = request.getString("action");
			String sessionid = request.getString("sessionid");
			
			if(action.equals("authenticate"))
			{
				String username = request.getString("username");
				String password = request.getString("password");
				if(username != null  &&  password != null)
				{
					UserProfile userProfile = new UserProfile(request(dataService, ""));
				}
				else
				{
					String msg = "An authenticate action requires a username and a password";
					logger.severe(msg);
					throw new FunctionErrorException(msg);
				}
			}
			else if(action.equals("validate"))
			{
				
			}

		}
		catch(Exception e)
		{	
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		return responsePayload;
	}

	public ServiceInformation getServiceInformation() 
	{
		return null;
	}

}
