package io.redback.managers.reportmanager.excel.js;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.redback.exceptions.RedbackException;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.CellView;
import jxl.format.Colour;
import jxl.write.DateTime;
import jxl.write.WritableSheet;


public class ExcelSheetJSWrapper extends ObjectJSWrapper {
	protected WritableSheet sheet;
	protected Map<String, Colour> colourMap;
	
	public ExcelSheetJSWrapper(WritableSheet s) {
		super(new String[] {"setCell"});
		sheet = s;
		colourMap = new HashMap<String, Colour>();
		colourMap.put("YELLOW", Colour.YELLOW);
		colourMap.put("RED", Colour.RED);
		colourMap.put("GREEN", Colour.GREEN);
	}
	
	public Object get(String key) {
		if(key.equals("setCell")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						WritableCellFormat wcf = new WritableCellFormat();
						if(arguments.length >= 4 && arguments[3] != null) {
							Colour c = colourMap.get(arguments[3]);
							if(c != null)
								wcf.setBackground(c);
						}
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
						throw new RuntimeException("Error setting cell value", e);
					}
				}
			};
		} else {	
			return null;
		}
	}
}
