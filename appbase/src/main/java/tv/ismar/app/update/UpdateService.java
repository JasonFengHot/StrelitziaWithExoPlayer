package tv.ismar.app.update;

import android.app.Service;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.utils.AppUtils;
import com.blankj.utilcode.utils.FileUtils;
import com.blankj.utilcode.utils.ShellUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.ismartv.downloader.DownloadEntity;
import cn.ismartv.downloader.DownloadManager;
import cn.ismartv.downloader.DownloadStatus;
import cn.ismartv.injectdb.library.content.ContentProvider;
import cn.ismartv.injectdb.library.query.Select;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.core.Md5;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.UpgradeRequestEntity;
import tv.ismar.app.network.entity.VersionInfoV2Entity;


/**
 * Created by huibin on 10/20/16.
 */

public class UpdateService extends Service implements Loader.OnLoadCompleteListener<Cursor> {
    public static final String APP_UPDATE_ACTION = "cn.ismartv.vod.action.app_update";
    private static final String TAG = "UpdateService";
    private SkyService mSkyService;

    private File upgradeFile;
    private CursorLoader mCursorLoader;

    public static final int LOADER_ID_APP_UPDATE = 0xca;

    private CopyOnWriteArrayList<String> md5Jsons;

//    private VersionInfoV2Entity mVersionInfoV2Entity;

    public static final int INSTALL_SILENT = 0x7c;

    private volatile boolean isInstallSilent = false;

    public static boolean installAppLoading = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        upgradeFile = Environment.getExternalStorageDirectory();
        mSkyService = SkyService.ServiceManager.getUpgradeService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int installType = 0;
        if (intent != null) {
            installType = intent.getIntExtra("install_type", 0);
        }
        Log.d(TAG, "onStartCommand: " + installType);
        if (INSTALL_SILENT == installType) {
            isInstallSilent = true;
        }
        fetchAppUpgrade();
        return super.onStartCommand(intent, flags, startId);
    }


    private void fetchAppUpgrade() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sn = mSharedPreferences.getString("sn_token", "");
        if (TextUtils.isEmpty(sn)) {
            return;
        }
        String manu = "sharp";
        String app = "sky";
        String modelName = Build.PRODUCT.replace(" ", "_");
        String location = IsmartvActivator.getInstance().getProvince().get("province_py");
