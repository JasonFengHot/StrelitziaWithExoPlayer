package tv.ismar.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;

import tv.ismar.app.entity.DBQuality;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.History;

public class DBHelper extends SQLiteOpenHelper {

    /** database version. this may changed with future update. */
    private static final int DATABASE_VERSION = 5;
    /** database file name. */
    private static final String DATABASE_NAME = "daisy.db";
    /** SQLite command string. use to create history_table and favorite_table. */
    private static final String CREATE_HISTORY_TABLE =
            "CREATE TABLE IF NOT EXISTS 'history_table' "
                    + "('_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'title' TEXT NOT NULL, 'url' TEXT NOT NULL, 'adlet_url' TEXT, "
                    + "'last_played_time' INTEGER DEFAULT(0), 'last_position' INTEGER DEFAULT(0), 'content_model' TEXT NOT NULL, 'quality' INTEGER DEFAULT(1), "
                    + "'last_quality' INTEGER DEFAULT(1), 'is_complex' INTEGER DEFAULT(0), 'is_continue' INTEGER DEFAULT(0), 'sub_url' TEXT,'isnet' TEXT NOT NULL,'price' INTEGER DEFAULT(0),'cpid' INTEGER DEFAULT(1),'cpname' TEXT,'cptitle' TEXT,'paytype' INTEGER DEFAULT(1))";

