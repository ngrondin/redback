package io.redback.services.impl;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.DataClient;
import io.redback.services.GeoServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.GeoRoute;
import io.redback.utils.Geometry;

public class RedbackGeoServer extends GeoServer
{
	protected String apiKey;
	protected String dataService;
	protected DataClient dataClient;
	protected CollectionConfig cacheCollection;
	protected String outboundService;
	protected String geocodeUrl;
	protected String addressUrl;
	protected String timezoneUrl;
	protected String distanceUrl;


	public RedbackGeoServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		apiKey = config.getString("apikey");
		outboundService = config.getString("outboundservice");
		dataService = config.getString("dataservice");
		if(dataService != null) {
			dataClient = new DataClient(firebus, dataService);
			cacheCollection = new CollectionConfig(config.getObject("cachecollection"), "rbgs_cache");
		}
		geocodeUrl = config.containsKey("geocodeurl") ? config.getString("geocodeurl") : "https://maps.googleapis.com/maps/api/geocode/json";
		addressUrl = config.containsKey("addressurl") ? config.getString("addressurl") : "https://maps.googleapis.com/maps/api/place/autocomplete/json";
		timezoneUrl = config.containsKey("timezoneurl") ? config.getString("timezoneurl") : "https://maps.googleapis.com/maps/api/timezone/json";
		distanceUrl = config.containsKey("dirstanceurl") ? config.getString("dirstanceurl") : "https://maps.googleapis.com/maps/api/distancematrix/json";
	}

	protected Geometry geocode(String address) throws RedbackException
	{
		Geometry geometry = null;
		try
		{
			DataMap request = new DataMap();
			request.put("method", "get");
			request.put("url", geocodeUrl + "?address=" + address.replaceAll(" ", "%20") + "&key=" + apiKey);
			DataMap resp = requestService(request);
			if(resp.getList("results").size() > 0) {
				DataMap geoData = new DataMap();
				geoData.put("type", "point");
				DataMap coords = new DataMap();
				coords.put("latitude", resp.getNumber("results.0.geometry.location.lat"));
				coords.put("longitude", resp.getNumber("results.0.geometry.location.lng"));
				geoData.put("coords", coords);
				geometry = new Geometry(geoData);
			}
		}
		catch(Exception e)
		{
			throw new RedbackException("Error geocoding address", e);
		}
		return geometry;
		
	}

	protected String geocode(Geometry geometry)  throws RedbackException
	{
		String address = null;
		try
		{
			DataMap request = new DataMap();
			request.put("method", "get");
			request.put("url", geocodeUrl + "?latlng=" + geometry.getLatitude() + "," + geometry.getLongitude() + "&key=" + apiKey);
			DataMap resp = requestService(request);
			if(resp.getList("results").size() > 0) {
				address = resp.getString("results.0.formatted_address");
			} 
		}
		catch(Exception e)
		{
			throw new RedbackException("Error geocoding address", e);
		}
		return address;
	}

	protected List<String> address(String search, Geometry location, Long radius)  throws RedbackException
	{
		List<String> list = new ArrayList<String>();
		try
		{
			String url = addressUrl + "?input=" + search.replaceAll(" ", "%20") + "&key=" + apiKey;
			if(location != null) 
				url += "&location=" + location.getLatitude() + "," + location.getLongitude();
			if(radius != null) 
				url += "&radius=" + radius;
			DataMap request = new DataMap();
			request.put("method", "get");
			request.put("url", url);
			DataMap resp = requestService(request);
			if(resp.getList("predictions").size() > 0) {
				for(int i = 0; i < resp.getList("predictions").size(); i++) 
				{
					DataMap prediction = resp.getList("predictions").getObject(i);
					String address = prediction.getString("description");
					list.add(address);
				}
			} 
		}
		catch(Exception e)
		{
			throw new RedbackException("Error geocoding address", e);
		}
		return list;
	}

	protected String timezone(Geometry geometry)  throws RedbackException
	{
		String zoneId = null;
		try
		{
			DataMap request = new DataMap();
			request.put("method", "get");
			request.put("url", timezoneUrl + "?location=" + geometry.getLatitude() + "," + geometry.getLongitude());
			DataMap resp = requestService(request);
			if(resp.containsKey("timezone")) {
				zoneId = resp.getString("timezone");
			}
		}
		catch(Exception e)
		{
			throw new RedbackException("Error getting timezone", e);
		}
		return zoneId;
	}

	protected GeoRoute travel(Geometry start, Geometry end) throws RedbackException 
	{
		GeoRoute route = null;	
		if(start != null && end != null) {
			try
			{
				DataMap request = new DataMap();
				request.put("method", "get");
				request.put("url", distanceUrl + "?origins=" + start.getLatitude() + "," + start.getLongitude() + "&destinations=" + end.getLatitude() + "," + end.getLongitude() + "&key=" + apiKey);
				DataMap resp = requestService(request);
				DataList rows = resp.getList("rows");
				if(rows.size() > 0) {
					DataList elements = ((DataMap)rows.get(0)).getList("elements");
					if(elements.size() > 0) {
						DataMap element = (DataMap)elements.get(0);
						if(element.getString("status").equals("OK")) {
							DataMap rc = new DataMap();
							rc.put("start", start.toDataMap());
							rc.put("end", end.toDataMap());
							rc.put("distance", element.getNumber("distance.value").longValue());
							rc.put("duration", 1000 * element.getNumber("duration.value").longValue());
							route = new GeoRoute(rc);						
						}
					}
				}
			}
			catch(Exception e)
			{
				throw new RedbackException("Error getting distance and time", e);
			}
		} else {
			throw new RedbackException("Error getting distance; start or end cannot be null");
		}
		return route;
	}
	
	protected DataMap requestService(DataMap request) throws RedbackException {
		try {
			DataMap resp = null;
			String reqStr = request.toString(0, true);
			if(dataClient != null) {
				DataMap cachedResult = dataClient.getData(cacheCollection.getName(), new DataMap("request", reqStr), null);
				if(cachedResult.getList("result").size() > 0) {
					resp = cachedResult.getList("result").getObject(0).getObject("response");
				}
			}
			if(resp == null) {
				Payload respPayload = firebus.requestService(outboundService, new Payload(request.toString()));
				resp = new DataMap(respPayload.getString());
				if(dataClient != null) {
					dataClient.putData(cacheCollection.getName(), new DataMap("request", reqStr), new DataMap("response", resp));
				}
			}
			return resp;
		} catch(Exception e) {
			throw new RedbackException("Error requesting external geo service", e);
		}
	}
}
