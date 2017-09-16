package tv.ismar.app.entity;

import android.database.Cursor;

import java.io.Serializable;

import tv.ismar.app.db.DBHelper.DBFields;

public class Favorite implements Serializable {

    private static final long serialVersionUID = -3481459533007774584L;

    public long id;
    public String title;
    public String url;
    public String adlet_url;
    public int quality;
    public boolean is_complex;
    public String isnet;
    public String content_model;
    public int cpid;
    public String cpname;
    public String cptitle;
    public int paytype;

    public Favorite() {
        super();
    }

    public Favorite(
            long id,
            String title,
            String url,
            String adlet_url,
            int quality,
            boolean is_complex,
            String content_model,
            String isNet,
            int cpid,
            String cpname,
            String cptitle,
            int paytype) {
        super();
        this.id = id;
        this.title = title;
        this.url = url;
        this.adlet_url = adlet_url;
        this.quality = quality;
        this.is_complex = is_complex;
        this.content_model = content_model;
        this.isnet = isNet;
        this.cpid = cpid;
        this.cpname = cpname;
        this.cptitle = cptitle;
        this.paytype = paytype;
    }

    public Favorite(Cursor c) {
        id = c.getLong(c.getColumnIndex(DBFields.FavoriteTable._ID));
        title = c.getString(c.getColumnIndex(DBFields.FavoriteTable.TITLE));
        url = c.getString(c.getColumnIndex(DBFields.FavoriteTable.URL));
        adlet_url = c.getString(c.getColumnIndex(DBFields.FavoriteTable.ADLET_URL));
        quality = c.getInt(c.getColumnIndex(DBFields.FavoriteTable.QUALITY));
        is_complex = c.getInt(c.getColumnIndex(DBFields.FavoriteTable.IS_COMPLEX)) != 0;
        content_model = c.getString(c.getColumnIndex(DBFields.FavoriteTable.CONTENT_MODEL));
        isnet = c.getString(c.getColumnIndex(DBFields.FavoriteTable.ISNET));
        cpid = c.getInt(c.getColumnIndex(DBFields.FavoriteTable.CPID));
        cpname = c.getString(c.getColumnIndex(DBFields.FavoriteTable.CPNAME));
        cptitle = c.getString(c.getColumnIndex(DBFields.FavoriteTable.CPTITLE));
        paytype = c.getInt(c.getColumnIndex(DBFields.FavoriteTable.PAYTYPE));
    }
}
