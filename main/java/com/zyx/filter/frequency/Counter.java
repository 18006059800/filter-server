package com.zyx.filter.frequency;

import com.zyx.filter.thrift.TTimeUnit;
import com.zyx.filter.util.TimeUtil;

/**
 * 维护一个时间单元内的过滤请求时间
 * 
 * @author Ervin.zhang
 */
public class Counter {

	/**
	 * 频度
	 */
	private int times;

	/**
	 * 
	 */
	private long duration;

	/**
	 * 时间单位
	 */
	private TTimeUnit timeUnit;

	/**
	 * 上次计数的时间戳
	 */
	private long lastCountTimestamp;

	/**
	 * 一个时间元元内计数的次数
	 */
	private int countTimes;

	public Counter() {
		super();
	}

	public Counter(int times, long duration, TTimeUnit timeUnit) {
		this.duration = duration;
		this.times = times;
		this.timeUnit = timeUnit;
	}

	/**
	 * @return the {@link #times}
	 */
	public int getTimes() {
		return times;
	}

	/**
	 * @param times
	 *            the {@link #times} to set
	 */
	public void setTimes(int times) {
		this.times = times;
	}

	/**
	 * @return the {@link #timeUnit}
	 */
	public TTimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * @param timeUnit
	 *            the {@link #timeUnit} to set
	 */
	public void setTimeUnit(TTimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	/**
	 * @return the {@link #duration}
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the {@link #duration} to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * 对请求进行计数
	 * 
	 * @return 如果计数成功返回true,否则返回false
	 */
	public boolean count() {
		long currTimestamp = System.currentTimeMillis();

		if (TimeUtil.inSameTimeUnits(lastCountTimestamp, currTimestamp,
				timeUnit, duration)) {
			if (countTimes >= times) {
				// 和上次计数在同一时间单元，并且同一时间单元计数次数已经达到了次数限制
				// 返回失败
				return false;
			}
		} else {
			// 和上次计算不在同一时间单元，已经切换到下一时间单元
			countTimes = 0;
		}
		lastCountTimestamp = currTimestamp;
		countTimes++;
		return true;
	}
}
