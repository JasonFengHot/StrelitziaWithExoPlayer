package tv.ismar.app.models;

import java.util.List;

/** Created by admin on 2016/2/2. */
public class VodFacetEntity {
    /** count : 0 total_count : 300 objects : [] name : 演员 content_type : person */
    public List<FacetEntity> facet;

    public static class FacetEntity {
        public int count;
        public int total_count;
        public String name;
        public String content_type;
        public float bean_score;
        public float price;
        public List<VodObjectEntity> objects;
    }
    //    private int count;
    //    private int total_count;
    //    private List<VodObjectEntity> objects;
    //
    //
    //    public int getCount() {
    //        return count;
    //    }
    //
    //    public void setCount(int count) {
    //        this.count = count;
    //    }
    //
    //    public int getTotal_count() {
    //        return total_count;
    //    }
    //
    //    public void setTotal_count(int total_count) {
    //        this.total_count = total_count;
    //    }
    //
    //    public List<VodObjectEntity> getObjects() {
    //        return objects;
    //    }
    //
    //    public void setObjects(List<VodObjectEntity> objects) {
    //        this.objects = objects;
    //    }

}
