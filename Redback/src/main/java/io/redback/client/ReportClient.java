package io.redback.client;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class ReportClient extends Client {

	public ReportClient(Firebus fb, String sn) {
		super(fb, sn);
	}
	
	public byte[] produce(Session session, String name, DataMap filter) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "produce");
			req.put("report", name);
			req.put("filter", filter);
			Payload resp = requestPayload(session, new Payload(req.toString()));
			return resp.data;
		} catch(Exception e) {
			throw new RedbackException("Error producing report", e);
		}
	}

	public String produceAndStore(Session session, String name, DataMap filter) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "producestore");
			req.put("report", name);
			req.put("filter", filter);
			DataMap resp = request(session, req);
			return resp.getString("fileuid");
		} catch(Exception e) {
			throw new RedbackException("Error producing and storing report", e);
		}
	}

}
