package com.zyx.filter.dao.impl;

import static com.zyx.filter.dao.Tables.BUSINESS;
import static com.zyx.filter.dao.Tables.FILTER;
import static com.zyx.filter.dao.Tables.KEYWORD;

import java.util.List;

import org.apache.thrift.TBase;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.to8to.commons.mongo.MongoClientBase;
import com.to8to.commons.utils.ThriftUtil;
import com.to8to.commons.utils.ThriftUtil.NullBehavior;
import com.zyx.filter.dao.FilterDao;
import com.zyx.filter.exception.FilterException;
import com.zyx.filter.text.FilterKeywordWrap;
import com.zyx.filter.thrift.TBusiness;
import com.zyx.filter.thrift.TFilter;
import com.zyx.filter.thrift.TFilterKeyword;
import com.zyx.filter.thrift.TFilterType;
import com.zyx.filter.thrift.TTextFilter;

/**
 * @author Ervin.zhang
 */
public class FilterDaoImpl implements FilterDao {

	private MongoClientBase client;

	public FilterDaoImpl(MongoClientBase client) {
		this.client = client;
	}

	/**
	 * 保存thrift对象
	 * 
	 * @param tableName 表名
	 * @param t 需要保存的thrift对象
	 */
	public <T extends TBase<?, ?>> void save(String tableName, T t) throws FilterException {
		if (t == null) {
			throw FilterException.thrirftObjectNullException();
		}
		try {
			// 本系统所有数据保存时，一略忽略null
			DBObject dbObject = ThriftUtil.thriftObject2DBObject(t, NullBehavior.IGNORE);
			WriteResult wr = client.save(tableName, dbObject);
			if (wr == null) {
				throw FilterException.updateFailed("save");
			}
		} catch (FilterException e) {
			throw e;
		} catch (Exception e) {
			throw new FilterException(e);
		}
	}

	/**
	 * 从指定表中删除指定id的数据
	 * 
	 * @param tableName 表名
	 * @param _id 被删除数据的id
	 */
	public void deleteById(String tableName, String _id) throws FilterException {
		if (!ObjectId.isValid(_id)) {
			throw FilterException.invalidObjectId();
		}
		try {
			WriteResult wr = client.remove(tableName, _id);
			if (wr == null) {
				throw FilterException.updateFailed("delete");
			}
		} catch (FilterException e) {
			throw e;
		} catch (Exception e) {
			throw new FilterException(e);
		}
	}

	/**
	 * 更新对象
	 * 
	 * @param tableName 表名
	 * @param t 新对象
	 * @param _id 被更新对象的id
	 * @param upsert 如果指定id不存在，是否插入新对象
	 */
	public <T extends TBase<?, ?>> void updateById(String tableName, T t, String _id, boolean upsert)
			throws FilterException {

		if (!ObjectId.isValid(_id)) {
			throw FilterException.invalidObjectId();
		}

		if (t == null) {
			throw FilterException.thrirftObjectNullException();
		}

		try {
			DBObject dbObject = ThriftUtil.thriftObject2DBObject(t);
			DBObject cond = new BasicDBObject("_id", _id);
			DBObject oper = new BasicDBObject("$set", dbObject);
			WriteResult wr = client.update(tableName, cond, oper, upsert, false);
			if (wr == null) {
				throw FilterException.updateFailed("update");
			}
		} catch (FilterException e) {
			throw e;
		} catch (Exception e) {
			throw new FilterException(e);
		}
	}

	/**
	 * 更新对象，upsert=false
	 */
	public <T extends TBase<?, ?>> void updateById(String tableName, T t, String _id) throws FilterException {
		updateById(tableName, t, _id, false);
	}

