package tv.ismar.app.util;

import android.text.TextUtils;

/**
 * Created by beaver on 16-8-22.
 */
public class Utils {

    public static boolean isEmptyText(String str) {
        return TextUtils.isEmpty(str) || str.equalsIgnoreCase("null");
    }

}
