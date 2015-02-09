package com.zyx.filter.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.zyx.filter.thrift.FilterRequest;
import com.zyx.filter.thrift.FilterService;
import com.zyx.filter.thrift.TBehaviorType;
import com.zyx.filter.thrift.TBusiness;
import com.zyx.filter.thrift.TFilter;
import com.zyx.filter.thrift.TFilterKeyword;
import com.zyx.filter.thrift.TFilterType;
import com.zyx.filter.thrift.TFrequencyFilter;
import com.zyx.filter.thrift.TTextFilter;
import com.zyx.filter.thrift.TTimeUnit;

public class FilterClient {
	TTransport transport;
	TProtocol protocol;
	FilterService.Client client;

	public boolean open(String host, int port) {
		transport = new TSocket(host, port);
		transport = new TFramedTransport(transport);
		try {
			transport.open();
		} catch (TTransportException e) {
			return false;
		}

		// protocol = new TBinaryProtocol(transport, true, true);
		protocol = new TBinaryProtocol(transport);

		client = new FilterService.Client(protocol);

		return true;
	}

	public boolean close() {
		transport.close();
		return true;
	}

	public void testAddKeyword() throws TException {
		TFilterKeyword keyword = new TFilterKeyword("色情")
				.setBehavior_type(TBehaviorType.REPLACE).setReplacement("###")
				.setFilter_id("5444d8aa7c1f9419e73d6b22");
		System.out.println(client.addKeyword(keyword));
	}

	public TFilterKeyword getFilterKeyword() throws TException {
		return client.getKeyword("544483787c1f84947efeebfd");
	}

	public void testUpdateKeyword() throws TException {
		TFilterKeyword k = getFilterKeyword();
		System.out.println(k);
		k.setBehavior_type(TBehaviorType.REMOVE);
		k.setReplacement(null);
		System.out.println(client.updateKeyword(k));
	}

	public void testDeleteKeyword() throws TException {
		System.out.println(client.deleteKeyword("5445f1e77c1f2737b5f6d97e"));
	}

	public void testAddTextFilter() throws TException {
		TTextFilter textFilter = new TTextFilter();
		TFilterKeyword fk = new TFilterKeyword("色情").setBehavior_type(
				TBehaviorType.REPLACE).setReplacement("**");
		List<TFilterKeyword> fks = new ArrayList<TFilterKeyword>();
		fks.add(fk);
		textFilter.setFilter_keywords(fks);
		TFilter filter = new TFilter().setName("global").setField("content")
				.setBusiness_id("5444a4a47c1f94896189d5e1")
				.setType(TFilterType.TEXT).setTextFilter(textFilter);
		System.out.println(client.addFilter(filter));
	}

	public void testAddFreqFilter() throws TException {
		TFrequencyFilter freqFilter = new TFrequencyFilter();
		freqFilter.setDuration(1).setTime_unit(TTimeUnit.MINUTES).setTimes(3);
		TFilter filter = new TFilter().setName("global").setField("username")
				.setBusiness_id("5444a4a47c1f94896189d5e1")
				.setType(TFilterType.FREQUENCY).setFrequencyFilter(freqFilter);
		System.out.println(client.addFilter(filter));
	}

	public void testGetFilter() throws TException {
		TFilter filter = client.getFilter("5444ba577c1f39df8136e32b");
		System.out.println(filter);
	}

	public void testAddBiz() throws TException {
		TBusiness biz = new TBusiness("System");
		System.out.println(client.addBusiness(biz));
	}

	public void testListFilters() throws TException {
		System.out.println(client.listFilters());
	}
	
	public void testFilter() throws TException {
		Map<String, String> filterField = new HashMap<String, String>();
		filterField.put("content", "what 色情 the fuck, 我草");
		filterField.put("username", "ervin2");
		FilterRequest req = new FilterRequest(filterField);
		System.out.println(client.filter(req));
	}

	public static void main(String[] args) throws TException {
		FilterClient c = new FilterClient();
		try {
			c.open("192.168.3.233", 1111);
			c.testFilter();
//			c.testDeleteKeyword();
			c.testFilter();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
	}
}
