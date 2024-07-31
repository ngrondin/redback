package io.redback.managers.aimanager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.data.ZonedTime;
import io.firebus.data.parse.DateParser;
import io.firebus.data.parse.DurationParser;
import io.firebus.data.parse.NumberParser;
import io.firebus.data.parse.TimeParser;
import io.firebus.logging.Logger;
import io.redback.client.ConfigClient;
import io.redback.client.ObjectClient;
import io.redback.client.ProcessAssignmentRemote;
import io.redback.client.ProcessClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.ConfigCache;
import io.redback.utils.Convert;
import io.redback.utils.NLCommandResponse;

public class SequenceExecuter {
	protected ConfigClient configClient;
	protected ObjectClient objectClient;
	protected ProcessClient processClient;
	protected ConfigCache<DataMap> objectConfigs;
	
	public SequenceExecuter(ConfigClient cc, ObjectClient oc, ProcessClient pc) {
		configClient = cc;
		objectClient = oc;
		processClient = pc;
		objectConfigs = new ConfigCache<DataMap>(configClient, "rbo", "object", 3600000, new ConfigCache.ConfigFactory<DataMap>() {
			public DataMap createConfig(DataMap map) throws Exception {
				return map;
			}});
	}
	
	public NLCommandResponse runSequence(Session session, String seqStr, SEContextLevel cl) throws RedbackException {
		SEContext context = new SEContext(session);
		if(cl != null) {
			if(cl instanceof ObjectContext) {
				ObjectContext oc = (ObjectContext)cl;
				if(oc.object == null)
					oc.object = objectClient.getObject(session, oc.objectname, oc.uid, false, true);
			} else if(cl instanceof ListContext) {
				ListContext lc = (ListContext)cl;
				if(lc.list == null)
					lc.list = objectClient.listObjects(context.session, lc.objectname, lc.filter, lc.search, null, false, true, 0, 100);
			}
			context.pushContextLevel(cl);
		}
		List<String> seq = Arrays.asList(seqStr.trim().split(" "));
		runSequence(context, seq);
		NLCommandResponse nlcr = new NLCommandResponse(context.textResponse.toString(), seqStr, context.uiActions);
		return nlcr;
	}

	protected void runSequence(SEContext context, List<String> seq) throws RedbackException {
		for(int cur = 0; cur < seq.size(); cur++) {
			String curToken = seq.get(cur);
			if(curToken.startsWith("$")) {
				List<String> params = getTokensUntil(seq, cur + 1, "$"); 
				runCommand(context, curToken, params);
				cur += params.size();					
			}
		}
	}
	
	protected void runCommand(SEContext context, String command, List<String> params) throws RedbackException {
		try {
			if(command.equals("$find"))
				find(context, params);
			else if(command.equals("$list"))
				list(context, params);
			else if(command.equals("$create"))
				create(context, params);
			else if(command.equals("$update"))
				update(context, params);	
			else if(command.equals("$link"))
				link(context, params);	
			else if(command.equals("$action"))
				action(context, params);
			else if(command.equals("$execute"))
				execute(context, params);
			else if(command.equals("$pop"))
				pop(context, params);				
			else if(command.equals("$reset"))
				reset(context);				
			else if(command.equals("$respond"))
				respond(context, params);		
			else if(command.equals("$navto"))
				navTo(context, params);		
			else if(command.equals("$navtocontext"))
				navToContext(context, params);		
			else if(command.equals("$opentab"))
				openTab(context, params);	
			else if(command.equals("$launchreport"))
				launchReport(context, params);
		} catch(Exception e) {
			Logger.severe("rb.ai.runcommand", e);
		}
	}
	
