package tv.ismar.detailpage.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import tv.ismar.app.BaseActivity;
import tv.ismar.detailpage.R;
import tv.ismar.player.view.PlayerFragment;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_MODEL;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity implements PlayerFragment.OnHidePlayerPageListener {
    private static final String TAG = "DetailPageActivity";

    private int mItemPk;
    private String content_model;
    private DetailPageFragment detailPageFragment;
    private PlayerFragment playerFragment;
    private GestureDetector mGestureDetector;
    private boolean isInDetailPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailpage);
        content_model = getIntent().getStringExtra(EXTRA_MODEL);
        mItemPk = getIntent().getIntExtra(EXTRA_PK, -1);
        if (TextUtils.isEmpty(content_model) || mItemPk == -1) {
            finish();
            return;
        }

        isInDetailPage = true;
        playerFragment = PlayerFragment.newInstance(mItemPk, 0, true);
        playerFragment.setOnHidePlayerPageListener(this);
        detailPageFragment = DetailPageFragment.newInstance(mItemPk, content_model);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_detail_container, playerFragment);
        fragmentTransaction.add(R.id.activity_detail_container, detailPageFragment);
        fragmentTransaction.commit();

        mGestureDetector = new GestureDetector(this, onGestureListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void goPlayer() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(detailPageFragment);
        fragmentTransaction.show(playerFragment);
        fragmentTransaction.commit();
        playerFragment.detailPageClickPlay();
        isInDetailPage = false;

    }

    public void onBuyVip(View view) {
        if (playerFragment != null) {
            playerFragment.buyVipOnShowAd();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (playerFragment != null && !isInDetailPage && playerFragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (playerFragment != null && !isInDetailPage && playerFragment.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInDetailPage && playerFragment != null && playerFragment.isBufferShow()) {
            return true;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (playerFragment != null) {
                if (playerFragment.isMenuShow()) {
                    return true;
                }
                playerFragment.showPannelDelayOut();
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    @Override
    public void onHide() {
        if (playerFragment == null || detailPageFragment == null) {
            finish();
            return;
        }
        isInDetailPage = true;
        playerFragment.initPlayer();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(playerFragment);
        fragmentTransaction.show(detailPageFragment);
        fragmentTransaction.commit();

    }

    @Override
    protected void onDestroy() {
        playerFragment = null;
        detailPageFragment = null;
        super.onDestroy();
    }
}
