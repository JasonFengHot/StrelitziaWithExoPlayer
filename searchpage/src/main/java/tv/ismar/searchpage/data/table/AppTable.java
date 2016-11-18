package tv.ismar.searchpage.data.table;

import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huaijie on 1/4/16.
 */


@Table(name = "app_table")
public class AppTable extends Model {

    @Column
    public String app_name;

    @Column
    public String app_package;

    @Column
    public int version_code;

    @Column
    public String version_name;

}
