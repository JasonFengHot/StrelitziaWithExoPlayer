package tv.ismar.pay;
import cn.ismartv.truetime.TrueTime;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.ismartv.truetime.TrueTime;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.PayVerifyEntity;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by huibin on 2016/9/14.
 */
public class CardPayFragment extends Fragment implements View.OnClickListener, OnHoverListener {

    private View contentView;
    private SkyService skyService;
    private Button submitBtn;
    private EditText cardNumberEdt;
    private PaymentActivity mPaymentActivity;

    private String flag;
    private TextView rechargeMsgTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            flag = bundle.getString("flag");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPaymentActivity = (PaymentActivity) activity;
        skyService = ((PaymentActivity) activity).mSkyService;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragmet_cardpay, null);
        submitBtn = (Button) contentView.findViewById(R.id.shiyuncard_submit);
        cardNumberEdt = (EditText) contentView.findViewById(R.id.shiyuncard_input);
        submitBtn.setOnClickListener(this);
        rechargeMsgTextView = (TextView)contentView.findViewById(R.id.recharge_error_msg);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View indicatorView = mPaymentActivity.findViewById(R.id.videocard);
        indicatorView.setNextFocusRightId(R.id.shiyuncard_input);
        submitBtn.setOnHoverListener(this);
        if (!TextUtils.isEmpty(flag)&& "usercenter_charge".equals(flag)){
            cardNumberEdt.requestFocus();
        }
    }

    private void cardRecharge(String cardNumber) {
        String pwd_prefix = cardNumber.substring(0, 10);
        String sur_prefix = cardNumber.substring(10, 16);
        String timestamp = TrueTime.now().getTime() + "";
        String sid = "sid";
        String user = IsmartvActivator.getInstance().getUsername();
        String user_id = "0";
        String app_name = "sky";
        String card_secret = null;
        try {
            card_secret = pwd_prefix + SHA1(user + sur_prefix + timestamp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        skyService.apiPayVerify(card_secret, app_name, user, user_id, timestamp, sid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PayVerifyEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        rechargeMsgTextView.setText("充值失败");
                        rechargeMsgTextView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(PayVerifyEntity entity) {
                        switch (entity.getStatus()) {
                            //充值成功,系统将自动为您购买,6s后返回
                            case "S":
                                rechargeMsgTextView.setText("充值成功");
                                rechargeMsgTextView.setVisibility(View.VISIBLE);
                                break;
                            //充值成功,系统将在第二天8点为您购买,10s后返回
                            case "T":
                                rechargeMsgTextView.setText("充值成功");
                                rechargeMsgTextView.setVisibility(View.VISIBLE);
                                break;
                            default:
                                rechargeMsgTextView.setText("充值失败");
                                rechargeMsgTextView.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });
    }


    private String SHA1(String text) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.shiyuncard_submit) {
            String cardNumber = cardNumberEdt.getText().toString();
            if (cardNumber.length() == 16 && TextUtils.isDigitsOnly(cardNumber)) {
                cardRecharge(cardNumber);
            } else {
                rechargeMsgTextView.setText("错误的观影卡密码");
                rechargeMsgTextView.setVisibility(View.VISIBLE);
            }
        }
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
}
