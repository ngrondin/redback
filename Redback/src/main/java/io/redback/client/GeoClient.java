package io.redback.client;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.Geometry;

public class GeoClient extends Client {

	public GeoClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public Geometry geocode(String address) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "geocode");
			req.put("address", address);
			DataMap resp = request(req);
			return new Geometry(resp.getObject("geometry"));
		} catch(Exception e) {
			throw new RedbackException("Error geocoding", e);
		}
	}

	public String geocode(Geometry geometry) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "geocode");
			req.put("geometry", geometry.toDataMap());
			DataMap resp = request(req);
			return resp.getString("address");
		} catch(Exception e) {
			throw new RedbackException("Error geocoding", e);
		}		
	}
	
	public List<String> address(String search, Geometry location, Long radius) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "address");
			req.put("search", search);
			if(location != null)
				req.put("location", location.toDataMap());
			if(radius != null)
				req.put("radius", radius);
			DataMap resp = request(req);
			List<String> list = new ArrayList<String>();
			for(int i = 0; i < resp.getList("result").size(); i++) {
				list.add(resp.getList("result").getString(i));
			}
			return list;
		} catch(Exception e) {
			throw new RedbackException("Error geocoding", e);
		}		
	}	
}
