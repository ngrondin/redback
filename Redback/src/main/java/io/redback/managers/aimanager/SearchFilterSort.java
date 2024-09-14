package io.redback.managers.aimanager;

import io.firebus.data.DataMap;

public class SearchFilterSort {
	public String search;
	public DataMap filter;
	public DataMap sort;
	
	public SearchFilterSort(String s, DataMap f, DataMap so) {
		search = s;
		filter = f;
		sort = so;
	}
}
