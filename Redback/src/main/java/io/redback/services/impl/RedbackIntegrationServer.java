package io.redback.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.client.GatewayClient;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.JSManager;
import io.redback.security.Session;
import io.redback.security.js.UserProfileJSWrapper;
import io.redback.services.IntegrationServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.js.JSConverter;

public class RedbackIntegrationServer extends IntegrationServer {
	
	protected class ClientConfig {
		public String name;
		public String clientId;
		public String clientSecrect;
		public String redirectUri;
		public String refreshUrl;
		public Expression headerExpr;
		public Expression urlExpr;
		public Expression methodExpr;
		public Expression bodyExpr;
		public Expression respExpr;
		
		public ClientConfig(DataMap cfg) throws RedbackException {
			name = cfg.getString("name");
			clientId = cfg.getString("clientid");
			clientSecrect = cfg.getString("clientsecret");
			redirectUri = cfg.getString("redirecturi");
			refreshUrl = cfg.getString("refreshurl");
			List<String> params = Arrays.asList(new String[] {"userprofile", "clientid", "clientsecret", "action", "object", "uid", "filter", "data", "options", "accesstoken", "response"});
			headerExpr = new Expression(jsManager, "client_" + name + "_header", params, cfg.getString("header"));
			urlExpr = new Expression(jsManager, "client_" + name + "_url", params, cfg.getString("url"));
			methodExpr = new Expression(jsManager, "client_" + name + "_method", params, cfg.getString("method"));
			bodyExpr = new Expression(jsManager, "client_" + name + "_body", params, cfg.getString("body"));
			respExpr = new Expression(jsManager, "client_" + name + "_response", params, cfg.getString("response"));
		}
	};
	
	protected JSManager jsManager;
	protected String configServiceName;
	protected String dataServiceName;
	protected String gatewayServiceName;
	protected DataClient dataClient;
	protected ConfigurationClient configClient;
	protected GatewayClient gatewayClient;
	protected CollectionConfig tokenCollection;
	protected Map<String, ClientConfig> clientConfigs;
	protected Map<String, DataMap> cachedTokens;


	public RedbackIntegrationServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		jsManager = new JSManager();
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		gatewayServiceName = config.getString("gatewayservice");
		dataClient = new DataClient(firebus, dataServiceName);
		configClient = new ConfigurationClient(firebus, configServiceName);
		gatewayClient = new GatewayClient(firebus, gatewayServiceName);
		tokenCollection = new CollectionConfig(c.getObject("tokencollection"), "rbin_tokens");
		clientConfigs = new HashMap<String, ClientConfig>();
		cachedTokens = new HashMap<String, DataMap>();
	}
	
	protected ClientConfig getClientConfig(Session session, String client) throws RedbackException {
		ClientConfig config = clientConfigs.get(client);
		if(config == null) {
			DataMap configData = configClient.getConfig(session, "rbin", "client", client);
			config = new ClientConfig(configData);
			clientConfigs.put(client, config);
		}
		return config;
	}
	
	protected String getAccessToken(Session session, ClientConfig config, String domain) throws RedbackException {
		DataMap tokens = cachedTokens.get(config.name + domain);
		if(tokens == null) {
			DataMap filter = new DataMap();
			filter.put("client", config.name);
			filter.put("domain", domain);
			DataMap resp = dataClient.getData(tokenCollection.getName(), tokenCollection.convertObjectToSpecific(filter), null);
			if(resp != null && resp.getList("result") != null) {
				tokens = resp.getList("result").getObject(0);
				cachedTokens.put(config.name + domain, tokens);
			}			
		}
		
		if(tokens != null) {
			long expiry = tokens.getNumber("expiry").longValue();
			if(expiry < System.currentTimeMillis()) {
				DataMap form = new DataMap();
				form.put("client_id", config.clientId);
				form.put("client_secret", config.clientSecrect);
				form.put("grant_type", "refresh_token");
				form.put("refresh_token", tokens.getString("refresh_token"));
				form.put("redirect_uri", config.redirectUri);
				DataMap refreshResp = gatewayClient.postForm(config.refreshUrl, form);
				if(refreshResp != null) {
					if(refreshResp.getString("refresh_token") != null)
						tokens.put("refresh_token", refreshResp.getString("refresh_token"));
					if(refreshResp.getString("access_token") != null)
						tokens.put("access_token", refreshResp.getString("access_token"));
					if(refreshResp.getNumber("expires_in") != null)
						tokens.put("expiry", System.currentTimeMillis() + (refreshResp.getNumber("expires_in").longValue() * 1000));
					DataMap key = new DataMap();
					key.put("client", config.name);
					key.put("domain", domain);
					dataClient.putData(tokenCollection.getName(), tokenCollection.convertObjectToSpecific(key), tokens);
				} else {
					throw new RedbackException("Error refreshing tokens");
				}				
			}
			return tokens.getString("access_token");
		} else {
			throw new RedbackException("Could not find tokens for client " + config.name + " and domain " + domain);
		}
	}
	
	protected Map<String, Object> createScriptContext(Session session, ClientConfig config, String domain, String action, String objectName, String uid, DataMap filter, DataMap data, DataMap options) throws RedbackException
	{
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
		context.put("clientid", config.clientId);
		context.put("clientsecret", config.clientSecrect);
		context.put("action", action);
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("filter", JSConverter.toJS(filter));
		context.put("data", JSConverter.toJS(data));
		context.put("options", JSConverter.toJS(options));
		context.put("accesstoken", getAccessToken(session, config, domain));
		return context;
	}
	
	protected DataMap gatewayRequest(ClientConfig config, Map<String, Object> context) throws RedbackException {
		DataMap resp = gatewayClient.call(
				(String)config.methodExpr.eval(context), 
				(String)config.urlExpr.eval(context), 
				(DataMap)config.bodyExpr.eval(context), 
				(DataMap)config.headerExpr.eval(context), 
				null);
		context.put("response", JSConverter.toJS(resp));
		return (DataMap)config.respExpr.eval(context);
	}

	protected DataMap get(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "get", objectName, uid, null, null, options);
		return gatewayRequest(config, context);
	}

	protected List<DataMap> list(Session session, String client, String domain, String objectName, DataMap filter, DataMap options, int page, int pageSize) throws RedbackException {
		List<DataMap> respList = new ArrayList<DataMap>();
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "list", objectName, null, filter, null, options);
		DataMap resp = gatewayRequest(config, context);
		DataList list = resp.getList("list");
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				respList.add(list.getObject(i));
			}
		}
		return respList;
	}

	protected DataMap update(Session session, String client, String domain, String objectName, String uid, DataMap data, DataMap options) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "update", objectName, uid, null, data, options);
		return gatewayRequest(config, context);
	}

	protected DataMap create(Session session, String client, String domain, String objectName, DataMap data, DataMap options) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "create", objectName, null, null, data, options);
		return gatewayRequest(config, context);
	}

	protected void delete(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "delete", objectName, uid, null, null, options);
		gatewayRequest(config, context);
	}

	public void clearCaches() {
		clientConfigs.clear();
		cachedTokens.clear();
	}

}
