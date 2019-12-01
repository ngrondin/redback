package com.nic.redback.security;

import java.util.ArrayList;

import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class UserProfile 
{
	protected DataMap profile;
	
	public UserProfile(DataMap p)
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
		DataList list = profile.getList("roles");
		for(int i = 0; i < list.size(); i++)
			roles.add(list.getString(i));
		return roles;
	}

	public ArrayList<String> getDomains()
	{
		ArrayList<String> domains = new ArrayList<String>();
		DataList list = profile.getList("domains");
		for(int i = 0; i < list.size(); i++)
			domains.add(list.getString(i));
		return domains;
	}
	
	public boolean hasAllDomains()
	{
		boolean hasAllDomains = false;
		
		DataList list = profile.getList("domains");
		for(int i = 0; i < list.size(); i++)
			if(list.getString(i).equals("*"))
				hasAllDomains = true;
		return hasAllDomains;
	}
	
	public DataMap getDBFilterDomainClause()
	{
		DataMap inClause = new DataMap();
		if(profile.containsKey("domains"))
			inClause.put("$in", profile.getList("domains"));
		else
			inClause.put("$in", new DataList());
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

	public DataMap getJSON()
	{
		return profile;
	}
	
	public DataMap getSimpleJSON()
	{
		DataMap ret = new DataMap();
		ret.put("username", getUsername());
		ret.put("attributes", profile.getObject("attributes").getCopy());
		return ret;
	}
}
