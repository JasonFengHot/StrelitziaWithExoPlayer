package tv.ismar.usercenter.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.usercenter.BR;
import tv.ismar.usercenter.presenter.UserInfoPresenter;

/**
 * Created by huibin on 10/28/16.
 */

public class UserInfoViewModel extends BaseObservable {

    private Context mContext;
    private UserInfoPresenter mPresenter;

    public UserInfoViewModel(Context applicationContext, UserInfoPresenter presenter) {
        mContext = applicationContext;
        mPresenter = presenter;
    }


    public void refresh() {
        notifyPropertyChanged(BR.balance);
    }

    @Bindable
    public String getSnCode() {
        return IsmartvActivator.getInstance().getSnToken();
    }

    @Bindable
    public String getDeviceName() {
        return Build.PRODUCT.replace(" ", "_");
    }


    @Bindable
    public String getBalance() {
        BigDecimal balance = mPresenter.getBalance();
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(balance);
    }

    @Bindable
    public int getUsernameVisibility() {
        return TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername()) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public String getUsername() {
        return IsmartvActivator.getInstance().getUsername();
    }
}
