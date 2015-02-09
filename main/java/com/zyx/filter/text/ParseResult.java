package com.zyx.filter.text;

import com.zyx.filter.thrift.TFilterLevel;

/**
 * 文本解析结果
 * 
 * @author Ervin.zhang
 */
public class ParseResult {

	/**
	 * 过滤系统给出的过滤级别
	 */
	private TFilterLevel level;

	/**
	 * 过滤后的文本
	 */
	private String filteredText;


	/**
	 * @return the {@link #level}
	 */
	public TFilterLevel getLevel() {
		return level;
	}

	/**
	 * @param level the {@link #level} to set
	 */
	public void setLevel(TFilterLevel level) {
		this.level = level;
	}

	/**
	 * @return the {@link #filteredText}
	 */
	public String getFilteredText() {
		return filteredText;
	}

	/**
	 * @param filteredText
	 *            the {@link #filteredText} to set
	 */
	public void setFilteredText(String filteredText) {
		this.filteredText = filteredText;
	}

}
