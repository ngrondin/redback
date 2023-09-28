package io.redback.managers.objectmanager.js;

import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.values.abs.SDynamicObject;
import io.firebus.script.values.abs.SValue;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.PackStream;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.utils.Convert;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.Converter;

public class PackStreamJSWrapper extends SDynamicObject
{
	protected PackStream packStream;
	protected String[] members = {"addQuery", "addList"};
	
	public PackStreamJSWrapper(PackStream ps)
	{
		packStream = ps;
	}
	
	public PackStream getPackStream()
	{
		return packStream;
	}
	
	public SValue getMember(String name)
	{
		if(name.equals("addList"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					if(arguments[0] instanceof List) {
						@SuppressWarnings("unchecked")
						List<RedbackObject> list = (List<RedbackObject>)arguments[0];
						packStream.addObjects(list);						
					}
					return null;
				}
			};				
		}
		else if(name.equals("addQuery"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					packStream.addQuery(arguments[0].toString(), (DataMap)arguments[1]);
					return null;
				}
			};				
		}
		else
		{
			return null;
		}
	}
}
