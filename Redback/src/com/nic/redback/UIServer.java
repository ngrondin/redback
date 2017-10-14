package com.nic.redback;

import java.io.InputStream;
import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class UIServer extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String configService;
	protected String resourceService;
	protected String resourceServiceType;

	public UIServer(JSONObject c)
	{
		super(c);
		configService = config.getString("configservice");
		resourceServiceType = config.getString("resourceservicetype");
		resourceService = config.getString("resourceservice");
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		Payload response = new Payload();
		try
		{
			StringBuilder sb = new StringBuilder();
			String mime = "";
			String get = payload.metadata.get("get");
			if(get == null  &&  payload.getString().length() > 0)
			{
				try
				{
					JSONObject request = new JSONObject(payload.getString());
					get = request.getString("get");
				}
				catch(Exception e)	{}
			}
			
			if(get != null)
			{
				String[] parts = get.split("/");
				String category = parts[0];
				String object = parts[1];
				
				if(category.equals("app"))
				{
					JSONObject result = request(configService, "{object:rbui_app,filter:{name:" + object + "}}");
					if(result != null)
					{
						JSONObject appConfig = result.getObject("result.0");
						String moduleName = appConfig.getString("module");
						String view = appConfig.getString("view");
						sb.append("<html>\r\n");
						sb.append("<head>\r\n");
						sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"../resource/main.css\"></head>\r\n");
						sb.append("<link rel=\"stylesheet\" href=\"https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.min.css\">\r\n");
						sb.append("<script src = \"https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js\"></script>\r\n");
						sb.append("<script src = \"https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-animate.min.js\"></script>\r\n");
						sb.append("<script src = \"https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-aria.min.js\"></script>\r\n");
						sb.append("<script src = \"https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-messages.min.js\"></script>\r\n");
						sb.append("<script src = \"https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.min.js\"></script>\r\n");
						sb.append("</head>\r\n");
						sb.append("<body>\r\n");
						sb.append("<script src = \"../resource/" + moduleName + ".js\"></script>\r\n");
						sb.append("<div ng-app=\"" + moduleName + "\">\r\n");
						sb.append("<ng-include src=\"'../view/" + view + "'\"></ng-include>\r\n");
						sb.append("</div></body></html>");
					}
				}
				else if(category.equals("view"))
				{
					JSONObject result = request(configService, "{object:rbui_view,filter:{name:" + object + "}}");
					if(result != null)
					{
						JSONObject viewConfig = result.getObject("result.0");
						String controllerName = viewConfig.getString("controller");
						String objectName = viewConfig.getString("object");
						JSONObject masterObject = viewConfig.getObject("master");
						//JSONObject parentRelationship = viewConfig.getObject("parentrelationship");
						JSONObject initialFilter = viewConfig.getObject("initialfilter");
						String attrStr = "";
						if(objectName != null)
							attrStr += " rb-object=\"" + objectName + "\"";
						if(masterObject != null)
							attrStr += " rb-master=\"" +  convertJSONToAttributeString(masterObject) + "\"";
						//if(parentRelationship != null)
						//	attrStr += " rb-master-relationship=\"" + convertJSONToAttributeString(parentRelationship) + "\"";
						if(initialFilter != null)
							attrStr += " rb-initial-filter=\"" + convertJSONToAttributeString(initialFilter) + "\"";
						sb.append("<div ng-controller=\"" + controllerName + "\"" + attrStr + ">");
						JSONList content = viewConfig.getList("content");
						if(content != null)
							for(int i = 0; i < content.size(); i++)
								sb.append(processFormObject(content.getObject(i), 0));
						sb.append("</div>");
					}
				}
				else if(category.equals("resource"))
				{
					String fileStr = null;
					if(resourceServiceType.equals("filestorage"))
					{
						Payload result = firebus.requestService(resourceService, new Payload(object));
						fileStr = result.getString();
					}
					else if(resourceServiceType.equals("db"))
					{
						JSONObject result = request(resourceService, "{object:rbui_resource,filter:{name:" + object + "}}");
						fileStr = result.getList("result").getObject(0).getString("content");
						sb.append(fileStr);
					}
					else
					{
						InputStream is = this.getClass().getResourceAsStream("/com/nic/redback/css/" + object);
						byte[] bytes = new byte[is.available()];
						is.read(bytes);
						fileStr = new String(bytes);
					}

					if(fileStr != null)
					{
						sb.append(fileStr);
						if(object.endsWith(".js"))
							mime = "application/javascript";
						else if(object.endsWith(".css"))
							mime = "text/css";
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
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}
	
	protected JSONObject request(String service, String request) throws JSONException, FunctionErrorException
	{
		Payload reqPayload = new Payload(request);
		Payload respPayload = firebus.requestService(configService, reqPayload);
		String respStr = respPayload.getString();
		JSONObject result = new JSONObject(respStr);
		return result;
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
	
	protected String processFormObject(JSONObject obj, int indent)
	{
		StringBuilder sb = new StringBuilder();
		String type = obj.getString("type");
		if(type != null)
		{
			String preString = null;
			String postString = null;
			if(type.equals("section"))
			{
				String orientation = obj.getString("orientation");
				String sectionClass = "vsection";
				if(orientation != null && orientation.equals("horizontal"))
					sectionClass = "hsection";
				preString = "<div class=\"" + sectionClass + "\">";
				postString = "</div>";
			}
			else if(type.equals("view"))
			{
				String name = obj.getString("name");
				if(name != null)
				{
					preString = "<ng-include src=\"'../view/" + name + "'\">";
					postString = "</ng-include>";
				}
			}
			else if(type.equals("list"))
			{
				String data1 = obj.getString("data1");
				String data2 = obj.getString("data2");
				if(data1 != null  &&  data2 != null)
				{
					preString = "<md-list flex=\"\"><md-list-item class=\"md-2-line\" ng-repeat=\"item in list\" ng-click=\"selectItem(item)\"><div class=\"md-list-item-text\" layout=\"column\"><h3>{{item.data." + data1 + "}}</h3><h4>{{item.data." + data2 + "}}</h4>";
					postString = "</md-list-item></md-list>";
				}
			}
			else if(type.equals("text"))
			{
				String data = obj.getString("data");
				if(data != null)
				{
					preString = "<div>{{object.data." + data + "}}";
					postString = "</div>";
				}
			}
			else if(type.equals("input"))
			{
				String label = obj.getString("label");
				String data = obj.getString("data");
				if(data != null)
				{
					preString = "<md-input-container class=\"md-block\" ><label>" + label + "</label><input ng-model=\"object.data." + data + "\">";
					postString = "</md-input-container>";
				}
			}
			else if(type.equals("datepicker"))
			{
				String label = obj.getString("label");
				String data = obj.getString("data");
				if(data != null)
				{
					preString = "<md-input-container class=\"md-block\" flex-gt-sm=\"\"><label>" + label + "</label><md-datepicker  ng-model=\"object.data." + data + "\">";
					postString = "</md-datepicker></md-input-container>";
				}
			}
			else if(type.equals("select"))
			{
				String label = obj.getString("label");
				String data = obj.getString("data");
				if(data != null)
				{
					preString = "<md-input-container class=\"md-block\" flex-gt-sm=\"\"><label>" + label + "</label><md-select  ng-model=\"object.data." + data + "\"><md-option ng-repeat=\"val in object.ctrl." + data + ".listofvalues\" value=\"{{val}}\">{{val}}";
					postString = "</md-option></md-select></md-input-container>";
				}
			}
			else if(type.equals("autocomplete"))
			{
				String label = obj.getString("label");
				String data = obj.getString("data");
				String relatedObjectDisplyAttribute= obj.getString("relatedobject.displayattribute");
				if(data != null)
				{
					preString = "<md-autocomplete cflex=\"\" required=\"\" md-input-name=\"autocompleteField\"  md-items=\"item in loadRelatedObjectList('" + data +"')\" md-selected-item=\"object.related." + data+ "\" md-item-text=\"object.related." + data+ ".data." + relatedObjectDisplyAttribute + "\" md-search-text=\"dynamicSearchText\" md-require-match=\"\" md-floating-label=\"" + label + "\">";
					preString +=	 "<md-item-template>{{item.data.name}}";
					postString = "</md-item-template></md-autocomplete>";
				}
			}
			else if(type.equals("button"))
			{
				String function = obj.getString("function");
				String label = obj.getString("label");
				if(function != null  &&  label != null)
				{
					preString = "<md-button class=\"md-primary md-raised\" ng-click=\"" + function + "()\">" + label;
					postString = "</md-button>";
				}
			}
			
			if(preString != null  &&  postString != null)
			{
				for(int i = 0; i < indent; i++)
					sb.append('\t');
				sb.append(preString);
				sb.append("\r\n");
				JSONList content = obj.getList("content");
				if(content != null)
				{
					for(int i = 0; i < content.size(); i++)
						sb.append(processFormObject(content.getObject(i), indent + 1));
				}
				for(int i = 0; i < indent; i++)
					sb.append('\t');
				sb.append(postString);	
				sb.append("\r\n");
			}
		}

		return sb.toString();
	}

}
