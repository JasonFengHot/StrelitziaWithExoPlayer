package tv.ismar.homepage.view;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.VideoView;

import com.blankj.utilcode.utils.StringUtils;
import com.konka.android.media.KKMediaPlayer;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.ad.AdsUpdateService;
import tv.ismar.app.ad.AdvertiseManager;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Util;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.db.AdvertiseTable;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.homepage.R;
import tv.ismar.homepage.adapter.ChannelRecyclerAdapter;
import tv.ismar.homepage.adapter.HorizontalSpacesItemDecoration;
import tv.ismar.homepage.adapter.OnItemActionListener;
import tv.ismar.homepage.fragment.ChannelBaseFragment;
import tv.ismar.homepage.fragment.ChildFragment;
import tv.ismar.homepage.fragment.EntertainmentFragment;
import tv.ismar.homepage.fragment.FilmFragment;
import tv.ismar.homepage.fragment.GuideFragment;
import tv.ismar.homepage.fragment.MessageDialogFragment;
import tv.ismar.homepage.fragment.SportFragment;
import tv.ismar.homepage.widget.Position;

//import org.apache.commons.lang3.StringUtils;

/**
 * Created by huaijie on 5/18/15.
 */
public class HomePageActivity extends BaseActivity implements HeadFragment.HeadItemClickListener {
    private static final String TAG = "TVGuideActivity";
    private static final int SWITCH_PAGE = 0X01;
    private static final int SWITCH_PAGE_FROMLAUNCH = 0X02;
    private ChannelBaseFragment currentFragment;
    private ChannelBaseFragment lastFragment;

    private HeadFragment headFragment;
    private ModuleMessagePopWindow exitPopup;
    /**
     * advertisement start
     */
//    private static final String DEFAULT_ADV_PICTURE = "file:///android_asset/poster.png";
//    private static final int TIME_COUNTDOWN = 0x0001;
//
//    private static final int[] secondsResId = {R.drawable.second_1, R.drawable.second_1, R.drawable.second_2,
//            R.drawable.second_3, R.drawable.second_4, R.drawable.second_5};
//    private ImageView adverPic;
//    private ImageView timerText;
//    private Handler messageHandler;
//    private AdvertisementManager mAdvertisementManager;
    private static final int MSG_AD_COUNTDOWN = 0x01;
    private VideoView ad_video;
    private ImageView ad_pic;
    private Button ad_timer;
    private AdvertiseManager advertiseManager;
    private List<AdvertiseTable> launchAds;
    private int countAdTime = 0;
    private int currentImageAdCountDown = 0;
    private boolean isStartImageCountDown = false;
    private boolean isPlayingVideo = false;
    private int playIndex;
    private FrameLayout layout_advertisement;
    private FrameLayout layout_homepage;
    /**
     * advertisement end
     */


    /**
     * PopupWindow
     */
    PopupWindow updatePopupWindow;

    PopupWindow netErrorPopupWindow;

    private LinearLayout channelListView;

    private View contentView;
    private ImageView home_scroll_left;
    private ImageView home_scroll_right;
    private ChannelChange channelChange = ChannelChange.CLICK_CHANNEL;
    private String homepage_template;
    private String homepage_url;

    private boolean scrollFromBorder;
    private ScrollType scrollType = ScrollType.right;
    private String lastviewTag;
    private int lastchannelindex = -1;
    private boolean rightscroll;
    private LeavePosition leavePosition = LeavePosition.RightBottom;
    private ImageView home_shadow_view;
    private static int channelscrollIndex = 0;

    public boolean isneedpause;

    private FragmentSwitchHandler fragmentSwitch;
    private BitmapDecoder bitmapDecoder;
    private Handler netErrorPopupHandler;
    private Runnable netErrorPopupRunnable;
    public static String brandName;

    @Override
    public void onUserCenterClick() {
        PageIntent pageIntent = new PageIntent();
        pageIntent.toUserCenter(this);
    }

    @Override
    public void onHistoryClick() {
        PageIntent pageIntent = new PageIntent();
        pageIntent.toHistory(this);
    }

    @Override
    public void onFavoriteClick() {
        PageIntent pageIntent = new PageIntent();
        pageIntent.toFavorite(this);
    }

    @Override
    public void onSearchClick() {
        PageIntent pageIntent = new PageIntent();
        pageIntent.toSearch(this);
    }

    private enum LeavePosition {
        LeftTop,
        LeftBottom,
        RightTop,
        RightBottom
    }

    public LeavePosition getLeavePosition() {
        return leavePosition;
    }

    public void setLeavePosition(LeavePosition leavePosition) {
        this.leavePosition = leavePosition;
    }

    private Position mCurrentChannelPosition = new Position(new Position.PositioinChangeCallback() {
        @Override
        public void onChange(int position) {
            if (channelEntityList.isEmpty()) {
                return;
            }
            if (position == 0) {
                home_scroll_left.setVisibility(View.GONE);
                if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                    channelChange = ChannelChange.RIGHT_ARROW;
            } else {
                home_scroll_left.setVisibility(View.VISIBLE);
            }

            if (position == channelEntityList.size() - 1) {
                home_scroll_right.setVisibility(View.GONE);
                if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                    channelChange = ChannelChange.LEFT_ARROW;
            } else {
                home_scroll_right.setVisibility(View.VISIBLE);
            }
            Message msg = new Message();
            msg.arg1 = position;
            msg.what = SWITCH_PAGE;
            if (fragmentSwitch.hasMessages(SWITCH_PAGE))
                fragmentSwitch.removeMessages(SWITCH_PAGE);
            fragmentSwitch.sendMessageDelayed(msg, 300);
            if (!scrollFromBorder) {
                home_tab_list.requestFocus();
            }
        }
    });

