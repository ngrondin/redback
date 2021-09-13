package io.redback.managers.reportmanager.excel.js;


import io.redback.exceptions.RedbackException;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


public class ExcelWorkbookJSWrapper extends ObjectJSWrapper {
	protected WritableWorkbook workbook;
	
	public ExcelWorkbookJSWrapper(WritableWorkbook wb) {
		super(new String[] {"addSheet"});
		workbook = wb;
	}
	
	public Object get(String key) {
		if(key.equals("addSheet")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					WritableSheet sheet = workbook.createSheet(name, 0);
					return new ExcelSheetJSWrapper(sheet);
				}
			};
		} else {	
			return null;
		}
	}
}
