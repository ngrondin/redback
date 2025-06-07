package io.redback.managers.reportmanager.excel.js;


import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


public class ExcelWorkbookJSWrapper extends ObjectJSWrapper {
	protected WritableWorkbook workbook;
	protected Session session;
	
	public ExcelWorkbookJSWrapper(Session sess, WritableWorkbook wb) {
		super(new String[] {"addSheet"});
		session = sess;
		workbook = wb;
	}
	
	public Object get(String key) {
		if(key.equals("addSheet")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					WritableSheet sheet = workbook.createSheet(name, 0);
					return new ExcelSheetJSWrapper(session, sheet);
				}
			};
		} else {	
			return null;
		}
	}
}
