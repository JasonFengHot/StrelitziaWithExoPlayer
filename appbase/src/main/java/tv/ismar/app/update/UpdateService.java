package tv.ismar.app.update;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blankj.utilcode.utils.FileUtils;
import com.blankj.utilcode.utils.ShellUtils;
import com.google.gson.Gson;

import java.io.File;

import cn.ismartv.downloader.DownloadEntity;
import cn.ismartv.downloader.DownloadManager;
import cn.ismartv.downloader.DownloadStatus;
import cn.ismartv.injectdb.library.content.ContentProvider;
import cn.ismartv.injectdb.library.query.Select;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.core.Md5;
import tv.ismar.app.network.SkyService;
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

    private String md5Json;

    private VersionInfoV2Entity mVersionInfoV2Entity;

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
        Log.d(TAG, "onStartCommand");
        fetchAppUpgrade();
        return super.onStartCommand(intent, flags, startId);

    }

    private void checkUpgrade(final VersionInfoV2Entity versionInfoV2Entity) {
        Log.i(TAG, "server version code ---> " + versionInfoV2Entity.getApplication().getVersion());
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "can't find this application!!!");
        }
        Log.i(TAG, "local version code ---> " + packageInfo.versionCode);
        md5Json = Md5.md5(new Gson().toJson(versionInfoV2Entity));

        DownloadEntity download = new Select().from(DownloadEntity.class).where("title = ?", md5Json).executeSingle();
        if (download == null || download.status != DownloadStatus.COMPLETED) {
            downloadApp(versionInfoV2Entity);
            mCursorLoader = new CursorLoader(this, ContentProvider.createUri(DownloadEntity.class, null),
                    null, "title = ?", new String[]{md5Json}, null);
            mCursorLoader.registerListener(LOADER_ID_APP_UPDATE, this);
            mCursorLoader.startLoading();
        } else {
            final File apkFile = new File(download.savePath);
            if (packageInfo.versionCode < Integer.parseInt(versionInfoV2Entity.getApplication().getVersion())) {
                if (apkFile.exists()) {
                    String serverMd5Code = versionInfoV2Entity.getApplication().getMd5();
                    String localMd5Code = Md5.md5File(apkFile);
                    Log.d(TAG, "local md5 ---> " + localMd5Code);
                    Log.d(TAG, "server md5 ---> " + serverMd5Code);
//                String currentActivityName = getCurrentActivityName(mContext);

                    int apkVersionCode = getLocalApkVersionCode(apkFile.getAbsolutePath());
                    int serverVersionCode = Integer.parseInt(versionInfoV2Entity.getApplication().getVersion());

                    Log.i(TAG, "download apk version code: " + apkVersionCode);
                    Log.i(TAG, "server apk version code: " + serverVersionCode);

                    if (serverMd5Code.equalsIgnoreCase(localMd5Code) && apkVersionCode == serverVersionCode) {
                        Log.i(TAG, "send install broadcast ...");
                        new Thread() {
                            @Override
                            public void run() {
                                String path = apkFile.getAbsolutePath();
                                Log.d(TAG, "install apk path: " + path);
                                boolean installSilentSuccess = installAppSilent(path);
                                Log.d(TAG, "installSilentSuccess: " + installSilentSuccess);

                                if (!installSilentSuccess) {
                                    Bundle bundle = new Bundle();
                                    bundle.putStringArrayList("msgs", versionInfoV2Entity.getApplication().getUpdate());
                                    bundle.putString("path", apkFile.getAbsolutePath());
                                    sendUpdateBroadcast(bundle);
                                }
                            }
                        }.start();
                    } else {
                        if (apkFile.exists()) {
                            apkFile.delete();
                        }
                        downloadApp(versionInfoV2Entity);
                    }
                } else {
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                    downloadApp(versionInfoV2Entity);
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


    private void fetchAppUpgrade() {
        String sn = "le_1y39rh8c";
        String manu = "BYD";
        String app = "sky";
        String modelName = "YT-X703F";
        String location = "SH";
//        int versionCode = 222;

        int versionCode = fetchInstallVersionCode();

        mSkyService.appUpgrade(sn, manu, app, modelName, location, versionCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                        mVersionInfoV2Entity = versionInfoV2Entity;
                        checkUpgrade(versionInfoV2Entity);
                    }
                });
    }

    private void downloadApp(VersionInfoV2Entity entity) {
        String url = entity.getApplication().getUrl();
        String json = new Gson().toJson(entity);
        String title = Md5.md5(json);
        DownloadManager.getInstance().start(url, title, json, upgradeFile.toString());
    }

    public static boolean installAppSilent(String filePath) {
        File file = FileUtils.getFileByPath(filePath);
        if (!FileUtils.isFileExists(file)) return false;
        String command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + filePath;
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(command, true, true);
        return commandResult.successMsg != null && commandResult.successMsg.toLowerCase().contains("success");
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        DownloadEntity downloadEntity = new Select().from(DownloadEntity.class).where("title = ?", md5Json).executeSingle();
        if (downloadEntity != null && downloadEntity.status == DownloadStatus.COMPLETED) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("msgs", mVersionInfoV2Entity.getApplication().getUpdate());
            bundle.putString("path", downloadEntity.savePath);
            sendUpdateBroadcast(bundle);
        }
    }
}
