package io.redback.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.firebus.data.DataEntity;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.redback.security.Session;

public class RedbackAggregateRemote {
	public DataMap data;
	protected ObjectClient objectClient;
	protected Session session;
	
	public RedbackAggregateRemote(Session s, ObjectClient oc, DataMap d) {
		objectClient = oc;
		session = s;
		data = d;
	}


	public Number getMetric(String metric) {
		DataEntity de = data.get("metrics." + metric);
		if(de != null)
			if(de instanceof DataLiteral)
				return ((DataLiteral)de).getNumber();
			else
				return 0;
		else
			return 0;
	}
	
	public Object getDimension(String attribute) {
		DataEntity de = data.get("dimensions." + attribute);
		if(de != null)
			if(de instanceof DataLiteral)
				return ((DataLiteral)de).getObject();
			else
				return null;
		else
			return null;
	}
	
	
	public RedbackObjectRemote getRelated(String attribute) {
		if(data.containsKey("related")) {
			DataMap d = data.getObject("related." + attribute);
			if(d != null)
				return new RedbackObjectRemote(session, objectClient, d);
			else
				return null;
		} else {
			return null;
		}
	}
	
	public List<String> getAttributeNames() {
		List<String> ret = new ArrayList<String>();
		Iterator<String> it = data.getObject("data").keySet().iterator();
		while(it.hasNext()) {
			ret.add(it.next());
		}
		return ret;
	}
	
	
}
