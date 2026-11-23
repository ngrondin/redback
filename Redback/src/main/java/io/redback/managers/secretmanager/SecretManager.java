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
 * Used only inside the secrets service. Every call checks the session it gets:
 * - put, status: the system user, or a user with write (put) or read (status) rights on the access category
 * - get: the system user only, so a value never goes back to a user
 * - and always access to the domain
 * - a secret name is free text made of letters, digits, spaces and . _ : - (for example "intclient:sftp1:secret")
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

	/** The plain value, or null when not set. System user only. */
	public String get(Session session, String domain, String name) throws RedbackException {
		checkDomain(session, domain, name);
		if(!session.getUserProfile().isSystem())
			throw new RedbackException("Only the system user can read a secret");
		DataMap row = getRow(domain, name);
		return row != null ? cipher.decrypt(slot(domain, name), row.getString("value")) : null;
	}

	protected DataMap getRow(String domain, String name) throws RedbackException {
		DataMap resp = collection.convertObjectToCanonical(dataClient.getData(collection.getName(), collection.convertObjectToSpecific(key(domain, name)), null));
		if(resp != null && resp.getList("result") != null && resp.getList("result").size() > 0)
			return resp.getList("result").getObject(0);
		return null;
	}

	protected void checkAccess(Session session, String domain, String name, boolean write) throws RedbackException {
		checkDomain(session, domain, name);
		if(session.getUserProfile().isSystem())
			return;
		String right = "rb.accesscat." + accessCat;
		boolean ok = write ? session.getUserProfile().canWrite(right) : session.getUserProfile().canRead(right);
		if(!ok)
			throw new RedbackException("No rights on secrets");
	}

	protected void checkDomain(Session session, String domain, String name) throws RedbackException {
		checkName(name);
		if(domain == null || !session.hasAccessToDomain(domain))
			throw new RedbackException("No access to domain " + domain);
	}

	protected void checkName(String name) throws RedbackException {
		if(name == null || !name.matches("[A-Za-z0-9._: \\-]{1,200}"))
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
