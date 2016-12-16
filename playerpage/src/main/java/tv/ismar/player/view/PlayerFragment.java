package tv.ismar.player.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import com.google.gson.Gson;
import com.qiyi.sdk.player.AdItem;
import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.IMediaPlayer;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.ad.Advertisement;
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
import tv.ismar.app.util.NetworkUtils;
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

public class PlayerFragment extends Fragment implements PlayerPageContract.View, PlayerMenu.OnCreateMenuListener,
        IPlayer.OnVideoSizeChangedListener, IPlayer.OnStateChangedListener, IPlayer.OnBufferChangedListener,
        IPlayer.OnInfoListener, Advertisement.OnVideoPlayAdListener {

    private final String TAG = "LH/PlayerFragment";
    private static final String PLAYER_VERSION = "2.0";
    public static final int PAYMENT_REQUEST_CODE = 0xd6;
    public static final int PAYMENT_SUCCESS_CODE = 0x5c;

    private static final byte POP_TYPE_BUFFERING_LONG = 1;// 播放过程中,缓冲时间过长
    private static final byte POP_TYPE_PLAYER_ERROR = 3;// 底层onError回调

    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_SUB_PK = "ARG_SUB_PK";
    private static final String ARG_SOURCE = "ARG_SOURCE";
    private static final String ARG_DETAIL_PAGE_ITEM = "ARG_DETAIL_PAGE_ITEM";
    private static final String ARG_DETAIL_PAGE_CLIP = "ARG_DETAIL_PAGE_CLIP";
    private static final String ARG_DETAIL_PAGE_POSITION = "ARG_DETAIL_PAGE_POSITION";
    private static final String ARG_DETAIL_PAGE_QUALITY = "ARG_DETAIL_PAGE_QUALITY";
    private static final String ARG_DETAIL_PAGE_PATHS = "ARG_DETAIL_PAGE_PATHS";
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

    // 数据相关
    private int itemPK = 0;// 当前影片pk值,通过/api/item/{pk}可获取详细信息
    public int subItemPk = 0;// 当前多集片pk值,通过/api/subitem/{pk}可获取详细信息
    private int mCurrentPosition;// 当前播放位置
    private ItemEntity mItemEntity;
    private ClipEntity mClipEntity;
    private boolean mIsPreview;// 是否试看
    private boolean isSwitchTelevision = false;// 手动切换剧集，不查历史记录
    private ClipEntity.Quality mCurrentQuality;

    // 历史记录
    private HistoryManager historyManager;
    private History mHistory;

    // 播放器UI
    private FrameLayout player_container;
    private LinearLayout panel_layout;
    private SeekBar player_seekBar;
    private PlayerMenu playerMenu;
    private ImageView player_logo_image;
    private ImageView ad_vip_btn;
    private TextView ad_count_text;
    private ListView player_menu;
    private LinearLayout player_buffer_layout;
    private ImageView player_buffer_img;
    private TextView player_buffer_text;
    private ImageView previous, forward;
    private AnimationDrawable animationDrawable;

    // 播放器相关操作逻辑
    private IsmartvPlayer mIsmartvPlayer;
    private static final int SHORT_STEP = 1000;
    private boolean mIsOnPaused = false;// 调用pause()之后部分机型会执行BufferStart(701)
    private boolean isInit = false;// 奇艺播放器在onPrepared之后无法获得影片总时长,故在onStart接口中获取
    private boolean mIsPlayingAd;// 判断是否正在播放广告
    private boolean mIsInAdDetail;// 是否在广告详情页
    private boolean isSeeking = false;// 空鼠拖动进度条,左右键快进快退,切换码率
    private boolean isFastFBClick = false;// 控制栏左右步进按钮
    private boolean isNeedOnResume = false;// 当前页面未销毁,不在栈的顶层
    private boolean isClickKeFu = false;// 跳转至客服界面再返回后,不再做广告请求
    private String mUser;// playerCheck 返回user类型
    private boolean mHasPreLoad;// 已经在详情页实现了预加载
    private String[] tempPaths;// 预加载时用到

    private FragmentPlayerBinding mBinding;
    private PlayerPageViewModel mModel;
    private PlayerPageContract.Presenter mPresenter;
    private PlayerPagePresenter mPlayerPagePresenter;
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private String source;
    private AdImageDialog adImageDialog;
    private Advertisement mAdvertisement;

    private boolean sharpKeyDownNotResume = false; // 夏普电视设置按键Activity样式为Dialog样式

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(int pk, int subPk, String source, String itemJson, String clipJson,
                                             int historyPosition, int historyQuality, String[] paths) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PK, pk);
        args.putInt(ARG_SUB_PK, subPk);
        args.putString(ARG_SOURCE, source);
        args.putString(ARG_DETAIL_PAGE_ITEM, itemJson);
        args.putString(ARG_DETAIL_PAGE_CLIP, clipJson);
        args.putInt(ARG_DETAIL_PAGE_POSITION, historyPosition);
        args.putInt(ARG_DETAIL_PAGE_QUALITY, historyQuality);
        args.putStringArray(ARG_DETAIL_PAGE_PATHS, paths);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            Log.e(TAG, "PlayerFragment error.");
            getActivity().finish();
            return;
        }
        if (!(getActivity() instanceof BaseActivity)) {
            getActivity().finish();
            Log.e(TAG, "Activity must be extends BaseActivity.");
            return;
        }
        itemPK = bundle.getInt(ARG_PK);
        subItemPk = bundle.getInt(ARG_SUB_PK);
        source = bundle.getString(ARG_SOURCE);
        String itemJson = bundle.getString(ARG_DETAIL_PAGE_ITEM);
        String clipJson = bundle.getString(ARG_DETAIL_PAGE_CLIP);// 注意clipJson地址已解密
        if (itemJson != null && clipJson != null) {
            mHasPreLoad = true;
            mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);
            mClipEntity = new Gson().fromJson(clipJson, ClipEntity.class);
            mCurrentPosition = bundle.getInt(ARG_DETAIL_PAGE_POSITION);
            mCurrentQuality = ClipEntity.Quality.getQuality(bundle.getInt(ARG_DETAIL_PAGE_QUALITY));
            tempPaths = bundle.getStringArray(ARG_DETAIL_PAGE_PATHS);
        }
        mPlayerPagePresenter = new PlayerPagePresenter((BaseActivity) getActivity(), this);
        mModel = new PlayerPageViewModel(getActivity(), mPlayerPagePresenter);
        panelShowAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_down);
        mPresenter.start();
        mAdvertisement = new Advertisement(getActivity());
        mAdvertisement.setOnVideoPlayListener(this);
        Log.e(TAG, "Version:" + PLAYER_VERSION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);
        mBinding.setTasks(mModel);
        mBinding.setActionHandler(mPresenter);
        View contentView = mBinding.getRoot();
        initView(contentView);
        return contentView;
    }

    private void initView(View contentView) {
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
        previous = (ImageView) contentView.findViewById(R.id.previous);
        forward = (ImageView) contentView.findViewById(R.id.forward);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousClick(v);
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardClick(v);
            }
        });
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

        player_container.setOnHoverListener(onHoverListener);
        player_container.setOnClickListener(onClickListener);
        player_seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "resultCode:" + resultCode + " request:" + requestCode);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode != PAYMENT_SUCCESS_CODE) {
                isNeedOnResume = false;
                getActivity().finish();
            }
        }
    }

    private void fetchItemData() {
        showBuffer(null);
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
        switch (type) {
            case EVENT_CLICK_VIP_BUY:
                isNeedOnResume = true;
                if (mIsmartvPlayer != null && mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                    toPayPage(mItemEntity.getPk(), 2, 3, null);
                } else {
                    toPayPage(mItemEntity.getPk(), 2, 2, null);
                }
                break;
            case EVENT_CLICK_KEFU:
                // 此处需要等Menu动画结束之后再跳转Activity
                isClickKeFu = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        try {
                            intent.setAction("cn.ismartv.speedtester.feedback");
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "Click kefu but 'cn.ismartv.speedtester.feedback' not found.");
                            PageIntent page = new PageIntent();
                            page.toHelpPage(getActivity());
                        }
                    }
                }, 400);
                break;
            case EVENT_COMPLETE_BUY:
                isNeedOnResume = true;
                ItemEntity.Expense expense = mItemEntity.getExpense();
                PageIntentInterface.ProductCategory mode = null;
                if (1 == mItemEntity.getExpense().getJump_to()) {
                    mode = PageIntentInterface.ProductCategory.item;
                }
                toPayPage(mItemEntity.getPk(), expense.getJump_to(), expense.getCpid(), mode);
                break;
        }
        mIsmartvPlayer.release(true);
        mIsmartvPlayer = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharpKeyDownNotResume) {
            sharpKeyDownNotResume = false;
            return;
        }
        // 从客服中心，购买页面返回
        if (isNeedOnResume || isClickKeFu) {
            isNeedOnResume = false;
            initPlayer();
        } else {
            // 首次进入
            Log.i(TAG, "onResume:" + mHasPreLoad);
            if (mHasPreLoad && BaseActivity.mSmartPlayer != null) {
                createPlayer(null);
            } else {
                fetchItemData();
            }
        }

    }

    @Override
    public void onStop() {
        hidePanel();
        timerStop();
        hideMenu();
        if (mAdvertisement != null) {
            mAdvertisement.stopSubscription();
        }
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        if (!mIsPlayingAd) {
            addHistory(mCurrentPosition, true);
        }
        if (popDialog != null && popDialog.isShowing()) {
            // 底层报错导致Activity 被销毁，如果再次显示弹出框，会报错
            popDialog.dismiss();
            popDialog = null;
        }
        if (!isNeedOnResume && !isClickKeFu) {
            mPresenter.stop();
            if (mIsmartvPlayer != null) {
                mIsmartvPlayer.release(true);
                mIsmartvPlayer = null;
            }
            if (BaseActivity.mSmartPlayer != null) {
                BaseActivity.mSmartPlayer = null;
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
    public void onBufferStart() {
        Log.i(TAG, "onBufferStart");
        if (!isSeeking) {
            timerStop();
            showBuffer(null);
        }
        if (mIsmartvPlayer != null && mIsmartvPlayer.isPlaying()) {
            if (mBufferingTimer == null) {
                mBufferingTimer = new Timer();
                mBufferingTask = new BufferingTask();
                mBufferingTimer.schedule(mBufferingTask, 50 * 1000, 50 * 1000);
            }
        }
    }

    @Override
    public void onBufferEnd() {
        Log.i(TAG, "onBufferEnd");
        cancelTimer();
        if (!isSeeking || mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_QIYI_PLAYER) {
            timerStart(500);
            hideBuffer();
        }
    }

    @Override
    public void onInfo(int what, Object extra) {
        Log.i(TAG, "onInfo:" + what + " extra:" + extra);
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_MIDDLE_AD_COMING:
                //TODO 即将进入爱奇艺广告,不可快进操作
                hidePanel();
                hideMenu();
                break;
            case IMediaPlayer.MEDIA_INFO_MIDDLE_AD_SKIPPED:
                //TODO 爱奇艺中插广告播放结束
                break;
        }
    }

    @Override
    public void onPrepared() {
        if (mIsmartvPlayer == null) {
            return;
        }
        Log.i(TAG, "onPrepared:" + mCurrentPosition + " playingAd:" + mIsPlayingAd);
        mModel.setPanelData(mIsmartvPlayer, mItemEntity.getTitle());
        hideBuffer();
        if (!mIsmartvPlayer.isPlaying()) {
            mIsmartvPlayer.start();
        }

    }

    private void initPlayer() {
        if (mIsmartvPlayer != null) {
            mIsmartvPlayer.release(true);
            mIsmartvPlayer = null;
        }
        player_container.setVisibility(View.GONE);
        player_logo_image.setVisibility(View.GONE);
        mIsPlayingAd = false;
        mIsInAdDetail = false;
        sharpKeyDownNotResume = false;
        mIsPreview = false;
        ad_vip_btn.setVisibility(View.GONE);
        ad_count_text.setVisibility(View.GONE);
        hideMenu();
        hidePanel();
        fetchItemData();
    }

    @Override
    public void onAdStart() {
        Log.i(TAG, "onAdStart");
        mIsPlayingAd = true;
        ad_vip_btn.setVisibility(View.VISIBLE);
        ad_count_text.setVisibility(View.VISIBLE);
        ad_vip_btn.setFocusable(true);
        ad_vip_btn.requestFocus();
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

    @Override
    public void onMiddleAdStart() {
        Log.i(TAG, "onMiddleAdStart");
        mIsPlayingAd = true;
        ad_vip_btn.setVisibility(View.VISIBLE);
        ad_count_text.setVisibility(View.VISIBLE);
        ad_vip_btn.setFocusable(true);
        ad_vip_btn.requestFocus();
        mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);

    }

    @Override
    public void onMiddleAdEnd() {
        Log.i(TAG, "onMiddleAdEnd");
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
        if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
            mIsmartvPlayer.start();
        }
        timerStart(500);
        showPannelDelayOut();
    }

    @Override
    public void onCompleted() {
        if ((mIsmartvPlayer == null) || isPopWindowShow()) {
            return;
        }
        hideMenu();
        hidePanel();
        timerStop();
        if (mIsPreview) {
            mIsPreview = false;
            if (mItemEntity.getLiveVideo() && "sport".equals(mItemEntity.getContentModel())) {
                getActivity().finish();
            } else {
                goOtherPage(EVENT_COMPLETE_BUY);
            }
        } else {
            mCurrentPosition = 0;
            ItemEntity[] subItems = mItemEntity.getSubitems();
            if (subItems != null) {
                for (int i = 0; i < subItems.length; i++) {
                    if (subItemPk == subItems[i].getPk() && i < subItems.length - 1) {
                        ItemEntity nextItem = subItems[i + 1];
                        if (nextItem != null && nextItem.getClip() != null) {
                            if (mIsmartvPlayer != null) {
                                mIsmartvPlayer.release(true);
                                mIsmartvPlayer = null;
                            }
                            mItemEntity.setTitle(nextItem.getTitle());
                            mItemEntity.setClip(nextItem.getClip());
                            subItemPk = nextItem.getPk();

                            showBuffer(PlAYSTART + mItemEntity.getTitle());

                            String sign = "";
                            String code = "1";
                            mPresenter.fetchMediaUrl(nextItem.getClip().getUrl(), sign, code);
                            return;
                        }
                    }
                }
            }
            String itemJson = new Gson().toJson(mItemEntity);
            Intent intent = new Intent("tv.ismar.daisy.PlayFinished");
            intent.putExtra("itemJson", itemJson);
            startActivity(intent);
            if (player_seekBar != null) {
                player_seekBar.setProgress(0);
            }
            getActivity().finish();
        }
    }

    @Override
    public boolean onError(String message) {
        Log.e(TAG, "onError:" + message);
        if (mIsmartvPlayer == null || isDetached()) {
            return true;
        }
        Toast.makeText(getActivity(), "Temp player onError.", Toast.LENGTH_SHORT).show();
        // TODO 播放器起播seekTo需要底层修改
//        showExitPopup(POP_TYPE_PLAYER_ERROR);
        return true;
    }

    @Override
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {

    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mItemEntity == null || mItemEntity.getLiveVideo() || mModel == null || mIsmartvPlayer == null) {
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
        mTimerHandler.removeCallbacks(timerRunnable);
        if (delay > 0) {
            mTimerHandler.postDelayed(timerRunnable, delay);
        } else {
            mTimerHandler.post(timerRunnable);
        }
        if (isSeeking) {
            tempSeekPosition = mIsmartvPlayer.getCurrentPosition();
        }
    }

    private void timerStop() {
        mTimerHandler.removeCallbacks(timerRunnable);
    }

    private Handler mTimerHandler = new Handler();

    private int tempSeekPosition;

    private Runnable timerRunnable = new Runnable() {
        public void run() {
            if (mItemEntity == null || mIsmartvPlayer == null) {
                mTimerHandler.removeCallbacks(timerRunnable);
                return;
            }
            if (mItemEntity.getLiveVideo() || !mIsmartvPlayer.isPlaying()) {
                return;
            }
            if (mIsmartvPlayer.isPlaying()) {
                int mediaPosition = mIsmartvPlayer.getCurrentPosition();
                // 播放过程中断网判断Start
                if (mCurrentPosition == mediaPosition) {
                    if (!NetworkUtils.isConnected(getActivity())) {
                        ((BaseActivity) getActivity()).showNetWorkErrorDialog(null);
                        Log.e(TAG, "Network error.");
                        return;
                    }
                }
                // 播放过程中断网判断End

                if (isSeeking && tempSeekPosition == mediaPosition) {
                    mTimerHandler.postDelayed(timerRunnable, 500);
                    return;
                }
//                if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
//                    // Seek操作之后，getCurrentPosition在seek之前位置附近
//                    // Seek操作之后，视云播放器已经播放了，画面还未动，解码延迟
//                    if (mCurrentPosition == mediaPosition) {
//                        mTimerHandler.postDelayed(timerRunnable, 500);
//                        return;
//                    }
//
//                }
                if (isSeeking) {
                    isSeeking = false;
                    tempSeekPosition = -1;
                }
                if (isBufferShow()) {
                    hideBuffer();
                }
                mCurrentPosition = mediaPosition;
                player_seekBar.setProgress(mCurrentPosition);
            }
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
                    mIsOnPaused = false;
                    showBuffer(null);
                    if (isFastFBClick) {
                        isFastFBClick = false;
                    }
                    break;
                case MSG_AD_COUNTDOWN:
                    int countDownTime = mIsmartvPlayer.getAdCountDownTime() / 1000;
                    String time = String.valueOf(countDownTime);
                    if (countDownTime < 10) {
                        time = "0" + time;
                    }
                    ad_count_text.setText("" + time);
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
                || mIsPlayingAd || !mIsmartvPlayer.isInPlaybackState() || isPopWindowShow()) {
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

    private void previousClick(View view) {
        if (!mItemEntity.getLiveVideo()) {
            if (!isSeeking) {
//                if (mIsmartvPlayer.isPlaying()) {
//                    mIsmartvPlayer.pause();
//                }
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

    private void forwardClick(View view) {
        if (!mItemEntity.getLiveVideo()) {
            if (!isSeeking) {
//                if (mIsmartvPlayer.isPlaying()) {
//                    mIsmartvPlayer.pause();
//                }
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
        if (itemEntity == null) {
            Toast.makeText(getActivity(), "不存在该影片.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
        mItemEntity = itemEntity;

        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        String historyUrl = Utils.getItemUrl(itemPK);
        String isLogin = "no";
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isLogin = "yes";
        }
        mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
        if (mHistory != null) {
            mCurrentPosition = (int) mHistory.last_position;
        }
        final String previewTitle = mItemEntity.getTitle();
        ItemEntity.Clip clip = itemEntity.getClip();
        ItemEntity[] subItems = itemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            Log.i(TAG, "loadPlayerItem_sub_item_pk:" + subItemPk);
            if (subItemPk <= 0) {
                // 传入的subItemPk值大于0表示指定播放某一集
                // 点击播放按钮时，如果有历史记录，应该播放历史记录的subItemPk,默认播放第一集
                subItemPk = subItems[0].getPk();

                if (mHistory != null) {
                    int sub_item_pk = Utils.getItemPk(mHistory.sub_url);
                    Log.i(TAG, "CheckHistory_sub_item_pk:" + sub_item_pk);
                    if (sub_item_pk > 0) {
                        subItemPk = sub_item_pk;
                        mCurrentPosition = (int) mHistory.last_position;
                    }
                }
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
        // playCheck
        final String sign = "";
        final String code = "1";
        final ItemEntity.Clip playCheckClip = clip;
        mIsPreview = false;
        if (mItemEntity.getExpense() != null) {
            PlayCheckManager.getInstance(((BaseActivity) getActivity()).mSkyService).check(String.valueOf(mItemEntity.getPk()), new PlayCheckManager.Callback() {
                @Override
                public void onSuccess(boolean isBuy, int remainDay, String user) {
                    mUser = user;
                    Log.e(TAG, "play check isBuy:" + isBuy + " " + remainDay + " " + mUser);
                    if (isBuy) {
                        mPresenter.fetchMediaUrl(playCheckClip.getUrl(), sign, code);
                    } else {
                        ItemEntity.Preview preview = mItemEntity.getPreview();
                        mItemEntity.setTitle(previewTitle);
                        if (preview != null) {
                            mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);
                            mIsPreview = true;
                        }


                    }

                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "play check fail");
                    ItemEntity.Preview preview = mItemEntity.getPreview();
                    mItemEntity.setTitle(previewTitle);
                    mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);
                    mIsPreview = true;

                }
            });
        } else {
            mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);

        }
    }

    @Override
    public void loadPlayerClip(ClipEntity clipEntity) {
        if (clipEntity == null) {
            Toast.makeText(getActivity(), "获取播放地址错误,退出播放器.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
        Log.d(TAG, clipEntity.toString());
        mClipEntity = clipEntity;
        mIsPlayingAd = false;
        isInit = false;
        mIsOnPaused = false;
        // 每次进入创建播放器前先获取历史记录，历史播放位置，历史分辨率，手动切换剧集例外
        if (!isSwitchTelevision) {
            initHistory();
        }
        if (mCurrentPosition > 0) {
            showBuffer(HISTORYCONTINUE + getTimeString(mCurrentPosition));
        } else {
            showBuffer(PlAYSTART + mItemEntity.getTitle());
        }

        isSwitchTelevision = false;

        String iqiyi = mClipEntity.getIqiyi_4_0();
        if (!mIsPreview && Utils.isEmptyText(iqiyi) && !isClickKeFu) {
            // 获取前贴片广告
            mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONSTART, source);
        } else {
            createPlayer(null);
        }
        isClickKeFu = false;

    }

    private void createPlayer(List<AdElementEntity> adList) {
        byte playerMode;
        if (mHasPreLoad) {
            // 视云预加载
            playerMode = PlayerBuilder.MODE_PRELOAD_PLAYER;
        } else {
            String iqiyi = mClipEntity.getIqiyi_4_0();
            if (Utils.isEmptyText(iqiyi)) {
                // 片源为视云
                playerMode = PlayerBuilder.MODE_SMART_PLAYER;
            } else {
                // 片源为爱奇艺
                playerMode = PlayerBuilder.MODE_QIYI_PLAYER;
            }
        }

        Log.i(TAG, "mCurrentPosition:" + mCurrentPosition);
        mIsmartvPlayer = PlayerBuilder.getInstance()
                .setActivity(getActivity())
                .setPlayerMode(playerMode)
                .setItemEntity(mItemEntity)
                .setContainer(player_container)
                .setStartPosition(mCurrentPosition)
                .setIsPreview(mIsPreview)
                .build();
        mIsmartvPlayer.setOnBufferChangedListener(this);
        mIsmartvPlayer.setOnStateChangedListener(this);
        mIsmartvPlayer.setOnVideoSizeChangedListener(this);
        mIsmartvPlayer.setOnInfoListener(this);
        mIsmartvPlayer.setUser(mUser);
        if (mHasPreLoad) {
            mIsmartvPlayer.setSmartPlayer(BaseActivity.mSmartPlayer, tempPaths);
        }
        mIsmartvPlayer.setDataSource(mClipEntity, mCurrentQuality, adList, new IPlayer.OnDataSourceSetListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "player init success." + mIsmartvPlayer);
                if(mIsmartvPlayer == null){
                    return;
                }
                if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_QIYI_PLAYER) {
                    mIsmartvPlayer.prepareAsync();
                    mCurrentQuality = mIsmartvPlayer.getCurrentQuality();
                } else {
                    if (mHasPreLoad) {
                        mHasPreLoad = false;
                    } else {
                        mCurrentQuality = mIsmartvPlayer.getCurrentQuality();
                    }
                }
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
        adImageDialog = new AdImageDialog(getActivity(), R.style.PauseAdDialog, pauseAdList);
        try {
            adImageDialog.show();
        } catch (android.view.WindowManager.BadTokenException e) {
            Log.i(TAG, "Pause advertisement dialog show error.");
            e.printStackTrace();
        }
    }

    @Override
    public void loadVideoStartAd(List<AdElementEntity> adList) {
        if (adList != null && !adList.isEmpty()) {
            mIsPlayingAd = true;
        }
        createPlayer(adList);

    }

    private void initHistory() {
        // 获取历史分辨率
        if (mCurrentQuality == null) {
            DBQuality dbQuality = historyManager.getQuality();
            if (dbQuality != null) {
                mCurrentQuality = ClipEntity.Quality.getQuality(dbQuality.quality);
            }
        }

        if (mIsPreview) {
            mCurrentPosition = 0;
        }

    }

    private void addHistory(int last_position, boolean sendToServer) {
        if (mItemEntity == null || mIsmartvPlayer == null || mIsPlayingAd) {
            return;
        }
        if (historyManager == null) {
            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
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
        ClipEntity.Quality quality = mIsmartvPlayer.getCurrentQuality();
        if (quality != null) {
            history.last_quality = mIsmartvPlayer.getCurrentQuality().getValue();
        }
        history.url = Utils.getItemUrl(itemPK);
        if (subItemPk > 0) {
            history.sub_url = Utils.getSubItemUrl(subItemPk);
        }
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken()))
            historyManager.addHistory(history, "yes");
        else
            historyManager.addHistory(history, "no");

        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken()) && sendToServer) {
            int offset = last_position;
            if (last_position == mIsmartvPlayer.getDuration()) {
                offset = -1;
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("offset", offset);
            if (subItemPk > 0) {
                params.put("subitem", subItemPk);
            } else {
                params.put("item", itemPK);
            }
            mPlayerPagePresenter.sendHistory(params);
        }

    }

    private boolean createMenu() {
        if (playerMenu == null) {
            playerMenu = new PlayerMenu(getActivity(), player_menu);
            playerMenu.setOnCreateMenuListener(this);
            // 添加电视剧子集
            PlayerMenuItem subMenu;
            ItemEntity[] subItems = mItemEntity.getSubitems();
            if (subItems != null && subItems.length > 0 && !mIsPreview) {
                subMenu = playerMenu.addSubMenu(MENU_TELEPLAY_ID_START, getResources().getString(R.string.player_menu_teleplay));
                for (ItemEntity subItem : subItems) {
                    boolean isSelected = false;
                    if (subItemPk == subItem.getPk()) {
                        isSelected = true;
                    }
                    String subItemTitle = subItem.getTitle();
                    if (subItemTitle.contains("第")) {
                        int ind = subItemTitle.indexOf("第");
                        subItemTitle = subItemTitle.substring(ind);
                    }
                    subMenu.addItem(subItem.getPk(), subItemTitle, isSelected);
                }
            }
            // 添加分辨率
            subMenu = playerMenu.addSubMenu(MENU_QUALITY_ID_START, getResources().getString(R.string.player_menu_quality));
            List<ClipEntity.Quality> qualities = mIsmartvPlayer.getQulities();
            if (qualities != null && !qualities.isEmpty()) {
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
        }
        return true;
    }

    public boolean onMenuClicked(PlayerMenu menu, int id) {
        if (mIsmartvPlayer == null) {
            return false;
        }
        boolean ret = false;
        if (id > MENU_QUALITY_ID_START && id <= MENU_QUALITY_ID_END) {
            if (!NetworkUtils.isConnected(getActivity())) {
                showExitPopup(POP_TYPE_PLAYER_ERROR);
                Log.e(TAG, "Network error switch quality.");
                return true;
            }
            // id值为quality值+1
            int qualityValue = id - 1;
            ClipEntity.Quality clickQuality = ClipEntity.Quality.getQuality(qualityValue);
            if (clickQuality == null || clickQuality == mIsmartvPlayer.getCurrentQuality()) {
                // 为空或者点击的码率和当前设置码率相同
                return false;
            }
            mCurrentPosition = mIsmartvPlayer.getCurrentPosition();
            mIsmartvPlayer.setStartPosition(mCurrentPosition);
            mIsmartvPlayer.switchQuality(clickQuality);
            isSeeking = true;
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            mCurrentQuality = clickQuality;
            mModel.updateQuality();
            // 写入数据库
            if (historyManager == null) {
                historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
            }
            historyManager.addOrUpdateQuality(new DBQuality(0, "", mIsmartvPlayer.getCurrentQuality().getValue()));
            ret = true;
        } else if (id > MENU_TELEPLAY_ID_START) {
            // id值为subItem pk值
            if (id == subItemPk) {
                return false;
            }
            for (ItemEntity subItem : mItemEntity.getSubitems()) {
                if (subItem.getPk() == id) {
                    mCurrentPosition = 0;
                    timerStop();
                    mIsmartvPlayer.release(false);
                    mIsmartvPlayer = null;
                    ItemEntity.Clip clip = subItem.getClip();
                    String sign = "";
                    String code = "1";
                    mItemEntity.setTitle(subItem.getTitle());
                    mItemEntity.setClip(clip);
                    subItemPk = subItem.getPk();

                    showBuffer(PlAYSTART + mItemEntity.getTitle());
                    if (clip != null) {
                        mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
                    }
                    ret = true;
                    break;
                }
            }
        } else if (id == MENU_KEFU_ID) {
            goOtherPage(EVENT_CLICK_KEFU);
            ret = true;
        } else if (id == MENU_RESTART) {
            showPannelDelayOut();
            player_seekBar.setProgress(0);
            mIsmartvPlayer.seekTo(0);
            mCurrentPosition = 0;
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
        Log.d(TAG, "showBuffer:" + msg);
        if (mIsOnPaused || isPopWindowShow()) {
            return;
        }
        if (msg != null) {
            player_buffer_text.setText(msg);
        }
        if (player_buffer_layout.getVisibility() != View.VISIBLE) {
            player_buffer_layout.setVisibility(View.VISIBLE);
            if (animationDrawable != null && !animationDrawable.isRunning()) {
                animationDrawable.start();
            }
        }
    }

    private void hideBuffer() {
        if (mIsmartvPlayer == null) {
            return;
        }
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
                    || isBufferShow()) {
                return;
            }
            if (isMenuShow()) {
                hideMenu();
                return;
            }
            if (mIsmartvPlayer.isPlaying()) {
                mIsmartvPlayer.pause();
                mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONPAUSE, source);
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

    private void exitPlayerWhilePlaying() {
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        cancelTimer();
        timerStop();
        hideBuffer();
        hidePanel();
        getActivity().finish();
    }

    private ModuleMessagePopWindow popDialog;

    private boolean isPopWindowShow() {
        return popDialog != null && popDialog.isShowing();
    }

    private void showExitPopup(final byte popType) {
        if (popDialog != null && popDialog.isShowing()) {
            popDialog.dismiss();
            popDialog = null;
        }
        cancelTimer();
        timerStop();
        hideBuffer();
        hidePanel();
        if (mIsmartvPlayer != null && mIsmartvPlayer.isPlaying()) {
            mIsmartvPlayer.pause();
        }
        String message = getString(R.string.player_error);
        String cancelText = getString(R.string.player_pop_cancel);
        boolean hideCancel = false;
        switch (popType) {
            case POP_TYPE_BUFFERING_LONG:
                message = getString(R.string.player_buffering_long);
                cancelText = getString(R.string.player_pop_switch_quality);
                break;
            case POP_TYPE_PLAYER_ERROR:
                hideCancel = true;
                break;
        }
        popDialog = new ModuleMessagePopWindow(getActivity());
        popDialog.setConfirmBtn(getString(R.string.player_pop_ok));
        popDialog.setCancelBtn(cancelText);
        popDialog.setFirstMessage(message);
        if (hideCancel) {
            popDialog.hideCancelBtn();
        }
        popDialog.showAtLocation(((BaseActivity) getActivity()).getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        popDialog.dismiss();
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
                switch (popType) {
                    case POP_TYPE_PLAYER_ERROR:
                        // 播放器异常情况,判断播放进度临界值,剩余时长8分钟为界,小于8分钟下次从头播放
                        int value = 8 * 1000 * 60;
                        if (mIsmartvPlayer != null && (mIsmartvPlayer.getDuration() - mCurrentPosition <= value)) {
                            mCurrentPosition = 0;
                        }
                        getActivity().finish();
                        break;
                    case POP_TYPE_BUFFERING_LONG:
                        if (!popDialog.isConfirmClick) {
                            if (!isMenuShow()) {
                                showMenu();
                            }
                        } else {
                            timerStart(0);
                            mIsmartvPlayer.start();
                        }
                        break;
                }
            }
        });
    }

    private void toPayPage(int pk, int jumpTo, int cpid, PageIntentInterface.ProductCategory model) {
        Log.d(TAG, "toPayPage:" + pk + " to:" + jumpTo + " cpid:" + cpid);
        PageIntentInterface.PaymentInfo paymentInfo = new PaymentInfo(model, pk, jumpTo, cpid);
        Intent intent = new Intent();
        switch (paymentInfo.getJumpTo()) {
            case PageIntentInterface.PAYMENT:
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(PageIntentInterface.EXTRA_PK, paymentInfo.getPk());
                intent.putExtra(PageIntentInterface.EXTRA_PRODUCT_CATEGORY, paymentInfo.getCategory().toString());
                break;
            case PageIntentInterface.PAY:
                intent.setAction("tv.ismar.pay.pay");
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            case PageIntentInterface.PAYVIP:
                intent.setAction("tv.ismar.pay.payvip");
                intent.putExtra("cpid", paymentInfo.getCpid());
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            default:
                throw new IllegalArgumentException();
        }
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    private String getModelName() {
        if (Build.PRODUCT.length() > 20) {
            return Build.PRODUCT.replaceAll(" ", "_").toLowerCase().substring(0, 19);
        } else {
            return Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
        }
    }

    private static boolean isQuit = false;
    private Timer quitTimer = new Timer();

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ("lcd_s3a01".equals(getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                sharpKeyDownNotResume = true;
            }
        } else if ("lx565ab".equals(getModelName())) {
            if (keyCode == 82 || keyCode == 707 || keyCode == 253) {
                sharpKeyDownNotResume = true;
            }
        } else if ("lcd_xxcae5a_b".equals(getModelName())) {
            if (keyCode == 497 || keyCode == 498 || keyCode == 490) {
                sharpKeyDownNotResume = true;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                sharpKeyDownNotResume = true;
            }
        }

        Log.i(TAG, "onKeyDown");
        if (mItemEntity == null || mIsmartvPlayer == null) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                getActivity().finish();
            }
            return true;
        }
        if (isMenuShow()) {
            return true;
        }
        IAdController adController = mIsmartvPlayer.getAdController();
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mIsPlayingAd || !mIsmartvPlayer.isInPlaybackState() || !isInit) {// !isInit, 视频正在加载时，不断重复按键，知道视频出现，此时若有广告会有问题
                    return true;
                }
                hidePanel();
                if (!isMenuShow()) {
                    showMenu();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // TODO 暂停广告按下消除
                // TODO 悦享看广告一定时间后可以消除
                Log.d(TAG, "DOWN:" + adController + " onPaused:" + mIsOnPaused);
                //隐藏暂停广告
                if (adController != null && mIsOnPaused) {
                    Log.d(TAG, "Invisible pause ad.");
                    adController.hideAd(AdItem.AdType.PAUSE);
                }
                //跳过悦享看广告
                else if (adController != null && adController.isEnableSkipAd()) {
                    Log.d(TAG, "Jump over ad.");
                    adController.skipAd();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (!isPopWindowShow() && mIsmartvPlayer != null && mIsmartvPlayer.isInPlaybackState() && !mIsPlayingAd) {
                    if (!isQuit) {
                        isQuit = true;
                        ExitToast.createToastConfig().show(getActivity().getApplicationContext(), 5000);
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                isQuit = false;
                            }
                        };
                        quitTimer.schedule(task, 5000);
                    } else {
                        ExitToast.createToastConfig().dismiss();
                        exitPlayerWhilePlaying();
                    }
                    return true;
                }
                Log.d(TAG, "BACK:" + adController);
                if (adController != null && mIsInAdDetail) {
                    // TODO 广告详情页面返回键后继续播放视频
                    Log.d(TAG, "From ad detail to player.");
                    mIsInAdDetail = false;
                    adController.hideAd(AdItem.AdType.CLICKTHROUGH);
                    return true;
                }
                if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                    mHandler.removeMessages(MSG_AD_COUNTDOWN);
                }
                getActivity().finish();
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd) {
                    return true;
                }
                if (mIsmartvPlayer.isPlaying()) {
                    mIsOnPaused = true;
                    mIsmartvPlayer.pause();
                    if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                        mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONPAUSE, source);
                    }
                } else {
                    mIsOnPaused = false;
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
                    mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONPAUSE, source);
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
                if (mIsPlayingAd) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        // TODO 前贴片,中插广告按右键跳转至图片或H5,需要指明类型
                        Log.d(TAG, "RIGHT:" + adController);
                        // 从前贴/中插广告跳转到图片或H5
                        if (adController != null && adController.isEnableClickThroughAd()) {
                            Log.d(TAG, "Jump to ad detail.");
                            mIsInAdDetail = true;
                            adController.showAd(AdItem.AdType.CLICKTHROUGH);
                        }
                    }
                    return true;
                }
                if (isMenuShow() || isPopWindowShow()) {
                    return true;
                }
                forwardClick(null);
                return true;
        }
//        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
//                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
//                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
//                keyCode != KeyEvent.KEYCODE_CALL &&
//                keyCode != KeyEvent.KEYCODE_ENDCALL &&
//                !isMenuShow() &&
//                !isPopWindowShow() &&
//                !mIsPlayingAd;
//        if (isKeyCodeSupported) {
//            showPannelDelayOut();
//            return true;
//        }
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
                    mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    return true;
            }
        }
        return false;
    }

    private Timer mBufferingTimer;
    private BufferingTask mBufferingTask;

    private void cancelTimer() {
        if (mBufferingTask != null) {
            mBufferingTask.cancel();
            mBufferingTask = null;
        }
        if (mBufferingTimer != null) {
            mBufferingTimer.cancel();
            mBufferingTimer = null;
            System.gc();
        }

    }

    class BufferingTask extends TimerTask {

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    cancelTimer();
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        showExitPopup(POP_TYPE_BUFFERING_LONG);
                    }
                }
            });

        }
    }

}
