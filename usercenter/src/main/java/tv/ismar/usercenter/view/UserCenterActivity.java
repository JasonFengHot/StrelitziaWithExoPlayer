package tv.ismar.usercenter.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.ActivityUtils;
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
        IsmartvActivator.AccountChangeCallback, PurchaseHistoryFragment.PurchaseLoadCallback {
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
    private boolean isFromRightToLeft = false;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);
        IsmartvActivator.getInstance().addAccountChangeListener(this);
        initViews();
        addHeader();
//        selectProduct();

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
//            TasksFilterType currentFiltering =
//                    (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
//            mTasksPresenter.setFiltering(currentFiltering);
        }
    }

    private void addHeader() {
        HeadFragment headFragment = new HeadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", "usercenter");
        headFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.header, headFragment).commit();
    }

    private void initViews() {
        userCenterIndicatorLayout = (LinearLayout) findViewById(R.id.user_center_indicator_layout);

        fragmentContainer = findViewById(R.id.user_center_container);

        fragmentContainer.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                Log.d(TAG, "new focus view: " + newFocus);
                if (lastHoveredView != null) {
                    lastHoveredView.setHovered(false);
                }
                clearTheLastHoveredVewState();
                if (oldFocus != null && newFocus != null && oldFocus.getTag() != null && oldFocus.getTag().equals(newFocus.getTag())) {
                    Log.d(TAG, "onGlobalFocusChanged same side");
                    isFromRightToLeft = false;
                } else {
                    if (newFocus != null && newFocus.getTag() != null && ("left").equals(newFocus.getTag())) {
                        Log.d(TAG, "onGlobalFocusChanged from right to left");
                        isFromRightToLeft = true;
                    } else {
                        isFromRightToLeft = false;
                    }

                }
            }
        });

        createIndicatorView();

        purchaseItem = userCenterIndicatorLayout.getChildAt(3);
        purchaseItem.setNextFocusRightId(purchaseItem.getId());

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
            if (i == 1) {
                frameLayout.setNextFocusRightId(R.id.charge_money);
            }
            if (i == 5) {
                frameLayout.setNextFocusDownId(frameLayout.getId());
            }
            indicatorView.add(frameLayout);
            userCenterIndicatorLayout.addView(frameLayout);
        }

        if (IsmartvActivator.getInstance().isLogin()) {
            changeViewState(indicatorView.get(2), ViewState.Disable);
        }

        userCenterIndicatorLayout.getChildAt(0).callOnClick();
    }

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
        mPurchaseHistoryFragment.setPurchaseLoadCallback(this);

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
        changeViewState(indicatorView.get(1), ViewState.Select);
        selectUserInfo();

    }

    private View.OnFocusChangeListener indicatorOnFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.isHovered()) {
                return;
            }
            if (hasFocus) {
                if (!isFromRightToLeft) {
                    v.callOnClick();
                    v.callOnClick();
                } else {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.user_center_container);
                    View itemView = null;
                    if (fragment instanceof ProductFragment) {
                        itemView = userCenterIndicatorLayout.getChildAt(0);
                    } else if (fragment instanceof UserInfoFragment) {
                        itemView = userCenterIndicatorLayout.getChildAt(1);
                    } else if (fragment instanceof LoginFragment) {
                        itemView = userCenterIndicatorLayout.getChildAt(2);
                    } else if (fragment instanceof PurchaseHistoryFragment) {
                        itemView = userCenterIndicatorLayout.getChildAt(3);
                    } else if (fragment instanceof HelpFragment) {
                        itemView = userCenterIndicatorLayout.getChildAt(4);
                    } else if (fragment instanceof LocationFragment) {
                        itemView = userCenterIndicatorLayout.getChildAt(5);
                    }
                    if (itemView != null && itemView.hasFocus()) {
                        itemView.callOnClick();
                    } else if (itemView != null) {
                        itemView.requestFocus();
                    }
                }
            } else {
                changeViewState(v, ViewState.Unfocus);
            }
        }
    };


    private View.OnHoverListener indicatorOnHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            if (mIndicatorItemHoverCallback != null) {
                mIndicatorItemHoverCallback.onIndicatorItemHover();
            }
            ImageView textHoverImage = (ImageView) v.findViewById(R.id.text_select_bg);
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setHovered(true);
                    v.requestFocus();
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
            return true;
        }
    };

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
    }

    @Override
    protected void onDestroy() {
        IsmartvActivator.getInstance().removeAccountChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onPurchaseLoadFinish() {
        purchaseItem.setNextFocusRightId(View.NO_ID);
    }

    private enum ViewState {
        Enable,
        Disable,
        Select,
        Unfocus,
        Hover,
        None
    }

    public interface IndicatorItemHoverCallback {
        void onIndicatorItemHover();
    }

    private IndicatorItemHoverCallback mIndicatorItemHoverCallback;

    public void setIndicatorItemHoverCallback(IndicatorItemHoverCallback indicatorItemHoverCallback) {
        mIndicatorItemHoverCallback = indicatorItemHoverCallback;
    }

    public void clearTheLastHoveredVewState() {
        if (lastHoveredView != null) {
            ImageView lastTextSelectImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
            lastTextSelectImage.setVisibility(View.INVISIBLE);
        }

    }
}