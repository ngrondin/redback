package com.nic.redback;

import java.io.InputStream;
import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class UIServer extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
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
						JSONObject initialFilter = viewConfig.getObject("initialfilter");
						String attrStr = "";
						if(objectName != null)
							attrStr += " rb-object=\"" + objectName + "\"";
						if(masterObject != null)
							attrStr += " rb-master=\"" +  convertJSONToAttributeString(masterObject) + "\"";
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
			else if(type.equals("vscroll"))
			{
				preString = "<div class=\"vscroll\">";
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
				String line1 = obj.getString("line1");
				String line2 = obj.getString("line2");
				if(line1 != null  &&  line2 != null)
				{
					line1 = line1.replace("{{", "{{item.");
					line2 = line2.replace("{{", "{{item.");
					preString = "<md-list flex=\"\"><md-list-item class=\"md-2-line\" ng-repeat=\"item in list\" ng-click=\"$emit('ObjectSelectedEmit', item)\"><div class=\"md-list-item-text\" layout=\"column\"><h3>" + line1 + "</h3><h4>" + line2 + "</h4>";
					postString = "</md-list-item></md-list>";
				}
			}
			else if(type.equals("text"))
			{
				String attribute = obj.getString("attribute");
				if(attribute != null)
				{
					preString = "<div>{{object.data." + attribute + "}}";
					postString = "</div>";
				}
			}
			else if(type.equals("input"))
			{
				String label = obj.getString("label");
				String attribute = obj.getString("attribute");
				if(attribute != null)
				{
					
					preString = "<md-input-container class=\"md-block\" ><label>" + label + "</label><input ng-model=\"object.data." + attribute + "\" ng-disabled=\"!object.validation." + attribute + ".editable\">";
					postString = "</md-input-container>";
				}
			}
			else if(type.equals("search"))
			{
				preString = "<md-input-container class=\"md-block\" ><input ng-model=\"searchText\" ng-change=\"search(searchText)\" aria-label=\"Search\">";
				postString = "</md-input-container>";
			}
			else if(type.equals("datepicker"))
			{
				String label = obj.getString("label");
				String attribute = obj.getString("attribute");
				if(attribute != null)
				{
					preString = "<md-input-container class=\"md-block\" flex-gt-sm=\"\"><label>" + label + "</label><md-datepicker  ng-model=\"object.data." + attribute + "\" ng-disabled=\"!object.validation." + attribute + ".editable\">";
					postString = "</md-datepicker></md-input-container>";
				}
			}
			else if(type.equals("select"))
			{
				String label = obj.getString("label");
				String attribute = obj.getString("attribute");
				String displayExpression = obj.getString("displayexpression");
				if(attribute != null  &&  displayExpression != null)
				{
					displayExpression = "'" + displayExpression.replace("{{", "' + item.data.").replace("}}", " + '") + "'";
					preString = "<md-input-container class=\"md-block\" flex-gt-sm=\"\"><label>" + label + "</label><md-select ng-model=\"object.related." + attribute + "\" md-on-open=\"loadRelatedObjectList('" + attribute +"', null)\" ng-change=\"relatedObjectHasChanged('" + attribute +"')\" ng-disabled=\"!object.validation." + attribute + ".editable\"><md-option ng-repeat=\"item in relatedObjectList." + attribute +"\" ng-value=\"item\">{{" + displayExpression + "}}";
					postString = "</md-option></md-select></md-input-container>";
				}
			}
			else if(type.equals("autocomplete"))
			{
				String label = obj.getString("label");
				String attribute = obj.getString("attribute");
				String displayExpression = obj.getString("displayexpression");
				String listExpression = obj.getString("listexpression");
				if(attribute != null  &&  displayExpression != null)
				{
					displayExpression = "'" + displayExpression.replace("{{", "' + item.data.").replace("}}", " + '") + "'";
					listExpression = listExpression.replace("{{", "{{item.data.");
					preString = "<md-autocomplete cflex=\"\" required=\"\" md-input-name=\"autocompleteField\"  md-items=\"item in loadRelatedObjectList('" + attribute +"', dynamicSearchText)\" md-selected-item=\"object.related." + attribute + "\" md-selected-item-change=\"relatedObjectHasChanged('" + attribute +"')\" md-search-text=\"dynamicSearchText\" md-item-text=\"" + displayExpression + "\" md-require-match=\"\" md-floating-label=\"" + label + "\" ng-disabled=\"!object.validation." + attribute + ".editable\">";
					preString += "<md-item-template>" + listExpression + "</md-item-template>";
					postString = "</md-autocomplete>";
				}
			}
			else if(type.equals("button"))
			{
				String action = obj.getString("action");
				String param = obj.getString("param");
				String label = obj.getString("label");
				if(action != null  &&  label != null)
				{
					preString = "<md-button class=\"md-primary md-raised\" ng-click=\"" + action + "(" + (param != null ? ("'" + param + "'") : "") + ");\">" + label;
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
