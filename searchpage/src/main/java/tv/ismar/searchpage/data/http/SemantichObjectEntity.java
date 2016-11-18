package tv.ismar.searchpage.data.http;

import tv.ismar.searchpage.model.Expense;

/**
 * Created by huaijie on 1/20/16.
 */
public class SemantichObjectEntity {
    private String title;
    private String poster_url;
    private String vertical_url;
    private String pk;
    private String url;
    private String content_model;
    private AttributesEntity attributes;
    private String description;
    private String focus;
    private String bean_score;
    private Expense expense;

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public String getBean_score() {
        return bean_score;
    }

    public void setBean_score(String bean_score) {
        this.bean_score = bean_score;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AttributesEntity getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributesEntity attributes) {
        this.attributes = attributes;
    }

    public String getContent_model() {
        return content_model;
    }

    public void setContent_model(String content_model) {
        this.content_model = content_model;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVertical_url() {
        return vertical_url;
    }

    public void setVertical_url(String vertical_url) {
        this.vertical_url = vertical_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster_url() {
        return poster_url;
    }

    public void setPoster_url(String poster_url) {
        this.poster_url = poster_url;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }


}
