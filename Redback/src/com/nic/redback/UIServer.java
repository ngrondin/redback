package com.nic.redback;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONEntity;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONLiteral;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.UserProfile;

public class UIServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String resourceService;
	protected String resourceServiceType;

	public UIServer( JSONObject c)
	{
		super(c);
		configService = config.getString("configservice");
		resourceServiceType = config.getString("resourceservicetype");
		resourceService = config.getString("resourceservice");
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		logger.info("UI service start");
		Payload response = new Payload();
		try
		{
			StringBuilder sb = new StringBuilder();
			UserProfile userProfile = null;
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
					sb.append(getResource(name, userProfile));
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
						userProfile = authenticate(username, password);
						response.metadata.put("sessionid", userProfile.getSessionId());
					}
					else if(sessionId != null)
					{
						userProfile = validateSession(sessionId);
					}					
					
					if(category.equals("app"))
					{
						logger.info("Get app " + name);
						sb.append(getApp(name, userProfile));
					}
					else if(category.equals("view"))
					{
						logger.info("Get view " + name);
						sb.append(getView(name, userProfile));
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
	
	protected String getApp(String name, UserProfile userProfile) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		StringBuilder sb = new StringBuilder();
		if(userProfile != null)
		{
			JSONObject result = request(configService, "{object:rbui_app,filter:{name:" + name + "}}");
			if(result != null)
			{
				JSONObject appConfig = result.getObject("result.0");
				String page = appConfig.getString("html");
				String defaultView = appConfig.getString("defaultview");
				String label = appConfig.getString("label");
				String html = getHTMLPage(page).replace("#defaultview#", defaultView).replace("#appname#", label);
				
				String menuFragment = getHTMLFragment("navigation");
				String menuGroupFragment = getHTMLFragment("navigationgroup");
				String menuItemFragment = getHTMLFragment("navigationitem");
				StringBuilder menuGroupBuilder = new StringBuilder();
				for(int i = 0; i < appConfig.getList("menugroups").size(); i++)
				{
					JSONObject menuGroup = appConfig.getList("menugroups").getObject(i);
					JSONList itemList = menuGroup.getList("menuitems");
					StringBuilder menuItemBuilder = new StringBuilder();
					for(int j = 0; j < itemList.size(); j++)
					{
						JSONObject menuItem = itemList.getObject(j);
						menuItemBuilder.append(menuItemFragment.replace("#itemlabel#", menuItem.getString("label")).replace("#viewname#", menuItem.getString("view")) + "\r\n");
					}
					menuGroupBuilder.append(menuGroupFragment.replace("#grouplabel#", menuGroup.getString("label")).replace("#content#", menuItemBuilder.toString().trim()).replace("#groupid#", "group" + i) + "\r\n");
				}			
				menuFragment = injectInHTML(menuFragment, "content", menuGroupBuilder.toString().trim());
				
				html = injectInHTML(html, "menu", menuFragment);
				sb.append(html);
			}
			else
			{
				sb.append("<html><head></head>\r\n");
				sb.append("<body>\r\n");
				sb.append("Application " + name + " does not exist\r\n");
				sb.append("</div></body></html>");					
			}
		}
		else
		{
			sb.append(getHTMLPage("login").replace("#get#", "app/" + name));
		}
		return sb.toString();
	}
	

	
	protected String getView(String name, UserProfile userProfile) throws JSONException
	{
		StringBuilder sb = new StringBuilder();
		if(userProfile != null)
		{
			sb.append(processComponentJSON(new JSONObject("{type:view, name:" + name + "}")));
		}
		else
		{
			sb.append("Not logged in");
		}
		return sb.toString();
	}
	
	
	protected String getResource(String name, UserProfile userProfile) throws FunctionErrorException, FunctionTimeoutException, JSONException, RedbackException, IOException
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
	
	
	protected String getHTMLPage(String name)
	{
		return getHTML("pages/" + name);
	}
	
	protected String getHTMLFragment(String name)
	{
		return getHTML("fragments/" + name);
	}
	
	protected String getHTML(String name)
	{
		try
		{
			InputStream is = this.getClass().getResourceAsStream("/com/nic/redback/html/" + name + ".html");
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			is.close();
			String str = new String(bytes);
			return str;
		}
		catch(IOException e)
		{
			logger.severe("Error when trying to retreive " + name + ".html : " + e.getMessage());
			return formatErrorMessage("Error when trying to retreive " + name + ".html", e);
		}		
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
	
	protected String convertJSONToAttributeString(JSONObject obj)
	{
		String ret = obj.toString();
		ret = ret.replaceAll("\r", "");
		ret = ret.replaceAll("\n", "");
		ret = ret.replaceAll("\t", "");
		ret = ret.replaceAll("\"", "'");
		return ret;
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
	
	
	protected String processComponentJSON(JSONObject componentJSON)
	{
		String type = componentJSON.getString("type");
		String componentStr = getHTMLFragment(type);
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
				try
				{
					JSONObject result = request(configService, "{object:rbui_view,filter:{name:" + viewName + "}}");
					if(result != null)
						componentJSON = result.getObject("result.0");
				}
				catch(Exception e)
				{
					componentStr = formatErrorMessage("Error retrieving view " + viewName, e);
				}
			}			

			if(inlineStyle.length() > 0)
				componentJSON.put("inlineStyle", inlineStyle);
			
			int pos1 = 0;
			int pos2 = 0;
			while(pos1 > -1)
			{
				pos1 = componentStr.indexOf("#", pos1 + 1);
				if(pos1 > -1)
				{
					pos2 = componentStr.indexOf("#", pos1 + 1);
					if(pos2 > -1)
					{
						String propStr = componentStr.substring(pos1 + 1, pos2);
						if(propStr.matches("[a-zA-Z0-9]+")  &&  !propStr.equals("content"))
						{
							JSONEntity valEntity = componentJSON.get(propStr);
							String value = "";
							if(propStr.equals("show"))
								value = "true";
							if(valEntity instanceof JSONLiteral)
								value = ((JSONLiteral)valEntity).getString();
							else if(valEntity instanceof JSONObject)
								value = convertJSONToAttributeString((JSONObject)valEntity);
							componentStr = componentStr.substring(0, pos1) + value + componentStr.substring(pos2 + 1);
						}
					}
				}
			}
			
			if(componentStr != null  &&  componentStr.indexOf("#content#") >= 0)
			{
				int posContent = componentStr.indexOf("#content#");
				int posNewLine = componentStr.substring(0, posContent).lastIndexOf("\r\n");
				String indentStr = componentStr.substring(posNewLine + 2, posContent);
				StringBuilder sb = new StringBuilder();
				JSONList content = componentJSON.getList("content");
				if(content != null)
				{
					for(int i = 0; i < content.size(); i++)
					{
						if(i > 0)
							sb.append("\r\n");
						sb.append(processComponentJSON(content.getObject(i)));
					}
				}
				String contentStr = sb.toString();
				contentStr = contentStr.replace("\r\n", "\r\n" + indentStr);
				componentStr = componentStr.replace("#content#", contentStr);
			}
		}

		return componentStr;
	}



}
