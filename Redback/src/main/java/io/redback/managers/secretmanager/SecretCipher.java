package io.redback.managers.secretmanager;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

/**
 * Encrypts and decrypts secret values with AES-256-GCM from the JDK.
 *
 * - keys come from config as a map of key id to a base64 256 bit key, plus the id used for new values
 * - a stored value reads "keyid:base64(iv + ciphertext + tag)", so old values still open after a new key is added
 * - each value is bound to its slot (domain and name) as additional data, so a value copied to another slot does not open
 */
public class SecretCipher {
	protected static final int IV_BYTES = 12;
	protected static final int TAG_BITS = 128;
	protected Map<String, SecretKeySpec> keys = new HashMap<String, SecretKeySpec>();
	protected String currentKeyId;
	protected SecureRandom random = new SecureRandom();

	/**
	 * Config:
	 * - keys: map of key id to base64 key (32 bytes)
	 * - currentkey: key id for new values
	 */
	public SecretCipher(DataMap config) throws RedbackException {
		DataMap keyMap = config != null ? config.getObject("keys") : null;
		currentKeyId = config != null ? config.getString("currentkey") : null;
		if(keyMap == null || keyMap.keySet().size() == 0)
			throw new RedbackException("Secrets need at least one encryption key");
		for(String id: keyMap.keySet()) {
			if(id.contains(":"))
				throw new RedbackException("Secret key id '" + id + "' cannot contain ':'");
			byte[] raw = Base64.getDecoder().decode(keyMap.getString(id));
			if(raw.length != 32)
				throw new RedbackException("Secret key '" + id + "' must be 32 bytes, base64 encoded");
			keys.put(id, new SecretKeySpec(raw, "AES"));
		}
		if(currentKeyId == null || !keys.containsKey(currentKeyId))
			throw new RedbackException("The current secret key is not in the keys");
	}

	public String encrypt(String slot, String plain) throws RedbackException {
		try {
			byte[] iv = new byte[IV_BYTES];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, keys.get(currentKeyId), new GCMParameterSpec(TAG_BITS, iv));
			cipher.updateAAD(slot.getBytes(StandardCharsets.UTF_8));
			byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
			byte[] out = new byte[iv.length + ct.length];
			System.arraycopy(iv, 0, out, 0, iv.length);
			System.arraycopy(ct, 0, out, iv.length, ct.length);
			return currentKeyId + ":" + Base64.getEncoder().encodeToString(out);
		} catch(Exception e) {
			throw new RedbackException("Error encrypting a secret", e);
		}
	}

	public String decrypt(String slot, String stored) throws RedbackException {
		int sep = stored != null ? stored.indexOf(":") : -1;
		if(sep < 1)
			throw new RedbackException("Stored secret has no key id");
		SecretKeySpec key = keys.get(stored.substring(0, sep));
		if(key == null)
			throw new RedbackException("Stored secret uses an unknown key '" + stored.substring(0, sep) + "'");
		try {
			byte[] in = Base64.getDecoder().decode(stored.substring(sep + 1));
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, in, 0, IV_BYTES));
			cipher.updateAAD(slot.getBytes(StandardCharsets.UTF_8));
			return new String(cipher.doFinal(in, IV_BYTES, in.length - IV_BYTES), StandardCharsets.UTF_8);
		} catch(Exception e) {
			throw new RedbackException("Error decrypting a secret", e);
		}
	}

	public boolean isCurrentKey(String stored) {
		return stored != null && stored.startsWith(currentKeyId + ":");
	}
}
