package tv.ismar.app;
import cn.ismartv.truetime.TrueTime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.Stack;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observer;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.update.UpdateService;
import tv.ismar.app.util.NetworkUtils;
import tv.ismar.app.widget.ExpireAccessTokenPop;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.NetErrorPopWindow;
import tv.ismar.app.widget.NoNetConnectWindow;
import tv.ismar.app.widget.NoNetModuleMessagePop;
import tv.ismar.app.widget.UpdatePopupWindow;
import tv.ismar.player.SmartPlayer;
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
    private NoNetModuleMessagePop noNetConnectWindow;
    public SkyService mSkyService;
    public SkyService mWeatherSkyService;
    public SkyService mWxApiService;
    public SkyService mIrisService;
    public SkyService mSpeedCallaService;
    public SkyService mLilyHostService;
    public long app_start_time;
    public static final String NO_NET_CONNECT_ACTION = "cn.ismartv.vod.action.nonet";
    //    public static SmartPlayer mSmartPlayer;// 由于目前需要在详情页实现预加载功能，故写此变量
    public static String brandName;

    public static Stack<Bundle> updateInfo = new Stack<>();

    private boolean activityIsAlive = false;

    public static boolean isCheckoutUpdate = true;

    public int totalAdsMills;

    private Handler updateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSkyService = SkyService.ServiceManager.getService();
        mWeatherSkyService = SkyService.ServiceManager.getWeatherService();
        mWxApiService = SkyService.ServiceManager.getWxApiService();
        mIrisService = SkyService.ServiceManager.getIrisService();
        mSpeedCallaService = SkyService.ServiceManager.getSpeedCallaService();
        mLilyHostService = SkyService.ServiceManager.getLilyHostService();
        app_start_time = TrueTime.now().getTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        registerUpdateReceiver();
        registerNoNetReceiver();
        activityIsAlive = true;

        //checkout update
        if (isCheckoutUpdate) {
            checkUpgrade();
            isCheckoutUpdate = false;
        }
    }

    @Override
    protected void onPause() {
        activityIsAlive = false;
        if (expireAccessTokenPop != null) {
            expireAccessTokenPop.dismiss();
            expireAccessTokenPop = null;
        }
        unregisterReceiver(mUpdateReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (updatePopupWindow != null) {
            updatePopupWindow.dismiss();
            updatePopupWindow = null;
        }
        super.onStop();
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
        netErrorPopWindow.setCancelBtn(getString(R.string.back));
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

    public void showNoNetConnectDialog() {
        noNetConnectWindow = NoNetConnectWindow.getInstance(this);
        noNetConnectWindow.setFirstMessage(getString(R.string.no_connectNet));
        noNetConnectWindow.setConfirmBtn(getString(R.string.setting_network));
        noNetConnectWindow.setCancelBtn(getString(R.string.exit_app));
        noNetConnectWindow.showAtLocation(getRootView(), Gravity.CENTER, 0, 0, new ModuleMessagePopWindow.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        noNetConnectWindow.dismiss();
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);

                    }
                },
                new ModuleMessagePopWindow.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        noNetConnectWindow.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(NO_NET_CONNECT_ACTION);
                        sendBroadcast(intent);
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
            Log.i("onNoNet", "onerror" + NetworkUtils.isConnected(BaseActivity.this));
            if (!NetworkUtils.isConnected(BaseActivity.this) && !NetworkUtils.isWifi(BaseActivity.this)) {
                Log.i("onNoNet", "" + NetworkUtils.isConnected(BaseActivity.this));
                showNoNetConnectDialog();
            } else if (e instanceof HttpException) {
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
                            if (activityIsAlive) {
                                showUpdatePopup(getRootView(), updateInfo);
                            }
                        }
                    }
                }, 2000);
            }
        }
    };
    private BroadcastReceiver onNetConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private void registerNoNetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NO_NET_CONNECT_ACTION);
        registerReceiver(onNetConnectReceiver, intentFilter);
    }


    private void registerUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(APP_UPDATE_ACTION);
        registerReceiver(mUpdateReceiver, intentFilter);
    }

    private void showUpdatePopup(final View view, final Stack<Bundle> stack) {
        if (!stack.isEmpty()) {
            updatePopupWindow = new UpdatePopupWindow(this, stack.pop());
            updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            updatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    showUpdatePopup(view, stack);
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        if (updateHandler!=null){
            updateHandler.removeCallbacks(updateRunnable);
        }
        unregisterReceiver(onNetConnectReceiver);
        super.onDestroy();
    }

    private void checkUpgrade() {
        updateHandler = new Handler();
        updateHandler.postDelayed(updateRunnable, (1000 * 3) + totalAdsMills);
    }

    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), UpdateService.class);
            intent.putExtra("install_type", 0);
            startService(intent);
        }
    };
}
