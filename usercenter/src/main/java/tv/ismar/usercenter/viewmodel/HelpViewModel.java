package tv.ismar.usercenter.viewmodel;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import tv.ismar.usercenter.presenter.HelpPresenter;

/**
 * Created by huibin on 10/28/16.
 */

public class HelpViewModel extends BaseObservable {
    private Context mContext;

    public HelpViewModel(Context applicationContext, HelpPresenter helpPresenter) {
        mContext = applicationContext;

    }

    @Bindable
    public String getVersionCode() {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return "版本号：" + packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
