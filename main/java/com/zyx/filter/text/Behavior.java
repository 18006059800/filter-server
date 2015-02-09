package com.zyx.filter.text;

import com.zyx.filter.thrift.TFilterLevel;


/**
 * 文本过滤行为
 * @author Ervin.zhang
 */
@FunctionalInterface
public interface Behavior {
	
	/**
	 * 当文本遇到一个关键字时，会执行action方法，对文本进行处理，或者返回标志
	 * @param text 被过滤文本
	 * @return 过滤行为类型
	 */
	TFilterLevel action(StringBuilder text);
	
}
