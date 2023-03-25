package io.redback.security;

import java.util.Random;

import io.firebus.script.ScriptContext;



public class Session
{
	public String id;
	public String token;
	public UserProfile userProfile;
	public UserProfile elevatedUserProfile;
	public String timezone;
	public String domainLock;
	public ScriptContext scriptContext;

	public Session() 
	{
		init(null, null, null);
	}
	
	public Session(String i)
	{
		init(i, null, null);
	}
	
	public Session(String t, UserProfile up)
	{
		init(null, t, up);
	}
	
	public Session(String i, String t, UserProfile up)
	{
		init(i, t, up);
	}
	
	protected void init(String i, String t, UserProfile up) 
	{
		if(i == null) {
			Random rnd = new Random();
			char[] array = new char[10];	
			for(int j = 0; j < array.length; j++) {
				array[j] = (char)(97 + rnd.nextInt(26));
			}
		    id = new String(array);			
		} else {
			id = i;
		}
		token = t;
		userProfile = up;	
	}
	
	public void setUserProfile(UserProfile up) 
	{
		userProfile = up;
	}
	
	public void setElevatedUserProfile(UserProfile up)
	{
		elevatedUserProfile = up;
	}
	
	public void setToken(String t)
	{
		token = t;
	}
	
	public void setTimezone(String zoneId) 
	{
		timezone = zoneId;
	}
	
	public void setDomainLock(String domain) 
	{
		domainLock = domain;
	}

	public void setScriptContext(ScriptContext sc) 
	{
		scriptContext = sc;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getToken()
	{
		return token;
	}
	
	public UserProfile getUserProfile()
	{
		return elevatedUserProfile != null ? elevatedUserProfile : userProfile;
	}
	
	public String getTimezone()
	{
		return timezone;
	}
	
	public String getDomainLock()
	{
		return domainLock;
	}
	
	public ScriptContext getScriptContext() 
	{
		return scriptContext;
	}

}
