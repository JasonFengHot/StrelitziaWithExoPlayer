package tv.ismar.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.utils.AppUtils;

import tv.ismar.account.ActiveService;
import tv.ismar.app.update.UpdateService;

import static tv.ismar.app.update.UpdateService.INSTALL_SILENT;

/**
 * Created by huibin on 12/24/2016.
 */

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectivityChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
            Log.i(TAG, "netWork has lost");
        } else {
            Log.i(TAG, "netWork has connect");
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String apiDomain = mSharedPreferences.getString("api_domain", "");

            if (TextUtils.isEmpty(apiDomain)){
                //更新会去激活
            }else {
                startIntervalActive(context);
            }

            if (BootUpdateReceiver.checkUpdate = true) {
                BootUpdateReceiver.checkUpdate = false;
                checkUpdate(context);
            }


        }

        NetworkInfo tmpInfo = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
        Log.i(TAG, tmpInfo.toString() + " {isConnected = " + tmpInfo.isConnected() + "}");
    }


    private void checkUpdate(final Context context) {
        Log.d(TAG, "onReceive Update App");
        Log.d(TAG, "AppUtils appInfo: " + AppUtils.isSystemApp(context));
        Log.d(TAG, "AppUtils getAppVersionCode: " + AppUtils.getAppVersionCode(context));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent updateIntent = new Intent();
                updateIntent.setClass(context, UpdateService.class);
                updateIntent.putExtra("install_type", INSTALL_SILENT);
                context.startService(updateIntent);
            }
        }, 1000 * 3);
    }


    private void startIntervalActive(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, ActiveService.class);
        context.startService(intent);
    }

}
