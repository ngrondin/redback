package io.redback.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.script.Function;
import io.firebus.script.ScriptFactory;
import io.firebus.script.exceptions.ScriptException;
import io.redback.client.ConfigClient;
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
import io.redback.utils.HTMLMetaParser;
import io.redback.utils.KeyEscaper;
import io.redback.utils.StringUtils;
import io.redback.utils.js.HTMLJSWrapper;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class RedbackUIServer extends UIServer 
{
	protected String devpath;
	protected ScriptFactory scriptFactory;
	protected HashMap<String, Function> jspScripts;
	protected ConfigCache<DataMap> viewConfigs;
	protected ConfigCache<DataMap> menuConfigs;
	protected ConfigClient configClient;
	protected DataClient dataClient;
	protected CollectionConfig viewCollection;

	
	public RedbackUIServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		devpath = config.getString("devpath");
		scriptFactory = new ScriptFactory(); 
		jspScripts = new HashMap<String, Function>();
		configClient = new ConfigClient(firebus, config.getString("configservice"));
		if(config.containsKey("dataservice")) {
			dataClient = new DataClient(firebus, config.getString("dataservice"));
		}
		viewCollection = new CollectionConfig(config.getObject("collection"), "rbui_view");
		viewConfigs = new ConfigCache<DataMap>(configClient, "rbui", "view", 3600000, new ConfigCache.ConfigFactory<DataMap> () {
			public DataMap createConfig(DataMap map) throws Exception {
				return KeyEscaper.escape(map);
			}
		});
		menuConfigs = new ConfigCache<DataMap>(configClient, "rbui", "menu", 3600000, new ConfigCache.ConfigFactory<DataMap> () {
			public DataMap createConfig(DataMap map) throws Exception {
				return KeyEscaper.escape(map);
			}
		});
	}
	
	public void configure() {
		viewConfigs.clear();
		menuConfigs.clear();
	}

	protected HTML getAppClient(Session session, String name, String version) throws RedbackException
	{
		HTML html = null;
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("global", config.getObject("globalvariables"));
		context.put("version", version);
		context.put("deployment",  config);
		context.put("utils", new RedbackUtilsJSWrapper());
		context.put("session", new SessionJSWrapper(session));
		try {
			DataMap appConfig = getAppConfig(session, name);
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
					DataMap ret = (DataMap)appConfig.getCopy();
					DataList menuConfig = appConfig.getList("menu");
					DataList menus = new DataList();
					if(menuConfig != null) {
						for(int i = 0; i < menuConfig.size(); i++) {
							DataMap menu = buildMenuFor(session, menuConfig.getString(i));
							if(menu != null)
								menus.add(menu);
						}						
					}
					ret.put("menu", menus);
					return ret;
				}
				else 
				{
					throw new RedbackUnauthorisedException("Unauthorised to access application '" + appName + "'");
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
	
	protected DataMap buildMenuFor(Session session, String name) throws RedbackException
	{
		DataMap menuConfig = menuConfigs.get(session, name);
		if(menuConfig != null)
			return buildMenuFor(session, menuConfig);
		return null;
	}
	
	protected DataMap buildMenuFor(Session session, DataMap menuConfig) throws RedbackException
	{
		if(menuConfig.getString("type").equals("menugroup")) {
			List<DataMap> subMenuConfigs = menuConfigs.list(session, new DataMap("group", menuConfig.getString("name"))); 
			subMenuConfigs.sort(new Comparator<DataMap>() {public int compare(DataMap a, DataMap b) {return a.getNumber("order").intValue() - b.getNumber("order").intValue();}});
			DataList subList = new DataList();
			for(DataMap subMenuConfig: subMenuConfigs) {
				DataMap subMenu = buildMenuFor(session, subMenuConfig);
				if(subMenu != null)
					subList.add(subMenu);
			}
			if(subList.size() > 0) {
				DataMap menu = new DataMap("type", menuConfig.getString("type"), "icon", menuConfig.getString("icon"), "label", menuConfig.getString("label"));
				menu.put("content", subList);
				return menu;
			}
		} else if(menuConfig.getString("type").equals("menulink")) {
			DataMap viewConfig = getViewConfigIfCanRead(session, menuConfig.getString("view"));
			if(viewConfig != null) {
				return new DataMap("type", menuConfig.getString("type"), "view", menuConfig.getString("view"), "icon", menuConfig.getString("icon"), "label", menuConfig.getString("label"));
			}
		}
		return null;
	}

	protected DataMap getView(Session session, String viewName)
	{
		return getView(session, viewName, null);
	}

	protected DataMap getView(Session session, String viewName, Map<String, Object> context) 
	{
		DataMap view = new DataMap();
		try {
			DataMap viewConfig = getViewConfigIfCanRead(session, viewName);
			if(viewConfig != null) {
				view.put("label", viewConfig.getString("label"));
				view.put("onload", viewConfig.getString("onload"));
				view.put("content", getViewContent(session, viewConfig, context));	
			} else {
				view.put("error", "No access to view " + viewName);
			}
		} catch(Exception e) {
			view.put("error", "No access to view " + viewName);
			Logger.severe("rb.ui.getview", new DataMap("name", viewName), e);
		}
		return view;
	}
	
	protected DataMap getViewConfigIfCanRead(Session session, String name) throws RedbackException {
		DataMap viewConfig = viewConfigs.get(session, name, false);
		if(viewConfig != null) {
			String accessCat = viewConfig.getString("accesscat");
			if(session.getUserProfile().canRead("rb.views." + name) || session.getUserProfile().canRead("rb.accesscat." + accessCat)) {
				return viewConfig;
			} 
		}
		return null;
	}

	protected DataList getViewContent(Session session, String viewName, Map<String, Object> context) throws RedbackException 
	{
		DataMap viewConfig = getViewConfigIfCanRead(session, viewName);
		if(viewConfig != null)
			return getViewContent(session, viewConfig, context);
		else 
			return new DataList();
	}
	
	protected DataList getViewContent(Session session, DataMap viewConfig, Map<String, Object> context) throws RedbackException 
	{
		DataList viewContent = new DataList();
		DataList contentList = viewConfig.getList("content");
		for(int i = 0; i < contentList.size(); i++) {
			DataMap viewPart = generateViewPartFromComponentConfig(session, contentList.getObject(i), context); 
			if(viewPart != null)
				viewContent.add(viewPart);
		}
		return viewContent;
	}

	protected DataMap generateViewPartFromComponentConfig(Session session, DataMap componentConfig, Map<String, Object> context) throws RedbackException
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
						DataList childViewContent = getViewContent(session, childComponentConfig.getString("name"), context);
						for(int j = 0; j < childViewContent.size(); j++)
							viewPartContentList.add(childViewContent.getObject(j));
						
					} else {
						DataMap childViewPart = generateViewPartFromComponentConfig(session, childComponentConfig, context);
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
		Function script = getCompiledJSP(name, version);
		if(script != null) {
			try {
				HTML html = new HTML();
				context.put("sb", new HTMLJSWrapper(html));
				script.call(context);
				return html;
			} catch(ScriptException e) {
				throw new RedbackException("Error executing jsp", e);
			}			
		} else {
			throw new RedbackException("JSP " + name + " does not exist");
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

	protected DataMap getUrlPreview(Session session, String url) throws RedbackException {
		Pattern pattern = Pattern.compile("^(https:\\/\\/youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=|&v=)([^#&?]*).*");
	    Matcher matcher = pattern.matcher(url);
	    boolean matchFound = matcher.find();
	    if(matchFound) {
	    	String part = matcher.group(2);
	    	return new DataMap("url", url, "iframeurl", ("//www.youtube.com/embed/" + part));
	    } else {
	    	DataMap meta = HTMLMetaParser.parseUrl(url);
			meta.put("url", url);
			return meta;
	    }

	}
}