//        int versionCode = 222;

        int versionCode = fetchInstallVersionCode();

        List<UpgradeRequestEntity> upgradeRequestEntities = new ArrayList<>();
        UpgradeRequestEntity requestEntity = new UpgradeRequestEntity();
        requestEntity.setApp(app);
        requestEntity.setLoc(location);
        requestEntity.setManu(manu);
        requestEntity.setModelname(modelName);
        requestEntity.setSn(sn);
        requestEntity.setVer(String.valueOf(versionCode));

        upgradeRequestEntities.add(requestEntity);

        mSkyService.appUpgrade(upgradeRequestEntities)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Observer<VersionInfoV2Entity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(VersionInfoV2Entity versionInfoV2Entity) {
                        md5Jsons = new CopyOnWriteArrayList<String>();
                        String title;
                        String selection = "title in (";
                        for (VersionInfoV2Entity.ApplicationEntity applicationEntity : versionInfoV2Entity.getUpgrades()) {
                            title = Md5.md5(new Gson().toJson(applicationEntity));
                            md5Jsons.add(title);
                            checkUpgrade(applicationEntity);
                            selection += "?,";
                        }
                        selection = selection.substring(0, selection.length() - 1);
                        selection += ")";


                        final String finalSelection = selection;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mCursorLoader = new CursorLoader(getApplicationContext(), ContentProvider.createUri(DownloadEntity.class, null),
                                        null, finalSelection, md5Jsons.toArray(new String[]{}), null);
                                mCursorLoader.registerListener(LOADER_ID_APP_UPDATE, UpdateService.this);
                                mCursorLoader.startLoading();
                            }
                        });
                    }
                });
    }


    private void checkUpgrade(final VersionInfoV2Entity.ApplicationEntity applicationEntity) {
        Log.i(TAG, "server version code ---> " + applicationEntity.getVersion());
        int installVersionCode;
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(applicationEntity.getProduct(), 0);
            installVersionCode = packageInfo.versionCode;


        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "can't find this application!!!");
            installVersionCode = 0;
        }

        if (installVersionCode >= Integer.parseInt(applicationEntity.getVersion())) {
            Log.w(TAG, "installVersionCode >= applicationEntity.getVersion()");
            return;
        }

        Log.i(TAG, "local version code ---> " + installVersionCode);
        String title = Md5.md5(new Gson().toJson(applicationEntity));


        DownloadEntity download = new Select().from(DownloadEntity.class).where("title = ?", title).executeSingle();
        if (download == null || download.status != DownloadStatus.COMPLETED) {
            postDownload(applicationEntity);

        } else {
            final File apkFile = new File(download.savePath);
            if (installVersionCode < Integer.parseInt(applicationEntity.getVersion())) {
                if (apkFile.exists()) {
                    String serverMd5Code = applicationEntity.getMd5();
                    String localMd5Code = Md5.md5File(apkFile);
                    Log.d(TAG, "local md5 ---> " + localMd5Code);
                    Log.d(TAG, "server md5 ---> " + serverMd5Code);
//                String currentActivityName = getCurrentActivityName(mContext);

                    int apkVersionCode = getLocalApkVersionCode(apkFile.getAbsolutePath());
                    int serverVersionCode = Integer.parseInt(applicationEntity.getVersion());

                    Log.i(TAG, "download apk version code: " + apkVersionCode);
                    Log.i(TAG, "server apk version code: " + serverVersionCode);

                    if (serverMd5Code.equalsIgnoreCase(localMd5Code) && apkVersionCode == serverVersionCode) {
                        Log.i(TAG, "send install broadcast ...");
                        new Thread() {
                            @Override
                            public void run() {
                                String path = apkFile.getAbsolutePath();
                                Log.d(TAG, "install apk path: " + path);
                                Log.d(TAG, "isInstallSilent: " + isInstallSilent);
                                if (isInstallSilent) {
                                    installAppLoading = true;
                                    try {
                                        String[] args2 = {"chmod", "604", path};
                                        Runtime.getRuntime().exec(args2);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    boolean installSilentSuccess = installAppSilent(path, getApplicationContext());
                                    if (!installSilentSuccess) {
                                        installAppLoading = false;
                                    }
                                    Log.d(TAG, "installSilentSuccess: " + installSilentSuccess);
                                } else {
                                    Bundle bundle = new Bundle();
                                    bundle.putStringArrayList("msgs", applicationEntity.getUpdate());
                                    bundle.putString("path", apkFile.getAbsolutePath());
                                    sendUpdateBroadcast(bundle);
                                }
                            }
                        }.start();
                    } else {
                        if (apkFile.exists()) {
                            apkFile.delete();
                        }
                        postDownload(applicationEntity);
                    }
                } else {
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
//                    postDownload(applicationEntity);
                }
            } else {
                if (apkFile.exists()) {
                    apkFile.delete();
                }
            }
        }
    }

    private void sendUpdateBroadcast(Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(APP_UPDATE_ACTION);
        intent.putExtra("data", bundle);
        sendBroadcast(intent);
    }

    private int getLocalApkVersionCode(String path) {
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);
        int versionCode;
        try {
            versionCode = info.versionCode;
        } catch (Exception e) {
            versionCode = 0;
        }
        return versionCode;
    }

    private int fetchInstallVersionCode() {
        int versionCode = 0;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "can't find this application!!!");
        }
        return versionCode;
    }


    private void downloadApp(VersionInfoV2Entity.ApplicationEntity entity) {
        String url = entity.getUrl();
        String json = new Gson().toJson(entity);
        String title = Md5.md5(json);
        String filePath = getFilesDir().getAbsolutePath();
        DownloadManager.getInstance().start(url, title, json, filePath);
    }

    public static boolean installAppSilent(String filePath, Context context) {
        File file = FileUtils.getFileByPath(filePath);
        if (!FileUtils.isFileExists(file)) return false;
        boolean isSuccess = AppUtils.installAppSilent(context, filePath);
        return isSuccess;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
//        for (String json : md5Jsons) {

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String title = data.getString(data.getColumnIndex("title"));
            Log.d(TAG, "title: " + title);
            String status = data.getString(data.getColumnIndex("status"));
            if (status.equalsIgnoreCase("COMPLETED")) {
                DownloadEntity downloadEntity = new Select().from(DownloadEntity.class).where("title = ?", title).executeSingle();
                if (downloadEntity != null && downloadEntity.status == DownloadStatus.COMPLETED) {
                    VersionInfoV2Entity.ApplicationEntity applicationEntity = new Gson().fromJson(downloadEntity.json, VersionInfoV2Entity.ApplicationEntity.class);
                    checkUpgrade(applicationEntity);
                }
            }
        }
//        }
    }

    private void postDownload(VersionInfoV2Entity.ApplicationEntity applicationEntity) {


        downloadApp(applicationEntity);


    }
}
