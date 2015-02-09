package com.zyx.filter.util;

import java.util.Calendar;

import com.zyx.filter.thrift.TTimeUnit;

/**
 * 处理时间的工具类
 * 
 * @author Ervin.zhang
 */
public class TimeUtil {

	/**
	 * 判断两个时间戳是不是在多个时间单位内
	 */
	public static boolean inSameTimeUnits(long before, long after,
			TTimeUnit timeUnit, long units) {
		long unitMillis = 0;
		if (timeUnit == TTimeUnit.MONTHS) {
			unitMillis = 31 * TimeUnit.DAYS.toMillis(1L);
		} else if (timeUnit == TTimeUnit.YEARS) {
			unitMillis = 366 * TimeUnit.DAYS.toMillis(1L);
		} else {
			unitMillis = getMillis(timeUnit);
		}

		// 如果两个时间的差距大于一个时间单元，那这两个时间一定不在同一时间单元
		if (after - before > units * unitMillis) {
			return false;
		}

		Calendar calendar = Calendar.getInstance();

		// 将timeUnit转换成Calendar中的时间标签
		int timeTag = transferTimeUnit(timeUnit);
		calendar.setTimeInMillis(before);
		int beforeUnit = calendar.get(timeTag);
		calendar.setTimeInMillis(after);
		int afterUnit = calendar.get(timeTag);

		if (afterUnit - beforeUnit < units) {
			return true;
		}

		return false;
	}

	/**
	 * 判断两个时间戳是不是在同一个时间单位内，比如，判断是否是在同一天
	 */
	public static boolean inSameTimeUnit(long before, long after,
			TTimeUnit timeUnit) {
		return inSameTimeUnits(before, after, timeUnit, 1);
	}

	/**
	 * 将timeUnit转换成Calendar中的时间标签
	 */
	public static int transferTimeUnit(TTimeUnit timeUnit) {
		switch (timeUnit) {
		case MILLISECONDS:
			return Calendar.MILLISECOND;
		case SECONDS:
			return Calendar.SECOND;
		case MINUTES:
			return Calendar.MINUTE;
		case HOURS:
			return Calendar.HOUR;
		case DAYS:
			return Calendar.DAY_OF_MONTH;
		case WEEKS:
			return Calendar.WEEK_OF_YEAR;
		case MONTHS:
			return Calendar.MONTH;
		case YEARS:
			return Calendar.YEAR;
		default:
			throw new IllegalStateException();
		}
	}

	public static long getMillis(TTimeUnit timeUnit, long units) {
		return getMillis(timeUnit) * units;
	}
	
    
    public static long[] millis= new long[]{1L, 1000L, 60000L, 3600000L, 86400000L, 604800000L};

	public static long getMillis(TTimeUnit timeUnit) {
		int ordinal = timeUnit.ordinal();
		if (ordinal > TimeUnit.WEEKS.ordinal()) {
			throw new IllegalStateException("不能计算month/year的毫秒数！");
		}
		return millis[ordinal];
	}
	
	public static java.util.concurrent.TimeUnit transToJavaTimeUtil(TTimeUnit timeUnit) {
		return java.util.concurrent.TimeUnit.valueOf(timeUnit.name());
	}
	
	public static void main(String[] args) {
		System.out.println(TTimeUnit.HOURS.getValue());
		System.out.println(getMillis(TTimeUnit.WEEKS));
	}
}
