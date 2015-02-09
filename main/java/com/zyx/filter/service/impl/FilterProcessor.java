package com.zyx.filter.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyx.filter.Content;
import com.zyx.filter.Filter;
import com.zyx.filter.FilterContext;
import com.zyx.filter.thrift.FilterRequest;
import com.zyx.filter.thrift.FilterResponse;
import com.zyx.filter.thrift.TJudgeResult;

/**
 * 
 * @author Ervin.zhang
 */
public class FilterProcessor {

	private static Logger logger = LoggerFactory
			.getLogger(FilterServiceImpl.class);
	
	private static final String SYSTEM = "System";

	public FilterResponse process(FilterRequest req) {
		Map<String, String> resultMap = new HashMap<String, String>();
		List<Filter> filters = getAllFilters(req);
		if (filters == null || filters.size() == 0) {
			resultMap.put(Content.DESC, "没有设置任何过滤器！");
			logger.warn("没有设置任何过滤器！");
			return new FilterResponse(TJudgeResult.OK, resultMap);
		}
		TJudgeResult judgeResult = filter(filters, req.getFilterField(), resultMap);
		FilterResponse resp = new FilterResponse();
		resp.setJudgeResult(judgeResult);
		resp.setResultMap(resultMap);
		return resp;
	}



	/**
	 * 获取所有的过滤器（系统，业务）
	 */
	private List<Filter> getAllFilters(FilterRequest req) {
		List<Filter> bizFilters = FilterContext.getFilter(req.business_name);
		List<Filter> sysFilters = FilterContext.getFilter(SYSTEM);

		if (sysFilters == null && bizFilters == null) {
			return null;
		}
		if (sysFilters == null) {
			return bizFilters;
		}
		if (bizFilters == null) {
			return sysFilters;
		}

		List<Filter> filters = new ArrayList<Filter>();
		filters.addAll(sysFilters);
		filters.addAll(bizFilters);
		return filters;
	}

	private TJudgeResult filter(List<Filter> filters, Map<String, String> req, Map<String, String> resp) {
		TJudgeResult judgeResult = TJudgeResult.OK;
		for (Filter filter : filters) {
			TJudgeResult newResult = filter.doFilter(req, resp);
			// 敏感级别只能增加
			if (newResult.ordinal() > judgeResult.ordinal()) {
				judgeResult = newResult;
			}
			if (judgeResult == TJudgeResult.FORBIDDEN) {
				break;
			}
		}
		return judgeResult;
	}
}
