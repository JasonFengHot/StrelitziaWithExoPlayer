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
}
