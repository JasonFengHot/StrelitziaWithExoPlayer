package cn.ismartv.helperpage.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by fenghb on 14-7-14.
 */
public class CdnCacheLoader extends CursorLoader {
    private static final String TAG = "CacheLoader";
    private Context context;

    public CdnCacheLoader(Context context) {
        super(context);
        this.context = context;
    }

    public CdnCacheLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        this.context = context;
    }

    @Override
    public Cursor loadInBackground() {
        return super.loadInBackground();
    }
}
