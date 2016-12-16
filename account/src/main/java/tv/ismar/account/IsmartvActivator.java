package tv.ismar.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tv.ismar.account.core.Md5;
import tv.ismar.account.core.http.HttpService;
import tv.ismar.account.core.rsa.RSACoder;
import tv.ismar.account.core.rsa.SkyAESTool2;
import tv.ismar.account.data.ResultEntity;

/**
 * Created by huaijie on 5/17/16.
 */
public class IsmartvActivator {
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "IsmartvActivator";
    private static final String SKY_HOST = "http://sky.tvxio.com";
    private static final String SKY_HOST_TEST = "http://peachtest.tvxio.com";
    private static final String SIGN_FILE_NAME = "sign1";
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;

    private ResultEntity mResult;


    private String manufacture;
    private String kind;
    private String version;
    private String location;
    private String sn;
    private static Context mContext;
    private String fingerprint;
    private Retrofit SKY_Retrofit;
    private String deviceId;

    private static IsmartvActivator mInstance;

    public static IsmartvActivator getInstance() {
        if (mInstance == null) {
            mInstance = new IsmartvActivator();
        }
        return mInstance;
    }

    private SharedPreferences mSharedPreferences;

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static void initialize(Context context) {
        mContext = context;
    }

    private IsmartvActivator() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        manufacture = Build.BRAND.replace(" ", "_");
        kind = Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
        version = String.valueOf(getAppVersionCode());
        deviceId = getDeviceId();
        sn = generateSn();
        fingerprint = Md5.md5(sn);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

