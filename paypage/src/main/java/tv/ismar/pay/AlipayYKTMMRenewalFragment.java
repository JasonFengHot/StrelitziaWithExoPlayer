package tv.ismar.pay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ChoosewayEntity;
import tv.ismar.app.network.entity.OpenRenewEntity;

import static tv.ismar.pay.PaymentActivity.OderType.alipay_renewal;

/**
 * Created by huibin on 17-2-16.
 */

public class AlipayYKTMMRenewalFragment extends Fragment implements View.OnClickListener ,OnHoverListener{
    private TextView agreementTextView;

    private Button confirmBtn;
    private Button cancelBtn;

    private SkyService mSkyService;
    private PaymentActivity mActivity;
    private TextView priceLineTextView;

    private String url;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = ((PaymentActivity) activity);
        mSkyService = mActivity.mSkyService;
    }


    @Override
    public void onResume() {
        super.onResume();
        mActivity.purchaseCheck(PaymentActivity.CheckType.OrderPurchase);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        url = bundle.getString("url");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alipayyktmmrenewal, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        confirmBtn = (Button) view.findViewById(R.id.confirm);
        cancelBtn = (Button) view.findViewById(R.id.cancel);
        priceLineTextView = (TextView)view.findViewById(R.id.description_line_2);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnHoverListener(this);
        cancelBtn.setOnHoverListener(this);

        priceLineTextView.setText(String.format(getString(R.string.yktmm_price) ,
                mActivity.getmItemEntity().getExpense().getRenew_price(),
                mActivity.getmItemEntity().getExpense().getNominal_price() ));

        agreementTextView = (TextView) view.findViewById(R.id.agreement);
        agreementTextView.setText(Html.fromHtml("<u>《视云连续扣费协议》</u>"));
        agreementTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.confirm) {
            openRenewal(url);

        } else if (i == R.id.cancel) {
            mActivity.aliPayBtn.requestFocus();
            mActivity.changeNormalAlipay();
        } else if (i == R.id.agreement) {
            mActivity.aliPayBtn.requestFocus();
            Intent intent = new Intent();
            intent.setClass(getContext(), RenewalAgreementActivity.class);
            startActivity(intent);
        }

    }

    private void openRenewal(String url) {
        mSkyService.openRenew(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });

    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                mActivity.tmp.requestFocus();
                break;
        }
        return false;
    }
}
