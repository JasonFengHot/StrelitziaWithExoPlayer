package tv.ismar.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;


public class SPUtils {

    private static SPUtils helper;
    private SharedPreferences mSharedPreferences = null;
    private Context ctx;
    private SharedPreferences.Editor editor;

    private SPUtils(Context context) {
        this.ctx = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.editor = mSharedPreferences.edit();
    }

    public static void init(Context context) {
        if (helper == null) {
            helper = new SPUtils(context);
        }
    }

    public static SPUtils getInstance() {
        if (helper == null) {
            throw new NullPointerException("NOT INIT sphelper,please call init in app first");
        }
        return helper;
    }

    public static void putValue(String key, Object value) {
        if (value instanceof String) {
            getInstance().editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            getInstance().editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            getInstance().editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            getInstance().editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            getInstance().editor.putLong(key, (Long) value);
        } else {
            getInstance().editor.putString(key, value.toString());
        }
        getInstance().editor.commit();
    }

    public static Object getValue(String key, Object defaultKey) {
        if (defaultKey instanceof String) {
            return getInstance().mSharedPreferences.getString(key, (String) defaultKey);
        } else if (defaultKey instanceof Integer) {
            return getInstance().mSharedPreferences.getInt(key, (Integer) defaultKey);
        } else if (defaultKey instanceof Boolean) {
            return getInstance().mSharedPreferences.getBoolean(key, (Boolean) defaultKey);
        } else if (defaultKey instanceof Float) {
            return getInstance().mSharedPreferences.getFloat(key, (Float) defaultKey);
        } else if (defaultKey instanceof Long) {
            return getInstance().mSharedPreferences.getLong(key, (Long) defaultKey);
        }
        return null;
    }

    public static void remove(String key) {
        getInstance().editor.remove(key).commit();
    }

    public static void clear() {
        getInstance().editor.clear().commit();
    }

    public static float[] getFloatArray(SharedPreferences pref, String key) {
        float[] array = null;
        String s = pref.getString(key, null);
        if (s != null) {
            try {
                JSONArray json = new JSONArray(s);
                array = new float[json.length()];
                for (int i = 0; i < array.length; i++) array[i] = (float) json.getDouble(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }
}
