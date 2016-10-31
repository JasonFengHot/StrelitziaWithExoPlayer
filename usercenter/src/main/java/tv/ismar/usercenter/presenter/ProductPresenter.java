package tv.ismar.usercenter.presenter;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.YouHuiDingGouEntity;
import tv.ismar.usercenter.ProductContract;
import tv.ismar.usercenter.view.ProductFragment;
import tv.ismar.usercenter.view.UserCenterActivity;

/**
 * Created by huibin on 10/28/16.
 */

public class ProductPresenter implements ProductContract.Presenter {
    private SkyService mSkyService;
    private UserCenterActivity mActivity;
    private ProductFragment mFragment;

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


    @Override
    public void fetchProduct() {
        mSkyService.apiYouhuidinggou()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<YouHuiDingGouEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(YouHuiDingGouEntity entity) {
                        mFragment.loadProductItem(entity);
                    }
                });

    }
}
