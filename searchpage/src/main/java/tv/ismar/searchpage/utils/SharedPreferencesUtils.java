package tv.ismar.searchpage.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    // sp = getSharedPreferences("config", 0);
    // Editor edit = sp.edit();
    //
    // edit.putBoolean("isUpdate", mAutoUpDate.isToggle());
    //
    // edit.commit();
    // sp的名字
    public static final String SP_NAME = "config";
    private static SharedPreferences sp;

    public static void saveBoolean(Context context, String key, boolean value) {

        if (sp == null) sp = context.getSharedPreferences(SP_NAME, 0);

        sp.edit().putBoolean(key, value).commit();
    }

    public static void saveInt(Context context, String key, int value) {

        if (sp == null) sp = context.getSharedPreferences(SP_NAME, 0);

        sp.edit().putInt(key, value).commit();
    }

    public static void saveString(Context context, String key, String value) {

        if (sp == null) sp = context.getSharedPreferences(SP_NAME, 0);

        sp.edit().putString(key, value).commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        if (sp == null) sp = context.getSharedPreferences(SP_NAME, 0);

        return sp.getInt(key, defValue);
    }

    public static String getString(Context context, String key, String defValue) {
        if (sp == null) sp = context.getSharedPreferences(SP_NAME, 0);

        return sp.getString(key, defValue);
    }

    // SharedPreferences sp = getSharedPreferences("config", 0);
    //
    // boolean result = sp.getBoolean("isUpdate", false);

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        if (sp == null) sp = context.getSharedPreferences(SP_NAME, 0);

        return sp.getBoolean(key, defValue);
    }
}
