package tv.ismar.pay;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.AccountBalanceEntity;

/**
 * Created by huibin on 9/13/16.
 */
public class PaymentActivity extends BaseActivity implements View.OnClickListener {
    private Fragment loginFragment;
    private Fragment weixinFragment;
    private Fragment alipayFragment;
    private Fragment cardpayFragment;
    private Fragment balanceFragment;

    private Button weixinPayBtn;
    private Button aliPayBtn;
    private Button cardPayBtn;
    private Button balancePayBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        weixinPayBtn = (Button) findViewById(R.id.weixin);
        aliPayBtn = (Button) findViewById(R.id.alipay);
        cardPayBtn = (Button) findViewById(R.id.videocard);
        balancePayBtn = (Button) findViewById(R.id.balance_pay);

        weixinPayBtn.setOnClickListener(this);
        aliPayBtn.setOnClickListener(this);
        cardPayBtn.setOnClickListener(this);
        balancePayBtn.setOnClickListener(this);

        loginFragment = new LoginFragment();
        weixinFragment = new WeixinPayFragment();
        alipayFragment = new AlipayFragment();
        cardpayFragment = new CardPayFragment();
        balanceFragment = new BalancePayFragment();

        if (TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken())) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_page, loginFragment)
                    .commit();
        } else {
            fetchAccountBalance();
        }

    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
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

    private void fetchAccountBalance() {
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
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_page, weixinFragment)
                                    .commit();
                        } else {
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_page, balanceFragment)
                                    .commit();
                        }
                    }
                });
    }


}
