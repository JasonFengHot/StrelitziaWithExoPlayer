package tv.ismar.app.models;

import com.google.gson.JsonObject;

/**
 * Created by huaijie on 1/20/16.
 */
public class SemanticSearchRequestEntity {

    private JsonObject semantic;
    private String content_type;
    private int page_on;
    private int page_count;


    public JsonObject getSemantic() {
        return semantic;
    }

    public void setSemantic(JsonObject semantic) {
        this.semantic = semantic;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public int getPage_on() {
        return page_on;
    }

    public void setPage_on(int page_on) {
        this.page_on = page_on;
    }

    public int getPage_count() {
        return page_count;
    }

    public void setPage_count(int page_count) {
        this.page_count = page_count;
    }


}
