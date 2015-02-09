package com.zyx.filter.frequency;

import java.util.HashMap;
import java.util.Map;

import com.zyx.filter.Filter;
import com.zyx.filter.exception.FilterException;
import com.zyx.filter.frequency.FrequencyFilterWrap;
import com.zyx.filter.thrift.TTimeUnit;

public class FrequencyFilterTest {
	public static void main(String[] args) throws InterruptedException,
			FilterException {
		// Cache<Object, Object> cache = CacheBuilder.newBuilder()
		// .expireAfterWrite(3, TimeUnit.SECONDS.getJavaTimeUnit())
		// .build();
		// String str = "fdsf";
		// cache.put("xx", str);
		// Thread.sleep(2000);
		// cache.put("xxx", str);
		// Thread.sleep(2000);
		// System.out.println(cache.getIfPresent("xx") == null);
		Filter filter = new FrequencyFilterWrap("username", 1, TTimeUnit.MINUTES, 5, true);
		Map<String, String> req = new HashMap<String, String>();
		req.put("username", "ervin");
		for (int i = 0; i < 10000; i++) {
			System.out.println(filter.doFilter(req, null));
			Thread.sleep(1000);
		}
	}
}
