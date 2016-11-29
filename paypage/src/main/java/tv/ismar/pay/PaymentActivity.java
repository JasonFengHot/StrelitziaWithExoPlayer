package tv.ismar.pay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.pay.LoginFragment.LoginCallback;

/**
 * Created by huibin on 9/13/16.
 */
public class PaymentActivity extends BaseActivity implements View.OnClickListener, LoginCallback {
    private static final String TAG = "PaymentActivity";
    private LoginFragment loginFragment;
    private Fragment weixinFragment;
    private Fragment alipayFragment;
    private Fragment cardpayFragment;
    private Fragment balanceFragment;

    private Button weixinPayBtn;
    private Button aliPayBtn;
    private Button cardPayBtn;
    private Button balancePayBtn;
    private ViewGroup payTypeLayout;

    private Subscription mOrderCheckLoopSubscription;

    private String category;
    private int pk;

    private ItemEntity mItemEntity;
    private TextView title;
    private TextView loginTip;
    private TextView username;

    public static final int PAYMENT_REQUEST_CODE = 0xd6;
    public static final int PAYMENT_SUCCESS_CODE = 0x5c;
    public static final int PAYMENT_FAILURE_CODE = 0xd2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        category = intent.getStringExtra(PageIntentInterface.EXTRA_PRODUCT_CATEGORY);
        setContentView(R.layout.activity_payment);

        weixinPayBtn = (Button) findViewById(R.id.weixin);
        aliPayBtn = (Button) findViewById(R.id.alipay);
        cardPayBtn = (Button) findViewById(R.id.videocard);
        balancePayBtn = (Button) findViewById(R.id.balance_pay);
        title = (TextView) findViewById(R.id.payment_title);
        payTypeLayout = (ViewGroup) findViewById(R.id.pay_type_layout);
        loginTip = (TextView) findViewById(R.id.login_tip);
        username = (TextView) findViewById(R.id.username);

        weixinPayBtn.setOnClickListener(this);
        aliPayBtn.setOnClickListener(this);
        cardPayBtn.setOnClickListener(this);
        balancePayBtn.setOnClickListener(this);

        loginFragment = new LoginFragment();
        loginFragment.setLoginCallback(this);
        weixinFragment = new WeixinPayFragment();
        alipayFragment = new AlipayFragment();
        cardpayFragment = new CardPayFragment();
        balanceFragment = new BalancePayFragment();

        if (category.equals(PageIntentInterface.ProductCategory.charge.name())) {
            changeChagrgeStatus();
            title.setText("充值");
        } else {
            String itemJson = intent.getStringExtra(PageIntent.EXTRA_ITEM_JSON);
            if (!TextUtils.isEmpty(itemJson)) {
                mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);
                pk = mItemEntity.getPk();
                purchaseCheck(CheckType.PlayCheck);

            } else {
                pk = intent.getIntExtra("pk", 0);
                fetchItem(pk, category);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (i == R.id.weixin) {
            transaction.replace(R.id.fragment_page, weixinFragment).commit();
        } else if (i == R.id.alipay) {
            transaction.replace(R.id.fragment_page, alipayFragment).commit();
        } else if (i == R.id.videocard) {
            transaction.replace(R.id.fragment_page, cardpayFragment).commit();
        } else if (i == R.id.balance_pay) {
            transaction.replace(R.id.fragment_page, balanceFragment).commit();
        }
    }

