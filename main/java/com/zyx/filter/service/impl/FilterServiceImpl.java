package com.zyx.filter.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyx.filter.FilterContext;
import com.zyx.filter.aop.Advice;
import com.zyx.filter.aop.ProxyFactory;
import com.zyx.filter.dao.FilterDao;
import com.zyx.filter.exception.FilterException;
import com.zyx.filter.frequency.FrequencyFilterWrap;
import com.zyx.filter.text.FilterKeywordWrap;
import com.zyx.filter.text.TextFilterWrap;
import com.zyx.filter.thrift.FilterRequest;
import com.zyx.filter.thrift.FilterResponse;
import com.zyx.filter.thrift.FilterService;
import com.zyx.filter.thrift.TBusiness;
import com.zyx.filter.thrift.TFilter;
import com.zyx.filter.thrift.TFilterKeyword;
import com.zyx.filter.thrift.TFilterType;
import com.zyx.filter.thrift.TResponse;

public class FilterServiceImpl implements FilterService.Iface {

	private static final TResponse SUCCESS = new TResponse(0, "SUCCESS");

	private Logger logger = LoggerFactory.getLogger(FilterServiceImpl.class);

	private FilterDao dao;

	public FilterServiceImpl(FilterDao dao) {
		this.dao = (FilterDao) ProxyFactory.getProxy(dao, new Advice() {
			@Override
			public void before(Method method, Object[] args) {

			}

			@Override
			public void after(Method method, Object[] args, long millSeconds,
					Object retVal) {
				logger.debug("FilterDao.{}方法执行耗时：{} 毫秒", method.getName(),
						millSeconds);
			}

			@Override
			public void tryCatch(Exception e) throws Throwable {
				if (e instanceof InvocationTargetException) {
					throw ((InvocationTargetException) e).getTargetException();
				}
				throw e;
			}
		});
	}
	
	public TResponse getExceptionResponse(FilterException exception) {
		return new TResponse(exception.getCode(), exception.getMessage());
	}
	
	@Override
	public TResponse addKeyword(TFilterKeyword keyword) throws TException {
		try {
			FilterKeywordWrap.checkValid(keyword);
			dao.addKeyword(keyword);
			FilterContext.addKeyword(keyword);
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TResponse deleteKeyword(String _id) throws TException {
		try {
			dao.deleteKeyword(_id);
			FilterContext.deleteKeyword(_id);
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TResponse updateKeyword(TFilterKeyword keyword) throws TException {
		try {
			FilterKeywordWrap.checkValid(keyword);
			dao.updateKeyword(keyword);
			// TODO 
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TFilterKeyword getKeyword(String _id) throws TException {
		try {
			return dao.getKeyword(_id);
		} catch (FilterException e) {
			return null;
		}
	}
	
	private void checkFilter(TFilter filter) throws FilterException {
		if (filter == null) {
			throw FilterException.fieldNullException("filter");
		}
		if (filter.type == null) {
			throw FilterException.fieldNullException("filter.type");
		}
		if (filter.type == TFilterType.FREQUENCY) {
			FrequencyFilterWrap.checkValid(filter);
		} else {
			TextFilterWrap.checkValid(filter);
		}
	}

	@Override
	public TResponse addFilter(TFilter filter) throws TException {
		try {
			checkFilter(filter);
			dao.addFilter(filter);
			FilterContext.addFilter(filter);
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TResponse deleteFilter(String _id) throws TException {
		try {
			dao.deleteFilter(_id);
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TResponse updateFilter(TFilter filter) throws TException {
		try {
			checkFilter(filter);
			dao.updateFilter(filter);
			// TODO
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TFilter getFilter(String _id) throws TException {
		try {
			return dao.getFilter(_id);
		} catch (FilterException e) {
			return null;
		}
	}

	@Override
	public TResponse addBusiness(TBusiness business) throws TException {
		try {
			dao.addBusiness(business);
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TResponse deleteBusiness(String _id) throws TException {
		try {
			dao.deleteBusiness(_id);
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TResponse updateBusiness(TBusiness business) throws TException {
		try {
			dao.updateBusiness(business);
		} catch (FilterException e) {
			return getExceptionResponse(e);
		}
		return SUCCESS;
	}

	@Override
	public TBusiness getBusiness(String _id) throws TException {
		try {
			return dao.getBusiness(_id);
		} catch (FilterException e) {
			return null;
		}
	}

	@Override
	public List<TFilter> listFilters() throws TException {
		return dao.listFilters();
	}
	
	@Override
	public FilterResponse filter(FilterRequest req) throws TException {
		FilterProcessor processor = new FilterProcessor();
		return processor.process(req);
	}
	
}
