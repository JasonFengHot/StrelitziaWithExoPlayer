package tv.ismar.usercenter.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.YouHuiDingGouEntity;
import tv.ismar.usercenter.ProductContract;
import tv.ismar.usercenter.view.ProductFragment;
import tv.ismar.usercenter.view.UserCenterActivity;

/** Created by huibin on 10/28/16. */
public class ProductPresenter implements ProductContract.Presenter {
    private SkyService mSkyService;
    private UserCenterActivity mActivity;
    private ProductFragment mFragment;

    private Subscription youhuidinggouSubscription;

    public ProductPresenter(ProductFragment productFragment) {
        productFragment.setPresenter(this);
        mFragment = productFragment;
    }

    @Override
    public void start() {
        mActivity = (UserCenterActivity) mFragment.getActivity();
        mSkyService = mActivity.mSkyService;
        fetchProduct();
    }

    public void stop() {
        if (youhuidinggouSubscription != null && youhuidinggouSubscription.isUnsubscribed()) {
            youhuidinggouSubscription.unsubscribe();
            youhuidinggouSubscription = null;
        }
    }

    @Override
    public void fetchProduct() {
        youhuidinggouSubscription =
                mSkyService
                        .apiYouhuidinggou()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                mActivity.new BaseObserver<YouHuiDingGouEntity>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onNext(YouHuiDingGouEntity entity) {
                                        mFragment.loadProductItem(entity);
                                    }
                                });
    }
}
