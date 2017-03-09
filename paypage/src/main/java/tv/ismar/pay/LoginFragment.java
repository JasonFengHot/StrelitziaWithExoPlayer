package tv.ismar.pay;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.History;
import tv.ismar.app.entity.Item;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AccountsLoginEntity;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.statistics.AccountStatistics;

/**
 * Created by huibin on 2016/9/14.
 */
public class LoginFragment extends BaseFragment implements View.OnHoverListener {
    private Button identifyCodeBtn;
    private EditText edit_identifycode;
    private Button btn_submit;
    private EditText edit_mobile;
    private TextView count_tip;

    private SkyService mSkyService;

    private View contentView;

    private BaseActivity activity;

    private LoginCallback mLoginCallback;

    private String source = "";

    private ImageView tmp;
    private boolean fragmentIsPause = false;

    private Subscription countDownSubscription;
    private Subscription accountsCombineSub;
    private Subscription verificationCodeSub;
    private Subscription accountsLoginSub;
    private String phoneNumber;
    private String verification;


    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (BaseActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSkyService = activity.mSkyService;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Bundle bundle = getArguments();
        if (bundle != null) {
            source = bundle.getString("source");
        }

        if (!source.equals("usercenter")) {
            contentView = inflater.inflate(R.layout.fragment_pay_login_, null);

        } else {
            contentView = inflater.inflate(R.layout.fragment_pay_login, null);
        }

        return contentView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        Bundle bundle = getArguments();
        if (bundle != null) {
            source = bundle.getString("source");
        }

        if (!source.equals("usercenter")) {
            edit_mobile.requestFocus();
            edit_mobile.setNextFocusLeftId(edit_mobile.getId());
            identifyCodeBtn.setNextFocusUpId(identifyCodeBtn.getId());
            edit_identifycode.setNextFocusLeftId(edit_identifycode.getId());
            edit_identifycode.setNextFocusRightId(edit_identifycode.getId());
            btn_submit.setNextFocusLeftId(btn_submit.getId());

        } else {
            tmp.setNextFocusLeftId(R.id.usercenter_login_register);
        }

        btn_submit.setOnHoverListener(this);
        identifyCodeBtn.setOnHoverListener(this);

        if (source.equals("usercenter")) {
            edit_mobile.setNextFocusLeftId(R.id.usercenter_login_register);
            edit_identifycode.setNextFocusLeftId(R.id.usercenter_login_register);
            btn_submit.setNextFocusLeftId(R.id.usercenter_login_register);
        }
        if (savedInstanceState != null) {
            phoneNumber = savedInstanceState.getString("phone_number", "");
            verification = savedInstanceState.getString("verification_code", "");
        }

        if (phoneNumber != null) {
            edit_mobile.setText(phoneNumber);
        }
        if (verification != null) {
            edit_identifycode.setText(verification);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentIsPause = false;
    }

    private void initView() {
        tmp = (ImageView) contentView.findViewById(R.id.tmp);
        count_tip = (TextView) contentView.findViewById(R.id.pay_count_tip);
        edit_mobile = (EditText) contentView.findViewById(R.id.pay_edit_mobile);
        edit_identifycode = (EditText) contentView.findViewById(R.id.pay_edit_identifycode);
        identifyCodeBtn = (Button) contentView.findViewById(R.id.pay_identifyCodeBtn);
        btn_submit = (Button) contentView.findViewById(R.id.pay_btn_submit);

        btn_submit.setOnHoverListener(onHoverListener);
        identifyCodeBtn.setOnHoverListener(onHoverListener);
        edit_mobile.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                return false;
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });
        identifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_identifycode.requestFocus();
                edit_identifycode.requestFocusFromTouch();

