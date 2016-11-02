package tv.ismar.usercenter.presenter;

import tv.ismar.usercenter.LocationContract;
import tv.ismar.usercenter.view.LocationFragment;
import tv.ismar.usercenter.view.UserCenterActivity;

/**
 * Created by huibin on 10/28/16.
 */

public class LocationPresenter implements LocationContract.Presenter {
    private LocationFragment mFragment;
    private UserCenterActivity mActivity;

    public LocationPresenter(LocationFragment locationFragment) {
        locationFragment.setPresenter(this);
        mFragment = locationFragment;

    }

    @Override
    public void start() {
        mActivity = (UserCenterActivity) mFragment.getActivity();
    }

    @Override
    public void fetchWeather(String geoId) {

    }
}
