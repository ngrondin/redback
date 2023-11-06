package io.redback.managers.aimanager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import io.firebus.data.DataMap;
import io.firebus.data.ZonedTime;
import io.firebus.data.parse.DateParser;
import io.firebus.data.parse.NumberParser;
import io.firebus.data.parse.TimeParser;
import io.redback.client.ObjectClient;
import io.redback.client.ProcessAssignmentRemote;
import io.redback.client.ProcessClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.NLCommandResponse;

public class SequenceExecuter {
	protected ObjectClient objectClient;
	protected ProcessClient processClient;
	
	public SequenceExecuter(ObjectClient oc, ProcessClient pc) {
		objectClient = oc;
		processClient = pc;
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
		NLCommandResponse nlcr = new NLCommandResponse(context.textResponse.toString(), context.uiActions);
		return nlcr;
	}

	protected void runSequence(SEContext context, List<String> seq) throws RedbackException {
		for(int cur = 0; cur < seq.size(); cur++) {
			String curToken = seq.get(cur);
			if(curToken.startsWith("$")) {
				List<String> params = new ArrayList<String>();
				for(int i = cur + 1; i < seq.size() && !seq.get(i).startsWith("$"); i++)
					params.add(seq.get(i));
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
			else if(command.equals("$pop"))
				pop(context, params);				
			else if(command.equals("$respond"))
				respond(context, params);		
			else if(command.equals("$navto"))
				navTo(context, params);		
		} catch(Exception e) {
			//Just ignore exceptions
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
		if(params.size() >= 2 && cl != null) {
			String attribute = params.get(0);
			Object value = getValue(context, params.subList(1, params.size()));
			for(RedbackObjectRemote ror: listObjectsFromContext(context)) {
				ror.set(attribute, value);
			}
		}
	}
	
	protected void link(SEContext context, List<String> params) throws RedbackException {
		SEContextLevel cl = context.getContextLevel();
		if(params.size() >= 3 && cl != null) {
			String attribute = params.get(0);
			String relatedObjectName = params.get(1);
			RedbackObjectRemote target = findObject(context, relatedObjectName, params.subList(2, params.size()), false);
			for(RedbackObjectRemote ror: listObjectsFromContext(context)) {
				ror.setRelated(attribute, target);
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
	
	protected void pop(SEContext context, List<String> params) throws RedbackException {
		context.popContextLevel();
	}
	
	protected void respond(SEContext context, List<String> params) throws RedbackException {
		context.addResponse(getValue(context, params).toString());
	}
	
	protected void navTo(SEContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			String view = params.get(0);
			SEContextLevel c = context.getContextLevel();
			if(params.size() == 1) {
				if(c instanceof ObjectContext) {
					ObjectContext oc = (ObjectContext)c;
					context.uiActions.add("navtouid");
					context.uiActions.add(view);
					context.uiActions.add(oc.uid);
				} else if(c instanceof ListContext) {
					ListContext lc = (ListContext)c;
					context.uiActions.add("navtosearch");
					context.uiActions.add(view);
					context.uiActions.add(lc.search);
				} else {
					context.uiActions.add("navtosearch");
					context.uiActions.add(view);
				}
			} else if(params.size() >= 2) {
				Object val = getValue(context, params.subList(1, params.size()));
				context.uiActions.add("navtosearch");
				context.uiActions.add(view);
				context.uiActions.add(val.toString());
			}		
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
		DataMap filter = null;
		String search = null;
		if(params.size() == 1) {
			search = getValue(context, params).toString();
		} else if(params.size() >= 2) {
			String key = params.get(0);
			Object val = getValue(context, params.subList(1, params.size()));
			filter = new DataMap(key, val);
		}
		List<RedbackObjectRemote> list = objectClient.listObjects(context.session, objectName, filter, search, null, false, true, 0, 100);
		if(addToContext)
			context.pushContextLevel(new ListContext(list, filter, search));
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
			if(token.startsWith("#")) {
				int l = -1;
				String attribute = token;
				while(attribute.startsWith("#")) {
					l++;
					attribute = attribute.substring(1);
				}
				SEContextLevel cl = context.getContextLevel(l);
				if(cl instanceof ObjectContext) {
					ObjectContext oc = (ObjectContext)cl;
					if(attribute.equals("uid"))
						val = oc.object.getUid();
					else
						val = oc.object.getObject(attribute);
				}
			} else {
				val = parseString(token);
			}
			if(tokens.size() == 1) {
				return val;
			} else {
				if(sb.length() > 0) sb.append(" ");
				sb.append(val.toString());
			}
		}
		return sb.toString();
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
