package tv.ismar.searchpage.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/2/2.
 */
public class FaceResultItem {
    /**
     * content_type : movie
     * count : 0
     * objects : [{"adlet_url":"http://res.tvxio.com/media/upload/20140922/upload/haidi480140211_adlet.jpg","attributes":{"actor":[[40792,"秀兰·邓波儿"]],"air_date":"1937-01-01","area":[10034,"美国"],"director":[[50530,"Allan Dwan"]],"genre":[[10021,"剧情片"],[10434,"儿童片"],[10656,"家庭片"]]},"bean_score":8.3,"caption":"","content_model":"movie","episode":0,"focus":"邓波儿和爷爷相依为命","image":"http://res.tvxio.com/media/upload/haidi480140211.jpg","is_complex":true,"item_pk":389888,"item_url":"http://skytest.tvxio.com/v1_0/SKY/0g0/api/item/389888/","list_url":"http://res.tvxio.com/img/vertical/hdg254_p2187692690.jpg","live_video":false,"model_name":"item","pk":389888,"position":0,"poster_url":"http://res.tvxio.com/media/upload/20140922/upload/haidi480140211_poster.jpg","publish_date":"2014-02-11 22:18:28","quality":2,"rated":2,"tags":[],"thumb_url":"http://res.tvxio.com/media/upload/20140922/upload/haidi480140211_thumb.jpg","title":"海蒂(国)","url":"http://skytest.tvxio.com/v1_0/SKY/0g0/api/item/389888/"},{"adlet_url":"http://res.tvxio.com/media/upload/upload/rtsan4802700625_adlet.jpg","attributes":{"actor":[[26266,"Amita Pathak"]],"air_date":"2008-01-01","area":[10482,"印度"],"director":[[26264,"Anil Devgan"]],"genre":[[10019,"爱情片"],[10021,"剧情片"]]},"bean_score":4.3,"caption":"","content_model":"movie","episode":0,"focus":"沙卡付出真心得到爱情","image":"http://res.tvxio.com/media/upload/rtsan4802700625.jpg","is_complex":true,"item_pk":68648,"item_url":"http://skytest.tvxio.com/v1_0/SKY/0g0/api/item/68648/","list_url":"http://res.tvxio.com/img/vertical/rtsan254_p2230603363.jpg","live_video":false,"model_name":"item","pk":68648,"position":0,"poster_url":"http://res.tvxio.com/media/upload/upload/rtsan4802700625_poster.jpg","publish_date":"2012-03-31 11:25:49","quality":4,"rated":5,"tags":[],"thumb_url":"http://res.tvxio.com/media/upload/upload/rtsan4802700625_thumb.jpg","title":"让她说爱你","url":"http://skytest.tvxio.com/v1_0/SKY/0g0/api/item/68648/"}]
     * total_count : 0
     */

    public List<FacetEntity> facet;

    public static List<FaceResultItem> arrayFaceResultItemFromData(String str) {

        Type listType = new TypeToken<ArrayList<FaceResultItem>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
    }

    public static class FacetEntity {
        public String content_type;
        public int count;
        public int total_count;
        /**
         * adlet_url : http://res.tvxio.com/media/upload/20140922/upload/haidi480140211_adlet.jpg
         * attributes : {"actor":[[40792,"秀兰·邓波儿"]],"air_date":"1937-01-01","area":[10034,"美国"],"director":[[50530,"Allan Dwan"]],"genre":[[10021,"剧情片"],[10434,"儿童片"],[10656,"家庭片"]]}
         * bean_score : 8.3
         * caption :
         * content_model : movie
         * episode : 0
         * focus : 邓波儿和爷爷相依为命
         * image : http://res.tvxio.com/media/upload/haidi480140211.jpg
         * is_complex : true
         * item_pk : 389888
         * item_url : http://skytest.tvxio.com/v1_0/SKY/0g0/api/item/389888/
         * list_url : http://res.tvxio.com/img/vertical/hdg254_p2187692690.jpg
         * live_video : false
         * model_name : item
         * pk : 389888
         * position : 0
         * poster_url : http://res.tvxio.com/media/upload/20140922/upload/haidi480140211_poster.jpg
         * publish_date : 2014-02-11 22:18:28
         * quality : 2
         * rated : 2
         * tags : []
         * thumb_url : http://res.tvxio.com/media/upload/20140922/upload/haidi480140211_thumb.jpg
         * title : 海蒂(国)
         * url : http://skytest.tvxio.com/v1_0/SKY/0g0/api/item/389888/
         */

        public List<ObjectsEntity> objects;

        public static List<FacetEntity> arrayFacetEntityFromData(String str) {

            Type listType = new TypeToken<ArrayList<FacetEntity>>() {
            }.getType();

            return new Gson().fromJson(str, listType);
        }

        public static class ObjectsEntity {
            public String adlet_url;
            /**
             * actor : [[40792,"秀兰·邓波儿"]]
             * air_date : 1937-01-01
             * area : [10034,"美国"]
             * director : [[50530,"Allan Dwan"]]
             * genre : [[10021,"剧情片"],[10434,"儿童片"],[10656,"家庭片"]]
             */

            public AttributesEntity attributes;
            public double bean_score;
            public String caption;
            public String content_model;
            public int episode;
            public String focus;
            public String image;
            public boolean is_complex;
            public int item_pk;
            public String item_url;
            public String list_url;
            public boolean live_video;
            public String model_name;
            public int pk;
            public int position;
            public String poster_url;
            public String publish_date;
            public int quality;
            public int rated;
            public String thumb_url;
            public String title;
            public String url;
            public List<?> tags;



            public static class AttributesEntity {
                public String air_date;
                public List<List<Integer>> actor;
                public List<Integer> area;
                public List<List<Integer>> director;
                public List<List<Integer>> genre;

                public static List<AttributesEntity> arrayAttributesEntityFromData(String str) {

                    Type listType = new TypeToken<ArrayList<AttributesEntity>>() {
                    }.getType();

                    return new Gson().fromJson(str, listType);
                }
            }
        }
    }
    public static List<FacetEntity.ObjectsEntity> arrayObjectsEntityFromData(String str) {

        Type listType = new TypeToken<ArrayList<FacetEntity.ObjectsEntity>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
    }

//    public List<FacetEntity> facet;
//
//    public static class FacetEntity {
//        public int count;
//        public int total_count;
//        public String content_type;
//        public List<ObjectsEntity> objects;
//
//        public static class ObjectsEntity {
//            public String image;
//            public String focus;
//            public String content_model;
//            public int quality;
//            public int rated;
//            public String title;
//            public String adlet_url;
//            public String list_url;
//            public double bean_score;
//            public String poster_url;
//            public int pk;
//            public String item_url;
//
//            public AttributesEntity attributes;
//            public int episode;
//            public String thumb_url;
//            public boolean live_video;
//            public String url;
//            public String caption;
//            public String publish_date;
//            public boolean is_complex;
//            public int position;
//            public Object expense;
//            public int item_pk;
//            public String model_name;
//            public List<?> tags;
//
//            public static class AttributesEntity {
//                public String air_date;
//                public List<List<Integer>> director;
//                public List<List<Integer>> genre;
//                public List<Integer> area;
//                public List<List<Integer>> actor;
//            }
//        }
//    }
}
