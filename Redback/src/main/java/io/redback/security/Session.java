package io.redback.security;

import java.util.Random;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.ScriptContext;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackUnauthorisedException;
import io.redback.utils.TxStore;



public class Session
{
	public String id;
	public String token;
	public UserProfile userProfile;
	public UserProfile elevatedUserProfile;
	public String timezone;
	public String domainLock;
	public int domainLockLevel = 0;
	public ScriptContext scriptContext;
	public int scriptLevel = 0;
	public TxStore<Object> txStore;
	public DataMap stats;
	public DataMap data;

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
		stats = new DataMap();
		data = new DataMap();
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

	public void setScriptContext(ScriptContext sc) 
	{
		scriptContext = sc;
	}
	
	public void setTxStore(TxStore<Object> txs)
	{
		txStore = txs;
	}
	
	public void setStat(String name, Object value) 
	{
		stats.put(name, value);
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
	
	public void pushDomainLock(String domain) throws RedbackException
	{
		if(domainLock != null && !domain.equals(domainLock)) {
			throw new RedbackException("Domain already locked to '" + domainLock + "' but tried to lock with '" + domain + "'");
		} else if(domainLock == null && !userProfile.hasDomain(domain)) {
			throw new RedbackUnauthorisedException("Cannot lock to domain " + domain);
		} else {
			domainLock = domain;
			domainLockLevel++;
		}
	}
	
	public void popDomainLock() 
	{
		domainLockLevel--;
		if(domainLockLevel == 0) domainLock = null;
	}
	
	public String getDomainLock()
	{
		return domainLock;
	}
	
	public DataMap getDomainFilterClause()
	{
		if(domainLock != null) {
			DataList list = new DataList();
			list.add(domainLock);
			list.add("root");
			return new DataMap("$in", list);
		} else {
			return getUserProfile().getDBFilterDomainClause();
		}
	}
	
	public boolean hasAccessToDomain(String domain) 
	{
		if(domainLock != null) {
			return domainLock.equals(domain);
		} else {
			return getUserProfile().hasDomain(domain);
		}
	}
	
	public ScriptContext getScriptContext() 
	{
		return scriptContext;
	}
	
	public TxStore<Object> getTxStore() 
	{
		return txStore;
	}
	
	public boolean hasTxStore() 
	{
		return txStore != null;
	}
	
	public void setData(String k, Object v) 
	{
		data.put(k, v);
	}
	
	public Object getData(String k) 
	{
		return data.get(k);
	}

	public DataMap getStats() 
	{
		return stats;
	}
	
	public void pushScriptLevel() 
	{
		scriptLevel++;
	}
	
	public void popScriptLevel() 
	{
		scriptLevel--;
	}
	
	public boolean isInScript() 
	{
		return scriptLevel > 0;
	}
	
	
}
