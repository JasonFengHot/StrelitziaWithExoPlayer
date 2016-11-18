package tv.ismar.searchpage.data.http;

/**
 * Created by huaijie on 1/5/16.
 */
public class JsonRes {
    private String parsed_text;
    private String raw_text;
    private Object results;

    public Object getResults() {
        return results;
    }

    public void setResults(Object results) {
        this.results = results;
    }

    public String getParsed_text() {
        return parsed_text;
    }

    public void setParsed_text(String parsed_text) {
        this.parsed_text = parsed_text;
    }

    public String getRaw_text() {
        return raw_text;
    }

    public void setRaw_text(String raw_text) {
        this.raw_text = raw_text;
    }
}
