package io.redback.managers.reportmanager.excel.js;

import java.util.Arrays;
import java.util.Date;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import jxl.write.Label;
import jxl.write.Number;
import jxl.write.DateTime;
import jxl.write.WritableSheet;


public class ExcelSheetJSWrapper implements ProxyObject {
	protected String[] members = {
			"setCell"
		};
	protected WritableSheet sheet;
	
	public ExcelSheetJSWrapper(WritableSheet s) {
		sheet = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("setCell")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						int col = arguments[0].asInt();
						int row = arguments[1].asInt();
						if(arguments[2].isNumber()) {
							Number num = new Number(col, row, arguments[2].asDouble());
							sheet.addCell(num);
						} else if(arguments[2].isString()) {
							Label lbl = new Label(col, row, arguments[2].asString());	
							sheet.addCell(lbl);
						} else if(arguments[2].isDate()) {
							DateTime dt = new DateTime(col, row, Date.from(arguments[2].asInstant()));
							sheet.addCell(dt);
						}
						
						return null;
					} catch(Exception e) {
						throw new RuntimeException("Error setting cell value", e);
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
