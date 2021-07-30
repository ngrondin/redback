package io.redback.client;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;

public class GatewayClient extends Client
{

	public GatewayClient(Firebus fb, String sn) 
	{
		super(fb, sn);
		setTimeout(60000);
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
	
	public DataMap put(String url, Object body) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("body", body);
		return call(req);	
	}
	
	public DataMap putForm(String url, DataMap form) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("form", form);
		return call(req);	
	}

	public DataMap put(String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("body", body);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap putForm(String url, DataMap form, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "put");
		req.put("url", url);
		req.put("form", form);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);
	}
	
	public DataMap patch(String url, Object body) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("body", body);
		return call(req);	
	}
	
	public DataMap patchForm(String url, DataMap form) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("form", form);
		return call(req);	
	}

	public DataMap patch(String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("body", body);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);	
	}

	public DataMap patchForm(String url, DataMap form, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", "patch");
		req.put("url", url);
		req.put("form", form);
		if(headers != null)
			req.put("headers", headers);
		if(cookie != null)
			req.put("cookie", cookie);
		return call(req);
	}	
	
	public DataMap call(String method, String url, Object body, DataMap headers, DataMap cookie) throws RedbackException {
		DataMap req = new DataMap();
		req.put("method", method);
		req.put("url", url);
		req.put("body", body);
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
		else if(ret instanceof String)
			return new DataMap("result", ret.toString());
		else 
			return new DataMap();
	}


}
