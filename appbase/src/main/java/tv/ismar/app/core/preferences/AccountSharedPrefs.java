package tv.ismar.app.core.preferences;

import android.content.SharedPreferences;
import android.text.TextUtils;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.db.location.ProvinceTable;

/**
 * Created by huaijie on 8/3/15.
 */
public class AccountSharedPrefs {
    private static final String TAG = "AccountSharedPrefs";

    private static final String SHARED_PREFS_NAME = "account";

    public static final String APP_UPDATE_DOMAIN = "app_update_domain";
    public static final String LOG_DOMAIN = "log_domain";
    public static final String API_DOMAIN = "api_domain";
    public static final String CARNATION_DOMAIN = "carnation_domain";
    public static final String ADVERTISEMENT_DOMAIN = "advertisement_domain";

    public static final String DEVICE_TOKEN = "device_token";
    public static final String SN_TOKEN = "sn_token";
    public static final String ZDEVICE_TOKEN = "zdevice_token";
    public static final String ACESS_TOKEN = "acess_token";
    public static final String ZUSER_TOKEN = "zuser_token";
    public static final String PACKAGE_INFO = "package_info";
    public static final String EXPIRY_DATE = "expiry_date";

    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String PROVINCE_PY = "province_py";
    public static final String ISP = "isp";
    public static final String IP = "ip";
    public static final String GEO_ID = "geo_id";
    public static final String FIRST_USE = "first_use";
    public static final String WEATHER_INFO = "weather_info";

    private static AccountSharedPrefs instance;


    private static SharedPreferences mSharedPreferences;

    public static AccountSharedPrefs getInstance() {
        if (instance == null) {
            instance = new AccountSharedPrefs();
        }
        return instance;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public static void initialize(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
        mSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public static String getSharedPrefs(String key) {
        if (mSharedPreferences == null) {
            return "";
        }
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        return mSharedPreferences.getString(key, "");
    }

    public static void setSharedPrefs(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    private static SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(PROVINCE)) {
                changeProvincePY(sharedPreferences.getString(PROVINCE, ""));
            }
        }
    };

    private static void changeProvincePY(String provinceName) {
        ProvinceTable provinceTable = new Select().from(ProvinceTable.class)
                .where(ProvinceTable.PROVINCE_NAME + " = ?", provinceName).executeSingle();
        if (provinceTable != null) {
            setSharedPrefs(AccountSharedPrefs.PROVINCE_PY, provinceTable.pinyin);
        }
    }
}
