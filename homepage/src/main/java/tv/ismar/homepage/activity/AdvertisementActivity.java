package tv.ismar.homepage.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import tv.ismar.app.core.advertisement.AdvertisementManager;
import tv.ismar.app.core.logger.AdvertisementLogger;
import tv.ismar.app.db.LaunchAdvertisementEntity;
import tv.ismar.app.core.preferences.LogSharedPrefs;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.homepage.R;

public class AdvertisementActivity extends Activity {
    private static final String TAG = "AdvertisementActivity";
    private static final String DEFAULT_ADV_PICTURE = "file:///android_asset/poster.png";
    private static final int TIME_COUNTDOWN = 0x0001;

    private static final int[] secondsResId = {R.drawable.second_1, R.drawable.second_2,
            R.drawable.second_3, R.drawable.second_4, R.drawable.second_5};
    private ImageView adverPic;
    private ImageView timerText;
    private Handler messageHandler;
    private volatile boolean flag = true;
    private AdvertisementManager mAdvertisementManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flag = true;
        setContentView(R.layout.activity_advertisement);
        initViews();
        placeAdvertisementPic();
        messageHandler = new MessageHandler(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        finish();
    }

    @Override
    protected void onDestroy() {
        flag = false;
        super.onDestroy();
    }

    private void initViews() {
        adverPic = (ImageView) findViewById(R.id.advertisement_pic);
        timerText = (ImageView) findViewById(R.id.adver_timer);
    }

    private void placeAdvertisementPic() {
        mAdvertisementManager = new AdvertisementManager();
        String path = mAdvertisementManager.getAppLaunchAdvertisement();
        Log.d(TAG, "fetch advertisement path: " + path);
        Picasso.with(this)
                .load(path)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                .into(adverPic, new Callback() {
                    @Override
                    public void onSuccess() {
                        timerCountDown();
                        String launchAppAdvEntityStr = LogSharedPrefs.getSharedPrefs(LogSharedPrefs.SHARED_PREFS_NAME);
                        LaunchAdvertisementEntity.AdvertisementData[] advertisementDatas = new Gson().fromJson(launchAppAdvEntityStr, LaunchAdvertisementEntity.AdvertisementData[].class);
                        if (null != advertisementDatas) {
                            LaunchAdvertisementEntity.AdvertisementData data = advertisementDatas[0];
                            new CallaPlay().boot_ad_play(data.getTitle(), data.getMedia_id(), data.getMedia_url(), data.getDuration());
                        }
                    }

                    @Override
                    public void onError() {
                        new CallaPlay().bootAdvExcept(AdvertisementLogger.BOOT_ADV_PLAY_EXCEPTION_CODE, AdvertisementLogger.BOOT_ADV_PLAY_EXCEPTION_STRING);
                        Picasso.with(AdvertisementActivity.this)
                                .load(DEFAULT_ADV_PICTURE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                                .into(adverPic);
                        timerCountDown();
                    }
                });
    }

    private void timerCountDown() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 4; i >= 0; i--) {
                    Message message = new Message();
                    message.what = TIME_COUNTDOWN;
                    message.obj = i;
                    messageHandler.sendMessage(message);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void intentToLauncher() {
        Intent intent = new Intent(this, TVGuideActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void showCountTime(int second) {
        timerText.setImageResource(secondsResId[second]);
        if (second == 0 && flag) {
            finish();
//            intentToLauncher();
        }
    }


    static class MessageHandler extends Handler {
        WeakReference<AdvertisementActivity> mWeakReference;

        public MessageHandler(AdvertisementActivity activity) {
            mWeakReference = new WeakReference<AdvertisementActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AdvertisementActivity activity = mWeakReference.get();
            if (mWeakReference != null) {
                switch (msg.what) {
                    case TIME_COUNTDOWN:
                        int second = Integer.parseInt(String.valueOf(msg.obj));
                        activity.showCountTime(second);
                        break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (messageHandler != null) {
            messageHandler.removeMessages(TIME_COUNTDOWN);
        }
        super.onBackPressed();
    }
}
