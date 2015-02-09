package com.zyx.filter.exception;

import com.zyx.filter.thrift.TTimeUnit;

/**
 * FilterException维护一个异常表，记录异常code和message
 * 
 * @author Ervin.zhang
 */
public class FilterException extends Exception {
	
	private static final long serialVersionUID = -1207633324998966467L;

	private int code;
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public FilterException() {
		super();
	}

	public FilterException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public FilterException(FilterException e) {
		super(e.getMessage());
		this.code = e.code;
	}
	
	public FilterException(Exception e) {
		super(e.getMessage());
		this.code = 1000;
	}
	
	public static FilterException thrirftObjectNullException() {
		return new FilterException(3001, "thrift对象为空！");
	}
	
	public static FilterException invalidObjectId() {
		return new FilterException(3001, "无效的ObjectId!");
	}
	
	public static FilterException updateFailed(String oper) {
		return new FilterException(3002, oper + "操作执行失败!");
	}
	
	public static FilterException idNotFoundException(String _id) {
		return new FilterException(3003, "找不到对象，_id : " + _id);
	}
	
	public static FilterException fieldNullException(String fieldName) {
		return new FilterException(4001, fieldName + "字段不能为null！");
	}
	
	public static FilterException fieldEmptyException(String fieldName) {
		return new FilterException(4002, fieldName + "字段不能为空！");
	}
	
	public static FilterException fieldNotPositiveException(String fieldName) {
		return new FilterException(4003, fieldName + "字段必须大于零！");
	}
	
	public static FilterException fieldNegativeException(String fieldName) {
		return new FilterException(4004, fieldName + "字段不能是负数！");
	}
	
	public static FilterException errorTimeUnit(TTimeUnit timeUnit) {
		return new FilterException(4005, "非natrue的时期形式不支持" + timeUnit);
	}
	
	public static FilterException nullReplacementExcpetion() {
		return new FilterException(5001, "当指定过滤行为为替换时，必须指定代替的内容！");
	}
}
