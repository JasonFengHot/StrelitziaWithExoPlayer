package tv.ismar.app.db;


import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huaijie on 7/31/15.
 */
@Table(name = "advertisement", id = "_id")
public class AdvertiseTable extends Model {

    public static final String TITLE = "title";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String EVERYDAY_TIME_FROM = "everyday_time_from";
    public static final String EVERYDAY_TIME_TO = "everyday_time_to";
    public static final String MEDIA_URL = "media_url";
    public static final String MEDIA_TYPE = "media_type";
    public static final String DURATION = "duration";
    public static final String MD5 = "md5";
    public static final String LOCATION = "location";
    public static final String TYPE = "type";
    public static final String URL = "url";

    @Column
    public String title;

    @Column
    public long start_date;

    @Column
    public long end_date;

    @Column
    public long everyday_time_from;

    @Column
    public long everyday_time_to;

    @Column
    public String media_id;

    @Column
    public String media_url;

    @Column
    public String media_type;

    @Column
    public int duration;

    @Column
    public String md5;

    @Column
    public String location;

    @Column
    public String type;

    @Column
    public String url;

}
