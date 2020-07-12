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

	public RedbackGeoServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		apiKey = config.getString("apikey");
		outboundService = config.getString("outboundservice");
	}

	protected Geometry geocode(String address) throws RedbackException
	{
		Geometry geometry = null;
		try
		{
			DataMap request = new DataMap();
			request.put("method", "get");
			request.put("url", "https://maps.googleapis.com/maps/api/geocode/json?address=" + address.replaceAll(" ", "%20") + "&key=" + apiKey);
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
			request.put("url", "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + geometry.getLatitude() + "," + geometry.getLongitude() + "&key=" + apiKey);
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

	protected List<String> address(String search)  throws RedbackException
	{
		List<String> list = new ArrayList<String>();
		try
		{
			DataMap request = new DataMap();
			request.put("method", "get");
			request.put("url", "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + search.replaceAll(" ", "%20") + "&key=" + apiKey);
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

}
