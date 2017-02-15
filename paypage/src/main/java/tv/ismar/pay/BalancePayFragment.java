package tv.ismar.pay;

import cn.ismartv.truetime.TrueTime;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.statistics.PurchaseStatistics;


/**
 * Created by huibin on 2016/9/14.
 */
public class BalancePayFragment extends Fragment implements View.OnClickListener, OnHoverListener {

    private View contentView;
    private Button submitBtn;
    private Button cancleBtn;

    private PaymentActivity activity;

    private TextView balanceTv;
    private TextView priceTv;
    private TextView durationTv;
    private ItemEntity itemEntity;

    private TextView payErrorTip;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            float result = (float) msg.obj;
            balanceTv.setText(String.format(getString(R.string.pay_card_balance_title_label), result));
            activity.finish();
            return false;
        }
    });
    private Subscription apiOrderCreateSub;
    private Subscription accountsBalanceSub;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (PaymentActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragmet_balancepay, null);
        submitBtn = (Button) contentView.findViewById(R.id.card_balance_submit);
        submitBtn.setOnClickListener(this);
        cancleBtn = (Button) contentView.findViewById(R.id.card_balance_cancel);
        cancleBtn.setOnClickListener(this);

        balanceTv = (TextView) contentView.findViewById(R.id.card_balance_title_label);
        priceTv = (TextView) contentView.findViewById(R.id.package_price);
        durationTv = (TextView) contentView.findViewById(R.id.package_exprice_label);
        payErrorTip = (TextView) contentView.findViewById(R.id.pay_error_tip);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        submitBtn.setOnHoverListener(this);
        cancleBtn.setOnHoverListener(this);
        fetchAccountBalance();

        itemEntity = activity.getmItemEntity();
        priceTv.setText(String.format(getString(R.string.pay_package_price),
                itemEntity.getExpense().getPrice(), itemEntity.getExpense().getDuration()));
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.card_balance_submit) {
            createOrder();
            purchaseClickStatistics();
        } else if (i == R.id.card_balance_cancel) {
            activity.finish();
        }
    }

    private void purchaseClickStatistics() {
        int item = itemEntity.getItemPk();
        String userId = IsmartvActivator.getInstance().getUsername();
        String title = itemEntity.getTitle();
        String clip = "";
        if (itemEntity.getClip() != null) {
            clip = String.valueOf(itemEntity.getClip().getPk());
        }

        String type = "ismartv";
        new PurchaseStatistics().videoPurchaseClick(String.valueOf(item), userId, title, clip, type);
    }

    private void purchaseOkStatistics(String valid, float balance) {

        String item = String.valueOf(itemEntity.getItemPk());
        String account = String.valueOf(balance);
        String price = String.valueOf(itemEntity.getExpense().getPrice());
        String userId = IsmartvActivator.getInstance().getUsername();
        String type = "ismartv";
        String clip = "";
        if (itemEntity.getClip() != null) {
            clip = String.valueOf(itemEntity.getClip().getPk());
        }
        String title = itemEntity.getTitle();
        new PurchaseStatistics().videoPurchaseOk(item, account, valid, price, userId, type, clip, title);
    }

    public void createOrder() {
        String waresId = String.valueOf(activity.getPk());
        String waresType = activity.getModel();
        String source = "sky";
        String timestamp = null;
        String sign = null;

        {
            timestamp = TrueTime.now().getTime() + "";
            IsmartvActivator activator = IsmartvActivator.getInstance();
            String encode = "sn=" + activator.getSnToken()
                    + "&source=sky" + "&timestamp=" + timestamp
                    + "&wares_id=" + waresId + "&wares_type="
                    + waresType;
            sign = activator.encryptWithPublic(encode);
        }

        apiOrderCreateSub = activity.mSkyService.apiOrderCreate("create", waresId, waresType, source, timestamp, sign, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        payErrorTip.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String json = null;
                        try {
                            json = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        float result = new JsonParser().parse(json).getAsJsonObject().get("balance").getAsFloat();
                        purchaseOkStatistics("", result);
                        activity.setResult(PaymentActivity.PAYMENT_SUCCESS_CODE);
                        Message message = new Message();
                        message.what = 0;
                        message.obj = result;
                        handler.sendMessageDelayed(message, 1000);
                    }
                });

    }

    private void fetchAccountBalance() {
        accountsBalanceSub = activity.mSkyService.accountsBalance()
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
                        if (entity.getBalance().compareTo(new BigDecimal(itemEntity.getExpense().getPrice())) >= 0) {
                            submitBtn.setEnabled(true);
                        } else {
                            submitBtn.setEnabled(false);
                            submitBtn.setFocusable(false);
                            submitBtn.setFocusableInTouchMode(false);
                        }
                        balanceTv.setText(String.format(getString(R.string.pay_card_balance_title_label), entity.getBalance()));
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
                break;
        }
        return false;
    }

    @Override
    public void onPause() {
        if (accountsBalanceSub != null && accountsBalanceSub.isUnsubscribed()) {
            accountsBalanceSub.unsubscribe();
        }

        if (apiOrderCreateSub != null && apiOrderCreateSub.isUnsubscribed()) {
            apiOrderCreateSub.unsubscribe();
        }

        super.onPause();
    }
}
