package com.zyx.filter.aop;

import java.lang.reflect.Proxy;

/**
 * 本类用于构造微信服务的动态代理对象
 * 
 * @author Ervin.zhang
 *
 */
public class ProxyFactory {

	/**
	 * 构造动态代理对象
	 * 
	 * @param target
	 *            被代理对象
	 * @param advice
	 *            方法通知
	 * @return 代理对象
	 */
	public static Object getProxy(Object target, Advice advice) {
		return Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(),
				target.getClass().getInterfaces(), (proxy, method, args) -> {
					advice.before(method, args);
					long startTime = System.currentTimeMillis();
					Object result = null;
					try {
						result = method.invoke(target, args);
					} catch (Exception e) {
						advice.tryCatch(e);
					}
					long endTime = System.currentTimeMillis();
					advice.after(method, args, endTime - startTime, result);
					return result;
				});
	}

}
