package tv.ismar.app.db.location;


import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huaijie on 8/3/15.
 */
@Table(name = "app_isp", id = "_id")
public class IspTable extends Model {
    public static final String ISP_ID = "isp_id";
    public static final String ISP_NAME = "isp_name";

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String isp_id;

    @Column
    public String isp_name;
}