    private View.OnFocusChangeListener scrollViewListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                int i = v.getId();
                if (i == R.id.home_scroll_left) {
                    scrollFromBorder = true;
                    scrollType = ScrollType.left;
                    channelChange = ChannelChange.LEFT_ARROW;
                    recyclerAdapter.arrowScroll(View.FOCUS_LEFT, true);
                    rightscroll = true;


                } else if (i == R.id.home_scroll_right) {
                    scrollFromBorder = true;
                    scrollType = ScrollType.right;
                    channelChange = ChannelChange.RIGHT_ARROW;
                    recyclerAdapter.arrowScroll(View.FOCUS_RIGHT, true);
                    rightscroll = false;

                }
            }
        }
    };

    // 定时器
    private Timer sensorTimer;
    private MyTimerTask myTimerTask;

    class MyTimerTask extends TimerTask {

        private int width;
        private int hoverOnArrow; // 0表示左侧，1表示右侧

        MyTimerTask(int arrow, int width) {
            this.hoverOnArrow = arrow;
            this.width = width;
        }

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    autoScroll(hoverOnArrow, width);
                    cancelTimer();
                }
            });
        }

    }

    private int scrollPosition = -1;

    private void autoScroll(int direction, int width) {
        switch (direction) {
            case 0:// left
                if (scrollPosition == 0) {
                    home_tab_list.smoothScrollToPosition(0);
                } else {
                    home_tab_list.smoothScrollBy(-(width + mTabSpace), 0);
                }
                break;
            case 1:// right
                if (scrollPosition == recyclerAdapter.getItemCount() - 1) {
                    home_tab_list.smoothScrollToPosition(recyclerAdapter.getItemCount() - 1);
                } else {
                    home_tab_list.smoothScrollBy(width + mTabSpace, 0);
                }
                break;
        }
    }

    private void checkScroll(int position, long delay) {
//        Log.i("LH/","checkPosition:"+position+" isSendMessage:"+isSendMessage);
        View view = home_tab_list.getLayoutManager().findViewByPosition(position);
        if (view == null) {
            return;
        }
        int tabMargin = getResources().getDimensionPixelSize(R.dimen.tv_guide_channel_margin_lr) - mTabSpace;
        int tabRightX = Util.getDisplayPixelWidth(HomePageActivity.this) - tabMargin;
        int[] currentPos = new int[2];
        view.getLocationOnScreen(currentPos);
        int currentWidth = view.getWidth();
        scrollPosition = position;

        if (currentPos[0] + currentWidth > tabRightX + 1) {
            if (delay == 0) {
                autoScroll(1, currentWidth);
            } else {
                if (sensorTimer == null) {
                    sensorTimer = new Timer();
                    myTimerTask = new MyTimerTask(1, currentWidth);
                    sensorTimer.schedule(myTimerTask, 500, 500);
                }
            }
        } else if (currentPos[0] < tabMargin) {
            if (delay == 0) {
                autoScroll(0, currentWidth);
            } else {
                if (sensorTimer == null) {
                    sensorTimer = new Timer();
                    myTimerTask = new MyTimerTask(0, currentWidth);
                    sensorTimer.schedule(myTimerTask, 500, 500);
                }
            }
        }

    }

    private void cancelTimer() {
        if (myTimerTask != null) {
            myTimerTask.cancel();
            myTimerTask = null;
        }
        if (sensorTimer != null) {
            sensorTimer.cancel();
            sensorTimer = null;
            System.gc();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        if (savedInstanceState != null)
            savedInstanceState = null;
        super.onCreate(savedInstanceState);
//        new Thread(new InitializeProcess(this)).start();
        fragmentSwitch = new FragmentSwitchHandler(this);
        activityTag = "BaseActivity";
        contentView = LayoutInflater.from(this).inflate(R.layout.activity_tv_guide, null);
        setContentView(contentView);
        /**
         * advertisement start
         */
        layout_advertisement = (FrameLayout) findViewById(R.id.layout_advertisement);
        layout_homepage = (FrameLayout) findViewById(R.id.layout_homepage);
        ad_video = (VideoView) findViewById(R.id.ad_video);
        ad_pic = (ImageView) findViewById(R.id.ad_pic);
        ad_timer = (Button) findViewById(R.id.ad_timer);

        advertiseManager = new AdvertiseManager(getApplicationContext());
        launchAds = advertiseManager.getAppLaunchAdvertisement();
        for (AdvertiseTable adTable : launchAds) {
            int duration = adTable.duration;
            countAdTime += duration;
        }
        playLaunchAd(0);
        /**
         * advertisement end
         */

        homepage_template = getIntent().getStringExtra("homepage_template");
        homepage_url = getIntent().getStringExtra("homepage_url");
        final View vv = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                vv.setBackgroundDrawable(bitmapDrawable);
            }
        });


        vv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean ret = false;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        Log.i("zhangjiqiang", "KEYCODE_DPAD_LEFT getNextFocusRightId==" + v.getNextFocusLeftId() + "//getLeft" + v.getLeft());
                        ret = true;
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        Log.i("zhangjiqiang", "KEYCODE_DPAD_RIGHT getNextFocusRightId==" + v.getNextFocusRightId() + "//getRight" + v.getRight());
                        ret = true;
                        break;
                }
                return ret;
            }
        });

        initViews();
        getHardInfo();
        Properties sysProperties = new Properties();
        try {
            InputStream is = getAssets().open("configure/setup.properties");
            sysProperties.load(is);
            brandName = sysProperties.getProperty("platform");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fetchChannels();
    }

    private boolean hoverOnArrow;

    private View.OnHoverListener onArrowHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    hoverOnArrow = true;
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    hoverOnArrow = false;
                    break;
            }
            return false;
        }
    };

    private int mTabSpace;

    private void initViews() {
        Bundle bundle = new Bundle();
        bundle.putString("type", HeadFragment.HEADER_HOMEPAGE);
        bundle.putString("channel_name", getString(R.string.str_home));
        headFragment = new HeadFragment();
        headFragment.setHeadItemClickListener(this);
        headFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.home_head, headFragment)
                .commit();

        home_tab_list = (RecyclerView) findViewById(R.id.home_tab_list);
        recyclerAdapter = new ChannelRecyclerAdapter(this, channelEntityList, home_tab_list);
        home_tab_list.setAdapter(recyclerAdapter);
        home_tab_list.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        home_tab_list.setLayoutManager(layoutManager);
        mTabSpace = getResources().getDimensionPixelSize(R.dimen.tv_guide_h_grid_view_horizontalSpacing);
        HorizontalSpacesItemDecoration decoration = new HorizontalSpacesItemDecoration(
                mTabSpace,
                getResources().getDimensionPixelSize(R.dimen.fragment_padding_lr),
                recyclerAdapter);
        home_tab_list.addItemDecoration(decoration);

        home_scroll_left = (ImageView) findViewById(R.id.home_scroll_left);
        home_scroll_right = (ImageView) findViewById(R.id.home_scroll_right);
        home_scroll_left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // longhai add
                recyclerAdapter.arrowScroll(View.FOCUS_LEFT, false);
            }
        });
        home_scroll_right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // longhai add
                recyclerAdapter.arrowScroll(View.FOCUS_RIGHT, false);
            }
        });
        home_scroll_left.setOnHoverListener(onArrowHoverListener);
        home_scroll_right.setOnHoverListener(onArrowHoverListener);
        home_scroll_left.setOnFocusChangeListener(scrollViewListener);
        home_scroll_right.setOnFocusChangeListener(scrollViewListener);
        home_shadow_view = (ImageView) findViewById(R.id.home_shadow_view);

        home_tab_list.requestFocus();
        recyclerAdapter.setOnItemActionListener(new OnItemActionListener() {

            @Override
            public void onItemHoverListener(View v, MotionEvent event, int position) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                    case MotionEvent.ACTION_HOVER_MOVE:
                        checkScroll(position, 500);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        cancelTimer();
                        break;
                }
            }

            @Override
            public void onItemFocusListener(View v, boolean hasFocus, int position) {

            }

            @Override
            public void onItemClickListener(View v, int position) {
                checkScroll(position, 0);

                channelChange = ChannelChange.CLICK_CHANNEL;
                mCurrentChannelPosition.setPosition(position);


            }

            @Override
            public void onItemSelectedListener(int position) {
                checkScroll(position, 0);

                mCurrentChannelPosition.setPosition(position);
            }
        });

        home_tab_list.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.i("LH/", "onFocusChange:" + hasFocus + " hover:" + hoverOnArrow + " v:" + v.isHovered());
                View currentView = home_tab_list.getLayoutManager().findViewByPosition(recyclerAdapter.getSelectedPosition());
                if (currentView != null) {
                    if (hasFocus) {
                        LinearLayout channel_item_back = (LinearLayout) currentView.findViewById(R.id.channel_item_back);
                        if (v.isHovered() && !hoverOnArrow) {
                            channel_item_back.setBackgroundResource(R.drawable.channel_item_focus);
                        } else {
                            channel_item_back.setBackgroundResource(R.drawable.channel_item_selectd_focus);
                        }
                    } else {
                        LinearLayout channel_item_back = (LinearLayout) currentView.findViewById(R.id.channel_item_back);
                        channel_item_back.setBackgroundResource(R.drawable.channel_item_focus);
                    }
                }
            }
        });

        home_tab_list.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.i("LH/","onKeyDown:");
                int selectedPosition = recyclerAdapter.getSelectedPosition();
                boolean isHandled = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            if (recyclerAdapter.getOnHoveredPosition() != -1) {
                                if (recyclerAdapter.onHoveredView != null && recyclerAdapter.getOnHoveredPosition() != selectedPosition) {
                                    LinearLayout channel_item_back1 = (LinearLayout) recyclerAdapter.onHoveredView.findViewById(R.id.channel_item_back);
                                    channel_item_back1.setBackgroundResource(R.drawable.channel_item_normal);
                                    TextView channel_item_text1 = (TextView) recyclerAdapter.onHoveredView.findViewById(R.id.channel_item);
                                    channel_item_text1.setTextColor(getResources().getColor(R.color._ffffff));
                                    recyclerAdapter.setOnHoveredPosition(-1);
                                }
                            }
                            v.setHovered(false);
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            isHandled = true;
                            if (recyclerAdapter.getOnHoveredPosition() >= 0) {
                                if (recyclerAdapter.onHoveredView != null) {
                                    LinearLayout channel_item_back = (LinearLayout) recyclerAdapter.onHoveredView.findViewById(R.id.channel_item_back);
                                    channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
                                    TextView channel_item_text = (TextView) recyclerAdapter.onHoveredView.findViewById(R.id.channel_item);
                                    channel_item_text.setTextColor(getResources().getColor(R.color._ffffff));
                                }

                                if (recyclerAdapter.getOnHoveredPosition() == 0) {
                                    if (selectedPosition > 0) {
                                        recyclerAdapter.setLastSelectedPosition(selectedPosition);
                                        recyclerAdapter.setSelectedPosition(0);
                                        mCurrentChannelPosition.setPosition(0);
                                        recyclerAdapter.changeStatus();
                                    }
                                } else {
                                    recyclerAdapter.setLastSelectedPosition(selectedPosition);
                                    recyclerAdapter.setSelectedPosition(recyclerAdapter.getOnHoveredPosition() - 1);
                                    mCurrentChannelPosition.setPosition(recyclerAdapter.getOnHoveredPosition() - 1);
                                    recyclerAdapter.changeStatus();
                                }
                                recyclerAdapter.setOnHoveredPosition(-1);
                            } else {
                                if (selectedPosition > 0) {
                                    recyclerAdapter.setLastSelectedPosition(selectedPosition);
                                    selectedPosition -= 1;
                                    recyclerAdapter.setSelectedPosition(selectedPosition);
                                    mCurrentChannelPosition.setPosition(selectedPosition);
                                    recyclerAdapter.changeStatus();

                                }
                            }
                            checkScroll(recyclerAdapter.getSelectedPosition(), 0);
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            isHandled = true;
                            if (recyclerAdapter.getOnHoveredPosition() >= 0) {
                                if (recyclerAdapter.onHoveredView != null) {
                                    LinearLayout channel_item_back = (LinearLayout) recyclerAdapter.onHoveredView.findViewById(R.id.channel_item_back);
                                    channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
                                    TextView channel_item_text = (TextView) recyclerAdapter.onHoveredView.findViewById(R.id.channel_item);
                                    channel_item_text.setTextColor(getResources().getColor(R.color._ffffff));
                                }

                                if (recyclerAdapter.getOnHoveredPosition() == recyclerAdapter.getItemCount() - 1) {
                                    if (selectedPosition < recyclerAdapter.getItemCount() - 1) {
                                        recyclerAdapter.setLastSelectedPosition(selectedPosition);
                                        recyclerAdapter.setSelectedPosition(recyclerAdapter.getItemCount() - 1);
                                        mCurrentChannelPosition.setPosition(recyclerAdapter.getItemCount() - 1);
                                        recyclerAdapter.changeStatus();
                                    }
                                } else {
                                    recyclerAdapter.setLastSelectedPosition(selectedPosition);
                                    recyclerAdapter.setSelectedPosition(recyclerAdapter.getOnHoveredPosition() + 1);
                                    mCurrentChannelPosition.setPosition(recyclerAdapter.getOnHoveredPosition() + 1);
                                    recyclerAdapter.changeStatus();
                                }
                                recyclerAdapter.setOnHoveredPosition(-1);
                            } else {
                                if (selectedPosition < recyclerAdapter.getItemCount() - 1) {
                                    recyclerAdapter.setLastSelectedPosition(selectedPosition);
                                    selectedPosition += 1;
                                    recyclerAdapter.setSelectedPosition(selectedPosition);
                                    mCurrentChannelPosition.setPosition(selectedPosition);
                                    recyclerAdapter.changeStatus();
                                }
                            }
                            checkScroll(recyclerAdapter.getSelectedPosition(), 0);
                            break;
                    }
                }
                return isHandled;
            }
        });

    }

    @Override
    public void onBackPressed() {
        showExitPopup(contentView);
    }

    private static boolean channelflag = false;

    /**
     * fetch channel
     */
    private void fetchChannels() {
        mSkyService.fetchDpi();
        mSkyService.apiTvChannels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ChannelEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ChannelEntity[] channelEntities) {
                        fillChannelLayout(channelEntities);
                    }
                });
    }

    private void fillChannelLayout(ChannelEntity[] channelEntities) {
        if (neterrorshow)
            return;
        home_scroll_right.setVisibility(View.VISIBLE);
        ChannelEntity[] mChannelEntitys = channelEntities;
        if (!channelEntityList.isEmpty()) {
            return;
        }

        ChannelEntity launcher = new ChannelEntity();
        launcher.setChannel("launcher");
        launcher.setName("首页");
        launcher.setHomepage_template("launcher");
        channelEntityList.add(launcher);

        for (ChannelEntity e : mChannelEntitys) {
            channelEntityList.add(e);
        }
        recyclerAdapter.notifyDataSetChanged();
//                createChannelView();
        if (brandName != null && brandName.toLowerCase().contains("changhong")) {
            homepage_template = "template3";
        }
        if (!StringUtils.isEmpty(homepage_template)) {
            for (int i = 0; i < mChannelEntitys.length; i++) {
                if (brandName != null && brandName.toLowerCase().contains("changhong")) {
                    if ("sport".equalsIgnoreCase(mChannelEntitys[i].getChannel())) {
                        channelscrollIndex = i + 1;
                        scrollType = ScrollType.none;
                        recyclerAdapter.setSelectedPosition(channelscrollIndex);
                        mCurrentChannelPosition.setPosition(channelscrollIndex);
                        headFragment.setSubTitle(mChannelEntitys[i].getName());
                    }
                } else {
                    if (homepage_template.equals(mChannelEntitys[i].getHomepage_template()) && mChannelEntitys[i].getHomepage_url().contains(homepage_url)) {
                        channelscrollIndex = i + 1;
                        Log.i("LH/", "channelscrollIndex:" + channelscrollIndex);
                        if (channelscrollIndex > 0 && !fragmentSwitch.hasMessages(SWITCH_PAGE_FROMLAUNCH)) {
                            scrollType = ScrollType.none;
                            recyclerAdapter.setSelectedPosition(channelscrollIndex);
                            mCurrentChannelPosition.setPosition(channelscrollIndex);
//                                fragmentSwitch.sendEmptyMessage(SWITCH_PAGE_FROMLAUNCH);
                        }
                        headFragment.setSubTitle(mChannelEntitys[i].getName());
                        break;
                    }
                }
            }
        }
        if (currentFragment == null && !isFinishing() && channelscrollIndex <= 0) {
            try {
                currentFragment = new GuideFragment();
                lastFragment = currentFragment;
                ChannelEntity channelEntity = new ChannelEntity();
                launcher.setChannel("launcher");
                launcher.setName("首页");
                launcher.setHomepage_template("launcher");
                currentFragment.setChannelEntity(channelEntity);
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.home_container, currentFragment, "template").commitAllowingStateLoss();
                home_tab_list.requestFocus();
            } catch (IllegalStateException e) {
            }

        }
    }

    private RecyclerView home_tab_list;
    private ChannelRecyclerAdapter recyclerAdapter;
    private List<ChannelEntity> channelEntityList = new ArrayList<>();

    private void setFocusChannelView(View view) {
        TextView channelBtn = (TextView) view.findViewById(R.id.channel_item);
        channelBtn.setBackgroundResource(R.drawable.channel_focus_frame);
        channelBtn.setTextColor(FOCUS_CHANNEL_TEXTCOLOR);
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.05f, 1, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        channelBtn.startAnimation(animationSet);
    }

    private void setLostFocusChannel(View view) {
        TextView channelBtn = (TextView) view.findViewById(R.id.channel_item);
        channelBtn.setTextColor(NORMAL_CHANNEL_TEXTCOLOR);
        channelBtn.setBackgroundResource(R.drawable.channel_item_normal);
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.05f, 1f, 1.05f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        channelBtn.startAnimation(animationSet);
    }

    private void setClickChannelView(View view) {
        Log.i("LH/", "view:" + view);
        if (view == null) {
            return;
        }
        TextView channelBtn = (TextView) view.findViewById(R.id.channel_item);
        view.setBackgroundResource(R.drawable.channel_item_focus);
        channelBtn.setTextColor(NORMAL_CHANNEL_TEXTCOLOR);
    }

    private int FOCUS_CHANNEL_BG = 0xffffba00;
    private int FOCUS_CHANNEL_TEXTCOLOR = 0xffffba00;
    private int NORMAL_CHANNEL_TEXTCOLOR = 0xffffffff;

    private void createChannelView() {
//        List<ChannelEntity> channelList;
//        ChannelEntity launcher = new ChannelEntity();
//        launcher.setChannel("launcher");
//        launcher.setName("首页");
//        launcher.setHomepage_template("launcher");

//        channelList = new ArrayList<ChannelEntity>();
//        // channelList.add(launcher);
//        for (ChannelEntity entity : channelEntities) {
//            channelList.add(entity);
//        }

    }

