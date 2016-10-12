package tv.ismar.detailpage.view;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.detailpage.R;
import tv.ismar.player.view.PlayerFragment;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_FROMPAGE;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_MODEL;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity implements PlayerFragment.OnHidePlayerPageListener {
    private static final String TAG = "DetailPageActivity";

    private Subscription apiItemSubsc;
    private String fromPage;
    private ItemEntity mItemEntity;

    private DetailPageFragment detailPageFragment;
    private PlayerFragment playerFragment;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailpage);
        Intent intent = getIntent();

        int itemPK = intent.getIntExtra(EXTRA_PK, -1);
        String itemJson = intent.getStringExtra(EXTRA_ITEM_JSON);
        fromPage = intent.getStringExtra(EXTRA_FROMPAGE);

        if (TextUtils.isEmpty(itemJson) && itemPK == -1){
            finish();
            return;
        }

        if (!TextUtils.isEmpty(itemJson)){
            mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);
            loadFragment();
        }else {
            fetchItem(String.valueOf(itemPK));
        }

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
        playerFragment.onPlayerFragment = true;
        playerFragment.detailPageClickPlay();

    }

    public void onBuyVip(View view) {
        if (playerFragment != null) {
            playerFragment.buyVipOnShowAd();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (playerFragment != null && playerFragment.onPlayerFragment && playerFragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (playerFragment != null && playerFragment.onPlayerFragment && playerFragment.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (playerFragment != null && playerFragment.onPlayerFragment && playerFragment.isBufferShow()) {
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
        playerFragment.onPlayerFragment = false;
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

    public void fetchItem(String pk) {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }

        apiItemSubsc = mSkyService.apiItem(pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity  = itemEntity;
                       loadFragment();
                    }
                });
    }

    private void loadFragment(){
        playerFragment = PlayerFragment.newInstance(mItemEntity.getPk(), 0, true);
        playerFragment.setOnHidePlayerPageListener(this);
        playerFragment.onPlayerFragment = false;
        detailPageFragment = DetailPageFragment.newInstance(fromPage,new Gson().toJson(mItemEntity));

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_detail_container, playerFragment);
        fragmentTransaction.add(R.id.activity_detail_container, detailPageFragment);
        fragmentTransaction.commit();
    }
}
