package com.zyx.filter.aop;

import java.lang.reflect.Method;

public interface Advice {
	
	void before(Method method, Object[] args);

	void after(Method method, Object[] args, long millSeconds, Object retVal);
	
	void tryCatch(Exception e) throws Throwable;
}
