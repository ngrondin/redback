package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.logging.Logger;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
import io.redback.utils.HTML;

public abstract class UIServer extends AuthenticatedServiceProvider
{
	public UIServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}
	
	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException
	{
		try
		{
			Logger.finer("rb.ui.start", null);
			Payload response = new Payload();
			String get = extractGetString(payload);
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
					Logger.finer("rb.ui.getresource", new DataMap("name", name));
					response.setData(getResource(session, name, version));
					response.metadata.put("mime", getResourceMimeType(name));
				}
				else
				{
					response.setData("<html><body>Unauthorized</body></html>");
					response.metadata.put("mime", "text/html");
					response.metadata.put("httpcode", "401");
				}
			}
			Logger.finer("rb.ui.finish", null);
			return response;
		}
		catch(DataException e)
		{
			throw new RedbackException("Error in UI server", e);
		}
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException
	{
		try
		{
			Payload response = new Payload();
			String get = extractGetString(payload);

			if(get != null)
			{
				String[] parts = get.split("/");
				String category = null;
				String version = null;
				String name = null;			
				if(parts.length >= 3)
				{
					category = parts[0];
					version = parts[1];
					name = parts[2];
				}
				if(parts.length == 2)
				{
					category = parts[0];
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
					Logger.finer("rb.ui.getresource", new DataMap("name", name));
					response.setData(getResource(session, name, version));
					response.metadata.put("mime", getResourceMimeType(name));
				}
				else if(category.equals("app"))
				{
					Logger.finer("rb.ui.getapp", new DataMap("name", name));
					response.setData(getAppClient(session, name, version).toString());
					response.metadata.put("mime", "text/html");
				}
				else if(category.equals("config"))
				{
					Logger.finer("rb.ui.getconfig", new DataMap("name", name));
					response.setData(getAppConfig(session, name).toString());
					response.metadata.put("mime", "application/json");
				}				
				else if(category.equals("menu"))
				{
					Logger.finer("rb.ui.getmenu", new DataMap("name", name));
					response.setData(getMenu(session, name).toString());
					response.metadata.put("mime", "text/html");
				}
				else if(category.equals("view"))
				{
					Logger.finer("rb.ui.getview", new DataMap("name", name));
					response.setData(getView(session, name).toString());
					response.metadata.put("mime", "application/json");						
				}
			}
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
		DataMap request = payload.getDataMap();
		if(get == null) 
			get = request.getString("get");
		if(get != null && get.startsWith("/"))
			get = get.substring(1);
		return get;
	}
	
	protected abstract HTML getAppClient(Session session, String name, String version) throws RedbackException;

	protected abstract DataMap getAppConfig(Session session, String name) throws RedbackException;

	protected abstract DataMap getMenu(Session session, String version) throws RedbackException;
	
	protected abstract DataMap getView(Session session, String viewName);

	protected abstract byte[] getResource(Session session, String name, String version) throws RedbackException;

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
	
	protected HTML formatErrorMessage(String msg, Throwable e)
	{
		HTML html = new HTML();
		html.append("<div>" + msg + "<br/>");
		if(e != null) {
			String emsg = e.getMessage();
			StackTraceElement[] elems = e.getStackTrace();
			if(elems.length > 0) {
				emsg += " (" + elems[0].getFileName() + "  " + elems[0].getLineNumber() + ")";
			}
			html.append(emsg);
			Throwable t = e;
			while((t = t.getCause()) != null)
				html.append("<br/>" + t.getMessage());
		}
		html.append("</div>");
		return html;
	}
	


}
