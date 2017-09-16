package tv.ismar.app.db.location;

import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/** Created by huaijie on 8/3/15. */
@Table(name = "app_cdn", id = "_id")
public class CdnTable extends Model {
    public static final String CDN_ID = "cdn_id";
    public static final String CDN_NAME = "cdn_name";
    public static final String CDN_NICK = "cdn_nick";
    public static final String CDN_FLAG = "cdn_flag";
    public static final String CDN_IP = "cdn_ip";
    public static final String DISTRICT_ID = "district_id";
    public static final String ISP_ID = "isp_id";
    public static final String ROUTE_TRACE = "route_trace";
    public static final String SPEED = "speed";
    public static final String CHECKED = "checked";

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public long cdn_id;

    @Column public String cdn_name;

    @Column public String cdn_nick;

    @Column public int cdn_flag;

    @Column public String cdn_ip;

    @Column public String district_id;

    @Column public String isp_id;

    @Column public int route_trace;

    @Column public int speed;

    @Column public boolean checked;
}