//    private void registerUpdateReceiver() {
//        appUpdateReceiver = new AppUpdateReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(AppConstant.APP_UPDATE_ACTION);
//        registerReceiver(appUpdateReceiver, intentFilter);
//    }

    /**
     * receive app update broadcast, and show update popup window
     */
//    class AppUpdateReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final Bundle bundle = intent.getBundleExtra("data");
//            contentView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    showUpdatePopup(contentView, bundle);
//                }
//            }, 2000);
//
//        }
//    }

    /**
     * show update popup, now update app or next time update
     */
//    private void showUpdatePopup(View view, Bundle bundle) {
//        final Context context = this;
//        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_update, null);
//        contentView.setBackgroundResource(R.drawable.app_update_bg);
//        home_shadow_view.setVisibility(View.VISIBLE);
//        float density = getResources().getDisplayMetrics().density;
//
//        int appUpdateHeight = (int) (getResources().getDimension(R.dimen.app_update_bg_height));
//        int appUpdateWidht = (int) (getResources().getDimension(R.dimen.app_update_bg_width));
//
//
//        updatePopupWindow = new PopupWindow(null, appUpdateHeight, appUpdateWidht);
//        updatePopupWindow.setContentView(contentView);
//        updatePopupWindow.setFocusable(true);
//        updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
//
//        Button updateNow = (Button) contentView.findViewById(R.id.update_now_bt);
//        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);
//
//        final String path = bundle.getString("path");
//
//        ArrayList<String> msgs = bundle.getStringArrayList("msgs");
//
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.leftMargin = (int) (getResources().getDimension(R.dimen.app_update_content_margin_left));
//        layoutParams.topMargin = (int) (getResources().getDimension(R.dimen.app_update_line_margin_));
//
//        for (String msg : msgs) {
//            View textLayout = LayoutInflater.from(this).inflate(R.layout.update_msg_text_item, null);
//            TextView textView = (TextView) textLayout.findViewById(R.id.update_msg_text);
//            textView.setText(msg);
//            updateMsgLayout.addView(textLayout);
//        }
//
//        updateNow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updatePopupWindow.dismiss();
//                home_shadow_view.setVisibility(View.GONE);
//                installApk(context, path);
//            }
//        });
//    }

