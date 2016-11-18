package tv.ismar.searchpage.core.filter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class word_filter {

	public word_filter() {
		ft = new HashMap<Character, word_filter_node>();
	}

	public void clearDic() {
		ft.clear();
	}

	public boolean add_word(String word, int tag) {
		int word_len = word.length();
		Map<Character, word_filter_node> cur_ft = ft;

		for (int i = 0; i < word_len; i++) {
			char p = word.charAt(i);
			if (!cur_ft.containsKey(p)) {
				// 已经找到叶子节点，但是word还没有匹配完
				word_filter_node new_node = new word_filter_node(
						word.substring(i + 1), true, tag);
				cur_ft.put(new Character(p), new_node);
				break;
			} else {
				word_filter_node cur_node = cur_ft.get(p);

				if (cur_node.tail.isEmpty()) {
					if (i + 1 == word_len) {
						cur_node.tag = tag;
						cur_node.terminator = true;
						break;
					} else {
						cur_ft = cur_node.ft;

						if (cur_ft == null) {
							cur_node.ft = new HashMap<Character, word_filter_node>();
							cur_ft = cur_node.ft;
						}
					}
				} else {
					if (i + 1 < word_len
							&& cur_node.tail.equals(word.substring(i + 1))) {
						// 对于当前的字符串是之前的某个子串的情况，
						// 需要设置terminator和tag
						cur_node.tag = tag;
						cur_node.terminator = true;
						break;
					} else {
						// 获取tail的首字符，将当前的节点扩展出子节点
						word_filter_node new_node = new word_filter_node(
								cur_node.tail.substring(1), true, tag);

						cur_node.ft = new HashMap<Character, word_filter_node>();
						cur_node.ft.put(new Character(cur_node.tail.charAt(0)),
								new_node);

						if (i + 1 == word_len) {
							cur_node.tag = tag;
							cur_node.terminator = true;
						} else {
							cur_node.tag = 0;
							cur_node.terminator = false;
						}

						cur_node.tail = "";

						cur_ft = cur_node.ft;
					}
				}
			}

		}
		return true;
	}

	public boolean add_wrods(String[] words, int tag) {
		for (String word : words) {
			if (!add_word(word, tag)) {
				return false;
			}
		}
		return true;
	}

	public List<WordFilterResult> Match(String content) {
		int content_len = content.length();
		int cur = 0;
		int prev = 0;
		int mark_count_pos = -1;
		int mark_tag = 0;

		Map<Character, word_filter_node> cur_ft = ft;

		List<WordFilterResult> results = new LinkedList<WordFilterResult>();
		while (cur < content_len) {
			char p = content.charAt(cur);
			if (cur_ft == null || !cur_ft.containsKey(p)) {
				if (mark_count_pos >= 0) {
					WordFilterResult rslt = new WordFilterResult(prev,
							mark_count_pos, mark_tag);
					results.add(rslt);

					// 从前面识别到的单词的后一个字符开始继续搜索
					prev = mark_count_pos + 1;
					cur = prev;
					mark_count_pos = -1;
					mark_tag = 0;
				} else {
					cur = prev + 1;
					prev = cur;
				}

				// 从树根开始继续搜索
				cur_ft = ft;
			} else {
				word_filter_node cur_node = cur_ft.get(p);
				if (cur_node.tail.isEmpty()) {
					if (cur_node.terminator) {
						mark_count_pos = cur;
						mark_tag = cur_node.tag;
					}

					// 继续往搜索树的子树搜索
					cur_ft = cur_node.ft;
					cur++;
				} else {
					// 当前节点因为有tail，必定没有子节点了,而且必然是terminator
					int tail_len = cur_node.tail.length();

					if (tail_len <= content_len - cur - 1
							&& cur_node.tail.equals(content.substring(cur + 1,
									cur + 1 + tail_len))) {
						// tail部分正好是content当前位置后面字符串的子串，则发现一个新的匹配点
						WordFilterResult rslt = new WordFilterResult(prev,
								cur + tail_len, cur_node.tag);
						results.add(rslt);

						prev = cur + tail_len + 1;
						cur = prev;

						mark_count_pos = -1;
						mark_tag = 0;
					} else {
						if (mark_count_pos >= 0) {
							WordFilterResult rslt = new WordFilterResult(
									prev, mark_count_pos, mark_tag);
							results.add(rslt);

							// 从前面识别到的单词的后一个字符开始继续搜索
							prev = mark_count_pos + 1;
							cur = prev;
							mark_count_pos = -1;
							mark_tag = 0;
						} else {
							// 将原文读取指针回溯到prev+1
							cur = prev + 1;
							prev = cur;
						}
					}

					// 从树根开始继续搜索
					cur_ft = ft;
				}
			}
		}

		// 遍历完毕后，对于尚未提交到result列表的最后一个发现的字符串写入结果集
		if (mark_count_pos >= 0) {
			WordFilterResult rslt = new WordFilterResult(prev,
					mark_count_pos, mark_tag);
			results.add(rslt);
		}

		return results;
	}

	protected Map<Character, word_filter_node> ft;
}