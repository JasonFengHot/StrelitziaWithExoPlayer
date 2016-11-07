package tv.ismar.usercenter.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseFragment;
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
public class UserCenterActivity extends BaseActivity implements LoginFragment.LoginCallback, BaseFragment.OnViewLoadCallback {
    private static final String TAG = UserCenterActivity.class.getSimpleName();

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

    private View tmp;

    private ArrayList<View> indicatorView;

    private static final int[] INDICATOR_TEXT_RES_ARRAY = {
            R.string.usercenter_store,
            R.string.usercenter_userinfo,
            R.string.usercenter_login_register,
            R.string.usercenter_purchase_history,
            R.string.usercenter_help,
            R.string.usercenter_location
    };
    private LinearLayout userCenterIndicatorLayout;

    private View lastSelectedView;
    private View lastHoveredView;

    private boolean isSelecteFragment = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercenter);
        tmp = findViewById(R.id.tmp);
        initViews();
//        selectProduct();

        selectHelp();
        // Load previously saved state, if available.
        if (savedInstanceState != null) {
//            TasksFilterType currentFiltering =
//                    (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
//            mTasksPresenter.setFiltering(currentFiltering);
        }


    }

    private void initViews() {
        userCenterIndicatorLayout = (LinearLayout) findViewById(R.id.user_center_indicator_layout);
        createIndicatorView();
    }


    private void createIndicatorView() {
        indicatorView = new ArrayList<>();
        userCenterIndicatorLayout.removeAllViews();
        for (int res : INDICATOR_TEXT_RES_ARRAY) {
            View frameLayout = LayoutInflater.from(this).inflate(R.layout.item_usercenter_indicator, null);
            TextView textView = (TextView) frameLayout.findViewById(R.id.indicator_text);
            textView.setText(res);
            frameLayout.setTag(res);
            frameLayout.setOnClickListener(indicatorViewOnClickListener);
            frameLayout.setOnFocusChangeListener(indicatorOnFocusListener);
            frameLayout.setOnHoverListener(indicatorOnHoverListener);
            frameLayout.setNextFocusRightId(R.id.vertical_divider_line);
            indicatorView.add(frameLayout);
            userCenterIndicatorLayout.addView(frameLayout);
        }
    }

    private View.OnClickListener indicatorViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            changeViewState(v, ViewState.Select);
            int i = (int) v.getTag();
            if (i == R.string.usercenter_store) {
                selectProduct();
            } else if (i == R.string.usercenter_userinfo) {
                selectUserInfo();
            } else if (i == R.string.usercenter_login_register) {
                selectLogin();
            } else if (i == R.string.usercenter_purchase_history) {
                selectPurchaseHistory();
            } else if (i == R.string.usercenter_help) {
                selectHelp();
            } else if (i == R.string.usercenter_location) {
                selectLocation();
            }
        }
    };

    private void selectProduct() {

        // Create the fragment
        mProductFragment = ProductFragment.newInstance();
        mProductFragment.setViewLoadCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mProductFragment, R.id.user_center_container);

        // Create the presenter
        mProductPresenter = new ProductPresenter(mProductFragment);

        ProductViewModel productViewModel =
                new ProductViewModel(getApplicationContext(), mProductPresenter);

        mProductFragment.setViewModel(productViewModel);

    }

    private void selectUserInfo() {

        // Create the fragment
        mUserInfoFragment = UserInfoFragment.newInstance();
        mUserInfoFragment.setViewLoadCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mUserInfoFragment, R.id.user_center_container);

        // Create the presenter
        mUserInfoPresenter = new UserInfoPresenter(mUserInfoFragment);

        UserInfoViewModel userInfoViewModel =
                new UserInfoViewModel(getApplicationContext(), mUserInfoPresenter);

        mUserInfoFragment.setViewModel(userInfoViewModel);
    }

    private void selectLogin() {

        // Create the fragment
        mLoginFragment = LoginFragment.newInstance();
        mLoginFragment.setViewLoadCallback(this);
        mLoginFragment.setLoginCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mLoginFragment, R.id.user_center_container);

    }

    private void selectPurchaseHistory() {

        // Create the fragment
        mPurchaseHistoryFragment = PurchaseHistoryFragment.newInstance();
        mPurchaseHistoryFragment.setViewLoadCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mPurchaseHistoryFragment, R.id.user_center_container);

        // Create the presenter
        mPurchaseHistoryPresenter = new PurchaseHistoryPresenter(mPurchaseHistoryFragment);

        PurchaseHistoryViewModel purchaseHistoryViewModel =
                new PurchaseHistoryViewModel(getApplicationContext(), mProductPresenter);

        mPurchaseHistoryFragment.setViewModel(purchaseHistoryViewModel);
    }

    private void selectHelp() {
        // Create the fragment
        mHelpFragment = HelpFragment.newInstance();
        mHelpFragment.setViewLoadCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mHelpFragment, R.id.user_center_container);

        // Create the presenter
        mHelpPresenter = new HelpPresenter(mHelpFragment);

        HelpViewModel helpViewModel =
                new HelpViewModel(getApplicationContext(), mHelpPresenter);

        mHelpFragment.setViewModel(helpViewModel);
    }

    private void selectLocation() {

        // Create the fragment
        mLocationFragment = LocationFragment.newInstance();
        mLocationFragment.setViewLoadCallback(this);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), mLocationFragment, R.id.user_center_container);

        // Create the presenter
        mLocationPresenter = new LocationPresenter(mLocationFragment);

        LocationViewModel locationViewModel =
                new LocationViewModel(getApplicationContext(), mLocationPresenter);

        mLocationFragment.setViewModel(locationViewModel);

    }

    @Override
    public void onSuccess() {
        selectUserInfo();
    }

    private View.OnFocusChangeListener indicatorOnFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && isSelecteFragment) {
                changeViewState(v, ViewState.Select);
                int i = (int) v.getTag();
                if (i == R.string.usercenter_store) {
                    selectProduct();
                } else if (i == R.string.usercenter_userinfo) {
                    selectUserInfo();
                } else if (i == R.string.usercenter_login_register) {
                    selectLogin();
                } else if (i == R.string.usercenter_purchase_history) {
                    selectPurchaseHistory();
                } else if (i == R.string.usercenter_help) {
                    selectHelp();
                } else if (i == R.string.usercenter_location) {
                    selectLocation();
                }
            }
        }
    };

    private View.OnHoverListener indicatorOnHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            ImageView textHoverImage = (ImageView) v.findViewById(R.id.text_select_bg);
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (lastHoveredView != null) {
                        ImageView lastTextSelectImage = (ImageView) lastHoveredView.findViewById(R.id.text_select_bg);
                        lastTextSelectImage.setVisibility(View.INVISIBLE);
                    }
                    textHoverImage.setVisibility(View.VISIBLE);
                    lastHoveredView = v;
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    textHoverImage.setVisibility(View.INVISIBLE);
                    break;
                default:
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
                break;
            case None:
                textView.setTextColor(getResources().getColor(R.color._ffffff));
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                break;
            case Disable:
                textSelectImage.setVisibility(View.INVISIBLE);
                textFocusImage.setVisibility(View.INVISIBLE);
                textView.setText(R.string.usercenter_login);
                textView.setTextColor(getResources().getColor(R.color.personinfo_login_button_disable));
                parentView.setFocusable(false);
                parentView.setFocusableInTouchMode(false);
                parentView.setClickable(false);
                break;
            case Enable:
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
    public void loadComplete() {

//        if (lastSelectedView != null) {
//            isSelecteFragment = false;
//            lastSelectedView.requestFocus();
//            changeViewState(lastSelectedView, ViewState.Select);
//            isSelecteFragment = true;
//        }
    }

    private enum ViewState {
        Enable,
        Disable,
        Select,
        Hover,
        None
    }
}