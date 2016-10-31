package tv.ismar.app.db;

import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huibin on 8/11/16.
 */

@Table(name = "dpi", id = "_id")
public class DpiTable extends Model {
    @Column
    public int pay_type;
    @Column
    public String image;
    @Column
    public int cp;
    @Column
    public int name;

}
