package io.redback.utils;

import java.util.Iterator;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;

public class FilterProcessor {
	
	public static boolean apply(DataMap data, DataMap filter) {
		boolean ret = true;
		
		Iterator<String> it = filter.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			if(key.equals("$and")) {
				boolean subret = true;
				DataList list = filter.getList(key);
				for(int i = 0; i < list.size(); i++) 
					if(FilterProcessor.apply(data, list.getObject(i)) == false) 
						subret = false;
				if(subret == false)
					ret = false;
			} else if(key.equals("$or")) {
				boolean subret = false;
				DataList list = filter.getList(key);
				for(int i = 0; i < list.size(); i++) 
					if(FilterProcessor.apply(data, list.getObject(i)) == true) 
						subret = true;
				if(subret == true)
					ret = true;
			} else {
				DataEntity ent = filter.get(key);
				if(ent instanceof DataMap) {
					DataMap func = (DataMap)ent;
					String funcName = (String)func.keySet().toArray()[0];
					DataEntity funcVal = func.get(funcName);
					if(funcName.equals("$gt") && funcVal instanceof DataLiteral) {
						if(((DataLiteral)funcVal).getNumber().doubleValue() > data.getNumber(key).doubleValue())
							ret = false;
					} else if(funcName.equals("$lt") && funcVal instanceof DataLiteral) {
						if(((DataLiteral)funcVal).getNumber().doubleValue() < data.getNumber(key).doubleValue())
							ret = false;
					} else if(funcName.equals("$ne") && funcVal instanceof DataLiteral) {
						if(((DataLiteral)funcVal).getObject().equals(data.get(key)))
							ret = false;
					} else if(funcName.equals("$in") && funcVal instanceof DataList) {
						boolean subret = false;
						DataList list = (DataList)funcVal;
						for(int i = 0; i < list.size(); i++) 
							if(((DataLiteral)list.get(i)).equals(data.get(key)))
								subret = true;
						if(subret == false)
							ret = false;
					} else if(funcName.equals("$nin") && funcVal instanceof DataList) {
						DataList list = (DataList)funcVal;
						for(int i = 0; i < list.size(); i++) 
							if(((DataLiteral)list.get(i)).equals(data.get(key)))
								ret = false;
					}
					
				} else if(ent instanceof DataLiteral) {
					DataLiteral lit = (DataLiteral)ent;
					if(lit.equals(data.get(key)) == false)
						ret = false;
				} else {
					ret = false;
				}
			}			
		}		
		
		return ret;
	}

}
