package io.redback.utils;

import java.util.Arrays;

import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;

public class DataMapCompare {
	
	public static DataMap differences(DataMap map1, DataMap map2, String[] keys) {
		DataMap ret = new DataMap();
		Iterable<String> searchKeays = keys != null ? Arrays.asList(keys) : map1.keySet();
		for(String key : searchKeays) {
			DataEntity e1 = map1.get(key);
			DataEntity e2 = map2.get(key);
			if(areDataEntitiesDifferent(e1, e2)) ret.put(key, e1);
		}
		return ret;
	}
	
	public static boolean areDataEntitiesDifferent(DataEntity e1, DataEntity e2) {
		boolean diff = false;
		if(e1 == null && e2 == null) return false;
		if(e1 == null || e2 == null) return true;
		if(!e1.getClass().equals(e2.getClass())) diff = true;
		if(e1 instanceof DataLiteral) {
			if(!e1.equals(e2)) diff = true;
		} else if(e1 instanceof DataList) {
			DataList l1 = (DataList)e1;
			DataList l2 = (DataList)e2;
			if(l1.size() != l2.size()) diff = true;
			for(int i = 0; i < l1.size(); i++) {
				if(areDataEntitiesDifferent(l1.get(i), l2.get(i))) diff = true;
			}			
		} else if(e1 instanceof DataMap ) {
			DataMap subDiff = differences((DataMap)e1, (DataMap)e2, null);
			if(subDiff.keySet().size() > 0) diff = true;
		}
		return diff;
	}

}
