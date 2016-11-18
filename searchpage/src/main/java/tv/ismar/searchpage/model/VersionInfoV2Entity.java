package tv.ismar.searchpage.model;

import java.util.ArrayList;

/**
 * Created by huaijie on 10/22/15.
 */
public class VersionInfoV2Entity {
    public  ApplicationEntity application;
    public String version;
    public String homepage;



    public class  ApplicationEntity {
        public String product;
        public String name;
        public String screenshot;
        public String url;
        public ArrayList<String> update;
        public String summary;
        public String version;
        public String md5;

    }
}
