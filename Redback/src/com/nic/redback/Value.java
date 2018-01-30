package com.nic.redback;

import java.util.Date;

import com.nic.firebus.utils.JSONLiteral;
import com.nic.firebus.utils.JSONObject;

public class Value
{
	protected String stringValue;
	protected Number numberValue;
	protected Boolean boolValue;
	protected Date dateValue;
	protected JSONObject jsonObjectValue;
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
		else if(v instanceof JSONLiteral)
		{
			JSONLiteral l = (JSONLiteral)v;
			if(l.getType() == JSONLiteral.TYPE_STRING)
			{
				valueClass = String.class;
				stringValue = l.getString();
			}
			else if(l.getType() == JSONLiteral.TYPE_NUMBER)
			{
				valueClass = Number.class;
				numberValue = l.getNumber();
			}
			else if(l.getType() == JSONLiteral.TYPE_BOOLEAN)
			{
				valueClass = Boolean.class;
				boolValue = l.getBoolean();
			}
			else if(l.getType() == JSONLiteral.TYPE_DATE)
			{
				valueClass = Date.class;
				dateValue = l.getDate();
			}
		}
		else if(v instanceof JSONObject)
		{
			valueClass = JSONObject.class;
			jsonObjectValue = (JSONObject)v;
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
		else if(valueClass == JSONObject.class)
			return jsonObjectValue;
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
		else if(valueClass == JSONObject.class)
			return jsonObjectValue.toString();
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
		return null;
	}
	
	public JSONLiteral getJSONLiteral()
	{
		if(valueClass == String.class)
			return new JSONLiteral(stringValue);
		else if(valueClass == Number.class)
			return new JSONLiteral(boolValue);
		else if(valueClass == Boolean.class)
			return new JSONLiteral(numberValue);
		return null;
	}
	
	public boolean isNull()
	{
		return valueClass == null;
	}
	
	public boolean equals(Value v)
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
					if(getString().equals(v.getString()))
							return true;
				if(valueClass == Boolean.class)
					if(getBoolean() == v.getBoolean())
							return true;
				if(valueClass == Number.class)
					if(getNumber().equals(v.getNumber()))
							return true;
				if(valueClass == Date.class)
					if(getNumber().equals(v.getNumber()))
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
