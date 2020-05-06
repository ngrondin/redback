package io.redback.test;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class RedbackObjectProxy {
	public DataMap data;
	protected RedbackTestUnit tester;
	
	public RedbackObjectProxy(RedbackTestUnit t, DataMap d) {
		tester = t;
		data = d;
	}
	
	public String getUid() {
		return data.getString("uid");
	}
	
	public Object get(String attribute) {
		if(attribute.equals("uid")) {
			return data.getString("uid");
		} else if(attribute.indexOf(".") == -1) {
			return data.getString("data." + attribute);
		} else {
			String[] parts = attribute.split(".");
				if(data.containsKey("related") && data.containsKey("related." + parts[0])) {				
				DataEntity entity = data.get("related." + parts[0] + ".data." + parts[1]);
				if(entity instanceof DataLiteral)
					return ((DataLiteral)entity).getObject();
				else 
					return entity;
			} else {
				return null;
			}
		}
	}
	
	public void set(String username, String attribute, Object value) throws RedbackException {
		RedbackObjectProxy result = tester.updateObject(username, data.getString("objectname"), data.getString("uid"), attribute, value);
		this.data = result.data;
	}
}
