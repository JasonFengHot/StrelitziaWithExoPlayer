package tv.ismar.app.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/** Created by beaver on 16-9-6. */
public class HistoryFavoriteProvider extends ContentProvider {

    public static final String AUTHORITY = "tv.ismar.daisy.provider.hf";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.daisy.hf";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.daisy.hf";

    private static final int HISTORIES = 1;
    private static final int HISTORIES_ID = 2;
    private static final int FAVORITES = 3;
    private static final int FAVORITES_ID = 4;
    private static final int QUALITIES = 5;
    private static final int QUALITIES_ID = 6;
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "histories", HISTORIES);
        sUriMatcher.addURI(AUTHORITY, "histories/#", HISTORIES_ID);

        // 这里要增加另一张表的匹配项
        sUriMatcher.addURI(AUTHORITY, "favorites", FAVORITES);
        sUriMatcher.addURI(AUTHORITY, "favorites/#", FAVORITES_ID);
        // 这里要增加另一张表的匹配项
        sUriMatcher.addURI(AUTHORITY, "qualities", QUALITIES);
        sUriMatcher.addURI(AUTHORITY, "qualities/#", QUALITIES_ID);
    }

    private DBHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy;
        // 这里要对不同表的匹配结果做不同处理
        switch (sUriMatcher.match(uri)) {
            case HISTORIES_ID:
                // Adding the ID to the original query
                qb.appendWhere(DBHelper.DBFields.HistroyTable._ID + "=" + uri.getLastPathSegment());
            case HISTORIES:
                qb.setTables(DBHelper.DBFields.HistroyTable.TABLE_NAME);
                // If no sort order is specified use the default
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = "last_played_time desc";
                } else {
                    orderBy = sortOrder;
                }
                break;
            case FAVORITES_ID:
                qb.appendWhere(
                        DBHelper.DBFields.FavoriteTable._ID + "=" + uri.getLastPathSegment());
            case FAVORITES:
                qb.setTables(DBHelper.DBFields.FavoriteTable.TABLE_NAME);
                // If no sort order is specified use the default
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = "_id desc";
                } else {
                    orderBy = sortOrder;
                }
                break;
            case QUALITIES_ID:
                qb.appendWhere(DBHelper.DBFields.QualityTable._ID + "=" + uri.getLastPathSegment());
            case QUALITIES:
                qb.setTables(DBHelper.DBFields.QualityTable.TABLE_NAME);
                // If no sort order is specified use the default
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = "_id desc";
                } else {
                    orderBy = sortOrder;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
