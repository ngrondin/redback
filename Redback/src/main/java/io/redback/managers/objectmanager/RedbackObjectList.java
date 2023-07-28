package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedbackObjectList extends ArrayList<RedbackObject> {
	private static final long serialVersionUID = 1L;
	private Map<String, RedbackObject> map = new HashMap<String, RedbackObject>(); 

	public boolean add(RedbackObject rbo) {
		if(rbo != null) {
			String key = rbo.getKey();
			if(!map.containsKey(key)) {
				map.put(key, rbo);
				return super.add(rbo);
			} 			
		}
		return false;
	}
	
	public boolean addAll(List<RedbackObject> list) {
		boolean ret = false;
		for(RedbackObject rbo : list) 
			ret = ret || add(rbo);
		return ret;
	}
}
