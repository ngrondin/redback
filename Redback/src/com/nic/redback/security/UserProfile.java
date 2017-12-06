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
	
	public String getDefaultDomain()
	{
		return profile.getString("defaultdomain");
	}
	
	public String getSessionId()
	{
		return profile.getString("sessionid");
	}

	public JSONObject getJSON()
	{
		return profile;
	}
}
