package tv.ismar.detailpage.view;

import android.content.DialogInterface;
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
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.R;
import tv.ismar.player.view.PlayerFragment;

import static tv.ismar.app.core.PageIntentInterface.DETAIL_TYPE_ITEM;
import static tv.ismar.app.core.PageIntentInterface.DETAIL_TYPE_PKG;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_TYPE;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity implements PlayerFragment.OnHidePlayerPageListener {
    private static final String TAG = "DetailPageActivity";

    private Subscription apiItemSubsc;
    private String source;
    private ItemEntity mItemEntity;

    private DetailPageFragment detailPageFragment;
    private PackageDetailFragment mPackageDetailFragment;
    private PlayerFragment playerFragment;
    private GestureDetector mGestureDetector;
    private boolean viewInit;
    public LoadingDialog mLoadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_detailpage);
        Intent intent = getIntent();

        int itemPK = intent.getIntExtra(EXTRA_PK, -1);
        String itemJson = intent.getStringExtra(EXTRA_ITEM_JSON);
        source = intent.getStringExtra(EXTRA_SOURCE);
        int type = intent.getIntExtra(EXTRA_TYPE, 0);
        String url = intent.getStringExtra("url");

        if (TextUtils.isEmpty(itemJson) && itemPK == -1 && TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        showDialog();

        //解析来至launcher的参数
        if (!TextUtils.isEmpty(url)) {
            String[] arrayTmp = url.split("/");
            itemPK = Integer.parseInt(arrayTmp[arrayTmp.length - 1]);
            switch (arrayTmp[arrayTmp.length - 2]) {
                case "item":
                    type = DETAIL_TYPE_ITEM;
                    break;
                case "package":
                    type = DETAIL_TYPE_PKG;
                    break;
            }
        }

        if (!TextUtils.isEmpty(itemJson)) {
            mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);
            loadFragment(type);
        } else {
            fetchItem(String.valueOf(itemPK), type);
        }

        mGestureDetector = new GestureDetector(this, onGestureListener);

    }


    @Override
    protected void
    onResume() {
        super.onResume();
        if (viewInit && playerFragment != null && playerFragment.goFinishPageOnResume) {
            // 不能在播放器onComplete接口调用是因为会导致进入播放完成页前会先闪现详情页
            onHide();
        } else if (viewInit && playerFragment != null && !playerFragment.sharpKeyDownNotResume) {
            // 多个详情页显示时，逐一返回时需要初始化播放器
            playerFragment.subItemPk = 0;
            playerFragment.initPlayer();
        }
        viewInit = true;

    }

    public void goPlayer() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.hide(detailPageFragment);
//        fragmentTransaction.show(playerFragment);
        fragmentTransaction.replace(R.id.activity_detail_container,playerFragment);
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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.hide(playerFragment);
//        fragmentTransaction.show(detailPageFragment);
        fragmentTransaction.replace(R.id.activity_detail_container,detailPageFragment);
        fragmentTransaction.commit();
        playerFragment.onPlayerFragment = false;
        playerFragment.subItemPk = 0;
        playerFragment.initPlayer();

    }

    @Override
    protected void onDestroy() {
        playerFragment = null;
        detailPageFragment = null;
        viewInit = false;
        super.onDestroy();
    }

    public void fetchItem(String pk, final int type) {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
        String opt = "";
        switch (type) {
            case DETAIL_TYPE_ITEM:
                opt = "item";
                break;
            case DETAIL_TYPE_PKG:
                opt = "package";
                break;
        }

        apiItemSubsc = mSkyService.apiOptItem(pk, opt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity = itemEntity;
                        loadFragment(type);
                    }
                });
    }

    private void loadFragment(int type) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (type) {
            case PageIntent.DETAIL_TYPE_ITEM:
                String itemJson = new Gson().toJson(mItemEntity);
                playerFragment = PlayerFragment.newInstance(mItemEntity.getPk(), 0, itemJson, source);
                playerFragment.setOnHidePlayerPageListener(this);
                playerFragment.onPlayerFragment = false;
                detailPageFragment = DetailPageFragment.newInstance(source, itemJson);
//                fragmentTransaction.add(R.id.activity_detail_container, playerFragment);
                fragmentTransaction.add(R.id.activity_detail_container, detailPageFragment);
                fragmentTransaction.commit();
                break;
            case PageIntent.DETAIL_TYPE_PKG:
                String packJson = new Gson().toJson(mItemEntity);
                mPackageDetailFragment = PackageDetailFragment.newInstance(source, packJson);
                fragmentTransaction.add(R.id.activity_detail_container, mPackageDetailFragment);
                fragmentTransaction.commit();
                break;
        }

    }

    public void showDialog() {
        mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        mLoadingDialog.showDialog();
    }


}
