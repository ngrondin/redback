package io.redback.client;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.GeoRoute;
import io.redback.utils.Geometry;

public class GeoClient extends Client {

	public GeoClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public Geometry geocode(Session session, String address) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "geocode");
			req.put("address", address);
			DataMap resp = requestDataMap(session, req);
			return new Geometry(resp.getObject("geometry"));
		} catch(Exception e) {
			throw new RedbackException("Error geocoding", e);
		}
	}

	public String geocode(Session session, Geometry geometry) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "geocode");
			req.put("geometry", geometry.toDataMap());
			DataMap resp = requestDataMap(session, req);
			return resp.getString("address");
		} catch(Exception e) {
			throw new RedbackException("Error geocoding", e);
		}		
	}
	
	public List<String> address(Session session, String search, Geometry location, Long radius) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "address");
			req.put("search", search);
			if(location != null)
				req.put("location", location.toDataMap());
			if(radius != null)
				req.put("radius", radius);
			DataMap resp = requestDataMap(session, req);
			List<String> list = new ArrayList<String>();
			for(int i = 0; i < resp.getList("result").size(); i++) {
				list.add(resp.getList("result").getString(i));
			}
			return list;
		} catch(Exception e) {
			throw new RedbackException("Error geocoding", e);
		}		
	}	
	
	public String timezone(Session session, Geometry geometry) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "timezone");
			req.put("geometry", geometry.toDataMap());
			DataMap resp = requestDataMap(session, req);
			return resp.getString("timezone");
		} catch(Exception e) {
			throw new RedbackException("Error getting timezone", e);
		}		
	}
	
	public GeoRoute travel(Session session, Geometry start, Geometry end) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "travel");
			req.put("start", start.toDataMap());
			req.put("end", end.toDataMap());
			DataMap resp = requestDataMap(session, req);
			return new GeoRoute(resp);
		} catch(Exception e) {
			throw new RedbackException("Error getting travel distance and time", e);
		}		
	}
}
