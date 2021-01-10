package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Geometry;

public abstract class GeoServer extends ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");


	public GeoServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackService(Session session, Payload payload) throws RedbackException {
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
						String searchTerm = request.getString("search");
						Geometry location = request.containsKey("location") ? new Geometry(request.getObject("location")) : null;
						Long radius = request.containsKey("radius") ? request.getNumber("radius").longValue() : null; 
						List<String> addresses = address(searchTerm, location, radius);
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
				else if(action.equals("timezone"))
				{
					if(request.containsKey("geometry"))
					{
						String zoneId = timezone(new Geometry(request.getObject("geometry")));
						responseData = new DataMap("timezone", zoneId);
					}
					else
					{
						throw new FunctionErrorException("A 'timezone' action requires a 'geometry' attribute");
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
			throw new RedbackException("Exception in geo service", e);
		}		

		logger.finer("Geo service finish");
		return response;			
		
	}


	public void clearCaches() {
		
	}
	
	protected abstract Geometry geocode(String address) throws RedbackException;
	
	protected abstract String geocode(Geometry geometry) throws RedbackException;
	
	protected abstract List<String> address(String search, Geometry location, Long radius) throws RedbackException;	

	protected abstract String timezone(Geometry geometry) throws RedbackException;
	
}
