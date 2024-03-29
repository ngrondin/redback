package io.redback.managers.objectmanager;

import java.util.Set;

import io.firebus.data.DataMap;
import io.firebus.script.ScriptContext;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public abstract class RedbackElement 
{
	protected Session session;
	protected ObjectManager objectManager;
	protected ObjectConfig config;
	protected ScriptContext scriptContext;

	public ObjectConfig getObjectConfig()
	{
		return config;
	}
	
	public ObjectManager getObjectManager()
	{
		return objectManager;
	}
	
	public Session getUserSession()
	{
		return session;
	}
	
	public ScriptContext getScriptContext()
	{
		return scriptContext;
	}

	public abstract Set<String> getAttributeNames(); 
	
	public abstract Value get(String name) throws RedbackException;
	
	public abstract DataMap getRelatedFindFilter(String attributeName) throws RedbackException;
	
	public abstract void setRelated(String name, RedbackObject relatedObject) throws RedbackException;
}
