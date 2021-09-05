package io.redback.utils;

import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;

public class KeyEscaper {

	public static DataMap escape(DataMap map) {
		DataMap ret = new DataMap();
		for(String key: map.keySet()) {
			String newKey = key;
			if(newKey.startsWith("_$")) {
				newKey = newKey.substring(1);
			}
			ret.put(newKey, escapeDE(map.get(key)));
		}
		return ret;
	}
	
	public static DataEntity escapeDE(DataEntity ent) {
		DataEntity ret = null;
		if(ent instanceof DataMap) {
			ret = escape((DataMap)ent);
		} else if(ent instanceof DataList) {
			DataList list = (DataList)ent;
			DataList newList = new DataList();
			for(int i = 0; i < list.size(); i++) {
				newList.add(escapeDE(list.get(i)));
			}
			ret = newList;
		} else if(ent instanceof DataLiteral) {
			ret = ent;
		}
		return ret;
	}
}
