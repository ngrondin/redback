package io.redback.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.utils.DataEntity;
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
import io.redback.utils.StringUtils;
import io.redback.utils.js.JSConverter;

public class RedbackIntegrationServer extends IntegrationServer {
	
	protected class ClientConfig {
		public String name;
		public String clientId;
		public String clientSecrect;
		public String scope;
		public String extraLoginParams;
		public String loginUrl;
		public String tokenUrl;
		public Expression headerExpr;
		public Expression urlExpr;
		public Expression methodExpr;
		public Expression bodyExpr;
		public Expression respExpr;
		
		public ClientConfig(DataMap cfg) throws RedbackException {
			name = cfg.getString("name");
			clientId = cfg.getString("clientid");
			clientSecrect = cfg.getString("clientsecret");
			scope = cfg.getString("scope");
			extraLoginParams = cfg.getString("extraloginparams");
			loginUrl = cfg.getString("loginurl");
			tokenUrl = cfg.getString("tokenurl");
			List<String> params = Arrays.asList(new String[] {"userprofile", "clientid", "clientsecret", "action", "object", "uid", "filter", "data", "options", "clientdata", "response"});
			headerExpr = new Expression(jsManager, "client_" + name + "_header", params, cfg.getString("header"));
			urlExpr = new Expression(jsManager, "client_" + name + "_url", params, cfg.getString("url"));
			methodExpr = new Expression(jsManager, "client_" + name + "_method", params, cfg.getString("method"));
			bodyExpr = new Expression(jsManager, "client_" + name + "_body", params, cfg.getString("body"));
			respExpr = new Expression(jsManager, "client_" + name + "_response", params, cfg.getString("response"));
		}
	};
	
	private Logger logger = Logger.getLogger("io.redback");
	protected JSManager jsManager;
	protected boolean loadAllOnInit;
	protected int preCompile;
	protected String configServiceName;
	protected String dataServiceName;
	protected String gatewayServiceName;
	protected DataClient dataClient;
	protected ConfigurationClient configClient;
	protected GatewayClient gatewayClient;
	protected CollectionConfig clientDataCollection;
	protected Map<String, ClientConfig> clientConfigs;
	protected Map<String, DataMap> cachedClientData;
	protected String publicUrl;

