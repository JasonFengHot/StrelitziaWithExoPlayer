package tv.ismar.searchpage.data.http;

/**
 * Created by huaijie on 2016/1/30.
 */
public class IndicatorResponseEntity {

    private Object searchData;
    private String type;
    private String semantic;

    public String getSemantic() {
        return semantic;
    }

    public void setSemantic(String semantic) {
        this.semantic = semantic;
    }

    public Object getSearchData() {
        return searchData;
    }

    public void setSearchData(Object searchData) {
        this.searchData = searchData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
