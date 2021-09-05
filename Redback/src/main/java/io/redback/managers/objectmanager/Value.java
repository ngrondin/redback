package io.redback.managers.objectmanager;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.data.ZonedTime;


public class Value
{
	protected String stringValue;
	protected Number numberValue;
	protected Boolean boolValue;
	protected Date dateValue;
	protected ZonedTime timeValue;
	protected DataMap mapValue;
	protected DataList listValue;
	protected Class<?> valueClass;

	
	public Value(Object v)
	{
		if(v instanceof String)
		{
			valueClass = String.class;
			stringValue = (String)v;
		}
		else if(v instanceof Number)
		{
			valueClass = Number.class;
			numberValue = (Number)v;
		}
		else if(v instanceof Boolean)
		{
			valueClass = Boolean.class;
			boolValue = (Boolean)v;
		}
		else if(v instanceof Date)
		{
			valueClass = Date.class;
			dateValue = (Date)v;
		}
		else if(v instanceof DataLiteral)
		{
			DataLiteral l = (DataLiteral)v;
			if(l.getType() == DataLiteral.TYPE_STRING)
			{
				valueClass = String.class;
				stringValue = l.getString();
			}
			else if(l.getType() == DataLiteral.TYPE_NUMBER)
			{
				valueClass = Number.class;
				numberValue = l.getNumber();
			}
			else if(l.getType() == DataLiteral.TYPE_BOOLEAN)
			{
				valueClass = Boolean.class;
				boolValue = l.getBoolean();
			}
			else if(l.getType() == DataLiteral.TYPE_DATE)
			{
				valueClass = Date.class;
				dateValue = l.getDate();
			}
			else if(l.getType() == DataLiteral.TYPE_TIME)
			{
				valueClass = ZonedTime.class;
				timeValue = l.getTime();
			}
		}
		else if(v instanceof DataMap)
		{
			valueClass = DataMap.class;
			mapValue = (DataMap)v;
		}
		else if(v instanceof DataList)
		{
			valueClass = DataList.class;
			listValue = (DataList)v;
		}
	}
	
	public Object getObject()
	{
		if(valueClass == String.class)
			return stringValue;
		else if(valueClass == Number.class)
			return numberValue;
		else if(valueClass == Boolean.class)
			return boolValue;
		else if(valueClass == Date.class)
			return dateValue;
		else if(valueClass == ZonedTime.class)
			return timeValue;
		else if(valueClass == DataMap.class)
			return mapValue;
		else if(valueClass == DataList.class)
			return listValue;
		return null;		
	}
	
	public String getString()
	{
		if(valueClass == String.class)
			return stringValue;
		else if(valueClass == Number.class)
			return "" + numberValue;
		else if(valueClass == Boolean.class)
			return "" + boolValue;
		else if(valueClass == Date.class)
			return dateValue.toString();
		else if(valueClass == ZonedTime.class)
			return timeValue.toString();
		else if(valueClass == DataMap.class)
			return mapValue.toString();
		else if(valueClass == DataList.class)
			return listValue.toString();
		return "";
	}

	public boolean getBoolean()
	{
		if(valueClass == Boolean.class)
			return boolValue;
		return false;
	}
	
	public Number getNumber()
	{
		if(valueClass == String.class)
			try
			{
				return Double.parseDouble(stringValue);
			}
			catch(NumberFormatException e)
			{
				return 0;
			}
		else if(valueClass == Number.class)
			return numberValue;
		else if(valueClass == Boolean.class)
			return boolValue == true ? 1 : 0;
		else if(valueClass == Date.class)
			return dateValue.getTime();
		return 0;
	}
	
	public Date getDate()
	{
		if(valueClass == String.class)
			return null;
		else if(valueClass == Number.class)
			return new Date(numberValue.longValue());
		else if(valueClass == Boolean.class)
			return null;
		else if(valueClass == Date.class)
			return dateValue;
		else if(valueClass == ZonedTime.class)
			return Date.from(timeValue.atDate(ZonedDateTime.now()).toInstant());
		return null;
	}
	
	public ZonedTime getTime()
	{
		if(valueClass == Date.class)
			return new ZonedTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(((Date)dateValue).getTime()), ZoneId.systemDefault()));
		else if(valueClass == ZonedTime.class)
			return timeValue;
		return null;
	}
	
	public DataLiteral getJSONLiteral()
	{
		if(valueClass == String.class)
			return new DataLiteral(stringValue);
		else if(valueClass == Number.class)
			return new DataLiteral(boolValue);
		else if(valueClass == Boolean.class)
			return new DataLiteral(numberValue);
		return null;
	}
	
	public boolean isNull()
	{
		return valueClass == null;
	}
	
	public boolean equalsIgnoreCase(Object o)
	{
		if(o instanceof Value)
			return _equals((Value)o, true);
		else
			return _equals(new Value(o), true);
	}

	public boolean equals(Object o)
	{
		if(o instanceof Value)
			return _equals((Value)o, false);
		else
			return _equals(new Value(o), false);
	}
	
	protected boolean _equals(Value v, boolean ignoreCase)
	{
		if(v == null)
		{
			if(valueClass == null)
				return true;
		}
		else
		{
			if(valueClass == null  &&  v.getValueClass() == null)
			{
				return true;
			}
			else if(valueClass == v.getValueClass())
			{
				if(valueClass == String.class)
					if(ignoreCase == false) {
						if(getString().equals(v.getString()))
							return true;
					} else {
						if(getString().equalsIgnoreCase(v.getString()))
							return true;
					}
				if(valueClass == Boolean.class)
					if(getBoolean() == v.getBoolean())
							return true;
				if(valueClass == Number.class)
					if(getNumber().equals(v.getNumber()))
							return true;
				if(valueClass == Date.class)
					if(getNumber().equals(v.getNumber()))
							return true;
				if(valueClass == ZonedTime.class)
					if(getDate().getTime() == v.getDate().getTime())
							return true;
			}
			else if(valueClass == String.class && v.getValueClass() == Number.class)
			{
				if(getString().equals(v.getString()))
					return true;
			}
			else if(valueClass == Number.class && v.getValueClass() == String.class)
			{
				if(v.getString().equals(getString()))
					return true;
			}
		}
		return false;
	}

	public Class<?> getValueClass()
	{
		return valueClass;
	}
	
	public String toString()
	{
		return "(" + (valueClass == null ? "Null" : valueClass.getSimpleName()) + ") " + getString(); 
	}
}
