package io.redback.utils.dataset.js;

import java.util.Arrays;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.utils.dataset.DataSet;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class DataSetJSWrapper  extends ObjectJSWrapper {

	protected DataSet dataset;
	
	public DataSetJSWrapper(DataSet ds) {
		super(new String[] {"getRowCount", "getColumnNames", "hasNext", "next", "getRow", "selectColumns", "uniqueValues", "filter"});
		dataset = ds;
	}
	
	public DataSet getDataSet() {
		return dataset;
	}

	public Object get(String key) throws RedbackException {
		if(key.equals("getRowCount")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return dataset.getRowCount();
				}
			};
		} else if(key.equals("getColumnNames")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					List<String> cols = Arrays.asList(dataset.getColumnNames());
					return cols;
				}
			};
		} else if(key.equals("addRow")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					dataset.addRow(arguments);
					return null;
				}
			};

		} else if(key.equals("hasNext")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return dataset.hasNext();
				}
			};
		} else if(key.equals("next")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					dataset.next();
					return null;
				}
			};
		} else if(key.equals("getRow")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap row = dataset.getRow();
					return row;
				}
			};	
		} else if(key.equals("selectColumns")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String[] cols = new String[arguments.length];
					for(int i = 0; i < arguments.length; i++) 
						cols[i] = arguments[i].toString();
					DataSet nds = dataset.selectColumns(cols);
					return nds;
				}
			};
		} else if(key.equals("uniqueValues")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataList list = dataset.uniqueValues(arguments[0].toString());
					return list;
				}
			};		
		} else if(key.equals("filter")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataSet nds = dataset.filter(arguments[0].toString(), arguments[1]);
					return nds;
				}
			};	
		} else if(key.equals("getRawData")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return dataset.getRawData();
				}
			};				
		} else {
			return null;
		}
	}

}
