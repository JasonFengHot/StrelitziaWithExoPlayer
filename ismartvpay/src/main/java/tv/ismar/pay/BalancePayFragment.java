package tv.ismar.pay;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;

/**
 * Created by huibin on 2016/9/14.
 */
public class BalancePayFragment extends Fragment implements View.OnClickListener {

    private View contentView;
    private Button submitBtn;

    private PaymentActivity activity;


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
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.card_balance_submit) {
            createOrder();
        } else if (i == R.id.card_balance_cancel) {

        }
    }


    public void createOrder() {
        String waresId = String.valueOf(activity.getPk());
        String waresType = activity.getModel();
        String source = "sky";
        String timestamp = null;
        String sign = null;
        {
            timestamp = System.currentTimeMillis() + "";
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

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                    }
                });

    }
}