    private static final String CREATE_FAVORITE_TABLE =
            "CREATE TABLE IF NOT EXISTS 'favorite_table' "
                    + "('_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'title' TEXT NOT NULL, 'url' TEXT NOT NULL, 'content_model' TEXT, "
                    + "'adlet_url' TEXT, 'quality' INTEGER DEFAULT(1), 'is_complex' INTEGER DEFAULT(0),'isnet' TEXT NOT NULL,'cpid' INTEGER DEFAULT(1),'cpname' TEXT,'cptitle' TEXT,'paytype' INTEGER DEFAULT(1))";
    private static final String CREATE_QUALITY_TABLE =
            "CREATE TABLE IF NOT EXISTS 'quality_table' "
                    + "('_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'url' TEXT NOT NULL,  'quality' INTEGER DEFAULT(1))";
    private Context mContext;
    private SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_FAVORITE_TABLE);
        db.execSQL(CREATE_QUALITY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // delete old tables
        if (newVersion != oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DBFields.HistroyTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DBFields.FavoriteTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DBFields.QualityTable.TABLE_NAME);

            db.execSQL(CREATE_HISTORY_TABLE);
            db.execSQL(CREATE_FAVORITE_TABLE);
            db.execSQL(CREATE_QUALITY_TABLE);
        }
    }

    /**
     * Use to query all history records. sorted by last_played_time column.
     *
     * @return an ArrayList contains all History objects.
     */
    public ArrayList<History> getAllHistories(String isnet) {
        ArrayList<History> historyList = new ArrayList<History>();
        Cursor cur =
                db.query(
                        DBFields.HistroyTable.TABLE_NAME,
                        null,
                        DBFields.HistroyTable.ISNET + "= ?",
                        new String[] {isnet},
                        null,
                        null,
                        " last_played_time desc");
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    historyList.add(new History(cur));
                } while (cur.moveToNext());
            }
            cur.close();
            cur = null;
        }
        return historyList;
    }

    /**
     * Use to query all favorite record.
     *
     * @return an ArrayList contains all Favorite objects.
     */
    public ArrayList<Favorite> getAllFavorites(String isnet) {
        // Cursor cur = db.query(DBFields.FavoriteTable.TABLE_NAME, null, DBFields.FavoriteTable.URL
        // + " = ? and " + DBFields.FavoriteTable.ISNET + "= ?", new String[]{url,isnet}, null,
        // null, " _id desc");
        ArrayList<Favorite> favoriteList = new ArrayList<Favorite>();
        Cursor cur =
                db.query(
                        DBFields.FavoriteTable.TABLE_NAME,
                        null,
                        DBFields.FavoriteTable.ISNET + "= ?",
                        new String[] {isnet},
                        null,
                        null,
                        " _id desc");
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    favoriteList.add(new Favorite(cur));
                } while (cur.moveToNext());
            }
            cur.close();
            cur = null;
        }
        return favoriteList;
    }

    /**
     * Only use to insert a new row to given table. Generally, you do not need to use this method
     * directly.
     *
     * @param cv wrapper data to insert.
     * @param table the table name which you want to insert to.
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(ContentValues cv, String table, int limit) {
        long result = 0;
        db.beginTransaction();
        try {
            if (limit > 0 && table.equals(DBFields.HistroyTable.TABLE_NAME)) {
                Cursor cur =
                        db.query(
                                table,
                                new String[] {"_id"},
                                null,
                                null,
                                null,
                                null,
                                " last_played_time asc",
                                null);
                if (cur != null && cur.getCount() >= limit) {
                    cur.moveToFirst();
                    long id = cur.getLong(cur.getColumnIndex("_id"));
                    db.delete(table, " _id = ? ", new String[] {String.valueOf(id)});
                    cur.close();
                }
            }
            result = db.insert(table, null, cv);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    /**
     * Use to update a record of history_table.
     *
     * @param history the history object. id must not be zero.
     */
    public void updateHistory(History history) {
        ContentValues cv = new ContentValues();
        cv.put(DBFields.HistroyTable._ID, history.id);
        cv.put(DBFields.HistroyTable.TITLE, history.title);
        cv.put(DBFields.HistroyTable.URL, history.url);
        cv.put(DBFields.HistroyTable.ADLET_URL, history.adlet_url);
        cv.put(DBFields.HistroyTable.CONTENT_MODEL, history.content_model);
        cv.put(DBFields.HistroyTable.QUALITY, history.quality);
        cv.put(DBFields.HistroyTable.LAST_QUALITY, history.last_quality);
        cv.put(DBFields.HistroyTable.IS_COMPLEX, history.is_complex ? 1 : 0);
        cv.put(DBFields.HistroyTable.IS_CONTINUE, history.is_continue ? 1 : 0);
        cv.put(DBFields.HistroyTable.LAST_PLAY_TIME, history.last_played_time);
        cv.put(DBFields.HistroyTable.LAST_POSITION, history.last_position);
        cv.put(DBFields.HistroyTable.ISNET, history.isnet);
        cv.put(DBFields.HistroyTable.SUB_URL, history.sub_url);
        cv.put(DBFields.HistroyTable.PRICE, history.price);
        cv.put(DBFields.HistroyTable.CPID, history.cpid);
        cv.put(DBFields.HistroyTable.CPNAME, history.cpname);
        cv.put(DBFields.HistroyTable.CPTITLE, history.cptitle);
        cv.put(DBFields.HistroyTable.PAYTYPE, history.paytype);
        db.update(
                DBFields.HistroyTable.TABLE_NAME,
                cv,
                "_id = ?",
                new String[] {String.valueOf(history.id)});
    }

    public void updateFavorite(Favorite favorite) {
        ContentValues cv = new ContentValues();
        cv.put(DBFields.FavoriteTable._ID, favorite.id);
        cv.put(DBFields.FavoriteTable.TITLE, favorite.title);
        cv.put(DBFields.FavoriteTable.URL, favorite.url);
        cv.put(DBFields.FavoriteTable.ADLET_URL, favorite.adlet_url);
        cv.put(DBFields.FavoriteTable.QUALITY, favorite.quality);
        cv.put(DBFields.FavoriteTable.IS_COMPLEX, favorite.is_complex ? 1 : 0);
        cv.put(DBFields.FavoriteTable.CONTENT_MODEL, favorite.content_model);
        cv.put(DBFields.FavoriteTable.ISNET, favorite.isnet);
        cv.put(DBFields.HistroyTable.CPID, favorite.cpid);
        cv.put(DBFields.HistroyTable.CPNAME, favorite.cpname);
        cv.put(DBFields.HistroyTable.CPTITLE, favorite.cptitle);
        cv.put(DBFields.HistroyTable.PAYTYPE, favorite.paytype);
        db.update(
                DBFields.FavoriteTable.TABLE_NAME,
                cv,
                " _id = ?",
                new String[] {String.valueOf(favorite.id)});
    }

    public void updateQualtiy(DBQuality quality) {
        ContentValues cv = new ContentValues();
        cv.put(DBFields.QualityTable._ID, quality.id);
        cv.put(DBFields.QualityTable.URL, quality.url);
        cv.put(DBFields.QualityTable.QUALITY, quality.quality);

        db.update(
                DBFields.QualityTable.TABLE_NAME,
                cv,
                " _id = ?",
                new String[] {String.valueOf(quality.id)});
    }

    public History queryHistoryByUrl(String url, String isnet) {
        History history = null;
        String[] aa = url.split("/");
        String pk = aa[aa.length - 1];
        Cursor cur =
                db.query(
                        DBFields.HistroyTable.TABLE_NAME,
                        null,
                        DBFields.HistroyTable.URL
                                + " like ? and "
                                + DBFields.HistroyTable.ISNET
                                + "= ?",
                        new String[] {"%" + pk + "%", isnet},
                        null,
                        null,
                        " _id desc");
        if (cur != null) {
            if (cur.moveToFirst()) {
                history = new History(cur);
            }
            cur.close();
            cur = null;
        }
        return history;
    }

    public Favorite queryFavoriteByUrl(String url, String isnet) {
        Favorite favorite = null;
        Cursor cur =
                db.query(
                        DBFields.FavoriteTable.TABLE_NAME,
                        null,
                        DBFields.FavoriteTable.URL
                                + " = ? and "
                                + DBFields.FavoriteTable.ISNET
                                + "= ?",
                        new String[] {url, isnet},
                        null,
                        null,
                        " _id desc");
        if (cur != null) {
            if (cur.moveToFirst()) {
                favorite = new Favorite(cur);
            }
            cur.close();
            cur = null;
        }
        return favorite;
    }

    public DBQuality queryQualtiy() {
        DBQuality quality = null;
        Cursor cur =
                db.query(
                        DBFields.QualityTable.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        " _id desc");
        if (cur != null) {
            if (cur.moveToFirst()) {
                quality = new DBQuality(cur);
            }
            cur.close();
            cur = null;
        }
        return quality;
    }

    public int delete(String table, String url, String isnet) {
        if (url == null) {
            return db.delete(table, null, null);
        }
        return db.delete(
                table,
                " url = ? and " + DBFields.FavoriteTable.ISNET + "=?",
                new String[] {url, isnet});
    }

    public int deleteHistory(String table, String url, String isnet) {
        if (url == null) {
            return db.delete(table, null, null);
        }
        return db.delete(
                table,
                " url = ? and " + DBFields.HistroyTable.ISNET + "=?",
                new String[] {url, isnet});
    }

    public void releaseDB() {
        db.close();
        db = null;
    }

    public interface DBFields {
        /** Table columns about the history table. */
        interface HistroyTable extends BaseColumns {
            Uri CONTENT_URI =
                    Uri.parse("content://" + HistoryFavoriteProvider.AUTHORITY + "/histories");
            String TABLE_NAME = "history_table";
            String TITLE = "title";
            String URL = "url";
            String ADLET_URL = "adlet_url";
            String CONTENT_MODEL = "content_model";
            String QUALITY = "quality";
            String LAST_QUALITY = "last_quality";
            String IS_COMPLEX = "is_complex";
            String IS_CONTINUE = "is_continue";
            String LAST_PLAY_TIME = "last_played_time";
            String LAST_POSITION = "last_position";
            String SUB_URL = "sub_url";
            String ISNET = "isnet";
            String PRICE = "price";
            String CPID = "cpid";
            String CPNAME = "cpname";
            String CPTITLE = "cptitle";
            String PAYTYPE = "paytype";
        }

        interface FavoriteTable extends BaseColumns {
            Uri CONTENT_URI =
                    Uri.parse("content://" + HistoryFavoriteProvider.AUTHORITY + "/favorites");
            String TABLE_NAME = "favorite_table";
            String TITLE = "title";
            String URL = "url";
            String ADLET_URL = "adlet_url";
            String IS_COMPLEX = "is_complex";
            String QUALITY = "quality";
            String CONTENT_MODEL = "content_model";
            String ISNET = "isnet";
            String CPID = "cpid";
            String CPNAME = "cpname";
            String CPTITLE = "cptitle";
            String PAYTYPE = "paytype";
        }

        interface QualityTable extends BaseColumns {
            Uri CONTENT_URI =
                    Uri.parse("content://" + HistoryFavoriteProvider.AUTHORITY + "/qualities");
            String TABLE_NAME = "quality_table";
            String URL = "url";
            String QUALITY = "quality";
        }
    }
}
