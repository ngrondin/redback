package io.redback.test;

import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ObjectClient;
import io.redback.client.ProcessAssignmentRemote;
import io.redback.client.ProcessClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.security.Session;
public class TestClient {
	
	protected Firebus firebus;
	protected ObjectClient objectClient;
	protected ProcessClient processClient;
	protected String url;
	protected DataMap roleMap;
	protected HttpClient httpClient;
	
	protected class HTTPProxy implements ServiceProvider {
		protected String servicePath;
		
		public HTTPProxy(String sp) {
			servicePath = sp;
		}

		public Payload service(Payload payload) throws FunctionErrorException {
			try {
				HttpPost httpRequest = new HttpPost(url + "/" + servicePath);
				httpRequest.setEntity(new ByteArrayEntity(payload.getBytes()));
				httpRequest.setHeader("Content-Type", "application/json");
				httpRequest.setHeader("Cookie", "rbtoken=" + payload.metadata.get("token"));
				HttpResponse response = httpClient.execute(httpRequest);
	    		int respStatus = response.getStatusLine().getStatusCode(); 
	    		HttpEntity entity = response.getEntity();
	    		if(respStatus >= 200 && respStatus < 400)
	    		{
	    			String responseStr = EntityUtils.toString(entity);
	    			EntityUtils.consume(entity);
	    			Payload respPayload = new Payload(responseStr);    			
	    			return respPayload;
	    		} else {
	    			throw new RedbackException("HTTP response " + respStatus);
	    		}
			} catch(Exception e) {
				throw new FunctionErrorException("Error proxying firebus request", e);
			}
		}

		public ServiceInformation getServiceInformation() {
			return null;
		}
		
	}

	public TestClient(String fbNet, String fbPass, String os, String ps, String u, String jwtIssuer, String jwtSecret, DataMap rm) {
		firebus = new Firebus(fbNet, fbPass);
		firebus.setThreadCount(50);
		firebus.setDefaultTimeout(15000);
		if(u == null) {
			objectClient = new ObjectClient(firebus, os);
			processClient = new ProcessClient(firebus, ps);
		} else {
			url = u;
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		    connectionManager.setMaxTotal(100);
		    connectionManager.setDefaultMaxPerRoute(50);
			httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
			firebus.registerServiceProvider("proxy_" + os, new HTTPProxy(os), 100);
			firebus.registerServiceProvider("proxy_" + ps, new HTTPProxy(ps), 100);
			objectClient = new ObjectClient(firebus, "proxy_" + os);
			processClient = new ProcessClient(firebus, "proxy_" + ps);
		}
		roleMap = rm;
		Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
		Iterator<String> it = roleMap.keySet().iterator();
		while(it.hasNext()) {
			String role = it.next();
			String token = JWT.create()
					.withIssuer(jwtIssuer)
					.withClaim("email", roleMap.getObject(role).getString("username"))
					.withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
					.sign(algorithm);
			roleMap.getObject(role).put("token", token);
		}
	}
	
	public boolean isReady() {
		return firebus.hasConnections() || url != null;
	}
	
	protected Session newSession(String role) throws RedbackException {
		Session session = new Session();
		if(roleMap.getObject(role) != null) {
			session.setToken(roleMap.getObject(role).getString("token"));
			session.setTimezone(ZoneId.systemDefault().getId());
			return session;
		} else {
			throw new RedbackException("Role " + role + " is not defined in role map");
		}
	}
	
	public RedbackObjectRemote getObject(String role, String objectname, String uid) throws RedbackException {
		RedbackObjectRemote ror = objectClient.getObject(newSession(role), objectname, uid);
		return ror;
	}

	public List<RedbackObjectRemote> listObjects(String role, String objectname, DataMap filter) throws RedbackException {
		List<RedbackObjectRemote> list = objectClient.listObjects(newSession(role), objectname, filter);
		return list;
	}
	
	public RedbackObjectRemote createObject(String role, String objectname) throws RedbackException {
		return createObject(role, objectname, new DataMap());
	}
	
	public RedbackObjectRemote createObject(String role, String objectname, DataMap data) throws RedbackException {
		RedbackObjectRemote ror = objectClient.createObject(newSession(role), objectname, data, true);
		return ror;
	}
	
	public ProcessAssignmentRemote getAssignment(String role, String objectname, String uid) throws RedbackException {
		DataMap filter = new DataMap();
		filter.put("data.objectname", objectname);
		filter.put("data.uid", uid);
		ProcessAssignmentRemote par = processClient.getAssignment(newSession(role), filter);
		return par;
	}
	
	public ProcessAssignmentRemote waitForAssignment(String role, String objectname, String uid, String code) throws RedbackException {
		ProcessAssignmentRemote processAssignment = null;
		do {
			processAssignment = getAssignment(role, objectname, uid);
		} while(processAssignment != null && !processAssignment.getInteractionCode().equals(code));
		return processAssignment;
	}
}
