package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.ServiceProvider;
import io.redback.utils.GeoInfo;
import io.redback.utils.GeoRoute;
import io.redback.utils.Geometry;

public abstract class GeoServer extends ServiceProvider
{

	public GeoServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackService(Session session, Payload payload) throws RedbackException {
		Logger.finer("rb.geo.start", null);
		Payload response = new Payload();
		try
		{
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			DataMap responseData = null;
			if(action != null)
			{
				if(action.equals("geocode"))
				{
					GeoInfo geoinfo = null;
					if(request.containsKey("address"))
					{
						geoinfo = geocode(request.getString("address"));
					}
					else if(request.containsKey("geometry"))
					{
						geoinfo = geocode(new Geometry(request.getObject("geometry")));
					}
					else
					{
						throw new RedbackException("A 'geocode' action requires an 'address' attribute");
					}
					responseData = new DataMap();
					if(geoinfo != null) {
						responseData.put("geometry", geoinfo.geometry.toDataMap());
						responseData.put("address", geoinfo.address);
						responseData.put("addressparts", geoinfo.addressParts);
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
			response.setData(responseData);
			response.metadata.put("mime", "application/json");
		}
		catch(Exception e)
		{
			throw new RedbackException("Exception in geo service", e);
		}		

		Logger.finer("rb.geo.finish", null);
		return response;			
		
	}
	
	protected abstract GeoInfo geocode(String address) throws RedbackException;
	
	protected abstract GeoInfo geocode(Geometry geometry) throws RedbackException;
	
	protected abstract List<String> address(String search, Geometry location, Long radius) throws RedbackException;	

	protected abstract String timezone(Geometry geometry) throws RedbackException;
	
	protected abstract GeoRoute travel(Geometry start, Geometry end) throws RedbackException;
	
}
