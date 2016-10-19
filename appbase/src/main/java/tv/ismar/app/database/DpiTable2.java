package tv.ismar.app.database;

import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

@Table(name = "dpi2", id = "_id")
public class DpiTable2 extends Model {
    @Column
    public int pay_type;
    @Column
    public String image;
    @Column
    public int cp;
    @Column
    public int name;

}
