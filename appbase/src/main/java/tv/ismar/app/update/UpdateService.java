package tv.ismar.app.update;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Retrofit;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.VersionInfoV2Entity;

import static tv.ismar.app.update.UpdateService.DownloadEntity.download_length;
import static tv.ismar.app.update.UpdateService.DownloadEntity.save_path;
import static tv.ismar.app.update.UpdateService.DownloadEntity.status;
import static tv.ismar.app.update.UpdateService.DownloadEntity.total_length;
import static tv.ismar.app.update.UpdateService.DownloadStatus.COMPLETED;
import static tv.ismar.app.update.UpdateService.DownloadStatus.DOWNLOADING;
import static tv.ismar.app.update.UpdateService.DownloadStatus.ERROR;


/**
 * Created by huibin on 10/20/16.
 */

public class UpdateService extends Service {
    private static final String TAG = "UpdateService";
    private SkyService mSkyService;
    private Subscription upgradeSubscription;
    private Subscription downloadSubscription;

    private DownloadStatus mStatus;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSkyService = SkyService.ServiceManager.getUpgradeService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fetchAppUpgrade();
        return super.onStartCommand(intent, flags, startId);

    }


    private void fetchAppUpgrade() {
        String sn = "le_1y39rh8c";
        String manu = "BYD";
        String app = "sky";
        String modelName = "YT-X703F";
        String location = "SH";
        int versionCode = 222;

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
                        downloadApp(versionInfoV2Entity);
                    }
                });
    }


    private void downloadApp(VersionInfoV2Entity entity) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://www.baidu.com/")
                .build();


        String url = entity.getApplication().getUrl();
        long seekPosition = Long.parseLong(getProperties(download_length));
        Log.d(TAG, "download url: ");
        downloadSubscription = retrofit.create(SkyService.class).download(url, buildRange(seekPosition))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ResponseBody>() {


                    @Override
                    protected void finalize() throws Throwable {
                        super.finalize();
                        Log.d(TAG, "finalize()");
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mStatus = ERROR;
                        setProperties(status, mStatus.toString());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {

                            setProperties(total_length, String.valueOf(responseBody.contentLength()));
                            saveFile(responseBody.source());
                        } catch (IOException e) {
                            mStatus = ERROR;
                            setProperties(status, mStatus.toString());
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void saveFile(BufferedSource bufferedSource) throws IOException {
        mStatus = DOWNLOADING;
        long seekPosition = Long.parseLong(getProperties(download_length));
        File file = new File(getProperties(save_path));
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
        randomAccessFile.seek(seekPosition);

        byte[] buffer = new byte[10 * 1024];

        int readLength;
        long downloadLength = seekPosition;
        long updateLength = 0;
        while ((readLength = bufferedSource.read(buffer)) > 0 && mStatus == DOWNLOADING) {
            if (mStatus == DOWNLOADING) {
                randomAccessFile.write(buffer, 0, readLength);
                downloadLength += readLength;
                updateLength += readLength;
                if (updateLength > 1024 * 1024) {
                    updateLength = 0;
                    setProperties(download_length, String.valueOf(downloadLength));
                    Log.d(TAG, file.toString() + " ===> download length: " + downloadLength / (1024F * 1024F) + "MB");
                }
            } else {
                if (downloadSubscription != null && downloadSubscription.isUnsubscribed()) {
                    downloadSubscription.unsubscribe();
                }
            }
        }
        randomAccessFile.close();
        bufferedSource.close();
        mStatus = COMPLETED;
        setProperties(status, mStatus.toString());
        Log.d(TAG, file.toString() + " ===> download complete!!!");
    }

    public String buildRange(long rangePosition) {
        return "bytes=" + rangePosition + "-";
    }


    private void installApp() {

    }


    private void setProperties(DownloadEntity entity, String value) {
        SharedPreferences preferences = getSharedPreferences("app_update", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(entity.toString(), value);
        editor.apply();
    }

    private String getProperties(DownloadEntity entity) {
        SharedPreferences preferences = getSharedPreferences("app_update", MODE_PRIVATE);
        return preferences.getString(entity.toString(), "0");
    }

    protected enum DownloadEntity {
        title,

        url,

        url_md5,

        total_length,

        download_length,

        save_path,

        status
    }

    protected enum DownloadStatus {
        DOWNLOADING,

        CANCEL,

        ERROR,

        COMPLETED,

        PAUSE
    }
}
