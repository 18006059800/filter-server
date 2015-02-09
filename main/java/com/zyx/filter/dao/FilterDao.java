package com.zyx.filter.dao;

import java.util.List;

import com.zyx.filter.exception.FilterException;
import com.zyx.filter.thrift.TBusiness;
import com.zyx.filter.thrift.TFilter;
import com.zyx.filter.thrift.TFilterKeyword;

/**
 * 过滤系统基本数据的增删改查接口
 * @author Ervin.zhang
 */
public interface FilterDao {
	
	/**
	 * 增加关键字
	 */
	void addKeyword(TFilterKeyword keyword) throws FilterException;
	
	/**
	 * 删除关键字
	 */
	void deleteKeyword(String _id) throws FilterException;
	
	/**
	 * 更新关键字
	 */
	void updateKeyword(TFilterKeyword keyword) throws FilterException;
	
	/**
	 * 获取关键字
	 */
	TFilterKeyword getKeyword(String _id) throws FilterException;
	
	/**
	 * 增加一个业务
	 */
	void addBusiness(TBusiness business) throws FilterException;
	
	/**
	 * 删除一个业务
	 */
	void deleteBusiness(String _id) throws FilterException;
	
	/**
	 * 更新业务
	 */
	void updateBusiness(TBusiness business) throws FilterException;
	
	/**
	 * 获取一个业务
	 */
	TBusiness getBusiness(String _id) throws FilterException;
	
	/**
	 * 增加过滤器
	 */
	void addFilter(TFilter filter) throws FilterException;
	
	/**
	 * 删除过滤器
	 */
	void deleteFilter(String _id) throws FilterException;
	
	/**
	 * 更新过滤器
	 */
	void updateFilter(TFilter filter) throws FilterException;
	
	/**
	 * 获取过滤器
	 */
	TFilter getFilter(String _id) throws FilterException;
	
	/**
	 * 根据过滤器id查找相关的关键字
	 */
	List<TFilterKeyword> listFilterKeywords(String filterId);
	
	/**
	 * 查出所有的过滤器
	 */
	List<TFilter> listFilters();
	
	/**
	 * 查出所有的过滤器,关键字过滤器会包括所有的关键字
	 */
	List<TFilter> listCompleteFilters() throws FilterException;
	
}
