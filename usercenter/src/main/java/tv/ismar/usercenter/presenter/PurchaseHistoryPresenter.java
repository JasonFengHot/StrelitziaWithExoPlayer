package tv.ismar.usercenter.presenter;

import cn.ismartv.truetime.TrueTime;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AccountsOrdersEntity;
import tv.ismar.usercenter.PurchaseHistoryContract;
import tv.ismar.usercenter.view.PurchaseHistoryFragment;
import tv.ismar.usercenter.view.UserCenterActivity;

/** Created by huibin on 10/28/16. */
public class PurchaseHistoryPresenter implements PurchaseHistoryContract.Presenter {
    private PurchaseHistoryFragment mFragment;

    private UserCenterActivity mActivity;
    private SkyService mSkyService;
    private Subscription accountsOrdersSub;

    public PurchaseHistoryPresenter(PurchaseHistoryFragment purchaseHistoryFragment) {
        purchaseHistoryFragment.setPresenter(this);
        mFragment = purchaseHistoryFragment;
    }

    @Override
    public void start() {
        mActivity = (UserCenterActivity) mFragment.getActivity();
        mSkyService = mActivity.mSkyService;
        fetchAccountsOrders();
    }

    @Override
    public void stop() {
        if (accountsOrdersSub != null && accountsOrdersSub.isUnsubscribed()) {
            accountsOrdersSub.unsubscribe();
        }
    }

    @Override
    public void fetchAccountsOrders() {
        String timestamp = String.valueOf(TrueTime.now().getTime());
        IsmartvActivator activator = IsmartvActivator.getInstance();
        String sign =
                activator.encryptWithPublic(
                        "sn=" + activator.getSnToken() + "&timestamp=" + timestamp);

        accountsOrdersSub =
                mSkyService
                        .accountsOrders(timestamp, sign)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                mActivity.new BaseObserver<AccountsOrdersEntity>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onNext(AccountsOrdersEntity accountsOrdersEntity) {
                                        mFragment.loadAccountOrders(accountsOrdersEntity);
                                    }
                                });
    }
}
