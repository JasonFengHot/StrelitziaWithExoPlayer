package tv.ismar.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tv.ismar.app.update.UpdateService;

import static tv.ismar.app.update.UpdateService.INSTALL_SILENT;

/**
 * Created by huibin on 10/25/16.
 */

public class BootUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = BootUpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent updateIntent = new Intent();
        updateIntent.setClass(context, UpdateService.class);
        updateIntent.putExtra("install_type", INSTALL_SILENT);
        context.startService(updateIntent);
        Log.d(TAG, "onReceive Update App");
    }
}
