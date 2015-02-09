package com.zyx.filter.server;

import static com.zyx.filter.FilterContext.config;
import static com.zyx.filter.FilterContext.createFilterMap;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.to8to.commons.mongo.MongoClientBase;
import com.to8to.commons.utils.Config;
import com.to8to.kitt.SimpleThriftBizHandler;
import com.to8to.kitt.ThriftServer;
import com.to8to.kitt.ThriftServerChannelInitializer;
import com.zyx.filter.aop.Advice;
import com.zyx.filter.aop.ProxyFactory;
import com.zyx.filter.dao.FilterDao;
import com.zyx.filter.dao.impl.FilterDaoImpl;
import com.zyx.filter.exception.FilterException;
import com.zyx.filter.service.impl.FilterServiceImpl;
import com.zyx.filter.thrift.FilterService;
import com.zyx.filter.thrift.TFilter;

/**
 * 过滤服务启动入口 
 * @author Ervin.zhang
 */
public class FilterServer {
	
	public static Logger logger = LoggerFactory.getLogger(FilterServer.class);
	private FilterDao dao;
	private String inst;

	public FilterServer(String inst) throws FilterException {
		this.inst = inst;
		init();

	}

	private void init() throws FilterException {
		config = new Config("filter", inst);
		logger.info(config.toString());

		String host = config.get("db.host");

		String database = config.get("db.database");

		logger.info("连接mongo:{}/{}", host, database);

		MongoClientBase client = new MongoClientBase(host, null, null, database);

		// 创建dao
		logger.info("创建WeixinDao.......");
		dao = new FilterDaoImpl(client);
		
		// 初始化过滤器
		createFilters();
	}
	
	private void createFilters() throws FilterException {
		List<TFilter> filters = dao.listCompleteFilters();
		createFilterMap(filters);
	}
	
	/**
	 * 启动服务
	 */
	public void startServer() {
		String bindIp = config.get("thrift.bindIp");
		logger.info("weixin服务绑定Ip：{}", bindIp);

		int bindPort = config.getInt("thrift.bindPort", 1111);
		
		logger.info("weixin服务绑定端口：{}", bindPort);

		FilterService.Iface impl = new FilterServiceImpl(dao);
		
		// 使用动态代理，对service的方法进行监控
		FilterService.Iface iface = (FilterService.Iface) ProxyFactory.getProxy(impl, new Advice() {
			
			public Logger adviceLog = LoggerFactory.getLogger(FilterServer.class);
			
			@Override
			public void before(Method method, Object[] args) {
				adviceLog.debug("进入{}.{}方法，参数列表：{}", impl.getClass().getName(), method.getName(), args);
			}

			@Override
			public void after(Method method, Object[] args, long millSeconds,
					Object retVal) {
				logger.debug(" {} 方法执行完毕, 返回值：{}, 方法执行耗时：{} 毫秒", method.getName(), retVal, millSeconds);
				if (millSeconds > 100) {
					logger.warn("{}方法执行耗时超过100毫秒，耗时：{}毫秒,参数列表：{}", method.getName(), millSeconds, args);
				}
			}

			@Override
			public void tryCatch(Exception e) throws Exception {
				throw e;
			}
			
		});
		
		FilterService.Processor<FilterService.Iface> processor = new FilterService.Processor<FilterService.Iface>(
				iface);

		ThriftServerChannelInitializer channelInitilizer = new ThriftServerChannelInitializer(
				FilterService.class, new TBinaryProtocol.Factory(), false,
				new SimpleThriftBizHandler(iface, processor));
		ThriftServer s = new ThriftServer();
		logger.info("启动过滤服务...");
		s.start(channelInitilizer, bindIp, bindPort);
	
	}
	
	public static void main(String[] args) {
		String inst = "1";
    	if(args.length > 0)
    		inst = args[0];
    	FilterServer server = null;
		try {
			server = new FilterServer(inst);
		} catch (FilterException e) {
			logger.error("启动失败！！！！{}", e.getMessage());
			e.printStackTrace();
		}
    	server.startServer();
	}
}
