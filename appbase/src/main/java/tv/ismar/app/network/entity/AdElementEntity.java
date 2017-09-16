package tv.ismar.app.network.entity;

import java.io.Serializable;
import java.util.HashMap;

/** Created by longhai on 16-9-12. */
public class AdElementEntity implements Serializable {

    private int retcode;
    private String retmsg;
    private String title; // 广告标题
    private int media_id; // 展示的投放活动id
    private String description; // 广告描述信息
    private String media_url; // 视频或图片资源地址
    private String tag; // 对于非打点标签类广告，tag值为空, 对于打点标签类广告，tag不为空，"00:05:28"  为打点广告在clip中弹出的时间点；
    private HashMap<String, String> coordinate; // 打点标签在播放器中的坐标位置
    private String report_url; // 客户端广告曝光上报地址
    private String md5; //
    private String media_type; // video,img,html  广告视频，图片，网页
    private int serial; // 第几个展示，从0开始表示第一个
    private int start; // 第几秒开始
    private int end; // 第几秒结束
    private int duration; // 一共展示时长多少
    private String start_date;
    private String end_date;
    private String start_time;
    private String end_time;

    public int getRetcode() {
        return retcode;
    }

    public void setRetcode(int retcode) {
        this.retcode = retcode;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMedia_id() {
        return media_id;
    }

    public void setMedia_id(int media_id) {
        this.media_id = media_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getReport_url() {
        return report_url;
    }

    public void setReport_url(String report_url) {
        this.report_url = report_url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }
}
