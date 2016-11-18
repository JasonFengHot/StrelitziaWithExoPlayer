package tv.ismar.app.models;

import java.util.List;

/**
 * Created by admin on 2016/2/18.
 */
public class Recommend {

    public int count;
    public List<ObjectsEntity> objects;
    public static class ObjectsEntity {
        public String adlet_url;
        public double bean_score;
        public String caption;
        public String content_model;
        public int counting_count;
        public String description;
        public String detail_url;
        public Expense expense;
        public String focus;
        public boolean is_complex;
        public int item_pk;
        public boolean live_video;
        public int pk;
        public int position;
        public String poster_url;
        public String source;
        public String title;
        public String url;
        public String vertical_url;



    }


}
