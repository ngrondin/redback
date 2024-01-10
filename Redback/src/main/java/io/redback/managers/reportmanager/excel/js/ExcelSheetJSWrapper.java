package io.redback.managers.reportmanager.excel.js;

import java.util.Date;

import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.excel.CellFormatter;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import jxl.CellView;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;


public class ExcelSheetJSWrapper extends ObjectJSWrapper {
	protected WritableSheet sheet;
	
	public ExcelSheetJSWrapper(WritableSheet s) {
		super(new String[] {"setCell"});
		sheet = s;
	}
	
	public Object get(String key) {
		if(key.equals("setCell")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						WritableCellFormat wcf = CellFormatter.createFormat(arguments.length >= 4 ? arguments[3] : null);
						int col = ((Long)arguments[0]).intValue();
						int row = ((Long)arguments[1]).intValue();
						if(arguments[2] instanceof java.lang.Number) {
							Number num = new Number(col, row, ((java.lang.Number)arguments[2]).doubleValue(), wcf);
							sheet.addCell(num);
						} else if(arguments[2] instanceof String) {
							Label lbl = new Label(col, row, ((String)arguments[2]), wcf);	
							sheet.addCell(lbl);
						} else if(arguments[2] instanceof Date) {
							DateTime dt = new DateTime(col, row, ((Date)arguments[2]), wcf);
							sheet.addCell(dt);
						}
						
						return null;
					} catch(Exception e) {
						throw new RuntimeException("Error setting cell value", e);
					}
				}
			};
		} else if(key.equals("setColumnWidth")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						int col = ((Long)arguments[0]).intValue();
						int width = ((Long)arguments[1]).intValue() * 128;
						CellView cv = new CellView();
						cv.setSize(width);
						sheet.setColumnView(col, cv);
						return null;
					} catch(Exception e) {
						throw new RuntimeException("Error setting col width", e);
					}
				}
			};
		} else if(key.equals("setRowHeight")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						int row = ((Long)arguments[0]).intValue();
						int height = ((Long)arguments[1]).intValue() * 128;
						CellView cv = new CellView();
						cv.setSize(height);
						sheet.setRowView(row, cv);
						return null;
					} catch(Exception e) {
						throw new RuntimeException("Error setting row height", e);
					}
				}
			};
		} else {	
			return null;
		}
	}
}
