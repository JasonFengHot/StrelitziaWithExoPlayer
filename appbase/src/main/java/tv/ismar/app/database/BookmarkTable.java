package tv.ismar.app.database;

import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huibin on 8/24/16.
 */

@Table(name = "bookmark", id = "_id")
public class BookmarkTable extends Model {

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public int pk;

    @Column
    public String title;

    @Column
    public boolean sync;
}
