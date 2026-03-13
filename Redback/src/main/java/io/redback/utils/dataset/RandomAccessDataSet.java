package io.redback.utils.dataset;

import io.firebus.data.DataMap;

public interface RandomAccessDataSet extends DataSet {
	
	public DataMap getRow(int i);
	
	public int getRowCount();
	
}

