package tv.ismar.player.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
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
import tv.ismar.app.reporter.IsmartvMedia;
import tv.ismar.app.util.NetworkUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.player.PlayerPageContract;
import tv.ismar.player.R;
import tv.ismar.player.databinding.FragmentPlayerBinding;
import tv.ismar.player.media.DaisyVideoView;
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
    //    private static final String ARG_DETAIL_PAGE_ITEM = "ARG_DETAIL_PAGE_ITEM";
//    private static final String ARG_DETAIL_PAGE_CLIP = "ARG_DETAIL_PAGE_CLIP";
//    private static final String ARG_DETAIL_PAGE_POSITION = "ARG_DETAIL_PAGE_POSITION";
//    private static final String ARG_DETAIL_PAGE_QUALITY = "ARG_DETAIL_PAGE_QUALITY";
//    private static final String ARG_DETAIL_PAGE_PATHS = "ARG_DETAIL_PAGE_PATHS";
//    private static final String ARG_DETAIL_PAGE_ADS = "ARG_DETAIL_PAGE_ADS";
    private static final String HISTORYCONTINUE = "上次放映：";
    private static final String PlAYSTART = "即将放映：";
    private static final int MSG_SEK_ACTION = 103;
    private static final int MSG_AD_COUNTDOWN = 104;
    private static final int MSG_SHOW_BUFFERING_LONG = 105;
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
    private DaisyVideoView player_surface;// 夏普585某些ROM退出黑屏一下
    private LinearLayout panel_layout;
    private SeekBar player_seekBar;
    private PlayerMenu playerMenu;
    private ImageView player_logo_image;
    private TextView ad_count_text, ad_vip_text;
    private View ad_vip_layout;
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
    //    private boolean mHasPreLoad;// 已经在详情页实现了预加载
//    private String[] tempPaths;// 预加载时用到
//    private List<AdElementEntity> tempAds;// 预加载时用到
    private boolean isExit = false;// 播放器退出release需要时间，此时的UI事件会导致ANR
    private boolean closePopup = false;// 网速由不正常到正常时判断，关闭弹窗后不做任何操作
    //    private boolean isFinishing;
    private boolean isClickBufferLong;// 夏普s3相关适配，限速切换码率后，恢复网速，导致timerStart无法正常开启

    private FragmentPlayerBinding mBinding;
    private PlayerPageViewModel mModel;
    private PlayerPageContract.Presenter mPresenter;
    private PlayerPagePresenter mPlayerPagePresenter;
    private Animation panelShowAnimation;
    private Animation panelHideAnimation;
    private String source;// 日志上报相关
    private AdImageDialog adImageDialog;
    private Advertisement mAdvertisement;

    private boolean sharpKeyDownNotResume = false; // 夏普电视设置按键Activity样式为Dialog样式
    public boolean mounted = false; // SD卡弹出后操作问题
    private ImageView shadowview;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(int pk, int subPk, String source) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PK, pk);
        args.putInt(ARG_SUB_PK, subPk);
        args.putString(ARG_SOURCE, source);
