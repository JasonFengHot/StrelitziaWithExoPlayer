package tv.ismar.player.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.konka.android.media.KKMediaPlayer;

import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.player.R;
import tv.ismar.player.SmartPlayer;

public class PlayerActivity extends BaseActivity {

    private final String TAG = "LH/PlayerActivity";
//    public static final String DETAIL_PAGE_ITEM = "detail_page_item";
//    public static final String DETAIL_PAGE_CLIP = "detail_page_clip";
//    public static final String HISTORY_POSITION = "detail_history_position";
//    public static final String HISTORY_QUALITY = "detail_history_quality";
//    public static final String DETAIL_PAGE_PATHS = "detail_page_paths"; // SmartPlayer没有提供获取所有(包含广告)url的接口
//    public static final String DETAIL_PAGE_AD_LISTS = "detail_page_ad_lists"; // 广告列表

    private PlayerFragment playerFragment;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Intent intent = getIntent();
        int itemPK = intent.getIntExtra(PageIntentInterface.EXTRA_PK, 0);// 当前影片pk值,通过/api/item/{pk}可获取详细信息
        int subItemPk = intent.getIntExtra(PageIntentInterface.EXTRA_SUBITEM_PK, 0);// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
        String source = intent.getStringExtra(PageIntentInterface.EXTRA_SOURCE);

//        String itemJson = null;
//        String clipJson = null;
//        int historyPosition = 0;
//        int historyQuality = -1;
//        String[] paths = null;
//        String adList = null;
//        if (mSmartPlayer != null) {
//            // 以下两个值不为空，表明预加载成功，无需重复流程
//            itemJson = intent.getStringExtra(DETAIL_PAGE_ITEM);
//            clipJson = intent.getStringExtra(DETAIL_PAGE_CLIP);
//            historyPosition = intent.getIntExtra(HISTORY_QUALITY, 0);
//            historyQuality = intent.getIntExtra(HISTORY_QUALITY, -1);
//            paths = intent.getStringArrayExtra(DETAIL_PAGE_PATHS);
//            adList = intent.getStringExtra(DETAIL_PAGE_AD_LISTS);
//        }

        if (itemPK <= 0) {
            finish();
            Log.i(TAG, "itemId can't be null.");
            return;
        }

//        playerFragment = PlayerFragment.newInstance(itemPK, subItemPk, source, itemJson, clipJson,
//                historyPosition, historyQuality, paths, adList);
        playerFragment = PlayerFragment.newInstance(itemPK, subItemPk, source);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_player_container, playerFragment)
                .commit();

        mGestureDetector = new GestureDetector(this, onGestureListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!TextUtils.isEmpty(brandName) && brandName.equalsIgnoreCase("konka")) {
//            try {
//                Class.forName("com.konka.android.media.KKMediaPlayer");
//                KKMediaPlayer localKKMediaPlayer1 = new KKMediaPlayer();
//                KKMediaPlayer.setContext(this);
//                localKKMediaPlayer1.setAspectRatio(2);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

    }

    public void onBuyVip(View view) {
        if (playerFragment != null) {
            playerFragment.buyVipOnShowAd();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (playerFragment != null && playerFragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (playerFragment != null && playerFragment.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (playerFragment != null && playerFragment.isBufferShow()) {
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

//    private View getRootView() {
//        return ((ViewGroup) (getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
//    }

}
