package io.redback.utils.js;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SInternalObject;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.managers.objectmanager.js.RedbackAggregateJSWrapper;
import io.redback.managers.objectmanager.js.RedbackObjectJSWrapper;
import io.redback.utils.dataset.DataSet;
import io.redback.utils.dataset.js.DataSetJSWrapper;

public class Converter {
	public static SValue tryConvertIn(Object o) {
		try {
			return convertIn(o);
		} catch(ScriptException e) {
			return null;
		}
	}

	public static SValue convertIn(Object o) throws ScriptValueException {
		if(o instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>)o;
			SInternalObject io = new SInternalObject();
			for(String key: m.keySet()) 
				io.putMember(key, convertIn(m.get(key)));
			return io;
		} else if(o instanceof DataList) {
			DataList list = (DataList)o;
			SArray a = new SArray();
			for(int i = 0; i < list.size(); i++)
					a.set(i, convertIn(list.get(i)));
			return a;
		} else if(o instanceof List) {
			List<?> list = (List<?>)o;
			SArray a = new SArray();
			for(int i = 0; i < list.size(); i++) 
				a.set(i, convertIn(list.get(i)));
			return a;	
		} else if(o instanceof RedbackObject) {
			return new RedbackObjectJSWrapper((RedbackObject)o);
		} if(o instanceof RedbackAggregate) {
			return new RedbackAggregateJSWrapper((RedbackAggregate)o);			
		} else if(o instanceof DataSet) {
			return new DataSetJSWrapper((DataSet)o);
		} else {
			return io.firebus.script.Converter.convertIn(o);
		}
	}
	
	public static Object tryConvertOut(SValue v) {
		try {
			return convertOut(v);
		} catch(ScriptException e) {
			return null;
		}
	}
	
	public static Object convertOut(SValue v) throws ScriptException {
		 if(v instanceof SArray) {
			SArray a = (SArray)v;
            SValue v1 = a.getSize() > 0 ? a.get(0) : null;
            if(v1 != null && v1 instanceof RedbackObjectJSWrapper) {
                List<Object> list = new ArrayList<Object>();
                for(int i = 0; i < a.getSize(); i++)
                    list.add(convertOut(a.get(i)));
                return list;    
            } else {
            	DataList list = new DataList();
    			for(int i = 0; i < a.getSize(); i++)
    				list.add(convertOut(a.get(i)));
    			return list;
            }
		} else if(v instanceof SInternalObject) {
			SObject o = (SInternalObject)v;
			DataMap map = new DataMap();
			String[] keys = o.getMemberKeys();
			if(keys != null) {
				for(int i = 0; i < keys.length; i++) {
					SValue prop = o.getMember(keys[i]);
					if(!(prop instanceof SCallable))
						map.put(keys[i], convertOut(o.getMember(keys[i])));
				}
			}
			return map;			
		} else if(v instanceof RedbackObjectJSWrapper) {
			return ((RedbackObjectJSWrapper)v).getRedbackObject();
		} if(v instanceof RedbackAggregateJSWrapper) {
			return ((RedbackAggregateJSWrapper)v).getRedbackAggregate();
		} else if(v instanceof DataSetJSWrapper) {
			return ((DataSetJSWrapper)v).getDataSet();
		} else {
			return io.firebus.script.Converter.convertOut(v);
		}
	}
}
