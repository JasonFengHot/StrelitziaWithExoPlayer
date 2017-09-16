package tv.ismar.app.entity;

import android.database.Cursor;

import java.io.Serializable;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.db.DBHelper.DBFields;

public class History implements Serializable, Comparable<History> {

    private static final long serialVersionUID = -921551037735535764L;

    public long id;
    public String userid;
    public String title;
    public String url;
    public String adlet_url;
    public String content_model;
    public int quality = 1;
    public int last_quality = 1;
    public boolean is_complex;
    public boolean is_continue;
    public long last_played_time;
    public long last_position;
    public String sub_url;
    public String isnet;
    public int price;
    public int cpid;
    public String cpname;
    public String cptitle;
    public int paytype;

    public History() {
        super();
    }

    public History(
            long id,
            String title,
            String url,
            String adlet_url,
            String content_model,
            int quality,
            int last_quality,
            boolean is_complex,
            boolean is_continue,
            long last_played_time,
            long last_position,
            String sub_url,
            String isNet,
            int price,
            int cpid,
            String cpname,
            String cptitle,
            int paytype) {
        super();
        this.id = id;
        this.userid = IsmartvActivator.getInstance().getDeviceToken();
        this.title = title;
        this.url = url;
        this.adlet_url = adlet_url;
        this.content_model = content_model;
        this.quality = quality;
        this.last_quality = last_quality;
        this.is_complex = is_complex;
        this.is_continue = is_continue;
        this.last_played_time = last_played_time;
        this.last_position = last_position;
        this.sub_url = sub_url;
        this.isnet = isNet;
        this.price = price;
        this.cpid = cpid;
        this.cpname = cpname;
        this.cptitle = cptitle;
        this.paytype = paytype;
    }

    public History(Cursor c) {
        id = c.getLong(c.getColumnIndex(DBFields.HistroyTable._ID));
        title = c.getString(c.getColumnIndex(DBFields.HistroyTable.TITLE));
        url = c.getString(c.getColumnIndex(DBFields.HistroyTable.URL));
        adlet_url = c.getString(c.getColumnIndex(DBFields.HistroyTable.ADLET_URL));
        content_model = c.getString(c.getColumnIndex(DBFields.HistroyTable.CONTENT_MODEL));
        quality = c.getInt(c.getColumnIndex(DBFields.HistroyTable.QUALITY));
        last_quality = c.getInt(c.getColumnIndex(DBFields.HistroyTable.LAST_QUALITY));
        is_complex =
                c.getInt(c.getColumnIndex(DBFields.HistroyTable.IS_COMPLEX)) == 0 ? false : true;
        is_continue =
                c.getInt(c.getColumnIndex(DBFields.HistroyTable.IS_CONTINUE)) == 0 ? false : true;
        last_played_time = c.getLong(c.getColumnIndex(DBFields.HistroyTable.LAST_PLAY_TIME));
        last_position = c.getLong(c.getColumnIndex(DBFields.HistroyTable.LAST_POSITION));
        sub_url = c.getString(c.getColumnIndex(DBFields.HistroyTable.SUB_URL));
        isnet = c.getString(c.getColumnIndex(DBFields.HistroyTable.ISNET));
        price = c.getInt(c.getColumnIndex(DBFields.HistroyTable.PRICE));
        cpid = c.getInt(c.getColumnIndex(DBFields.HistroyTable.CPID));
        cpname = c.getString(c.getColumnIndex(DBFields.HistroyTable.CPNAME));
        cptitle = c.getString(c.getColumnIndex(DBFields.HistroyTable.CPTITLE));
        paytype = c.getInt(c.getColumnIndex(DBFields.HistroyTable.PAYTYPE));
    }

    /** This is reverse order */
    @Override
    public int compareTo(History another) {
        return (int) (another.last_played_time - this.last_played_time);
    }
}
