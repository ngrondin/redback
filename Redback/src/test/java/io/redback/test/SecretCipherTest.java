package io.redback.test;

import java.util.Base64;

import io.firebus.data.DataMap;
import io.redback.managers.secretmanager.SecretCipher;

public class SecretCipherTest {

	public static void main(String[] args) throws Exception {
		String k1 = Base64.getEncoder().encodeToString(new byte[32]);
		byte[] b2 = new byte[32];
		b2[0] = 7;
		String k2 = Base64.getEncoder().encodeToString(b2);

		SecretCipher c1 = new SecretCipher(config("k1", "k1", k1));
		String stored = c1.encrypt("eimain/intclient:sftp1:secret", "acme-pass");
		check("round trip", c1.decrypt("eimain/intclient:sftp1:secret", stored).equals("acme-pass"));
		check("value is not in the stored text", !stored.contains("acme-pass") && stored.startsWith("k1:"));
		check("same value twice gives different text", !stored.equals(c1.encrypt("eimain/intclient:sftp1:secret", "acme-pass")));
		check("other slot does not open", fails(() -> c1.decrypt("qfmain/intclient:sftp1:secret", stored)));
		byte[] raw = Base64.getDecoder().decode(stored.substring(3));
		raw[raw.length - 20] ^= 1;
		String tampered = "k1:" + Base64.getEncoder().encodeToString(raw);
		check("changed text does not open", fails(() -> c1.decrypt("eimain/intclient:sftp1:secret", tampered)));

		DataMap both = config("k2", "k1", k1);
		both.getObject("keys").put("k2", k2);
		SecretCipher c2 = new SecretCipher(both);
		check("old key still opens after a new key", c2.decrypt("eimain/intclient:sftp1:secret", stored).equals("acme-pass"));
		check("new values use the new key", c2.encrypt("eimain/x", "y").startsWith("k2:") && !c2.isCurrentKey(stored));
		check("wrong key does not open", fails(() -> new SecretCipher(config("k1", "k1", k2)).decrypt("eimain/intclient:sftp1:secret", stored)));
		check("short key refused", fails(() -> new SecretCipher(config("k1", "k1", Base64.getEncoder().encodeToString(new byte[16])))));
		check("missing keys refused", fails(() -> new SecretCipher(new DataMap())));
		System.out.println("All secret cipher checks passed");
	}

	static DataMap config(String current, String id, String key) {
		DataMap cfg = new DataMap();
		cfg.put("currentkey", current);
		cfg.put("keys", new DataMap(id, key));
		return cfg;
	}

	interface Step { void run() throws Exception; }

	static boolean fails(Step s) {
		try { s.run(); return false; } catch(Exception e) { return true; }
	}

	static void check(String what, boolean ok) {
		System.out.println((ok ? "ok   " : "FAIL ") + what);
		if(!ok) System.exit(1);
	}
}
