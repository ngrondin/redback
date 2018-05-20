package com.nic.redback.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.utils.RedbackStringBuilder;
import com.nic.redback.utils.StringUtils;

public class UIServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String resourceService;
	protected String resourceServiceType;
	protected ScriptEngine jsEngine;
	protected HashMap<String, CompiledScript> jspScripts;
	protected HashMap<String, JSONObject> viewConfigs;


	public UIServer( JSONObject c)
	{
		super(c);
		configService = config.getString("configservice");
		resourceServiceType = config.getString("resourceservicetype");
		resourceService = config.getString("resourceservice");
		jsEngine = new ScriptEngineManager().getEngineByName("javascript");
		jspScripts = new HashMap<String, CompiledScript>();
		viewConfigs = new HashMap<String, JSONObject>();
	}
	
	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		try
		{
			logger.info("UI unauthenticated service start");
			Payload response = new Payload();
			String get = extractGetString(payload);
			if(get != null)
			{
				String[] parts = get.split("/");
				String category = parts[0];
				String name = parts[1];
				
				if(category.equals("resource"))
				{
					logger.info("Get resource " + name);
					response.setData(getResource(name));
					response.metadata.put("mime", getResourceMimeType(name));
				}
				else if(category.equals("app"))
				{
					Bindings context = jsEngine.createBindings();
					context.put("get", "app/" + name);
					response.setData(executeJSP(("pages/login"), context));
				}
				else
				{
					throw new FunctionErrorException("This request requires authentication");
				}
			}
			logger.info("UI unauthenticated service finish");
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
			logger.info("UI authenticated service start");
			Payload response = new Payload();
			String get = extractGetString(payload);

			if(get != null)
			{
				String[] parts = get.split("/");
				String category = parts[0];
				String name = parts[1];
				
				if(category.equals("resource"))
				{
					logger.info("Get resource " + name);
					response.setData(getResource(name));
					response.metadata.put("mime", getResourceMimeType(name));
				}
				else if(category.equals("app"))
				{
					logger.info("Get app " + name);
					response.setData(getApp(name, session));
					response.metadata.put("mime", "text/html");
				}
				else if(category.equals("view"))
				{
					logger.info("Get view " + name);
					response.setData(getView(name, session, null));
					response.metadata.put("mime", "text/html");
				}
			}
			logger.info("UI authenticated service finish");
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
	
	protected String extractGetString(Payload payload) throws JSONException
	{
		String get = payload.metadata.get("get");
		if(payload.getString().length() > 0)
		{
			JSONObject request = new JSONObject(payload.getString());
			if(get == null) 
				get = request.getString("get");
		}
		return get;
	}
	
	protected String getApp(String name, Session session) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		StringBuilder sb = new StringBuilder();
		Bindings context = jsEngine.createBindings();
		context.put("global", config.getObject("globalvariables"));
		if(session != null)
		{
			if(session.getUserProfile().canRead("rb.apps." + name))
			{
				context.put("session", session);
				JSONObject result = request(configService, "{object:rbui_app,filter:{name:" + name + "}}");
				if(result != null)
				{
					JSONObject appConfig = result.getObject("result.0");
					String page = appConfig.getString("page");
					context.put("config", appConfig);
					String html = executeJSP("pages/" + page, context);
					if(html.contains("#menu#"))
							html = injectInHTML(html, "menu", getMenu(session));
					sb.append(html);
				}
				else
				{
					sb.append(executeJSP("pages/error", context).replace("#errormessage#", "Application " + name + " cannot be found"));
				}
			}
			else
			{
				sb.append(executeJSP("pages/error", context).replace("#errormessage#", "No access to application " + name + ""));
			}
		}
		else
		{
			context.put("get", "app/" + name);
			sb.append(executeJSP(("pages/login"), context));
		}
		return sb.toString();
	}
	
	
	protected String getMenu(Session session) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		JSONObject menu = new JSONObject("{type:menu, content:[]}");
		JSONObject result = request(configService, "{object:rbui_menu,filter:{domain:" + session.getUserProfile().getDBFilterDomainClause() + "}}");
		JSONList resultList = result.getList("result");
		for(int i = 0; i < resultList.size(); i++)
		{
			if(resultList.getObject(i).getString("type").equals("menugroup"))
			{
				boolean validGroup = false;
				JSONObject menuGroup = resultList.getObject(i);
				menuGroup.put("content", new JSONList());
				for(int j =0; j < resultList.size(); j++)
				{
					if(resultList.getObject(j).getString("type").equals("menulink")  &&  resultList.getObject(j).getString("group").equals(menuGroup.getString("_id")))
					{
						JSONObject menuLink = resultList.getObject(j);
						if(session.getUserProfile().canRead("rb.views." + menuLink.getString("view")))
						{
							menuGroup.getList("content").add(menuLink);
							validGroup = true;
						}
					}
				}
				if(validGroup)
					menu.getList("content").add(menuGroup);
			}
		}
		Bindings context = jsEngine.createBindings();
		context.put("session", session);
		return generateHTMLFromComponentConfig(menu, context);
	}

	
	protected String getView(String viewName, Session session, Bindings context) 
	{
		String viewHTML = "";
		if(session != null)
		{
			if(context == null)
			{
				context = jsEngine.createBindings();
				context.put("session", session);
				context.put("canWrite", true);
				context.put("canExecute", true);
			}

			if(session.getUserProfile().canRead("rb.views." + viewName))
			{
				try
				{
					JSONObject viewConfig = viewConfigs.get(viewName);
					if(viewConfig == null)
					{
						JSONObject result = request(configService, "{object:rbui_view,filter:{name:" + viewName + "}}");
						if(result != null)
						{
							viewConfig = result.getObject("result.0");
							//if(viewConfig.get("basefilter") != null)
							//	viewConfig.put("basefilter", convertFilter(viewConfig.getObject("basefilter")));
							viewConfigs.put(viewName, viewConfig);
						}
					}
					if(viewConfig != null)
					{
						//componentJSON = viewConfig;
						//String objectName = componentJSON.getString("object");
						//context.put("objectName", objectName);
						//context.put("viewName", viewName);
						context.put("canWrite", session.getUserProfile().canWrite("rb.views." + viewName) & (boolean)context.get("canWrite"));
						context.put("canExecute", session.getUserProfile().canExecute("rb.views." + viewName) & (boolean)context.get("canExecute"));
						JSONList contentList = viewConfig.getList("content");
						for(int i = 0; i < contentList.size(); i++)
						{
							viewHTML += generateHTMLFromComponentConfig(contentList.getObject(i), context);
						}
					}
					else
					{
						viewHTML = "<div>View name does not exist</div>";
					}
				}
				catch(Exception e)
				{
					viewHTML = formatErrorMessage("Error retrieving view " + viewName, e);
				}
			}
			else
			{
				viewHTML = "";
			}
			//return generateHTMLFromComponentJSON(new JSONObject("{type:view, name:" + name + "}"), bindings);
		}
		else
		{
			viewHTML = "Not logged in";
		}
		return viewHTML;
	}
	
	
	protected String getResource(String name) throws FunctionErrorException, FunctionTimeoutException, JSONException, RedbackException, IOException
	{
		String fileStr = null;
		String type = "";
		if(name.endsWith(".js"))
			type = "js";
		else if(name.endsWith(".css"))
			type = "css";
		else if(name.endsWith(".svg"))
			type = "svg";
		else if(name.endsWith(".ico"))
			type = "icons";
		
		if(type.equals("svg"))
		{
			JSONObject result = request(configService, "{object:rbui_resource,filter:{\"name\":\"" + name + "\"}}");
			if(result.getList("result").size() > 0)
				fileStr = StringUtils.unescape(result.getList("result").getObject(0).getString("content"));
		}
		else if(resourceServiceType.equals("filestorage"))
		{
			logger.info("Requesting firebus service : " + resourceService);
			Payload result = firebus.requestService(resourceService, new Payload(name));
			logger.info("Received firebus service response from : " + resourceService);
			fileStr = result.getString();
		}
		else if(resourceServiceType.equals("db"))
		{
			JSONObject result = request(resourceService, "{object:rbui_resource,filter:{name:" + name + "}}");
			fileStr = result.getList("result").getObject(0).getString("content");
		}
		else
		{
			InputStream is = this.getClass().getResourceAsStream("/com/nic/redback/services/uiserver/client/" + type + "/" + name);
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			fileStr = new String(bytes);
		}
		return fileStr;
	}
	
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
		return mime;
	}
		
	protected CompiledScript getCompiledJSP(String name) throws RedbackException
	{
		CompiledScript script = jspScripts.get(name);
		if(script == null)
		{
			try
			{
				InputStream is = this.getClass().getResourceAsStream("/com/nic/redback/services/uiserver/jsp/" + name + ".jsp");
				byte[] bytes = new byte[is.available()];
				is.read(bytes);
				is.close();
				String jsp = new String(bytes);
				
				jsp = jsp.replace("\r\n", "\\r\\n").replace("'", "\\'");
				int pos1 = -1;
				int pos2 = -1;
				while((pos1 = jsp.indexOf("<%=")) != -1  &&  (pos2 = jsp.indexOf("%>", pos1 + 2)) != -1)
					jsp = jsp.substring(0, pos1) + "');\r\nsb.append(" + jsp.substring(pos1 + 3, pos2).trim().replace("\\'", "'") + ");\r\nsb.append('" + jsp.substring(pos2 + 2);

				while((pos1 = jsp.indexOf("<%")) != -1  &&  (pos2 = jsp.indexOf("%>", pos1 + 2)) != -1)
					jsp = jsp.substring(0, pos1) + "');\r\n" + jsp.substring(pos1 + 2, pos2).trim().replace("\\'",  "'").replace("\\r\\n", "\r\n") + "\r\nsb.append('" + jsp.substring(pos2 + 2);
				jsp = "sb.append('" + jsp + "');";

				script = ((Compilable) jsEngine).compile(jsp);
				jspScripts.put(name,  script);
			}
			catch(Exception e)
			{
				String error = "Error when trying to retreive " + name + ".jsp";
				logger.severe(error + " : " + e.getMessage());
				throw new RedbackException(error, e);
			}					
		}
		return script;
	}	
	
	protected String executeJSP(String name, Bindings context)
	{
		context.put("sb", new RedbackStringBuilder());
		try
		{
			CompiledScript script = getCompiledJSP(name);
			script.eval(context);
			String html = ((RedbackStringBuilder)context.get("sb")).toString();
			while(html.indexOf("\r\n\r\n") > -1)
				html = html.replace("\r\n\r\n", "\r\n");
			return html;
		}
		catch(Exception e)
		{
			return formatErrorMessage("Error exeucting jsp '" + name + "'", e);
		}
	}	
	
	protected String generateHTMLFromComponentConfig(JSONObject componentConfig, Bindings context)
	{
		String type = componentConfig.getString("type");
		String componentHTML = "";
		if(type != null)
		{
			if(type.equals("view"))
			{
				String viewName = componentConfig.getString("name");
				Session session = (Session)context.get("session"); 
				componentHTML = getView(viewName, session, context);
			}
			else
			{
				String inlineStyle = "";
				String grow = componentConfig.getString("grow");
				String shrink = componentConfig.getString("shrink");
				if(grow != null)
					inlineStyle += "flex-grow:" + ((int)Double.parseDouble(grow)) + ";";
				if(shrink != null)
					inlineStyle += "flex-shrink:" + ((int)Double.parseDouble(shrink)) + ";";

				if(inlineStyle.length() > 0)
					componentConfig.put("inlineStyle", inlineStyle);
				if(componentConfig.get("show") == null)
					componentConfig.put("show", "true");
				context.put("config", componentConfig);
				componentHTML = executeJSP("fragments/" + type, context);

				if(componentHTML != null  &&  componentHTML.indexOf("#content#") >= 0)
				{
					int posContent = componentHTML.indexOf("#content#");
					int posNewLine = componentHTML.substring(0, posContent).lastIndexOf("\r\n");
					String indentStr = componentHTML.substring(posNewLine + 2, posContent);
					StringBuilder sb = new StringBuilder();
					JSONList content = componentConfig.getList("content");
					if(content != null)
					{
						for(int i = 0; i < content.size(); i++)
						{
							if(i > 0)
								sb.append("\r\n");
							sb.append(generateHTMLFromComponentConfig(content.getObject(i), context));
						}
					}
					String contentStr = sb.toString();
					contentStr = contentStr.replace("\r\n", "\r\n" + indentStr);
					componentHTML = componentHTML.replace("#content#", contentStr);
				}
			}
		}

		return componentHTML;
	}

	protected String injectInHTML(String html, String tag, String fragment)
	{
		int posContent = html.indexOf("#" + tag + "#");
		int posNewLine = html.substring(0, posContent).lastIndexOf("\r\n");
		String indentStr = html.substring(posNewLine + 2, posContent);
		fragment = fragment.replace("\r\n", "\r\n" + indentStr);
		html = html.replace("#" + tag + "#", fragment);		
		return html;
	}
	

	public static JSONObject convertFilter(JSONObject in)
	{
		JSONObject out = new JSONObject();
		Iterator<String> it = in.keySet().iterator();
		while(it.hasNext())
		{
			String inKey = it.next();
			String outKey = inKey;
			if(inKey.startsWith("_"))
				outKey = "$" + inKey.substring(1);
			if(in.get(inKey) instanceof JSONObject)
				out.put(outKey, convertFilter(in.getObject(inKey)));
			else
				out.put(outKey, in.get(inKey));				
		}
		return out;
	}
	
	protected String formatErrorMessage(String msg, Exception e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<div>" + msg + "<br/>");
		sb.append(e.getMessage());
		Throwable t = e;
		while((t = t.getCause()) != null)
			sb.append("<br/>" + t.getMessage());
		sb.append("</div>");
		return sb.toString();
	}
	


}
