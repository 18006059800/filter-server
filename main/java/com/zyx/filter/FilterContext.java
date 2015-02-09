package com.zyx.filter;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.to8to.commons.utils.Config;
import com.to8to.filter.exception.FilterException;
import com.to8to.filter.frequency.FrequencyFilterWrap;
import com.to8to.filter.text.TextFilterWrap;
import com.to8to.filter.thrift.TFilter;
import com.to8to.filter.thrift.TFilterKeyword;
import com.to8to.filter.thrift.TFilterType;

public class FilterContext {

	public static Config config;

	private static Map<String, List<Filter>> filterMap;
	
	/**
	 * key:filter_id  value:TextFilterWrap
	 */
	public static Map<String, TextFilterWrap> textFilterMap = new HashMap<String, TextFilterWrap>();
	
	/**
	 * key:filter_id  value:FrequencyFilterWrap
	 */
	public static Map<String, FrequencyFilterWrap> frequencyFilterMap = new HashMap<String, FrequencyFilterWrap>();

	public static Map<String, String> keywordFilterIdMap = new HashMap<String, String>();
	
	public static void createFilterMap(List<TFilter> tfilters) {
		if (tfilters == null) {
			filterMap = new HashMap<String, List<Filter>>();
		}
		filterMap = tfilters
				.stream()
				// 频率过滤器排前面
				.sorted((f1, f2) -> f2.getType().ordinal()- f1.getType().ordinal())
				.collect(
					groupingBy(
						// 以业务名称分组
						f -> f.getBusiness_name(),
						// 使用纯程安全的HashMap
						ConcurrentHashMap::new, 
						mapping(
							// 将TFilter转成可用的Filter实例	
							FilterContext::wrap, 
							toList()
						)
					)
				);
				
	}

	private static Filter wrap(TFilter filter) {
		try {
			switch (filter.getType()) {
			case TEXT:
				TextFilterWrap textFilter = new TextFilterWrap(filter);
				for (TFilterKeyword keyword : textFilter.getFilterKeywords()) {
					keywordFilterIdMap.put(keyword.get_id(), filter.get_id());
				}
				textFilterMap.put(filter.get_id(), textFilter);
				// 创建特里查找树
				textFilter.createTrieTree();
				return textFilter;
			case FREQUENCY:
				FrequencyFilterWrap freqFilter = new FrequencyFilterWrap(filter);
				frequencyFilterMap.put(filter.get_id(), freqFilter);
				return freqFilter;
			default:
				throw new IllegalStateException();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void addKeyword(TFilterKeyword keyword) throws FilterException {
		TextFilterWrap textFilter = textFilterMap.get(keyword.getFilter_id());
		if (textFilter == null) {
			throw FilterException.fieldNullException("textFilter");
		}
		textFilter.addKeyword(keyword);
		keywordFilterIdMap.put(keyword.get_id(), keyword.getFilter_id());
	}
	
	public static void deleteKeyword(String keywordId) throws FilterException {
		String filterId = keywordFilterIdMap.get(keywordId);
		TextFilterWrap wrap = textFilterMap.get(filterId);
		wrap.deleteKeyword(keywordId);
	}
	
	public static void addFilter(TFilter filter) throws FilterException {
		if (filter.getType() == TFilterType.TEXT) {
			textFilterMap.put(filter.get_id(), new TextFilterWrap(filter));
		} else if (filter.getType() == TFilterType.FREQUENCY) {
			frequencyFilterMap.put(filter.get_id(), new FrequencyFilterWrap(filter));
		}
		List<Filter> bizFilters = filterMap.get(filter.getBusiness_id());
		if (bizFilters == null) {
			bizFilters = new ArrayList<Filter>();
		}
		bizFilters.add(wrap(filter));
		filterMap.put(filter.getBusiness_id(), bizFilters);
	}
	
	public static List<Filter> getFilter(String businessName) {
		if (businessName == null) {
			return null;
		}
		return filterMap.get(businessName);
	}
}
