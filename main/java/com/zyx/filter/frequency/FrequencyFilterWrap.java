package com.zyx.filter.frequency;

import static com.zyx.filter.thrift.TJudgeResult.FORBIDDEN;
import static com.zyx.filter.thrift.TJudgeResult.OK;

import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zyx.filter.AbstractFilter;
import com.zyx.filter.Content;
import com.zyx.filter.exception.FilterException;
import com.zyx.filter.thrift.TFilter;
import com.zyx.filter.thrift.TJudgeResult;
import com.zyx.filter.thrift.TTimeUnit;
import com.zyx.filter.util.TimeUtil;

/**
 * 频率过滤器
 * 
 * @author Ervin.zhang
 */
public class FrequencyFilterWrap extends AbstractFilter {

	/**
	 * 延时缓存
	 */
	private Cache<Object, Object> delayCache;

	/**
	 * 时限
	 */
	private long duration;

	/**
	 * 时间单位, 默认为“秒”
	 */
	private TTimeUnit timeUnit = TTimeUnit.SECONDS;

	/**
	 * 时限（时间单位为毫秒）
	 */
	private long durationMillis;

	/**
	 * 次数限制
	 */
	private int times;

	/**
	 * 是否按自然期处理，比如时间单位为“天”时，isNature=true 表示按自然天过滤，每天都从0点到24点算 isNatrue=false
	 * 表示从发表内容一刻算法，到第二天同一时候
	 */
	private boolean isNature = false;

	public FrequencyFilterWrap(String field, long duration, TTimeUnit timeUnit,
			int times) throws FilterException {
		this(field, duration, timeUnit, times, false);
	}

	public FrequencyFilterWrap(String field, long duration, TTimeUnit timeUnit,
			int times, boolean isNature) throws FilterException {

		this.field = field;
		this.duration = duration;
		this.timeUnit = timeUnit;
		this.times = times;

		this.isNature = isNature;

		// 输入校验
		checkInput();

		if (!isNature) {
			// 得到以毫秒为单位的时间
			durationMillis = TimeUtil.getMillis(timeUnit);
		}

		createDelayCache();

	}
	
	public FrequencyFilterWrap(TFilter filter) throws FilterException {
		if (filter.frequencyFilter == null) {
			throw FilterException.fieldNullException("filter.frequencyFilter");
		}
		this.name = filter.name;
		this.field = filter.field;
		this.duration = filter.frequencyFilter.duration;
		this.timeUnit = filter.frequencyFilter.time_unit;
		this.times = filter.frequencyFilter.times;
		this.isNature = filter.frequencyFilter.is_natrue;
		checkInput();
		
		if (!isNature) {
			// 得到以毫秒为单位的时间
			durationMillis = TimeUtil.getMillis(timeUnit);
		}
		
		createDelayCache();
	}
	
	public static void checkValid(TFilter filter) throws FilterException {
		new FrequencyFilterWrap(filter);
	}

	@Override
	public void checkInput() throws FilterException {

		super.checkInput();

		if (duration <= 0) {
			throw FilterException.fieldNotPositiveException("duration");
		}
		if (times <= 0) {
			throw FilterException.fieldNotPositiveException("times");
		}
		if (timeUnit == null) {
			throw FilterException.fieldNullException("timeUnit");
		}
		if (!isNature) {
			if (timeUnit == TTimeUnit.YEARS || timeUnit == TTimeUnit.MONTHS) {
				throw FilterException.errorTimeUnit(timeUnit);
			}
		}

	}

	/**
	 * 创建延时缓存
	 */
	public void createDelayCache() {
		if (timeUnit == TTimeUnit.WEEKS) {
			delayCache = CacheBuilder
					.newBuilder()
					.expireAfterWrite(duration * 7,
							java.util.concurrent.TimeUnit.DAYS).build();
		} else if (timeUnit == TTimeUnit.MONTHS) {
			delayCache = CacheBuilder
					.newBuilder()
					.expireAfterWrite(duration * 31,
							java.util.concurrent.TimeUnit.DAYS).build();
		} else if (timeUnit == TTimeUnit.YEARS) {
			delayCache = CacheBuilder
					.newBuilder()
					.expireAfterWrite(duration * 366,
							java.util.concurrent.TimeUnit.DAYS).build();
		} else {
			delayCache = CacheBuilder.newBuilder()
					.expireAfterWrite(duration, TimeUtil.transToJavaTimeUtil(timeUnit))
					.build();
		}
	}

	/**
	 * 对对象出现的频度进行验证
	 * 
	 * @param filterObject
	 *            需要进行频度过滤的对象
	 * @return 达到频度限制上限则返回false,否则返回true
	 */
	@Override
	public TJudgeResult filter(Object filterObject, Map<String, String> resp) {
		Object value = delayCache.getIfPresent(filterObject);
		if (isNature) {
			Counter counter = null;
			if (value == null) {
				counter = new Counter(times, duration, timeUnit);
			} else {
				counter = (Counter) value;
			}
			delayCache.put(filterObject, counter);
			return resp(counter.count(), resp, filterObject);
		} else {
			Recorder recorder = null;
			if (value == null) {
				recorder = new Recorder(times, durationMillis);
			} else {
				recorder = (Recorder) value;
			}
			// 重新刷新该键的时间，以免失效
			delayCache.put(filterObject, recorder);
			return resp(recorder.record(), resp, filterObject);
		}
	}
	
	private TJudgeResult resp(boolean passed, Map<String, String> resp, Object val) {
		if (passed) {
			return OK;
		} else {
			resp.put(Content.FORBIDDEN_REASON, field + ":" + val + "出现次数受限！");
			return FORBIDDEN;
		}
	}
	
	/**
	 * @return the {@link #duration}
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @return the {@link #timeUnit}
	 */
	public TTimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * @return the {@link #times}
	 */
	public int getTimes() {
		return times;
	}

	/**
	 * @return the {@link #isNature}
	 */
	public boolean isNature() {
		return isNature;
	}

}
