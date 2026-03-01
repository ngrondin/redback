package io.redback.utils.dataset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class MemoryDataSet implements DataSet {
	protected String[] headers;
	protected List<Object[]> data;
	protected int cur;
	
	public MemoryDataSet() {
		data = new ArrayList<Object[]>();
		cur = 0;
	}
	
	public MemoryDataSet(String[] hdrs) {
		data = new ArrayList<Object[]>();
		cur = 0;
		headers = hdrs;
	}
	
	public void addRow(Object  ...vals) {
		if(vals.length == headers.length) {
			Object[] row = new Object[headers.length];
			for(int i = 0; i < headers.length; i++)
				row[i] = vals[i];
			data.add(row);
		}
	}
	
	public int getRowCount() {
		return data.size();
	}

	public String[] getColumnNames() {
		return headers;
	}

	public boolean hasNext() {
		return cur < data.size();
	}

	public void next() {
		cur++;
	}
	
	public DataMap getRow() {
		DataMap row = new DataMap();
		for(int i = 0; i < headers.length; i++)
			row.put(headers[i], data.get(cur)[i]);
		return row;
	}

	public DataSet selectColumns(String... cols) {
		int[] ind = new int[cols.length];
		for(int i = 0; i < cols.length; i++) 
			ind[i] = getColIndex(cols[i]);
		MemoryDataSet mds = new MemoryDataSet(cols);
		for(Object[] row: data) {
			Object[] newRow = new Object[cols.length];
			for(int i = 0; i < cols.length; i++)
				newRow[i] = row[ind[i]];
			mds.addRow(newRow);
		}
		return mds;
	}

	public DataList uniqueValues(String col) {
		int ind = getColIndex(col);
		Set<Object> set = new HashSet<Object>();
		for(Object[] row: data) 
			set.add(row[ind]);
		DataList list = new DataList();
		for(Object o : set)
			list.add(o);
		return list;
	}

	public DataSet filter(String col, Object val) {
		int ind = getColIndex(col);
		MemoryDataSet mds = new MemoryDataSet(headers);
		for(Object[] row: data) 
			if(row[ind].equals(val))
				mds.data.add(row);
		return mds;
	}
	
	protected int getColIndex(String col) {
		for(int j = 0; j < headers.length; j++) 
			if(headers[j].equals(col))
					return j;
		return -1;
	}
	
	public DataList getRawData() {
		DataList list = new DataList();
		for(Object[] row: data) {
			DataMap map = new DataMap();
			for(int i = 0; i < headers.length; i++)
				map.put(headers[i], row[i]);		
			list.add(map);
		}
		return list;
	}

}
