package tv.ismar.usercenter.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.entity.WeatherEntity;
import tv.ismar.usercenter.BR;
import tv.ismar.usercenter.presenter.LocationPresenter;

/** Created by huibin on 10/28/16. */
public class LocationViewModel extends BaseObservable {
    private Context mContext;
    private LocationPresenter mLocationPresenter;
    private WeatherEntity weatherEntity;

    private String selectedCity;

    public LocationViewModel(Context applicationContext, LocationPresenter locationPresenter) {
        mContext = applicationContext;
        mLocationPresenter = locationPresenter;
    }

    @BindingAdapter({"weatherIcon"})
    public static void loadImage(ImageView view, String imageUrl) {
        Picasso.with(view.getContext()).load(imageUrl).into(view);
    }

    public void loadWeather(WeatherEntity weatherEntity) {
        this.weatherEntity = weatherEntity;
        notifyPropertyChanged(BR.currentCity);
        notifyPropertyChanged(BR.todayCondition);
        notifyPropertyChanged(BR.todayTemp);
        notifyPropertyChanged(BR.todayWeatherIcon);
        notifyPropertyChanged(BR.tomorrowCondition);
        notifyPropertyChanged(BR.tomorrowTemp);
        notifyPropertyChanged(BR.tomorrowWeatherIcon);
    }

    @Bindable
    public int getSelectedCityVisibility() {
        return TextUtils.isEmpty(selectedCity) ? View.INVISIBLE : View.VISIBLE;
    }

    public void loadselectedCity() {
        notifyPropertyChanged(BR.selectedCity);
        notifyPropertyChanged(BR.selectedCityVisibility);
    }

    @Bindable
    public String getCurrentCity() {
        return IsmartvActivator.getInstance().getCity().get("city");
    }

    @Bindable
    public String getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(String selectedCity) {
        this.selectedCity = selectedCity;
    }

    @Bindable
    public String getTodayTemp() {
        try {
            if (weatherEntity
                    .getToday()
                    .getTemplow()
                    .equals(weatherEntity.getToday().getTemphigh())) {
                return weatherEntity.getToday().getTemplow() + "℃ ";
            } else {
                return weatherEntity.getToday().getTemplow()
                        + "℃ ~ "
                        + weatherEntity.getToday().getTemphigh()
                        + "℃";
            }
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Bindable
    public String getTomorrowTemp() {
        try {
            if (weatherEntity
                    .getTomorrow()
                    .getTemplow()
                    .equals(weatherEntity.getTomorrow().getTemphigh())) {
                return weatherEntity.getTomorrow().getTemplow() + "℃ ";
            } else {
                return weatherEntity.getTomorrow().getTemplow()
                        + "℃ ~ "
                        + weatherEntity.getTomorrow().getTemphigh()
                        + "℃";
            }
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Bindable
    public String getTodayCondition() {
        try {
            return weatherEntity.getToday().getCondition();
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Bindable
    public String getTomorrowCondition() {
        try {
            return weatherEntity.getTomorrow().getCondition();
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Bindable
    public String getTodayWeatherIcon() {
        try {
            return weatherEntity.getToday().getImage_url();
        } catch (NullPointerException e) {
            return "url";
        }
    }

    @Bindable
    public String getTomorrowWeatherIcon() {
        try {
            return weatherEntity.getTomorrow().getImage_url();
        } catch (NullPointerException e) {
            return "url";
        }
    }
}
