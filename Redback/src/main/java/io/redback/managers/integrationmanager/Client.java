package io.redback.managers.integrationmanager;

import java.util.HashMap;
import java.util.Map;

import io.firebus.data.DataEntity;
import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.firebus.script.ScriptContext;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptValueException;
import io.redback.client.GatewayClient;
import io.redback.exceptions.RedbackConfigNotFoundException;
import io.redback.exceptions.RedbackException;
import io.redback.managers.integrationmanager.js.GatewayCallJSWrapper;
import io.redback.security.Session;
import io.redback.security.js.UserProfileJSWrapper;

public class Client {
	protected Session session;
	protected IntegrationManager integrationManager;
	protected ClientConfig config;
	protected DataMap data;
	protected GatewayClient gatewayClient;
	protected Map<String, Object> baseScriptContext;
	
	public Client(Session s, IntegrationManager im, ClientConfig cc, DataMap cd, GatewayClient gwc) {
		session = s;
		integrationManager = im;
		config = cc;
		setData(cd);
		gatewayClient = gwc;
		baseScriptContext = new HashMap<String, Object>();
		baseScriptContext.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
		baseScriptContext.put("clientid", config.clientId);
		baseScriptContext.put("clientsecret", config.clientSecrect);
		baseScriptContext.put("baseurl", config.baseUrl);
		baseScriptContext.put("clientdata", data);
	}
	
	public void setData(DataMap d) {
		if(data == null)
			data = new DataMap();
		for(String key: d.keySet()) {
			data.put(key, d.get(key));
		}
	}
	
	protected ScriptContext getScriptContext(Map<String, Object> subContext) throws ScriptValueException {
		ScriptContext ctx = integrationManager.getScriptFactory().createScriptContext();
		for(String key : baseScriptContext.keySet()) ctx.declare(key, baseScriptContext.get(key));
		for(String key : subContext.keySet()) ctx.declare(key, subContext.get(key));	
		return ctx;
	}
	
	protected ScriptContext getFunctionScriptContext() throws ScriptValueException {
		ScriptContext ctx = integrationManager.getScriptFactory().createScriptContext();
		for(String key : baseScriptContext.keySet()) 
			ctx.declare(key, baseScriptContext.get(key));
		ctx.declare("call", new GatewayCallJSWrapper(this));
		return ctx;
	}
	
	protected String completeUrl(String url) {
		if(!url.startsWith("http://") || url.startsWith("https://")) {
			String fullUrl = config.baseUrl;
			if(!fullUrl.endsWith("/"))
				fullUrl = fullUrl + "/";
			fullUrl = fullUrl + (url.startsWith("/") ? url.substring(1) : url);
			return fullUrl;
		} else {
			return url;
		}
	}
	

	
	public DataMap call(Map<String, Object> actionScriptContext) throws RedbackException {
		try {
			ScriptContext scriptContext = getScriptContext(actionScriptContext);
			String method = (String)config.methodExpr.eval(scriptContext);
			String url = completeUrl((String)config.urlExpr.eval(scriptContext));
			Object body = config.bodyExpr.eval(scriptContext);
			DataMap headers = (DataMap)config.headerExpr.eval(scriptContext);
			DataMap resp = gatewayClient.call(method, url, body, headers, null);
			scriptContext.put("response", resp);
			DataMap finalResp = (DataMap)config.respExpr.eval(scriptContext);
			return finalResp;
		} catch(ScriptException e) {
			throw new RedbackException("Error executing generic external call", e);
		}		
	}
	
	public DataMap call(String method, String url, Object body, DataMap headers) throws RedbackException {
		try {
			String finalMethod = method != null ? method : (String)config.methodExpr.eval(baseScriptContext);
			String finalUrl = completeUrl(url != null ? url : (String)config.urlExpr.eval(baseScriptContext));
			Object finalBody = body != null ? body : config.bodyExpr.eval(baseScriptContext);
			DataMap finalHeaders = headers != null ? headers : (DataMap)config.headerExpr.eval(baseScriptContext);
			DataMap resp = gatewayClient.call(finalMethod, finalUrl, finalBody, finalHeaders, null);
			return resp;
		} catch(ScriptException e) {
			throw new RedbackException("Error executing function external call", e);
		}		
	}
	
	public String getRemoteDomain() throws RedbackException {
		try {
			Object resp = config.getRemoteDomainFunc.call(getFunctionScriptContext());
			if(resp != null && resp instanceof String) {
				data.put("remotedomain", resp);			
				return (String)resp;				
			} else {
				return null;
			}
		} catch (ScriptException e) {
			throw new RedbackException("Error running the integration getdomain function", e);
		}
	}
	
	public DataMap executeFunction(String functionName, DataEntity data, DataMap options) throws RedbackException {
		Function function = config.functions.get(functionName);
		if(function != null) {
			try {
				Object resp = function.call(getFunctionScriptContext(), data, options);
				if(resp instanceof DataMap) {
					return (DataMap)resp;
				} else {
					return new DataMap("result", resp);
				}
			} catch (ScriptException e) {
				throw new RedbackException("Error running the integration function " + functionName, e);
			}
		} else {
			throw new RedbackConfigNotFoundException("Cannot find integration client function " + functionName);
		}
	}

}
