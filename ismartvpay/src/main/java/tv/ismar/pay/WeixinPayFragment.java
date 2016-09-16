package tv.ismar.pay;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by huibin on 2016/9/14.
 */
public class WeixinPayFragment extends Fragment implements PaymentActivity.QrcodeCallback {

    private View contentView;
    private ImageView qrcodeview;

    private PaymentActivity paymentActivity;

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
        contentView = inflater.inflate(R.layout.fragmet_weixinpay, null);
        qrcodeview = (ImageView) contentView.findViewById(R.id.qrcodeview);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        paymentActivity.createOrder(PaymentActivity.OderType.weixin, this);

    }

    @Override
    public void onBitmap(Bitmap bitmap) {
        qrcodeview.setImageBitmap(bitmap);
    }
}
