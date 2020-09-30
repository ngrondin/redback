package io.redback.client.js;

import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.client.GatewayClient;
import io.redback.utils.js.JSConverter;

public class GatewayClientJSWrapper implements ProxyObject {
	
	//private Logger logger = Logger.getLogger("io.redback");
	protected GatewayClient gatewayClient;
	protected String[] members = {"get", "post", "postform"};

	public GatewayClientJSWrapper(GatewayClient gc)
	{
		gatewayClient = gc;
	}
	
	public Object getMember(String key) {
		if(key.equals("get")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String url = arguments[0].asString();
					DataMap headers = arguments.length > 1 ? (DataMap)JSConverter.toJava(arguments[1]) : null;
					DataMap cookie = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
					try
					{
						DataMap ret = gatewayClient.get(url, headers, cookie);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on gateway get", e);
					}
				}
			};
		} else if(key.equals("post")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String url = arguments[0].asString();
					Object body = JSConverter.toJava(arguments[1]);
					DataMap headers = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						DataMap ret = gatewayClient.post(url, body, headers, cookie);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on gateway post", e);
					}
				}
			};
		} else if(key.equals("postform")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String url = arguments[0].asString();
					DataMap form = (DataMap)JSConverter.toJava(arguments[1]);
					DataMap headers = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
					DataMap cookie = arguments.length > 3 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						DataMap ret = gatewayClient.postForm(url, form, headers, cookie);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on gateway post", e);
					}
				}
			};
		} else {
			return null;
		}
	}

	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));
	}
	
	public boolean hasMember(String key) {
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
		
	}
	
	
}
