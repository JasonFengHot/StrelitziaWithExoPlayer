package tv.ismar.app.models;

/** Created by admin on 2016/2/2. */
public class VodObjectEntity {
    private String title;
    private String adlet_url;
    private String list_url;
    private String content_model;
    private String url;
    private String focus;
    private double bean_score;
    private Expense expense;
    private long pk;

    public String getFocus() {
        return focus;
    }

    public double getBean_score() {
        return bean_score;
    }

    public Expense getExpense() {

        return expense;
    }

    public String getContent_model() {
        return content_model;
    }

    public String getUrl() {
        return url;
    }

    public String getList_url() {
        return list_url;
    }

    public void setList_url(String list_url) {
        this.list_url = list_url;
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

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }
}
