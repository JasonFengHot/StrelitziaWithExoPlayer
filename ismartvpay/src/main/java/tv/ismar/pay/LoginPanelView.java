package tv.ismar.pay;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import cn.ismartv.activator.Activator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import tv.ismar.daisy.BaseActivity;
import tv.ismar.daisy.R;
import tv.ismar.daisy.VodApplication;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SimpleRestClient.HttpPostRequestInterface;
import tv.ismar.daisy.core.preferences.AccountSharedPrefs;
import tv.ismar.daisy.models.Favorite;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.ui.widget.dialog.MessageDialogFragment;
import tv.ismar.sakura.utils.DeviceUtils;

import java.net.ContentHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginPanelView extends LinearLayout {

    private Button identifyCodeBtn;
    private SimpleRestClient mSimpleRestClient;
    private EditText edit_identifycode;
    private Button btn_submit;
    private EditText edit_mobile;
    private TextView count_tip;
    private IsmartCountTimer timeCount;
    private boolean suspension_window = false;
    private Item[] mHistoriesByNet;
    private Context mcontext;

    public LoginPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoginPanelView);
        LayoutInflater mInflater = LayoutInflater.from(context);
        View myView;
        suspension_window = a.getBoolean(R.styleable.LoginPanelView_suspension_window, false);
        if (suspension_window) {
            myView = mInflater.inflate(R.layout.pay_person_login, null);
        } else {
            myView = mInflater.inflate(R.layout.person_center_login, null);
        }
        mcontext = context;
        setOrientation(LinearLayout.VERTICAL);
        mSimpleRestClient = new SimpleRestClient();
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
//        edit_mobile.setOnHoverListener(onHoverListener);
//        edit_identifycode.setOnHoverListener(onHoverListener);
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
                mSimpleRestClient.doSendRequest("/accounts/login/", "post",
                        "device_token=" + SimpleRestClient.device_token
                                + "&username="
                                + edit_mobile.getText().toString()
                                + "&auth_number=" + identifyCode,
                        new HttpPostRequestInterface() {

                            @Override
                            public void onPrepare() {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onSuccess(String info) {
                                // TODO Auto-generated method stub
                                try {
                                    org.json.JSONObject json = new org.json.JSONObject(
                                            info);
                                    String auth_token = json
                                            .getString(VodApplication.AUTH_TOKEN);
                                    String zuser_token = json
                                            .getString(VodApplication.AUTH_TOKEN);
                                    DaisyUtils
                                            .getVodApplication(getContext())
                                            .getEditor()
                                            .putString(
                                                    VodApplication.AUTH_TOKEN,
                                                    auth_token);
                                    DaisyUtils
                                            .getVodApplication(getContext())
                                            .getEditor()
                                            .putString(
                                                    VodApplication.MOBILE_NUMBER,
                                                    edit_mobile.getText()
                                                            .toString());
                                    DaisyUtils.getVodApplication(getContext())
                                            .save();
                                    SimpleRestClient.access_token = auth_token;
                                    SimpleRestClient.mobile_number = edit_mobile
                                            .getText().toString();
                                    SimpleRestClient.zuser_token = json.getString("zuser_token");
                                    AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs
                                            .getInstance();
                                    accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ZUSER_TOKEN, json.getString("zuser_token"));
                                    GetFavoriteByNet();
                                    getHistoryByNet();
                                    showLoginSuccessPopup();
                                    if (callback != null) {
                                        callback.onSuccess(auth_token);
                                    }
                                    String bindflag = DaisyUtils.getVodApplication(mcontext)
                                            .getPreferences().getString(VodApplication.BESTTV_AUTH_BIND_FLAG, "");
                                    if (StringUtils.isNotEmpty(bindflag) && "privilege".equals(bindflag)) {
                                        bindBestTvauth();
                                    }

                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    callback.onFailed(e.toString());
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailed(String error) {
                                // TODO Auto-generated method stub
                                callback.onFailed(error);
                                setcount_tipText("登录失败");
                            }

                        });
            }
        });
        identifyCodeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if ("".equals(edit_mobile.getText().toString())) {
                    // count_tip.setText("请输入手机号");
                    setcount_tipText("请输入手机号");
                    return;
                }
                boolean ismobile = isMobileNumber(edit_mobile.getText()
                        .toString());
                if (!ismobile) {
                    // count_tip.setText("不是手机号码");
                    setcount_tipText("不是手机号码");
                    return;
                }
                timeCount = new IsmartCountTimer(identifyCodeBtn, R.drawable.person_btn_selector, R.drawable.btn_disabled_bg);
                timeCount.start();
                count_tip.setVisibility(View.VISIBLE);

                edit_identifycode.requestFocus();

                mSimpleRestClient.doSendRequest("/accounts/auth/", "post",
                        "device_token=" + SimpleRestClient.device_token
                                + "&username="
                                + edit_mobile.getText().toString(),
                        new HttpPostRequestInterface() {

                            @Override
                            public void onSuccess(String info) {
                                count_tip.setText("获取验证码成功，请提交!       ");
                                count_tip.setTextColor(Color.WHITE);
                                count_tip.setVisibility(View.VISIBLE);

                            }

                            @Override
                            public void onPrepare() {
                                count_tip.setText("60秒后可再次点击获取验证码       ");
                                count_tip.setTextColor(Color.WHITE);
                                count_tip.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFailed(String error) {
                                // TODO Auto-generated method stub
                                timeCount.cancel();
                                identifyCodeBtn.setEnabled(true);
                                identifyCodeBtn.setBackgroundResource(R.drawable.channel_item_normal);
                                identifyCodeBtn.setText("获取验证码");
                                setcount_tipText("获取验证码失败\n");
                            }
                        });
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

    LoginInterface callback;

    public void setLoginListener(LoginInterface callback) {
        this.callback = callback;
    }

    public interface LoginInterface {
        public void onSuccess(String info);

        public void onFailed(String error);
    }


    private void setcount_tipText(String str) {
        count_tip.setText(str + "       ");
        count_tip.setTextColor(Color.RED);
        count_tip.setVisibility(View.VISIBLE);
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
                    }

                    @Override
                    public void onFailed(String error) {
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
            history.url = SimpleRestClient.root_url + "/api/item/" + item.item_pk + "/";
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

    private Item[] FavoriteList;

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
            lp.width = ((int) (width * 0.55));
            lp.height = ((int) (height * 0.39));
            lp.x = ((int) (width * 0.335));
            lp.y = ((int) (height * 0.39));
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

    ;

    private void bindBestTvauth() {
        long timestamp = System.currentTimeMillis();
        Activator activator = Activator.getInstance(getContext());
        String mac = DeviceUtils.getLocalMacAddress(mcontext);
        mac = mac.replace("-", "").replace(":", "");
        String rsaResult = activator.PayRsaEncode("sn="
                + SimpleRestClient.sn_token + "&timestamp=" + timestamp);
        String params = "device_token=" + SimpleRestClient.device_token
                + "&access_token=" + SimpleRestClient.access_token
                + "&sharp_bestv=" + mac
                + "&timestamp=" + timestamp + "&sign=" + rsaResult;
        mSimpleRestClient.doSendRequest(SimpleRestClient.root_url
                        + "/accounts/combine/", "post", params,
                new HttpPostRequestInterface() {

                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onSuccess(String info) {
                        DaisyUtils.getVodApplication(mcontext).getEditor().putString(VodApplication.BESTTV_AUTH_BIND_FLAG, "combined");
                    }

                    @Override
                    public void onFailed(String error) {
                    }

                });
    }

    private void showLoginSuccessPopup() {
        String msg = mcontext.getString(R.string.login_success_name);
        String phoneNumber = edit_mobile.getText().toString();
        final MessageDialogFragment dialog = new MessageDialogFragment(mcontext, String.format(msg, phoneNumber), mcontext.getString(R.string.login_success));
        dialog.showAtLocation(this, Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                    }
                },
                null
        );
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
}
