package tv.ismar.detailpage.view;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.Constants;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.SPUtils;
import tv.ismar.app.util.SystemFileUtil;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.LabelImageView;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.databinding.FragmentDetailpageEntertainmentSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageMovieSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageNormalSharpBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;
import tv.ismar.statistics.DetailPageStatistics;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.POSITION;
import static tv.ismar.app.core.PageIntentInterface.TYPE;

public class DetailPageFragment extends Fragment implements DetailPageContract.View, View.OnHoverListener {
    private static final String TAG = "LH/DetailPageFragment";
    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_CONTENT_MODEL = "ARG_CONTENT_MODEL";

    private DetailPageViewModel mModel;
    private DetailPageContract.Presenter mPresenter;

    private FragmentDetailpageMovieSharpBinding mMovieBinding;
    private FragmentDetailpageEntertainmentSharpBinding mEntertainmentBinding;
    private FragmentDetailpageNormalSharpBinding mNormalBinding;
    private DetailPagePresenter mDetailPagePresenter;

    private int relViews;
    private int[] mRelImageViewIds = {R.id.rel_1_img, R.id.rel_2_img, R.id.rel_3_img, R.id.rel_4_img, R.id.rel_5_img, R.id.rel_6_img};
    private int[] mRelTextViewIds = {R.id.rel_1_text, R.id.rel_2_text, R.id.rel_3_text, R.id.rel_4_text, R.id.rel_5_text, R.id.rel_6_text};
    private int[] mRelTextViewFocusIds = {R.id.rel_1_focus_text, R.id.rel_2_focus_text, R.id.rel_3_focus_text, R.id.rel_4_focus_text};
    private int[] mRelItemViews = {R.id.related_item_layout_1, R.id.related_item_layout_2, R.id.related_item_layout_3, R.id.related_item_layout_4};

    private LabelImageView[] relRelImageViews;
    private TextView[] relTextViews;
    private TextView[] relFocusTextViews;

    //传递参数
    private String fromPage;

    private HeadFragment headFragment;
    private String mHeadTitle;
    private volatile boolean itemIsLoad;
    private volatile boolean relateIsLoad;
    private ItemEntity mItemEntity;
    private ItemEntity[] relateItems;
    private int mRemandDay = 0;
    private BaseActivity mActivity;

    private View tmp;

    private View palyBtnView;
    private View purchaseBtnView;
    private View exposideBtnView;
    private View favoriteBtnView;
    private View moreBtnView;

    private DetailPageStatistics mPageStatistics;
    private String isLogin = "no";
    private String to="";
    private int position;
    private String type="item";

    public DetailPageFragment() {
        // Required empty public constructor
    }


    public static DetailPageFragment newInstance(String fromPage, String itemJson) {
        DetailPageFragment fragment = new DetailPageFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_SOURCE, fromPage);
        args.putString(EXTRA_ITEM_JSON, itemJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageStatistics = new DetailPageStatistics();
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            fromPage = bundle.getString(EXTRA_SOURCE);
            String itemJson = bundle.getString(EXTRA_ITEM_JSON);
            position = bundle.getInt(POSITION,-1);
            type=bundle.getString(TYPE);
            mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);

        }

        if (!(getActivity() instanceof BaseActivity)) {
            getActivity().finish();
            Log.e(TAG, "Activity must be extends BaseActivity.");
            return;
        }
        mActivity = (BaseActivity) getActivity();
        mDetailPagePresenter = new DetailPagePresenter((DetailPageActivity) getActivity(), this, mItemEntity.getContentModel());
        mModel = new DetailPageViewModel(mActivity, mDetailPagePresenter);
        mDetailPagePresenter.setItemEntity(mItemEntity);
        String source=getActivity().getIntent().getStringExtra("fromPage");
        if(source!=null&&source.equals("launcher")) {
            tempInitStaticVariable();
            BaseActivity.baseSection="";
            BaseActivity.baseChannel="";
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.launcher_vod_click("item",mItemEntity.getPk(),mItemEntity.getTitle(),position);

            String province = (String) SPUtils.getValue(InitializeProcess.PROVINCE_PY, "");
            String city = (String) SPUtils.getValue(InitializeProcess.CITY, "");
            String isp = (String) SPUtils.getValue(InitializeProcess.ISP, "");
            callaPlay.app_start(IsmartvActivator.getInstance().getSnToken(),
                    VodUserAgent.getModelName(), DeviceUtils.getScreenInch(getActivity()),
                    android.os.Build.VERSION.RELEASE,
                    SimpleRestClient.appVersion,
                    SystemFileUtil.getSdCardTotal(getActivity().getApplicationContext()),
                    SystemFileUtil.getSdCardAvalible(getActivity().getApplicationContext()),
                    IsmartvActivator.getInstance().getUsername(), province, city, isp, source,
                    DeviceUtils.getLocalMacAddress(getActivity().getApplicationContext()),
                    SimpleRestClient.app, getActivity().getPackageName()
            );

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        return loadItemModel(inflater, container, mItemEntity);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, Constants.TEST);
        mPresenter.start();