                fetchVerificationCode();
            }
        });

    }

    public static boolean isMobileNumber(String mobiles) {
        if (mobiles.length() == 11) {
            return true;
        } else {
            return false;
        }
    }


    private void setcount_tipText(String str) {
        count_tip.setText(str + "       ");
        count_tip.setTextColor(Color.RED);
        count_tip.setVisibility(View.VISIBLE);
    }

    private void bindBestTvAuth() {
//        long timestamp = TrueTime.now().getTime();
//        Activator activator = Activator.getInstance(getContext());
//        String mac = DeviceUtils.getLocalMacAddress(mcontext);
//        mac = mac.replace("-", "").replace(":", "");
//        String rsaResult = activator.PayRsaEncode("sn="
//                + SimpleRestClient.sn_token + "&timestamp=" + timestamp);
//        String params = "device_token=" + SimpleRestClient.device_token
//                + "&access_token=" + SimpleRestClient.access_token
//                + "&sharp_bestv=" + mac
//                + "&timestamp=" + timestamp + "&sign=" + rsaResult;


        String sharpBestv = "";
        String timestamp = "";
        String sign = "";

        accountsCombineSub = mSkyService.accountsCombine(sharpBestv, timestamp, sign)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });

    }

    private void showLoginSuccessPopup() {
        String msg = activity.getString(R.string.login_success_name);
        String phoneNumber = edit_mobile.getText().toString();
        final ModuleMessagePopWindow dialog = new ModuleMessagePopWindow(activity);
        dialog.setFirstMessage(String.format(msg, phoneNumber));
        dialog.setSecondMessage(activity.getString(R.string.login_success));
        dialog.showAtLocation(contentView, Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                        if (mLoginCallback != null) {
                            mLoginCallback.onSuccess();
                        }
                    }
                },
                null);
    }

    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);
                    v.requestFocus();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
            }
            return false;
        }
    };

    private void fetchVerificationCode() {
//        edit_mobile.setText("15370770697");
        String username = edit_mobile.getText().toString();
        if ("".equals(edit_mobile.getText().toString())) {
            setcount_tipText("请输入手机号");
            return;
        }
        boolean ismobile = isMobileNumber(username);
        if (!ismobile) {
            setcount_tipText("不是手机号码");
            return;
        }

        verificationCodeSub = mSkyService.accountsAuth(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        identifyCodeBtn.setEnabled(true);
                        identifyCodeBtn.setBackgroundResource(R.drawable.channel_item_normal);
                        identifyCodeBtn.setText("获取验证码");
                        setcount_tipText("获取验证码失败\n");
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        timeCountDown();
                        count_tip.setText("60秒后可再次点击获取验证码       ");
                        count_tip.setTextColor(Color.WHITE);
                        count_tip.setVisibility(View.VISIBLE);
                        edit_identifycode.requestFocus();
                        count_tip.setText("获取验证码成功，请提交!       ");
                        count_tip.setTextColor(Color.WHITE);
                        count_tip.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void doLogin() {
        String username = edit_mobile.getText().toString();
        String authNumber = edit_identifycode.getText().toString();
        if ("".equals(authNumber)) {
            setcount_tipText("验证码不能为空!");
            return;
        }
        if ("".equals(username)) {
            setcount_tipText("手机号不能为空!");
            return;
        }
        boolean ismobile = isMobileNumber(username);
        if (!ismobile) {
            setcount_tipText("不是手机号码");
            return;
        }
        accountsLoginSub = mSkyService.accountsLogin(username, authNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountsLoginEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        setcount_tipText("登录失败");
                    }

                    @Override
                    public void onNext(AccountsLoginEntity entity) {

                        String username = edit_mobile.getText().toString();
                        IsmartvActivator.getInstance().saveUserInfo(username, entity.getAuth_token(), entity.getZuser_token());
                        loginStatistics(username);
                        if (mLoginCallback != null) {
                            mLoginCallback.onSuccess();
                        }
                        edit_mobile.setText("");
                        edit_identifycode.setText("");
                        if (countDownSubscription!=null && !countDownSubscription.isUnsubscribed()){
                            countDownSubscription.unsubscribe();
                        }
                        showLoginSuccessPopup();

                    }
                });
    }


    private void loginStatistics(String username) {
        new AccountStatistics().userLogin(username);
    }

    private void timeCountDown() {
        final int count = 60;
        if (countDownSubscription != null && !countDownSubscription.isUnsubscribed()) {
            countDownSubscription.isUnsubscribed();
        }

        countDownSubscription = Observable.interval(0, 1, TimeUnit.SECONDS, Schedulers.io())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long time) {
                        Log.i("time count down: ", "thread: " + Thread.currentThread().getName());
                        return count - time.intValue();
                    }
                })
                .take(60)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        identifyCodeBtn.setText("获取验证码");
                        identifyCodeBtn.setEnabled(true);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 1) {
                            edit_mobile.setNextFocusRightId(identifyCodeBtn.getId());
                            identifyCodeBtn.setText("获取验证码");
                            identifyCodeBtn.setEnabled(true);
                        } else {
                            edit_mobile.setNextFocusRightId(edit_mobile.getId());
                            identifyCodeBtn.setEnabled(false);
                            identifyCodeBtn.setTextColor(Color.WHITE);
                            identifyCodeBtn.setText(integer + " s");
                        }
                    }
                });
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
                if (!fragmentIsPause) {
                    tmp.requestFocus();
                    tmp.requestFocusFromTouch();
                }
                break;
        }
        return false;
    }

    public interface LoginCallback {
        void onSuccess();
    }

    public void setLoginCallback(LoginCallback loginCallback) {
        mLoginCallback = loginCallback;
    }

    @Override
    public void onPause() {
        if (accountsCombineSub != null && accountsCombineSub.isUnsubscribed()) {
            accountsCombineSub.unsubscribe();
        }
        if (verificationCodeSub != null && verificationCodeSub.isUnsubscribed()) {
            verificationCodeSub.unsubscribe();
        }
        if (accountsLoginSub != null && accountsLoginSub.isUnsubscribed()) {
            accountsLoginSub.unsubscribe();
        }

        btn_submit.setOnHoverListener(null);
        identifyCodeBtn.setOnHoverListener(null);
        edit_mobile.setOnEditorActionListener(null);
        btn_submit.setOnClickListener(null);
        identifyCodeBtn.setOnClickListener(null);
        onHoverListener = null;

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e("login","onSaveInstanceState");
        outState.putString("phone_number", edit_mobile.getText().toString());
        outState.putString("verification_code", edit_identifycode.getText().toString());
        super.onSaveInstanceState(outState);
    }

}