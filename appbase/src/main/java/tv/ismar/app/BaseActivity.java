package tv.ismar.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.utils.AppUtils;

import java.util.ArrayList;
import java.util.Stack;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observer;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.models.SemantichObjectEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.widget.ExpireAccessTokenPop;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.NetErrorPopWindow;
import tv.ismar.app.widget.UpdatePopupWindow;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static tv.ismar.app.update.UpdateService.APP_UPDATE_ACTION;

/**
 * Created by beaver on 16-8-19.
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private UpdatePopupWindow updatePopupWindow;
    private LoadingDialog mLoadingDialog;
    private ModuleMessagePopWindow netErrorPopWindow;
    private ModuleMessagePopWindow expireAccessTokenPop;
    public SkyService mSkyService;
    public SkyService mWeatherSkyService;
    protected String activityTag = "";
    public long app_start_time;

    public static Stack<Bundle> updateInfo = new Stack<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSkyService = SkyService.ServiceManager.getService();
        mWeatherSkyService = SkyService.ServiceManager.getWeatherService();
        app_start_time = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        registerUpdateReceiver();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (updatePopupWindow == null || !updatePopupWindow.isShowing()) {
                  //  showUpdatePopup(mRootView);
                }
            }
        }, 2000);

    }

    @Override
    protected void onPause() {
        if (updatePopupWindow != null) {
            updatePopupWindow.dismiss();
            updatePopupWindow = null;
        }

        if (expireAccessTokenPop != null) {
            expireAccessTokenPop.dismiss();
            expireAccessTokenPop = null;
        }
        unregisterReceiver(mUpdateReceiver);
        super.onPause();
    }

    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void showDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        }
        if (msg != null) {
            mLoadingDialog.setTvText(msg);
        }
        mLoadingDialog.showDialog();
    }

    public void dismissDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog.setTvText(getString(R.string.loading_text));
        }
    }

    public boolean isDialogShow() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return true;
        }
        return false;
    }

    public View getRootView() {
        return ((ViewGroup) (getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
    }

    public void showNetWorkErrorDialog(Throwable e) {
        netErrorPopWindow = NetErrorPopWindow.getInstance(this);
        netErrorPopWindow.setFirstMessage(getString(R.string.fetch_net_data_error));
        netErrorPopWindow.setConfirmBtn(getString(R.string.setting_network));
        netErrorPopWindow.setCancelBtn(getString(R.string.i_know));
        netErrorPopWindow.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        netErrorPopWindow.dismiss();
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);

                    }
                },
                new ModuleMessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        netErrorPopWindow.dismiss();
                    }
                });
    }

    public boolean isshowNetWorkErrorDialog() {
        return netErrorPopWindow != null && netErrorPopWindow.isShowing();
    }

    public abstract class BaseObserver<T> implements Observer<T> {
        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            if (e instanceof HttpException) {
                HttpException httpException = (HttpException) e;
                if (httpException.code() == 401) {
                    showExpireAccessTokenPop();
                } else {
                    showNetWorkErrorDialog(e);
                }
            } else {
                showNetWorkErrorDialog(e);
            }
        }
    }


    public void showExpireAccessTokenPop() {

        expireAccessTokenPop = ExpireAccessTokenPop.getInstance(this);
        expireAccessTokenPop.setFirstMessage(getString(R.string.access_token_expire));
        expireAccessTokenPop.setConfirmBtn(getString(R.string.confirm));
        expireAccessTokenPop.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        expireAccessTokenPop.dismiss();
                        IsmartvActivator.getInstance().removeUserInfo();
                    }
                },
                null);
    }

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d("UpdateReceiver", intent.getBundleExtra("data").toString());
            Bundle bundle = intent.getBundleExtra("data");

            boolean isExsit = false;
            for (Bundle b : updateInfo) {
                if (b.get("path").equals(bundle.get("path")) &&
                        b.get("msgs").equals(bundle.get("msgs"))) {
                    isExsit = true;

                }
            }

            if (!isExsit) {
                updateInfo.push(bundle);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (updatePopupWindow == null || !updatePopupWindow.isShowing()) {
                         //   showUpdatePopup(getRootView(), updateInfo);
                        }
                    }
                }, 2000);
            }
        }
    };


    private void registerUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(APP_UPDATE_ACTION);
        registerReceiver(mUpdateReceiver, intentFilter);
    }

    private void showUpdatePopup(final View view, final Stack<Bundle> stack) {
        if (!stack.isEmpty()) {
            Log.d(TAG, "showUpdatePopup");
            updatePopupWindow = new UpdatePopupWindow(this, stack.pop());
            updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            updatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                  //  showUpdatePopup(view, stack);
                }
            });
        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
