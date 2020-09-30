package io.redback.client;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class GatewayClient extends Client
{

	public GatewayClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public DataMap get(String url) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "get");
		req.put("url", url);
		return call(req);
	}

	public DataMap get(String url, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "get");
		req.put("url", url);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap post(String url, Object body) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("body", body);
		return call(req);	
	}
	
	public DataMap postForm(String url, DataMap form) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("form", form);
		return call(req);	
	}

	public DataMap post(String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("body", body);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap postForm(String url, DataMap form, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "post");
		req.put("url", url);
		req.put("form", form);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);
	}

	protected DataMap call(DataMap req) throws RedbackException
	{
		Object ret = request(req);
		if(ret instanceof DataMap)
			return (DataMap)ret;
		else
			return new DataMap("result", ret.toString());
	}


}
