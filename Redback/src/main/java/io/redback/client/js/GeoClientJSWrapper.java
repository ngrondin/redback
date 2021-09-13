package io.redback.client.js;


import io.firebus.data.DataMap;
import io.redback.client.GeoClient;
import io.redback.exceptions.RedbackException;
import io.redback.utils.GeoRoute;
import io.redback.utils.Geometry;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class GeoClientJSWrapper extends ObjectJSWrapper {
	protected GeoClient geoClient;
	
	public GeoClientJSWrapper(GeoClient gc)
	{
		super(new String[] {"geocode", "address", "timezone", "travel"});
		geoClient = gc;
	}
	
	public Object get(String key) {
		if(key.equals("geocode")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					Object arg = arguments[0];
					if(arg instanceof DataMap) {
						String address = geoClient.geocode(new Geometry((DataMap)arg));
						return address;
					} else if(arg instanceof String) {
						Geometry geometry = geoClient.geocode((String)arg);
						return geometry.toDataMap();
					}
					return null;
				}
			};
		} else if(key.equals("address")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String search = (String)arguments[0];
					Geometry location = arguments.length > 1 ? new Geometry((DataMap)(arguments[1])) : null;
					Long radius = arguments.length > 2 ? (Long)arguments[2] : null;
					return geoClient.address(search, location, radius);
				}
			};
		} else if(key.equals("timezone")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap geoMap = (DataMap)(arguments[0]);
					Geometry geo = new Geometry(geoMap);
					String timezone = geoClient.timezone(geo);
					return timezone;
				}
			};			
		} else if(key.equals("travel")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap start = (DataMap)(arguments[0]);
					DataMap end = (DataMap)(arguments[1]);
					GeoRoute route = geoClient.travel(new Geometry(start), new Geometry(end));
					return route.toDataMap();
				}
			};			
		} else {
			return null;
		}
	}


}
