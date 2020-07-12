package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Geometry;

public abstract class GeoServer extends Service implements ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");


	public GeoServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		logger.finer("Geo service start");
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			DataMap responseData = null;
			if(action != null)
			{
				if(action.equals("geocode"))
				{
					if(request.containsKey("address"))
					{
						Geometry geometry = geocode(request.getString("address"));
						responseData = new DataMap("geometry", geometry.toDataMap());
					}
					else if(request.containsKey("geometry"))
					{
						String address = geocode(new Geometry(request.getObject("geometry")));
						responseData = new DataMap("address", address);
					}
					else
					{
						throw new FunctionErrorException("A 'geocode' action requires an 'address' attribute");
					}
				}
				else if(action.equals("address"))
				{
					if(request.containsKey("search"))
					{
						List<String> addresses = address(request.getString("search"));
						DataList list = new DataList();
						for(String a: addresses)
							list.add(a);
						responseData = new DataMap("result", list);
					}
					else
					{
						throw new FunctionErrorException("An 'address' action requires an 'address' attribute");
					}					
				}
				else
				{
					throw new FunctionErrorException("Valid actions are 'address', 'geocode' and 'reversegc'");
				}
			}
			response.setData(responseData.toString());
			response.metadata.put("mime", "application/json");
		}
		catch(Exception e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}		

		logger.finer("Geo service finish");
		return response;			
		
	}

	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException {
		return null;
	}

	public void clearCaches() {
		
	}
	
	protected abstract Geometry geocode(String address) throws RedbackException;
	
	protected abstract String geocode(Geometry geometry) throws RedbackException;
	
	protected abstract List<String> address(String search) throws RedbackException;	

}
