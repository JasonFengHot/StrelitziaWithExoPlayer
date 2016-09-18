package tv.ismar.player.view;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.player.PlayerPageContract;
import tv.ismar.player.R;
import tv.ismar.player.databinding.ActivityPlayerBinding;
import tv.ismar.player.media.IPlayer;
import tv.ismar.player.media.IsmartvPlayer;
import tv.ismar.player.media.PlayerBuilder;
import tv.ismar.player.presenter.PlayerPagePresenter;
import tv.ismar.player.viewmodel.PlayerPageViewModel;

public class PlayerActivity extends BaseActivity implements PlayerPageContract.View,
        IPlayer.OnVideoSizeChangedListener, IPlayer.OnStateChangedListener, IPlayer.OnBufferChangedListener {

    private final String TAG = "LH/PlayerActivity";

    private int itemPK = 0;
    private int subItemPk = 0;
    private int mediaHistoryPosition;
    private int mCurrentTeleplayIndex = 0;
    private int mCurrentQualityIndex = 0;
    private ItemEntity mItemEntity;

    // 播放器
    private IsmartvPlayer mIsmartvPlayer;
    private SurfaceView surfaceView;
    private FrameLayout player_container;
    private LinearLayout panel_layout;
    private SeekBar player_seekBar;
    private ExpandableListView player_menu_list;

    private PlayerPageViewModel mModel;
    private PlayerPageContract.Presenter mPresenter;
    private PlayerPagePresenter mPlayerPagePresenter;
    private ActivityPlayerBinding mBinding;

    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private Animation slideInRight;
    private Animation slideOutRight;
    private boolean mIsPlayingAd;
    private String[] keys = new String[]{"画面质量", "剧集"};
    private Map<String, List<String>> menuMaps = new HashMap<>();
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String itemId = intent.getStringExtra(PageIntentInterface.EXTRA_ITEM_ID);
        String subItemId = intent.getStringExtra(PageIntentInterface.EXTRA_SUBITEM_ID);
        mediaHistoryPosition = intent.getIntExtra(PageIntentInterface.EXTRA_MEDIA_POSITION, 0);
        subItemId = subItemId == null ? "0" : subItemId;
        try {
            itemPK = Integer.valueOf(itemId);
            subItemPk = Integer.valueOf(subItemId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (itemPK <= 0) {
            finish();
            Log.i(TAG, "itemId can't be null.");
            return;
        }

        mPlayerPagePresenter = new PlayerPagePresenter(getApplicationContext(), this);
        mModel = new PlayerPageViewModel(this, mPlayerPagePresenter);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_player);
        mBinding.setTasks(mModel);
        mBinding.setActionHandler(mPresenter);

        surfaceView = findView(R.id.surfaceView);
        player_container = findView(R.id.player_container);
        panel_layout = findView(R.id.panel_layout);
        player_seekBar = findView(R.id.player_seekBar);
        player_seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        player_menu_list = findView(R.id.player_menu_list);
        player_menu_list.setOnChildClickListener(onChildClickListener);

        panelShowAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(this,
                R.anim.fly_down);
        slideInRight = AnimationUtils.loadAnimation(this,
                R.anim.slide_in_right);
        slideOutRight = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);

        mPresenter.start();
        mPresenter.fetchItem(itemId);
        showProgressDialog(null);

        mGestureDetector = new GestureDetector(this, onGestureListener);

    }

    @Override
    protected void onStop() {
        mPresenter.stop();
        if (mIsmartvPlayer != null) {
            mIsmartvPlayer.release();
        }
        hidePanel();
        super.onStop();
    }

    @Override
    public void loadItem(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
        ItemEntity.Clip clip = itemEntity.getClip();
        ItemEntity.SubItem[] subItems = itemEntity.getSubitems();
        if (subItemPk > 0 && subItems != null) {
            // 获取当前要播放的电视剧Clip
            for (int i = 0; i < subItems.length; i++) {
                int _subItemPk = subItems[i].getPk();
                if (subItemPk == _subItemPk) {
                    mCurrentTeleplayIndex = i;
                    clip = subItems[i].getClip();
                    mItemEntity.setTitle(subItems[i].getTitle());
                    break;
                }
            }
        }
        String sign = "";
        String code = "1";
        mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);

    }

    @Override
    public void loadClip(ClipEntity clipEntity) {
        Log.d(TAG, clipEntity.toString());
        String iqiyi = clipEntity.getIqiyi_4_0();
        byte playerMode;
        if (Utils.isEmptyText(iqiyi)) {
            // 片源为视云
            playerMode = PlayerBuilder.MODE_SMART_PLAYER;
        } else {
            // 片源为爱奇艺
            playerMode = PlayerBuilder.MODE_QIYI_PLAYER;
        }
        initPlayerData(playerMode);
        mIsmartvPlayer = PlayerBuilder.getInstance()
                .setActivity(PlayerActivity.this)
                .setPlayerMode(playerMode)
                .setItemEntity(mItemEntity)
                .setSurfaceView(surfaceView)
                .setContainer(player_container)
                .build();
        mIsmartvPlayer.setDataSource(clipEntity, new IPlayer.OnDataSourceSetListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "player init success.");
                mIsmartvPlayer.prepareAsync();
            }

            @Override
            public void onFailed(String message) {
                Log.i(TAG, "player init fail: " + message);
                Toast.makeText(PlayerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        mIsmartvPlayer.setOnBufferChangedListener(this);
        mIsmartvPlayer.setOnStateChangedListener(this);
        mIsmartvPlayer.setOnVideoSizeChangedListener(this);

    }

    private void initPlayerData(byte playerMode) {
        menuMaps.clear();
    }

    @Override
    public void setPresenter(PlayerPageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onHttpFailure(Throwable e) {
    }

    @Override
    public void onHttpInterceptor(Throwable e) {
    }

    @Override
    public void onBufferStart() {
        showProgressDialog("视频加载中...");

    }

    @Override
    public void onBufferEnd() {
        dismissProgressDialog();

    }

    @Override
    public void onPrepared() {
        if (mIsmartvPlayer == null) {
            return;
        }
        mModel.setPanelData(mIsmartvPlayer, mItemEntity.getTitle());
        if (mediaHistoryPosition > 0) {
            mIsmartvPlayer.seekTo(mediaHistoryPosition);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.start();
                }
            }
        }, 500);

    }

    @Override
    public void onAdStart() {
        mIsPlayingAd = true;
    }

    @Override
    public void onAdEnd() {
        mIsPlayingAd = false;
    }

    // 奇艺播放器在onPrepared时无法获取到影片时长
    @Override
    public void onStarted() {
        Log.i(TAG, "clipLength:" + mIsmartvPlayer.getDuration());
        player_seekBar.setMax(mIsmartvPlayer.getDuration());
        mModel.updatePlayerPause();
        timerStart(0);
        showPanel();

        if (menuMaps.isEmpty()) {
            List<ClipEntity.Quality> qualities = mIsmartvPlayer.getQulities();
            if (qualities != null && qualities.size() > 0) {
                List<String> qualityString = new ArrayList<>();
                int i = 0;
                for (ClipEntity.Quality quality : qualities) {
                    qualityString.add(ClipEntity.Quality.getString(quality));
                    // 初始化当前播放分辨率index
                    if (mIsmartvPlayer.getCurrentQuality() == quality) {
                        mCurrentQualityIndex = i;
                    }
                    i++;
                }
                menuMaps.put(keys[0], qualityString);
            }
            ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
            if (subItems != null) {
                ArrayList<String> teles = new ArrayList<>();
                for (ItemEntity.SubItem subItem : subItems) {
                    teles.add(subItem.getTitle());
                }
                menuMaps.put(keys[1], teles);
            }
            MyExpandableListAdapter adapter = new MyExpandableListAdapter(PlayerActivity.this);
            player_menu_list.setAdapter(adapter);
        }

    }

    @Override
    public void onPaused() {
        mModel.updatePlayerPause();
    }

    @Override
    public void onSeekComplete() {
        timerStart(500);
        showPanel();
    }

    @Override
    public void onCompleted() {
        if (mItemEntity.getSubitems() != null && mCurrentTeleplayIndex < mItemEntity.getSubitems().length - 1) {
            String sign = "";
            String code = "1";
            mCurrentTeleplayIndex++;
            ItemEntity.SubItem subItem = mItemEntity.getSubitems()[mCurrentTeleplayIndex];
            mItemEntity.setTitle(subItem.getTitle());
            ItemEntity.Clip clip = subItem.getClip();
            if (clip != null) {
                mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
                return;
            }
        }
        finish();
    }

    @Override
    public boolean onError(String message) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {

    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mModel.updateTimer(progress, mIsmartvPlayer.getDuration());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            timerStop();
            hidePanelHandler.removeCallbacks(hidePanelRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!mItemEntity.getLiveVideo()) {
                int seekProgress = seekBar.getProgress();
                int maxSeek = mIsmartvPlayer.getDuration() - 3 * 1000;
                if (seekProgress >= maxSeek) {
                    seekProgress = maxSeek;
                }
                mIsmartvPlayer.seekTo(seekProgress);
            }
        }
    };

    private ExpandableListView.OnChildClickListener onChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if (mIsmartvPlayer == null) {
                return true;
            }
            switch (groupPosition) {
                case 0:
                    List<ClipEntity.Quality> qualities = mIsmartvPlayer.getQulities();
                    if (!qualities.isEmpty()) {
                        ClipEntity.Quality quality = mIsmartvPlayer.getQulities().get(childPosition);
                        if (mIsmartvPlayer.getCurrentQuality() != quality) {
                            mediaHistoryPosition = mIsmartvPlayer.getCurrentPosition();
                            mIsmartvPlayer.switchQuality(quality);
                            mCurrentQualityIndex = childPosition;
                            if (player_menu_list.getVisibility() == View.VISIBLE) {
                                player_menu_list.startAnimation(slideOutRight);
                                player_menu_list.setVisibility(View.GONE);
                            }
                            showProgressDialog(null);
                        }
                    }
                    break;
                case 1:
                    if (mCurrentTeleplayIndex != childPosition) {
                        mediaHistoryPosition = 0;
                        mCurrentTeleplayIndex = childPosition;
                        if (player_menu_list.getVisibility() == View.VISIBLE) {
                            player_menu_list.startAnimation(slideOutRight);
                            player_menu_list.setVisibility(View.GONE);
                        }
                        String sign = "";
                        String code = "1";
                        ItemEntity.SubItem subItem = mItemEntity.getSubitems()[mCurrentTeleplayIndex];
                        mItemEntity.setTitle(subItem.getTitle());
                        ItemEntity.Clip clip = subItem.getClip();
                        if (clip != null) {
                            mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
                        }
                    }
                    break;
            }
            player_menu_list.collapseGroup(groupPosition);
            return true;
        }
    };

    private void timerStart(int delay) {
        if (mIsmartvPlayer == null) {
            Log.e(TAG, "checkTaskStart: mIsmartvPlayer is null.");
            return;
        }
        timerStop();
        if (delay > 0) {
            mTimerHandler.postDelayed(timerRunnable, delay);
        } else {
            mTimerHandler.post(timerRunnable);
        }
    }

    private void timerStop() {
        mTimerHandler.removeCallbacks(timerRunnable);
    }

    private Handler mTimerHandler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        public void run() {
            if (!mItemEntity.getLiveVideo() && mIsmartvPlayer.isPlaying()) {
                int currentPosition = mIsmartvPlayer.getCurrentPosition();
                player_seekBar.setProgress(currentPosition);
            }
            mTimerHandler.postDelayed(timerRunnable, 1000);
        }
    };

    private Handler hidePanelHandler = new Handler();

    private Runnable hidePanelRunnable = new Runnable() {
        @Override
        public void run() {
            hidePanel();
            hidePanelHandler.removeCallbacks(hidePanelRunnable);
        }
    };

    private void showPanel() {
        if (panel_layout == null || mIsmartvPlayer == null) {
            return;
        }
        if (mIsPlayingAd || !mIsmartvPlayer.isInPlaybackState())
            return;
        if (panel_layout.getVisibility() != View.VISIBLE) {
            panel_layout.startAnimation(panelShowAnimation);
            panel_layout.setVisibility(View.VISIBLE);
            hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
        } else {
            hidePanelHandler.removeCallbacks(hidePanelRunnable);
            hidePanelHandler.postDelayed(hidePanelRunnable, 3000);
        }

    }

    private void hidePanel() {
        if (panel_layout.getVisibility() == View.VISIBLE) {
            panel_layout.startAnimation(panelHideAnimation);
            panel_layout.setVisibility(View.GONE);
        }
    }

    private void toggleMenuVisiblity() {
        if (player_menu_list.getVisibility() == View.VISIBLE) {
            player_menu_list.startAnimation(slideOutRight);
            player_menu_list.setVisibility(View.GONE);
        } else {
            player_menu_list.startAnimation(slideInRight);
            player_menu_list.setVisibility(View.VISIBLE);
            player_menu_list.requestFocus();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("LH/", "onKeyDown:" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                toggleMenuVisiblity();
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (mIsmartvPlayer.isInPlaybackState() && isKeyCodeSupported && player_menu_list.getVisibility() != View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                    showPanel();
                } else {
                    mIsmartvPlayer.start();
                    hidePanel();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.start();
                    hidePanel();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                    showPanel();
                }
                return true;
            } else {
                showPanel();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isProgressDialogShow()) {
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
            if (player_menu_list.getVisibility() == View.VISIBLE) {
                return true;
            }
            showPanel();
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
            if (e1.getX() - e2.getX() > 120 || e1.getX() - e2.getX() < -120) {
                toggleMenuVisiblity();
                return true;
            }
            if (player_menu_list.getVisibility() == View.VISIBLE) {
                return true;
            }
            return false;
        }
    };

    class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private Context mContext;

        public MyExpandableListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getGroupCount() {
            return menuMaps.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (groupPosition < keys.length) {
                return menuMaps.get(keys[groupPosition]).size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return keys[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            List<String> children = menuMaps.get(keys[groupPosition]);
            return children.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            LinearLayout ll = new LinearLayout(mContext);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            TextView textView = getTextView(R.dimen.h0_size);
            textView.setTag(groupPosition);
            textView.setText(getGroup(groupPosition).toString());
            ll.addView(textView);
            return ll;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = getTextView(R.dimen.h1_size);
            textView.setTag(10 + childPosition);
            textView.setText(getChild(groupPosition, childPosition).toString());

            switch (groupPosition) {
                case 0:
                    Log.i("LH/", "childPosition:" + childPosition + " qualityIndex:" + mCurrentQualityIndex);
                    if (childPosition == mCurrentQualityIndex) {
                        Drawable drawable = getResources().getDrawable(R.drawable.player_menu_indicator);
                        drawable.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen.h2_size), getResources().getDimensionPixelSize(R.dimen.h2_size));
                        textView.setCompoundDrawables(drawable, null, null, null);
                    }
                    break;
                case 1:
                    if (childPosition == mCurrentTeleplayIndex) {
                        Drawable drawable = getResources().getDrawable(R.drawable.player_menu_indicator);
                        drawable.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen.h2_size), getResources().getDimensionPixelSize(R.dimen.h2_size));
                        textView.setCompoundDrawables(drawable, null, null, null);
                    }
                    break;
            }
            return textView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {

        }

        @Override
        public void onGroupCollapsed(int groupPosition) {

        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return 0;
        }

        private TextView getTextView(int id) {
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.player_menu_item_h));
            final TextView textView = new TextView(mContext);
            textView.setLayoutParams(lp);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(id));
            textView.setTextColor(getResources().getColor(R.color.color_white));

            textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        textView.setBackgroundResource(R.color.color_focus);
                    } else {
                        textView.setBackgroundResource(android.R.color.transparent);
                    }
                }
            });
            return textView;
        }
    }
}
