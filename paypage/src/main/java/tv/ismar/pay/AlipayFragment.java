package tv.ismar.pay;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 2016/9/14.
 */
public class AlipayFragment extends Fragment implements PaymentActivity.QrcodeCallback {

    private View contentView;

    private ImageView qrcodeview;

    private PaymentActivity paymentActivity;
    private TextView priceTv;
    private TextView expireTv;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        paymentActivity = (PaymentActivity) activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragmet_alipay, null);
        qrcodeview = (ImageView) contentView.findViewById(R.id.qrcodeview);
        priceTv = (TextView) contentView.findViewById(R.id.payinfo_price);
        expireTv = (TextView) contentView.findViewById(R.id.payinfo_exprice);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        paymentActivity.createOrder(PaymentActivity.OderType.alipay, this);
        ItemEntity entity = paymentActivity.getmItemEntity();
        priceTv.setText(String.format(getString(R.string.pay_payinfo_price_label), entity.getExpense().price));
        expireTv.setText(String.format(getString(R.string.pay_payinfo_exprice_label), entity.getExpense().duration));
    }

    @Override
    public void onResume() {
        super.onResume();
        qrcodeview.setVisibility(View.INVISIBLE);
        paymentActivity.purchaseCheck(PaymentActivity.CheckType.OrderPurchase);

    }

    @Override
    public void onBitmap(Bitmap bitmap) {
        qrcodeview.setImageBitmap(bitmap);
        qrcodeview.setVisibility(View.VISIBLE);
    }
}
