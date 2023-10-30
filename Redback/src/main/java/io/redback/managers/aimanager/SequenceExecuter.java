package io.redback.managers.aimanager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.ZonedTime;
import io.firebus.data.parse.DateParser;
import io.firebus.data.parse.NumberParser;
import io.firebus.data.parse.TimeParser;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class SequenceExecuter {
	protected ObjectClient objectClient;
	protected String[] reserved = {"navto", "find", "create", "update", "link", "execute"};
	
	public SequenceExecuter(ObjectClient oc) {
		objectClient = oc;
	}
	
	public List<String> runSequence(Session session, List<String> seq) throws RedbackException {
		List<String> residual = new ArrayList<String>();
		Map<String, Object> context = new HashMap<String, Object>();
		for(int cur = 0; cur < seq.size(); cur++) {
			String curToken = seq.get(cur);
			if(isReserved(curToken)) {
				int nextRes = -1;
				for(int i = cur + 1; i < seq.size() && nextRes == -1; i++)
					if(isReserved(seq.get(i)))
						nextRes = i;
				int paramLen = seq.size() - cur - 1;
				if(nextRes != -1)
					paramLen = nextRes - cur - 1;
				if(isResidual(curToken)) {
					for(int i = 0; i < 1 + paramLen; i++) 
						residual.add(seq.get(cur + i));
				} else {
					String[] params = new String[paramLen];
					for(int i = 0; i < paramLen; i++)
						params[i] = seq.get(cur + i + 1);
					runCommand(session, curToken, params, context);
				}
				cur += paramLen ;					

			} 
		}
		return residual;
	}
	
	protected boolean isReserved(String token) {
		for(String res : reserved) 
			if(res.equals(token))
				return true;
		return false;
	}
	
	protected boolean isResidual(String token) {
		return token.equals("navto");
	}
	
	protected void runCommand(Session session, String command, String[] params, Map<String, Object> context) throws RedbackException {
		if(command.equals("create"))
			create(session, params, context);
		else if(command.equals("find"))
			find(session, params, context);
		else if(command.equals("update"))
			update(session, params, context);	
		else if(command.equals("link"))
			link(session, params, context);			
	}
	
	protected void find(Session session, String[] params, Map<String, Object> context) throws RedbackException {
		if(params.length >= 2) {
			String search = "";
			for(int i = 1; i < params.length; i++)
				search = search + " " + params[i];
			search = search.trim();
			List<RedbackObjectRemote> list = objectClient.listObjects(session, params[0], null, search, null, false, true, 0, 10);
			if(list.size() > 0) {
				context.put("object", list.get(0));				
			}
		}
	}
	
	protected void create(Session session, String[] params, Map<String, Object> context) throws RedbackException {
		if(params.length >= 1) {
			RedbackObjectRemote ror = objectClient.createObject(session, params[0], null, false);
			context.put("object", ror);
		}
	}
	
	protected void update(Session session, String[] params, Map<String, Object> context) throws RedbackException {
		if(params.length >= 2) {
			RedbackObjectRemote ror = (RedbackObjectRemote)context.get("object");
			if(ror != null) {
				String value = "";
				for(int i = 1; i < params.length; i++)
					value = value + " " + params[i];
				ror.set(params[0], parseString(value.trim()));
			}
		}
	}
	
	protected void link(Session session, String[] params, Map<String, Object> context) throws RedbackException {
		if(params.length >= 3) {
			RedbackObjectRemote ror = (RedbackObjectRemote)context.get("object");
			if(ror != null) {
				String attribute = params[0];
				String relatedObjectName = params[1];
				String search = "";
				for(int i = 2; i < params.length; i++)
					search = search + " " + params[i];
				List<RedbackObjectRemote> list = objectClient.listObjects(session, relatedObjectName, null, search, null, false, true, 0, 10);
				if(list.size() > 0) {
					ror.setRelated(attribute, list.get(0));
				}
			}
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
						return numberValue.doubleValue();
					} else {
						return str;
						
					}
				}
			}
		}
		

		
	}
}