	/**
	 * 根据id找对象
	 * 
	 * @param tableName 表名
	 * @param clazz 目标对象的class
	 * @param _id 对象id
	 * @return 查找的对象
	 */
	public <T extends TBase<?, ?>> T getById(String tableName, Class<T> clazz, String _id)
			throws FilterException {

		if (!ObjectId.isValid(_id)) {
			throw FilterException.invalidObjectId();
		}

		DBObject dbObject = client.findById(tableName, _id);

		if (dbObject == null) {
			throw FilterException.idNotFoundException(_id);
		}
		try {
			T t = ThriftUtil.dbObject2ThriftObject(dbObject, clazz);
			return t;
		} catch (Exception e) {
			throw new FilterException(e);
		}
	}

	public String getObjectId() {
		return new ObjectId().toString();
	}

	@Override
	public void addKeyword(TFilterKeyword keyword) throws FilterException {
		keyword.set_id(getObjectId());
		save(KEYWORD, keyword);
	}

	@Override
	public void deleteKeyword(String _id) throws FilterException {
		deleteById(KEYWORD, _id);
	}

	@Override
	public void updateKeyword(TFilterKeyword keyword) throws FilterException {
		updateById(KEYWORD, keyword, keyword.get_id());
	}

	@Override
	public TFilterKeyword getKeyword(String _id) throws FilterException {
		return getById(KEYWORD, TFilterKeyword.class, _id);
	}

	@Override
	public void addBusiness(TBusiness business) throws FilterException {
		business.set_id(getObjectId());
		save(BUSINESS, business);
	}

	@Override
	public void deleteBusiness(String _id) throws FilterException {
		deleteById(BUSINESS, _id);
	}

	@Override
	public void updateBusiness(TBusiness business) throws FilterException {
		updateById(BUSINESS, business, business.get_id());
	}

	@Override
	public TBusiness getBusiness(String _id) throws FilterException {
		return getById(BUSINESS, TBusiness.class, _id);
	}

	@Override
	public void addFilter(TFilter filter) throws FilterException {
		filter.set_id(getObjectId());
		if (filter.getType() == TFilterType.TEXT && filter.textFilter.filter_keywords != null) {
			for (TFilterKeyword keyword : filter.textFilter.filter_keywords) {
				FilterKeywordWrap.checkValid(keyword);
				keyword.setFilter_id(filter.get_id());
				addKeyword(keyword);
			}
		}
		filter.textFilter = null;
		save(FILTER, filter);
	}

	@Override
	public void deleteFilter(String _id) throws FilterException {
		deleteById(FILTER, _id);
	}

	@Override
	public void updateFilter(TFilter filter) throws FilterException {
		updateById(FILTER, filter, filter.get_id());
	}

	@Override
	public TFilter getFilter(String _id) throws FilterException {
		TFilter filter = getById(FILTER, TFilter.class, _id);
		// 如果是文本过滤器，则需要从关键字表里找到需要过滤的关键字
		if (filter.getType() == TFilterType.TEXT) {
			String filterId = filter.get_id();
			filter.setTextFilter(new TTextFilter().setFilter_keywords(listFilterKeywords(filterId)));
		}
		return filter;
	}

	@Override
	public List<TFilterKeyword> listFilterKeywords(String filterId) {
		DBObject cond = new BasicDBObject("filter_id", filterId);
		DBCursor cursor = client.cursor(KEYWORD, cond, null, null, 0, 0);
		return ThriftUtil.listCursor(cursor, TFilterKeyword.class);
	}

	@Override
	public List<TFilter> listFilters() {
		DBCursor cursor = client.cursor(FILTER, null, null, null, 0, 0);
		return ThriftUtil.listCursor(cursor, TFilter.class);
	}

	@Override
	public List<TFilter> listCompleteFilters() throws FilterException {
		List<TFilter> filters = listFilters();
		if (filters != null) {
			for (TFilter filter : filters) {
				TBusiness biz = getBusiness(filter.getBusiness_id());
				filter.setBusiness_name(biz.getName());
				if (filter.getType() == TFilterType.TEXT) {
					filter.setTextFilter(new TTextFilter().setFilter_keywords(listFilterKeywords(filter
							.get_id())));
				}
			}
		}
		return filters;
	}

}
