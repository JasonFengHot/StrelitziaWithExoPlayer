package tv.ismar.app.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by huibin on 17-3-30.
 */

public class VoiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String currentActivityName = getCurrentActivityName(context);
        if (currentActivityName.equals("tv.ismar.player.view.PlayerActivity")) {

        }else {
            Intent voiceIntent = new Intent();
            voiceIntent.setClassName(context, "tv.ismar.homepage.view.HomePageActivity");
            voiceIntent.putExtra("position", "1");
            voiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(voiceIntent);
        }
    }

    public String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getClassName();
    }
}
