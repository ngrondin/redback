package io.redback.utils.dataset;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public interface DataSet {
	
	public String[] getColumnNames();
	
	public boolean hasNext();
	
	public void next();
	
	public DataMap getRow();
	
	public DataSet selectColumns(String ...cols);
	
	public DataList uniqueValues(String col);
	
	public DataSet filter(String col, Object val);
	
	public DataList getRawData();
}
