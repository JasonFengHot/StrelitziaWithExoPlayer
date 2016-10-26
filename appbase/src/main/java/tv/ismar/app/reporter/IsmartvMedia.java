package tv.ismar.app.reporter;

import java.io.Serializable;

/**
 * Created by Beaver on 2016/6/2.
 */
public class IsmartvMedia implements Serializable {

    /**
     * 片名
     */
    private String title;
    /**
     * pk值
     */
    private int pk;
    /**
     * subItem pk值
     */
    private int subItemPk;
    /**
     * clip pk值
     */
    private int clipPk;
    /**
     * clip length值
     */
    private int clipLength;
    /**
     * 频道
     */
    private String channel;
    /**
     * 来源
     */
    private String source;
    /**
     * 列表名
     */
    private String section;
    /**
     * 是否直播
     */
    private boolean isLive;
    private boolean isPay;
    private String vendor;
    private String contentModel;
    private String topic;
    private String director;
    private String actor;
    private String genre;

    public IsmartvMedia(int pk, int subItemPk) {
        this.pk = pk;
        this.subItemPk = subItemPk;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getPk() {
        return pk;
    }

    public int getSubItemPk() {
        return subItemPk;
    }

    public int getClipPk() {
        return clipPk;
    }

    public void setClipPk(int clipPk) {
        this.clipPk = clipPk;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public boolean isPay() {
        return isPay;
    }

    public void setPay(boolean pay) {
        isPay = pay;
    }

    public int getClipLength() {
        return clipLength;
    }

    public void setClipLength(int clipLength) {
        this.clipLength = clipLength;
    }

    public String getContentModel() {
        return contentModel;
    }

    public void setContentModel(String contentModel) {
        this.contentModel = contentModel;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
