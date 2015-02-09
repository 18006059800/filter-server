package com.zyx.filter.text;

import java.util.List;
import java.util.Map;

import com.zyx.filter.AbstractFilter;
import com.zyx.filter.Content;
import com.zyx.filter.exception.FilterException;
import com.zyx.filter.thrift.TFilter;
import com.zyx.filter.thrift.TFilterKeyword;
import com.zyx.filter.thrift.TFilterLevel;
import com.zyx.filter.thrift.TJudgeResult;
import com.zyx.trie.Trie;

/**
 * <p>文本过滤器，根据关键字进行过滤</p>
 * <p>关键字信息中包含相应的过滤行为</p>
 * @see BehaviorType
 * @see FilterKeywordWrap
 * @author Ervin.zhang
 */
public class TextFilterWrap extends AbstractFilter {

	/**
	 * 过滤关键字
	 */
	private List<TFilterKeyword> filterKeywords;

	/**
	 * 特里查找树
	 */
	private volatile Trie trie;
	
	public TextFilterWrap(TFilter filter) throws FilterException {
		if (filter.textFilter == null) {
			throw FilterException.fieldNullException("textFilter");
		}
		this.name = filter.name;
		this.field = filter.field;
		this.filterKeywords = filter.textFilter.filter_keywords;
		checkInput();
	}
	
	public static void checkValid(TFilter filter) throws FilterException {
		new TextFilterWrap(filter);
	}
	
	@Override
	public void checkInput() throws FilterException {
		
		super.checkInput();
		
//		if (filterKeywords == null) {
//			throw FilterException.fieldNullException("filterKeywords");
//		}
	}

	/**
	 * 创建特里查找树，output置一个过滤行为
	 * @throws FilterException 
	 */
	public void createTrieTree() throws FilterException {
		if (filterKeywords == null) {
			throw FilterException.fieldNullException("filterKeywords");
		}
		Trie newTrie = new Trie();
		for (TFilterKeyword filterKeyword : filterKeywords) {
			FilterKeywordWrap wrap = new FilterKeywordWrap(filterKeyword);
			newTrie.addKeyword(filterKeyword.getKeyword(),
					wrap.getBehavior());
		}
		newTrie.checkBuild();
		trie = newTrie;
	}

	/**
	 * 文本过滤
	 */
	@Override
	public TJudgeResult filter(Object filterObject, Map<String, String> resp) {
		String text = String.valueOf(filterObject);
		ParseResult result = trie.filterText(text);
		// 将过滤后的文本放到resp里
		resp.put(field, result.getFilteredText());
		TJudgeResult judgeResult = trans2Judge(result.getLevel());
		if (judgeResult == TJudgeResult.FORBIDDEN) {
			resp.put(Content.FORBIDDEN_REASON, field + "中包含被禁止的敏感词");
		}
		return judgeResult;
	}
	
	private TJudgeResult trans2Judge(TFilterLevel level) {
		if (level == null) {
			return TJudgeResult.OK;
		}
		switch (level) {
		case ADJUST:
			return TJudgeResult.ADJUST;
		case NOTICE:
			return TJudgeResult.NOTICE;
		case CHECK:
			return TJudgeResult.CHECK;
		case FORBIDDEN:
			return TJudgeResult.FORBIDDEN;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * @return the {@link #filterKeywords}
	 */
	public List<TFilterKeyword> getFilterKeywords() {
		return filterKeywords;
	}
	
	public void addKeyword(TFilterKeyword keyword) throws FilterException {
		filterKeywords.add(keyword);
		// 重建trie树
		createTrieTree();
	}

	public void deleteKeyword(String keywordId) throws FilterException {
		for (int i = 0; i < filterKeywords.size(); i++) {
			TFilterKeyword keyword = filterKeywords.get(i);
			if (keywordId.equals(keyword.get_id())) {
				filterKeywords.remove(i);
				break;
			}
		}
		// 重建trie树
		createTrieTree();
	}
}
