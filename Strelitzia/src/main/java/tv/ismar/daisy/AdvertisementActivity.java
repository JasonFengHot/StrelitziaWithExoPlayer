package tv.ismar.daisy;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.ad.AdvertiseManager;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.util.Utils;

public class AdvertisementActivity extends BaseActivity {

    private static final String TAG = "LH/AdvertiseActivity";

    private static final int MSG_AD_COUNTDOWN = 0x01;

    private VideoView ad_video;
    private ImageView ad_pic;
    private Button ad_timer;
    private AdvertiseManager advertiseManager;
    private List<AdvertiseTable> launchAds;
    private int countAdTime = 0;
    private int currentImageAdCountDown = 0;
    private boolean isStartImageCountDown = false;
    private boolean isPlayingVideo = false;
    private int playIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);
        ad_video = (VideoView) findViewById(R.id.ad_video);
        ad_pic = (ImageView) findViewById(R.id.ad_pic);
        ad_timer = (Button) findViewById(R.id.ad_timer);

        advertiseManager = new AdvertiseManager(getApplicationContext());
        launchAds = advertiseManager.getAppLaunchAdvertisement();
        for (AdvertiseTable adTable : launchAds) {
            int duration = adTable.duration;
            countAdTime += duration;
        }
        playLaunchAd(0);

    }

    private void playLaunchAd(final int index) {
        playIndex = index;
        if (launchAds.get(index).media_type.equals(AdvertiseManager.TYPE_VIDEO)) {
            isPlayingVideo = true;
        }
        if (isPlayingVideo) {
            if (ad_video.getVisibility() != View.VISIBLE) {
                ad_pic.setVisibility(View.GONE);
                ad_video.setVisibility(View.VISIBLE);
            }
            ad_video.setVideoPath(launchAds.get(index).location);
            ad_video.setOnPreparedListener(onPreparedListener);
            ad_video.setOnCompletionListener(onCompletionListener);
        } else {
            if (ad_pic.getVisibility() != View.VISIBLE) {
                ad_video.setVisibility(View.GONE);
                ad_pic.setVisibility(View.VISIBLE);
            }
            Picasso.with(this)
                    .load(launchAds.get(index).location)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                    .into(ad_pic, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (playIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                        }

                        @Override
                        public void onError() {
                            ad_pic.setImageBitmap(Utils.getImgFromAssets(AdvertisementActivity.this, "poster.png"));
                            if (playIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                        }
                    });
        }

    }

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            ad_video.start();
            if (playIndex == 0) {
                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
            }
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (playIndex == launchAds.size() - 1) {
                return;
            }
            playLaunchAd(playIndex++);
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AD_COUNTDOWN:
                    if (ad_timer == null) {
                        return;
                    }
                    if (countAdTime == 0) {
                        mHandler.removeMessages(MSG_AD_COUNTDOWN);
                        goNextPage();
                        return;
                    }
                    if (ad_timer.getVisibility() != View.VISIBLE) {
                        ad_timer.setVisibility(View.VISIBLE);
                    }
                    ad_timer.setText(String.valueOf(countAdTime));
                    if (!isPlayingVideo) {
                        if (currentImageAdCountDown == 0 && !isStartImageCountDown) {
                            currentImageAdCountDown = launchAds.get(playIndex).duration;
                            isStartImageCountDown = true;
                        } else {
                            if (currentImageAdCountDown == 0) {
                                playLaunchAd(playIndex++);
                                isStartImageCountDown = false;
                            } else {
                                currentImageAdCountDown--;
                            }
                        }
                        countAdTime--;
                    } else {
                        countAdTime = getAdCountDownTime();
                    }
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, 1000);
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        if (ad_video != null) {
            ad_video.stopPlayback();
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        super.onStop();
    }

    private void goNextPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private int getAdCountDownTime() {
        if (launchAds == null || launchAds.isEmpty() || !isPlayingVideo) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = playIndex;
        if (currentAd == launchAds.size() - 1) {
            totalAdTime = launchAds.get(launchAds.size() - 1).duration;
        } else {
            for (int i = currentAd; i < launchAds.size(); i++) {
                totalAdTime += launchAds.get(i).duration;
            }
        }
        return totalAdTime - ad_video.getCurrentPosition() / 1000;
    }
}
