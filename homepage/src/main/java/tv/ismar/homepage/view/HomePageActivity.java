package tv.ismar.homepage.view;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.konka.android.media.KKMediaPlayer;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.ad.AdsUpdateService;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.homepage.R;

public class HomePageActivity extends BaseActivity implements AdvertisementFragment.OnHideAdPageListener {

    private static final String TAG = "LH/HomePageActivity";

    private AdvertisementFragment advertisementFragment;
    private TVGuideFragment tvGuideFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        if (savedInstanceState != null)
            savedInstanceState = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        String homepage_template = getIntent().getStringExtra("homepage_template");
        String homepage_url = getIntent().getStringExtra("homepage_url");
        advertisementFragment = AdvertisementFragment.newInstance();
        advertisementFragment.setOnHideAdPageListener(this);
        tvGuideFragment = TVGuideFragment.newInstance(homepage_template, homepage_url);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_homepage_container, tvGuideFragment);
        fragmentTransaction.add(R.id.activity_homepage_container, advertisementFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (advertisementFragment != null) {
            super.onBackPressed();
        } else {
            if (tvGuideFragment != null) {
                tvGuideFragment.onBackPressed();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Class.forName("com.konka.android.media.KKMediaPlayer");
            KKMediaPlayer localKKMediaPlayer1 = new KKMediaPlayer();
            KKMediaPlayer.setContext(this);
            localKKMediaPlayer1.setAspectRatio(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!DaisyUtils.isNetworkAvailable(this) && advertisementFragment == null && tvGuideFragment != null) {
            Log.e("tvguide", "onresume Isnetwork");
            tvGuideFragment.showNetErrorPopup();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (tvGuideFragment != null) {
            String homepage_template = intent.getStringExtra("homepage_template");
            String homepage_url = intent.getStringExtra("homepage_url");
            tvGuideFragment.onNewIntent(homepage_template, homepage_url);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (advertisementFragment != null) {
            return super.onKeyDown(keyCode, event);
        } else {
            return tvGuideFragment.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onHide() {
        if (advertisementFragment == null || tvGuideFragment == null) {
            finish();
            return;
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.show(tvGuideFragment);
        fragmentTransaction.remove(advertisementFragment);
        fragmentTransaction.commit();
        advertisementFragment = null;
        startAdsService();

    }

    private void startAdsService() {
        Intent intent = new Intent();
        intent.setClass(this, AdsUpdateService.class);
        startService(intent);
    }
}