//        mPresenter.fetchItem(String.valueOf(mItemEntity.getPk()));
//        loadItem(mItemEntity);
        mPresenter.fetchItemRelate(String.valueOf(mItemEntity.getPk()));
        if (videoIsStart()) {
            palyBtnView.requestFocus();
            palyBtnView.requestFocusFromTouch();
        } else {
            purchaseBtnView.requestFocus();
            purchaseBtnView.requestFocusFromTouch();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        to="";
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isLogin = "yes";
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadItem(mItemEntity);
            }
        }).start();
        mPageStatistics.videoDetailIn(mItemEntity, fromPage);

        mModel.notifyBookmark(true);
    }

    @Override
    public void onPause() {
        if(!to.equals(""))
        mPageStatistics.videoDetailOut(mItemEntity,to);
        mPresenter.stop();
        super.onPause();
    }

    @Override
    public void onStop() {
//        String sn = IsmartvActivator.getInstance().getSnToken();
//        Log.i("LH/", "sn:" + sn);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mPageStatistics.videoDetailOut(mItemEntity,fromPage);
        super.onDestroy();
    }

    @Override
    public void loadItem(ItemEntity itemEntity) {
        if(isLogin.equals("yes")&&mItemEntity.getExpense()!=null&&mRemandDay<=0) {
            Log.e("refresh","true");
            if (itemEntity.getContentModel().equals("sport")) {
                mPresenter.requestPlayCheck(String.valueOf(mItemEntity.getPk()));
            } else {
                mPresenter.requestPlayCheck(String.valueOf(itemEntity.getPk()));
            }
        }
        mModel.replaceItem(itemEntity);
        itemIsLoad = true;
        hideLoading();
        mItemEntity = itemEntity;
    }

    @Override
    public void loadItemRelate(ItemEntity[] itemEntities) {
        relateItems = itemEntities;
        int length=itemEntities.length;
        if (length< relViews) {
            for (int i = length; i < relViews; i++) {
                ((View) relRelImageViews[i].getParent()).setVisibility(View.INVISIBLE);
            }
            if(itemEntities.length>0) {
                if (mNormalBinding != null) {
                    mNormalBinding.getRoot().findViewById(mRelItemViews[itemEntities.length - 1]).setNextFocusDownId(R.id.detail_relative_button);
                    moreBtnView.setNextFocusUpId(mRelItemViews[itemEntities.length - 1]);
                }
            }
        }

        for (int i = 0; i < length && i < relViews; i++) {
            moreBtnView.setNextFocusLeftId(View.NO_ID);
            switch (mItemEntity.getContentModel()) {
                case "movie":
                    relRelImageViews[i].setLivUrl(itemEntities[i].getList_url());
                    break;
                default:
                    relRelImageViews[i].setLivUrl(itemEntities[i].getPosterUrl());
                    break;

            }
            if (mNormalBinding != null) {
                View itemView = mNormalBinding.getRoot().findViewById(mRelItemViews[i]);
                itemView.setTag(i);
                itemView.setOnClickListener(relateItemOnClickListener);
                itemView.setOnHoverListener(this);
                final int finalI = i;
                itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            relTextViews[finalI].setSelected(true);
                        } else {
                            relTextViews[finalI].setSelected(false);
                        }
                    }
                });
