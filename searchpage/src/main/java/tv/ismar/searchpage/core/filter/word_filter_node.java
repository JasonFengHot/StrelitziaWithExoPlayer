package tv.ismar.searchpage.core.filter;

import java.util.Map;

public class word_filter_node
{
	public word_filter_node(String tl, boolean term, int tg )
	{
		tail = tl;
		terminator = term;
		tag  = tg;
	}
	
	public Map<Character, word_filter_node> ft;	// filter tree
	public String tail;							// 后缀部分
	public boolean     terminator;					// 终止符标记
	public int         tag;								// 识别出来后单词的标记
}
