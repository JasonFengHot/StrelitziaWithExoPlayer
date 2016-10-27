package tv.ismar.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import tv.ismar.app.update.UpdateService;

import static tv.ismar.app.update.UpdateService.INSTALL_SILENT;

/**
 * Created by huibin on 10/25/16.
 */

public class BootUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = BootUpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive Update App");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent updateIntent = new Intent();
                updateIntent.setClass(context, UpdateService.class);
                updateIntent.putExtra("install_type", INSTALL_SILENT);
                context.startService(updateIntent);
            }
        }, 1000 * 60);

    }
}
