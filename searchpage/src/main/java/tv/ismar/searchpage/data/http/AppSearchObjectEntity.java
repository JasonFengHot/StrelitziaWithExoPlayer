package tv.ismar.searchpage.data.http;

/**
 * Created by huaijie on 1/20/16.
 */
public class AppSearchObjectEntity {
    private boolean isLocal;

    public boolean isLocal() {
        return isLocal;
    }

    public void setIsLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    private String title;
    private String adlet_url;
    private String url;
    private String caption;
    private String pk;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAdlet_url() {
        return adlet_url;
    }

    public void setAdlet_url(String adlet_url) {
        this.adlet_url = adlet_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
