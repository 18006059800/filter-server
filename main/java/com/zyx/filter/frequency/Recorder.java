package com.zyx.filter.frequency;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Recorder维护一个定长的时间队列，队列的长度为一段时间内某个行为的次数限制
 * @author Ervin.zhang
 */
public class Recorder {

	/**
	 * 频度
	 */
	private int times;
	
	/**
	 * 时间范围
	 */
	private long durationMillis;
	
	/**
	 * 时间队列，维护最新{@link #times}个行为的时间
	 */
	private Queue<Long> timestampQueue;
	
	public Recorder(int times, long durationMillis) {
		this.times = times;
		this.durationMillis = durationMillis;
		timestampQueue = new LinkedList<Long>();
	}

 	/**
	 * 将行为的时间记录到队列中
	 * @return 如果超过频度限制返回false，否则返回true
	 */
	public boolean record() {
		long currTimestamp = System.currentTimeMillis();
		// 队列已满
		if (timestampQueue.size() >= times) {
			long diffMillis = currTimestamp - timestampQueue.peek();
			// 队列已满 并且 列头时间小于当前时间，说明在时间范围内，超过了频率限制
			if (diffMillis <= durationMillis) {
				return false;
			} else {
				// 队列已满，但列头时间大于当前时间，说明列头已不在限制时间范围内，可以清除，并把行为的时间记录到队列
				timestampQueue.poll();
				timestampQueue.offer(currTimestamp);
			}
		} else {
			// 队列未满，直接把行为的时间记录到队列
			timestampQueue.offer(currTimestamp);
		}
		return true;
	}
}