//    public void installApk(Context mContext, String path) {
//        Uri uri = Uri.parse("file://" + path);
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(uri, "application/vnd.android.package-archive");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mContext.startActivity(intent);
//    }
//
    private void showExitPopup(View view) {
        exitPopup = new ModuleMessagePopWindow(this);
        exitPopup.setConfirmBtn(getString(R.string.vod_ok));
        exitPopup.setCancelBtn(getString(R.string.vod_cancel));
        exitPopup.setFirstMessage(getString(R.string.str_exit));
        home_shadow_view.setVisibility(View.VISIBLE);
        exitPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                home_shadow_view.setVisibility(View.GONE);
            }
        });
        exitPopup.showAtLocation(view, Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        exitPopup.dismiss();
                        CallaPlay callaPlay = new CallaPlay();
//                        callaPlay.app_exit(TrueTime.now().getTime() - app_start_time, SimpleRestClient.appVersion);
                        callaPlay.app_exit(System.currentTimeMillis() - app_start_time, SimpleRestClient.appVersion);
                        HomePageActivity.this.finish();
                        ArrayList<String> cache_log = MessageQueue.getQueueList();
                        HashSet<String> hasset_log = new HashSet<String>();
                        for (int i = 0; i < cache_log.size(); i++) {
                            hasset_log.add(cache_log.get(i));
                        }
                        DaisyUtils
                                .getVodApplication(HomePageActivity.this)
                                .getEditor()
                                .putStringSet(VodApplication.CACHED_LOG,
                                        hasset_log);
                        DaisyUtils.getVodApplication(getApplicationContext())
                                .save();
                        System.exit(0);
                    }
                },
                new ModuleMessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        exitPopup.dismiss();
                        home_shadow_view.setVisibility(View.GONE);
                    }
                }
        );
    }


    private boolean neterrorshow = false;

    private void showNetErrorPopup() {
        if (neterrorshow)
            return;
        final MessageDialogFragment dialog = new MessageDialogFragment(HomePageActivity.this, getString(R.string.fetch_net_data_error), null);
        dialog.setButtonText(getString(R.string.setting_network), getString(R.string.i_know));
        try {
            dialog.showAtLocation(contentView, Gravity.CENTER,
                    new MessageDialogFragment.ConfirmListener() {
                        @Override
                        public void confirmClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            HomePageActivity.this.startActivity(intent);
                        }
                    }, new MessageDialogFragment.CancelListener() {

                        @Override
                        public void cancelClick(View view) {
                            dialog.dismiss();
                            neterrorshow = true;
                        }
                    });
            dialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    neterrorshow = true;
                }
            });
            neterrorshow = true;
        } catch (android.view.WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    BitmapDecoder ddddBitmapDecoder;

    private void selectChannelByPosition(int position) {
        String tag;
        if (lastchannelindex != -1) {
            if (lastchannelindex < position) {
                scrollType = ScrollType.right;
            } else {
                scrollType = ScrollType.left;
            }
        }
        if (position == 0) {
            home_scroll_left.setVisibility(View.GONE);
            if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                channelChange = ChannelChange.RIGHT_ARROW;
        } else {
            home_scroll_left.setVisibility(View.VISIBLE);
        }

        if (position == channelEntityList.size() - 1) {
            home_scroll_right.setVisibility(View.GONE);
            if (channelChange != null && channelChange != ChannelChange.CLICK_CHANNEL)
                channelChange = ChannelChange.LEFT_ARROW;
        } else {
            home_scroll_right.setVisibility(View.VISIBLE);
        }
        ChannelEntity channelEntity = channelEntityList.get(position);
        headFragment.setSubTitle(channelEntity.getName());
        currentFragment = null;
        if ("template1".equals(channelEntity.getHomepage_template())) {
            currentFragment = new FilmFragment();
            tag = "template1";
        } else if ("template2".equals(channelEntity.getHomepage_template())) {
            currentFragment = new EntertainmentFragment();
            tag = "template2";
        } else if ("template3".equals(channelEntity.getHomepage_template())) {
            currentFragment = new SportFragment();
            tag = "template3";
        } else if ("template4".equals(channelEntity.getHomepage_template())) {
            currentFragment = new ChildFragment();
            tag = "template4";
        } else {
            currentFragment = new GuideFragment();
            tag = "template";
        }
        if (lastFragment != null) {
            if (lastFragment instanceof ChildFragment) {
                if (currentFragment instanceof ChildFragment) {
                } else {
//                    destroybackground();
                    if (ddddBitmapDecoder != null) {
                        ddddBitmapDecoder.removeAllCallback();
//                                              ddddBitmapDecoder.interrupt();
                    }
                    ddddBitmapDecoder = new BitmapDecoder();
                    ddddBitmapDecoder.decode(this, R.drawable.main_bg,
                            new BitmapDecoder.Callback() {
                                @Override
                                public void onSuccess(
                                        BitmapDrawable bitmapDrawable) {
                                    contentView
                                            .setBackgroundDrawable(bitmapDrawable);
                                }
                            });
                }
            } else {
                if (currentFragment instanceof ChildFragment) {
                    if (ddddBitmapDecoder != null) {
                        ddddBitmapDecoder.removeAllCallback();
                    }
                    ddddBitmapDecoder = new BitmapDecoder();
                    ddddBitmapDecoder.decode(this,
                            R.drawable.channel_child_bg,
                            new BitmapDecoder.Callback() {
                                @Override
                                public void onSuccess(
                                        BitmapDrawable bitmapDrawable) {
                                    contentView
                                            .setBackgroundDrawable(bitmapDrawable);
                                }
                            });
                } else {
                }
            }
        }
        lastFragment = currentFragment;
        currentFragment.setScrollFromBorder(scrollFromBorder);
        if (scrollFromBorder) {
            currentFragment.setRight(rightscroll);
            currentFragment.setBottomFlag(lastviewTag);
        }
        currentFragment.setChannelEntity(channelEntity);
        ChannelBaseFragment t = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template");
        ChannelBaseFragment t1 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template1");
        ChannelBaseFragment t2 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template2");
        ChannelBaseFragment t3 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template3");
        ChannelBaseFragment t4 = (ChannelBaseFragment) getSupportFragmentManager().findFragmentByTag("template4");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if ("template".equals(tag)) {
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t != null) {
                t.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t.setRight(rightscroll);
                    t.setBottomFlag(lastviewTag);
                }
                transaction.show(t);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template1".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t1 != null && !t1.isRemoving()) {
                t1.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t1.setRight(rightscroll);
                    t1.setBottomFlag(lastviewTag);
                }
                t1.setChannelEntity(channelEntity);
                t1.refreshData();
                transaction.show(t1);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template2".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null)
                transaction.hide(t4);
            if (t2 != null && !t2.isRemoving()) {
                t2.setChannelEntity(channelEntity);
                t2.refreshData();
                t2.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t2.setRight(rightscroll);
                    t2.setBottomFlag(lastviewTag);
                }
                transaction.show(t2);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template3".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t4 != null)
                transaction.hide(t4);
            if (t3 != null) {
                t3.setChannelEntity(channelEntity);
                t3.refreshData();
                t3.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t3.setRight(rightscroll);
                    t3.setBottomFlag(lastviewTag);
                }
                transaction.show(t3);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        if ("template4".equals(tag)) {
            if (t != null)
                transaction.hide(t);
            if (t1 != null)
                transaction.hide(t1);
            if (t2 != null)
                transaction.hide(t2);
            if (t3 != null)
                transaction.hide(t3);
            if (t4 != null && !t4.isRemoving()) {
                t4.setChannelEntity(channelEntity);
                t4.refreshData();
                t4.setScrollFromBorder(scrollFromBorder);
                if (scrollFromBorder) {
                    t4.setRight(rightscroll);
                    t4.setBottomFlag(lastviewTag);
                }
                transaction.show(t4);
                transaction.commitAllowingStateLoss();
            } else {
                replaceFragment(currentFragment, tag, transaction);
            }
        }
        // longhai add
        if (home_tab_list == null) {
            return;
        }
        lastchannelindex = position;
        switch (lastchannelindex) {
            case 0:
                home_tab_list.setNextFocusUpId(R.id.guidefragment_firstpost);
                break;
            case 1:
                home_tab_list.setNextFocusUpId(R.id.filmfragment_secondpost);
                break;
            case 2:
                home_tab_list.setNextFocusUpId(R.id.filmfragment_thirdpost);
                break;
            case 3:
                home_tab_list.setNextFocusUpId(R.id.vaiety_channel2_image);
                break;
            case 4:
                home_tab_list.setNextFocusUpId(R.id.vaiety_channel3_image);
                break;
            case 5:
                home_tab_list.setNextFocusUpId(R.id.sport_channel4_image);
                break;
            case 6:
                home_tab_list.setNextFocusUpId(R.id.vaiety_channel4_image);
                break;
            case 7:
                home_tab_list.setNextFocusUpId(R.id.child_more);
                break;
            case 8:
                home_tab_list.setNextFocusUpId(R.id.listmore);
                break;
            case 9:
                home_tab_list.setNextFocusUpId(R.id.listmore);
                break;
            case 10:
                home_tab_list.setNextFocusUpId(R.id.listmore);
                break;
            default:
                break;
        }
    }

    private void setbackground(int id) {

        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;

        opt.inPurgeable = true;

        opt.inInputShareable = true;
        opt.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
        opt.inDensity = getResources().getDisplayMetrics().densityDpi;

        InputStream is = getResources().openRawResource(

                id);

        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
        contentView.setBackgroundDrawable(bd);
    }

    private void destroybackground() {
        BitmapDrawable bd = (BitmapDrawable) contentView.getBackground();
        contentView.setBackgroundResource(0);//别忘了把背景设为null，避免onDraw刷新背景时候出现used a recycled bitmap错误
        if (bd == null)
            return;
        bd.setCallback(null);
        bd.getBitmap().recycle();
    }

    private void replaceFragment(Fragment fragment, String tag, FragmentTransaction transaction) {
        switch (scrollType) {
            case left:
                transaction.setCustomAnimations(
                        R.anim.push_right_in,
                        R.anim.push_right_out);
                break;
            case right:
                transaction.setCustomAnimations(
                        R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
        }

        transaction.replace(R.id.home_container, fragment, tag).commitAllowingStateLoss();
    }

    private void getHardInfo() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        SimpleRestClient.densityDpi = metric.densityDpi;
        SimpleRestClient.screenWidth = metric.widthPixels;
        SimpleRestClient.screenHeight = metric.heightPixels;
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            SimpleRestClient.appVersion = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void channelRequestFocus(String channel) {
        switch (channelChange) {
            case CLICK_CHANNEL:
//                home_tab_list.requestFocus();
//                channelHashMap.get(channel).requestFocus();
//                channelHashMap.get(channel).requestFocusFromTouch();
                break;
            case LEFT_ARROW:
//                arrow_left.requestFocus();
//                arrow_left.requestFocusFromTouch();
                break;
            case RIGHT_ARROW:
//                arrow_right.requestFocus();
//                arrow_right.requestFocusFromTouch();
                break;
        }

    }

    enum ChannelChange {
        CLICK_CHANNEL,
        LEFT_ARROW,
        RIGHT_ARROW
    }

    @Override
    protected void onResume() {
        super.onResume();
        neterrorshow = false;
        channelscrollIndex = 0;
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
        if (!DaisyUtils.isNetworkAvailable(this)) {
            Log.e("tvguide", "onresume Isnetwork");
            showNetErrorPopup();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (netErrorPopupHandler != null && netErrorPopupRunnable != null) {
            netErrorPopupHandler.removeCallbacks(netErrorPopupRunnable);
        }
        if (fragmentSwitch.hasMessages(SWITCH_PAGE))
            fragmentSwitch.removeMessages(SWITCH_PAGE);
        if (fragmentSwitch.hasMessages(SWITCH_PAGE_FROMLAUNCH))
            fragmentSwitch.removeMessages(SWITCH_PAGE_FROMLAUNCH);
    }

    @Override
    protected void onDestroy() {
        if (bitmapDecoder != null && bitmapDecoder.isAlive()) {
            bitmapDecoder.interrupt();
        }
        if (ddddBitmapDecoder != null && ddddBitmapDecoder.isAlive()) {
            ddddBitmapDecoder.interrupt();
        }
        if (!(updatePopupWindow == null)) {
            updatePopupWindow.dismiss();
        }
        if (exitPopup != null && exitPopup.isShowing()) {
            exitPopup.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        homepage_template = intent.getStringExtra("homepage_template");
        homepage_url = intent.getStringExtra("homepage_url");
        if (StringUtils.isEmpty(homepage_template)
                || StringUtils.isEmpty(homepage_url)) {
            fetchChannels();
        } else {
//            if (StringUtils.isNotEmpty(SimpleRestClient.root_url)) {
            fetchChannels();
//            }
        }
    }


    public void setLastViewTag(String flag) {
        lastviewTag = flag;
    }

    public void resetBorderFocus() {
        scrollFromBorder = false;
    }

    private enum ScrollType {
        none,
        left,
        right;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ("lcd_s3a01".equals(VodUserAgent.getModelName())) {
            if (keyCode == 707 || keyCode == 774 || keyCode == 253) {
                isneedpause = false;
            }
        } else if ("lx565ab".equals(VodUserAgent.getModelName())) {
            if (keyCode == 82 || keyCode == 707 || keyCode == 253) {
                isneedpause = false;
            }
        } else {
            if (keyCode == 223 || keyCode == 499 || keyCode == 480) {
                isneedpause = false;
            }
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                home_tab_list.setHovered(false);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private static int nextselectflag;

    static class FragmentSwitchHandler extends Handler {
        private WeakReference<HomePageActivity> weakReference;

        public FragmentSwitchHandler(HomePageActivity activity) {
            weakReference = new WeakReference<HomePageActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HomePageActivity activity = weakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case SWITCH_PAGE:
                        activity.selectChannelByPosition(msg.arg1);
                        break;
                    case SWITCH_PAGE_FROMLAUNCH:

                        // longhai
//					if (nextselectflag == 0
//							|| (nextselectflag != activity.scroll
//									.getnextSelectPosition())) {
//						channelscrollIndex = channelscrollIndex - 1;
//					}
//
//					nextselectflag = activity.scroll.getnextSelectPosition();
//					activity.scroll.arrowScroll(View.FOCUS_RIGHT);
//					if (channelscrollIndex > 0) {
//						sendEmptyMessage(SWITCH_PAGE_FROMLAUNCH);
//					} else {
//						channelflag = false;
//					}
                        break;
                }
            }
        }
    }

    /**
     * advertisement start
     */
    private void playLaunchAd(final int index) {
        playIndex = index;
        if (launchAds.get(index).media_type.equals(AdvertiseManager.TYPE_VIDEO)) {
            isPlayingVideo = true;
        }
        if (isPlayingVideo) {
            if (ad_video.getVisibility() != View.VISIBLE) {
                ad_pic.setVisibility(View.GONE);
                ad_video.setVisibility(View.VISIBLE);
            }
            ad_video.setVideoPath(launchAds.get(index).location);
            ad_video.setOnPreparedListener(onPreparedListener);
            ad_video.setOnCompletionListener(onCompletionListener);
        } else {
            if (ad_pic.getVisibility() != View.VISIBLE) {
                ad_video.setVisibility(View.GONE);
                ad_pic.setVisibility(View.VISIBLE);
            }
            Picasso.with(this)
                    .load(launchAds.get(index).location)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_CACHE)
                    .into(ad_pic, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (playIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                        }

                        @Override
                        public void onError() {
                            ad_pic.setImageBitmap(Utils.getImgFromAssets(HomePageActivity.this, "poster.png"));
                            if (playIndex == 0) {
                                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
                            }
                        }
                    });
        }

    }

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            ad_video.start();
            if (playIndex == 0) {
                mHandler.sendEmptyMessage(MSG_AD_COUNTDOWN);
            }
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (playIndex == launchAds.size() - 1) {
                mHandler.removeMessages(MSG_AD_COUNTDOWN);
                goNextPage();
                return;
            }
            playLaunchAd(playIndex++);
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AD_COUNTDOWN:
                    if (ad_timer == null) {
                        return;
                    }
                    if (!isPlayingVideo && countAdTime == 0) {
                        mHandler.removeMessages(MSG_AD_COUNTDOWN);
                        goNextPage();
                        return;
                    }
                    if (ad_timer.getVisibility() != View.VISIBLE) {
                        ad_timer.setVisibility(View.VISIBLE);
                    }
                    ad_timer.setText(countAdTime + "s");
                    int refreshTime;
                    if (!isPlayingVideo) {
                        refreshTime = 1000;
                        if (currentImageAdCountDown == 0 && !isStartImageCountDown) {
                            currentImageAdCountDown = launchAds.get(playIndex).duration;
                            isStartImageCountDown = true;
                        } else {
                            if (currentImageAdCountDown == 0) {
                                playLaunchAd(playIndex++);
                                isStartImageCountDown = false;
                            } else {
                                currentImageAdCountDown--;
                            }
                        }
                        countAdTime--;
                    } else {
                        refreshTime = 500;
                        countAdTime = getAdCountDownTime();
                    }
                    sendEmptyMessageDelayed(MSG_AD_COUNTDOWN, refreshTime);
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        if (ad_video != null) {
            ad_video.stopPlayback();
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        super.onStop();
    }

    private void goNextPage() {
        layout_advertisement.setVisibility(View.GONE);
        layout_homepage.setVisibility(View.VISIBLE);
        if (ad_video != null) {
            ad_video.stopPlayback();
        }
        if (mHandler.hasMessages(MSG_AD_COUNTDOWN)) {
            mHandler.removeMessages(MSG_AD_COUNTDOWN);
        }
        startAdsService();
    }

    private int getAdCountDownTime() {
        if (launchAds == null || launchAds.isEmpty() || !isPlayingVideo) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = playIndex;
        if (currentAd == launchAds.size() - 1) {
            totalAdTime = launchAds.get(launchAds.size() - 1).duration;
        } else {
            for (int i = currentAd; i < launchAds.size(); i++) {
                totalAdTime += launchAds.get(i).duration;
            }
        }
        return totalAdTime - ad_video.getCurrentPosition() / 1000 - 1;
    }

    private void startAdsService() {
        Intent intent = new Intent();
        intent.setClass(this, AdsUpdateService.class);
        startService(intent);
    }
    /**
     * advertisement end
     */

}
