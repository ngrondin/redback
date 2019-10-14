package com.nic.redback.security;

import java.util.ArrayList;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class UserProfile 
{
	protected JSONObject profile;
	
	public UserProfile(JSONObject p)
	{
		profile = p;
	}
	
	public String getUsername()
	{
		return profile.getString("username");
	}
	
	public String getPasswordHash()
	{
		return profile.getString("passwordhash");
	}

	
	public ArrayList<String> getRoles()
	{
		ArrayList<String> roles = new ArrayList<String>();
		JSONList list = profile.getList("roles");
		for(int i = 0; i < list.size(); i++)
			roles.add(list.getString(i));
		return roles;
	}

	public ArrayList<String> getDomains()
	{
		ArrayList<String> domains = new ArrayList<String>();
		JSONList list = profile.getList("domains");
		for(int i = 0; i < list.size(); i++)
			domains.add(list.getString(i));
		return domains;
	}
	
	public JSONObject getDBFilterDomainClause()
	{
		JSONObject inClause = new JSONObject();
		inClause.put("$in", profile.getList("domains"));
		return inClause;
	}
	
	public String getAttribute(String name)
	{
		return profile.getString("attributes." + name);
	}

	public String getRights(String name)
	{
		return profile.getString("rights." + name);
	}
	
	public boolean canRead(String name)
	{
		String r = getRights(name);
		if(r != null  &&  r.toLowerCase().contains("r"))
			return true;
		else
			return false;
	}

	public boolean canWrite(String name)
	{
		String r = getRights(name);
		if(r != null  &&  r.toLowerCase().contains("w"))
			return true;
		else
			return false;
	}

	public boolean canExecute(String name)
	{
		String r = getRights(name);
		if(r != null  &&  r.toLowerCase().contains("x"))
			return true;
		else
			return false;
	}

	public JSONObject getJSON()
	{
		return profile;
	}
	
	public JSONObject getSimpleJSON()
	{
		JSONObject ret = new JSONObject();
		ret.put("username", getUsername());
		ret.put("attributes", profile.getObject("attributes").getCopy());
		return ret;
	}
}
