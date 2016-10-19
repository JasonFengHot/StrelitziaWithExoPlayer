package tv.ismar.app.db.location;


import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huaijie on 8/3/15.
 */

@Table(name = "app_province", id = "_id")
public class ProvinceTable extends Model {
    public static final String PINYIN = "pinyin";
    public static final String PROVINCE_NAME = "province_name";


    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String province_id;

    @Column
    public String province_name;

    @Column
    public String pinyin;

    @Column
    public String district_id;
}
