package com.nic.redback.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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

public class UIServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String resourceService;
	protected String resourceServiceType;
	protected ScriptEngine jsEngine;
	protected HashMap<String, CompiledScript> jspScripts;


	public UIServer( JSONObject c)
	{
		super(c);
		configService = config.getString("configservice");
		resourceServiceType = config.getString("resourceservicetype");
		resourceService = config.getString("resourceservice");
		jsEngine = new ScriptEngineManager().getEngineByName("javascript");
		jspScripts = new HashMap<String, CompiledScript>();
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		logger.info("UI service start");
		Payload response = new Payload();
		try
		{
			StringBuilder sb = new StringBuilder();
			Session session = null;
			String sessionId = payload.metadata.get("sessionid");
			String get = payload.metadata.get("get");
			String username = null;
			String password = null;
			String mime = "";

			if(payload.getString().length() > 0)
			{
				try
				{
					JSONObject request = new JSONObject(payload.getString());
					if(get == null) 
						get = request.getString("get");
					username = request.getString("username");
					password = request.getString("password");
				}
				catch(Exception e)	{}
			}
			
			if(get != null)
			{
				String[] parts = get.split("/");
				String category = parts[0];
				String name = parts[1];
				
				if(category.equals("resource"))
				{
					logger.info("Get resource " + name);
					sb.append(getResource(name));
					if(name.endsWith(".js"))
						mime = "application/javascript";
					else if(name.endsWith(".css"))
						mime = "text/css";
					else if(name.endsWith(".ico"))
						mime = "image/x-icon";
				}
				else
				{
					if(username != null  &&  password != null)
					{
						session = authenticate(username, password);
						response.metadata.put("sessionid", session.getSessionId().toString());
					}
					else if(sessionId != null)
					{
						session = validateSession(sessionId);
					}					
					
					if(category.equals("app"))
					{
						logger.info("Get app " + name);
						sb.append(getApp(name, session));
					}
					else if(category.equals("view"))
					{
						logger.info("Get view " + name);
						sb.append(getView(name, session));
					}
				}
			}
			response.setData(sb.toString());
			response.metadata.put("mime", mime);
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
		logger.info("UI service finish");
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}
	
	protected String getApp(String name, Session session) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		StringBuilder sb = new StringBuilder();
		Bindings context = jsEngine.createBindings();
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
					
					String menuFragment = executeJSP("fragments/navigation", context);
					StringBuilder menuGroupBuilder = new StringBuilder();
					for(int i = 0; i < appConfig.getList("menugroups").size(); i++)
					{
						JSONObject menuGroup = appConfig.getList("menugroups").getObject(i);
						menuGroup.put("groupid", "group" + i);
						JSONList itemList = menuGroup.getList("menuitems");
						StringBuilder menuItemBuilder = new StringBuilder();
						for(int j = 0; j < itemList.size(); j++)
						{
							JSONObject menuItem = itemList.getObject(j);
							menuItem.put("groupid", "group" + i);
							context.put("config", menuItem);
							if(session.getUserProfile().canRead("rb.views." +  menuItem.getString("view")))
								menuItemBuilder.append(executeJSP("fragments/navigationitem", context) + "\r\n");								
						}
						if(menuItemBuilder.length() > 0)
						{
							context.put("config", menuGroup);
							menuGroupBuilder.append(executeJSP("fragments/navigationgroup", context).replace("#content#", menuItemBuilder.toString().trim()) + "\r\n");
						}
					}			
					menuFragment = injectInHTML(menuFragment, "content", menuGroupBuilder.toString().trim());
					html = injectInHTML(html, "menu", menuFragment);
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
	

	
	protected String getView(String name, Session session) throws JSONException
	{
		if(session != null)
		{
			Bindings bindings = jsEngine.createBindings();
			bindings.put("session", session);
			return generateHTMLFromComponentJSON(new JSONObject("{type:view, name:" + name + "}"), bindings);
		}
		else
		{
			return "Not logged in";
		}
	}
	
	
	protected String getResource(String name) throws FunctionErrorException, FunctionTimeoutException, JSONException, RedbackException, IOException
	{
		String fileStr = null;
		String type = "";
		if(name.endsWith(".js"))
			type = "js";
		else if(name.endsWith(".css"))
			type = "css";
		else if(name.endsWith(".ico"))
			type = "icons";

		if(resourceServiceType.equals("filestorage"))
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
			InputStream is = this.getClass().getResourceAsStream("/com/nic/redback/" + type + "/" + name);
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			fileStr = new String(bytes);
		}
		return fileStr;
	}
		
	protected CompiledScript getCompiledJSP(String name) throws RedbackException
	{
		CompiledScript script = jspScripts.get(name);
		if(script == null)
		{
			try
			{
				InputStream is = this.getClass().getResourceAsStream("/com/nic/redback/jsp/" + name + ".jsp");
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
	
	protected String generateHTMLFromComponentJSON(JSONObject componentJSON, Bindings context)
	{
		String type = componentJSON.getString("type");
		String componentHTML = "";
		if(type != null)
		{
			String inlineStyle = "";
			String grow = componentJSON.getString("grow");
			String shrink = componentJSON.getString("shrink");
			if(grow != null)
				inlineStyle += "flex-grow:" + ((int)Double.parseDouble(grow)) + ";";
			if(shrink != null)
				inlineStyle += "flex-shrink:" + ((int)Double.parseDouble(shrink)) + ";";

			if(type.equals("view"))
			{
				String viewName = componentJSON.getString("name");
				Session session = (Session)context.get("session"); 
				if(session.getUserProfile().canRead("rb.views." + viewName))
				{
					try
					{
						JSONObject result = request(configService, "{object:rbui_view,filter:{name:" + viewName + "}}");
						if(result != null)
						{
							componentJSON = result.getObject("result.0");
							String objectName = componentJSON.getString("object");
							context.put("objectName", objectName);
							context.put("viewName", viewName);
							context.put("canWrite", session.getUserProfile().canWrite("rb.views." + viewName) & session.getUserProfile().canWrite("rb.objects." + objectName));
							context.put("canExecute", session.getUserProfile().canExecute("rb.views." + viewName) & session.getUserProfile().canExecute("rb.objects." + objectName));
						}
						else
						{
							return "<div>View name does not exist</div>";
						}
					}
					catch(Exception e)
					{
						return formatErrorMessage("Error retrieving view " + viewName, e);
					}
				}
				else
				{
					return "";
				}
			}			

			if(inlineStyle.length() > 0)
				componentJSON.put("inlineStyle", inlineStyle);
			if(componentJSON.get("show") == null)
				componentJSON.put("show", "true");
			context.put("config", componentJSON);

			componentHTML = executeJSP("fragments/" + type, context);
					
			if(componentHTML != null  &&  componentHTML.indexOf("#content#") >= 0)
			{
				int posContent = componentHTML.indexOf("#content#");
				int posNewLine = componentHTML.substring(0, posContent).lastIndexOf("\r\n");
				String indentStr = componentHTML.substring(posNewLine + 2, posContent);
				StringBuilder sb = new StringBuilder();
				JSONList content = componentJSON.getList("content");
				if(content != null)
				{
					for(int i = 0; i < content.size(); i++)
					{
						if(i > 0)
							sb.append("\r\n");
						sb.append(generateHTMLFromComponentJSON(content.getObject(i), context));
					}
				}
				String contentStr = sb.toString();
				contentStr = contentStr.replace("\r\n", "\r\n" + indentStr);
				componentHTML = componentHTML.replace("#content#", contentStr);
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