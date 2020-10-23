package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.HTML;
import io.redback.utils.Timer;

public abstract class UIServer extends AuthenticatedServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");

	public UIServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}
	
	public Payload unAuthenticatedService(Session session, Payload payload) throws RedbackException
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
				String version = null;
				
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
		catch(DataException e)
		{
			throw new RedbackException("Error in UI server", e);
		}
	}

	public Payload authenticatedService(Session session, Payload payload) throws RedbackException
	{
		try
		{
			logger.finer("UI authenticated service start");
			Payload response = new Payload();
			String get = extractGetString(payload);
			Timer timer = new Timer("rbui", get);

			if(get != null)
			{
				String[] parts = get.split("/");
				String category = null;
				String name = null;
				String version = null;
				if(parts.length >= 3)
				{
					category = parts[0];
					version = parts[1];
					name = parts[2];
				}
				if(parts.length == 2)
				{
					category = parts[0];
					//version = "default";
					name = parts[1];
				}
				else if(parts.length == 1)
				{
					category = "app";
					if(!parts[0].equals(""))
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
					logger.finer("Get app " + (name != null ? name : "?"));
					response.setData(getApp(session, name, version).toString());
					response.metadata.put("mime", "text/html");
				}
				else if(category.equals("menu"))
				{
					logger.finer("Get menu " + name);
					response.setData(getMenu(session, version).toString());
					response.metadata.put("mime", "text/html");
				}
				else if(category.equals("view"))
				{
					logger.finer("Get view " + name);
					response.setData(getView(session, name, version).toString());
					response.metadata.put("mime", "text/html");
				}
			}
			logger.finer("UI authenticated service finish");
			timer.mark();
			return response;
		}
		catch(DataException e)
		{
			throw new RedbackException("Error in UI server", e);
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
	
	protected abstract HTML getApp(Session session, String name, String version) throws RedbackException;
	
	protected abstract HTML getMenu(Session session, String version) throws RedbackException;
	
	protected abstract HTML getView(Session session, String viewName, String version);
	
	protected abstract byte[] getResource(String name, String version) throws RedbackException;

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
		else if(name.endsWith(".apk"))
			mime = "application/vnd.android.package-archive";
		else if(name.endsWith(".plist"))
			mime = "application/xml";
		else
			mime = "application/octet-stream";
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
