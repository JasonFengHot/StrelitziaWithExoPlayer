package tv.ismar.pay;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.widget.MessagePopWindow;

public class LoginPanelView extends LinearLayout {

    private Button identifyCodeBtn;
    private EditText edit_identifycode;
    private Button btn_submit;
    private EditText edit_mobile;
    private TextView count_tip;
    private IsmartCountTimer timeCount;
    private boolean suspension_window = false;
    private Context mcontext;

    private SkyService mSkyService;

    public LoginPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSkyService = SkyService.ServiceManager.getService();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoginPanelView);
        LayoutInflater mInflater = LayoutInflater.from(context);
        View myView;
        suspension_window = a.getBoolean(R.styleable.LoginPanelView_suspension_window, false);
        myView = mInflater.inflate(R.layout.pay_person_login, null);
        mcontext = context;
        setOrientation(LinearLayout.VERTICAL);
        addView(myView);
        initView();
        a.recycle();
    }

    private void initView() {
        count_tip = (TextView) findViewById(R.id.pay_count_tip);
        edit_mobile = (EditText) findViewById(R.id.pay_edit_mobile);
        edit_identifycode = (EditText) findViewById(R.id.pay_edit_identifycode);
        identifyCodeBtn = (Button) findViewById(R.id.pay_identifyCodeBtn);
        btn_submit = (Button) findViewById(R.id.pay_btn_submit);
        btn_submit.setOnHoverListener(onHoverListener);
        identifyCodeBtn.setOnHoverListener(onHoverListener);
        edit_mobile.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                return false;
            }
        });

        btn_submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });


        identifyCodeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchVerificationCode();
            }
        });

    }

    public static boolean isMobileNumber(String mobiles) {
//        Pattern p = Pattern
//                .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
//        Matcher m = p.matcher(mobiles);
//        return m.matches();
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
//        long timestamp = System.currentTimeMillis();
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

        mSkyService.accountsCombine(sharpBestv, timestamp, sign)
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
        String msg = mcontext.getString(R.string.login_success_name);
        String phoneNumber = edit_mobile.getText().toString();
        final MessagePopWindow dialog = new MessagePopWindow(mcontext);
        dialog.setFirstMessage(String.format(msg, phoneNumber));
        dialog.setSecondMessage(mcontext.getString(R.string.login_success));
        dialog.showAtLocation(this, Gravity.CENTER, 0, 0, new MessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
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
        if ("".equals(edit_mobile.getText().toString())) {
            setcount_tipText("请输入手机号");
            return;
        }
        boolean ismobile = isMobileNumber(edit_mobile.getText()
                .toString());
        if (!ismobile) {
            setcount_tipText("不是手机号码");
            return;
        }
        timeCount = new IsmartCountTimer(identifyCodeBtn, R.drawable.person_btn_selector, R.drawable.btn_disabled_bg);
        timeCount.start();
        count_tip.setVisibility(View.VISIBLE);

        edit_identifycode.requestFocus();

        count_tip.setText("60秒后可再次点击获取验证码       ");
        count_tip.setTextColor(Color.WHITE);
        count_tip.setVisibility(View.VISIBLE);

        String username = "";
        mSkyService.accountsAuth(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        timeCount.cancel();
                        identifyCodeBtn.setEnabled(true);
                        identifyCodeBtn.setBackgroundResource(R.drawable.channel_item_normal);
                        identifyCodeBtn.setText("获取验证码");
                        setcount_tipText("获取验证码失败\n");
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        count_tip.setText("获取验证码成功，请提交!       ");
                        count_tip.setTextColor(Color.WHITE);
                        count_tip.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void doLogin() {
        // TODO Auto-generated method stub
        String identifyCode = edit_identifycode.getText().toString();
        if ("".equals(identifyCode)) {
            // count_tip.setText("验证码不能为空!");
            setcount_tipText("验证码不能为空!");
            return;
        }
        if ("".equals(edit_mobile.getText().toString())) {
            setcount_tipText("手机号不能为空!");
            return;
        }
        boolean ismobile = isMobileNumber(edit_mobile.getText()
                .toString());
        if (!ismobile) {
            // count_tip.setText("不是手机号码");
            setcount_tipText("不是手机号码");
            return;
        }
//                mSimpleRestClient.doSendRequest("/accounts/login/", "post",
//                        "device_token=" + SimpleRestClient.device_token
//                                + "&username="
//                                + edit_mobile.getText().toString()
//                                + "&auth_number=" + identifyCode,
//                        new HttpPostRequestInterface() {
//
//                            @Override
//                            public void onPrepare() {
//                                // TODO Auto-generated method stub
//
//                            }
//
//                            @Override
//                            public void onSuccess(String info) {
//                                // TODO Auto-generated method stub
//                                try {
//                                    org.json.JSONObject json = new org.json.JSONObject(
//                                            info);
//                                    String auth_token = json
//                                            .getString(VodApplication.AUTH_TOKEN);
//                                    String zuser_token = json
//                                            .getString(VodApplication.AUTH_TOKEN);
//                                    DaisyUtils
//                                            .getVodApplication(getContext())
//                                            .getEditor()
//                                            .putString(
//                                                    VodApplication.AUTH_TOKEN,
//                                                    auth_token);
//                                    DaisyUtils
//                                            .getVodApplication(getContext())
//                                            .getEditor()
//                                            .putString(
//                                                    VodApplication.MOBILE_NUMBER,
//                                                    edit_mobile.getText()
//                                                            .toString());
//                                    DaisyUtils.getVodApplication(getContext())
//                                            .save();
//                                    SimpleRestClient.access_token = auth_token;
//                                    SimpleRestClient.mobile_number = edit_mobile
//                                            .getText().toString();
//                                    SimpleRestClient.zuser_token = json.getString("zuser_token");
//                                    AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs
//                                            .getInstance();
//                                    accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ZUSER_TOKEN, json.getString("zuser_token"));
//                                    GetFavoriteByNet();
//                                    getHistoryByNet();
//                                    showLoginSuccessPopup();
//                                    if (callback != null) {
//                                        callback.onSuccess(auth_token);
//                                    }
//                                    String bindflag = DaisyUtils.getVodApplication(mcontext)
//                                            .getPreferences().getString(VodApplication.BESTTV_AUTH_BIND_FLAG, "");
//                                    if (StringUtils.isNotEmpty(bindflag) && "privilege".equals(bindflag)) {
//                                        bindBestTvauth();
//                                    }
//
//                                } catch (JSONException e) {
//                                    // TODO Auto-generated catch block
//                                    callback.onFailed(e.toString());
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onFailed(String error) {
//                                // TODO Auto-generated method stub
//                                callback.onFailed(error);
//                                setcount_tipText("登录失败");
//                            }
//
//                        });
        String username = "";
        String authNumber = "";

        mSkyService.accountsLogin(username, authNumber)
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
}

