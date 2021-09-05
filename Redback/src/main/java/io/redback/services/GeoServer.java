package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.ServiceProvider;
import io.redback.utils.GeoRoute;
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
						throw new RedbackException("A 'geocode' action requires an 'address' attribute");
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
						throw new RedbackException("An 'address' action requires an 'address' attribute");
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
						throw new RedbackException("A 'timezone' action requires a 'geometry' attribute");
					}
				}
				else if(action.equals("travel")) 
				{
					Geometry start = new Geometry(request.getObject("start"));
					Geometry end = new Geometry(request.getObject("end"));
					GeoRoute route = travel(start, end);
					if(route != null)
						responseData = route.toDataMap();
					else
						throw new RedbackException("Cannot find route");
				}
				else
				{
					throw new RedbackException("Valid actions are 'address', 'geocode', 'timezone' and 'travel'");
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
	
	protected abstract Geometry geocode(String address) throws RedbackException;
	
	protected abstract String geocode(Geometry geometry) throws RedbackException;
	
	protected abstract List<String> address(String search, Geometry location, Long radius) throws RedbackException;	

	protected abstract String timezone(Geometry geometry) throws RedbackException;
	
	protected abstract GeoRoute travel(Geometry start, Geometry end) throws RedbackException;
	
}
