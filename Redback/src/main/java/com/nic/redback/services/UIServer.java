package com.nic.redback.services;

import java.io.IOException;
import java.util.logging.Logger;

import javax.script.Bindings;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.utils.HTML;

public abstract class UIServer extends AuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");

	public UIServer(DataMap c, Firebus f)
	{
		super(c, f);
	}
	
	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		try
		{
			logger.finer("UI unauthenticated service start");
			Payload response = new Payload();
			String get = extractGetString(payload);
			if(get != null)
			{
				String[] parts = get.split("/");
				String category = parts[0];
				String name = parts[1];
				String version = "default";
				
				if(category.equals("resource"))
				{
					logger.finer("Get resource " + name);
					response.setData(getResource(name, version));
					response.metadata.put("mime", getResourceMimeType(name));
				}
				else
				{
					response.setData("<html><body>Unauthorized</body></html>");
					response.metadata.put("mime", "text/html");
					response.metadata.put("httpcode", "401");
				}
			}
			logger.finer("UI unauthenticated service finish");
			return response;
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
	}

	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		try
		{
			logger.finer("UI authenticated service start");
			Payload response = new Payload();
			String get = extractGetString(payload);

			if(get != null && !get.equals(""))
			{
				String[] parts = get.split("/");
				String category = null;
				String version = null;
				String name = null;
				if(parts.length >= 3)
				{
					version = parts[0];
					category = parts[1];
					name = parts[2];
				}
				if(parts.length == 2)
				{
					version = "default";
					category = parts[0];
					name = parts[1];
				}
				else if(parts.length == 1)
				{
					version = "default";
					category = "app";
					name = parts[0];
				}
				
				if(category.equals("resource"))
				{
					logger.finer("Get resource " + name);
					response.setData(getResource(name, version));
					response.metadata.put("mime", getResourceMimeType(name));
				}
				else if(category.equals("app"))
				{
					logger.finer("Get app " + name);
					response.setData(getApp(session, name, version).toString());
					response.metadata.put("mime", "text/html");
				}
				else if(category.equals("view"))
				{
					logger.finer("Get view " + name);
					response.setData(getView(session, name, version, null).toString());
					response.metadata.put("mime", "text/html");
				}
			}
			logger.finer("UI authenticated service finish");
			return response;
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}
	
	protected String extractGetString(Payload payload) throws DataException
	{
		String get = payload.metadata.get("get");
		if(payload.getString().length() > 0)
		{
			DataMap request = new DataMap(payload.getString());
			if(get == null) 
				get = request.getString("get");
		}
		if(get.startsWith("/"))
			get = get.substring(1);
		return get;
	}
	
	protected abstract HTML getApp(Session session, String name, String version) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException;
	
	protected abstract HTML getMenu(Session session, String version) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException;
	
	protected abstract HTML getView(Session session, String viewName, String version, Bindings context);
	
	protected abstract byte[] getResource(String name, String version) throws FunctionErrorException, FunctionTimeoutException, DataException, RedbackException, IOException;

	protected String getResourceMimeType(String name)
	{
		String mime = null;
		if(name.endsWith(".js"))
			mime = "application/javascript";
		else if(name.endsWith(".css"))
			mime = "text/css";
		else if(name.endsWith(".svg"))
			mime = "image/svg+xml";
		else if(name.endsWith(".ico"))
			mime = "image/x-icon";
		else if(name.endsWith(".png"))
			mime = "image/png";
		return mime;
	}
	
	protected HTML formatErrorMessage(String msg, Exception e)
	{
		HTML html = new HTML();
		html.append("<div>" + msg + "<br/>");
		html.append(e.getMessage());
		Throwable t = e;
		while((t = t.getCause()) != null)
			html.append("<br/>" + t.getMessage());
		html.append("</div>");
		return html;
	}
	


}
