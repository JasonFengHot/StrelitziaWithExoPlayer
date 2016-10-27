package tv.ismar.app.ad;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.google.gson.Gson;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.util.SPUtils;
import tv.ismar.app.util.Utils;

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
            String shared = new Gson().toJson(adList).replaceAll(" ", "");
            String savedJson = (String) SPUtils.getValue(AdvertiseManager.EXTRA_LAUNCH_APP_AD, "");
            if (!Utils.isEmptyText(savedJson) && shared.equals(savedJson)) {
                return;
            }
            SPUtils.putValue(AdvertiseManager.EXTRA_LAUNCH_APP_AD, shared);
            AdvertiseManager advertiseManager = new AdvertiseManager(getApplicationContext());
            advertiseManager.updateAppLaunchAdvertisement(adList);
        }

    }

    private Timer mTimer;
    private MyTimerTask myTimerTask;

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mAdvertisement.fetchAppStartAd(Advertisement.AD_MODE_APPSTART);
                }
            });
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
