package io.redback.managers.secretmanager;

import java.util.Date;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;

/**
 * Keeps secrets encrypted in their own collection, one row per domain and name.
 *
 * - users can set, clear and see the status of a secret, never read its value
 * - only code running in the same process (for example the integration manager) reads a value, with get
 * - a secret name is free text made of letters, digits and . _ : - (for example "intclient:sftp1:secret")
 *
 * Config:
 * - dataservice: the data service holding the collection
 * - collection: collection config, default name rbsec_secret
 * - accesscat: access category a user needs write rights on to set or clear a secret, default admin
 * - keys, currentkey: see SecretCipher
 */
public class SecretManager {
	protected DataClient dataClient;
	protected CollectionConfig collection;
	protected SecretCipher cipher;
	protected String accessCat;

	public SecretManager(DataMap config, Firebus firebus) throws RedbackException {
		dataClient = new DataClient(firebus, config.getString("dataservice"));
		collection = new CollectionConfig(config.getObject("collection"), "rbsec_secret");
		cipher = new SecretCipher(config);
		accessCat = config.containsKey("accesscat") ? config.getString("accesscat") : "admin";
	}

	public void put(Session session, String domain, String name, String value) throws RedbackException {
		checkAccess(session, domain, name, true);
		if(value == null || value.length() == 0) {
			dataClient.deleteData(collection.getName(), collection.convertObjectToSpecific(key(domain, name)));
		} else {
			DataMap data = new DataMap();
			data.put("value", cipher.encrypt(slot(domain, name), value));
			data.put("updated", new Date());
			data.put("updatedby", session.getUserProfile().getUsername());
			dataClient.putData(collection.getName(), collection.convertObjectToSpecific(key(domain, name)), collection.convertObjectToSpecific(data));
		}
	}

	public DataMap status(Session session, String domain, String name) throws RedbackException {
		checkAccess(session, domain, name, false);
		DataMap row = getRow(domain, name);
		DataMap status = new DataMap();
		status.put("name", name);
		status.put("set", row != null);
		if(row != null) {
			status.put("updated", row.get("updated"));
			status.put("updatedby", row.getString("updatedby"));
		}
		return status;
	}

	/** The plain value, or null when not set. In-process use only, never returned to a user. */
	public String get(String domain, String name) throws RedbackException {
		DataMap row = getRow(domain, name);
		return row != null ? cipher.decrypt(slot(domain, name), row.getString("value")) : null;
	}

	/** Sets a value from in-process code, for example a refreshed token. */
	public void putAsSystem(String domain, String name, String value, String by) throws RedbackException {
		checkName(name);
		DataMap data = new DataMap();
		data.put("value", cipher.encrypt(slot(domain, name), value));
		data.put("updated", new Date());
		data.put("updatedby", by);
		dataClient.putData(collection.getName(), collection.convertObjectToSpecific(key(domain, name)), collection.convertObjectToSpecific(data));
	}

	protected DataMap getRow(String domain, String name) throws RedbackException {
		DataMap resp = collection.convertObjectToCanonical(dataClient.getData(collection.getName(), collection.convertObjectToSpecific(key(domain, name)), null));
		if(resp != null && resp.getList("result") != null && resp.getList("result").size() > 0)
			return resp.getList("result").getObject(0);
		return null;
	}

	protected void checkAccess(Session session, String domain, String name, boolean write) throws RedbackException {
		checkName(name);
		if(domain == null || !session.hasAccessToDomain(domain))
			throw new RedbackException("No access to domain " + domain);
		String right = "rb.accesscat." + accessCat;
		boolean ok = write ? session.getUserProfile().canWrite(right) : session.getUserProfile().canRead(right);
		if(!ok)
			throw new RedbackException("No rights on secrets");
	}

	protected void checkName(String name) throws RedbackException {
		if(name == null || !name.matches("[A-Za-z0-9._:\\-]{1,200}"))
			throw new RedbackException("Invalid secret name");
	}

	protected DataMap key(String domain, String name) {
		DataMap key = new DataMap();
		key.put("domain", domain);
		key.put("name", name);
		return key;
	}

	protected String slot(String domain, String name) {
		return domain + "/" + name;
	}
}
