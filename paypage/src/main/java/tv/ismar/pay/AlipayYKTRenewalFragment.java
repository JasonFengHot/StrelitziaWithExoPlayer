package tv.ismar.pay;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by huibin on 17-2-16.
 */

public class AlipayYKTRenewalFragment extends Fragment{

    private TextView yktRenewalPriceText;
    private TextView yktRenewalDurationText;

    private PaymentActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (PaymentActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_alipayyktrenewal, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        yktRenewalPriceText = (TextView) view.findViewById(R.id.description_line_1);
        yktRenewalDurationText = (TextView) view.findViewById(R.id.description_line_2);

        yktRenewalPriceText.setText(String.format(getString(R.string.ykt_renewal_price), mActivity.getmItemEntity().getExpense().getRenew_price()));
        yktRenewalDurationText.setText(String.format(getString(R.string.ykt_renewal_duration), mActivity.getmItemEntity().getExpense().getDuration()));
    }

}
