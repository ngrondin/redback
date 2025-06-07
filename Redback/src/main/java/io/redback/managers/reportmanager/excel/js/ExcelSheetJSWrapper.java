package io.redback.managers.reportmanager.excel.js;

import java.util.Date;

import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.excel.CellFormatter;
import io.redback.security.Session;
import io.redback.utils.DateUtils;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import jxl.CellView;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;


public class ExcelSheetJSWrapper extends ObjectJSWrapper {
	protected Session session;
	protected WritableSheet sheet;
	protected CellFormatter cellFormatter;
	
	public ExcelSheetJSWrapper(Session sess, WritableSheet sh) {
		super(new String[] {"setCell"});
		session = sess;
		sheet = sh;
		cellFormatter = new CellFormatter();
	}
	
	public Object get(String key) {
		if(key.equals("setCell")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						int col = ((Long)arguments[0]).intValue();
						int row = ((Long)arguments[1]).intValue();
						Object val = arguments[2];
						WritableCellFormat wcf = cellFormatter.createFormat(arguments.length >= 4 ? arguments[3] : null);
						if(val instanceof java.lang.Number) {
							double doubleVal = ((java.lang.Number)val).doubleValue();
							Number num = new Number(col, row, doubleVal, wcf);
							sheet.addCell(num);
						} else if(val instanceof String) {
							Label lbl = new Label(col, row, (String)val, wcf);	
							sheet.addCell(lbl);
						} else if(val instanceof Date) {
							Date convertedDate = DateUtils.convertDateToTimezone((Date)val, session.getTimezone());
							DateTime dt = new DateTime(col, row, convertedDate, wcf, DateTime.GMT);
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