//                relRelImageViews[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        int position = (int) v.getTag();
//                        if (hasFocus) {
//                            relTextViews[position].setSelected(true);
//                        } else {
//                            relTextViews[position].setSelected(false);
//                        }
//                    }
//                });

            } else {
                relRelImageViews[i].setTag(i);
                relRelImageViews[i].setOnClickListener(relateItemOnClickListener);
                relRelImageViews[i].setOnHoverListener(this);
                relRelImageViews[i].setNextFocusDownId(relRelImageViews[i].getId());

                relRelImageViews[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        int position = (int) v.getTag();
                        if (hasFocus) {
                            relTextViews[position].setSelected(true);
                        } else {
                            relTextViews[position].setSelected(false);
                        }
                    }
                });
            }


            ItemEntity.Expense expense = itemEntities[i].getExpense();
            if (expense != null && !Utils.isEmptyText(expense.getCptitle())) {
                relRelImageViews[i].setLivVipPosition(LabelImageView.LEFTTOP);
                String imageUrl = VipMark.getInstance().getImage(mActivity, expense.getPay_type(), expense.getCpid());
                relRelImageViews[i].setLivVipUrl(imageUrl);
            }
            String scoreStr = itemEntities[i].getBeanScore();
            if (!Utils.isEmptyText(scoreStr)) {
                float score = 0;
                try {
                    score = Float.parseFloat(scoreStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (score > 0) {
                    relRelImageViews[i].setLivRate(score);
                }
            }
//            relTextViews[i].setMarqueeRepeatLimit(-1);
//            relTextViews[i].setEllipsize(TextUtils.TruncateAt.MARQUEE);
            relTextViews[i].setText(itemEntities[i].getTitle());

            if (mMovieBinding != null || mEntertainmentBinding != null) {
                relRelImageViews[i].setLivLabelText(itemEntities[i].getFocus());

            } else {
                relFocusTextViews[i].setText(itemEntities[i].getFocus());
            }
        }
        relateIsLoad = true;
        hideLoading();
        if (mMovieBinding != null && mMovieBinding.detailBtnLinear != null)
            mMovieBinding.detailBtnLinear.setVisibility(View.VISIBLE);
    }


    @Override
    public void notifyPlayCheck(PlayCheckEntity playCheckEntity) {
        mModel.notifyPlayCheck(playCheckEntity);
        mRemandDay = playCheckEntity.getRemainDay();

//        boolean isBuy;
//        if (playCheckEntity.getRemainDay() == 0) {
//            isBuy = false;// 过期了。认为没购买
//        } else {
//            isBuy = true;// 购买了，剩余天数大于0
//        }
//        ((DetailPageActivity)getActivity()).playCheckResult(isBuy);
    }

    @Override
    public void notifyBookmark(boolean mark, boolean isSuccess) {
        mModel.notifyBookmark(isSuccess);
        if (mark) {
            if (isSuccess) {
                showToast(getString(R.string.vod_bookmark_add_success));
            } else {
                showToast(getString(R.string.vod_bookmark_add_unsuccess));
            }
        } else {
            if (isSuccess) {
                showToast(getString(R.string.vod_bookmark_remove_success));
            } else {
                showToast(getString(R.string.vod_bookmark_remove_unsuccess));
            }
        }
    }

    @Override
    public void onError() {
        try {
            if (((DetailPageActivity) getActivity()).mLoadingDialog != null && ((DetailPageActivity) getActivity()).mLoadingDialog.isShowing()) {
                ((DetailPageActivity) getActivity()).mLoadingDialog.dismiss();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void setPresenter(DetailPageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void hideLoading() {
        if (((DetailPageActivity) getActivity()).mLoadingDialog != null && ((DetailPageActivity) getActivity()).mLoadingDialog.isShowing() && itemIsLoad && relateIsLoad) {
            ((DetailPageActivity) getActivity()).mLoadingDialog.dismiss();
            HashMap<String, Object> dataCollectionProperties = new HashMap<>();
            dataCollectionProperties.put(EventProperty.CLIP, mItemEntity.getClip().getPk());
            dataCollectionProperties.put(EventProperty.DURATION, (int)((System.currentTimeMillis()-((DetailPageActivity) getActivity()).start_time)/1000));
            String quality="";
            switch (mItemEntity.getQuality()){
                case 2:
                    quality="normal";
                    break;
                case 3:
                    quality="medium";
                    break;
                case 4:
                    quality="high";
                    break;
                case 5:
                    quality="ultra";
                    break;
                default:
                    quality="adaptive";
                    break;
            }
            dataCollectionProperties.put(EventProperty.QUALITY, quality);
            dataCollectionProperties.put(EventProperty.TITLE, mItemEntity.getTitle());
            dataCollectionProperties.put(EventProperty.ITEM, mItemEntity.getPk());
            dataCollectionProperties.put(EventProperty.SUBITEM, mItemEntity.getItemPk());
            dataCollectionProperties.put(EventProperty.LOCATION,"detail");
            new NetworkUtils.DataCollectionTask().execute(NetworkUtils.DETAIL_PLAY_LOAD, dataCollectionProperties);
        }

        mModel.showLayout();
    }

    private View.OnClickListener relateItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ItemEntity item = relateItems[(int) v.getTag()];
            mPageStatistics.videoRelateClick(mItemEntity.getPk(), item);
            new PageIntent().toDetailPage(getContext(), Source.RELATED.getValue(), item.getPk());
            to="relate";
        }
    };

    private String getModelType(String content_model) {
        String resourceType = null;
        if (content_model.equals("movie")) {
            resourceType = "电影";
            // teleplay 为电视剧trailer
        } else if (content_model.equals("teleplay")) {
            resourceType = "电视剧";
            // variety 为综艺
        } else if (content_model.equals("variety")) {
            resourceType = "综艺";
            // documentary 为纪录片
        } else if (content_model.equals("documentary")) {
            resourceType = "纪录片";
            // entertainment 为娱乐
        } else if (content_model.equals("entertainment")) {
            resourceType = "娱乐";
            // trailer 为片花
        } else if (content_model.equals("trailer")) {
            resourceType = "片花";
            // music 为音乐
        } else if (content_model.equals("music")) {
            resourceType = "音乐";
            // comic 为喜剧
        } else if (content_model.equals("comic")) {
            resourceType = "喜剧";
            // sport 为体育
        } else if (content_model.equals("sport")) {
            resourceType = "体育";
        }
        return resourceType;
    }

    private void showToast(String text) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.simple_toast, null);
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(mActivity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private View loadItemModel(LayoutInflater inflater, ViewGroup container, ItemEntity itemEntity) {
        View contentView;
        String content_model = itemEntity.getContentModel();
        mHeadTitle = getModelType(content_model);
        if ((("variety".equals(content_model) && mItemEntity.getExpense() == null)) || ("entertainment".equals(content_model) && mItemEntity.getExpense() == null)) {
            relViews = 4;
            mEntertainmentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_entertainment_sharp, container, false);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
            contentView = mEntertainmentBinding.getRoot();
            tmp = mEntertainmentBinding.tmp;

            palyBtnView = mEntertainmentBinding.detailBtnPlay;
//            purchaseBtnView = mEntertainmentBinding.
            exposideBtnView = mEntertainmentBinding.detailBtnDrama;
            favoriteBtnView = mEntertainmentBinding.detailBtnCollect;
            moreBtnView = mEntertainmentBinding.detailRelativeButton;
            palyBtnView.setNextFocusDownId(R.id.detail_relative_button);
        } else if ("movie".equals(content_model)) {
            relViews = 6;
            mMovieBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_movie_sharp, container, false);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
            contentView = mMovieBinding.getRoot();
            tmp = mMovieBinding.tmp;
            palyBtnView = mMovieBinding.detailBtnPlay;
            purchaseBtnView = mMovieBinding.detailBtnBuy;
//            exposideBtnView = mMovieBinding.detailBtnDrama;
            favoriteBtnView = mMovieBinding.detailBtnCollect;
            moreBtnView = mMovieBinding.detailRelativeButton;
        } else {
            relViews = 4;
            relFocusTextViews = new TextView[relViews];
            mNormalBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_normal_sharp, container, false);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
            contentView = mNormalBinding.getRoot();
            tmp = mNormalBinding.tmp;
            palyBtnView = mNormalBinding.detailBtnPlay;
            purchaseBtnView = mNormalBinding.detailBtnBuy;
            exposideBtnView = mNormalBinding.detailBtnDrama;
            favoriteBtnView = mNormalBinding.detailBtnCollect;
            moreBtnView = mNormalBinding.detailRelativeButton;
        }

        palyBtnView.setOnHoverListener(this);
        palyBtnView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    to="play";
                }
            }
        });
        if (purchaseBtnView != null) {
            purchaseBtnView.setOnHoverListener(this);
            purchaseBtnView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        to="pay";
                    }
                }
            });
        }

        if (exposideBtnView != null) {
            exposideBtnView.setOnHoverListener(this);
        }
        favoriteBtnView.setOnHoverListener(this);
        moreBtnView.setOnHoverListener(this);
        moreBtnView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    to="relate";
                }
            }
        });
        relRelImageViews = new LabelImageView[relViews];
        relTextViews = new TextView[relViews];

        for (int i = 0; i < relViews; i++) {
            relRelImageViews[i] = (LabelImageView) contentView.findViewById(mRelImageViewIds[i]);
            relRelImageViews[i].setVisibility(View.VISIBLE);
            relTextViews[i] = (TextView) contentView.findViewById(mRelTextViewIds[i]);
            if (!(content_model.equals("variety") && itemEntity.getExpense() == null) &&
                    !(content_model.equals("entertainment") && itemEntity.getExpense() == null)
                    && !content_model.equals("movie")) {
                relFocusTextViews[i] = (TextView) contentView.findViewById(mRelTextViewFocusIds[i]);
            }
        }
        headFragment = new HeadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", HeadFragment.HEADER_DETAILPAGE);
        bundle.putString("channel_name", mHeadTitle);
        headFragment = new HeadFragment();
        headFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().add(R.id.detail_head, headFragment).commit();
        return contentView;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                tmp.requestFocusFromTouch();
                break;
        }

        return false;
    }

    private boolean videoIsStart() {
        if (mItemEntity.getStartTime() != null) {
            Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            currentCalendar.setTime(TrueTime.now());
            Calendar startCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            startCalendar.setTime(mItemEntity.getStartTime());
            if (currentCalendar.after(startCalendar)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    // 从launcher进入详情页，初始化赋值问题
    private void tempInitStaticVariable() {
        new Thread() {
            @Override
            public void run() {
                DisplayMetrics metric = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
                SimpleRestClient.densityDpi = metric.densityDpi;
                SimpleRestClient.screenWidth = metric.widthPixels;
                SimpleRestClient.screenHeight = metric.heightPixels;
                PackageManager manager = getActivity().getPackageManager();
                try {
                    PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
                    SimpleRestClient.appVersion = info.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String apiDomain = IsmartvActivator.getInstance().getApiDomain();
                String ad_domain = IsmartvActivator.getInstance().getAdDomain();
                String log_domain = IsmartvActivator.getInstance().getLogDomain();
                String upgrade_domain = IsmartvActivator.getInstance().getUpgradeDomain();
                if (apiDomain != null && !apiDomain.contains("http")) {
                    apiDomain = "http://" + apiDomain;
                }
                if (ad_domain != null && !ad_domain.contains("http")) {
                    ad_domain = "http://" + ad_domain;
                }
                if (log_domain != null && !log_domain.contains("http")) {
                    log_domain = "http://" + log_domain;
                }
                if (upgrade_domain != null && !upgrade_domain.contains("http")) {
                    upgrade_domain = "http://" + upgrade_domain;
                }
                SimpleRestClient.root_url = apiDomain;
                SimpleRestClient.ad_domain = ad_domain;
                SimpleRestClient.log_domain = log_domain;
                SimpleRestClient.upgrade_domain = upgrade_domain;
                SimpleRestClient.device_token = IsmartvActivator.getInstance().getDeviceToken();
                SimpleRestClient.sn_token = IsmartvActivator.getInstance().getSnToken();
                SimpleRestClient.zuser_token = IsmartvActivator.getInstance().getZUserToken();
                SimpleRestClient.zdevice_token = IsmartvActivator.getInstance().getZDeviceToken();
            }
        }.start();

    }
}