        SKY_Retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(SKY_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private String getDeviceId() {
        String deviceId = "test";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }

    public synchronized ResultEntity execute() {
        ResultEntity resultEntity;
        if (isSignFileExists()) {
            resultEntity = active();
        } else {
            resultEntity = getLicence();
        }

        if (resultEntity == null){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "激活失败！", Toast.LENGTH_SHORT).show();
                }
            });

            resultEntity = new ResultEntity();
        }

        return resultEntity;
    }

    private int getAppVersionCode() {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getAppVersionName() {
        String appVersionName = new String();
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            appVersionName = pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersionName;
    }

    private boolean isSignFileExists() {
        return mContext.getFileStreamPath(SIGN_FILE_NAME).exists();
    }


    private ResultEntity getLicence() {
        Log.d(TAG, "getLicence");
        try {
            Response<ResponseBody> response = SKY_Retrofit.create(HttpService.class).trustGetlicence(fingerprint, sn, manufacture, "1")
                    .execute();
            if (response.errorBody() == null) {
                String result = response.body().string();
                writeToSign(result.getBytes());
                return active();
            } else {
                return null;
            }

        } catch (IOException e) {
            return null;
        }
    }

    public ResultEntity active() {
        Log.d(TAG, "active");
        String sign = "ismartv=201415&kind=" + kind + "&sn=" + sn;
        String rsaEncryptResult = encryptWithPublic(sign);
        try {
            Response<ResultEntity> resultResponse = SKY_Retrofit.create(HttpService.class).
                    trustSecurityActive(sn, manufacture, kind, version, rsaEncryptResult,
                            fingerprint, "v3_0", getAndroidDevicesInfo())
                    .execute();
            if (resultResponse.errorBody() == null) {
                mResult = resultResponse.body();
                saveAccountInfo(mResult);
                return mResult;
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
    }


    private String getAndroidDevicesInfo() {
        try {
            JSONObject json = new JSONObject();
            String versionName = getAppVersionName();
            String serial = Build.SERIAL;
            String hh = Build.ID + "//" + Build.SERIAL;
            Md5.md5(Build.SERIAL + Build.ID);
            json.put("fingerprintE", Md5.md5(Build.SERIAL + Build.ID));
            json.put("fingerprintD", hh);
            json.put("versionName", versionName);
            json.put("serial", serial);
            json.put("deviceId", deviceId);
            return json.toString() + "///" + this.location;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void writeToSign(byte[] bytes) {
        FileOutputStream fs;
        try {
            fs = mContext.openFileOutput(SIGN_FILE_NAME, Context.MODE_WORLD_READABLE);
            fs.write(bytes);
            fs.flush();
            fs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String decryptSign(String key, String ContentPath) {
        String decryptResult = new String();
        File file = new File(ContentPath);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int count = fileInputStream.available();
                byte[] bytes = new byte[count];
                fileInputStream.read(bytes);
                fileInputStream.close();
                decryptResult = SkyAESTool2.decrypt(key.substring(0, 16), Base64.decode(bytes, Base64.URL_SAFE));
            } catch (Exception e) {
                file.delete();
            }
        }
        return decryptResult;
    }


    public String encryptWithPublic(String string) {
        String signPath = mContext.getFileStreamPath(SIGN_FILE_NAME).getAbsolutePath();
        String result = decryptSign(sn, signPath);
        String publicKey = result.split("\\$\\$\\$")[1];
        try {
            String input = Md5.md5(string);
            byte[] rsaResult = RSACoder.encryptByPublicKey(input.getBytes(), publicKey);
            return Base64.encodeToString(rsaResult, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getMacAddress() {
        return "mac_address";
    }

    public String getDeviceToken() {
        String deviceToken = mSharedPreferences.getString("device_token", "");
        if (TextUtils.isEmpty(deviceToken)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDevice_token();
        } else {
            return deviceToken;
        }
    }

    public String getApiDomain() {
        String apiDomain = mSharedPreferences.getString("api_domain", "");
        if (TextUtils.isEmpty(apiDomain)||apiDomain.equals("1.1.1.1")) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDomain();
        } else {
            return apiDomain;
        }
    }

    public String getUpgradeDomain() {
        String upgradeDomain = mSharedPreferences.getString("upgrade_domain", "");
        if (TextUtils.isEmpty(upgradeDomain)||upgradeDomain.equals("1.1.1.1")) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getUpgrade_domain();
        } else {
            return upgradeDomain;
        }
    }

    public String getAdDomain() {
        // 广告测试地址
//        return "124.42.65.66:8082";
        String adDomain = mSharedPreferences.getString("ad_domain", "");
        if (TextUtils.isEmpty(adDomain)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getAd_domain();
        } else {
            return adDomain;
        }
    }

    public String getLogDomain() {
        String logDomain = mSharedPreferences.getString("log_domain", "");
        if (TextUtils.isEmpty(logDomain)||logDomain.equals("1.1.1.1")) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getLog_Domain();
        } else {
            return logDomain;
        }
    }

    public String getAuthToken() {
        return mSharedPreferences.getString("auth_token", "");

    }

    public String getZUserToken() {
        return mSharedPreferences.getString("zuser_token", "");
    }

    public String getSnToken() {
        String snToken = mSharedPreferences.getString("sn_token", "");
        if (TextUtils.isEmpty(snToken)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDevice_token();
        } else {
            return snToken;
        }

    }

    public String getZDeviceToken() {
        String zdeviceToken = mSharedPreferences.getString("zdevice_token", "");
        if (TextUtils.isEmpty(zdeviceToken)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDevice_token();
        } else {
            return zdeviceToken;
        }

    }

    public void saveAccountInfo(ResultEntity resultEntity) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("device_token", resultEntity.getDevice_token());
        editor.putString("sn_token", resultEntity.getSn_Token());
        editor.putString("api_domain", resultEntity.getDomain());
        editor.putString("log_domain", resultEntity.getLog_Domain());
        editor.putString("ad_domain", resultEntity.getAd_domain());
        editor.putString("upgrade_domain", resultEntity.getUpgrade_domain());
        editor.putString("zdevice_token", resultEntity.getZdevice_token());
        editor.commit();
    }

    private void setAuthToken(String authToken) {
        mSharedPreferences.edit().putString("auth_token", authToken).commit();
    }


    private void setzUserToken(String authToken) {
        mSharedPreferences.edit().putString("zuser_token", authToken).commit();

    }

    private void setUsername(String username) {
        mSharedPreferences.edit().putString("username", username).commit();
    }


    public void saveUserInfo(String username, String authToken, String zUserhToken) {
        setUsername(username);
        setAuthToken(authToken);

        setzUserToken(zUserhToken);
    }

    public void setProvince(String name, String pinyin) {
        mSharedPreferences.edit().putString("province", name).commit();
        mSharedPreferences.edit().putString("province_py", pinyin).commit();

    }

    public HashMap<String, String> getProvince() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("province", mSharedPreferences.getString("province", ""));
        hashMap.put("province_py", mSharedPreferences.getString("province_py", ""));
        return hashMap;
    }

    public void setCity(String name, String geoId) {
        mSharedPreferences.edit().putString("city", name).commit();
        mSharedPreferences.edit().putString("geo_id", geoId).commit();
    }

    public HashMap<String, String> getCity() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("city", mSharedPreferences.getString("city", ""));
        hashMap.put("geo_id", mSharedPreferences.getString("geo_id", ""));
        return hashMap;
    }


    public void setIp(String ip) {
        mSharedPreferences.edit().putString("ip", ip).commit();
    }

    public String getIp() {
        return mSharedPreferences.getString("ip", "");
    }

    public void setIsp(String isp) {
        mSharedPreferences.edit().putString("isp", isp).commit();
    }

    public String getIsp() {
        return mSharedPreferences.getString("isp", "");
    }

    public void removeUserInfo() {
        mSharedPreferences.edit().putString("auth_token", "").commit();
        mSharedPreferences.edit().putString("zuser_token", "").commit();
        mSharedPreferences.edit().putString("username", "").commit();

        for (AccountChangeCallback callback : mAccountChangeCallbacks) {
            callback.onLogout();
        }
    }

    public String getUsername() {
        return mSharedPreferences.getString("username", "");
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public boolean isLogin() {
        return TextUtils.isEmpty(getUsername()) ? false : true;
    }

    private String generateSn() {
        String mysn;
        mysn = stringFromJNI();
        mysn = helloMd5(mysn);
        if ("noaddress".equals(mysn)) {
            mysn = Md5.md5(getDeviceId() + Build.SERIAL);
        }
        Log.d(TAG, "sn: " + mysn);
        return mysn;
    }

    public native String stringFromJNI();


    public native String helloMd5(String str);

    public interface AccountChangeCallback {
        void onLogout();
    }

    private List<AccountChangeCallback> mAccountChangeCallbacks = new ArrayList<>();

    public void addAccountChangeListener(AccountChangeCallback callback) {
        mAccountChangeCallbacks.add(callback);
    }

    public void removeAccountChangeListener(AccountChangeCallback callback) {
        mAccountChangeCallbacks.remove(callback);
    }

}
