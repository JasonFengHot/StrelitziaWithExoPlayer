package tv.ismar.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import tv.ismar.account.core.Md5;
import tv.ismar.account.core.http.HttpService;
import tv.ismar.account.core.rsa.RSACoder;
import tv.ismar.account.core.rsa.SkyAESTool2;
import tv.ismar.account.data.ResultEntity;

/**
 * Created by huaijie on 5/17/16.
 */
public class IsmartvActivator {
    private static final String TAG = "IsmartvActivator";
    private static final String DEFAULT_HOST = "http://sky.tvxio.com";
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
        sn = Md5.md5((deviceId + Build.SERIAL).trim());
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
                .baseUrl(DEFAULT_HOST)
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

    public ResultEntity execute() {
        if (isSignFileExists()) {
            return active();
        } else {
            return getLicence();
        }
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
        try {
            Response<ResponseBody> response = SKY_Retrofit.create(HttpService.class).trustGetlicence(fingerprint, sn, manufacture, "1")
                    .execute();
            if (response.errorBody() == null) {
                writeToSign(response.body().bytes());
                return active();
            } else {
                return null;
            }

        } catch (IOException e) {
            return null;
        }
    }

    private ResultEntity active() {
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
        if (TextUtils.isEmpty(apiDomain)) {
            ResultEntity resultEntity = execute();
            saveAccountInfo(resultEntity);
            return resultEntity.getDomain();
        } else {
            return apiDomain;
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
        if (TextUtils.isEmpty(logDomain)) {
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

    public void saveAccountInfo(ResultEntity resultEntity) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("device_token", resultEntity.getDevice_token());
        editor.putString("sn_token", resultEntity.getSn_Token());
        editor.putString("api_domain", resultEntity.getDomain());
        editor.putString("log_domain", resultEntity.getLog_Domain());
        editor.putString("ad_domain", resultEntity.getAd_domain());
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
        setzUserToken(authToken);
    }



    public void removeUserInfo() {
        mSharedPreferences.edit().putString("auth_token", "").commit();
        mSharedPreferences.edit().putString("zuser_token", "").commit();
        mSharedPreferences.edit().putString("username", "").commit();
    }

    public String getUsername() {
        return mSharedPreferences.getString("username", "");
    }

}
