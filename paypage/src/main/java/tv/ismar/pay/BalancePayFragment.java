package tv.ismar.pay;
import cn.ismartv.turetime.TrueTime;
import cn.ismartv.turetime.TrueTime;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONTokener;

import java.io.IOException;
import java.math.BigDecimal;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 2016/9/14.
 */
public class BalancePayFragment extends Fragment implements View.OnClickListener {

    private View contentView;
    private Button submitBtn;
    private Button cancleBtn;

    private PaymentActivity activity;

    private TextView balanceTv;
    private TextView priceTv;
    private TextView durationTv;


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
        cancleBtn=(Button) contentView.findViewById(R.id.card_balance_cancel);
        cancleBtn.setOnClickListener(this);

        balanceTv = (TextView) contentView.findViewById(R.id.card_balance_title_label);
        priceTv = (TextView) contentView.findViewById(R.id.package_price);
        durationTv = (TextView) contentView.findViewById(R.id.package_exprice_label);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchAccountBalance();

        ItemEntity itemEntity = activity.getmItemEntity();
        priceTv.setText(String.format(getString(R.string.pay_package_price),
                itemEntity.getExpense().getPrice(), itemEntity.getExpense().getDuration()));
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.card_balance_submit) {
            createOrder();
        } else if (i == R.id.card_balance_cancel) {
            activity.finish();
        }
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

        activity.mSkyService.apiOrderCreate(waresId, waresType, source, timestamp, sign)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(activity, "支付失败!", Toast.LENGTH_LONG).show();
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
                        balanceTv.setText(String.format(getString(R.string.pay_card_balance_title_label), result));
                        activity.setResult(PaymentActivity.PAYMENT_SUCCESS_CODE);
                        activity.finish();
                    }
                });

    }

    private void fetchAccountBalance() {
        activity.mSkyService.accountsBalance()
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
                        balanceTv.setText(String.format(getString(R.string.pay_card_balance_title_label), entity.getBalance()));
                    }
                });
    }

}
