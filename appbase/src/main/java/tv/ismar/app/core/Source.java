package tv.ismar.app.core;

/**
 * Created by longhai on 16-10-14.
 */
public enum Source {

    LAUNCHER("launcher"),//launcher进入
    LIST("list"),//列表页进入
    HISTORY("history"),//历史记录
    FAVORITE("favorite"),//我的收藏
    SEARCH("search"),//文字搜索
    VOICESEARCH("voicesearch"),//文字搜索
    RELATED("related"),//关联影片
    TOPIC("topic"),//专题
    RETRIEVAL("retrieval"),//条件检索
    ORDER("order"),//我的订购
    LIVE("live"),//轮播的已播放影片
    TVHOME("tvhome"),//首页五个推荐,其余为unknow
    TOPVIDEO("topvideo"),//首页上方的视频,其余为unknow
    HOMEPAGE("homepage"),//首页五个推荐,其余为unknow
    UNKNOWN("unknown");

    private String source;

    Source(String source) {
        this.source = source;
    }

    public String getValue() {
        return source;
    }

}
