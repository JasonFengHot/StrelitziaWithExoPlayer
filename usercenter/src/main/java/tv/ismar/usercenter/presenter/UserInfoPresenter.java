package tv.ismar.usercenter.presenter;

import java.math.BigDecimal;

import cn.ismartv.truetime.TrueTime;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.AccountPlayAuthEntity;
import tv.ismar.usercenter.UserInfoContract;
import tv.ismar.usercenter.view.UserCenterActivity;
import tv.ismar.usercenter.view.UserInfoFragment;

/**
 * Created by huibin on 10/28/16.
 */

public class UserInfoPresenter implements UserInfoContract.Presenter {

    private SkyService mSkyService;
    private UserCenterActivity mActivity;
    private UserInfoFragment mFragment;

    private BigDecimal balance = new BigDecimal(0);

    private AccountPlayAuthEntity mAccountPlayAuthEntity;


    public AccountPlayAuthEntity getAccountPlayAuthEntity() {
        return mAccountPlayAuthEntity;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public UserInfoPresenter(UserInfoFragment userInfoFragment) {
        userInfoFragment.setPresenter(this);
        mFragment = userInfoFragment;

    }

    @Override
    public void start() {
        mActivity = (UserCenterActivity) mFragment.getActivity();
        mSkyService = mActivity.mSkyService;
        fetchBalance();
        fetchPrivilege();
    }

    @Override
    public void stop() {

    }


    @Override
    public void fetchBalance() {
        mSkyService.accountsBalance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<AccountBalanceEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(AccountBalanceEntity accountBalanceEntity) {
                        balance = accountBalanceEntity.getBalance().add(accountBalanceEntity.getSn_balance());
                        mFragment.loadBalance(accountBalanceEntity);
                    }
                });
    }

    @Override
    public void fetchPrivilege() {
        String timestamp = String.valueOf(TrueTime.now().getTime());
        IsmartvActivator activator = IsmartvActivator.getInstance();
        String sign = activator.encryptWithPublic("sn=" + activator.getSnToken() + "&timestamp=" + timestamp);

        mSkyService.accountsPlayauths(timestamp, sign)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<AccountPlayAuthEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(AccountPlayAuthEntity accountPlayAuthEntity) {
                        mAccountPlayAuthEntity = accountPlayAuthEntity;
                        mFragment.loadPrivilege(accountPlayAuthEntity);
                    }
                });
    }

    @Override
    public void exitAccount() {
        mFragment.showExitAccountConfirmPop();
    }

}
