package tv.ismar.app.ad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
    private Timer mTimer;
    private MyTimerTask myTimerTask;

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
            List<AdvertiseTable> advertisementTables =
                    new Select().from(AdvertiseTable.class).execute();
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
                        File file =
                                new File(
                                        getFilesDir()
                                                + "/"
                                                + AdvertiseManager.AD_DIR
                                                + "/"
                                                + FileUtils.getFileByUrl(adTables.media_url));
                        if (file.exists() && file.delete()) {
                            new Delete()
                                    .from(AdvertiseTable.class)
                                    .where(AdvertiseTable.MEDIA_URL + "=?", mediaUrl)
                                    .execute();
                        }

                    } else if (adMaps.containsKey(mediaUrl)) {
                        // 接口返回数据和数据库都有,无需下载,按照测试的要求，需要先判断每条数据所有字段是否匹配本地，服务器端更改任何一条数据都应更新
                        String tabelTitle = adTables.title;
                        long tabelStartDate = adTables.start_date;
                        long tabelEndDate = adTables.end_date;
                        String tabelMediaId = adTables.media_id;
                        String tabelMediaUrl = adTables.media_url;
                        String tabelMediaType = adTables.media_type;
                        int tabelDuration = adTables.duration;
                        String tabelMd5 = adTables.md5;

                        AdElementEntity adElementEntity = adMaps.get(mediaUrl);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                        String start_date =
                                adElementEntity.getStart_date()
                                        + " "
                                        + adElementEntity.getStart_time();
                        String end_date =
                                adElementEntity.getEnd_date() + " " + adElementEntity.getEnd_time();
                        long adStartDate = 0, adEndDate = 0;
                        try {
                            adStartDate = dateFormat.parse(start_date).getTime();
                            adEndDate = dateFormat.parse(end_date).getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        String adTitle = adElementEntity.getTitle();
                        String adMedia_url = adElementEntity.getMedia_url();
                        String adMedia_type = adElementEntity.getMedia_type();
                        int adDuration = adElementEntity.getDuration();
                        String adMd5 = adElementEntity.getMd5();
                        String adMedia_id = String.valueOf(adElementEntity.getMedia_id());

                        boolean isEqual =
                                tabelTitle.equals(adTitle)
                                        && tabelStartDate == adStartDate
                                        && tabelEndDate == adEndDate
                                        && tabelMediaId.equals(adMedia_id)
                                        && tabelMediaType.equals(adMedia_type)
                                        && tabelMediaUrl.equals(adMedia_url)
                                        && tabelDuration == adDuration
                                        && tabelMd5.equals(adMd5);
                        Log.i(
                                TAG,
                                "isEqual:"
                                        + isEqual
                                        + " "
                                        + tabelStartDate
                                        + " "
                                        + tabelEndDate
                                        + " "
                                        + tabelDuration
                                        + "\n"
                                        + tabelMediaUrl
                                        + "\n"
                                        + tabelMediaId
                                        + " "
                                        + " "
                                        + tabelMediaType
                                        + " "
                                        + tabelMd5);
                        if (isEqual) {
                            adMaps.remove(mediaUrl);
                        } else {
                            File file =
                                    new File(
                                            getFilesDir()
                                                    + "/"
                                                    + AdvertiseManager.AD_DIR
                                                    + "/"
                                                    + FileUtils.getFileByUrl(adTables.media_url));
                            if (file.exists() && file.delete()) {
                                new Delete()
                                        .from(AdvertiseTable.class)
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
}
