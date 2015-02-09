package com.zyx.filter;

import java.util.Map;

import com.zyx.filter.exception.FilterException;
import com.zyx.filter.thrift.TJudgeResult;

public abstract class AbstractFilter implements Filter {
	
	/**
	 * 过滤器名称
	 */
	protected String name;
	
	/**
	 * 需要进行频率过滤的字段，例如:"ip"
	 */
	protected String field;
	

	/**
	 * @return the {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the {@link #name} to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the {@link #field}
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field the {@link #field} to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	@Override
	public TJudgeResult doFilter(Map<String, String> req, Map<String, String> resp) {
		
		if (req.containsKey(field)) {
			Object val = req.get(field);
			return filter(val, resp);
		}
		
		return TJudgeResult.OK;
		
	}
	
	/**
	 * 对字段进行校验，一般只在构造的时候进行校验，所以子类一般不提供相关字段的setter方法
	 */
	public void checkInput() throws FilterException {

		if (name == null || name.length() == 0) {
			throw FilterException.fieldEmptyException("name");
		}
		
		if (field == null || field.length() == 0) {
			throw FilterException.fieldEmptyException("field");
		}

	}
	
	public TJudgeResult filter(Object filterObject) {
		throw new AbstractMethodError();
	}
	
	public TJudgeResult filter(Object filterObject, Map<String, String> resp) {
		return filter(filterObject);
	}

}
