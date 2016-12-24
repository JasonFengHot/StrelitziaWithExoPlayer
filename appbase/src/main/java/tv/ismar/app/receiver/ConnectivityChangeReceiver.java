package tv.ismar.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.utils.AppUtils;

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
}
