package com.zyx.filter;

import java.util.Map;

import com.zyx.filter.thrift.TJudgeResult;

public interface Filter {
	
	/**
	 * @param req
	 * @param resp
	 * @return 是否需要继续往下过滤
	 */
	TJudgeResult doFilter(Map<String, String> req, Map<String, String> resp) ;
	
}
