package io.redback.managers.integrationmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.logging.Logger;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.ScriptFactory;
import io.redback.client.ConfigClient;
import io.redback.client.AccessManagementClient;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.client.SecretsClient;
import io.redback.client.GatewayClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.SysUserManager;
import io.redback.utils.CollectionConfig;
import io.redback.utils.ConfigCache;
import io.redback.utils.js.LoggerJSFunction;
import io.redback.utils.js.RedbackUtilsJSWrapper;

/**
 * Runs the integration clients (S3, SFTP, Gmail, Outlook...) for each domain, from the client data rows.
 *
 * Secrets: with a "secretservice" set, the secret fields of a client data row are kept encrypted by the secrets
 * service, reached over firebus as the system user, and the row only holds the marker "(secret)".
 * - a raw value found in a row is moved to the secrets on read, and the row gets the marker
 * - refreshed tokens are written to the secrets, never into the row
 * - an empty field means not set, so the existing disconnect script still works
 * - while the secrets service has no key the rows are used as they are, like before
 * Config:
 * - secretservice: name of the secrets service
 * - accessmanagementservice: used for the system user session
 * - secretfields: list of client data fields kept as secrets, default access_token, refresh_token, client_secret, secret
 */
public class IntegrationManager {
	public static final String SECRET_MARKER = "(secret)";
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
	protected SecretsClient secretsClient;
	protected SysUserManager sysUserManager;
	protected boolean secretsEnabled;
	protected long secretsCheckedAt;
	protected List<String> secretFields;

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
			if(config.getString("secretservice") != null) {
				secretsClient = new SecretsClient(firebus, config.getString("secretservice"));
				sysUserManager = new SysUserManager(new AccessManagementClient(firebus, config.getString("accessmanagementservice")), config);
			}
			secretFields = new ArrayList<String>();
			DataList fieldList = config.getList("secretfields");
			if(fieldList != null) {
				for(int i = 0; i < fieldList.size(); i++)
					secretFields.add(fieldList.getString(i));
			} else {
				secretFields.add("access_token");
				secretFields.add("refresh_token");
				secretFields.add("client_secret");
				secretFields.add("secret");
			}
			final IntegrationManager im = this;
			clientConfigs = new ConfigCache<ClientConfig>(configClient, "rbin", "client", 3600000, new ConfigCache.ConfigFactory<ClientConfig>() {
				public ClientConfig createConfig(DataMap map) throws Exception {
					return new ClientConfig(im, map);
				}});
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
	
	protected String getRedirectUri(String host, String clientName) {
		return host + "/" + name + "/" + clientName;
	}
	
