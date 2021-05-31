package io.redback.managers.reportmanager.excel.js;

import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


public class ExcelWorkbookJSWrapper implements ProxyObject {
	protected String[] members = {
			"addSheet"
		};
	protected WritableWorkbook workbook;
	
	public ExcelWorkbookJSWrapper(WritableWorkbook wb) {
		workbook = wb;
	}
	
	public Object getMember(String key) {
		if(key.equals("addSheet")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					WritableSheet sheet = workbook.createSheet(name, 0);
					return new ExcelSheetJSWrapper(sheet);
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
