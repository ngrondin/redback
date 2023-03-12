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

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.script.Function;
import io.firebus.script.ScriptFactory;
import io.firebus.script.exceptions.ScriptException;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.exceptions.RedbackUnauthorisedException;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;
import io.redback.services.UIServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.ConfigCache;
import io.redback.utils.HTML;
import io.redback.utils.KeyEscaper;
import io.redback.utils.StringUtils;
import io.redback.utils.js.HTMLJSWrapper;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class RedbackUIServer extends UIServer 
{
	protected String devpath;
	protected ScriptFactory scriptFactory;
	protected HashMap<String, Function> jspScripts;
	//protected HashMap<String, DataMap> viewConfigs;
	protected ConfigCache<DataMap> viewConfigs;
	protected ConfigurationClient configClient;
	protected DataClient dataClient;
	protected CollectionConfig viewCollection;

	
	public RedbackUIServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		devpath = config.getString("devpath");
		scriptFactory = new ScriptFactory(); 
		jspScripts = new HashMap<String, Function>();
		//viewConfigs = new HashMap<String, DataMap>();
		configClient = new ConfigurationClient(firebus, config.getString("configservice"));
		if(config.containsKey("dataservice")) {
			dataClient = new DataClient(firebus, config.getString("dataservice"));
		}
		viewCollection = new CollectionConfig(config.getObject("collection"), "rbui_view");
		viewConfigs = new ConfigCache<DataMap>(configClient, dataClient, "rbui", "view", viewCollection, new ConfigCache.ConfigFactory<DataMap> () {
			public DataMap createConfig(DataMap map) throws Exception {
				return KeyEscaper.escape(map);
			}
		});
	}
	
	public void configure() {
		viewConfigs.clear();
	}

	protected HTML getAppClient(Session session, String name, String version) throws RedbackException
	{
		HTML html = null;
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("global", config.getObject("globalvariables"));
		context.put("version", version);
		context.put("deployment",  config);
		context.put("utils", new RedbackUtilsJSWrapper());
		try {
			DataMap appConfig = getAppConfig(session, name);
			context.put("session", new SessionJSWrapper(session));
			String page = appConfig.getString("page");
			context.put("config", appConfig);
			html = executeJSP("pages/" + page, version, context);			
		} catch(Exception e) {
			Logger.warning("rb.ui.getapp", e.getMessage());
			html = executeJSP("pages/error", version, context).inject("errormessage", formatErrorMessage(e.getMessage(), e.getCause()));
		}
		return html;
	}
	
	
	protected DataMap getAppConfig(Session session, String name) throws RedbackException
	{
		if(session != null)
		{
			DataMap appConfig = null;
			if(name != null) {
				appConfig = configClient.getConfig(session, "rbui", "app", name); 
			} else {
				DataMap result = configClient.listConfigs(session, "rbui", "app", new DataMap("isdefault", true));
				if(result.getList("result").size() > 0) {
					appConfig = result.getList("result").getObject(0);
				}
			}
			if(appConfig != null) 
			{
				String appName = appConfig.getString("name");
				if(session.getUserProfile().canRead("rb.apps." + appName))
				{
					return appConfig;
				}
				else 
				{
					throw new RedbackUnauthorisedException("Unauthorised to access application '" + name + "'");
				}
			}
			else if(name != null)
			{
				throw new RedbackInvalidRequestException("Application '" + name + "' does not exist");
			}
			else 
			{
				throw new RedbackInvalidRequestException("Default application was not found");
			}
		}
		else
		{
			throw new RedbackInvalidRequestException("Session is required to retrieve app config");
		}
	}
	
	
	protected DataMap getMenu(Session session, String version) throws RedbackException
	{
		try {
			DataMap menu = new DataMap("{type:menu, content:[]}");
			DataMap result = configClient.listConfigs(session, "rbui", "menu");
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
			return menu;
		} catch(Exception e) {
			throw new RedbackException("Error getting menu", e);
		}
	}
	

	protected DataMap getView(Session session, String domain, String viewName)
	{
		return getView(session, domain, viewName, null);
	}

	protected DataMap getView(Session session, String domain, String viewName, Map<String, Object> context) 
	{
		DataMap view = new DataMap();
		if(session != null)
		{
			if(session.getUserProfile().canRead("rb.views." + viewName))
			{
				try
				{
					DataMap viewConfig = getViewConfig(session, domain, viewName);
					if(viewConfig != null) {
						view.put("label", viewConfig.getString("label"));
						view.put("onload", viewConfig.getString("onload"));
						view.put("content", getViewContent(session, domain, viewName, context));						
					} else {
						view.put("error", "View " + viewName + " does not exist");
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					view.put("error", "Error retrieving view " + viewName + ": " + e.getMessage());
				}
			}
		}
		else
		{
			view.put("error", "Not logged in");
		}
		return view;
	}


	protected DataList getViewContent(Session session, String domain, String viewName, Map<String, Object> context) throws RedbackException 
	{
		DataList viewContent = new DataList();
		DataMap viewConfig = getViewConfig(session, domain, viewName);
		if(viewConfig != null) {
			DataList contentList = viewConfig.getList("content");
			for(int i = 0; i < contentList.size(); i++) {
				DataMap viewPart = generateViewPartFromComponentConfig(session, domain, contentList.getObject(i), context); 
				if(viewPart != null)
					viewContent.add(viewPart);
			}
		}
		return viewContent;
	}

	protected DataMap generateViewPartFromComponentConfig(Session session, String domain, DataMap componentConfig, Map<String, Object> context) throws RedbackException
	{
		DataMap viewPart = new DataMap();
		String type = componentConfig.getString("type");
		String accessCat = componentConfig.getString("accesscat");
		if(type != null && !type.equals("view") && (accessCat == null || (accessCat != null && session.getUserProfile().canRead("rb.accesscat." + accessCat))))
		{
			Iterator<String> it = componentConfig.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				if(!key.equals("content")) {
					viewPart.put(key, componentConfig.get(key));
				}
			}
			if(componentConfig.get("show") == null)
				viewPart.put("show", "true");
			
			if(componentConfig.containsKey("content"))
			{
				DataList componentContentList = componentConfig.getList("content");
				DataList viewPartContentList = new DataList();
				for(int i = 0; i < componentContentList.size(); i++) {
					DataMap childComponentConfig = componentContentList.getObject(i);
					String childType = childComponentConfig.getString("type");
					if(childType.equals("view")) {
						DataList childViewContent = getViewContent(session, domain, childComponentConfig.getString("name"), context);
						for(int j = 0; j < childViewContent.size(); j++)
							viewPartContentList.add(childViewContent.getObject(j));
						
					} else {
						DataMap childViewPart = generateViewPartFromComponentConfig(session, domain, childComponentConfig, context);
						if(childViewPart != null)
							viewPartContentList.add(childViewPart);
					}
				}
				viewPart.put("content", viewPartContentList);
			}
		}
		return viewPart;
	}
	
	protected HTML executeJSP(String name, String version, Map<String, Object> context) throws RedbackException
	{
		HTML html = new HTML();
		context.put("sb", new HTMLJSWrapper(html));
		Function script = getCompiledJSP(name, version);
		try {
			script.call(context);
			return html;
		} catch(ScriptException e) {
			throw new RedbackException("Error executing jsp", e);
		}
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
					
					jsp = jsp.replace("\r", "\\r").replace("\n", "\\n").replace("'", "\\'");
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
					script = scriptFactory.createFunction("jsp_" + version + "_" + name.replace("/", "_"), varNames.toArray(new String[] {}), jsp);
					jspScripts.put(version + "/" + name,  script);
				}
			}
			catch(Exception e)
			{
				throw new RedbackException("Error when trying to retreive " + name + ".jsp", e);
			}					
		}
		return script;
	}	

	
	protected DataMap getViewConfig(Session session, String domain, String viewName) throws RedbackException
	{
		DataMap viewConfig = viewConfigs.get(session, viewName, domain);
		/*String viewKey = (domain != null ? domain : "root") + "." + viewName;
		DataMap viewConfig = viewConfigs.get(viewKey);
		if(viewConfig == null)
		{
			if(domain != null) {
				if(viewCollection != null && dataClient != null) {
					DataMap key = new DataMap();
					key.put("domain", domain);
					key.put("name", viewName);
					DataMap resp = dataClient.getData(viewCollection.getName(), viewCollection.convertObjectToSpecific(key));
					if(resp != null && resp.getList("result") != null && resp.getList("result").size() > 0) {
						viewConfig = viewCollection.convertObjectToCanonical(resp.getList("result").getObject(0));
						viewConfig = KeyEscaper.escape(viewConfig);
						viewConfigs.put(viewKey, viewConfig);
					}
				}
			} else {
				viewConfig = configClient.getConfig(session, "rbui", "view", viewName);
				viewConfigs.put(viewKey, viewConfig);				
			}
		}*/
		return viewConfig;
	}

	protected byte[] getResource(Session session, String name, String version) throws RedbackException
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
				DataMap result = configClient.getConfig(session, "rbui", "resource", name); 
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


}