	public RedbackIntegrationServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		jsManager = new JSManager("integration");
		loadAllOnInit = config.containsKey("loadalloninit") ? config.getBoolean("loadalloninit") : true;
		preCompile = config.containsKey("precompile") ? config.getNumber("precompile").intValue() : 0;
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		gatewayServiceName = config.getString("gatewayservice");
		dataClient = new DataClient(firebus, dataServiceName);
		configClient = new ConfigurationClient(firebus, configServiceName);
		gatewayClient = new GatewayClient(firebus, gatewayServiceName);
		clientDataCollection = new CollectionConfig(c.getObject("clientdatacollection"), "rbin_clientdata");
		clientConfigs = new HashMap<String, ClientConfig>();
		cachedClientData = new HashMap<String, DataMap>();
		publicUrl = config.getString("publicurl");
	}
	
	
	public void configure() {
		clientConfigs.clear();
		cachedClientData.clear();
		if(loadAllOnInit) {
			Session session = new Session();
			try {
				loadAllClientConfigs(session);
				jsManager.precompile(preCompile);
			} catch(Exception e) {
				logger.severe(StringUtils.rollUpExceptions(e));
			}
		}
	}

	public void start() {
		
	}	
	
	protected String getRedirectUri(String client) {
		return publicUrl + "/" + client;
	}
	
	protected void loadAllClientConfigs(Session session) throws RedbackException {
		DataMap result = configClient.listConfigs(session, "rbin", "client");
		DataList resultList = result.getList("result");
		for(int i = 0; i < resultList.size(); i++)
		{
			DataMap cfg = resultList.getObject(i);
			ClientConfig config = new ClientConfig(cfg);
			clientConfigs.put(cfg.getString("name"), config);
		}			
	}
	
	protected ClientConfig getClientConfig(Session session, String client) throws RedbackException {
		ClientConfig config = clientConfigs.get(client);
		if(config == null) {
			DataMap configData = configClient.getConfig(session, "rbin", "client", client);
			if(configData != null) {
				config = new ClientConfig(configData);
				clientConfigs.put(client, config);
			} else {
				throw new RedbackException("Cannot find integration client " + client);
			}
		}
		return config;
	}
	
	protected DataMap getClientData(Session session, ClientConfig config, String domain) throws RedbackException {
		String cacheKey = config.name + domain;
		DataMap clientData = cachedClientData.get(cacheKey);
		if(clientData == null) {
			DataMap filter = new DataMap();
			filter.put("client", config.name);
			filter.put("domain", domain);
			DataMap resp = dataClient.getData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(filter), null);
			if(resp != null && resp.getList("result") != null && resp.getList("result").size() > 0) {
				clientData = resp.getList("result").getObject(0);
				//cachedClientData.put(cacheKey, clientData); //Commented this out until multinode firebus comm works
			}			
		}
		
		if(clientData != null) {
			String accessToken = clientData.getString("access_token");
			long expiry = clientData.containsKey("expiry") ? clientData.getNumber("expiry").longValue() : 0;
			if(accessToken == null || accessToken.equals("") || expiry < System.currentTimeMillis()) {
				String refreshToken = clientData.getString("refresh_token");
				if(refreshToken != null && !refreshToken.equals("")) {
					DataMap form = new DataMap();
					form.put("client_id", config.clientId);
					form.put("client_secret", config.clientSecrect);
					form.put("grant_type", "refresh_token");
					form.put("refresh_token", refreshToken);
					form.put("redirect_uri", getRedirectUri(config.name));
					DataMap refreshResp = gatewayClient.postForm(config.tokenUrl, form);
					if(refreshResp != null) {
						if(refreshResp.getString("refresh_token") != null)
							clientData.put("refresh_token", refreshResp.getString("refresh_token"));
						if(refreshResp.getString("access_token") != null)
							clientData.put("access_token", refreshResp.getString("access_token"));
						if(refreshResp.getNumber("expires_in") != null)
							clientData.put("expiry", System.currentTimeMillis() + (refreshResp.getNumber("expires_in").longValue() * 1000));
						clientData.put("lastupdate", new Date());
						DataMap key = new DataMap();
						key.put("client", config.name);
						key.put("domain", domain);
						dataClient.putData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(key), clientData);
					} else {
						throw new RedbackException("Error refreshing tokens");
					}				
				} else {
					throw new RedbackException("No availabile refresh token");
				}
			}
			return clientData;
		} else {
			throw new RedbackException("Could not find tokens for client " + config.name + " and domain " + domain);
		}
	}
	
	protected Map<String, Object> createScriptContext(Session session, ClientConfig config, String domain, String action, String objectName, String uid, DataMap filter, DataEntity data, DataMap options) throws RedbackException
	{
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
		context.put("clientid", config.clientId);
		context.put("clientsecret", config.clientSecrect);
		context.put("clientdata", JSConverter.toJS(getClientData(session, config, domain)));
		context.put("action", action);
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("filter", JSConverter.toJS(filter));
		context.put("data", JSConverter.toJS(data));
		context.put("options", JSConverter.toJS(options));
		return context;
	}
	
	protected DataMap gatewayRequest(ClientConfig config, Map<String, Object> context) throws RedbackException {
		DataMap resp = gatewayClient.call(
				(String)config.methodExpr.eval(context), 
				(String)config.urlExpr.eval(context), 
				config.bodyExpr.eval(context), 
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

	protected DataMap update(Session session, String client, String domain, String objectName, String uid, DataEntity data, DataMap options) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "update", objectName, uid, null, data, options);
		return gatewayRequest(config, context);
	}

	protected DataMap create(Session session, String client, String domain, String objectName, DataEntity data, DataMap options) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "create", objectName, null, null, data, options);
		return gatewayRequest(config, context);
	}

	protected void delete(Session session, String client, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		Map<String, Object> context = createScriptContext(session, config, domain, "delete", objectName, uid, null, null, options);
		gatewayRequest(config, context);
	}

	protected String getLoginUrl(Session session, String client, String domain, String state) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		String url = config.loginUrl + "?response_type=code&redirect_uri=" + getRedirectUri(client) + "&client_id=" + config.clientId +"&scope=" + config.scope + (config.extraLoginParams != null ? "&" + config.extraLoginParams : "") + (state != null ? "&state=" + state : "");
		return url; 
	}

	protected void exchangeAuthCode(Session session, String client, String domain, String code) throws RedbackException {
		ClientConfig config = getClientConfig(session, client);
		DataMap form = new DataMap();
		form.put("client_id", config.clientId);
		form.put("client_secret", config.clientSecrect);
		form.put("grant_type", "authorization_code");
		form.put("code", code);
		form.put("redirect_uri", getRedirectUri(config.name));
		DataMap refreshResp = gatewayClient.postForm(config.tokenUrl, form);
		if(refreshResp != null) {
			DataMap clientData = new DataMap();
			if(refreshResp.getString("refresh_token") != null)
				clientData.put("refresh_token", refreshResp.getString("refresh_token"));
			if(refreshResp.getString("access_token") != null)
				clientData.put("access_token", refreshResp.getString("access_token"));
			if(refreshResp.getNumber("expires_in") != null)
				clientData.put("expiry", System.currentTimeMillis() + (refreshResp.getNumber("expires_in").longValue() * 1000));
			clientData.put("lastupdate", new Date());
			DataMap key = new DataMap();
			key.put("client", config.name);
			key.put("domain", domain);
			dataClient.putData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(key), clientData);
		} else {
			throw new RedbackException("Error exchanging authorization code");
		}			
	}


	
	protected void clearCachedClientData(Session session, String client, String domain) throws RedbackException {
		String key = client + domain;
		if(cachedClientData.containsKey(key)) {
			cachedClientData.remove(key);
		}
	}

}
