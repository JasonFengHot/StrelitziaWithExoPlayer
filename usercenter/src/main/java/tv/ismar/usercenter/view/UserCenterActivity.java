package tv.ismar.usercenter.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.History;
import tv.ismar.app.entity.Item;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.ActivityUtils;
import tv.ismar.pay.CardPayFragment;
import tv.ismar.pay.LoginFragment;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.presenter.HelpPresenter;
import tv.ismar.usercenter.presenter.LocationPresenter;
import tv.ismar.usercenter.presenter.ProductPresenter;
import tv.ismar.usercenter.presenter.PurchaseHistoryPresenter;
import tv.ismar.usercenter.presenter.UserInfoPresenter;
import tv.ismar.usercenter.viewmodel.HelpViewModel;
import tv.ismar.usercenter.viewmodel.LocationViewModel;
import tv.ismar.usercenter.viewmodel.ProductViewModel;
import tv.ismar.usercenter.viewmodel.PurchaseHistoryViewModel;
import tv.ismar.usercenter.viewmodel.UserInfoViewModel;


/**
 * Created by huaijie on 7/3/15.
 */
public class UserCenterActivity extends BaseActivity implements LoginFragment.LoginCallback,
        IsmartvActivator.AccountChangeCallback {
    private static final String TAG = UserCenterActivity.class.getSimpleName();
    private static final int MSG_INDICATOR_CHANGE = 0x9b;

    private HelpFragment mHelpFragment;
    private LocationFragment mLocationFragment;
    private LoginFragment mLoginFragment;
    private ProductFragment mProductFragment;
    private PurchaseHistoryFragment mPurchaseHistoryFragment;
    private UserInfoFragment mUserInfoFragment;

    private ProductPresenter mProductPresenter;
    private LocationPresenter mLocationPresenter;
    private HelpPresenter mHelpPresenter;
    private PurchaseHistoryPresenter mPurchaseHistoryPresenter;
    private UserInfoPresenter mUserInfoPresenter;

    private ArrayList<View> indicatorView;


    private boolean isOnKeyDown = false;
    private boolean fargmentIsActive = false;

    private static final int[] INDICATOR_TEXT_RES_ARRAY = {
            R.string.usercenter_store,
            R.string.usercenter_userinfo,
            R.string.usercenter_login_register,
            R.string.usercenter_purchase_history,
            R.string.usercenter_help,
            R.string.usercenter_location
    };

    private static final int[] INDICATOR_ID_RES_ARRAY = {
            R.id.usercenter_store,
            R.id.usercenter_userinfo,
            R.id.usercenter_login_register,
            R.id.usercenter_purchase_history,
            R.id.usercenter_help,
            R.id.usercenter_location
    };
    private LinearLayout userCenterIndicatorLayout;

    private View lastSelectedView;
    private View lastHoveredView;

    private View fragmentContainer;

    private View purchaseItem;

    public static final String LOCATION_FRAGMENT = "location";
    public static final String LOGIN_FRAGMENT = "login";

    private HeadFragment headFragment;

    private Subscription bookmarksSub;
    private Subscription historySub;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);
        IsmartvActivator.getInstance().addAccountChangeListener(this);
        addHeader();
        initViews();
