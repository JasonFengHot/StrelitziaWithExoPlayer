package tv.ismar.app.reporter;

import java.io.Serializable;

/**
 * 日志上报相关
 */
public class IsmartvMedia implements Serializable {

    private String title = "";
    private int pk;
    private int subItemPk;
    private int clipPk;
    private String channel = "";
    private String source = "";
    private String section = "";

    public IsmartvMedia(int pk, int subItemPk) {
        this.pk = pk;
        this.subItemPk = subItemPk;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getSubItemPk() {
        return subItemPk;
    }

    public void setSubItemPk(int subItemPk) {
        this.subItemPk = subItemPk;
    }
}
