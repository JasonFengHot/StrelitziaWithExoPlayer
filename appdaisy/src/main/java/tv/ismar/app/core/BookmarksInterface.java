package tv.ismar.app.core;

import android.content.Context;

/**
 * Created by huibin on 8/24/16.
 */
public interface BookmarksInterface {
    void addBookmarks(Context context, Object item);

    void removeBookmarks(Context context, Object item);

}
