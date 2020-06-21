package io.redback.client;

import io.firebus.Firebus;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class AccessManagementClient extends Client
{

	public AccessManagementClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}

	public Session validate(String token) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		DataMap req = new DataMap();
		req.put("action", "validate");
		req.put("token", token);
		DataMap resp = request(req);
		if(resp != null  &&  resp.getString("result").equals("ok"))
			return new Session(resp.getObject("session"));
		else
			throw new RedbackException("Token cannot be validated");
	}


}
