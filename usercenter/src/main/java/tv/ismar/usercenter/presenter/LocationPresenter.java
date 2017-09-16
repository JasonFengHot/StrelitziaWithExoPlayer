package tv.ismar.usercenter.presenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.WeatherEntity;
import tv.ismar.usercenter.LocationContract;
import tv.ismar.usercenter.view.LocationFragment;
import tv.ismar.usercenter.view.UserCenterActivity;

/** Created by huibin on 10/28/16. */
public class LocationPresenter implements LocationContract.Presenter {
    private LocationFragment mFragment;
    private UserCenterActivity mActivity;
    private SkyService mSkyService;
    private Subscription weatherSub;

    public LocationPresenter(LocationFragment locationFragment) {
        locationFragment.setPresenter(this);
        mFragment = locationFragment;
    }

    @Override
    public void start() {
        mActivity = (UserCenterActivity) mFragment.getActivity();
        mSkyService = mActivity.mWeatherSkyService;
    }

    @Override
    public void stop() {
        if (weatherSub != null && weatherSub.isUnsubscribed()) {
            weatherSub.unsubscribe();
        }
    }

    @Override
    public void fetchWeather(String geoId) {
        weatherSub =
                mSkyService
                        .apifetchWeatherInfo(geoId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Observer<WeatherEntity>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }

                                    @Override
                                    public void onNext(WeatherEntity weatherEntity) {
                                        parseXml(weatherEntity);
                                    }
                                });
    }

    private void parseXml(WeatherEntity weatherEntity) {
        mFragment.refreshWeather(weatherEntity);
    }
}
