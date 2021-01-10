package io.redback.client.js;

import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.client.GeoClient;
import io.redback.utils.Geometry;
import io.redback.utils.js.JSConverter;

public class GeoClientJSWrapper implements ProxyObject {
	
	//private Logger logger = Logger.getLogger("io.redback");
	protected GeoClient geoClient;
	protected String[] members = {"geocode", "address", "timezone"};

	public GeoClientJSWrapper(GeoClient gc)
	{
		geoClient = gc;
	}
	
	public Object getMember(String key) {
		if(key.equals("geocode")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					Object arg = JSConverter.toJava(arguments[0]);
					try
					{
						if(arg instanceof DataMap) {
							String address = geoClient.geocode(new Geometry((DataMap)arg));
							return address;
						} else if(arg instanceof String) {
							Geometry geometry = geoClient.geocode((String)arg);
							return JSConverter.toJS(geometry.toDataMap());
						}
						return null;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error processing geo request", e);
					}
				}
			};
		} else if(key.equals("address")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String search = arguments[0].asString();
					Geometry location = arguments.length > 1 ? new Geometry((DataMap)JSConverter.toJava(arguments[1])) : null;
					Long radius = arguments.length > 2 ? arguments[2].asLong() : null;
					try
					{
						List<String> list = geoClient.address(search, location, radius);
						return JSConverter.toJS(list);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error getting address list", e);
					}
				}
			};
		} else if(key.equals("timezone")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					DataMap geoMap = (DataMap)JSConverter.toJava(arguments[0]);
					try
					{
						Geometry geo = new Geometry(geoMap);
						String timezone = geoClient.timezone(geo);
						return timezone;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error processing timezone request", e);
					}
				}
			};			
		} else {
			return null;
		}
	}

	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));
	}
	
	public boolean hasMember(String key) {
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
		
	}
	
	
}