	protected DataMap getClientData(Session session, String name, String domain) throws RedbackException {
		if(session.hasAccessToDomain(domain)) {
			DataMap clientData = null;
			DataMap filter = new DataMap();
			filter.put("name", name);
			filter.put("domain", domain);
			DataMap resp = dataClient.getData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(filter), null);
			if(resp != null && resp.getList("result") != null && resp.getList("result").size() > 0) {
				clientData = resp.getList("result").getObject(0);
				openSecrets(domain, name, clientData);
				return clientData;
			} else {
				throw new RedbackException("Could not find tokens for client " + name + " and domain " + domain);
			}			
		} else {
			throw new RedbackException("No access to domain " + domain);
		}
	}

	protected Client getClient(Session session, String name, String domain, boolean checkToken) throws RedbackException {
		DataMap clientData = getClientData(session, name, domain);
		String cconfigName = clientData.getString("client");
		ClientConfig config = clientConfigs.get(session, cconfigName);
		String authType = clientData.getString("authtype") != null ? clientData.getString("authtype") : "oauth";
		if(authType.equals("oauth")) {
			String accessToken = clientData.getString("access_token");
			long expiry = clientData.containsKey("expiry") ? clientData.getNumber("expiry").longValue() : 0;
			if(checkToken && (accessToken == null || accessToken.equals("") || expiry < System.currentTimeMillis())) {
				String clientId = config.clientId != null ? config.clientId : clientData.getString("client_id"); //This is because some non-standard integrations have dynamic client_id
				String clientSecret = config.clientSecrect != null ? config.clientSecrect : clientData.getString("client_secret");
				String refreshToken = clientData.getString("refresh_token");
				if(refreshToken != null && !refreshToken.equals("")) {
					DataMap form = new DataMap();
					form.put("client_id", clientId);
					form.put("client_secret", clientSecret);
					form.put("grant_type", "refresh_token");
					form.put("refresh_token", refreshToken);
					if(config.loginUrl != null && session.getDataAsString("host") != null) {
						form.put("redirect_uri", getRedirectUri(session.getDataAsString("host"), config.name));
					}					
					DataMap refreshResp = gatewayClient.postForm(config.tokenUrl, form);
					if(refreshResp != null) {
						DataMap changed = new DataMap();
						if(refreshResp.getString("refresh_token") != null)
							changed.put("refresh_token", refreshResp.getString("refresh_token"));
						if(refreshResp.getString("access_token") != null)
							changed.put("access_token", refreshResp.getString("access_token"));
						if(refreshResp.getNumber("expires_in") != null)
							changed.put("expiry", System.currentTimeMillis() + (refreshResp.getNumber("expires_in").longValue() * 1000));
						changed.put("lastupdate", new Date());
						for(String k: changed.keySet())
							clientData.put(k, changed.get(k));
						putClientData(domain, name, changed);
					} else {
						throw new RedbackException("Error refreshing tokens");
					}				
				} else {
					throw new RedbackException("No availabile refresh token");
				}
			}
		} else if(authType.equals("key")) {
			String key = clientData.getString("key");
			if(key == null) {
				throw new RedbackException("No availabile access key");
			}
		}
		Client client = new Client(session, this, config, clientData, gatewayClient);
		return client;
	}
	
	public DataMap get(Session session, String name, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain, true);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "get");
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("options", options);
		return client.call(context);
	}

	public List<DataMap> list(Session session, String name, String domain, String objectName, DataMap filter, DataMap options, int page, int pageSize) throws RedbackException {
		Client client = getClient(session, name, domain, true);
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
		Client client = getClient(session, name, domain, true);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "update");
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("data", data);
		context.put("options", options);
		return client.call(context);
	}

	public DataMap create(Session session, String name, String domain, String objectName, DataEntity data, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain, true);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "create");
		context.put("object", objectName);
		context.put("data", data);
		context.put("options", options);
		return client.call(context);
	}

	public void delete(Session session, String name, String domain, String objectName, String uid, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain, true);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("action", "delete");
		context.put("object", objectName);
		context.put("uid", uid);
		context.put("options", options);
		client.call(context);
	}
	
	public Object execute(Session session, String name, String domain, String function, DataEntity data, DataMap options) throws RedbackException {
		Client client = getClient(session, name, domain, true);
		return client.executeFunction(function, data, options);
	}

	public String getLoginUrl(Session session, String name, String domain) throws RedbackException {
		Client client = getClient(session, name, domain, false);
		String host = session.getDataAsString("host");
		String redirect = getRedirectUri(host, client.config.name);
		String url = client.config.loginUrl + "?response_type=code&redirect_uri=" + redirect + "&client_id=" + client.config.clientId +"&scope=" + client.config.scope + (client.config.extraLoginParams != null ? "&" + client.config.extraLoginParams : "") + "&state=" + name;
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
		form.put("redirect_uri", getRedirectUri((String)session.getDataAsString("host"), client.config.name));
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
			putClientData(domain, state, clientData);
		} else {
			throw new RedbackException("Error exchanging authorization code");
		}			
	}

	/** Moves the raw secrets of every client data row of the domain into the secrets. Returns the number of rows changed. */
	public int moveSecrets(Session session, String domain) throws RedbackException {
		if(!secretsOn())
			throw new RedbackException("No secrets key set");
		if(domain == null || !session.hasAccessToDomain(domain) || !session.getUserProfile().canWrite("rb.accesscat.admin"))
			throw new RedbackException("No rights to move the integration secrets");
		DataMap resp = dataClient.getData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(new DataMap("domain", domain)), null);
		int count = 0;
		DataList rows = resp != null ? resp.getList("result") : null;
		for(int i = 0; rows != null && i < rows.size(); i++) {
			DataMap row = rows.getObject(i);
			if(openSecrets(domain, row.getString("name"), row))
				count++;
		}
		return count;
	}

	/** Puts the plain secret values into the row in memory. Raw values still in the db row are moved to the secrets first. */
	protected boolean openSecrets(String domain, String clientName, DataMap clientData) throws RedbackException {
		if(!secretsOn()) return false;
		Session sys = sysUserManager.getSession();
		DataMap moved = new DataMap();
		for(String field: secretFields) {
			String value = clientData.getString(field);
			if(value == null || value.equals("")) continue;
			if(value.equals(SECRET_MARKER)) {
				clientData.put(field, secretsClient.get(sys, domain, secretName(clientName, field)));
			} else {
				secretsClient.put(sys, domain, secretName(clientName, field), value);
				moved.put(field, SECRET_MARKER);
			}
		}
		if(moved.keySet().size() > 0)
			dataClient.putData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(key(domain, clientName)), clientDataCollection.convertObjectToSpecific(moved));
		return moved.keySet().size() > 0;
	}

	/** Writes fields to the client data row, the secret ones go to the secrets and the row gets the marker. */
	protected void putClientData(String domain, String clientName, DataMap data) throws RedbackException {
		boolean on = secretsOn();
		DataMap row = new DataMap();
		for(String field: data.keySet()) {
			Object value = data.get(field);
			if(on && secretFields.contains(field) && value instanceof String && !((String)value).equals("") && !value.equals(SECRET_MARKER)) {
				secretsClient.put(sysUserManager.getSession(), domain, secretName(clientName, field), (String)value);
				row.put(field, SECRET_MARKER);
			} else {
				row.put(field, value);
			}
		}
		dataClient.putData(clientDataCollection.getName(), clientDataCollection.convertObjectToSpecific(key(domain, clientName)), clientDataCollection.convertObjectToSpecific(row));
	}

	protected String secretName(String clientName, String field) {
		return "intclient:" + clientName + ":" + field;
	}

	protected DataMap key(String domain, String clientName) {
		DataMap key = new DataMap();
		key.put("name", clientName);
		key.put("domain", domain);
		return key;
	}

	/** Whether the secrets service has a key, asked again every 5 minutes, so a key set later is picked up. */
	protected boolean secretsOn() throws RedbackException {
		if(secretsClient == null) return false;
		long now = System.currentTimeMillis();
		if(now - secretsCheckedAt > 300000) {
			secretsEnabled = secretsClient.isEnabled(sysUserManager.getSession());
			secretsCheckedAt = now;
			if(!secretsEnabled)
				Logger.warning("rb.integration.nosecretkey", "The secrets service has no key, client secrets stay in the client data");
		}
		return secretsEnabled;
	}

}
