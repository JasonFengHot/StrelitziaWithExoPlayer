package tv.ismar.usercenter.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
        notifyPropertyChanged(BR.balanceVisibility);
        notifyPropertyChanged(BR.username);
        notifyPropertyChanged(BR.usernameVisibility);
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
        return format.format(balance) + "元";
    }

    @Bindable
    public int getBalanceVisibility() {
        return mPresenter.getBalance().setScale(1).equals(new BigDecimal(0).setScale(1)) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public int getUsernameVisibility() {
        return TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername()) ? View.GONE : View.VISIBLE;
    }


    @Bindable
    public String getUsername() {
        return IsmartvActivator.getInstance().getUsername();
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setlayoutMarginTop(View view, float marginTop) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.topMargin = (int) marginTop;
        view.setLayoutParams(layoutParams);
    }
}
