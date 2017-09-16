package tv.ismar.searchpage.core.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/** Created by huaijie on 2/23/16. */
public class LauncherService extends Service {
    private static final String LAUNCHER_ACTIVITY_NAME =
            "cn.ismartv.voice.ui.activity.HomeActivity";
    private static final String TAG = "LauncherService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String currentActivityName = getCurrentActivityName(getApplicationContext());
        if (!currentActivityName.equals(LAUNCHER_ACTIVITY_NAME)) {
            Log.i(TAG, "start service");
            Intent i = new Intent();
            i.setAction("cn.ismartv.voice.home");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.i(TAG, "getCurrentActivityName : pkg --->" + cn.getPackageName());
        Log.i(TAG, "getCurrentActivityName : cls ---> " + cn.getClassName());
        return cn.getClassName();
    }
}
