package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
import io.redback.utils.NLCommandResponse;

public abstract class AIServer extends AuthenticatedServiceProvider {

	public AIServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}


	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All requests need to be authenticated");
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			Payload response = new Payload();
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			DataMap responseData = null;
			if(action != null)
			{
				if(action.equals("nlcommand"))
				{
					String model = request.getString("model");
					String text = request.getString("text");
					DataMap context = request.getObject("context");
					NLCommandResponse resp = nlCommand(session, model, text, context);
					responseData = new DataMap("text", resp.text);
					DataList list = new DataList();
					if(resp.uiactions != null) {
						for(String entry: resp.uiactions) {
							list.add(entry);
						}
						responseData.put("uiactions", list);	
						responseData.put("sequence", resp.sequence);
					}
				}
				else if(action.equals("feedback")) 
				{
					String model = request.getString("model");
					String command = request.getString("command");
					String sequence = request.getString("sequence");
					int points = request.getNumber("points").intValue();
					feedback(session, model, command, sequence, points);
					responseData = new DataMap("result", "ok");
				}
				else
				{
					throw new RedbackException("Valid actions are 'nlcommand' and 'feedback'");
				}
			}
			response.setData(responseData);
			response.metadata.put("mime", "application/json");
	
			return response;
		} catch(DataException e) {
			throw new RedbackException("Data Error in AI server", e);
		}
	}
	
	protected abstract NLCommandResponse nlCommand(Session session, String model, String text, DataMap context) throws RedbackException;
	
	protected abstract void feedback(Session session, String model, String command, String sequence, int points) throws RedbackException;

}
