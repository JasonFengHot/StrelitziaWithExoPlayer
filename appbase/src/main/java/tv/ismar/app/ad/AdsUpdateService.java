package tv.ismar.app.ad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.util.FileUtils;

public class AdsUpdateService extends Service implements Advertisement.OnAppStartAdListener {

    private static final String TAG = "LH/AdsUpdateService";

    private Advertisement mAdvertisement;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAdvertisement = new Advertisement(this);
        mAdvertisement.setOnAppStartListener(this);

        if (mTimer == null) {
            mTimer = new Timer();
            myTimerTask = new MyTimerTask();
            mTimer.schedule(myTimerTask, 3000, 15 * 60 * 1000);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cancelTimer();
        if (mTimer == null) {
            mTimer = new Timer();
            myTimerTask = new MyTimerTask();
            mTimer.schedule(myTimerTask, 3000, 15 * 60 * 1000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }

    @Override
    public void loadAppStartAd(List<AdElementEntity> adList) {
        if (adList != null && !adList.isEmpty()) {
            Log.d(TAG, "loadAppStartAd:" + adList.size());
            List<AdElementEntity> needDownloadAds = new ArrayList<>();
            List<AdvertiseTable> advertisementTables = new Select().from(AdvertiseTable.class).execute();
            if (advertisementTables == null || advertisementTables.isEmpty()) {
                needDownloadAds.addAll(adList);
            } else {
                // 数据库中没有的数据需要下载,数据库中有服务器没有的需要删除,都有的不下载
                Map<String, AdElementEntity> adMaps = new HashMap<>();
                for (AdElementEntity adEntity : adList) {
                    Log.i(TAG, "ServerAd:" + adEntity.getMedia_url());
                    adMaps.put(adEntity.getMedia_url(), adEntity);
                }
                for (AdvertiseTable adTables : advertisementTables) {
                    String mediaUrl = adTables.media_url;
                    Log.i(TAG, "LocalAd:" + mediaUrl);
                    if (!adMaps.containsKey(mediaUrl)) {
                        // 接口返回数据没有,数据库有,需要删除
                        File file = new File(
                                getFilesDir() + "/" + AdvertiseManager.AD_DIR + "/" +
                                        FileUtils.getFileByUrl(adTables.media_url)
                        );
                        if (file.exists() && file.delete()) {
                            new Delete().from(AdvertiseTable.class)
                                    .where(AdvertiseTable.MEDIA_URL + "=?", mediaUrl)
                                    .execute();
                        }

                    } else if(adMaps.containsKey(mediaUrl)) {
                        // 接口返回数据和数据库都有,无需下载,按照测试的要求，需要先判断每条数据所有字段是否匹配本地，服务器端更改任何一条数据都应更新
                        String serverAd = new Gson().toJson(adTables);
                        String localAd = new Gson().toJson(adMaps.get(mediaUrl));
                        Log.i(TAG, "serverAd:" + serverAd + " localAd:" + localAd);
                        if(serverAd.equals(localAd)){
                            adMaps.remove(mediaUrl);
                        } else {
                            File file = new File(
                                    getFilesDir() + "/" + AdvertiseManager.AD_DIR + "/" +
                                            FileUtils.getFileByUrl(adTables.media_url)
                            );
                            if (file.exists() && file.delete()) {
                                new Delete().from(AdvertiseTable.class)
                                        .where(AdvertiseTable.MEDIA_URL + "=?", mediaUrl)
                                        .execute();
                            }
                        }
                    }
                }
                for (Map.Entry<String, AdElementEntity> entry : adMaps.entrySet()) {
                    Log.i(TAG, "needDownloadAds:" + entry.getValue());
                    needDownloadAds.add(entry.getValue());
                }
            }
            if (!needDownloadAds.isEmpty()) {
                AdvertiseManager advertiseManager = new AdvertiseManager(getApplicationContext());
                advertiseManager.updateAppLaunchAdvertisement(needDownloadAds);
            }

        } else {
            Log.d(TAG, "delete all ads");
            new Delete().from(AdvertiseTable.class).execute();
            try {
                FileUtils.deleteDir(getFilesDir() + "/" + AdvertiseManager.AD_DIR);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "delete ads file exception");
            }
        }

    }

    private Timer mTimer;
    private MyTimerTask myTimerTask;

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            mAdvertisement.fetchAppStartAd(Advertisement.AD_MODE_APPSTART);
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
        }

    }

    private void cancelTimer() {
        if (myTimerTask != null) {
            myTimerTask.cancel();
            myTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            System.gc();
        }
    }

}
