//package tv.ismar.app.update;
//
//import android.app.ActivityManager;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//
//import com.google.gson.Gson;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.HashMap;
//
//import tv.ismar.daisy.AppConstant;
//import tv.ismar.daisy.core.SimpleRestClient;
//import tv.ismar.daisy.core.client.IsmartvUrlClient;
//import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
//import tv.ismar.daisy.ui.activity.TVGuideActivity;
//import tv.ismar.daisy.utils.HardwareUtils;
//
///**
// * Created by huaijie on 10/22/15.
// */
//public class AppUpdateUtilsV2 {
//    public static final String SELF_APP_NAME = "Daisy.apk";
//    private static final String TAG = "AppUpdateUtilsV2";
//    private static final String APP_UPDATE_API_V2 = "/api/v2/upgrade/";
//    private static final String PLAYER_ACTIVITY_NAME = "tv.ismar.daisy.PlayerActivity";
//
//
//    private Context mContext;
//    private boolean checkDowload = false;
//    private String path;
//    private String apiHost;
//
//    private static AppUpdateUtilsV2 mInstance;
//
//    private AppUpdateUtilsV2(Context context) {
//        mContext = context;
//    }
//
//    public static AppUpdateUtilsV2 getInstance(Context context) {
//        if (mInstance == null) {
//            mInstance = new AppUpdateUtilsV2(context);
//        }
//        return mInstance;
//    }
//
//
//    public void checkAppUpdate(String host) {
//        apiHost = host;
//
//        //当前apk版本号
//        int currentApkVersionCode = fetchVersionCode();
//
//        //请求qpi
//        String api = host + APP_UPDATE_API_V2;
//
//        //地理位置信息
//        String location = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.PROVINCE_PY);
//
//        //请求参数
//        HashMap<String, String> paramters = new HashMap<String,String>();
//        paramters.put("sn", SimpleRestClient.sn_token);
//
//        if(TVGuideActivity.brandName != null){
//            paramters.put("manu", TVGuideActivity.brandName);
//        }else{
//            paramters.put("manu", "sharp");
//        }
//
//        paramters.put("app", "sky");
//
//        //LCD-UF30A
//        paramters.put("modelname", HardwareUtils.getModelName());
////        paramters.put("modelname", "LCD-UF30A");
//        paramters.put("loc", location);
//        paramters.put("ver", String.valueOf(currentApkVersionCode));
//
//
//        new IsmartvUrlClient().doNormalRequest(IsmartvUrlClient.Method.GET, api, paramters, new IsmartvUrlClient.CallBack() {
//            @Override
//            public void onSuccess(String result) {
//                VersionInfoV2Entity versionInfoV2Entity = new Gson().fromJson(result, VersionInfoV2Entity.class);
//                if (versionInfoV2Entity != null) {
//                    updateProcess(versionInfoV2Entity);
//                }
//            }
//
//            @Override
//            public void onFailed(Exception exception) {
//            }
//        });
//    }
//
//    private void updateProcess(VersionInfoV2Entity versionInfoV2Entity) {
//
//
//        Log.i(TAG, "server version code ---> " + versionInfoV2Entity.getApplication().getVersion());
//        path = mContext.getFilesDir().getAbsolutePath();
//        File apkFile = new File(path, SELF_APP_NAME);
//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            android.util.Log.e(TAG, "can't find this application!!!");
//        }
//        android.util.Log.i(TAG, "local version code ---> " + packageInfo.versionCode);
//        if (packageInfo.versionCode < Integer.parseInt(versionInfoV2Entity.getApplication().getVersion())) {
//            if (apkFile.exists()) {
//                String serverMd5Code = versionInfoV2Entity.getApplication().getMd5();
//                String localMd5Code = HardwareUtils.getMd5ByFile(apkFile);
//                android.util.Log.d(TAG, "local md5 ---> " + localMd5Code);
//                android.util.Log.d(TAG, "server md5 ---> " + serverMd5Code);
//                String currentActivityName = getCurrentActivityName(mContext);
//
//                int apkVersionCode = getApkVersionCode(mContext, apkFile.getAbsolutePath());
//                int serverVersionCode = Integer.parseInt(versionInfoV2Entity.getApplication().getVersion());
//
//                android.util.Log.i(TAG, "download apk version code: " + apkVersionCode);
//                android.util.Log.i(TAG, "server apk version code: " + serverVersionCode);
//
//                if (serverMd5Code.equalsIgnoreCase(localMd5Code) && !currentActivityName.equals(PLAYER_ACTIVITY_NAME)
//                        && apkVersionCode == serverVersionCode) {
//                    android.util.Log.i(TAG, "send install broadcast ...");
//                    Bundle bundle = new Bundle();
//                    bundle.putStringArrayList("msgs", versionInfoV2Entity.getApplication().getUpdate());
//                    bundle.putString("path", apkFile.getAbsolutePath());
//                    sendUpdateBroadcast(mContext, bundle);
//                } else {
//                    if (apkFile.exists()) {
//                        apkFile.delete();
//                    }
//                    String downloadUrl = versionInfoV2Entity.getApplication().getUrl();
//                    downloadAPK(mContext, downloadUrl);
//                }
//            } else {
//                if (apkFile.exists()) {
//                    apkFile.delete();
//                }
//                String downloadUrl = versionInfoV2Entity.getApplication().getUrl();
//                downloadAPK(mContext, downloadUrl);
//            }
//        } else {
//            if (apkFile.exists()) {
//                apkFile.delete();
//            }
//        }
//
//    }
//
//    private int fetchVersionCode() {
//        int versionCode = 0;
//
//        try {
//            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
//            versionCode = packageInfo.versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            android.util.Log.e(TAG, "can't find this application!!!");
//        }
//        return versionCode;
//    }
//
//    private void downloadAPK(final Context mContext, final String downloadUrl) {
//        checkDowload = false;
//        new Thread() {
//            @Override
//            public void run() {
//                android.util.Log.d(TAG, "downloadAPK is running...");
//                File fileName = null;
//                try {
//                    int byteread;
//                    URL url = new URL(downloadUrl);
//                    fileName = new File(path, SELF_APP_NAME);
//                    if (!fileName.exists())
//                        fileName.createNewFile();
//                    URLConnection conn = url.openConnection();
//                    InputStream inStream = conn.getInputStream();
//                    FileOutputStream fs = mContext.openFileOutput(SELF_APP_NAME, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
//                    byte[] buffer = new byte[1024];
//                    while ((byteread = inStream.read(buffer)) != -1) {
//                        fs.write(buffer, 0, byteread);
//                    }
//                    inStream.close();
//                    fs.flush();
//                    fs.close();
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                android.util.Log.d(TAG, "downloadAPK is end...");
//                if (!checkDowload) {
//                    Message message = messageHandler.obtainMessage();
//                    message.obj = apiHost;
//                    messageHandler.sendMessage(message);
//                }
//
//            }
//        }.start();
//    }
//
//
//    private void sendUpdateBroadcast(Context context, Bundle bundle) {
//        Intent intent = new Intent();
//        intent.setAction(AppConstant.APP_UPDATE_ACTION);
//        intent.putExtra("data", bundle);
//        context.sendBroadcast(intent);
//    }
//
//    /**
//     * get current activity task the top activity
//     *
//     * @param context
//     * @return
//     */
//    public String getCurrentActivityName(Context context) {
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
//        android.util.Log.i(TAG, "getCurrentActivityName : pkg --->" + cn.getPackageName());
//        android.util.Log.i(TAG, "getCurrentActivityName : cls ---> " + cn.getClassName());
//        return cn.getClassName();
//    }
//
//    private int getApkVersionCode(Context context, String path) {
//        final PackageManager pm = context.getPackageManager();
//        PackageInfo info = pm.getPackageArchiveInfo(path, 0);
//        int versionCode;
//        try {
//            versionCode = info.versionCode;
//        } catch (Exception e) {
//            versionCode = 0;
//        }
//        return versionCode;
//    }
//
//    private Handler messageHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            String host = (String) msg.obj;
//            checkAppUpdate(host);
//        }
//    };
//}
