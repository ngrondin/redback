package io.redback.utils;

import java.util.ArrayList;
import java.util.List;

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
}
