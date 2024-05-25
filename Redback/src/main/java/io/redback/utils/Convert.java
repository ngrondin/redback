package io.redback.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class Convert {

	public static List<DataMap> dataListToList(DataList list) {
		List<DataMap> ret = new ArrayList<DataMap>();
		for(int i = 0; i < list.size(); i++)
			ret.add(list.getObject(i));
		return ret;
	}
	
	public static DataList listToDataList(List<?> list) {
		DataList ret = new DataList();
		for(Object o: list) 
			ret.add(o);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static DataMap mapToDataMap(Map<String, ?> map) {
		DataMap dataMap = new DataMap();
		for(String key: map.keySet()) {
			Object val = map.get(key);
			if(val instanceof Map) {
				dataMap.put(key, mapToDataMap((Map<String, ?>)val));
			} else if(val instanceof List) {
				dataMap.put(key, listToDataList((List<?>)val));
			} else {
				dataMap.put(key, val);
			}
		}
		return dataMap;
	}
}