//        selectProduct();

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
//            TasksFilterType currentFiltering =
//                    (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
//            mTasksPresenter.setFiltering(currentFiltering);
        }

        selectIndicator(getIntent());
    }

    private void addHeader() {
        headFragment = new HeadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", "usercenter");
        headFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.header, headFragment).commit();
    }

    private void initViews() {
        userCenterIndicatorLayout = (LinearLayout) findViewById(R.id.user_center_indicator_layout);

        fragmentContainer = findViewById(R.id.user_center_container);

//        fragmentContainer.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
//            @Override
//            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
//                Log.d(TAG, "new focus view: " + newFocus);
//                if (lastHoveredView != null) {
//                    lastHoveredView.setHovered(false);
//                }
//                clearTheLastHoveredVewState();
//                if (oldFocus != null && newFocus != null && oldFocus.getTag() != null && oldFocus.getTag().equals(newFocus.getTag())) {
//                    Log.d(TAG, "onGlobalFocusChanged same side");
//                    isFromRightToLeft = false;
//                } else {
//                    if (newFocus != null && newFocus.getTag() != null && ("left").equals(newFocus.getTag())) {
//                        Log.d(TAG, "onGlobalFocusChanged from right to left");
//                        isFromRightToLeft = true;
//                    } else {
//                        isFromRightToLeft = false;
//                    }
//
//                }
//            }
//        });

        createIndicatorView();
    }


    private void createIndicatorView() {
        indicatorView = new ArrayList<>();
        userCenterIndicatorLayout.removeAllViews();
        for (int i = 0; i < INDICATOR_TEXT_RES_ARRAY.length; i++) {
            View frameLayout = LayoutInflater.from(this).inflate(R.layout.item_usercenter_indicator, null);
            TextView textView = (TextView) frameLayout.findViewById(R.id.indicator_text);
            textView.setText(INDICATOR_TEXT_RES_ARRAY[i]);
            frameLayout.setTag("left");
            frameLayout.setId(INDICATOR_ID_RES_ARRAY[i]);
            frameLayout.setOnClickListener(indicatorViewOnClickListener);
            frameLayout.setOnFocusChangeListener(indicatorOnFocusListener);
            frameLayout.setOnHoverListener(indicatorOnHoverListener);
            if (i == 0){
                frameLayout.setNextFocusUpId(frameLayout.getId());
            }
            if (i == 1) {
                frameLayout.setNextFocusRightId(R.id.charge_money);
            }
            if (i == 5) {
                frameLayout.setNextFocusDownId(frameLayout.getId());
            }
            if (i == 2){
                frameLayout.setNextFocusRightId(R.id.pay_edit_mobile);
            }
            indicatorView.add(frameLayout);
            userCenterIndicatorLayout.addView(frameLayout);
        }

        if (IsmartvActivator.getInstance().isLogin()) {
            changeViewState(indicatorView.get(2), ViewState.Disable);
        }

//        userCenterIndicatorLayout.getChildAt(0).callOnClick();

    }


    private void selectProduct() {

        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof ProductFragment) {
            return;
        }

        mProductFragment = ProductFragment.newInstance();

        // Create the presenter
        mProductPresenter = new ProductPresenter(mProductFragment);

        ProductViewModel productViewModel =
                new ProductViewModel(getApplicationContext(), mProductPresenter);

        mProductFragment.setViewModel(productViewModel);

        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mProductFragment, R.id.user_center_container);


    }

    private void selectUserInfo() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof UserInfoFragment) {
            return;
        }
        // Create the fragment
        mUserInfoFragment = UserInfoFragment.newInstance();

        // Create the presenter
        mUserInfoPresenter = new UserInfoPresenter(mUserInfoFragment);

        UserInfoViewModel userInfoViewModel =
                new UserInfoViewModel(getApplicationContext(), mUserInfoPresenter);

        mUserInfoFragment.setViewModel(userInfoViewModel);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mUserInfoFragment, R.id.user_center_container);


    }

    private void selectLogin() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof LoginFragment) {
            return;
        }
        // Create the fragment
        if(mLoginFragment==null)
        mLoginFragment = LoginFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString("source", "usercenter");
        mLoginFragment.setArguments(bundle);
        mLoginFragment.setLoginCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mLoginFragment, R.id.user_center_container);

    }

    private void selectPurchaseHistory() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof PurchaseHistoryFragment) {
            return;
        }
        // Create the fragment
        mPurchaseHistoryFragment = PurchaseHistoryFragment.newInstance();
        // Create the presenter
        mPurchaseHistoryPresenter = new PurchaseHistoryPresenter(mPurchaseHistoryFragment);

        PurchaseHistoryViewModel purchaseHistoryViewModel =
                new PurchaseHistoryViewModel(getApplicationContext(), mPurchaseHistoryPresenter);

        mPurchaseHistoryFragment.setViewModel(purchaseHistoryViewModel);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mPurchaseHistoryFragment, R.id.user_center_container);


    }

    private void selectHelp() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof HelpFragment) {
            return;
        }
        // Create the fragment
        mHelpFragment = HelpFragment.newInstance();
        // Create the presenter
        mHelpPresenter = new HelpPresenter(mHelpFragment);

        HelpViewModel helpViewModel =
                new HelpViewModel(getApplicationContext(), mHelpPresenter);

        mHelpFragment.setViewModel(helpViewModel);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mHelpFragment, R.id.user_center_container);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppConstant.purchase_referer = "profile";
        fargmentIsActive = true;
        baseChannel="";
        baseSection="";
        if (IsmartvActivator.getInstance().isLogin()) {
            changeViewState(indicatorView.get(2), ViewState.Disable);
        } else {
            changeViewState(indicatorView.get(2), ViewState.Enable);
        }
    }

    private void selectLocation() {
        // Create the fragment
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof LocationFragment) {
            return;
        }
        mLocationFragment = LocationFragment.newInstance();
        // Create the presenter
        mLocationPresenter = new LocationPresenter(mLocationFragment);

        LocationViewModel locationViewModel =
                new LocationViewModel(getApplicationContext(), mLocationPresenter);

        mLocationFragment.setViewModel(locationViewModel);
        // Create the fragment

        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mLocationFragment, R.id.user_center_container);


    }

    @Override
    public void onSuccess() {
        changeViewState(indicatorView.get(2), ViewState.Disable);
        indicatorView.get(1).callOnClick();
        indicatorView.get(1).requestFocus();
        changeViewState(indicatorView.get(1), ViewState.Select);

        fetchFavorite();
        getHistoryByNet();
    }

    private View.OnFocusChangeListener indicatorOnFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.isHovered()) {
                return;
            }

            if (hasFocus) {
                if (isOnKeyDown) {
                    for (View myView : indicatorView) {
                        myView.setHovered(false);
                    }

                    changeViewState(v, ViewState.Select);
                    messageHandler.removeMessages(MSG_INDICATOR_CHANGE);
                    Message message = messageHandler.obtainMessage(MSG_INDICATOR_CHANGE, v);
                    messageHandler.sendMessageDelayed(message, 300);
                }
            } else {
                changeViewState(v, ViewState.Unfocus);
            }
        }
    };

    private View.OnClickListener indicatorViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mLocationFragment != null) {
                mLocationFragment.clearStatus();
            }

            int i = v.getId();
            if (i == R.id.usercenter_store) {
                selectProduct();
            } else if (i == R.id.usercenter_userinfo) {
                selectUserInfo();
            } else if (i == R.id.usercenter_login_register) {
                selectLogin();
            } else if (i == R.id.usercenter_purchase_history) {
                selectPurchaseHistory();
            } else if (i == R.id.usercenter_help) {
                selectHelp();
            } else if (i == R.id.usercenter_location) {
                selectLocation();
            }
            changeViewState(v, ViewState.Select);

        }
    };


    private View.OnHoverListener indicatorOnHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(final View v, MotionEvent event) {
            isOnKeyDown = false;
            ImageView textHoverImage = (ImageView) v.findViewById(R.id.text_select_bg);
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setHovered(true);
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }

                    if (lastHoveredView != null) {
                        ImageView lastTextSelectImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
                        lastTextSelectImage.setVisibility(View.INVISIBLE);
                    }
                    textHoverImage.setVisibility(View.VISIBLE);
                    lastHoveredView = v;
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    textHoverImage.setVisibility(View.INVISIBLE);
                    v.setHovered(false);
                    break;
            }
            return false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isOnKeyDown = true;
        if (lastHoveredView != null) {
            lastHoveredView.setHovered(false);
        }
        return super.onKeyDown(keyCode, event);
    }


    private void changeViewState(View parentView, ViewState viewState) {
        TextView textView = (TextView) parentView.findViewById(R.id.indicator_text);
        ImageView textSelectImage = (ImageView) parentView.findViewById(R.id.text_select_bg);
        ImageView textFocusImage = (ImageView) parentView.findViewById(R.id.text_focus_bg);
        switch (viewState) {
            case Select:
                if (parentView.isEnabled()) {
                    if (lastSelectedView != null) {
                        ImageView lastTextSelectImage = (ImageView) lastSelectedView.findViewById(R.id.text_select_bg);
                        ImageView lastTextFocusImage = (ImageView) lastSelectedView.findViewById(R.id.text_focus_bg);

                        lastTextSelectImage.setVisibility(View.INVISIBLE);
                        lastTextFocusImage.setVisibility(View.INVISIBLE);
                    }

                    if (lastHoveredView != null) {
                        ImageView lastTextHoverImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
                        lastTextHoverImage.setVisibility(View.INVISIBLE);
                    }

                    textSelectImage.setVisibility(View.VISIBLE);
                    textFocusImage.setImageResource(R.drawable.usercenter_indicator_focused);
                    textFocusImage.setVisibility(View.VISIBLE);
                    lastSelectedView = parentView;
                    lastHoveredView = parentView;
                }
                break;
            case Unfocus:
                textSelectImage.setVisibility(View.INVISIBLE);
                break;
            case Disable:
                parentView.setEnabled(false);
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                textView.setText(R.string.usercenter_login);
                textView.setTextColor(getResources().getColor(R.color.personinfo_login_button_disable));
                parentView.setFocusable(false);
                parentView.setFocusableInTouchMode(false);
                parentView.setClickable(false);
                break;
            case Enable:
                parentView.setEnabled(true);
                parentView.setFocusable(true);
                parentView.setFocusableInTouchMode(true);
                textView.setText(R.string.usercenter_login_register);
                textView.setTextColor(getResources().getColor(R.color._ffffff));
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                parentView.setBackgroundResource(R.drawable._000000000);
                parentView.setClickable(true);
                break;
        }

    }

    @Override
    public void onLogout() {
        changeViewState(indicatorView.get(2), ViewState.Enable);
        indicatorView.get(1).callOnClick();
        indicatorView.get(1).requestFocus();
        changeViewState(indicatorView.get(1), ViewState.Select);
    }

    @Override
    protected void onDestroy() {
        IsmartvActivator.getInstance().removeAccountChangeListener(this);
        super.onDestroy();
    }

    private enum ViewState {
        Enable,
        Disable,
        Select,
        Unfocus,
        Hover,
        None
    }

    public void clearTheLastHoveredVewState() {
        if (lastHoveredView != null) {
            ImageView lastTextSelectImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
            lastTextSelectImage.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.user_center_container) instanceof LocationFragment) {
            if (!mLocationFragment.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INDICATOR_CHANGE:
                    if (fargmentIsActive) {
                        View view = (View) msg.obj;
                        view.callOnClick();
                    }
                    break;
            }
        }
    };

    private void selectIndicator(Intent intent) {
        String flag = intent.getStringExtra("flag");
        if (!TextUtils.isEmpty(flag)) {
            if (flag.equals(LOCATION_FRAGMENT)) {
                indicatorView.get(5).callOnClick();
                indicatorView.get(5).requestFocus();
                changeViewState(indicatorView.get(5), ViewState.Select);
                selectLocation();
            }
        } else {
            indicatorView.get(0).callOnClick();
            indicatorView.get(0).requestFocus();
            changeViewState(indicatorView.get(0), ViewState.Select);
        }
    }

    @Override
    protected void onPause() {
        fargmentIsActive = false;
        if (messageHandler.hasMessages(MSG_INDICATOR_CHANGE)) {
            messageHandler.removeMessages(MSG_INDICATOR_CHANGE);
        }

        if (bookmarksSub != null && bookmarksSub.isUnsubscribed()) {
            bookmarksSub.unsubscribe();
        }

        if (historySub != null && historySub.isUnsubscribed()) {
            historySub.unsubscribe();
        }
        super.onPause();
    }

    public void refreshWeather() {
        if (headFragment != null) {
            HashMap<String, String> hashMap = IsmartvActivator.getInstance().getCity();
            String geoId = hashMap.get("geo_id");
            headFragment.fetchWeatherInfo(geoId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == CardPayFragment.CHARGE_MONEY_SUCCESS) {
            if (mUserInfoFragment != null) {
                mUserInfoFragment.setShowChargeSuccessPop(true);
            }
        }
    }


    private void fetchFavorite() {
        bookmarksSub = mSkyService.getBookmarks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Item[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(Item[] items) {
                        for (Item item : items) {
                            addFavorite(item);
                        }
                    }
                });
    }


    private void addFavorite(Item mItem) {
        if (isFavorite(mItem)) {
            String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItem.pk + "/";
            // DaisyUtils.getFavoriteManager(getContext())
            // .deleteFavoriteByUrl(url,"yes");
        } else {
            String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItem.pk + "/";
            Favorite favorite = new Favorite();
            favorite.title = mItem.title;
            favorite.adlet_url = mItem.adlet_url;
            favorite.content_model = mItem.content_model;
            favorite.url = url;
            favorite.quality = mItem.quality;
            favorite.is_complex = mItem.is_complex;
            favorite.isnet = "yes";
            DaisyUtils.getFavoriteManager(this).addFavorite(favorite, favorite.isnet);
        }
    }


    private boolean isFavorite(Item mItem) {
        if (mItem != null) {
            String url = mItem.item_url;
            if (url == null && mItem.pk != 0) {
                url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItem.pk + "/";
            }
            Favorite favorite = DaisyUtils.getFavoriteManager(this).getFavoriteByUrl(url, "yes");
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }

    private void getHistoryByNet() {
        historySub = mSkyService.getHistoryByNet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Item[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(Item[] items) {
                        for (Item item : items) {
                            addHistory(item);
                        }
                    }
                });
    }

    private void addHistory(Item item) {
        History history = new History();
        history.title = item.title;
        history.adlet_url = item.adlet_url;
        history.content_model = item.content_model;
        history.is_complex = item.is_complex;
        history.last_position = item.offset;
        history.last_quality = item.quality;
        if ("subitem".equals(item.model_name)) {
            history.sub_url = item.url;
            history.url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + item.item_pk + "/";
        } else {
            history.url = item.url;
        }

        history.is_continue = true;
        if (IsmartvActivator.getInstance().isLogin())
            DaisyUtils.getHistoryManager(this).addHistory(history, "yes", -1);
        else
            DaisyUtils.getHistoryManager(this).addHistory(history, "no", -1);

    }

    public void changeUserInfoSelectStatus(){
        indicatorView.get(1).callOnClick();
        indicatorView.get(1).requestFocus();
        changeViewState(indicatorView.get(1), ViewState.Select);
    }
}