    public void fetchAccountBalance() {
        mSkyService.accountsBalance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountBalanceEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(AccountBalanceEntity entity) {
                        if (entity.getBalance().floatValue() == 0) {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_page, weixinFragment)
                                    .commit();
                        } else {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_page, balanceFragment)
                                    .commit();
                        }
                    }
                });
    }


    public void purchaseCheck(CheckType checkType) {
        purchaseCheck(checkType, false);
    }

    public void purchaseCheck(CheckType checkType, boolean forceCheck) {
        if (!forceCheck) {
            if (mItemEntity.isRepeat_buy() && checkType == CheckType.PlayCheck) {
                return;
            }
        }

        if ("package".equalsIgnoreCase(category)) {
            orderCheckLoop(checkType, null, String.valueOf(pk), null);
        } else if ("subitem".equalsIgnoreCase(category)) {
            orderCheckLoop(checkType, null, null, String.valueOf(pk));
        } else {
            orderCheckLoop(checkType, String.valueOf(pk), null, null);
        }
    }

    @Override
    public void onSuccess() {
        fetchAccountBalance();
        changeLoginStatus(true);
        purchaseCheck(PaymentActivity.CheckType.PlayCheck, true);
    }

    public enum CheckType {
        PlayCheck,
        OrderPurchase
    }


    private void orderCheckLoop(final CheckType checkType, final String item, final String pkg, final String subItem) {
        if (mOrderCheckLoopSubscription != null && !mOrderCheckLoopSubscription.isUnsubscribed()) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        mOrderCheckLoopSubscription = Observable.interval(0, 10, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long aLong) {
                        switch (checkType) {
                            case PlayCheck:
                                try {
                                    return mSkyService.playcheck(item, pkg, subItem)
                                            .execute().body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            case OrderPurchase:
                                try {
                                    return mSkyService.orderpurchase(item, pkg, subItem)
                                            .execute().body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                        return null;
                    }
                })
                .takeUntil(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String responseBody) {
                        if (TextUtils.isEmpty(responseBody.toString()) || "0".equals(responseBody.toString())) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                })
                .take(60)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "orderCheckLoop onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "orderCheckLoop onError: " + e.getMessage());
                        purchaseCheck(CheckType.OrderPurchase);
                    }

                    @Override
                    public void onNext(String responseBody) {
                        if (responseBody != null && !"0".equals(responseBody)) {
                            setResult(PAYMENT_SUCCESS_CODE);
                            finish();
                        }
                    }
                });
    }

    public void createOrder(final OderType type, final QrcodeCallback callback) {
        String waresId = String.valueOf(pk);
        String waresType = category;
        String source = type.name();
        String timestamp = null;
        String sign = null;


        if (type == OderType.sky) {
            timestamp = TrueTime.now().getTime() + "";
            IsmartvActivator activator = IsmartvActivator.getInstance();
            String encode = "sn=" + activator.getSnToken()
                    + "&source=sky" + "&timestamp=" + timestamp
                    + "&wares_id=" + pk + "&wares_type="
                    + category;
            sign = activator.encryptWithPublic(encode);
        }


        mSkyService.apiOrderCreate(waresId, waresType, source, timestamp, sign)
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
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        opt.inPurgeable = true;
                        opt.inInputShareable = true;
                        switch (type) {
                            case alipay:
                                opt.inSampleSize = 2;
                                break;
                        }
                        callback.onBitmap(BitmapFactory.decodeStream(responseBody.byteStream(), null, opt));
                    }
                });

    }


    enum OderType {
        weixin,
        alipay,
        sky
    }

    interface QrcodeCallback {
        void onBitmap(Bitmap bitmap);
    }

    public String getModel() {
        return category;
    }


    public int getPk() {
        return pk;
    }

    private void fetchItem(int pk, String model) {
        String opt = model;

        mSkyService.apiOptItem(String.valueOf(pk), opt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity = itemEntity;
                        title.setText(itemEntity.getTitle());
                        purchaseCheck(CheckType.PlayCheck);
                        if (TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken())) {

                            changeLoginStatus(false);
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_page, loginFragment)
                                    .commit();
                        } else {
                            changeLoginStatus(true);
                            fetchAccountBalance();
                        }
                    }
                });
    }

    public ItemEntity getmItemEntity() {
        return mItemEntity;
    }

    public void changeLoginStatus(boolean isLogin) {
        if (isLogin) {
            username.setText(String.format(getString(R.string.welocome_tip), IsmartvActivator.getInstance().getUsername()));
            loginTip.setVisibility(View.GONE);
            for (int i = 0; i < payTypeLayout.getChildCount(); i++) {
                Button button = (Button) payTypeLayout.getChildAt(i);
                button.setTextColor(getResources().getColor(R.color.color_base_white));
                button.setEnabled(true);
                button.setFocusable(true);

            }
            payTypeLayout.getChildAt(3).requestFocus();

        } else {
            loginTip.setVisibility(View.VISIBLE);
            for (int i = 0; i < payTypeLayout.getChildCount(); i++) {
                Button button = (Button) payTypeLayout.getChildAt(i);
                button.setTextColor(getResources().getColor(R.color.paychannel_button_disable));
                button.setFocusable(false);
                button.setEnabled(false);
            }
        }
        if (payTypeLayout.getVisibility() == View.INVISIBLE)
            payTypeLayout.setVisibility(View.VISIBLE);
    }


    public void changeChagrgeStatus() {
        loginTip.setVisibility(View.INVISIBLE);
        for (int i = 0; i < payTypeLayout.getChildCount(); i++) {
            if (i != 2) {
                Button button = (Button) payTypeLayout.getChildAt(i);
                button.setTextColor(getResources().getColor(R.color.paychannel_button_disable));
                button.setFocusable(false);
                button.setEnabled(false);
            }
        }
        if (payTypeLayout.getVisibility() == View.INVISIBLE)
            payTypeLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_page, cardpayFragment)
                .commit();
    }


    @Override
    protected void onStop() {
        if (mOrderCheckLoopSubscription != null && !mOrderCheckLoopSubscription.isUnsubscribed()) {
            mOrderCheckLoopSubscription.unsubscribe();
        }
        mOrderCheckLoopSubscription = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
