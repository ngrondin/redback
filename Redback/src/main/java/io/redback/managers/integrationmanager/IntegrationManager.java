package io.redback.managers.integrationmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.ScriptFactory;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.client.GatewayClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;
import io.redback.utils.ConfigCache;
import io.redback.utils.js.LoggerJSFunction;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class IntegrationManager {
	protected String name;
	protected Firebus firebus;
	protected ScriptFactory scriptFactory;
	protected boolean loadAllOnInit;
	protected int preCompile;
	protected String configServiceName;
	protected String dataServiceName;
	protected String gatewayServiceName;
	protected String fileServiceName;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected GatewayClient gatewayClient;
	protected FileClient fileClient;
	protected CollectionConfig clientDataCollection;
	protected ConfigCache<ClientConfig> clientConfigs;
	protected String publicUrl;

	public IntegrationManager(String n, DataMap config, Firebus fb) throws RedbackException {
		try {
			name = n;
			firebus = fb;
			scriptFactory = new ScriptFactory();
			scriptFactory.setInRootScope("log", new LoggerJSFunction());
			scriptFactory.setInRootScope("rbutils", new RedbackUtilsJSWrapper());
			loadAllOnInit = config.containsKey("loadalloninit") ? config.getBoolean("loadalloninit") : true;
			preCompile = config.containsKey("precompile") ? config.getNumber("precompile").intValue() : 0;
			configServiceName = config.getString("configservice");
			dataServiceName = config.getString("dataservice");
			gatewayServiceName = config.getString("gatewayservice");
			fileServiceName = config.getString("fileservice");
			dataClient = new DataClient(firebus, dataServiceName);
			configClient = new ConfigClient(firebus, configServiceName);
			gatewayClient = new GatewayClient(firebus, gatewayServiceName);
			fileClient = new FileClient(firebus, fileServiceName);
			clientDataCollection = new CollectionConfig(config.getObject("clientdatacollection"), "rbin_clientdata");
			final IntegrationManager im = this;
			clientConfigs = new ConfigCache<ClientConfig>(configClient, "rbin", "client", 3600000, new ConfigCache.ConfigFactory<ClientConfig>() {
				public ClientConfig createConfig(DataMap map) throws Exception {
					return new ClientConfig(im, map);
				}});
			publicUrl = config.getString("publicurl");
		} catch(Exception e) {
			throw new RedbackException("Error initialising Integration Manager", e);
		}
	}
	
	public ScriptFactory getScriptFactory() {
		return scriptFactory;
	}
	
	public FileClient getFileClient() {
		return fileClient;
	}
	
	public void configure() {
		clientConfigs.clear();
	}
	
	protected String getRedirectUri(String client) {
		return publicUrl + "/" + client;
	}
	
	protected DataMap getClientData(Session session, String name, String domain) throws RedbackException {
		DataMap clientData = null;
		DataMap filter = new DataMap();
		filter.put("name", name);
		filter.put("domain", domain);
		DataMap resp = dataClient.getData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(filter), null);
		if(resp != null && resp.getList("result") != null && resp.getList("result").size() > 0) {
			clientData = resp.getList("result").getObject(0);
			return clientData;
		} else {
			throw new RedbackException("Could not find tokens for client " + name + " and domain " + domain);
		}
	}

	protected Client getClient(Session session, String name, String domain) throws RedbackException {
		return getClient(session, name, domain, true);
	}
	
	protected Client getClient(Session session, String name, String domain, boolean checkToken) throws RedbackException {
		DataMap clientData = getClientData(session, name, domain);
		String cconfigName = clientData.getString("client");
		ClientConfig config = clientConfigs.get(session, cconfigName);
		String accessToken = clientData.getString("access_token");
		long expiry = clientData.containsKey("expiry") ? clientData.getNumber("expiry").longValue() : 0;
		if(checkToken && (accessToken == null || accessToken.equals("") || expiry < System.currentTimeMillis())) {
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
					key.put("name", name);
					key.put("domain", domain);
					dataClient.putData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(key), clientData);
				} else {
					throw new RedbackException("Error refreshing tokens");
				}				
			} else {
				throw new RedbackException("No availabile refresh token");
			}
		}
		Client client = new Client(session, this, config, clientData, gatewayClient);
		return client;
	}
	
	public DataMap get(Session session, String name, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "get");
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("options", options);
		return client.call(context);
	}

	public List<DataMap> list(Session session, String name, String domain, String objectName, DataMap filter, DataMap options, int page, int pageSize) throws RedbackException {
		Client client = getClient(session, name, domain);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "list");
		context.put("object", objectName);
		context.put("filter", filter);
		context.put("options", options);
		DataMap resp = client.call(context);
		List<DataMap> respList = new ArrayList<DataMap>();
		DataList list = resp.getList("list");
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				respList.add(list.getObject(i));
			}
		}
		return respList;
	}

	public DataMap update(Session session, String name, String domain, String objectName, String uid, DataEntity data, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "update");
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("data", data);
		context.put("options", options);
		return client.call(context);
	}

	public DataMap create(Session session, String name, String domain, String objectName, DataEntity data, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "create");
		context.put("object", objectName);
		context.put("data", data);
		context.put("options", options);
		return client.call(context);
	}

	public void delete(Session session, String name, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "delete");
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("options", options);
		client.call(context);
	}
	
	public Object execute(Session session, String name, String domain, String function, DataEntity data, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain);
		return client.executeFunction(function, data, options);
	}

	public String getLoginUrl(Session session, String name, String domain) throws RedbackException {
		Client client = getClient(session, name, domain, false);
		String url = client.config.loginUrl + "?response_type=code&redirect_uri=" + getRedirectUri(client.config.name) + "&client_id=" + client.config.clientId +"&scope=" + client.config.scope + (client.config.extraLoginParams != null ? "&" + client.config.extraLoginParams : "") + "&state=" + name;
		return url; 
	}

	public void exchangeAuthCode(Session session, String name, String domain, String code, String state) throws RedbackException {
		String _name = name != null ? name : state;
		Client client = getClient(session, _name, domain, false);
		DataMap form = new DataMap();
		form.put("client_id", client.config.clientId);
		form.put("client_secret", client.config.clientSecrect);
		form.put("grant_type", "authorization_code");
		form.put("code", code);
		form.put("redirect_uri", getRedirectUri(client.config.name));
		DataMap refreshResp = gatewayClient.postForm(client.config.tokenUrl, form);
		if(refreshResp != null) {
			DataMap clientData = new DataMap();
			if(refreshResp.getString("refresh_token") != null)
				clientData.put("refresh_token", refreshResp.getString("refresh_token"));
			if(refreshResp.getString("access_token") != null)
				clientData.put("access_token", refreshResp.getString("access_token"));
			if(refreshResp.getNumber("expires_in") != null)
				clientData.put("expiry", System.currentTimeMillis() + (refreshResp.getNumber("expires_in").longValue() * 1000));
			clientData.put("lastupdate", new Date());
			client.setData(clientData);
			String remoteDoamin = client.getRemoteDomain();
			clientData.put("remotedomain", remoteDoamin);
			DataMap key = new DataMap();
			key.put("name", state);
			key.put("domain", domain);
			dataClient.putData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(key), clientData);		
		} else {
			throw new RedbackException("Error exchanging authorization code");
		}			
	}

}
