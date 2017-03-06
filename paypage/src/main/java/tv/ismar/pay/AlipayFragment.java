package tv.ismar.pay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 2016/9/14.
 */
public class AlipayFragment extends Fragment implements PaymentActivity.QrcodeCallback, View.OnClickListener, View.OnHoverListener {
    private View contentView;
    private ImageView qrcodeview;
    private PaymentActivity paymentActivity;
    private String type;
    private TextView agreementTextView;
    private TextView textViewLine1;
    private TextView textViewLine2;
    private TextView textViewLine3;
    private TextView textViewLine4;
    private Button changeBtn;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        paymentActivity = (PaymentActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        type = bundle.getString("type");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragmet_alipay, null);
        qrcodeview = (ImageView) contentView.findViewById(R.id.qrcodeview);
        textViewLine1 = (TextView) contentView.findViewById(R.id.text_line_1);
        textViewLine2 = (TextView) contentView.findViewById(R.id.text_line_2);
        textViewLine3 = (TextView) contentView.findViewById(R.id.text_line_3);
        textViewLine4 = (TextView) contentView.findViewById(R.id.text_line_4);

        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        agreementTextView = (TextView) view.findViewById(R.id.agreement);
        agreementTextView.setText(Html.fromHtml("<u>《视云连续扣费协议》</u>"));
        changeBtn = (Button) view.findViewById(R.id.change_btn);
        agreementTextView.setOnClickListener(this);
        changeBtn.setOnClickListener(this);
        changeBtn.setOnHoverListener(this);
        switch (type) {
            case "alipay_renewal":
                textViewLine1.setText("·到期前1天为您自动续费，");
                textViewLine2.setText(R.string.alipay_renew_line2_text);
                textViewLine3.setText(String.format(getString(R.string.renew_price_text), paymentActivity.getmItemEntity().getExpense().getRenew_price()));
                textViewLine4.setText(String.format(getString(R.string.renew_original_price_text), paymentActivity.getmItemEntity().getExpense().getNominal_price()));
                changeBtn.setText("切换为普通扫码");
                textViewLine4.setVisibility(View.VISIBLE);
                agreementTextView.setVisibility(View.VISIBLE);
                break;
            case "alipay_normal":
                textViewLine1.setText(String.format(getString(R.string.pay_payinfo_price_label), paymentActivity.getmItemEntity().getExpense().getPrice()));
                textViewLine2.setText(String.format(getString(R.string.pay_payinfo_exprice_label), paymentActivity.getmItemEntity().getExpense().getDuration()));
                textViewLine3.setText(getString(R.string.pay_payinfo_introduce_label));
                changeBtn.setText("返回连续包月");
                textViewLine4.setVisibility(View.INVISIBLE);
                agreementTextView.setVisibility(View.INVISIBLE);
                break;
            default:
                textViewLine1.setText(String.format(getString(R.string.pay_payinfo_price_label), paymentActivity.getmItemEntity().getExpense().getPrice()));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.alipay_pay_layout_mt), 0, 0);
                textViewLine1.setLayoutParams(params);
                textViewLine2.setText(String.format(getString(R.string.pay_payinfo_exprice_label), paymentActivity.getmItemEntity().getExpense().getDuration()));
                textViewLine3.setText(getString(R.string.pay_payinfo_introduce_label));
                textViewLine4.setVisibility(View.INVISIBLE);
                agreementTextView.setVisibility(View.INVISIBLE);
                changeBtn.setVisibility(View.INVISIBLE);
                break;
        }
        qrcodeview.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        paymentActivity.purchaseCheck(PaymentActivity.CheckType.OrderPurchase);
    }

    @Override
    public void onBitmap(Bitmap bitmap) {
        qrcodeview.setImageBitmap(bitmap);
        qrcodeview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.agreement) {
            Intent intent = new Intent(getContext(), RenewalAgreementActivity.class);
            startActivity(intent);
        } else if (i == R.id.change_btn) {
            Button button = (Button) view;
            switch (button.getText().toString()) {
                case "切换为普通扫码":
                    paymentActivity.aliPayBtn.requestFocus();
                    paymentActivity.changeNormalAlipay();
                    break;
                case "返回连续包月":
                    paymentActivity.aliPayBtn.requestFocus();
                    paymentActivity.changeRenewalAlipay();
                    break;
            }
        }
    }

    @Override
    public boolean onHover(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                view.requestFocus();
                view.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                paymentActivity.tmp.requestFocus();
                break;
        }
        return false;
    }
}
