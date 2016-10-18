package tv.ismar.player.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.PageIntentInterface.PaymentInfo;
import tv.ismar.app.core.PlayCheckManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.DBQuality;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.player.PlayerPageContract;
import tv.ismar.player.R;
import tv.ismar.player.databinding.FragmentPlayerBinding;
import tv.ismar.player.media.IPlayer;
import tv.ismar.player.media.IsmartvPlayer;
import tv.ismar.player.media.PlayerBuilder;
import tv.ismar.player.presenter.PlayerPagePresenter;
import tv.ismar.player.viewmodel.PlayerPageViewModel;

import static tv.ismar.app.core.PageIntentInterface.FromPage.unknown;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.item;

public class PlayerFragment extends Fragment implements PlayerPageContract.View, PlayerMenu.OnCreateMenuListener,
        IPlayer.OnVideoSizeChangedListener, IPlayer.OnStateChangedListener, IPlayer.OnBufferChangedListener {

    private final String TAG = "LH/PlayerFragment";
    public static final int PAYMENT_REQUEST_CODE = 0xd6;
    public static final int PAYMENT_SUCCESS_CODE = 0x5c;

    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_SUB_PK = "ARG_SUB_PK";
    private static final String ARG_SOURCE = "ARG_SOURCE";
    private static final String ARG_IN_DETAIL_PAGE = "ARG_IN_DETAIL_PAGE";
    private static final String HISTORYCONTINUE = "上次放映：";
    private static final String PlAYSTART = "即将放映：";
    private static final int MSG_SEK_ACTION = 103;
    private static final int MSG_AD_COUNTDOWN = 104;
    private static final int EVENT_CLICK_VIP_BUY = 0x10;
    private static final int EVENT_CLICK_KEFU = 0x11;
    private static final int EVENT_COMPLETE_BUY = 0x12;
    // 以下为弹出菜单id
    private static final int MENU_QUALITY_ID_START = 0;// 码率起始id
    private static final int MENU_QUALITY_ID_END = 8;// 码率结束id
    private static final int MENU_TELEPLAY_ID_START = 100;// 电视剧等多集影片起始id
    private static final int MENU_KEFU_ID = 20;// 客服中心
    private static final int MENU_RESTART = 30;// 从头播放

    private int itemPK = 0;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
    private int subItemPk = 0;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
    private int mCurrentPosition;// 当前播放位置
    private ItemEntity mItemEntity;
    private ClipEntity mClipEntity;
    private boolean mIsPreview;

    // 历史记录
    private HistoryManager historyManager;
    private History mHistory;
    private int mediaHistoryPosition;// 起播位置
    private boolean mIsContinue;

    // 播放器
    private IsmartvPlayer mIsmartvPlayer;
    private SurfaceView surfaceView;
    private FrameLayout player_container;
    private LinearLayout panel_layout;
    private SeekBar player_seekBar;
    private PlayerMenu playerMenu;
    private static final int SHORT_STEP = 1000;
    private boolean isSeeking = false;
    private boolean isFastFBClick = false;
    private ImageView player_logo_image;
    private ImageView ad_vip_btn;
    private TextView ad_count_text;
    private boolean isInit = false;
    private boolean mIsPlayingAd;
    private ListView player_menu;
    private LinearLayout player_buffer_layout;
    private ImageView player_buffer_img;
    private TextView player_buffer_text;
    private AnimationDrawable animationDrawable;

    private FragmentPlayerBinding mBinding;
    private PlayerPageViewModel mModel;
    private PlayerPageContract.Presenter mPresenter;
    private PlayerPagePresenter mPlayerPagePresenter;
    private boolean isNeedOnResume = false;
    private boolean isClickKeFu = false;
    private boolean isPlayInDetailPage;
    public boolean onPlayerFragment = true;
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    public static final String AD_MODE_ONSTART = "qiantiepian";
    public static final String AD_MODE_ONPAUSE = "zanting";
    private String source;
    private AdImageDialog adImageDialog;
    public boolean goFinishPageOnResume = false;

    private long testLoadItemTime, testLoadClipTime, testPlayCheckTime, testPreparedTime;

    public PlayerFragment() {
        // Required empty public constructor
    }

    private OnHidePlayerPageListener onHidePlayerPageListener;

    public void setOnHidePlayerPageListener(OnHidePlayerPageListener onHidePlayerPageListener) {
        this.onHidePlayerPageListener = onHidePlayerPageListener;
    }

    public interface OnHidePlayerPageListener {

        void onHide();

    }

    public static PlayerFragment newInstance(int pk, int subPk, boolean playInDetailPage, String source) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PK, pk);
        args.putInt(ARG_SUB_PK, subPk);
        args.putBoolean(ARG_IN_DETAIL_PAGE, playInDetailPage);
        args.putString(ARG_SOURCE, source);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemPK = getArguments().getInt(ARG_PK);
            subItemPk = getArguments().getInt(ARG_SUB_PK);
            isPlayInDetailPage = getArguments().getBoolean(ARG_IN_DETAIL_PAGE);
            source = getArguments().getString(ARG_SOURCE);
        }
        if (!(getActivity() instanceof BaseActivity)) {
            if (isPlayInDetailPage) {
                onPlayerFragment = false;
                onHidePlayerPageListener.onHide();
            } else {
                getActivity().finish();
            }
            Log.e(TAG, "Activity must be extends BaseActivity.");
            return;
        }
        mPlayerPagePresenter = new PlayerPagePresenter(getActivity().getApplicationContext(), this);
        mModel = new PlayerPageViewModel(getActivity(), mPlayerPagePresenter);
        panelShowAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_down);
        mPresenter.start();
        Log.e(TAG, "Version1.89.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);
        mBinding.setTasks(mModel);
        mBinding.setActionHandler(mPresenter);

        View contentView = mBinding.getRoot();
        surfaceView = (SurfaceView) contentView.findViewById(R.id.surfaceView);
        player_container = (FrameLayout) contentView.findViewById(R.id.player_container);
        panel_layout = (LinearLayout) contentView.findViewById(R.id.panel_layout);
        player_seekBar = (SeekBar) contentView.findViewById(R.id.player_seekBar);
        player_logo_image = (ImageView) contentView.findViewById(R.id.player_logo_image);
        ad_vip_btn = (ImageView) contentView.findViewById(R.id.ad_vip_btn);
        ad_count_text = (TextView) contentView.findViewById(R.id.ad_count_text);
        player_menu = (ListView) contentView.findViewById(R.id.player_menu);
        player_buffer_layout = (LinearLayout) contentView.findViewById(R.id.player_buffer_layout);
        player_buffer_img = (ImageView) contentView.findViewById(R.id.player_buffer_img);
        player_buffer_text = (TextView) contentView.findViewById(R.id.player_buffer_text);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated");
        player_buffer_img.setBackgroundResource(R.drawable.module_loading);
        animationDrawable = (AnimationDrawable) player_buffer_img.getBackground();
        ad_vip_btn.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        ad_vip_btn.setImageResource(R.drawable.ad_vip_btn_focus);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        ad_vip_btn.setImageResource(R.drawable.ad_vip_btn_normal);
                        break;
                }
                return false;
            }
        });

        surfaceView.setOnHoverListener(onHoverListener);
        surfaceView.setOnClickListener(onClickListener);
        player_container.setOnHoverListener(onHoverListener);
        player_container.setOnClickListener(onClickListener);
        player_seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        if (!isNeedOnResume && !isClickKeFu) {
            fetchItemData();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "resultCode:" + resultCode + " request:" + requestCode);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode != PAYMENT_SUCCESS_CODE) {
                isNeedOnResume = false;
                if (isPlayInDetailPage) {
                    onHidePlayerPageListener.onHide();
                } else {
                    getActivity().finish();
                }
            }
        }
    }

    private void fetchItemData() {
        showBuffer(null);
        testLoadItemTime = System.currentTimeMillis();
        mPresenter.fetchPlayerItem(String.valueOf(itemPK));
    }

    /**
     * 1.点击会员去广告进入购买页面
     * 2.点击菜单'客服中心'
     * 3.试看完成,自动进入购买页面.需要释放播放器,重新获取Clip接口数据
     */
    private void goOtherPage(int type) {
        hideMenu();
        hidePanel();
        timerStop();
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        mIsmartvPlayer.release();
        mIsmartvPlayer = null;
        switch (type) {
            case EVENT_CLICK_VIP_BUY:
                isNeedOnResume = true;
                toPayPage(String.valueOf(mItemEntity.getPk()), "2", "2", "");
                break;
            case EVENT_CLICK_KEFU:
                isClickKeFu = true;
                Intent intent = new Intent();
                try {
                    intent.setAction("cn.ismartv.speedtester.feedback");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Click kefu but 'cn.ismartv.speedtester.feedback' not found.");
                    intent.setAction("cn.ismar.sakura.launcher");
                    startActivity(intent);
                }
                break;
            case EVENT_COMPLETE_BUY:
                isNeedOnResume = true;
                ItemEntity.Expense expense = mItemEntity.getExpense();
                String mode = null;
                if (1 == mItemEntity.getExpense().getJump_to()) {
                    mode = "item";
                }
                toPayPage(String.valueOf(mItemEntity.getPk()), String.valueOf(expense.getJump_to()),
                        String.valueOf(expense.getCpid()), mode);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNeedOnResume || isClickKeFu) {
            isNeedOnResume = false;
            isClickKeFu = false;
            // 试看完成进入购买页面后返回
            if (mItemEntity == null || mPresenter == null) {
                if (isPlayInDetailPage) {
                    onHidePlayerPageListener.onHide();
                } else {
                    getActivity().finish();
                }
                return;
            }
            initPlayer();
        }
    }

    @Override
    public void onStop() {
        hidePanel();
        timerStop();
        hideMenu();
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        if (!mIsPlayingAd) {
            createHistory(mCurrentPosition);
            addHistory(mCurrentPosition);
        }
        if (!isNeedOnResume && !isClickKeFu) {
            mPresenter.stop();
            if (mIsmartvPlayer != null) {
                mIsmartvPlayer.release();
                mIsmartvPlayer = null;
            }
        }
        super.onStop();
    }

    public void buyVipOnShowAd() {
        if (mIsmartvPlayer == null) {
            return;
        }
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
        ad_vip_btn.setVisibility(View.GONE);
        ad_count_text.setVisibility(View.GONE);
        goOtherPage(EVENT_CLICK_VIP_BUY);

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
        Log.i(TAG, "onBufferStart");
        if (!isSeeking) {
            timerStop();
            showBuffer(null);
        }
    }

    @Override
    public void onBufferEnd() {
        Log.i(TAG, "onBufferEnd");
        if (!isSeeking || mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_QIYI_PLAYER) {
            timerStart(500);
            hideBuffer();
        }
    }

    @Override
    public void onPrepared() {
        Log.d(TAG, "testPreparedTime:" + (System.currentTimeMillis() - testPreparedTime));
        if (mIsmartvPlayer == null) {
            return;
        }
        Log.i(TAG, "onPrepared:" + mediaHistoryPosition + " playingAd:" + mIsPlayingAd);
        mModel.setPanelData(mIsmartvPlayer, mItemEntity.getTitle());

        if (onPlayerFragment) {
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER && mIsPlayingAd) {
                mIsmartvPlayer.start();
            } else {
                preparedToStart();
            }
        }

    }

    public void detailPageClickPlay() {
        if (isPlayInDetailPage && mIsmartvPlayer != null && mIsmartvPlayer.isInPlaybackState()) {
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER && mIsPlayingAd) {
                mIsmartvPlayer.start();
            } else {
                preparedToStart();
            }
        }
    }

    public void initPlayer() {
        if (mIsmartvPlayer != null) {
            mIsmartvPlayer.release();
            mIsmartvPlayer = null;
        }
        hideMenu();
        hidePanel();
        fetchItemData();
    }

    private void preparedToStart() {
        if (mediaHistoryPosition > 0) {
            if (mIsPreview && mediaHistoryPosition >= mIsmartvPlayer.getDuration()) {
                goOtherPage(EVENT_COMPLETE_BUY);
            } else {
                mIsmartvPlayer.seekTo(mediaHistoryPosition);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
                            mIsmartvPlayer.start();
                        }
                    }
                }, 500);
            }
        } else {
            if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
                mIsmartvPlayer.start();
            }
        }

    }

    @Override
    public void onAdStart() {
        Log.i(TAG, "onAdStart");
        mIsPlayingAd = true;
        switch (mIsmartvPlayer.getPlayerMode()) {
            case PlayerBuilder.MODE_SMART_PLAYER:
                ad_vip_btn.setVisibility(View.GONE);
                ad_count_text.setVisibility(View.VISIBLE);
                break;
            case PlayerBuilder.MODE_QIYI_PLAYER:
                ad_vip_btn.setVisibility(View.VISIBLE);
                ad_count_text.setVisibility(View.VISIBLE);
                ad_vip_btn.setFocusable(true);
                ad_vip_btn.requestFocus();
                break;
        }
        mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
    }

    @Override
    public void onAdEnd() {
        Log.i(TAG, "onAdEnd");
        mIsPlayingAd = false;
        ad_vip_btn.setVisibility(View.GONE);
        ad_count_text.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
    }

    // 奇艺播放器在onPrepared时无法获取到影片时长
    @Override
    public void onStarted() {
        Log.i(TAG, "onStarted");
        if (!isInit) {
            String logo = mItemEntity.getLogo();
            Log.i(TAG, "clipLength:" + mIsmartvPlayer.getDuration() + " logo:" + logo);
            if (!Utils.isEmptyText(logo) && mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                Picasso.with(getActivity()).load(logo).into(player_logo_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        player_logo_image.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                    }
                });

            }
            player_seekBar.setMax(mIsmartvPlayer.getDuration());
            player_seekBar.setPadding(0, 0, 0, 0);
            isInit = true;
        }
        if (!mIsPlayingAd) {
            mModel.updatePlayerPause();
            Log.i(TAG, "onStarted-seeking:" + isSeeking);
            if (!isSeeking) {
                timerStart(0);
            } else {
                showPannelDelayOut();
            }
        }
        if (adImageDialog != null && adImageDialog.isShowing()) {
            adImageDialog.dismiss();
        }
    }

    @Override
    public void onPaused() {
        Log.i(TAG, "onPaused");
        timerStop();
        mModel.updatePlayerPause();
        showPannelDelayOut();
    }

    @Override
    public void onSeekComplete() {
        Log.i(TAG, "onSeekComplete");
        if (isSeeking) {
            timerStart(500);
            if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
                mIsmartvPlayer.start();
            }
        }
    }

    @Override
    public void onCompleted() {
        hideMenu();
        hidePanel();
        timerStop();
        if (mIsPreview) {
            mIsPreview = false;
            if (mItemEntity.getLiveVideo() && "sport".equals(mItemEntity.getContentModel())) {
                if (isPlayInDetailPage) {
                    onHidePlayerPageListener.onHide();
                } else {
                    getActivity().finish();
                }
            } else {
                createHistory(mIsmartvPlayer.getDuration());
                addHistory(mIsmartvPlayer.getDuration());
                goOtherPage(EVENT_COMPLETE_BUY);
            }
        } else {
            ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
            if (subItems != null) {
                for (int i = 0; i < subItems.length; i++) {
                    if (subItemPk == subItems[i].getPk() && i < subItems.length - 1) {
                        ItemEntity.SubItem nextItem = subItems[i + 1];
                        if (nextItem != null && nextItem.getClip() != null) {
                            String sign = "";
                            String code = "1";
                            mItemEntity.setTitle(nextItem.getTitle());
                            mItemEntity.setClip(nextItem.getClip());
                            subItemPk = nextItem.getPk();
                            testLoadClipTime = System.currentTimeMillis();
                            mPresenter.fetchMediaUrl(nextItem.getClip().getUrl(), sign, code);
                            showBuffer(null);
                            addHistory(0);
                            return;
                        }
                    }
                }
            }
            Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
            intent.putExtra("itemPk", String.valueOf(mItemEntity.getPk()));
            startActivity(intent);
            if(player_seekBar != null){
                player_seekBar.setProgress(0);
            }
            mCurrentPosition = 0;
            addHistory(0);
            if (isPlayInDetailPage) {
                // 再次进入详情页时,需要处理逻辑
                goFinishPageOnResume = true;
            } else {
                getActivity().finish();
            }
        }
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
            if (mItemEntity == null || mItemEntity.getLiveVideo()) {
                return;
            }
            mModel.updateTimer(progress, mIsmartvPlayer.getDuration());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "onStartTrackingTouch");
            if (mItemEntity.getLiveVideo()) {
                return;
            }
            timerStop();
            // 拖动进度条是需要一直显示Panel
            mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "onStopTrackingTouch");
            if (mItemEntity.getLiveVideo()) {
                return;
            }
            isSeeking = true;
            showBuffer(null);
            int seekProgress = seekBar.getProgress();
            int maxSeek = mIsmartvPlayer.getDuration() - 3 * 1000;
            if (seekProgress >= maxSeek) {
                seekProgress = maxSeek;
            }
            mCurrentPosition = seekProgress;
            mIsmartvPlayer.seekTo(seekProgress);
        }
    };

    private void timerStart(int delay) {
        if (mIsmartvPlayer == null || mItemEntity == null) {
            Log.e(TAG, "checkTaskStart: mIsmartvPlayer is null.");
            return;
        }
        if (mIsPlayingAd) {
            return;
        }
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
            if (mItemEntity.getLiveVideo() || !mIsmartvPlayer.isPlaying()) {
                return;
            }
            if (isSeeking) {
                isSeeking = false;
                mTimerHandler.postDelayed(timerRunnable, 500);
                return;
            }
            int mediaPosition = mIsmartvPlayer.getCurrentPosition();
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                if (mCurrentPosition == mediaPosition) {
                    mTimerHandler.postDelayed(timerRunnable, 500);
                    return;
                }
                if (isBufferShow()) {
                    hideBuffer();
                }
            }
            mCurrentPosition = mediaPosition;
            player_seekBar.setProgress(mCurrentPosition);
            mTimerHandler.postDelayed(timerRunnable, 500);
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEK_ACTION:
                    Log.d(TAG, "MSG_SEK_ACTION seek to " + mCurrentPosition);
                    player_seekBar.setProgress(mCurrentPosition);
                    mIsmartvPlayer.seekTo(mCurrentPosition);
                    offsets = 0;
                    offn = 1;
                    if (isFastFBClick) {
                        isFastFBClick = false;
                        showBuffer(null);
                    }
                    break;
                case MSG_AD_COUNTDOWN:
                    int countDownTime = mIsmartvPlayer.getAdCountDownTime() / 1000;
                    String time = String.valueOf(countDownTime);
                    if (countDownTime < 10) {
                        time = "0" + time;
                    }
                    if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                        ad_count_text.setText("广告倒计时" + time);
                    } else {
                        ad_count_text.setText("" + time);
                    }
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, 1000);
                    break;
            }
        }
    };

    private int offsets = 0; // 进度条变化
    private int offn = 1;

    private void fastForward(int step) {
        int clipLength = mIsmartvPlayer.getDuration();
        if (mCurrentPosition >= clipLength) {
            player_seekBar.setProgress(clipLength - 3000);
            return;
        }
        if (clipLength > 1000000) {
            if (offsets != 1 && offsets % 5 != 0) {
                offsets += step;
            } else {
                if (offsets > 0) {
                    offn = offsets / 5;
                }
            }
            if (offn < 11) {
                mCurrentPosition += clipLength * offn * 0.01;
            } else {
                mCurrentPosition += clipLength * 0.1;
            }
        } else {
            mCurrentPosition += 10000;
        }

        if (mCurrentPosition > clipLength) {
            mCurrentPosition = clipLength - 3000;
        }
        player_seekBar.setProgress(mCurrentPosition);
    }

    private void fastBackward(int step) {
        int clipLength = mIsmartvPlayer.getDuration();
        if (mCurrentPosition <= 0) {
            player_seekBar.setProgress(0);
            return;
        }
        if (clipLength > 1000000) {
            if (offsets != 1 && offsets % 5 != 0) {
                offsets += step;
            } else {
                if (offsets > 0) {
                    offn = offsets / 5;
                }
            }
            if (offn < 11) {
                mCurrentPosition -= clipLength * offn * 0.01;
            } else {
                mCurrentPosition -= clipLength * 0.1;
            }
        } else {
            mCurrentPosition -= 10000;
        }
        if (mCurrentPosition <= 0)
            mCurrentPosition = 0;
        player_seekBar.setProgress(mCurrentPosition);
    }

    private boolean isPanelShow() {
        return panel_layout != null && panel_layout.getVisibility() == View.VISIBLE;
    }

    public void showPannelDelayOut() {
        if (panel_layout == null || mIsmartvPlayer == null || isPopWindowShow() || isMenuShow()
                || mIsPlayingAd || !mIsmartvPlayer.isInPlaybackState()) {
            return;
        }
        if (panel_layout.getVisibility() != View.VISIBLE) {
            panel_layout.startAnimation(panelShowAnimation);
            panel_layout.setVisibility(View.VISIBLE);
            mHidePanelHandler.postDelayed(mHidePanelRunnable, 3000);
        } else {
            mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
            mHidePanelHandler.postDelayed(mHidePanelRunnable, 3000);
        }
    }

    private void hidePanel() {
        if (panel_layout != null && panel_layout.getVisibility() == View.VISIBLE) {
            panel_layout.startAnimation(panelHideAnimation);
            panel_layout.setVisibility(View.GONE);
            mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
        }
    }

    private Handler mHidePanelHandler = new Handler();

    private Runnable mHidePanelRunnable = new Runnable() {
        @Override
        public void run() {
            hidePanel();
        }
    };

    public void previousClick(View view) {
        if (!mItemEntity.getLiveVideo()) {
            if (!isSeeking) {
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                }
                // 拖动进度条是需要一直显示Panel
                mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);
                }
                timerStop();
                isSeeking = true;
            }
            if (mHandler.hasMessages(MSG_SEK_ACTION))
                mHandler.removeMessages(MSG_SEK_ACTION);
            fastBackward(SHORT_STEP);
            if (view != null) {
                isFastFBClick = true;
                mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
            }
        }
    }

    public void forwardClick(View view) {
        if (!mItemEntity.getLiveVideo()) {
            if (!isSeeking) {
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                }
                // 拖动进度条是需要一直显示Panel
                mHidePanelHandler.removeCallbacks(mHidePanelRunnable);
                if (panel_layout.getVisibility() != View.VISIBLE) {
                    panel_layout.startAnimation(panelShowAnimation);
                    panel_layout.setVisibility(View.VISIBLE);
                }
                timerStop();
                isSeeking = true;
            }
            if (mHandler.hasMessages(MSG_SEK_ACTION))
                mHandler.removeMessages(MSG_SEK_ACTION);
            fastForward(SHORT_STEP);
            if (view != null) {
                isFastFBClick = true;
                mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
            }
        }
    }

    @Override
    public void loadPlayerItem(ItemEntity itemEntity) {
        Log.d(TAG, "testLoadItemTime:" + (System.currentTimeMillis() - testLoadItemTime));
        testPlayCheckTime = System.currentTimeMillis();
        if (itemEntity == null) {
            Toast.makeText(getActivity(), "不存在该影片.", Toast.LENGTH_SHORT).show();
            if (isPlayInDetailPage) {
                onHidePlayerPageListener.onHide();
            } else {
                getActivity().finish();
            }
            return;
        }
        mItemEntity = itemEntity;
        final String previewTitle = mItemEntity.getTitle();
        showBuffer(PlAYSTART + mItemEntity.getTitle());
        ItemEntity.Clip clip = itemEntity.getClip();
        ItemEntity.SubItem[] subItems = itemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            int history_sub_item = initHistorySubItemPk();
            if (history_sub_item > 0) {
                subItemPk = history_sub_item;
            }
            if (subItemPk <= 0) {
                subItemPk = subItems[0].getPk();
            }
            // 获取当前要播放的电视剧Clip
            for (int i = 0; i < subItems.length; i++) {
                int _subItemPk = subItems[i].getPk();
                if (subItemPk == _subItemPk) {
                    clip = subItems[i].getClip();
                    mItemEntity.setTitle(subItems[i].getTitle());
                    mItemEntity.setClip(clip);
                    break;
                }
            }
        }
        final String sign = "";
        final String code = "1";
        final ItemEntity.Clip playCheckClip = clip;
        mIsPreview = false;
        if (mItemEntity.getExpense() != null) {
            PlayCheckManager.getInstance(((BaseActivity) getActivity()).mSkyService).check(String.valueOf(mItemEntity.getPk()), new PlayCheckManager.Callback() {
                @Override
                public void onSuccess(boolean isBuy, int remainDay) {
                    Log.d(TAG, "testPlayCheckTime:" + (System.currentTimeMillis() - testPlayCheckTime));
                    testLoadClipTime = System.currentTimeMillis();
                    Log.e(TAG, "play check isBuy:" + isBuy + " " + remainDay);
                    if (isBuy) {
                        mPresenter.fetchMediaUrl(playCheckClip.getUrl(), sign, code);
                    } else {
                        ItemEntity.Preview preview = mItemEntity.getPreview();
                        mItemEntity.setTitle(previewTitle);
                        mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);
                        mIsPreview = true;
                    }
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "play check fail");
                    ItemEntity.Preview preview = mItemEntity.getPreview();
                    testLoadClipTime = System.currentTimeMillis();
                    mItemEntity.setTitle(previewTitle);
                    mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);
                    mIsPreview = true;
                }
            });
        } else {
            testLoadClipTime = System.currentTimeMillis();
            mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
        }
    }

    @Override
    public void loadPlayerClip(ClipEntity clipEntity) {
        Log.d(TAG, "testLoadClipTime:" + (System.currentTimeMillis() - testLoadClipTime));
        if (clipEntity == null) {
            Toast.makeText(getActivity(), "获取播放地址错误,将退出播放器.", Toast.LENGTH_SHORT).show();
            if (isPlayInDetailPage) {
                onHidePlayerPageListener.onHide();
            } else {
                getActivity().finish();
            }
            return;
        }
        Log.d(TAG, clipEntity.toString());
        mClipEntity = clipEntity;
        mIsPlayingAd = false;
        String iqiyi = mClipEntity.getIqiyi_4_0();
        if (!mIsPreview && Utils.isEmptyText(iqiyi) && !isClickKeFu) {
            // 获取前贴片广告
            mPresenter.fetchAdvertisement(mItemEntity, AD_MODE_ONSTART, source);
        } else {
            createPlayer(null);
        }

    }

    private void createPlayer(List<AdElementEntity> adList) {
        String iqiyi = mClipEntity.getIqiyi_4_0();
        byte playerMode;
        if (Utils.isEmptyText(iqiyi)) {
            // 片源为视云
            playerMode = PlayerBuilder.MODE_SMART_PLAYER;
        } else {
            // 片源为爱奇艺
            playerMode = PlayerBuilder.MODE_QIYI_PLAYER;
        }
        ClipEntity.Quality initQuality = initPlayerData(playerMode);
        mIsmartvPlayer = PlayerBuilder.getInstance()
                .setActivity(getActivity())
                .setPlayerMode(playerMode)
                .setItemEntity(mItemEntity)
                .setSurfaceView(surfaceView)
                .setContainer(player_container)
                .build();
        mIsmartvPlayer.setOnBufferChangedListener(this);
        mIsmartvPlayer.setOnStateChangedListener(this);
        mIsmartvPlayer.setOnVideoSizeChangedListener(this);
        mIsmartvPlayer.setDataSource(mClipEntity, initQuality, adList, new IPlayer.OnDataSourceSetListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "player init success.");
                mIsmartvPlayer.prepareAsync();
                testPreparedTime = System.currentTimeMillis();
            }

            @Override
            public void onFailed(String message) {
                Log.i(TAG, "player init fail: " + message);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void loadPauseAd(List<AdElementEntity> pauseAdList) {
        if (pauseAdList == null || pauseAdList.isEmpty()) {
            Log.i(TAG, "Get pause ad null.");
            return;
        }
        // 视频暂停广告
        adImageDialog = new AdImageDialog(getActivity(), pauseAdList);
        try {
            adImageDialog.show();
        } catch (android.view.WindowManager.BadTokenException e) {
            Log.i(TAG, "Pause advertisement dialog show error.");
            e.printStackTrace();
        }
    }

    @Override
    public void loadAdvertisement(List<AdElementEntity> adList) {
        if (adList != null && !adList.isEmpty()) {
            mIsPlayingAd = true;
        }
        createPlayer(adList);

    }

    private int initHistorySubItemPk() {
        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        if (itemPK != subItemPk) {
            String historyUrl = Utils.getItemUrl(itemPK);
            String isLogin = "no";
            if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
                isLogin = "yes";
            }
            mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
            if (mHistory != null) {
                mediaHistoryPosition = (int) mHistory.last_position;
                showBuffer(HISTORYCONTINUE + getTimeString(mediaHistoryPosition));
                int sub_item_pk = Utils.getItemPk(mHistory.sub_url);
                if (sub_item_pk > 0) {
                    return sub_item_pk;
                }
            }
        }
        return -1;
    }

    private ClipEntity.Quality initPlayerData(byte playerMode) {
        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        ClipEntity.Quality initQuality = null;
        isInit = false;
        String historyUrl = Utils.getItemUrl(itemPK);
        String isLogin = "no";
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isLogin = "yes";
        }
        mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
        if (mHistory != null) {
            initQuality = ClipEntity.Quality.getQuality(mHistory.last_quality);
            mIsContinue = mHistory.is_continue;
            mediaHistoryPosition = (int) mHistory.last_position;
            showBuffer(HISTORYCONTINUE + getTimeString(mediaHistoryPosition));
        } else {
            showBuffer(PlAYSTART + mItemEntity.getTitle());
        }
        return initQuality;

    }

    private void addHistory(int last_position) {
        if (mItemEntity == null && historyManager == null || mIsmartvPlayer == null || !mIsmartvPlayer.isInPlaybackState() || mIsPlayingAd) {
            return;
        }
        History history = new History();
        history.title = mItemEntity.getTitle();
        ItemEntity.Expense expense = mItemEntity.getExpense();
        if (expense != null) {
            history.price = (int) expense.getPrice();
            history.paytype = expense.getPay_type();
            history.cptitle = expense.getCptitle();
            history.cpid = expense.getCpid();
            history.cpname = expense.getCpname();
        } else
            history.price = 0;
        history.adlet_url = mItemEntity.getAdletUrl();
        history.content_model = mItemEntity.getContentModel();
        history.is_complex = mItemEntity.getIsComplex();
        history.last_position = last_position;
        history.last_quality = mIsmartvPlayer.getCurrentQuality().getValue();
        history.url = Utils.getItemUrl(itemPK);
        ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            history.sub_url = Utils.getSubItemUrl(subItemPk);
        }
        history.is_continue = mIsContinue;
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken()))
            historyManager.addHistory(history, "yes");
        else
            historyManager.addHistory(history, "no");
    }

    private void createHistory(int length) {
        if (mIsmartvPlayer == null || !mIsmartvPlayer.isInPlaybackState() || mIsPlayingAd || "".equals(IsmartvActivator.getInstance().getAuthToken())) {
            return;// 不登录不必上传
        }
        int offset = length;
        if (length == mIsmartvPlayer.getDuration()) {
            offset = -1;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        if (itemPK != subItemPk) {
            params.put("subitem", subItemPk);
        } else {
            params.put("item", itemPK);
        }
        mPlayerPagePresenter.sendHistory(params);

    }

    private boolean createMenu() {
        playerMenu = new PlayerMenu(getActivity(), player_menu);
        playerMenu.setOnCreateMenuListener(this);
        // 添加电视剧子集
        PlayerMenuItem subMenu;
        ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
        if (subItems != null && subItems.length > 0 && !mIsPreview) {
            subMenu = playerMenu.addSubMenu(MENU_TELEPLAY_ID_START, getResources().getString(R.string.player_menu_teleplay));
            for (ItemEntity.SubItem subItem : subItems) {
                boolean isSelected = false;
                if (subItemPk == subItem.getPk()) {
                    isSelected = true;
                }
                subMenu.addItem(subItem.getPk(), subItem.getTitle(), isSelected);
            }
        }
        // 添加分辨率
        subMenu = playerMenu.addSubMenu(MENU_QUALITY_ID_START, getResources().getString(R.string.player_menu_quality));
        List<ClipEntity.Quality> qualities = mIsmartvPlayer.getQulities();
        if (!qualities.isEmpty()) {
            for (int i = 0; i < qualities.size(); i++) {
                ClipEntity.Quality quality = qualities.get(i);
                String qualityName = ClipEntity.Quality.getString(quality);
                boolean isSelected = false;
                if (mIsmartvPlayer.getCurrentQuality() == quality) {
                    isSelected = true;
                }
                // quality id从0开始,此处加1
                subMenu.addItem(quality.getValue() + 1, qualityName, isSelected);
            }
        }
        // 添加客服
        playerMenu.addItem(MENU_KEFU_ID, getResources().getString(R.string.player_menu_kefu));
        // 添加从头播放
        if (mItemEntity != null && !mItemEntity.getLiveVideo()) {
            playerMenu.addItem(MENU_RESTART, getResources().getString(R.string.player_menu_restart));
        }
        return true;
    }

    public boolean onMenuClicked(PlayerMenu menu, int id) {
        if (mIsmartvPlayer == null) {
            return false;
        }
        boolean ret = false;
        if (id > MENU_QUALITY_ID_START && id <= MENU_QUALITY_ID_END) {
            // id值为quality值+1
            int qualityValue = id - 1;
            ClipEntity.Quality clickQuality = ClipEntity.Quality.getQuality(qualityValue);
            if (clickQuality == null || clickQuality == mIsmartvPlayer.getCurrentQuality()) {
                // 为空或者点击的码率和当前设置码率相同
                return false;
            }
            mediaHistoryPosition = mIsmartvPlayer.getCurrentPosition();
            mIsmartvPlayer.switchQuality(clickQuality);
            isSeeking = true;
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            mModel.updateQuality();
            // 写入数据库
            historyManager.addOrUpdateQuality(new DBQuality(0, "", mIsmartvPlayer.getCurrentQuality().getValue()));
            ret = true;
        } else if (id > MENU_TELEPLAY_ID_START) {
            // id值为subItem pk值
            if (id == subItemPk) {
                return false;
            }
            for (ItemEntity.SubItem subItem : mItemEntity.getSubitems()) {
                if (subItem.getPk() == id) {
                    mediaHistoryPosition = 0;
                    timerStop();
                    mIsmartvPlayer.release();
                    mIsmartvPlayer = null;
                    ItemEntity.Clip clip = subItem.getClip();
                    String sign = "";
                    String code = "1";
                    mItemEntity.setTitle(subItem.getTitle());
                    mItemEntity.setClip(clip);
                    subItemPk = subItem.getPk();
                    if (clip != null) {
                        testLoadClipTime = System.currentTimeMillis();
                        mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
                    }
                    showBuffer(null);
                    ret = true;
                    break;
                }
            }
        } else if (id == MENU_KEFU_ID) {
            createHistory(mCurrentPosition);
            addHistory(mCurrentPosition);
            goOtherPage(EVENT_CLICK_KEFU);
            ret = true;
        } else if (id == MENU_RESTART) {
            showPannelDelayOut();
            player_seekBar.setProgress(0);
            mIsmartvPlayer.seekTo(0);
            mediaHistoryPosition = 0;
            ret = true;
        }
        return ret;
    }

    public void onMenuCloseed(PlayerMenu menu) {
        // do nothing
    }

    private void showMenu() {
        if (!isMenuShow()) {
            if (isPanelShow()) {
                hidePanel();
            }
            createMenu();
            playerMenu.show();
        }
    }

    private void hideMenu() {
        if (isMenuShow()) {
            playerMenu.hide();
        }
    }

    public boolean isMenuShow() {
        if (playerMenu == null) {
            return false;
        }
        return playerMenu.isVisible();
    }

    private void showBuffer(String msg) {
        if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying())
            return;
        if (player_buffer_layout.getVisibility() != View.VISIBLE) {
            if (msg != null) {
                player_buffer_text.setText(msg);
            }
            player_buffer_layout.setVisibility(View.VISIBLE);
            if (animationDrawable != null && !animationDrawable.isRunning()) {
                animationDrawable.start();
            }
        }
    }

    private void hideBuffer() {
        if (player_buffer_layout.getVisibility() == View.VISIBLE) {
            player_buffer_layout.setVisibility(View.GONE);
            player_buffer_text.setText(getString(R.string.loading_text));
            if (animationDrawable != null && animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
        }
    }

    public boolean isBufferShow() {
        if (player_buffer_layout != null && player_buffer_layout.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            int what = event.getAction();
            switch (what) {
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (!mIsPlayingAd && isInit) {
                        showPannelDelayOut();
                    }
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsmartvPlayer == null || isPopWindowShow() ||
                    mIsPlayingAd || !mIsmartvPlayer.isInPlaybackState()
                    || isBufferShow() || isMenuShow()) {
                return;
            }
            if (mIsmartvPlayer.isPlaying()) {
                mIsmartvPlayer.pause();
                mPresenter.fetchAdvertisement(mItemEntity, AD_MODE_ONPAUSE, source);
            } else {
                mIsmartvPlayer.start();
            }
        }
    };

    private String getTimeString(int ms) {
        int left = ms;
        int hour = left / 3600000;
        left %= 3600000;
        int min = left / 60000;
        left %= 60000;
        int sec = left / 1000;
        return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
    }

    private ModuleMessagePopWindow popDialog;

    private boolean isPopWindowShow() {
        return popDialog != null && popDialog.isShowing();
    }

    private void showExitPopup() {
        if (mIsPlayingAd) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
            if (isPlayInDetailPage) {
                onHidePlayerPageListener.onHide();
            } else {
                getActivity().finish();
            }
            return;
        }
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        timerStop();
        mIsmartvPlayer.pause();
        popDialog = new ModuleMessagePopWindow(getActivity());
        popDialog.setFirstMessage(getString(R.string.player_exit));
        popDialog.showAtLocation(((BaseActivity) getActivity()).getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        popDialog.dismiss();
                        if (isPlayInDetailPage) {
                            onHidePlayerPageListener.onHide();
                            addHistory(mCurrentPosition);
                        } else {
                            getActivity().finish();
                        }
                    }
                },
                new ModuleMessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        popDialog.dismiss();
                    }
                });
        popDialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!popDialog.isConfirmClick) {
                    timerStart(0);
                    mIsmartvPlayer.start();
                }
            }
        });
    }

    private void toPayPage(String pk, String jumpTo, String cpid, String model) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switch (jumpTo) {
            case "1":
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
                intent.putExtra("model", model);
                break;
            case "0":
                intent.setAction("tv.ismar.pay.pay");
                intent.putExtra("item_id", pk);
                break;
            case "2":
                intent.setAction("tv.ismar.pay.payvip");
                intent.putExtra("cpid", cpid);
                intent.putExtra("item_id", pk);
                break;
        }
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown");
        if (mItemEntity == null || mIsmartvPlayer == null) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (isPlayInDetailPage) {
                    onHidePlayerPageListener.onHide();
                } else {
                    getActivity().finish();
                }
            }
            return true;
        }
        if (isMenuShow()) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mIsPlayingAd) {
                    return true;
                }
                hidePanel();
                if (!isMenuShow()) {
                    showMenu();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (!isPopWindowShow() && mIsmartvPlayer != null && mIsmartvPlayer.isInPlaybackState() && !mIsPlayingAd) {
                    showExitPopup();
                    return true;
                }
                if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                    mHandler.removeMessages(MSG_AD_COUNTDOWN);
                }
                if (isPlayInDetailPage) {
                    onHidePlayerPageListener.onHide();
                } else {
                    getActivity().finish();
                }
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd) {
                    return true;
                }
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                    mPresenter.fetchAdvertisement(mItemEntity, AD_MODE_ONPAUSE, source);
                } else {
                    mIsmartvPlayer.start();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd) {
                    return true;
                }
                if (!mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.start();
                    hidePanel();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd) {
                    return true;
                }
                if (mIsmartvPlayer.isPlaying()) {
                    mIsmartvPlayer.pause();
                    mPresenter.fetchAdvertisement(mItemEntity, AD_MODE_ONPAUSE, source);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd) {
                    return true;
                }
                previousClick(null);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd) {
                    return true;
                }
                forwardClick(null);
                return true;
        }
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL &&
                !isMenuShow() &&
                !isPopWindowShow() &&
                !mIsPlayingAd;
        if (isKeyCodeSupported) {
            showPannelDelayOut();
            return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isSeeking) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    showBuffer(null);
                    mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    return true;
            }
        }
        return false;
    }

}
