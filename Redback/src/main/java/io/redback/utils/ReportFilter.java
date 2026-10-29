package io.redback.utils;

import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class ReportFilter {
	public String object;
	public DataMap filter;
	public String search;
	public DataMap sort;
	public String uid;
	
	public ReportFilter(String o, DataMap f, String s, DataMap so, String u) {
		object = o;
		filter = f;
		search = s;
		sort = so;
		uid = u;
	}
	
	public static DataList convertToDataList(List<ReportFilter> filters) {
		DataList list = new DataList();
		for(ReportFilter filter: filters) {
			DataMap map = new DataMap("object", filter.object, "filter", filter.filter, "search", filter.search, "sort", filter.sort, "uid", filter.uid);
			list.add(map);
		}
		return list;
	}
}
