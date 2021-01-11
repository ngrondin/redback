package io.redback.services.impl;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.services.GeoServer;
import io.redback.utils.Geometry;

public class RedbackGeoServer extends GeoServer
{
	protected String apiKey;
	protected String outboundService;
	protected String geocodeUrl;
	protected String addressUrl;
	protected String timezoneUrl;


	public RedbackGeoServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		apiKey = config.getString("apikey");
		outboundService = config.getString("outboundservice");
		geocodeUrl = config.containsKey("geocodeurl") ? config.getString("geocodeurl") : "https://maps.googleapis.com/maps/api/geocode/json";
		addressUrl = config.containsKey("addressurl") ? config.getString("addressurl") : "https://maps.googleapis.com/maps/api/place/autocomplete/json";
		timezoneUrl = config.containsKey("timezoneurl") ? config.getString("timezoneurl") : "https://maps.googleapis.com/maps/api/timezone/json";
	}

	protected Geometry geocode(String address) throws RedbackException
	{
		Geometry geometry = null;
		try
		{
			DataMap request = new DataMap();
			request.put("method", "get");
			request.put("url", geocodeUrl + "?address=" + address.replaceAll(" ", "%20") + "&key=" + apiKey);
			Payload respPayload = firebus.requestService(outboundService, new Payload(request.toString()));
			DataMap resp = new DataMap(respPayload.getString());
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
			Payload respPayload = firebus.requestService(outboundService, new Payload(request.toString()));
			DataMap resp = new DataMap(respPayload.getString());
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
			Payload respPayload = firebus.requestService(outboundService, new Payload(request.toString()));
			DataMap resp = new DataMap(respPayload.getString());
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
			Payload respPayload = firebus.requestService(outboundService, new Payload(request.toString()));
			DataMap resp = new DataMap(respPayload.getString());
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
}
