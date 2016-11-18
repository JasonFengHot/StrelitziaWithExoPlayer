package tv.ismar.searchpage.core.update;//package cn.ismartv.Jasmine.core.update;
//
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//
//import cn.ismartv.Jasmine.MainApplication;
//import cn.ismartv.Jasmine.core.event.AnswerAvailableEvent;
//import cn.ismartv.Jasmine.core.http.HttpAPI;
//import cn.ismartv.Jasmine.core.http.HttpManager;
//import cn.ismartv.Jasmine.util.DeviceUtil;
//import retrofit2.Response;
//
//
///**
// * Created by huaijie on 10/22/15.
// */
//public class AppUpdateUtilsV2 extends Handler {
//    public static final String SELF_APP_NAME = "IsmartvJasmine.apk";
//    private static final String TAG = "AppUpdateUtilsV2";
//
//
//    private Context mContext;
//    private boolean checkDowload = false;
//    private String path;
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
//    public void checkAppUpdate() {
//        //当前apk版本号
//        int currentApkVersionCode = fetchVersionCode();
//        //地理位置信息
//        String location = MainApplication.getLocationPY();
//        String sn = MainApplication.getSnToken();
//        String app = "Jasmine";
//        String ver = String.valueOf(currentApkVersionCode);
//        String manu = "sharp";
//        String model = DeviceUtil.getModelName();
//
//
//        HttpManager.getInstance().resetAdapter_APP_UPDATE.create(HttpAPI.CheckAppUpdate.class).doRequest(sn, manu, app, model, location, ver).enqueue(new retrofit2.Callback<VersionInfoV2Entity>() {
//            @Override
//            public void onResponse(Response<VersionInfoV2Entity> response) {
//                if (response.errorBody() == null) {
//                    VersionInfoV2Entity versionInfoV2Entity = response.body();
//                    if (versionInfoV2Entity != null) {
//                        updateProcess(versionInfoV2Entity);
//                    }
//                } else {
////                    EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
////                EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));
//            }
//        });
//
//    }
//
//    private void updateProcess(VersionInfoV2Entity versionInfoV2Entity) {
//        Log.i(TAG, "server version code ---> " + versionInfoV2Entity.getApplication().getVersion());
//        path = mContext.getFilesDir().getAbsolutePath();
//        File apkFile = new File(path, SELF_APP_NAME);
//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "can't find this application!!!");
//        }
//        Log.i(TAG, "local version code ---> " + packageInfo.versionCode);
//        if (packageInfo.versionCode < Integer.parseInt(versionInfoV2Entity.getApplication().getVersion())) {
//            if (apkFile.exists()) {
//                String serverMd5Code = versionInfoV2Entity.getApplication().getMd5();
//                String localMd5Code = DeviceUtil.getMd5ByFile(apkFile);
//                Log.d(TAG, "local md5 ---> " + localMd5Code);
//                Log.d(TAG, "server md5 ---> " + serverMd5Code);
//
//                int apkVersionCode = getApkVersionCode(mContext, apkFile.getAbsolutePath());
//                int serverVersionCode = Integer.parseInt(versionInfoV2Entity.getApplication().getVersion());
//
//                Log.i(TAG, "download apk version code: " + apkVersionCode);
//                Log.i(TAG, "server apk version code: " + serverVersionCode);
//
//                if (serverMd5Code.equalsIgnoreCase(localMd5Code) && apkVersionCode == serverVersionCode) {
//                    Log.i(TAG, "send install broadcast ...");
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
//        try {
//            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
//            versionCode = packageInfo.versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "can't find this application!!!");
//        }
//        return versionCode;
//    }
//
//    private void downloadAPK(final Context mContext, final String downloadUrl) {
//        new Thread() {
//            @Override
//            public void run() {
//                Log.d(TAG, "downloadAPK is running...");
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
//                Log.d(TAG, "downloadAPK is end...");
//                if (!checkDowload) {
//                    checkDowload = true;
//                    sendEmptyMessage(0);
//                }
//            }
//        }.start();
//    }
//
//
//    private void sendUpdateBroadcast(Context context, Bundle bundle) {
//        AnswerAvailableEvent appUpdateEvent = new AnswerAvailableEvent(AnswerAvailableEvent.EventType.APP_UPDATE);
//        appUpdateEvent.setMsg(bundle);
//        EventBus.getDefault().post(appUpdateEvent);
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
//
//    @Override
//    public void handleMessage(Message msg) {
//        checkAppUpdate();
//    }
//}
