package tv.ismar.searchpage.data.http;

/**
 * Created by huaijie on 12/23/15.
 */
public class VoiceResultEntity {
    private String json_res;
    private String[] item;


    public String[] getItem() {
        return item;
    }

    public void setItem(String[] item) {
        this.item = item;
    }

    public String getJson_res() {
        return json_res;
    }

    public void setJson_res(String json_res) {
        this.json_res = json_res;
    }
}