//        args.putString(ARG_DETAIL_PAGE_ITEM, itemJson);
//        args.putString(ARG_DETAIL_PAGE_CLIP, clipJson);
//        args.putInt(ARG_DETAIL_PAGE_POSITION, historyPosition);
//        args.putInt(ARG_DETAIL_PAGE_QUALITY, historyQuality);
//        args.putStringArray(ARG_DETAIL_PAGE_PATHS, paths);
//        args.putString(ARG_DETAIL_PAGE_ADS, adLists);
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
//        String itemJson = bundle.getString(ARG_DETAIL_PAGE_ITEM);
//        String clipJson = bundle.getString(ARG_DETAIL_PAGE_CLIP);// 注意clipJson地址已解密
//        if (itemJson != null && clipJson != null) {
//            mHasPreLoad = true;
//            mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);
//            mClipEntity = new Gson().fromJson(clipJson, ClipEntity.class);
//            mCurrentPosition = bundle.getInt(ARG_DETAIL_PAGE_POSITION);
//            mCurrentQuality = ClipEntity.Quality.getQuality(bundle.getInt(ARG_DETAIL_PAGE_QUALITY));
//            tempPaths = bundle.getStringArray(ARG_DETAIL_PAGE_PATHS);
//            String adString = bundle.getString(ARG_DETAIL_PAGE_ADS);
//            if (!TextUtils.isEmpty(adString)) {
//                AdElementEntity[] adElementEntities = new Gson().fromJson(adString, AdElementEntity[].class);
//                tempAds = new ArrayList<>();
//                for (AdElementEntity adElement : adElementEntities) {
//                    tempAds.add(adElement);
//                }
//            }
//        }
        mPlayerPagePresenter = new PlayerPagePresenter((BaseActivity) getActivity(), this);
        mModel = new PlayerPageViewModel(getActivity(), mPlayerPagePresenter);
        panelShowAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_up);
        panelHideAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fly_down);
        mPresenter.start();
        mAdvertisement = new Advertisement(getActivity());
        mAdvertisement.setOnVideoPlayListener(this);
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
        player_surface = (DaisyVideoView) contentView.findViewById(R.id.player_surface);
        panel_layout = (LinearLayout) contentView.findViewById(R.id.panel_layout);
        player_seekBar = (SeekBar) contentView.findViewById(R.id.player_seekBar);
        player_logo_image = (ImageView) contentView.findViewById(R.id.player_logo_image);
        ad_vip_layout = contentView.findViewById(R.id.ad_vip_layout);
        ad_count_text = (TextView) contentView.findViewById(R.id.ad_count_text);
        ad_vip_text = (TextView) contentView.findViewById(R.id.ad_vip_text);
        player_menu = (ListView) contentView.findViewById(R.id.player_menu);
        player_buffer_layout = (LinearLayout) contentView.findViewById(R.id.player_buffer_layout);
        player_buffer_img = (ImageView) contentView.findViewById(R.id.player_buffer_img);
        player_buffer_text = (TextView) contentView.findViewById(R.id.player_buffer_text);
        previous = (ImageView) contentView.findViewById(R.id.previous);
        forward = (ImageView) contentView.findViewById(R.id.forward);
        shadowview = (ImageView) contentView.findViewById(R.id.shadowview);
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
        ad_vip_text.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_focus));
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_white));
                        break;
                }
                return false;
            }
        });
        ad_vip_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_focus));
                } else {
                    ad_vip_text.setTextColor(getResources().getColor(R.color.module_color_white));
                }
            }
        });

        player_container.setOnHoverListener(onHoverListener);
        player_container.setOnClickListener(onClickListener);
        player_surface.setOnHoverListener(onHoverListener);
        player_surface.setOnClickListener(onClickListener);
        player_seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "resultCode:" + resultCode + " request:" + requestCode);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode != PAYMENT_SUCCESS_CODE) {
                isNeedOnResume = false;
                finishActivity("finish");
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
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        isNeedOnResume = true;
        switch (type) {
            case EVENT_CLICK_VIP_BUY:
                if (mIsmartvPlayer != null && mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                    toPayPage(mItemEntity.getPk(), 2, 3, null);
                } else {
                    toPayPage(mItemEntity.getPk(), 2, 2, null);
                }
                break;
            case EVENT_CLICK_KEFU:
                isClickKeFu = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PageIntent page = new PageIntent();
                        page.toHelpPage(getActivity());
                    }
                }, 400);
                break;
            case EVENT_COMPLETE_BUY:
                if (mIsmartvPlayer != null) {
                    mIsmartvPlayer.logVideoExit(mCurrentPosition, "finish");
                }
                ItemEntity.Expense expense = mItemEntity.getExpense();
                PageIntentInterface.ProductCategory mode = null;
                if (1 == mItemEntity.getExpense().getJump_to()) {
                    mode = PageIntentInterface.ProductCategory.item;
                }
                toPayPage(mItemEntity.getPk(), expense.getJump_to(), expense.getCpid(), mode);
                break;
        }

        if (mIsmartvPlayer != null) {
            mIsmartvPlayer.stopPlayBack();
            mIsmartvPlayer = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        registerConnectionReceiver();
        if (sharpKeyDownNotResume || mounted) {
            sharpKeyDownNotResume = false;
            mounted = false;
            return;
        }
        // 从客服中心，购买页面返回
        if (isNeedOnResume) {
            isNeedOnResume = false;
            initPlayer();
        } else {
            // 首次进入
            Log.i(TAG, "onResume:");
            fetchItemData();
        }

    }

    @Override
    public void onStop() {
        unregisterConnectionReceiver();
        if (sharpKeyDownNotResume) {
            sharpKeyDownNotResume = false;
            super.onStop();
            return;
        }
        removeBufferingLongTime();
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
        if (mHandler.hasMessages(EVENT_CLICK_VIP_BUY)) {
            mHandler.removeMessages(EVENT_CLICK_VIP_BUY);
        }
        if (mHandler.hasMessages(EVENT_CLICK_KEFU)) {
            mHandler.removeMessages(EVENT_CLICK_KEFU);
        }
        if (mHandler.hasMessages(EVENT_COMPLETE_BUY)) {
            mHandler.removeMessages(EVENT_COMPLETE_BUY);
        }
        if (popDialog != null && popDialog.isShowing()) {
            // 底层报错导致Activity 被销毁，如果再次显示弹出框，会报错
            popDialog.dismiss();
            popDialog = null;
        }
        if (!isNeedOnResume && !isClickKeFu && !mounted && mPresenter != null) {
            mPresenter.stop();
//            if (mIsmartvPlayer != null) {
//                mIsmartvPlayer.stopPlayBack();
//                mIsmartvPlayer = null;
//            }
//            if (BaseActivity.mSmartPlayer != null) {
//                BaseActivity.mSmartPlayer = null;
//            }
        } else if (!isNeedOnResume && !isClickKeFu && mounted) {
            if (mIsmartvPlayer != null) {
                addHistory(mCurrentPosition, false, false);
                mIsmartvPlayer.stopPlayBack();
                mIsmartvPlayer = null;
            }
            mounted = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIsmartvPlayer != null) {
            mIsmartvPlayer.stopPlayBack();
            mIsmartvPlayer = null;
        }
        mModel = null;
    }

    public void buyVipOnShowAd() {
        if (mIsmartvPlayer == null) {
            return;
        }
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
        ad_vip_layout.setVisibility(View.GONE);
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
            showBuffer(null);
        }
    }

    @Override
    public void onBufferEnd() {
        Log.i(TAG, "onBufferEnd");
        if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_QIYI_PLAYER) {
            hideBuffer();
        } else {
            if (mIsmartvPlayer == null || !mIsmartvPlayer.isPlaying()) {
                return;
            }
            if (!isSeeking) {
                hideBuffer();
            }
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
        if (mIsmartvPlayer == null || isExit) {
            return;
        }
        Log.i(TAG, "onPrepared:" + mCurrentPosition + " playingAd:" + mIsPlayingAd);
        mModel.setPanelData(mIsmartvPlayer, mItemEntity.getTitle());
        if (mIsmartvPlayer != null && !mIsmartvPlayer.isPlaying()) {
            mIsmartvPlayer.start();
        }

    }

    private void initPlayer() {
        if (mIsmartvPlayer != null) {
            Log.i(TAG, "mIsmartvPlayer not null when init.");
            mIsmartvPlayer.stopPlayBack();
            mIsmartvPlayer = null;
        }
        player_logo_image.setVisibility(View.GONE);
        mIsPlayingAd = false;
        mIsInAdDetail = false;
        sharpKeyDownNotResume = false;
        mIsPreview = false;
        ad_vip_layout.setVisibility(View.GONE);
        hideMenu();
        hidePanel();
        fetchItemData();
    }

    @Override
    public void onAdStart() {
        Log.i(TAG, "onAdStart");
        shadowview.setVisibility(View.GONE);
        hideBuffer();
        mIsPlayingAd = true;
        ad_vip_layout.setVisibility(View.VISIBLE);
        ad_vip_text.setFocusable(true);
        ad_vip_text.requestFocus();
        mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
    }

    @Override
    public void onAdEnd() {
        Log.i(TAG, "onAdEnd");
        mIsPlayingAd = false;
        ad_vip_layout.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_AD_COUNTDOWN);
    }

    @Override
    public void onMiddleAdStart() {
        Log.i(TAG, "onMiddleAdStart");
        mIsPlayingAd = true;
        ad_vip_layout.setVisibility(View.VISIBLE);
        ad_vip_text.setFocusable(true);
        ad_vip_text.requestFocus();
        mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);

    }

    @Override
    public void onMiddleAdEnd() {
        Log.i(TAG, "onMiddleAdEnd");
        mIsPlayingAd = false;
        ad_vip_layout.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_AD_COUNTDOWN);

    }

    // 奇艺播放器在onPrepared时无法获取到影片时长
    @Override
    public void onStarted() {
        Log.i(TAG, "onStarted");
        if (mIsmartvPlayer == null || mItemEntity == null) {
            return;
        }
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
            shadowview.setVisibility(View.GONE);
            if (mItemEntity.getLiveVideo()) {
                hideBuffer();
            } else {
                player_seekBar.setMax(mIsmartvPlayer.getDuration());
                player_seekBar.setPadding(0, 0, 0, 0);
            }
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
        if ((mIsmartvPlayer == null) || isPopWindowShow() || isQuit || !NetworkUtils.isConnected(getActivity())) {// isQuit 奇艺试看视频结束后没有onCompleted,而是onStop.而部分机型返回键退出时也是调用onStop
            return;
        }
        hideMenu();
        hidePanel();
        timerStop();
        if (mIsPreview) {
            mIsPreview = false;
            if (mItemEntity.getLiveVideo() && "sport".equals(mItemEntity.getContentModel())) {
                addHistory(mCurrentPosition, true, true);
                finishActivity("finish");
            } else {
                addHistory(mCurrentPosition, false, true);
                goOtherPage(EVENT_COMPLETE_BUY);
            }
        } else {
            ItemEntity[] subItems = mItemEntity.getSubitems();
            if (subItems != null) {
                for (int i = 0; i < subItems.length; i++) {
                    if (subItemPk == subItems[i].getPk() && i < subItems.length - 1) {
                        ItemEntity nextItem = subItems[i + 1];
                        if (nextItem != null && nextItem.getClip() != null) {
                            if (mIsmartvPlayer != null) {
                                mIsmartvPlayer.logVideoExit(mCurrentPosition, "next");
                            }
                            // 菜单栏剧集切换
                            createMenu();
                            PlayerMenuItem menuItem = playerMenu.findItem(subItemPk);
                            if (menuItem != null) {
                                menuItem.selected = false;
                            }
                            mItemEntity.setTitle(nextItem.getTitle());
                            mItemEntity.setClip(nextItem.getClip());
                            subItemPk = nextItem.getPk();
                            PlayerMenuItem nextMenuItem = playerMenu.findItem(subItemPk);
                            if (nextMenuItem != null) {
                                nextMenuItem.selected = true;
                            }

                            mCurrentPosition = 0;
                            if (mIsmartvPlayer != null) {
                                mIsmartvPlayer.stopPlayBack();
                                mIsmartvPlayer = null;
                            }
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
            intent.putExtra("source", source);
            startActivity(intent);
            addHistory(0, true, true);
            finishActivity("finish");
        }
    }

    @Override
    public boolean onError(String message) {
        Log.e(TAG, "onError:" + message);
        if (mIsmartvPlayer == null || isDetached() || isExit) {
            return true;
        }
        if (isPopWindowShow()) {
            return true;
        }
        showExitPopup(POP_TYPE_PLAYER_ERROR);
        return true;
    }

    @Override
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {
        if (!isExit && isClickBufferLong) {
            isClickBufferLong = false;
            timerStart(0);
        }
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
        Log.d(TAG, "progressTimerStart: " + delay + " " + mIsPlayingAd + " " + mIsmartvPlayer);
        if (mIsmartvPlayer == null || mIsPlayingAd) {
            return;
        }
        mTimerHandler.removeCallbacks(timerRunnable);
        if (delay > 0) {
            mTimerHandler.postDelayed(timerRunnable, delay);
        } else {
            mTimerHandler.post(timerRunnable);
        }

    }

    private void timerStop() {
        Log.d(TAG, "progressTimerStop: ");
        mTimerHandler.removeCallbacks(timerRunnable);
    }

    private Handler mTimerHandler = new Handler();
    private int historyPosition;// 人为操控断网，再连接网络进入播放器，可能导致进入播放器起播后，网络获取到的是未连接情况

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsmartvPlayer == null || mItemEntity == null || mItemEntity.getLiveVideo() || isExit) {
                mTimerHandler.removeCallbacks(timerRunnable);
                return;
            }
            if (mIsmartvPlayer.isPlaying()) {
                int mediaPosition = mIsmartvPlayer.getCurrentPosition();
                // 播放过程中网络相关
                if (mCurrentPosition == mediaPosition && mediaPosition != historyPosition) {
                    Log.d(TAG, "Network videoBufferingShow：" + isBufferShow());
                    if (!NetworkUtils.isConnected(getActivity())) {
                        // 断开网络，连接网络后会在广播接收中恢复
                        addHistory(mCurrentPosition, true, false);
                        hidePanel();
                        ((BaseActivity) getActivity()).showNoNetConnectDialog();
                        Log.e(TAG, "Network error on timer runnable.");
                        return;
                    } else {
                        // 画面卡住不动，显示loading,由于网速恢复后timerRunnable需要继续显示,故此处需要不断postDelayed
                        // 由于部分机型，画面停止后，多次调用getCurrentPosition会导致onError回调，故时间间隔尽可能长
                        // 还应注意不能一直显示，让buffering的handler清除计时消息
                        if (!isBufferShow()) {
                            showBuffer(null);
                        }
                        mTimerHandler.postDelayed(timerRunnable, 2000);
                        return;
                    }
                }
                // 播放过程中网络相关End

                if (isBufferShow()) {
                    // 画面开始播放，buffer就需要消失
                    hideBuffer();
                }
                // 显示切换画质提示框后，恢复网络，弹窗需要消失
                if (isPopWindowShow()) {
                    removeBufferingLongTime();
                }

                if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                    // 视云播放器，onSeekComplete回调完成后，getCurrentPosition获取位置不是最新seekTo的位置,2s以后再更新进度条
                    if (isSeeking) {
                        isSeeking = false;
                        showPannelDelayOut();
                        mTimerHandler.postDelayed(timerRunnable, 2000);
                        return;
                    }
                } else {
                    if (isSeeking) {// 奇艺视频seek结束后需要置为false
                        isSeeking = false;
                        showPannelDelayOut();
                    }
                }

                // 更新进度条
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
                    if (isExit || mIsmartvPlayer == null) {
                        return;
                    }
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
                case MSG_SHOW_BUFFERING_LONG:
                    if (getActivity() != null && !isExit) {
                        if (!NetworkUtils.isConnected(getActivity())) {// 网络断开情况下无需显示切换分辨率
                            addHistory(mCurrentPosition, true, false);
                            ((BaseActivity) getActivity()).showNoNetConnectDialog();
                            Log.e(TAG, "Network error on MSG_SHOW_BUFFERING_LONG.");
                            return;
                        }
                        showExitPopup(POP_TYPE_BUFFERING_LONG);
                    }
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
            int history_sub_pk = 0;
            if (mHistory != null) {
                history_sub_pk = Utils.getItemPk(mHistory.sub_url);
            }
            Log.i(TAG, "loadItem-ExtraSubItemPk:" + subItemPk + " historySubPk:" + history_sub_pk);
            if (subItemPk <= 0) {
                // 点击播放按钮时，如果有历史记录，应该播放历史记录的subItemPk,默认播放第一集
                if (history_sub_pk > 0) {
                    subItemPk = history_sub_pk;
                    mCurrentPosition = (int) mHistory.last_position;
                } else {
                    subItemPk = subItems[0].getPk();
                    mCurrentPosition = 0;
                }

            } else {
                if (subItemPk != history_sub_pk) {
                    mCurrentPosition = 0;
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
                            mIsPreview = true;
                            mItemEntity.setLiveVideo(false);
                            mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);
                        }


                    }

                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "play check fail");
                    ItemEntity.Preview preview = mItemEntity.getPreview();
                    mItemEntity.setTitle(previewTitle);
                    mIsPreview = true;
                    mItemEntity.setLiveVideo(false);
                    mPresenter.fetchMediaUrl(preview.getUrl(), sign, code);

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
        isSeeking = false;
        isExit = false;
        timerStop();
        // 每次进入创建播放器前先获取历史记录，历史播放位置，历史分辨率，手动切换剧集例外
        if (!isSwitchTelevision) {
            initHistory();
        }
        if (mCurrentPosition > 0) {
            if (!mItemEntity.getLiveVideo()) {
                showBuffer(HISTORYCONTINUE + getTimeString(mCurrentPosition));
            }
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
//        if (mHasPreLoad) {
//            // 视云预加载
//            playerMode = PlayerBuilder.MODE_PRELOAD_PLAYER;
//        } else {
        PlayerBuilder.getInstance().release();
        String iqiyi = mClipEntity.getIqiyi_4_0();
        if (Utils.isEmptyText(iqiyi)) {
            // 片源为视云
            playerMode = PlayerBuilder.MODE_SMART_PLAYER;
        } else {
            // 片源为爱奇艺
            playerMode = PlayerBuilder.MODE_QIYI_PLAYER;
        }
//        }

        Log.i(TAG, "mCurrentPosition:" + mCurrentPosition);
        historyPosition = mCurrentPosition;
        mIsmartvPlayer = PlayerBuilder.getInstance()
                .setPlayerMode(playerMode)
                .setItemEntity(mItemEntity)
                .setStartPosition(mCurrentPosition)
                .setIsPreview(mIsPreview)
                .build();
        mIsmartvPlayer.setContext(getActivity());
        mIsmartvPlayer.setContainer(player_container);
        mIsmartvPlayer.setDaisyVideoView(player_surface);
        mIsmartvPlayer.setOnBufferChangedListener(this);
        mIsmartvPlayer.setOnStateChangedListener(this);
        mIsmartvPlayer.setOnVideoSizeChangedListener(this);
        mIsmartvPlayer.setOnInfoListener(this);
        mIsmartvPlayer.setUser(mUser);
//        if (mHasPreLoad) {
//            mIsmartvPlayer.setSmartPlayer(BaseActivity.mSmartPlayer, tempPaths, tempAds);
//        }
        // 日志上报相关 ---Start---
        IsmartvMedia ismartvMedia = new IsmartvMedia(itemPK, subItemPk);
        ismartvMedia.setTitle(mItemEntity.getTitle());
        ismartvMedia.setClipPk(mItemEntity.getClip().getPk());
        ismartvMedia.setChannel(BaseActivity.baseChannel);
        ismartvMedia.setSource(source);
        ismartvMedia.setSection(BaseActivity.baseSection);
        // 日志上报相关 ---End-----

        mIsmartvPlayer.setDataSource(ismartvMedia, mClipEntity, mCurrentQuality, adList, new IPlayer.OnDataSourceSetListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "player init success." + mIsmartvPlayer);
                if (mIsmartvPlayer == null) {
                    return;
                }
//                if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_QIYI_PLAYER) {
                mIsmartvPlayer.prepareAsync();
                mCurrentQuality = mIsmartvPlayer.getCurrentQuality();
//                } else {
//                    if (mHasPreLoad) {
//                        mHasPreLoad = false;
//                    } else {
//                        mCurrentQuality = mIsmartvPlayer.getCurrentQuality();
//                    }
//                }
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
        Log.i(TAG, "Show pause ad.");
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

        if (mIsPreview && !isClickKeFu) {
            mCurrentPosition = 0;
        }

    }

    private void addHistory(int last_position, boolean sendToServer, boolean isComplete) {
        if (mItemEntity == null || mIsmartvPlayer == null || mIsPlayingAd) {
            return;
        }
        int completePosition = -1;
        if (isComplete) {
            completePosition = mIsmartvPlayer.getDuration();
        }
        Log.i(TAG, "addHistory");
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
            historyManager.addHistory(history, "yes", completePosition);
        else
            historyManager.addHistory(history, "no", completePosition);

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
        if (mIsmartvPlayer == null) {
            return true;
        }
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
                ((BaseActivity) getActivity()).showNoNetConnectDialog();
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
            mIsOnPaused = false;// 暂停以后切换画质
            if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                timerStop();
                showBuffer(null);
            }
            mCurrentPosition = mIsmartvPlayer.getCurrentPosition();
            mIsmartvPlayer.setStartPosition(mCurrentPosition);
            mIsmartvPlayer.switchQuality(clickQuality);

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
                    if (mIsmartvPlayer != null) {
                        mIsmartvPlayer.logVideoExit(mCurrentPosition, "next");
                    }
                    mCurrentPosition = 0;
                    timerStop();
                    mIsmartvPlayer.stopPlayBack();
                    mIsmartvPlayer = null;

                    ItemEntity.Clip clip = subItem.getClip();
                    String sign = "";
                    String code = "1";
                    mItemEntity.setTitle(subItem.getTitle());
                    mItemEntity.setClip(clip);
                    subItemPk = subItem.getPk();
                    isSwitchTelevision = true;

                    player_logo_image.setVisibility(View.GONE);

                    showBuffer(PlAYSTART + mItemEntity.getTitle());
                    if (clip != null) {
                        mPresenter.fetchMediaUrl(clip.getUrl(), sign, code);
                    }
                    ret = true;
                    break;
                }
            }
        } else if (id == MENU_KEFU_ID) {
            mCurrentPosition = mIsmartvPlayer.getCurrentPosition();
            addHistory(mCurrentPosition, false, false);
            timerStop();
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
        Log.d(TAG, "showBuffer:" + msg + " " + mIsmartvPlayer);
        if (isExit) {
            return;
        }
        // 如果显示buffer,就需要发送50延时消息，显示加载时间过长
        // 显示buffer前先判断网络是否连接
        if (!NetworkUtils.isConnected(getActivity())) {
            // 断开网络，连接网络后会在广播接收中恢复
            addHistory(mCurrentPosition, true, false);
            hidePanel();
            timerStop();
            ((BaseActivity) getActivity()).showNoNetConnectDialog();
            return;
        }

        if (mIsmartvPlayer != null) {// 只要显示buffer就开始计时
            if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
                mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
            }
            mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUFFERING_LONG, 50 * 1000);
        }

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
        // buffer消失，就需要remove50秒延时消息
        removeBufferingLongTime();

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
            if (isExit) {
                return true;
            }
            int what = event.getAction();
            switch (what) {
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (!mIsPlayingAd && isInit && mItemEntity != null && !mItemEntity.getLiveVideo()) {
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
                    || isBufferShow() || isExit) {
                return;
            }
            if (isMenuShow()) {
                hideMenu();
                return;
            }
            playPauseVideo();
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

    private void finishActivity(String to) {
        isExit = true;
//        cancelTimer();
        if (mIsmartvPlayer != null) {
            mIsmartvPlayer.logVideoExit(mCurrentPosition, to);
        }
        if (mIsmartvPlayer != null) {
            mIsmartvPlayer.stopPlayBack();
            mIsmartvPlayer = null;
        }
        getActivity().finish();
    }

    private void exitPlayerWhilePlaying() {
        if (mHandler.hasMessages(MSG_SEK_ACTION)) {
            mHandler.removeMessages(MSG_SEK_ACTION);
        }
        timerStop();
        removeBufferingLongTime();
        hideBuffer();
        hidePanel();
        if (!mIsPlayingAd) {
            addHistory(mCurrentPosition, true, false);
        }
        finishActivity("source");
    }

    private ModuleMessagePopWindow popDialog;

    private boolean isPopWindowShow() {
        return popDialog != null && popDialog.isShowing();
    }

    private void showExitPopup(final byte popType) {
        if (isExit) {
            return;
        }
        if (popDialog != null && popDialog.isShowing()) {
            popDialog.dismiss();
            popDialog = null;
        }
        String message = getString(R.string.player_error);
        String cancelText = getString(R.string.player_pop_cancel);
        String confirmText = getString(R.string.player_pop_ok);
        boolean hideCancel = false;
        switch (popType) {
            case POP_TYPE_BUFFERING_LONG:
                if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
                    mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
                }
                message = getString(R.string.player_buffering_long);
                confirmText = getString(R.string.player_pop_cancel);
                cancelText = getString(R.string.player_pop_switch_quality);
                break;
            case POP_TYPE_PLAYER_ERROR:
                timerStop();
                hideBuffer();
                hidePanel();
                hideCancel = true;
                break;
        }
        popDialog = new ModuleMessagePopWindow(getActivity());
        popDialog.setConfirmBtn(confirmText);
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
                        if (mIsmartvPlayer != null) {
                            mIsmartvPlayer.logVideoExit(mCurrentPosition, "source");
                        }
                        // 播放器异常情况,判断播放进度临界值,剩余时长8分钟为界,小于8分钟下次从头播放
                        int value = 8 * 1000 * 60;
                        if (mIsmartvPlayer != null && (mIsmartvPlayer.getDuration() - mCurrentPosition <= value)) {
                            mCurrentPosition = 0;
                        }
                        if (!mIsPlayingAd) {
                            addHistory(mCurrentPosition, true, false);
                        }
                        mIsmartvPlayer = null;
                        finishActivity("source");
                        break;
                    case POP_TYPE_BUFFERING_LONG:
                        if (closePopup) {
                            closePopup = false;
                            return;
                        }
                        if (!isExit) {
                            isClickBufferLong = true;
                            if (mCurrentQuality == null) {
                                Log.e(TAG, "mCurrentQuality:" + mCurrentQuality);
                                return;
                            }
                            if (!popDialog.isConfirmClick) {
                                showBuffer(null);
//                                isClickBufferLongSwitch = true;
                                if (!isMenuShow()) {
                                    if (isPanelShow()) {
                                        hidePanel();
                                    }
                                    createMenu();
                                    ItemEntity[] subItems = mItemEntity.getSubitems();
                                    if (subItems != null && subItems.length > 0 && !mIsPreview) {
                                        // 电视剧
                                        playerMenu.showQuality(1);
                                    } else {
                                        // 电影
                                        playerMenu.showQuality(0);
                                    }
                                }
                            } else {
                                // 重新加载
                                if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                                    timerStop();
                                    showBuffer(null);
                                }
                                mIsmartvPlayer.setStartPosition(mCurrentPosition);
                                mIsmartvPlayer.switchQuality(mCurrentQuality);
                            }
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

    private void playPauseVideo() {
        if (mItemEntity == null || mIsmartvPlayer == null || mItemEntity.getLiveVideo()) {
            return;
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
    }

    private static boolean isQuit = false;
    private Timer quitTimer = new Timer();

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isExit) {
            return true;
        }
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
                    ad_vip_layout.setVisibility(View.VISIBLE);
                    ad_vip_text.setFocusable(true);
                    ad_vip_text.requestFocus();
                    return true;
                }
                if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
                    mHandler.removeMessages(MSG_AD_COUNTDOWN);
                }
                finishActivity("source");
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd || mItemEntity.getLiveVideo()) {
                    return true;
                }
                playPauseVideo();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd || mItemEntity.getLiveVideo()) {
                    return true;
                }
                if (!mIsmartvPlayer.isPlaying()) {
                    mIsOnPaused = false;
                    mIsmartvPlayer.start();
                    hidePanel();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd || mItemEntity.getLiveVideo()) {
                    return true;
                }
                if (mIsmartvPlayer.isPlaying()) {
                    mIsOnPaused = true;
                    mIsmartvPlayer.pause();
                    if (mIsmartvPlayer.getPlayerMode() == PlayerBuilder.MODE_SMART_PLAYER) {
                        mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONPAUSE, source);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (isMenuShow() || isPopWindowShow() || mIsPlayingAd || mItemEntity.getLiveVideo()) {
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
                            ad_vip_layout.setVisibility(View.GONE);
                            adController.showAd(AdItem.AdType.CLICKTHROUGH);
                        }
                    }
                    return true;
                }
                if (isMenuShow() || isPopWindowShow() || mItemEntity.getLiveVideo()) {
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
                    if (mIsmartvPlayer != null) {
                        mHandler.sendEmptyMessageDelayed(MSG_SEK_ACTION, 1000);
                    }
                    return true;
            }
        }
        return false;
    }

    private void removeBufferingLongTime() {
        if (mHandler.hasMessages(MSG_SHOW_BUFFERING_LONG)) {
            mHandler.removeMessages(MSG_SHOW_BUFFERING_LONG);
        }
        if (popDialog != null && popDialog.isShowing()) {
            closePopup = true;
            popDialog.dismiss();
            popDialog = null;
        }

    }

    private ConnectionChangeReceiver connectionChangeReceiver;

    private void registerConnectionReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectionChangeReceiver = new ConnectionChangeReceiver();
        getActivity().registerReceiver(connectionChangeReceiver, filter);
    }

    private void unregisterConnectionReceiver() {
        if (connectionChangeReceiver != null) {
            getActivity().unregisterReceiver(connectionChangeReceiver);
        }
    }

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isExit) {
                BaseActivity baseActivity = ((BaseActivity) getActivity());
                if (baseActivity == null) {
                    return;
                }
                if (NetworkUtils.isConnected(context)) {
                    baseActivity.dismissNoNetConnectDialog();
                    timerStart(0);
                } else if (isBufferShow() && !isPopWindowShow()) {
                    hideBuffer();
                    hidePanel();
                    timerStop();
                    addHistory(mCurrentPosition, true, false);
                    baseActivity.showNoNetConnectDialog();
                }
            }
        }
    }

}
