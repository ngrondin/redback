package com.nic.redback.services.impl;

import java.io.File;
import java.io.FileInputStream;
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

import com.nic.firebus.Firebus;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.UIServer;
import com.nic.redback.utils.HTML;
import com.nic.redback.utils.StringUtils;

public class RedbackUIServer extends UIServer
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String devpath;
	protected ScriptEngine jsEngine;
	protected HashMap<String, CompiledScript> jspScripts;
	protected HashMap<String, DataMap> viewConfigs;

	
	public RedbackUIServer(DataMap c, Firebus f) 
	{
		super(c, f);
		devpath = config.getString("devpath");
		jsEngine = new ScriptEngineManager().getEngineByName("javascript");
		jspScripts = new HashMap<String, CompiledScript>();
		viewConfigs = new HashMap<String, DataMap>();
	}


	protected HTML getApp(Session session, String name, String version) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		HTML html = null;
		Bindings context = jsEngine.createBindings();
		context.put("global", config.getObject("globalvariables"));
		if(session != null)
		{
			if(session.getUserProfile().canRead("rb.apps." + name))
			{
				context.put("session", session);
				try
				{
					DataMap appConfig = getConfig("rbui", "app", name); 
					String page = appConfig.getString("page");
					context.put("config", appConfig);
					html = executeJSP("pages/" + page, version, context);
					html.inject("menu", getMenu(session, version));
				}
				catch(Exception e)
				{
					html = executeJSP("pages/error", version, context).inject("errormessage", new HTML("Application " + name + " cannot be found"));
				}
			}
			else
			{
				html = executeJSP("pages/error", version, context).inject("errormessage", new HTML("No access to application " + name + ""));
			}
		}
		else
		{
			context.put("get", "app/" + name);
			html = executeJSP(("pages/login"), version, context);
		}
		return html;
	}
	
	
	protected HTML getMenu(Session session, String version) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		DataMap menu = new DataMap("{type:menu, content:[]}");
		DataMap result = listConfigs("rbui", "menu");
		DataList resultList = result.getList("result");
		for(int i = 0; i < resultList.size(); i++)
		{
			if(resultList.getObject(i).getString("type").equals("menugroup"))
			{
				boolean validGroup = false;
				DataMap menuGroup = resultList.getObject(i);
				menuGroup.put("content", new DataList());
				for(int j =0; j < resultList.size(); j++)
				{
					if(resultList.getObject(j).getString("type").equals("menulink")  &&  resultList.getObject(j).getString("group").equals(menuGroup.getString("_id")))
					{
						DataMap menuLink = resultList.getObject(j);
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
		return generateHTMLFromComponentConfig(menu, version, context);
	}

	
	protected HTML getView(Session session, String viewName, String version, Bindings context) 
	{
		HTML viewHTML = new HTML();
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
					DataMap viewConfig = viewConfigs.get(viewName);
					if(viewConfig == null)
					{
						viewConfig = getConfig("rbui", "view", viewName);
						viewConfigs.put(viewName, viewConfig);
					}
					if(viewConfig != null)
					{
						context.put("canWrite", session.getUserProfile().canWrite("rb.views." + viewName) & (boolean)context.get("canWrite"));
						context.put("canExecute", session.getUserProfile().canExecute("rb.views." + viewName) & (boolean)context.get("canExecute"));
						DataList contentList = viewConfig.getList("content");
						for(int i = 0; i < contentList.size(); i++)
							viewHTML.append(generateHTMLFromComponentConfig(contentList.getObject(i), version, context));
					}
					else
					{
						viewHTML.append("<div>View name does not exist</div>");
					}
				}
				catch(Exception e)
				{
					viewHTML = formatErrorMessage("Error retrieving view " + viewName, e);
				}
			}
			else
			{
				// Intentionnaly leaving the view blank when no right to read it
			}
		}
		else
		{
			viewHTML.append("Not logged in");
		}
		return viewHTML;
	}
	
	protected HTML generateHTMLFromComponentConfig(DataMap componentConfig, String version, Bindings context) throws RedbackException
	{
		String type = componentConfig.getString("type");
		HTML componentHTML = new HTML();
		if(type != null)
		{
			if(type.equals("view"))
			{
				String viewName = componentConfig.getString("name");
				Session session = (Session)context.get("session"); 
				componentHTML = getView(session, viewName, version, context);
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
				componentHTML = executeJSP("fragments/" + type, version, context);

				if(componentHTML.hasTag("content") && componentConfig.containsKey("content"))
				{
					HTML contentHTML = new HTML();
					DataList content = componentConfig.getList("content");
					for(int i = 0; i < content.size(); i++)
						contentHTML.append(generateHTMLFromComponentConfig(content.getObject(i), version, context));
					componentHTML.inject("content", contentHTML);
				}
			}
		}

		return componentHTML;
	}

	
	protected HTML executeJSP(String name, String version, Bindings context) throws RedbackException
	{
		context.put("sb", new HTML());
		try
		{
			CompiledScript script = getCompiledJSP(name, version);
			script.eval(context);
			HTML html = ((HTML)context.get("sb"));
			return html;
		}
		catch(Exception e)
		{
			throw new RedbackException("Error exeucting jsp '" + name + "'", e);
		}
	}	
	
	
	protected CompiledScript getCompiledJSP(String name, String version) throws RedbackException
	{
		CompiledScript script = jspScripts.get(name);
		if(script == null)
		{
			try
			{
				InputStream is = null;
				if(devpath != null)
				{
					File file = new File(devpath + "/" + version + "/jsp/" + name + ".jsp");
					if(file.exists())
						is = new FileInputStream(file);
				}
				else
				{
					is = this.getClass().getResourceAsStream("/com/nic/redback/services/uiserver/" + version + "/jsp/" + name + ".jsp");
				}
				
				if(is != null)
				{
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


	protected byte[] getResource(String name, String version) throws FunctionErrorException, FunctionTimeoutException, DataException, RedbackException, IOException
	{
		byte[] bytes = null;
		String type = "";
		if(name.endsWith(".js"))
			type = "js";
		else if(name.endsWith(".css"))
			type = "css";
		else if(name.endsWith(".svg"))
			type = "svg";
		else if(name.endsWith(".ico"))
			type = "icons";
		else if(name.endsWith(".png"))
			type = "png";
		
		if(type.equals("svg"))
		{
			DataMap result = this.getConfig("rbui", "resource", name); 
			bytes = StringUtils.unescape(result.getString("content")).getBytes();
		}
		else
		{
			InputStream is = null;
			if(devpath != null)
			{
				File file = new File(devpath + "/" + version + "/client/" + type + "/" + name);
				if(file.exists())
					is = new FileInputStream(file);
			}
			else
			{
				is = this.getClass().getResourceAsStream("/com/nic/redback/services/uiserver/" + version + "/client/" + type + "/" + name);
			}
			
			if(is != null)
			{
				bytes = new byte[is.available()];
				int bytesRead = 0;
				while(is.available() > 0)
				{
					bytesRead += is.read(bytes, bytesRead, (bytes.length - bytesRead));
				}
			}
			else
			{
				throw new FunctionErrorException("The resource was not found");
			}
		}
		return bytes;
	}
	

	public static DataMap convertFilter(DataMap in)
	{
		DataMap out = new DataMap();
		Iterator<String> it = in.keySet().iterator();
		while(it.hasNext())
		{
			String inKey = it.next();
			String outKey = inKey;
			if(inKey.startsWith("_"))
				outKey = "$" + inKey.substring(1);
			if(in.get(inKey) instanceof DataMap)
				out.put(outKey, convertFilter(in.getObject(inKey)));
			else
				out.put(outKey, in.get(inKey));				
		}
		return out;
	}

}