	protected void find(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 2) {
			String objectname = params.get(0);
			findObject(context, objectname, params.subList(1, params.size()), true);
		}
	}
	
	protected void list(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 2) {
			String objectname = params.get(0);
			listObjects(context, objectname, params.subList(1, params.size()), true);
		}
	}
	
	protected void create(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			String objectname = params.get(0);
			RedbackObjectRemote ror = objectClient.createObject(context.session, objectname, null, null, false, true);
			context.pushContextLevel(new ObjectContext(ror));
		}
	}
	
	protected void update(SEContext context, List<String> params) throws RedbackException {
		SEContextLevel cl = context.getContextLevel();
		if(params.size() >= 2 && cl != null && params.get(0).startsWith("@")) {
			List<RedbackObjectRemote> list = listObjectsFromContext(context);
			String objectName = list.get(0).getObjectName();
			if(list.size() > 0) {
				DataMap data = new DataMap();
				int i = 0;
				while(i < params.size()) {
					String key = params.get(i).substring(1);
					List<String> valTokens = getTokensUntil(params, i + 1, "@");
					DataMap relCfg = getAttributeRelationship(context, objectName, key);
					if(relCfg != null) {
						RedbackObjectRemote ror = findObject(context, relCfg.getString("name"), valTokens, false);
						String link = relCfg.getString("linkattribute");
						data.put(key, link.equals("uid") ? ror.getUid() : ror.getString(link));
					} else {
						data.put(key, getValue(context, valTokens));					
					}
					i += 1 + valTokens.size();
				}
				for(RedbackObjectRemote ror: list) {
					ror.set(data);
				}				
			}

		}
	}
	
	protected void link(SEContext context, List<String> params) throws RedbackException {
		SEContextLevel cl = context.getContextLevel();
		if(params.size() >= 3 && cl != null && params.get(0).startsWith("@")) {
			String attribute = params.get(0).substring(1);
			String relatedObjectName = params.get(1);
			RedbackObjectRemote target = findObject(context, relatedObjectName, params.subList(2, params.size()), false);
			for(RedbackObjectRemote ror: listObjectsFromContext(context)) {
				ror.set(attribute, target);
			}
		}
	}
	
	protected void action(SEContext context, List<String> params) throws RedbackException {
		SEContextLevel cl = context.getContextLevel();
		if(params.size() >= 1 && cl != null) {
			String action = getValue(context, params).toString();
			if(action != null && action.length() > 0) {
				for(RedbackObjectRemote ror: listObjectsFromContext(context)) {
					ProcessAssignmentRemote par = processClient.getAssignment(context.session, new DataMap("data.objectname", ror.getObjectName(), "data.uid", ror.getUid()));
					if(par != null)
						par.action(action);	
				}
			}
		}		
	}
	
	protected void execute(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			String scriptname = params.get(0);
			DataMap data = new DataMap();
			int i = 1;
			while(i < params.size()) {
				String key = params.get(i).substring(1);
				List<String> valTokens = getTokensUntil(params, i + 1, "@");
				data.put(key, getValue(context, valTokens));					
				i += 1 + valTokens.size();
			}
			objectClient.execute(context.session, scriptname, data);
		}
	}
	
	protected void reset(SEContext context) throws RedbackException {
		context.reset();
	}
	
	protected void pop(SEContext context, List<String> params) throws RedbackException {
		context.popContextLevel();
	}
	
	protected void respond(SEContext context, List<String> params) throws RedbackException {
		Object val = getValue(context, params);
		if(val != null)
			context.addResponse(val.toString());
	}
	
	protected void navTo(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			String view = params.get(0);
			SEContextLevel c = context.getContextLevel();
			if(params.size() == 1) {
				if(c instanceof ObjectContext) {
					ObjectContext oc = (ObjectContext)c;
					context.uiActions.add("$navtouid");
					context.uiActions.add(view);
					context.uiActions.add(oc.uid);
				} else if(c instanceof ListContext) {
					ListContext lc = (ListContext)c;
					if(lc.list.size() == 1) {
						context.uiActions.add("$navtouid");
						context.uiActions.add(view);
						context.uiActions.add(lc.list.getFirst().getUid());
					} else if(lc.list.size() > 0 && (lc.search != null || lc.filter != null)) {
						context.uiActions.add("$navtofilter");
						context.uiActions.add(view);
						context.uiActions.add(lc.search);						
						context.uiActions.add(lc.getUnresolvedUIFilter().toString(true, true));
					} else if(lc.list.size() > 0){
						context.uiActions.add("$navtouids");
						context.uiActions.add(view);
						String[] uids = new String[lc.list.size()];
						for(int i = 0; i < lc.list.size(); i++)
							uids[i] = lc.list.get(i).getUid();							
						context.uiActions.add(String.join(",", uids));							
					} else {
						context.addResponse("Can't find anything");
					}
				} else {
					context.uiActions.add("$navto");
					context.uiActions.add(view);
				}
			} else if(params.size() >= 2) {
				Object val = getValue(context, params.subList(1, params.size()));
				context.uiActions.add("$navtofilter");
				context.uiActions.add(view);
				context.uiActions.add(val.toString());
				context.uiActions.add(null);
			}		
		}
	}
	
	protected void navToContext(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			String view = params.get(0);
			SEContextLevel c = context.getContextLevel();
			if(params.size() == 1) {
				if(c instanceof ObjectContext) {
					ObjectContext oc = (ObjectContext)c;
					context.uiActions.add("$navtocontext");
					context.uiActions.add(view);
					context.uiActions.add(oc.uid);
				}
			}
		}
	}
	
	protected void openTab(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			Object o = getValue(context, params);
			String tabId = o != null ? o.toString() : "";
			context.uiActions.add("$opentab");
			context.uiActions.add(tabId);
		}
	}
	
	protected void launchReport(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			String reportName = params.get(0);
			context.uiActions.add("$launchreport");
			context.uiActions.add(reportName);
		}
	}
	
	protected RedbackObjectRemote findObject(SEContext context, String objectName, List<String> params, boolean addToContext) throws RedbackException {
		List<RedbackObjectRemote> list = listObjects(context, objectName, params, false);
		if(list.size() > 0) {
			RedbackObjectRemote ror = list.get(0);
			if(addToContext) 
				context.pushContextLevel(new ObjectContext(ror));
			return ror;
		} else {
			return null;
		}
	}
	
	protected List<RedbackObjectRemote> listObjects(SEContext context, String objectName, List<String> params, boolean addToContext) throws RedbackException {
		DataMap filter = new DataMap();
		String search = null;
		if(params.size() > 0) {
			int i = 0;
			while(i < params.size()) {
				if(params.get(i).startsWith("@")) {
					String key = params.get(i).substring(1);
					List<String> valTokens = getTokensUntil(params, i + 1, "@");
					DataMap relCfg = getAttributeRelationship(context, objectName, key);
					if(relCfg != null) {
						List<RedbackObjectRemote> list = listObjects(context, relCfg.getString("name"), valTokens, false);
						String link = relCfg.getString("linkattribute");
						DataList inList = new DataList();
						for(RedbackObjectRemote ror: list) 
							inList.add(link.equals("uid") ? ror.getUid() : ror.getString(link));
						filter.put(key, new DataMap("$in", inList));
					} else {
						Object val = getValue(context, valTokens);
						if(val instanceof DataList)
							filter.put(key, new DataMap("$in", (DataList)val));
						else if(val instanceof String)
							filter.put(key, new DataMap("$regex", "(?i)" + val));
						else if(val instanceof Number)
							filter.put(key, new DataMap("$regex", "(?i)" + val.toString()));						
						else
							filter.put(key, val);
					}				
					i += 1 + valTokens.size();
				} else {
					List<String> valTokens = getTokensUntil(params, 0, "@");
					search = getValue(context, valTokens).toString();
					i += valTokens.size();
				}
			}
		}
		List<RedbackObjectRemote> list = objectClient.listObjects(context.session, objectName, filter, search, null, false, true, 0, 1000);
		if(addToContext)
			context.pushContextLevel(new ListContext(list, filter, search, null));
		return list;
	}
	
	protected List<RedbackObjectRemote> listObjectsFromContext(SEContext context) throws RedbackException {
		SEContextLevel cl = context.getContextLevel();
		List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
		if(cl instanceof ObjectContext) {
			list.add(((ObjectContext)cl).object);
		} else if(cl instanceof ListContext) {
			list.addAll(((ListContext)cl).list);
		}
		return list;
	}
	
	
	protected Object getValue(SEContext context, List<String> tokens) throws RedbackException {
		StringBuilder sb = new StringBuilder();
		for(String token: tokens) {			
			Object val = null;
			if(token.startsWith("*")) {
				val = getValueFromContext(context, token);
			} else if(token.startsWith(">") || token.startsWith("<")) {
				val = getComparativeValue(token);
			} else {
				val = parseString(token);
			}
			if(tokens.size() == 1) {
				return val;
			} else {
				if(sb.length() > 0) sb.append(" ");
				sb.append(val != null ? val.toString() : "null");
			}
		}
		return sb.toString();
	}
	
	protected Object getValueFromContext(SEContext context, String attribute) throws RedbackException {
		if(attribute.startsWith("*")) {
			int l = -1;
			while(attribute.startsWith("*")) {
				l++;
				attribute = attribute.substring(1);
			}
			SEContextLevel cl = context.getContextLevel(l);
			if(cl instanceof ObjectContext) {
				ObjectContext oc = (ObjectContext)cl;
				if(attribute.equals("") || attribute.equals("uid")) return oc.object.getUid();
				else return oc.object.getObject(attribute);
			} else if(cl instanceof ListContext) {
				ListContext lc = (ListContext)cl;
				if(attribute.equals("_filter")) {
					return lc.filter;
				} else if(attribute.equals("_sort")) {
						return lc.sort;
				} else {
					DataList list = new DataList();
					for(RedbackObjectRemote ror: lc.list) {
						Object v = attribute.equals("") || attribute.equals("uid") ? ror.getUid() : ror.getObject(attribute);
						list.add(v);
					}
					return list;					
				}
			}
		}
		return null;
	}
	
	protected Object getComparativeValue(String str) throws RedbackException {
		Object val = null;
		String key = str.startsWith(">") ? "$gt" : str.startsWith("<") ? "$lt" : "";
		str = str.substring(1);
		if(str.startsWith("now")) {
			Object parsedVal = parseString(str.substring(3));
			long dur = parsedVal != null && parsedVal instanceof Long ? (long)parsedVal : 0;
			val = new Date(System.currentTimeMillis() + dur);
			str = str.substring(3);
		} else {
			val = parseString(str);
		}
		return new DataMap(key, val);
	}
	
	protected Object parseString(String str) {
		if(str.equalsIgnoreCase("true")  ||  str.equalsIgnoreCase("false")) {
			return str.equalsIgnoreCase("true") ? true : false;
		}
		else if(str.equalsIgnoreCase("null")) {
			return null;
		}
		else
		{
			ZonedDateTime dateValue = DateParser.parse(str);
			if(dateValue != null) {
				return Date.from(dateValue.toInstant());
			} else {
				ZonedTime timeValue = TimeParser.parse(str);
				if(timeValue != null) {
					return timeValue;
				} else {
					Long durationValue = DurationParser.parse(str);
					if(durationValue != null) {
						return durationValue;
					} else {
						Number numberValue = NumberParser.parse(str);
						if(numberValue != null) {
							return numberValue;
						} else {
							return str;	
						}						
					}
				}
			}
		}		
	}
	
	protected List<String> getTokensUntil(List<String> tokens, int start, String c) {
		List<String> ret = new ArrayList<String>();
		for(int i = start; i < tokens.size() && !tokens.get(i).startsWith(c); i++)
			ret.add(tokens.get(i));
		return ret;
	}
	
	protected DataMap getAttributeRelationship(SEContext context, String objectName, String attribute) throws RedbackException {
		DataMap cfg = objectConfigs.get(context.session, objectName);
		DataList attrList = cfg.getList("attributes");
		for(int i = 0; i < attrList.size(); i++) {
			DataMap attrCfg = attrList.getObject(i);
			if(attrCfg.getString("name").equals(attribute)) {
				return attrCfg.getObject("relatedobject");
			}
		}
		return null;
	}
}
