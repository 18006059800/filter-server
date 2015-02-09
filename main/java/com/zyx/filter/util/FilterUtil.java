package com.zyx.filter.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.to8to.filter.thrift.TFilter;

public class FilterUtil {
	/**
	 * 把所有过滤器按business_id分组
	 * 
	 * @param filters
	 *            所有过滤器
	 * @return 分组过的过滤器 key:business_id value:该业务下的所有过滤器
	 */
	public static Map<String, List<TFilter>> filtersGroupByBusiness(
			List<TFilter> filters) {

		if (filters == null) {
			return null;
		}

		return filters
				.stream()
				// 频率过滤器排前面
				.sorted((f1, f2) -> 
						f2.getType().ordinal() - f1.getType().ordinal())
				.collect(
						Collectors.groupingBy(f -> f.getBusiness_name(),
								Collectors.toList()));
	}
	
	

	public static void main(String[] args) {
		List<TFilter> filters = new ArrayList<TFilter>();
		for (int i = 0; i < 20; i++) {
			if (i % 3 == 0) {
				filters.add(new TFilter("aa" + i).setBusiness_id("111"));
			} else if (i % 3 == 1) {
				filters.add(new TFilter("bb" + i).setBusiness_id("222"));
			} else if (i % 3 == 2) {
				filters.add(new TFilter("cc" + i).setBusiness_id("333"));
			}
		}
		System.out.println(filtersGroupByBusiness(filters));
	}
}
