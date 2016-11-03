package tv.ismar.app.core.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.advertisement.AdvertisementManager;
import tv.ismar.app.core.client.IsmartvUrlClient;
import tv.ismar.app.entity.LaunchAdvertisementEntity;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.util.AppUtils;

/**
 * Created by huaijie on 3/19/15.
 */
public class PosterUpdateService extends Service {
    private static final String TAG = "PosterUpdateService";

    private static final int UPDATE_ADVERTISEMENT = 0x0001;

    public static final String POSTER_NAME = "poster.png";
    private static final String POSTER_TMP_NAME = "poster_tmp.png";

    private File posterFile;
    private File posterTmpFile;
    private Context mContext = this;

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADVERTISEMENT:
                    fetchAdvertisementInfo();
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        posterFile = new File(getFilesDir(), POSTER_NAME);
        posterTmpFile = new File(getFilesDir(), POSTER_TMP_NAME);
        posterUpdateTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void posterUpdateTask() {
        final Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "posterUpdateTask every 15 min");
                if (isPosterExpire() && posterFile.exists()) {
                    posterFile.delete();
                }

                messageHandler.sendEmptyMessage(UPDATE_ADVERTISEMENT);
            }
        };
//        timer.schedule(tt, 3000, 20  * 1000);
        timer.schedule(tt, 3000, 15 * 60 * 1000);
    }

    private void fetchAdvertisementInfo() {
        String api = SimpleRestClient.ad_domain + "/api/get/ad/";
        String adpId = "['" + "kaishi" + "']";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("adpid", adpId);

        new IsmartvUrlClient().doAdvertisementRequest(IsmartvUrlClient.Method.POST, api, params, new IsmartvUrlClient.CallBack() {
            @Override
            public void onSuccess(String result) {
                LaunchAdvertisementEntity launchAdvertisementEntity = new Gson().fromJson(result, LaunchAdvertisementEntity.class);
                if (null != launchAdvertisementEntity.getAds().getKaishi() && launchAdvertisementEntity.getAds().getKaishi().length != 0) {
                    LaunchAdvertisementEntity.AdvertisementData advertisementData = launchAdvertisementEntity.getAds().getKaishi()[0];
//                    downloadPic(advertisementData);
                    new AdvertisementManager().updateAppLaunchAdvertisement(launchAdvertisementEntity);


                } else {
                    new Delete().from(AdvertiseTable.class).execute();
                    Log.e(TAG, "fetch launch app advertisement error:\n"
                            + "retcode: " + launchAdvertisementEntity.getRetcode() + "\n"
                            + "retmsg: " + launchAdvertisementEntity.getRetmsg());
                }
            }

            @Override
            public void onFailed(Exception exception) {
                Log.e(TAG, "fetchAdvertisementInfo failed");
            }
        });
    }


    private void downloadPic(final LaunchAdvertisementEntity.AdvertisementData advertisementData) {
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "downloadPic is running...");
                try {
                    int byteread;
                    URL url = new URL(advertisementData.getMedia_url());
                    Log.i(TAG, "downloadPic ---> " + url);
                    if (!posterTmpFile.exists())
                        posterTmpFile.createNewFile();
                    URLConnection conn = url.openConnection();
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = openFileOutput(POSTER_TMP_NAME, Context.MODE_PRIVATE);
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    inStream.close();
                    fs.flush();
                    fs.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "downloadPic is end...");
                updateLocalPoster(advertisementData);
            }
        }.start();


    }

    private void updateLocalPoster(LaunchAdvertisementEntity.AdvertisementData advertisementData) {
        Log.i(TAG, "updateLocalPoster is running...");
        String cacheFileMd5 = AppUtils.getMd5ByFile(posterTmpFile);
        if (posterTmpFile.exists() &&
                cacheFileMd5.equalsIgnoreCase(advertisementData.getMd5())) {
            Log.i(TAG, "replace local poster png");
            posterTmpFile.renameTo(posterFile);
            Timestamp timestamp = null;
            try {
                timestamp = Timestamp.valueOf(advertisementData.getEnd_date() + " " + advertisementData.getEnd_time());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "updateLocalPoster: " + e.getMessage());
                return;
            }
            modifyPosterPreference(timestamp);
        }
    }

    private boolean isPosterExpire() {
        SharedPreferences preferences = getSharedPreferences("poster", Context.MODE_PRIVATE);
        String posterDateStr = preferences.getString("end_time", "1000-01-01 00:00:00");
        Timestamp posterExpireTimeStamp = Timestamp.valueOf(posterDateStr);
        Timestamp currentTimeStamp = new Timestamp(TrueTime.now().getTime());
        if (posterExpireTimeStamp.after(currentTimeStamp))
            return false;
        else
            return true;
    }

    private void modifyPosterPreference(Timestamp timestamp) {
        SharedPreferences preferences = getSharedPreferences("poster", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("end_time", timestamp.toString());
        editor.apply();
    }


    private File getLocalPosterFile() {
        return getFileStreamPath(POSTER_NAME);
    }

}
