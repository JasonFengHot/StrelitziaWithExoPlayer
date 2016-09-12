package tv.ismar.pay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnHoverListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class PaymentDialog extends Dialog {

    private static final String QRCODE_BASE_URL = "/api/order/create/";
    private static final String BALANCEPAY_BASE_URL = "/api/order/create/";
    private static final String GETBALANCE_BASE_URL = "/accounts/balance/";
    private static final String CARDRECHARGE_BASE_URL = "https://order.tvxio.com/api/pay/verify/";
    public static final String PURCHASE_PRUCHASE_BASE_URL = "/api/order/purchase/";
    public static final String PURCHASE_CHECK_BASE_URL = "/api/play/check/";
    public static final String ALI_PAY = "/api/order/choose_way/";
    private static final int REFRESH_PAY_STATUS = 0x10;
    private static final int SETQRCODE_VIEW = 0x11;
    private static final int PURCHASE_CHECK_RESULT = 0x12;
    private static final int ORDER_CHECK_INTERVAL = 10000;
    private static final int LOGIN_SUCESS = 0x14;
    private static final int SETDAIKOUPANELVISIBLE = 0x15;
    private static final int DAIKOU_ERROR = 0x16;
    private Context mycontext;
    private int width;
    private int height;
    private Button weixinpay_button;
    private Button guanyingcard_button;
    private Button zhifubao_button;
    private Button yuepay_button;
    private Button top_loginregister_button;
    private Button submit_cardpay;
    private Button submit_yuepay;
    private Button yueepay_canel;
    private Button top_login;

    private LinearLayout guanyingcard_pay_panel;
    private LoginPanelView login_panel;
    private LinearLayout qrcode_pay;
    private LinearLayout shiyuncard_panel;
    private RelativeLayout top_login_panel;
    private LinearLayout qrcode_panel;
    private RelativeLayout daikou_panel;
    private ImageView qrcodeview;
    private MessagePopWindow loginPopup;
    private TextView payinfo_price;
    private TextView payinfo_exprice;
    private TextView package_price;
    private TextView videotitle;
    private TextView recharge_error_msg;
    private TextView welocome_tip;
    private TextView card_balance_title_label;
    private TextView panel_label;
    private EditText shiyuncard_input;

    private Bitmap qrcodeBitmap;
    private Item mItem;
    private OrderResultListener paylistener;
    private int ordercheckcount;
    private boolean flag = true;
    private SimpleRestClient mSimpleRestClient;
    private Item[] mHistoriesByNet;
    private ImageView payment_shadow_view;
    private TextView ali_price;
    private TextView ali_exprie;
    private Button alipay_submit;
    private String ordertimestamp = System.currentTimeMillis() + "";

    public PaymentDialog(Context context) {
        super(context);
    }

    public PaymentDialog(Context context, int theme,
                         OrderResultListener paylistener) {
        super(context, theme);
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        mycontext = context;
        if (StringUtils.isNotEmpty(SimpleRestClient.access_token)
                && StringUtils.isNotEmpty(SimpleRestClient.mobile_number)) {
            getBalanceByToken();
        }
        this.paylistener = paylistener;
        mSimpleRestClient = new SimpleRestClient();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.paymentselect);
        ((BaseActivity) mycontext).setLoginCallback(this);
        initView();
        resizeWindow();
        if (StringUtils.isNotEmpty(SimpleRestClient.access_token)
                && StringUtils.isNotEmpty(SimpleRestClient.mobile_number)) {
            welocome_tip.setVisibility(View.VISIBLE);
            String welocome = mycontext.getResources().getString(
                    R.string.welocome_tip);
            welocome_tip.setText(String.format(welocome,
                    SimpleRestClient.mobile_number));
            if (mItem != null && !mItem.repeat_buy)
                purchaseCheck(PURCHASE_CHECK_BASE_URL);
        } else {
            welocome_tip.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        if (urlHandler.hasMessages(ORDER_CHECK_INTERVAL))
            urlHandler.removeMessages(ORDER_CHECK_INTERVAL);
        if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
            urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
        if (urlHandler.hasMessages(SETQRCODE_VIEW))
            urlHandler.removeMessages(SETQRCODE_VIEW);
        if (urlHandler.hasMessages(REFRESH_PAY_STATUS))
            urlHandler.removeMessages(REFRESH_PAY_STATUS);
        if (urlHandler.hasMessages(LOGIN_SUCESS))
            urlHandler.removeMessages(LOGIN_SUCESS);
        if (urlHandler.hasMessages(SETDAIKOUPANELVISIBLE))
            urlHandler.removeMessages(SETDAIKOUPANELVISIBLE);
        urlHandler.removeCallbacksAndMessages(null);
        if (qrcodeBitmap != null && qrcodeBitmap.isRecycled()) {
            qrcodeBitmap.recycle();
            qrcodeBitmap = null;
        }
        super.dismiss();
    }

    public void setItem(Item item) {
        mItem = item;
    }

    private void resizeWindow() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ((int) (width * 0.83));
        lp.height = ((int) (height * 0.83));
    }

    private void setbackground(int id, View v) {

        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;

        opt.inPurgeable = true;

        opt.inInputShareable = true;
        // opt.inTargetDensity =
        // mycontext.getResources().getDisplayMetrics().densityDpi;
        // opt.inDensity =
        // mycontext.getResources().getDisplayMetrics().densityDpi;

        InputStream is = mycontext.getResources().openRawResource(

                id);

        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

        BitmapDrawable bd = new BitmapDrawable(mycontext.getResources(), bm);
        v.setBackgroundDrawable(bd);
    }

    private void initView() {
        // View v = findViewById(R.id.large_layout);
        // setbackground(R.drawable.person_center_bg,v);
        weixinpay_button = (Button) findViewById(R.id.weixin);
        guanyingcard_button = (Button) findViewById(R.id.videocard);
        zhifubao_button = (Button) findViewById(R.id.zhifubao);
        yuepay_button = (Button) findViewById(R.id.balance_pay);
        top_loginregister_button = (Button) findViewById(R.id.top_login);
        submit_cardpay = (Button) findViewById(R.id.shiyuncard_submit);
        submit_yuepay = (Button) findViewById(R.id.card_balance_submit);
        yueepay_canel = (Button) findViewById(R.id.card_balance_cancel);
        top_login = (Button) findViewById(R.id.top_login);

        weixinpay_button.setOnClickListener(buttonClick);
        guanyingcard_button.setOnClickListener(buttonClick);
        zhifubao_button.setOnClickListener(buttonClick);
        yuepay_button.setOnClickListener(buttonClick);
        top_loginregister_button.setOnClickListener(buttonClick);
        submit_cardpay.setOnClickListener(buttonClick);
        submit_yuepay.setOnClickListener(buttonClick);
        yueepay_canel.setOnClickListener(buttonClick);
        top_login.setOnClickListener(buttonClick);

        guanyingcard_pay_panel = (LinearLayout) findViewById(R.id.guanyingcard_pay_panel);
        login_panel = (LoginPanelView) findViewById(R.id.login_panel);
        qrcode_pay = (LinearLayout) findViewById(R.id.qrcode_pay);
        shiyuncard_panel = (LinearLayout) findViewById(R.id.shiyuncard_panel);
        top_login_panel = (RelativeLayout) findViewById(R.id.top_login_panel);

        qrcodeview = (ImageView) findViewById(R.id.qrcodeview);

        payinfo_price = (TextView) findViewById(R.id.payinfo_price);
        payinfo_exprice = (TextView) findViewById(R.id.payinfo_exprice);
        package_price = (TextView) findViewById(R.id.package_price);
        videotitle = (TextView) findViewById(R.id.videotitle);
        recharge_error_msg = (TextView) findViewById(R.id.recharge_error_msg);
        welocome_tip = (TextView) findViewById(R.id.welocome_tip);
        card_balance_title_label = (TextView) findViewById(R.id.card_balance_title_label);
        payment_shadow_view = (ImageView) findViewById(R.id.payment_shadow_view);
        shiyuncard_input = (EditText) findViewById(R.id.shiyuncard_input);
        panel_label = (TextView) findViewById(R.id.panel_label);
        qrcode_panel = (LinearLayout) findViewById(R.id.qrcode_panel);
        daikou_panel = (RelativeLayout) findViewById(R.id.daikou_panel);
        ali_price = (TextView) findViewById(R.id.ali_price);
        ali_exprie = (TextView) findViewById(R.id.ali_exprie);
        alipay_submit = (Button) findViewById(R.id.alipay_submit);
        alipay_submit.setOnClickListener(buttonClick);
        shiyuncard_input.setOnHoverListener(mOnHoverListener);
        yuepay_button.setOnHoverListener(mOnHoverListener);
        submit_cardpay.setOnHoverListener(mOnHoverListener);
        yueepay_canel.setOnHoverListener(mOnHoverListener);
        zhifubao_button.setOnHoverListener(mOnHoverListener);
        weixinpay_button.setOnHoverListener(mOnHoverListener);
        guanyingcard_button.setOnHoverListener(mOnHoverListener);
        submit_yuepay.setOnHoverListener(mOnHoverListener);
        setPackageInfo();
        if (StringUtils.isNotEmpty(SimpleRestClient.access_token)
                && StringUtils.isNotEmpty(SimpleRestClient.mobile_number)) {
//			changeQrcodePayPanelState(true, true);
            panel_label.setVisibility(View.GONE);
        } else {
            disableButton();
        }
        login_panel.setLoginListener(loginInterFace);
    }

    private void disableButton() {
        if (StringUtils.isEmpty(SimpleRestClient.access_token)
                && StringUtils.isEmpty(SimpleRestClient.mobile_number)) {
            weixinpay_button.setEnabled(false);
            weixinpay_button.setFocusable(false);
            weixinpay_button.setTextColor(getContext().getResources().getColor(
                    R.color.paychannel_button_disable));
            guanyingcard_button.setEnabled(false);
            guanyingcard_button.setFocusable(false);
            guanyingcard_button.setTextColor(getContext().getResources()
                    .getColor(R.color.paychannel_button_disable));
            zhifubao_button.setEnabled(false);
            zhifubao_button.setFocusable(false);
            zhifubao_button.setTextColor(getContext().getResources().getColor(
                    R.color.paychannel_button_disable));
            yuepay_button.setEnabled(false);
            yuepay_button.setFocusable(false);
            yuepay_button.setTextColor(getContext().getResources().getColor(
                    R.color.paychannel_button_disable));
            changeQrcodePayPanelState(false, false);
            changeLoginPanelState(true);
            changeYuePayPanelState(false, false);
            changeshiyuncardPanelState(false);
        }

    }

    private void enableButton() {
        weixinpay_button.setEnabled(true);
        weixinpay_button.setFocusable(true);
        weixinpay_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
        guanyingcard_button.setEnabled(true);
        guanyingcard_button.setFocusable(true);
        guanyingcard_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
        zhifubao_button.setEnabled(true);
        zhifubao_button.setFocusable(true);
        zhifubao_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
        yuepay_button.setEnabled(true);
        yuepay_button.setFocusable(true);
        yuepay_button.setTextColor(getContext().getResources().getColor(
                R.color.white));
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        private Activator activator;

        @Override
        public void onClick(View view) {
            if (qrcodeBitmap != null && qrcodeBitmap.isRecycled()) {
                qrcodeBitmap.recycle();
                qrcodeBitmap = null;
            }
            switch (view.getId()) {
                case R.id.weixin: {
                    if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
                        urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                    changeQrcodePayPanelState(true, true);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(false);
                    purchaseCheck(PURCHASE_PRUCHASE_BASE_URL);
                }
                break;
                case R.id.videocard: {
                    if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
                        urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(true);
                }
                break;
                case R.id.zhifubao: {
                    if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
                        urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                    changeQrcodePayPanelState(true, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(false);
                    purchaseCheck(PURCHASE_PRUCHASE_BASE_URL);
                }
                break;
                case R.id.balance_pay: {
                    ordertimestamp = System.currentTimeMillis() + "";
                    if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
                        urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(true, false);
                    changeshiyuncardPanelState(false);
                }
                break;
                case R.id.top_login: {
                    if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
                        urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                    flag = true;
                    // ((BaseActivity)mycontext).loginQQorWX();
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(true);
                    changeYuePayPanelState(false, false);
                    changeshiyuncardPanelState(false);
                    purchaseCheck(PURCHASE_PRUCHASE_BASE_URL);
                }
                break;

                case R.id.shiyuncard_submit: {
                    String inputValue = shiyuncard_input.getText().toString();
                    // inputValue = "9115134571196781";
                    if (inputValue.length() == 16 && isNumeric(inputValue)) {
                        card_recharge(inputValue);
                        submit_cardpay.setClickable(false);
                    } else {
                        recharge_error_msg.setVisibility(View.VISIBLE);
                        recharge_error_msg.setText("错误的观影卡密码");
                    }
                }
                break;

                case R.id.card_balance_submit: {
                    submit_yuepay.setClickable(false);
                    SimpleRestClient client = new SimpleRestClient();
                    String encode = "sn=" + SimpleRestClient.sn_token
                            + "&source=sky" + "&timestamp=" + ordertimestamp
                            + "&wares_id=" + mItem.pk + "&wares_type="
                            + mItem.model_name;

                    // encode =
                    // "sn=sk_39knib8o&source=sky&timestamp=1432014663168&wares_id=657880&wares_type=item";
                    activator = Activator.getInstance(getContext());
                    String rsaResult = activator.PayRsaEncode(encode);
                    // String rsaResult = activator.PayRsaEncodeByJava(encode);
                    if (rsaResult != null && !"".equals(rsaResult)) {
                        client.doSendRequest(BALANCEPAY_BASE_URL, "post",
                                "wares_id=" + mItem.pk + "&wares_type="
                                        + mItem.model_name + "&device_token="
                                        + SimpleRestClient.device_token
                                        + "&access_token="
                                        + SimpleRestClient.access_token
                                        + "&source=sky" + "&timestamp=" + ordertimestamp
                                        + "&sn=" + SimpleRestClient.sn_token
                                        + "&sign=" + rsaResult + "&event_id=" + rsaResult, balancePay);
                    }

                }
                break;

                case R.id.card_balance_cancel: {
                    doCancel();
                }
                break;
                case R.id.alipay_submit: {
                    new Thread() {
                        @Override
                        public void run() {
                            String url = alipay_submit.getTag().toString();
                            URL myFileUrl = null;
                            try {
                                myFileUrl = new URL(url);
                                HttpURLConnection connection = (HttpURLConnection) myFileUrl
                                        .openConnection();
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(1000);
                                connection.setReadTimeout(2000);
                                connection.connect();
                                int code = connection.getResponseCode();
                                StringBuffer sb = new StringBuffer();
                                if (code == 200) {
                                    BufferedReader buff = new BufferedReader(
                                            new InputStreamReader(
                                                    connection.getInputStream(),
                                                    "UTF-8"));
                                    String line = null;
                                    while ((line = buff.readLine()) != null) {
                                        sb.append(line);
                                    }
                                    buff.close();
                                    connection.disconnect();
                                }
                                JSONObject jsonObject = new JSONObject(
                                        sb.toString());
                                String status = jsonObject.getString("status");
                                if ("T".equals(status)) {
                                    urlHandler
                                            .sendEmptyMessage(PURCHASE_CHECK_RESULT);
                                } else {
                                    String display_message = jsonObject
                                            .getString("display_message");
                                    Message m = new Message();
                                    m.what = DAIKOU_ERROR;
                                    m.obj = display_message;
                                    urlHandler.sendMessage(m);
                                }
                            } catch (Exception e) {
                                Message m = new Message();
                                m.what = DAIKOU_ERROR;
                                m.obj = "代扣失败";
                                urlHandler.sendMessage(m);
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    break;
                }
            }
        }
    };

    private void changeYuePayPanelState(boolean visible, boolean needCheck) {
        if (visible) {
            getBalanceByToken();
            guanyingcard_pay_panel.setVisibility(View.VISIBLE);
        } else {
            guanyingcard_pay_panel.setVisibility(View.GONE);
        }
    }

    private void getBalanceByToken() {
        SimpleRestClient client = new SimpleRestClient();
        client.doSendRequest(GETBALANCE_BASE_URL, "get", "",
                fetchBalancerResult);
    }

    private android.os.Handler urlHandler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SETQRCODE_VIEW: {
                    qrcode_panel.setVisibility(View.VISIBLE);
                    daikou_panel.setVisibility(View.GONE);
                    qrcodeview.setImageBitmap(qrcodeBitmap);
                    if (qrcodeBitmap != null && qrcodeBitmap.isRecycled())
                        qrcodeBitmap.recycle();
                    qrcodeBitmap = null;
                    break;
                }
                case SETDAIKOUPANELVISIBLE: {
                    qrcode_panel.setVisibility(View.GONE);
                    daikou_panel.setVisibility(View.VISIBLE);
                    break;
                }
                case REFRESH_PAY_STATUS: {
                    paylistener.payResult(true);
                    dismiss();
                    break;
                }
                case PURCHASE_CHECK_RESULT: {
                    if (isShowing())
                        purchaseCheck(PURCHASE_PRUCHASE_BASE_URL);
                    break;
                }
                case LOGIN_SUCESS: {
                    welocome_tip.setVisibility(View.VISIBLE);
                    // top_login_panel.setVisibility(View.GONE);
                    // ((BaseActivity)mycontext).callWGQueryQQUserInfo();
                    break;
                }
                case DAIKOU_ERROR: {
                    if (msg.obj != null) {
                        String display_message = msg.obj.toString();
                        Toast.makeText(mycontext, display_message,
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
        }
    };

    private void changeQrcodePayPanelState(boolean visible,
                                           final boolean isweixin) {
        if (visible) {
            qrcodeview.setImageDrawable((new ColorDrawable(Color.WHITE)));
            qrcode_pay.setVisibility(View.VISIBLE);
            daikou_panel.setVisibility(View.GONE);
            new Thread() {

                @Override
                public void run() {
                    super.run();
                    if (isweixin) {
                        qrcodeBitmap = returnBitMap(SimpleRestClient.root_url
                                + QRCODE_BASE_URL, "POST", "wares_id="
                                + mItem.pk + "&wares_type=" + mItem.model_name
                                + "&device_token="
                                + SimpleRestClient.device_token
                                + "&access_token="
                                + SimpleRestClient.access_token
                                + "&source=weixin");
                        urlHandler.sendEmptyMessage(SETQRCODE_VIEW);
                        if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT)) {
                            urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                            urlHandler
                                    .sendEmptyMessageDelayed(
                                            PURCHASE_CHECK_RESULT,
                                            ORDER_CHECK_INTERVAL);
                        }
                    } else {
//						aliPayChannel(SimpleRestClient.root_url + ALI_PAY,
//								"wares_id=" + mItem.pk + "&wares_type="
//										+ mItem.model_name + "&device_token="
//										+ SimpleRestClient.device_token
//										+ "&access_token="
//										+ SimpleRestClient.access_token
//										+ "&source=alipay");
                        qrcodeBitmap = returnBitMap(SimpleRestClient.root_url
                                + QRCODE_BASE_URL, "POST", "wares_id="
                                + mItem.pk + "&wares_type=" + mItem.model_name
                                + "&device_token="
                                + SimpleRestClient.device_token
                                + "&access_token="
                                + SimpleRestClient.access_token
                                + "&source=alipay");
                        urlHandler.sendEmptyMessage(SETQRCODE_VIEW);
                        if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT)) {
                            urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                            urlHandler
                                    .sendEmptyMessageDelayed(
                                            PURCHASE_CHECK_RESULT,
                                            ORDER_CHECK_INTERVAL);
                        }
                    }
                }

            }.start();
        } else {
            qrcode_pay.setVisibility(View.GONE);
        }
    }

    private void changeshiyuncardPanelState(boolean visible) {
        if (visible) {
            shiyuncard_panel.setVisibility(View.VISIBLE);
            guanyingcard_button.setNextFocusRightId(R.id.shiyuncard_input);
        } else {
            shiyuncard_panel.setVisibility(View.GONE);
            guanyingcard_button.setNextFocusRightId(-1);
        }
    }

    private void changeLoginPanelState(boolean visible) {
        if (visible)
            login_panel.setVisibility(View.VISIBLE);
        else
            login_panel.setVisibility(View.GONE);
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private Bitmap returnBitMap(String url, String method, String params) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myFileUrl
                    .openConnection();
            if ("POST".equals(method)) {
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
            }
            connection.setRequestMethod(method);
            connection.connect();
            if ("POST".equals(method)) {
                DataOutputStream out = new DataOutputStream(
                        connection.getOutputStream());
                out.writeBytes(params);
                out.flush();
                out.close();
            }
            int code = connection.getResponseCode();
            if (code == 302) {
                String redirectlocation = connection.getHeaderField("Location");
                myFileUrl = new URL(redirectlocation);
                if (myFileUrl.getProtocol().toLowerCase().equals("https")) {
                    trustAllHosts();
                    HttpsURLConnection https = (HttpsURLConnection) myFileUrl.openConnection();
                    https.setHostnameVerifier(DO_NOT_VERIFY);
                    connection = https;
                } else {
                    connection = (HttpsURLConnection) myFileUrl.openConnection();
                }
                connection.setConnectTimeout(2000);
                connection.setRequestMethod("GET");
                connection.connect();
                code = connection.getResponseCode();
            }
            InputStream is = connection.getInputStream();
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            // opt.inTempStorage = new byte[1024];
            if (params.contains("alipay")) {
                opt.inSampleSize = 2;
            }
            bitmap = BitmapFactory.decodeStream(is, null, opt);
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkServerTrusted");
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aliPayChannel(String url, String params) {
        URL myFileUrl = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myFileUrl
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(2000);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();
            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());
            out.writeBytes(params);
            out.flush();
            out.close();
            int code = connection.getResponseCode();
            StringBuffer sb = new StringBuffer();
            if (code == 200) {
                BufferedReader buff = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = buff.readLine()) != null) {
                    sb.append(line);
                }
                buff.close();
                connection.disconnect();
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            String type = jsonObject.getString("type");
            if ("agreement".equals(type)) {
                JSONObject agreeOject = jsonObject.getJSONObject("agreement");
                String qrcodeUrl = agreeOject.getString("url");
                qrcodeBitmap = returnBitMap(qrcodeUrl, "GET", null);
                urlHandler.sendEmptyMessage(SETQRCODE_VIEW);
                if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT)) {
                    urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                    urlHandler.sendEmptyMessageDelayed(PURCHASE_CHECK_RESULT,
                            ORDER_CHECK_INTERVAL);
                }
            } else if ("qrcode".equals(type)) {
                JSONObject agreeOject = jsonObject.getJSONObject("qrcode");
                String qrcodeUrl = agreeOject.getString("url");
                qrcodeBitmap = returnBitMap(qrcodeUrl, "GET", null);
                urlHandler.sendEmptyMessage(SETQRCODE_VIEW);
                if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT)) {
                    urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                    urlHandler.sendEmptyMessageDelayed(PURCHASE_CHECK_RESULT,
                            ORDER_CHECK_INTERVAL);
                }
            } else if ("pay".equals(type)) {
                if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
                    urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
                JSONObject agreeOject = jsonObject.getJSONObject("pay");
                JSONObject commodity = agreeOject.getJSONObject("commodity");
                String qrcodeUrl = agreeOject.getString("url");
                alipay_submit.setTag(qrcodeUrl);
                double itemprice = commodity.getDouble("total_fee");
                ali_price.setText("价　格 : " + itemprice + "元");
                ali_exprie.setText("有效期 :  " + commodity.getString("duration")
                        + "天");
                urlHandler.sendEmptyMessage(SETDAIKOUPANELVISIBLE);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPackageInfo() {
        videotitle.setText(mItem.title);
        String price = mycontext.getResources().getString(
                R.string.pay_payinfo_price_label);
        String exprice = mycontext.getResources().getString(
                R.string.pay_payinfo_exprice_label);
        String package_info = mycontext.getResources().getString(
                R.string.pay_package_price);
        float itemprice = 0;
        if ("subitem".equalsIgnoreCase(mItem.model_name)) {
            itemprice = mItem.expense.subprice;
        } else if ("item".equalsIgnoreCase(mItem.model_name)) {
            itemprice = mItem.expense.price;
        } else if ("package".equalsIgnoreCase(mItem.model_name)) {
            itemprice = mItem.expense.price;
        }
        payinfo_price.setText(String.format(price, itemprice));
        payinfo_exprice.setText(String.format(exprice, mItem.expense.duration));
        package_price.setText(String.format(package_info, itemprice,
                mItem.expense.duration));
    }

    private void card_recharge(String cardNumber) {
        String pwd_prefix = cardNumber.substring(0, 10);
        String sur_prefix = cardNumber.substring(10, 16);
        String timestamp = System.currentTimeMillis() + "";
        String sid = "sid";
        String user = SimpleRestClient.mobile_number;
        String user_id = "0";
        String app_name = "sky";
        String sn = SimpleRestClient.sn_token;
        String card_secret = "";
        try {
            card_secret = SHA1(user + sur_prefix + timestamp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        card_secret = "card_secret=" + pwd_prefix + card_secret;
        app_name = "&app_name=" + app_name;
        user = "&user=" + SimpleRestClient.mobile_number;
        user_id = "&user_id=" + user_id;
        timestamp = "&timestamp=" + timestamp;
        sid = "&sid=" + sid;
        sn = "&sn=" + sn;

        String params = card_secret + app_name + user + user_id + timestamp
                + sid + sn;
        SimpleRestClient client = new SimpleRestClient();
        client.doSendRequest(CARDRECHARGE_BASE_URL, "post", params,
                rechargeResult);
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private String SHA1(String text) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private HttpPostRequestInterface fetchBalancerResult = new HttpPostRequestInterface() {
        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            try {
                JSONObject object = new JSONObject(info);
                String balance = StringUtils
                        .isNotEmpty(SimpleRestClient.access_token) ? object
                        .getString("balance") : object.getString("sn_balance");
                float balancefloat = Float.parseFloat(balance);
                String balancevalue = mycontext.getResources().getString(
                        R.string.pay_card_balance_title_label);
                card_balance_title_label.setText(String.format(balancevalue,
                        balancefloat));
                if (balancefloat < mItem.expense.price) {
                    submit_yuepay.setEnabled(false);
                    submit_yuepay.setFocusable(false);
                    submit_yuepay.setFocusableInTouchMode(false);
                    submit_yuepay.setTextColor(getContext().getResources()
                            .getColor(R.color.paychannel_button_disable));
                } else {
                    submit_yuepay.setEnabled(true);
                    submit_yuepay.setFocusable(true);
                    submit_yuepay.setFocusableInTouchMode(true);
                    submit_yuepay.setTextColor(getContext().getResources()
                            .getColor(R.color.white));
                }
                if (flag) {
                    if (balancefloat > mItem.expense.price) {
                        guanyingcard_pay_panel.setVisibility(View.VISIBLE);
                        changeQrcodePayPanelState(false, false);
                        changeLoginPanelState(false);
                        changeshiyuncardPanelState(false);
                        yuepay_button.requestFocus();
                    } else {
                        changeQrcodePayPanelState(true, true);
                        changeLoginPanelState(false);
                        changeYuePayPanelState(false, false);
                        changeshiyuncardPanelState(false);
                    }
                }
                flag = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailed(String error) {
        }
    };

    private HttpPostRequestInterface balancePay = new HttpPostRequestInterface() {

        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            try {
                JSONObject object = new JSONObject(info);
                String balance = object.getString("balance");
                float balancefloat = Float.parseFloat(balance);
                String balancevalue = mycontext.getResources().getString(
                        R.string.pay_card_balance_title_label);
                card_balance_title_label.setText(String.format(balancevalue,
                        balancefloat));
                purchaseCheck(PURCHASE_PRUCHASE_BASE_URL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailed(String error) {
            submit_yuepay.setClickable(true);
        }
    };

    private HttpPostRequestInterface rechargeResult = new HttpPostRequestInterface() {

        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            try {
                JSONObject object = new JSONObject(info);
                String statusString = object.getString("status");
                String errmsg = object.getString("err_desc");
                recharge_error_msg.setVisibility(View.VISIBLE);
                if ("S".equalsIgnoreCase(statusString)) {
                    recharge_error_msg.setText("充值成功,系统将自动为您购买,6s后返回");
                    changeQrcodePayPanelState(false, false);
                    changeLoginPanelState(false);
                    changeYuePayPanelState(true, true);
                    changeshiyuncardPanelState(false);
                    yuepay_button.requestFocus();
                } else if ("T".equalsIgnoreCase(statusString)) {
                    recharge_error_msg.setText("充值成功,系统将在第二天8点为您购买,10s后返回");
                } else {
                    recharge_error_msg.setText(errmsg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            submit_cardpay.setClickable(true);
        }

        @Override
        public void onFailed(String error) {
            recharge_error_msg.setVisibility(View.VISIBLE);
            recharge_error_msg.setText(error);
            submit_cardpay.setClickable(true);
        }
    };

    private void purchaseCheck(String patch) {
        if (urlHandler.hasMessages(PURCHASE_CHECK_RESULT))
            urlHandler.removeMessages(PURCHASE_CHECK_RESULT);
        SimpleRestClient client = new SimpleRestClient();
        String typePara = "&item=" + mItem.pk;
        if ("package".equalsIgnoreCase(mItem.model_name)) {
            typePara = "&package=" + mItem.pk;
        } else if ("subitem".equalsIgnoreCase(mItem.model_name)) {
            typePara = "&subitem=" + mItem.pk;
        }
        client.doSendRequest(patch, "post", "device_token="
                + SimpleRestClient.device_token + "&access_token="
                + SimpleRestClient.access_token + typePara, purchaseCheck);
    }

    private HttpPostRequestInterface purchaseCheck = new HttpPostRequestInterface() {

        @Override
        public void onPrepare() {
        }

        @Override
        public void onSuccess(String info) {
            if (info != null && !"0".equals(info)) {
                urlHandler.sendEmptyMessage(REFRESH_PAY_STATUS);
            } else {
                ordercheckcount++;
                if (ordercheckcount < 60)
                    urlHandler.sendEmptyMessageDelayed(PURCHASE_CHECK_RESULT,
                            ORDER_CHECK_INTERVAL);
            }
        }

        @Override
        public void onFailed(String error) {
            ordercheckcount++;
            if (ordercheckcount < 60)
                urlHandler.sendEmptyMessageDelayed(PURCHASE_CHECK_RESULT,
                        ORDER_CHECK_INTERVAL);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                doCancel();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private LoginPanelView.LoginInterface loginInterFace = new LoginPanelView.LoginInterface() {

        @Override
        public void onSuccess(String info) {
            urlHandler.sendEmptyMessage(LOGIN_SUCESS);
            enableButton();
            panel_label.setVisibility(View.GONE);
            getBalanceByToken();
            String welocome = mycontext.getResources().getString(
                    R.string.welocome_tip);
            welocome_tip.setText(String.format(welocome,
                    SimpleRestClient.mobile_number));
            purchaseCheck(PURCHASE_CHECK_BASE_URL);
        }

        @Override
        public void onFailed(String error) {
        }

    };

    private void doCancel() {
        paylistener.payResult(false);
        dismiss();
    }

    private String authToken;

    @Override
    public void onLoginSuccess(String result) {

        AuthTokenEntity authTokenEntity = new Gson().fromJson(result,
                AuthTokenEntity.class);
        authToken = authTokenEntity.getAuth_token();

        getBalanceByToken();

        String welocome = mycontext.getResources().getString(
                R.string.welocome_tip);
        welocome_tip.setText(String.format(welocome, nickname));
        DaisyUtils.getVodApplication(getContext()).getEditor()
                .putString(VodApplication.MOBILE_NUMBER, nickname);
        DaisyUtils.getVodApplication(getContext()).getEditor()
                .putString(VodApplication.AUTH_TOKEN, authToken);
        DaisyUtils.getVodApplication(getContext()).save();
        SimpleRestClient.mobile_number = nickname;

        SimpleRestClient.access_token = authToken;
        SimpleRestClient.zuser_token = authTokenEntity.getZuser_token();
        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs
                .getInstance();
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ZUSER_TOKEN, authTokenEntity.getZuser_token());
        GetFavoriteByNet();
        getHistoryByNet();
        urlHandler.sendEmptyMessage(LOGIN_SUCESS);
        enableButton();
        showLoginSuccessPopup();
    }

    @Override
    public void onLoginFailed() {

    }

    private String nickname;

    @Override
    public void oncallWGQueryQQUserInfo(String nickName) {
        nickname = nickName;

    }

    @Override
    public void onSameAccountListener() {
        Toast.makeText(getContext(), "输入相同账号!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelLogin() {

    }

    public interface OrderResultListener {
        public void payResult(boolean result);
    }

    class AccountAboutDialog extends Dialog {
        private int width;
        private int height;
        private TextView warnmsg_view;
        private Button ok_bt;
        private Button cancel_btButton;
        private Button account_bind_ok1_bt;
        private boolean iscancelshow;
        private String warningmessage;
        private LinearLayout account_bind_panel;

        public String getWarningmessage() {
            return warningmessage;
        }

        public void setWarningmessage(String warningmessage) {
            this.warningmessage = warningmessage;
        }

        public AccountAboutDialog(Context context, int theme) {
            super(context, theme);
            WindowManager wm = (WindowManager) getContext().getSystemService(
                    Context.WINDOW_SERVICE);
            width = wm.getDefaultDisplay().getWidth();
            height = wm.getDefaultDisplay().getHeight();
            setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.account_bind_dialog);
            warnmsg_view = (TextView) findViewById(R.id.account_bind_warn_msg);
            cancel_btButton = (Button) findViewById(R.id.account_bind_cancel_bt);
            account_bind_ok1_bt = (Button) findViewById(R.id.account_bind_ok1_bt);
            ok_bt = (Button) findViewById(R.id.account_bind_ok_bt);
            account_bind_panel = (LinearLayout) findViewById(R.id.account_bind_panel);
            account_bind_ok1_bt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // if
                    // (StringUtils.isNotEmpty(SimpleRestClient.access_token)) {
                    // warnmsg_view
                    // .setText(R.string.account_combine_msg);
                    // account_bind_panel.setVisibility(View.VISIBLE);
                    // account_bind_ok1_bt.setVisibility(View.GONE);
                    // }
                    dismiss();
                }
            });

            ok_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long timestamp = System.currentTimeMillis();
                    Activator activator = Activator.getInstance(getContext());
                    String rsaResult = activator.PayRsaEncode("sn="
                            + SimpleRestClient.sn_token + "&timestamp="
                            + timestamp);
                    String params = "device_token="
                            + SimpleRestClient.device_token + "&access_token="
                            + SimpleRestClient.access_token + "&timestamp="
                            + timestamp + "&sign=" + rsaResult;
                    mSimpleRestClient.doSendRequest(SimpleRestClient.root_url
                                    + "/accounts/combine/", "post", params,
                            new HttpPostRequestInterface() {

                                @Override
                                public void onPrepare() {
                                }

                                @Override
                                public void onSuccess(String info) {
                                    dismiss();
                                }

                                @Override
                                public void onFailed(String error) {
                                    dismiss();
                                }

                            });
                }
            });
            cancel_btButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            warnmsg_view.setText(warningmessage);
            resizeWindow();
        }

        public boolean isIscancelshow() {
            return iscancelshow;
        }

        public void setIscancelshow(boolean iscancelshow) {
            this.iscancelshow = iscancelshow;
        }

        private void resizeWindow() {
            Window dialogWindow = getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = ((int) (width * 0.46));
            lp.height = ((int) (height * 0.33));
            lp.x = ((int) (width * 0.335));
            lp.y = ((int) (height * 0.28));
            lp.gravity = Gravity.LEFT | Gravity.TOP;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    dismiss();
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    private void getHistoryByNet() {
        mSimpleRestClient.doSendRequest("/api/histories/", "get", "",
                new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        // Log.i(tag, msg);

                        // 解析json
                        mHistoriesByNet = mSimpleRestClient.getItems(info);
                        if (mHistoriesByNet != null) {
                            for (Item i : mHistoriesByNet) {
                                addHistory(i);
                            }
                        }

                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub
                        // Log.i(tag, msg);
                    }
                });
    }

    private Item[] FavoriteList;

    private void GetFavoriteByNet() {
        mSimpleRestClient.doSendRequest("/api/bookmarks/", "get", "",
                new HttpPostRequestInterface() {

                    @Override
                    public void onSuccess(String info) {
                        // TODO Auto-generated method stub
                        FavoriteList = mSimpleRestClient.getItems(info);
                        if (FavoriteList != null) {
                            // 添加记录到本地
                            for (Item i : FavoriteList) {
                                addFavorite(i);
                            }
                        }
                    }

                    @Override
                    public void onPrepare() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onFailed(String error) {
                        // TODO Auto-generated method stub

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
            history.url = SimpleRestClient.root_url + "/api/item/"
                    + item.item_pk + "/";
        } else {
            history.url = item.url;

        }
        history.is_continue = true;
        if (SimpleRestClient.isLogin())
            DaisyUtils.getHistoryManager(getContext()).addHistory(history,
                    "yes");
        else
            DaisyUtils.getHistoryManager(getContext())
                    .addHistory(history, "no");

    }

    private boolean isFavorite(Item mItem) {
        if (mItem != null) {
            String url = mItem.item_url;
            if (url == null && mItem.pk != 0) {
                url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                        + "/";
            }
            Favorite favorite = null;
            favorite = DaisyUtils.getFavoriteManager(getContext())
                    .getFavoriteByUrl(url, "yes");
            if (favorite != null) {
                return true;
            }
        }

        return false;
    }

    private void addFavorite(Item mItem) {
        if (isFavorite(mItem)) {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                    + "/";
            // DaisyUtils.getFavoriteManager(getContext())
            // .deleteFavoriteByUrl(url,"yes");
        } else {
            String url = SimpleRestClient.sRoot_url + "/api/item/" + mItem.pk
                    + "/";
            Favorite favorite = new Favorite();
            favorite.title = mItem.title;
            favorite.adlet_url = mItem.adlet_url;
            favorite.content_model = mItem.content_model;
            favorite.url = url;
            favorite.quality = mItem.quality;
            favorite.is_complex = mItem.is_complex;
            favorite.isnet = "yes";
            DaisyUtils.getFavoriteManager(getContext()).addFavorite(favorite,
                    favorite.isnet);
        }
    }

    private void showLoginSuccessPopup() {
        int xOffset = (int) mycontext.getResources().getDimension(
                R.dimen.loginfragment_successPop_xOffset);
        int yOffset = (int) mycontext.getResources().getDimension(
                R.dimen.loginfragment_successPop_yOffset);
        String msg = mycontext.getText(R.string.login_success_name).toString();
        payment_shadow_view.setVisibility(View.VISIBLE);
        loginPopup = new MessagePopWindow(mycontext);
        loginPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                payment_shadow_view.setVisibility(View.GONE);
            }
        });
        loginPopup.setFirstMessage(String.format(msg,
                SimpleRestClient.mobile_number));
        loginPopup.setSecondMessage(R.string.login_success);
        loginPopup.showAtLocation(login_panel, Gravity.CENTER, xOffset,
                yOffset, new MessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        loginPopup.dismiss();
                        payment_shadow_view.setVisibility(View.GONE);
                    }
                }, null);
    }

    private OnHoverListener mOnHoverListener = new OnHoverListener() {

        @Override
        public boolean onHover(View v, MotionEvent keycode) {
            switch (keycode.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
                default:
                    break;
            }
            return false;
        }
    };
}