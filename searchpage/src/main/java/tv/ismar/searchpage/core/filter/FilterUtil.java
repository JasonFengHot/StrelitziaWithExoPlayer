package tv.ismar.searchpage.core.filter;

import java.util.List;

/**
 * Created by huaijie on 2016/1/20.
 */
public class FilterUtil {
    private static final String TAG = "FilterUtil";
    private static final String[] words1 = new String[]{"增大", "查询", "播放", "我想看", "打开", "搜索", "静音", "取消", "关机", "播放", "运行", "我想玩", "减小", "查找"};

    public static List<WordFilterResult> filter(String content) {
        word_filter wf = new word_filter();
        wf.add_wrods(words1, 1);
        List<WordFilterResult> results = wf.Match(content);
        return results;
    }


    public void input(String action, String name) {
        String key = action + name;
        switch (key) {
            case "dakaiweixin":
                break;
            default:
                break;
        }
    }
}
