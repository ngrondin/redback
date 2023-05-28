package io.redback.managers.integrationmanager;

import java.util.HashMap;
import java.util.Map;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.firebus.script.Function;
import io.redback.exceptions.RedbackException;

public class ClientConfig {
	protected IntegrationManager integrationManager;
	public String name;
	public String clientId;
	public String clientSecrect;
	public String scope;
	public String extraLoginParams;
	public String baseUrl;
	public String loginUrl;
	public String tokenUrl;
	public Expression headerExpr;
	public Expression urlExpr;
	public Expression methodExpr;
	public Expression bodyExpr;
	public Expression respExpr;
	public Function getRemoteDomainFunc;
	public Map<String, Function> functions;
	
	public ClientConfig(IntegrationManager im, DataMap cfg) throws RedbackException {
		try {
			integrationManager= im;
			name = cfg.getString("name");
			clientId = cfg.getString("clientid");
			clientSecrect = cfg.getString("clientsecret");
			scope = cfg.getString("scope");
			extraLoginParams = cfg.getString("extraloginparams");
			loginUrl = cfg.getString("loginurl");
			tokenUrl = cfg.getString("tokenurl");
			baseUrl = cfg.getString("baseurl");
			headerExpr = im.getScriptFactory().createExpression("client_" + name + "_header", "(" + cfg.getString("header") + ")");
			urlExpr = im.getScriptFactory().createExpression("client_" + name + "_url", "(" + cfg.getString("url") + ")");
			methodExpr = im.getScriptFactory().createExpression("client_" + name + "_method", "(" + cfg.getString("method") + ")");
			bodyExpr = im.getScriptFactory().createExpression("client_" + name + "_body", "(" + cfg.getString("body") + ")");
			respExpr = im.getScriptFactory().createExpression("client_" + name + "_response", "(" + cfg.getString("response") + ")");
			getRemoteDomainFunc = im.getScriptFactory().createFunction("client_" + name + "_function_getdomain", new String[] {}, cfg.getString("getdomain"));
			functions = new HashMap<String, Function>();
			if(cfg.containsKey("functions")) {
				for(String funcName: cfg.getObject("functions").keySet()) {
					functions.put(funcName, im.getScriptFactory().createFunction("client_" + name + "_function_" + funcName, new String[] {"data", "options"}, cfg.getObject("functions").getString(funcName)));
				}				
			}
		} catch(Exception e) {
			throw new RedbackException("Error initialising integration client config", e);
		}
	}
}
