package io.redback.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ConfigurationClient;
import io.redback.managers.jsmanager.Function;
import io.redback.managers.jsmanager.JSManager;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;
import io.redback.services.UIServer;
import io.redback.utils.HTML;
import io.redback.utils.StringUtils;
import io.redback.utils.js.HTMLJSWrapper;
import io.redback.utils.js.JSConverter;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class RedbackUIServer extends UIServer 
{
	//private Logger logger = Logger.getLogger("io.redback");
	protected String devpath;
	protected JSManager jsManager;
	protected HashMap<String, Function> jspScripts;
	protected HashMap<String, DataMap> viewConfigs;
	protected ConfigurationClient configClient;

	
	public RedbackUIServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		devpath = config.getString("devpath");
		jsManager = new JSManager(); 
		jspScripts = new HashMap<String, Function>();
		viewConfigs = new HashMap<String, DataMap>();
		configClient = new ConfigurationClient(firebus, config.getString("configservice"));
	}


	protected HTML getApp(Session session, String name, String version) throws RedbackException
	{
		HTML html = null;
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("global", JSConverter.toJS(config.getObject("globalvariables")));
		context.put("version", version);
		context.put("deployment",  JSConverter.toJS(config));
		context.put("utils", new RedbackUtilsJSWrapper());
		if(session != null)
		{
			DataMap appConfig = null;
			if(name != null) {
				appConfig = configClient.getConfig("rbui", "app", name); 
			} else {
				DataMap result = configClient.listConfigs("rbui", "app", new DataMap("isdefault", true));
				if(result.getList("result").size() > 0) {
					appConfig = result.getList("result").getObject(0);
				}
			}
			if(appConfig != null) 
			{
				String appName = appConfig.getString("name");
				if(session.getUserProfile().canRead("rb.apps." + appName))
				{
					context.put("session", new SessionJSWrapper(session));
					try
					{
						String page = appConfig.getString("page");
						context.put("config", JSConverter.toJS(appConfig));
						html = executeJSP("pages/" + page, version, context);
					}
					catch(Exception e)
					{
						html = executeJSP("pages/error", version, context).inject("errormessage", formatErrorMessage("Error retrieving application " + appName, e));
					}
				}
				else
				{
					html = executeJSP("pages/error", version, context).inject("errormessage", new HTML("No access to application " + appName + ""));
				}
			}
			else 
			{
				html = executeJSP("pages/error", version, context).inject("errormessage", new HTML("No application name provided and no default configured"));
			}
		}
		else
		{
			context.put("get", "app/" + name);
			html = executeJSP(("pages/login"), version, context);
		}
		return html;
	}
	
	
	protected HTML getMenu(Session session, String version) throws RedbackException
	{
		try {
			DataMap menu = new DataMap("{type:menu, content:[]}");
			DataMap result = configClient.listConfigs("rbui", "menu");
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
			{
				if(resultList.getObject(i).getString("type").equals("menugroup"))
				{
					boolean validGroup = false;
					DataMap menuGroup = resultList.getObject(i);
					menuGroup.put("content", new DataList());
					for(int j = 0; j < resultList.size(); j++)
					{
						if(resultList.getObject(j).getString("type").equals("menulink") && menuGroup.getString("name").equals(resultList.getObject(j).getString("group")))
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
					{
						menuGroup.getList("content").sort("order");
						menu.getList("content").add(menuGroup);
					}
				} 
				else if(resultList.getObject(i).getString("type").equals("menulink") && resultList.getObject(i).getString("group") == null)
				{
					DataMap menuLink = resultList.getObject(i);
					if(session.getUserProfile().canRead("rb.views." + menuLink.getString("view")))
						menu.getList("content").add(menuLink);
				}
			}
			menu.getList("content").sort("order");
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("session", new SessionJSWrapper(session));
			context.put("utils", new RedbackUtilsJSWrapper());
			context.put("parents", JSConverter.toJS(new DataMap()));
			return generateHTMLFromComponentConfig(session, menu, version, context);
		} catch(Exception e) {
			throw new RedbackException("Error getting menu", e);
		}
	}
	
	protected HTML getView(Session session, String viewName, String version)
	{
		return getView(session, viewName, version, null);
	}

	
	protected HTML getView(Session session, String viewName, String version, Map<String, Object> context) 
	{
		HTML viewHTML = new HTML();
		if(session != null)
		{
			if(session.getUserProfile().canRead("rb.views." + viewName))
			{
				try
				{
					DataMap viewConfig = viewConfigs.get(viewName);
					if(viewConfig == null)
					{
						viewConfig = configClient.getConfig("rbui", "view", viewName);
						viewConfigs.put(viewName, viewConfig);
					}
					if(viewConfig != null)
					{
						if(context == null)
						{
							context = new HashMap<String, Object>();
							context.put("session", new SessionJSWrapper(session));
							context.put("utils", new RedbackUtilsJSWrapper());
							context.put("parents", JSConverter.toJS(new DataMap()));
							context.put("canWrite", true);
							context.put("canExecute", true);
							viewHTML.append("<rb-view\r\n\t(afterload)=\"setTitle('" + viewConfig.getString("label") + "')\">\r\n\t#content#\r\n</rb-view>");
						}
						context.put("canWrite", session.getUserProfile().canWrite("rb.views." + viewName) & (boolean)context.get("canWrite"));
						context.put("canExecute", session.getUserProfile().canExecute("rb.views." + viewName) & (boolean)context.get("canExecute"));

						DataList contentList = viewConfig.getList("content");
						HTML contentHTML = new HTML();
						for(int i = 0; i < contentList.size(); i++)
							contentHTML.append(generateHTMLFromComponentConfig(session, contentList.getObject(i), version, context));
						if(viewHTML.hasTag("content"))
							viewHTML.inject("content", contentHTML);
						else
							viewHTML = contentHTML;
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
	
	
	protected HTML generateHTMLFromComponentConfig(Session session, DataMap componentConfig, String version, Map<String, Object> context) throws RedbackException
	{
		String type = componentConfig.getString("type");
		HTML componentHTML = new HTML();
		if(type != null)
		{
			if(type.equals("view"))
			{
				String viewName = componentConfig.getString("name");
				componentHTML = getView(session, viewName, version, context);
			}
			else
			{
				String id = type + (new Random()).nextInt(10000);
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
				context.put("config", JSConverter.toJS(componentConfig));
				context.put("id", id);

				componentHTML = executeJSP("fragments/" + type, version, context);
				if(componentHTML.hasTag("content") && componentConfig.containsKey("content"))
				{
					Value parentSameType = (Value)((ProxyObject)context.get("parents")).getMember(type);
					((ProxyObject)context.get("parents")).putMember(type, Value.asValue(id));
					HTML contentHTML = new HTML();
					DataList content = componentConfig.getList("content");
					for(int i = 0; i < content.size(); i++)
						contentHTML.append(generateHTMLFromComponentConfig(session, content.getObject(i), version, context));
					componentHTML.inject("content", contentHTML);
					if(parentSameType != null)
						((ProxyObject)context.get("parents")).putMember(type, parentSameType);
					else
						((ProxyObject)context.get("parents")).removeMember(type);
				}
			}
		}
		return componentHTML;
	}

	
	protected HTML executeJSP(String name, String version, Map<String, Object> context) throws RedbackException
	{
		HTML html = new HTML();
		context.put("sb", new HTMLJSWrapper(html));
		Function script = getCompiledJSP(name, version);
		script.execute(context);
		return html;
	}	
	
	
	protected Function getCompiledJSP(String name, String version) throws RedbackException
	{
		if(version == null)
			version = "default";
		Function script = jspScripts.get(version + "/" + name);
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
					is = this.getClass().getResourceAsStream("/io/redback/services/uiserver/" + version + "/jsp/" + name + ".jsp");
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
					
					List<String> varNames = new ArrayList<String>();
					varNames.add("global");
					varNames.add("utils");
					varNames.add("version");
					varNames.add("session");
					varNames.add("canWrite");
					varNames.add("canExecute");
					varNames.add("config");
					varNames.add("deployment");
					varNames.add("id");
					varNames.add("parents");
					varNames.add("sb");
					script = new Function(jsManager, "jsp_" + version + "_" + name.replace("/", "_"), varNames, jsp);
					jspScripts.put(version + "/" + name,  script);
				}
			}
			catch(Exception e)
			{
				String error = "Error when trying to retreive " + name + ".jsp";
				//logger.severe(error + " : " + e.getMessage());
				throw new RedbackException(error, e);
			}					
		}
		return script;
	}	


	protected byte[] getResource(String name, String version) throws RedbackException
	{
		try {
			byte[] bytes = null;
			if(version == null)
				version = "default";
			String type = "";
			if(name.endsWith(".js"))
				type = "js";
			else if(name.endsWith(".css"))
				type = "css";
			else if(name.endsWith(".svg"))
				type = "svg";
			else if(name.endsWith(".ico"))
				type = "ico";
			else if(name.endsWith(".png"))
				type = "png";
			else if(name.endsWith(".apk"))
				type = "apk";
			else if(name.endsWith(".ipa"))
				type = "ipa";
			else if(name.endsWith(".plist"))
				type = "plist";
			
			if(type.equals("svg"))
			{
				DataMap result = configClient.getConfig("rbui", "resource", name); 
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
					is = this.getClass().getResourceAsStream("/io/redback/services/uiserver/" + version + "/client/" + type + "/" + name);
				}
				
				if(is != null)
				{
					ByteArrayOutputStream result = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) != -1)
					    result.write(buffer, 0, length);
					is.close();
					bytes = result.toByteArray();
				}
				else
				{
					throw new RedbackException("The resource was not found : " + name);
				}
			}
			return bytes;
		} catch(Exception e) {
			throw new RedbackException("Error getting resources", e);
		}
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

	
	public void clearCaches()
	{
		viewConfigs.clear();
	}

}
