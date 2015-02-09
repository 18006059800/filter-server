package com.zyx.filter.text;

import com.to8to.commons.utils.StringUtil;
import com.to8to.filter.exception.FilterException;
import com.to8to.filter.thrift.TBehaviorType;
import com.to8to.filter.thrift.TFilterKeyword;
import com.to8to.filter.thrift.TFilterLevel;

/**
 * <p>
 * 过滤关键字包装对象，负责异常处理，行为构建
 * </p>
 * 
 * @author Ervin.zhang
 */
public class FilterKeywordWrap {

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 过滤行为类型
	 */
	private TBehaviorType behaviorType;

	/**
	 * 关键字过滤级别
	 */
	private TFilterLevel filterLevel;

	/**
	 * 如果{@link #behaviorType} == REPLACE，则要指定替换的内容
	 */
	private String replacement;

	/**
	 * 关键词出现的次数
	 */
	private int appearTimes;

	/**
	 * 关键词出现的次数限制
	 */
	private int appearTimesLimit;

	public FilterKeywordWrap(TFilterKeyword filterKeyword)
			throws FilterException {
		this.keyword = filterKeyword.keyword;
		this.behaviorType = filterKeyword.behavior_type;
		this.replacement = filterKeyword.replacement;
		this.appearTimesLimit = filterKeyword.appear_times_limit;
		this.filterLevel = filterKeyword.getLevel();
		checkInput();
	}
	
	public static void checkValid(TFilterKeyword filterKeyword) throws FilterException {
		new FilterKeywordWrap(filterKeyword);
	}

	/**
	 * 对输入的字段进行校验，因为只在构造的时候进行校验，所以不提供相关字段的setter方法
	 */
	public void checkInput() throws FilterException {
		if (StringUtil.isEmpty(keyword)) {
			throw FilterException.fieldEmptyException("keyword");
		}
		if (behaviorType == null) {
			throw FilterException.fieldNullException("behaviorType");
		}
		if (behaviorType == TBehaviorType.REPLACE && replacement == null) {
			throw FilterException.nullReplacementExcpetion();
		}
		if (filterLevel == null) {
			throw FilterException.fieldNullException("filterLevel");
		}
		if (appearTimesLimit < 0) {
			throw FilterException.fieldNegativeException("appearTimesLimit");
		}
	}

	/**
	 * 根据{@code FilterKeyword}的信息，生成相应的行为
	 * 
	 * @return 实现了{@code Behavior}接口的类，该类描述了文本遇到关键字的具体行为
	 */
	public Behavior getBehavior() {
		switch (behaviorType) {
		case NONE:
			return (text) -> limit();
		case REMOVE:
			return this::remove;
		case REPLACE:
			return this::replace;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * 当文本中出现了keyword,从文本中删除keyword
	 * 
	 * @param text
	 *            被过滤文本
	 * @return 过滤行为类型
	 */
	private TFilterLevel remove(StringBuilder text) {
		text.delete(text.length() - keyword.length(), text.length());
		return limit();
	}

	/**
	 * 当文本中出现了keyword,替换keyword
	 * 
	 * @param text
	 *            被过滤文本
	 * @return 过滤行为类型
	 */
	private TFilterLevel replace(StringBuilder text) {
		text.replace(text.length() - keyword.length(), text.length(),
				replacement);
		return limit();
	}

	/**
	 * 如果启用了关键词出现次数统计 那么判断是否已达到关键词出现次数限制，没果没有达到，则返回ADJUST 否则激活filterLevel
	 * 
	 * @return 已达到返回true, 否则false
	 */
	private TFilterLevel limit() {
		if (appearTimesLimit > 0) {
			appearTimes++;
			if (appearTimes < appearTimesLimit) {
				return TFilterLevel.ADJUST;
			}
		}
		return filterLevel;
	}

	/**
	 * @return the {@link #keyword}
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @return the {@link #behaviorType}
	 */
	public TBehaviorType getBehaviorType() {
		return behaviorType;
	}

	/**
	 * @return the {@link #replacement}
	 */
	public String getReplacement() {
		return replacement;
	}

}
