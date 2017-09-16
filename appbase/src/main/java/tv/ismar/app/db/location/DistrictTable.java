package tv.ismar.app.db.location;

import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/** Created by huaijie on 8/3/15. */
@Table(name = "app_district", id = "_id")
public class DistrictTable extends Model {

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String district_id;

    @Column public String district_name;
}
