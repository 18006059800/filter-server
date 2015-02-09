package com.zyx.trie;

/**
 * 匹配结果输出
 * @author Ervin.zhang
 */
public class Output  {

	/** 匹配关键字 */
    public final String keyword;
    /** 输出内容 */
    public Object data;

    public Output(final int start, final int end, final String keyword, Object data) {
        this.data = data;
        this.keyword = keyword;
    }
    
    public Output(final int start, final int end, final String keyword) {
        this(start, end, keyword, null);
    }

    public String getKeyword() {
        return this.keyword;
    }

    public Object getData() {
		return data;
	}

	@Override
    public String toString() {
        String t = super.toString() + "=" + this.keyword;
        if(data != null)
        	t += "(" + data + ")";
        return t;
	}

}
