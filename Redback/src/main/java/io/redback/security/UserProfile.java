package io.redback.security;

import java.util.ArrayList;

import io.firebus.data.DataEntity;
import io.firebus.data.DataFilter;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;

public class UserProfile 
{
	protected DataMap profile;
	protected DataList domains = new DataList();;
	protected boolean hasAllDomains = false;
	
	public UserProfile(DataMap p)
	{
		profile = p;
		domains.add("root");
		if(profile.getList("domains") != null) {
			for(int i = 0; i < profile.getList("domains").size(); i++) {
				String d = profile.getList("domains").getString(i);
				if(d.equals("*")) 
					hasAllDomains = true;
				else if(!domains.contains(d))
					domains.add(d);
			}			
		}	
	}
	
	public String getUsername()
	{
		return profile.getString("username");
	}
	
	public long getExpiry()
	{
		return profile.getNumber("expiry").longValue();
	}
	
	public void setExpiry(long l)
	{
		profile.put("expiry", l);
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
	
	public String getDefaultDomain()
	{
		String domain = getAttribute("rb.defaultdomain");
		if(domain == null && profile.containsKey("domains")) {
			DataList list = profile.getList("domains");
			if(list.size() > 0) 
				domain = list.getString(0);
		}
		return domain;
	}
	
	public boolean hasAllDomains()
	{
		return hasAllDomains;
	}
	
	public boolean hasDomain(String domain)
	{
		return hasAllDomains || domains.contains(domain);
	}
	
	public DataMap getDBFilterDomainClause()
	{
		if(hasAllDomains) {
			return null;
		} else {
			return new DataMap("$in", domains);
		}
	}
	
	public String getAttribute(String name)
	{
		return profile.getString("attributes." + name);
	}

	public boolean getRights(String name, String operation, DataMap context)
	{
		String op = operation.equalsIgnoreCase("read") ? "r" : operation.equalsIgnoreCase("write") ? "w" : operation.equalsIgnoreCase("execute") ? "x" : "";
		DataEntity cfg = profile.get("rights." + name);
		if(cfg instanceof DataLiteral) {
			DataLiteral lit = (DataLiteral)cfg;
			if(lit.getType() == DataLiteral.TYPE_BOOLEAN) {
				return lit.getBoolean();
			} else if(lit.getType() == DataLiteral.TYPE_STRING) {
				String str = lit.getString();
				if(str.contains(op))
					return true;
				else 
					return false;
			}
		} else if(cfg instanceof DataMap) {
			DataEntity opCfg = ((DataMap)cfg).get(operation);
			if(opCfg instanceof DataLiteral) {
				return ((DataLiteral)opCfg).getBoolean();
			} else if(opCfg instanceof DataMap) {
				if(context != null) {
					DataFilter filter = new DataFilter((DataMap)cfg); //This is not correct at the DataMap will be an ExpressionMap
					return filter.apply(context);
				} else {
					return true;
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean canRead(String name)
	{
		return getRights(name, "read", null);
	}

	public boolean canWrite(String name)
	{
		return getRights(name, "write", null);
	}

	public boolean canExecute(String name)
	{
		return getRights(name, "execute", null);
	}

	public DataMap getReadFilter(String name) 
	{
		DataEntity cfg = profile.get("rights." + name);
		if(cfg instanceof DataMap) {
			DataEntity opCfg = ((DataMap)cfg).get("read");
			if(opCfg instanceof DataMap) {
				return (DataMap)opCfg;
			}
		}
		return null;		
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
