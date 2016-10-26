package tv.ismar.player.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.player.R;

public class PlayerActivity extends BaseActivity {

    private final String TAG = "LH/PlayerActivity";

    private int itemPK = 0;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
    private int subItemPk = 0;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
    private PlayerFragment playerFragment;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Intent intent = getIntent();
        itemPK = intent.getIntExtra(PageIntentInterface.EXTRA_PK, 0);
        subItemPk = intent.getIntExtra(PageIntentInterface.EXTRA_SUBITEM_PK, 0);
        String source = intent.getStringExtra(PageIntentInterface.EXTRA_SOURCE);

        if (itemPK <= 0) {
            finish();
            Log.i(TAG, "itemId can't be null.");
            return;
        }

        playerFragment = PlayerFragment.newInstance(itemPK, subItemPk, null, source);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_player_container, playerFragment)
                .commit();

        mGestureDetector = new GestureDetector(this, onGestureListener);

    }

    @Override
    protected void onStop() {

        super.onStop();
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
