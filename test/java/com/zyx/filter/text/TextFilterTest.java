package com.zyx.filter.text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.zyx.filter.exception.FilterException;
import com.zyx.filter.thrift.TBehaviorType;
import com.zyx.filter.thrift.TFilterKeyword;
import com.zyx.filter.thrift.TFilterLevel;
import com.zyx.trie.Trie;

public class TextFilterTest {

	public static void main(String[] args) throws FilterException {
		List<TFilterKeyword> filterKeywords = new ArrayList<TFilterKeyword>();
//		filterKeywords.add(new TFilterKeyword("fuck").setAppear_times_limit(2)
//				.setLevel(TFilterLevel.FORBIDDEN)
//				.setBehavior_type(TBehaviorType.REPLACE)
//				.setReplacement("{what}"));
		filterKeywords.add(new TFilterKeyword("nima").setBehavior_type(
				TBehaviorType.REPLACE).setReplacement("**").setLevel(TFilterLevel.NOTICE));
//		filterKeywords.add(new TFilterKeyword("草")
//				.setBehavior_type(TBehaviorType.REPLACE).setReplacement("??")
//				.setLevel(TFilterLevel.CHECK));
		// filterKeywords.add(new FilterKeyword("禁止", BehaviorType.FORBIDDEN));
//		TextFilterWrap filter = new TextFilterWrap("content", filterKeywords);
//		Map<String, String> req = new HashMap<String, String>();
//		req.put("content",
//				"wocao nima 我草， what the fuck hahafuckfuckffuckkk  这个要禁止的！");
//		Map<String, String> resp = new HashMap<String, String>();
//		TJudgeResult jr = filter.doFilter(req, resp);
//		System.out.println(resp.get("content"));
//		System.out.println(jr);
//		ThriftUtil2.listCursor(null);
	}

	public String simpleReplace(String text, List<String> replacements) {
		long start = System.currentTimeMillis();
		for (String r : replacements) {
			text = text.replaceAll(r, "**");
		}
		long end = System.currentTimeMillis();
		System.out.println("耗时：" + (end - start) + " 毫秒");
		return text;

	}

	public String filterReplace(String text, List<TFilterKeyword> replacements)
			throws FilterException {
		long start = System.currentTimeMillis();
		Trie trie = new Trie();
		for (TFilterKeyword filterKeyword : replacements) {
			FilterKeywordWrap wrap = new FilterKeywordWrap(filterKeyword);
			trie.addKeyword(filterKeyword.getKeyword(), wrap.getBehavior());
		}
		trie.checkBuild();
		long end = System.currentTimeMillis();
		System.out.println("创建树耗时：" + (end - start) + "毫秒");
		ParseResult pr = trie.filterText(text);
		System.out.println("过滤耗时：" + (System.currentTimeMillis() - end) + "毫秒");
		return pr.getFilteredText();
	}

	public List<String> keywords() {
		List<String> keys = new ArrayList<String>();
		keys.add("草");
		keys.add("你妹");
		keys.add("我了个去");
		keys.add("fuck");
		for (int i = 0; i < 10000; i++) {
			keys.add("草" + i);
			keys.add("你妹" + i);
			keys.add("我了个去" + i);
			keys.add("fuck" + i);
		}
		return keys;
	}

	public List<TFilterKeyword> filterKeywords() throws FilterException {
		List<TFilterKeyword> filterKeywords = new ArrayList<TFilterKeyword>();
		for (String key : keywords()) {
			filterKeywords.add(new TFilterKeyword(key).setBehavior_type(
					TBehaviorType.REPLACE).setReplacement("##"));
		}
		return filterKeywords;
	}

	// 1,测试性能
	// 40000个短关键字占了近20M的内存， 但速度很快，并且不随关键字数的增长而线性增长，40000个关键字和10个关键字速度只差三倍
	@Test
	public void testPerformance() throws InterruptedException, FilterException {
		InputStream in = this.getClass().getResourceAsStream("/content.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		reader.lines().forEach(sb::append);
		System.out.println(simpleReplace(sb.toString(), keywords()));
		System.out.println(filterReplace(sb.toString(), filterKeywords()));
		Thread.sleep(1000000);
	}

	// 2,调试inc
	// 3,全局架构
}
