package io.redback.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataEntity;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;

public class RedbackAggregateRemote {
	public DataMap data;
	protected Firebus firebus;
	protected String objectService;
	protected String token;
	
	public RedbackAggregateRemote(Firebus fb, String os, String t, DataMap d) {
		firebus = fb;
		objectService = os;
		token = t;
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
	
	
	public RedbackAggregateRemote getRelated(String attribute) {
		if(data.containsKey("related")) {
			DataMap d = data.getObject("related." + attribute);
			if(d != null)
				return new RedbackAggregateRemote(firebus, objectService, token, d);
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
