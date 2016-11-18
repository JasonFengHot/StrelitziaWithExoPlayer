package tv.ismar.searchpage.core.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Word_split {
    private static Word_split ws = null;
    private word_filter wf;

    private Word_split() {
        //
        wf = new word_filter();
//        loadData(UploadDicDataServlet.Dic_Dir + "location", 1);
//        loadData(UploadDicDataServlet.Dic_Dir + "actor", 2);
//        loadData(UploadDicDataServlet.Dic_Dir + "video", 3);
    }

    public static Word_split getInstance(String path) {
        if (ws == null) {
            System.out.println("getInstance");
//            Configuration cfg = DefaultConfig.getInstance();
//            cfg.setUseSmart(true);
//            Dictionary.initial(cfg);
//			Dictionary dictionary = Dictionary.getSingleton();
            ws = new Word_split();
        }
        return ws;
    }

    public void add_words(String[] words, int tag) {
        wf.add_wrods(words, tag);
    }

    public void clearData() {
        wf.clearDic();
//        loadData(UploadDicDataServlet.Dic_Dir + "location", 1);
    }

    public class Match_result {
        public String content;
        public int tag;

    }

    public List<Match_result> MatchWords(String content) {
        List<Match_result> result = new ArrayList<Match_result>();
        List<WordFilterResult> results = wf.Match(content);
        for (WordFilterResult rslt : results) {
            Match_result element = new Match_result();
            element.content = content.substring(rslt.start, rslt.end + 1);
            element.tag = rslt.tag;
            result.add(element);
        }
        return result;
    }

    private void loadData(String filepath, int tag) {
        FileReader reader = null;
        try {
            File file = new File(filepath);
            if (!file.exists())
                file.createNewFile();
            reader = new FileReader(file.getAbsolutePath());
            BufferedReader br = new BufferedReader(reader);
            List<String> locationlist = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null) {
                locationlist.add(line);
            }
            String[] aa = locationlist.toArray(new String[0]);
            add_words(aa, tag);
            System.out.println(tag + "++" + locationlist.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        word_filter wf = new word_filter();

        String[] words1 = new String[]{"中华", "中华人民共和国", "中华人民", "中华人民共和",
                "中华人民共和国", "中华民族"};
        String[] words2 = new String[]{"应用", "应用程序", "应用电子学院", "应用电子"};

        String[] words3 = new String[]{"刘德华", "张学友", "黄晓明"};

        String[] words4 = new String[]{"少林寺", "新少林寺", "变形金刚", "魔界"};

        wf.add_wrods(words1, 1);
        wf.add_wrods(words2, 2);
        wf.add_wrods(words3, 3);
        wf.add_wrods(words4, 4);

        String[] tests = new String[]{"刘德华的电视剧新少林寺", "刘德华黄晓明新少林寺变形金刚魔界电影",
                "刘德华和黄晓明的电视剧新少林寺以及变形金刚包括魔界电影", "你好，中华人民共和国的应用电子学院故事会应用电子",
                "中华人民共和国", "你好，中华人民共和国", "我爱你中华民族", "我的中华我的中华民族少林寺",
                "刘德华+少林寺中华人民共和国中华中华人民共和"};

        for (String content : tests) {
            List<WordFilterResult> results = wf.Match(content);
            System.out.printf("content---> %s\n", content);

            int i = 0;
            for (WordFilterResult rslt : results) {
                i++;
                System.out.printf("%d: %d-%d %s       tag(%d)\n", i,
                        rslt.start, rslt.end,
                        content.substring(rslt.start, rslt.end + 1), rslt.tag);
            }
            System.out.println();
        }

        return;
    }
}
