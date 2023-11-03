package io.redback.managers.aimanager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
	
	public NLCommandResponse runSequence(Session session, List<String> seq, ObjectContext oc) throws RedbackException {
		SeqExContext context = new SeqExContext(session);
		if(oc != null) {
			if(oc.objectname != null && oc.uid != null && oc.object == null) 
				oc.object = objectClient.getObject(session, oc.objectname, oc.uid, false, true);
			context.pushObjectContext(oc);
		}
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
		NLCommandResponse nlcr = new NLCommandResponse(context.textResponse.toString(), context.uiActions);
		return nlcr;
	}

	
	protected void runCommand(SeqExContext context, String command, List<String> params) throws RedbackException {
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
			else if(command.equals("$respond"))
				respond(context, params);		
			else if(command.equals("$navto"))
				navTo(context, params);		
		} catch(Exception e) {
			//Just ignore exceptions
		}
	}
	
	protected void find(SeqExContext context, List<String> params) throws RedbackException {
		if(params.size() >= 2) {
			String objectname = params.get(0);
			String search = getValue(context, params.subList(1, params.size())).toString();
			RedbackObjectRemote ror = findObject(context, objectname, search);
			if(ror != null) {
				context.pushObjectContext(ror, null, search);
			}
		}
	}
	
	protected void list(SeqExContext context, List<String> params) throws RedbackException {
		if(params.size() >= 2) {
			String objectname = params.get(0);
			String search = getValue(context, params.subList(1, params.size())).toString();
			List<RedbackObjectRemote> list = listObjects(context, objectname, search);
			context.pushObjectContext(list, null, search);
		}
	}
	
	protected void create(SeqExContext context, List<String> params) throws RedbackException {
		if(params.size() >= 1) {
			String objectname = params.get(0);
			RedbackObjectRemote ror = objectClient.createObject(context.session, objectname, null, null, false, true);
			context.pushObjectContext(ror, null, null);
		}
	}
	
	protected void update(SeqExContext context, List<String> params) throws RedbackException {
		ObjectContext oc = context.getObjectContext();
		if(params.size() >= 2 && oc != null && oc.object != null) {
			oc.object.set(params.get(0), getValue(context, params.subList(1, params.size())));
		}
	}
	
	protected void link(SeqExContext context, List<String> params) throws RedbackException {
		ObjectContext oc = context.getObjectContext();
		if(params.size() >= 3 && oc != null && oc.object != null) {
			String attribute = params.get(0);
			String relatedObjectName = params.get(1);
			String search = getValue(context, params.subList(2, params.size())).toString();
			if(search.length() > 0) {
				RedbackObjectRemote ror = findObject(context, relatedObjectName, search);
				if(ror != null) {
					oc.object.setRelated(attribute, ror);
				}
			}
		}
	}
	
	protected void action(SeqExContext context, List<String> params) throws RedbackException {
		ObjectContext oc = context.getObjectContext();
		if(params.size() >= 1 && oc != null && oc.object != null) {
			String action = getValue(context, params).toString();
			if(action != null && action.length() > 0) {
				ProcessAssignmentRemote par = processClient.getAssignment(context.session, new DataMap("data.objectname", oc.objectname, "data.uid", oc.uid));
				if(par != null)
					par.action(action);				
			}
		}
	}
	
	protected void respond(SeqExContext context, List<String> params) throws RedbackException {
		context.addResponse(getValue(context, params).toString());
	}
	
	protected void navTo(SeqExContext context, List<String> params) throws RedbackException {
		if(params.size() == 1 && context.getObjectContext() != null && context.getObjectContext().uid != null) {
			context.uiActions.add("navtouid");
			context.uiActions.add(params.get(0));
			context.uiActions.add(context.getObjectContext().uid);
		} else if(params.size() == 2) {
			context.uiActions.add("navtosearch");
			context.uiActions.add(params.get(0));
			if(params.get(1).startsWith("#") && context.getObjectContext() != null) {
				String attribute = params.get(1).substring(1);
				context.uiActions.add(context.getObjectContext().object.getString(attribute));
			} else {
				context.uiActions.add(params.get(1));				
			}
		}
	}
	
	protected Object getValue(SeqExContext context, List<String> tokens) throws RedbackException {
		StringBuilder sb = new StringBuilder();
		boolean nextIsDay = false;
		String dayStr = null;
		boolean nextIsTime = false;
		String timeStr = null;
		for(String token: tokens) {
			if(nextIsDay) {
				dayStr = token;
				nextIsDay = false;
			} else if(nextIsTime) {
				timeStr = token;
				nextIsTime = false;
			} else if(token.equals("%day")) {
				nextIsDay = true;
			} else if(token.equals("%time")) {
				nextIsTime = true;
			} else {
				if(sb.length() > 0) 
					sb.append(" ");
				sb.append(token);
			}
		}
		if(dayStr != null || timeStr != null) {
			return null; // getDate(dayStr, timeStr);
		} else {
			return parseString(sb.toString());			
		}
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
	
	
	/*protected Date getDate(String dayStr, String timeStr) {
		Date now = new Date();
		HawkingConfiguration config = new HawkingConfiguration();
		HawkingTimeParser parser = new HawkingTimeParser();
		Calendar cal = Calendar.getInstance();
		if(dayStr != null) {
			DatesFound datesFound = parser.parse(dayStr, now, config, "eng");
			List<ParserOutput> list = datesFound.getParserOutputs();
			if(list.size() > 0) {
				DateTime foundDay = list.get(0).getDateRange().getStart();
				cal.set(Calendar.YEAR, foundDay.getYear());
				cal.set(Calendar.MONTH, foundDay.getMonthOfYear());
				cal.set(Calendar.DATE, foundDay.getDayOfMonth());
			}
		}
		if(timeStr != null) {
			DatesFound datesFound = parser.parse(timeStr, now, config, "eng");
			List<ParserOutput> list = datesFound.getParserOutputs();
			if(list.size() > 0) {
				DateTime foundTime = list.get(0).getDateRange().getStart();
				cal.set(Calendar.HOUR, foundTime.getHourOfDay());
				cal.set(Calendar.MINUTE, foundTime.getMinuteOfHour());
				cal.set(Calendar.SECOND, foundTime.getSecondOfMinute());
			}
		}
		return cal.getTime();
	}*/
	
	protected RedbackObjectRemote findObject(SeqExContext context, String objectName, String search) throws RedbackException {
		List<RedbackObjectRemote> list = listObjects(context, objectName, search);
		if(list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	protected List<RedbackObjectRemote> listObjects(SeqExContext context, String objectName, String search) throws RedbackException {
		return objectClient.listObjects(context.session, objectName, null, search, null, false, true, 0, 100);
	}
	
	

}
