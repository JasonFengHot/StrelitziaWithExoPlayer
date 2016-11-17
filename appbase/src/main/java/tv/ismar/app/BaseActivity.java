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

import rx.Observer;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static tv.ismar.app.update.UpdateService.APP_UPDATE_ACTION;

/**
 * Created by beaver on 16-8-19.
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private PopupWindow updatePopupWindow;
    private LoadingDialog mLoadingDialog;
    private ModuleMessagePopWindow netErrorPopWindow;
    public SkyService mSkyService;
    private View mRootView;
    public SkyService mWeatherSkyService;
    public static final String ACTION_CONNECT_ERROR = "tv.ismar.daisy.CONNECT_ERROR";
    protected String activityTag = "";
    public long app_start_time;

    public static Stack<Bundle> updateInfo = new Stack<>();

    private static final int UPDATE_APP_REQUEST_CODE = 0x5723;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSkyService = SkyService.ServiceManager.getService();
        mRootView = getWindow().getDecorView();
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
                    showUpdatePopup(mRootView);
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
        netErrorPopWindow = new ModuleMessagePopWindow(this);
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
            showNetWorkErrorDialog(e);
        }
    }


    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d("UpdateReceiver", intent.getBundleExtra("data").toString());
            Bundle bundle = intent.getBundleExtra("data");
            updateInfo.push(bundle);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (updatePopupWindow == null || !updatePopupWindow.isShowing()) {
                        showUpdatePopup(mRootView);
                    }
                }
            }, 2000);
        }
    };


    private void registerUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(APP_UPDATE_ACTION);
        registerReceiver(mUpdateReceiver, intentFilter);
    }


    private void showUpdatePopup(final View view) {
        if (updateInfo.isEmpty()) {
            return;
        }
        Bundle bundle = updateInfo.pop();
        final Context context = this;
        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_update, null);
        contentView.setBackgroundResource(R.drawable.app_update_bg);
        float density = getResources().getDisplayMetrics().density;

        int appUpdateHeight = (int) (getResources().getDimension(R.dimen.app_update_bg_height));
        int appUpdateWidht = (int) (getResources().getDimension(R.dimen.app_update_bg_width));


        updatePopupWindow = new PopupWindow(null, appUpdateHeight, appUpdateWidht);
        updatePopupWindow.setContentView(contentView);
        updatePopupWindow.setOutsideTouchable(true);
//        updatePopupWindow.setFocusable(true);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.15f;
        getWindow().setAttributes(params);


        updatePopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        updatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (updatePopupWindow == null || !updatePopupWindow.isShowing()) {
                            showUpdatePopup(mRootView);
                        }
                    }
                }, 2000);
            }
        });

        Button updateNow = (Button) contentView.findViewById(R.id.update_now_bt);
        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);

        final String path = bundle.getString("path");

        ArrayList<String> msgs = bundle.getStringArrayList("msgs");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) (getResources().getDimension(R.dimen.app_update_content_margin_left));
        layoutParams.topMargin = (int) (getResources().getDimension(R.dimen.app_update_line_margin_));

        for (String msg : msgs) {
            View textLayout = LayoutInflater.from(this).inflate(R.layout.update_msg_text_item, null);
            TextView textView = (TextView) textLayout.findViewById(R.id.update_msg_text);
            textView.setText(msg);
            updateMsgLayout.addView(textLayout);
        }

        updateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePopupWindow.dismiss();
                AppUtils.installApp(BaseActivity.this, path, UPDATE_APP_REQUEST_CODE);
            }
        });
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_APP_REQUEST_CODE) {
            Log.d(TAG, "result code: " + resultCode);
        }
    }
}
