package tv.ismar.app.core.receiver;

import android.content.Context;
import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by huaijie on 2015/7/16.
 */
public class TimeCountdownBroadcastSender {
    public static final String COUNT = "count";
    public static final String ACTION_TIME_COUNTDOWN = "action_time_countdown";

    private static TimeCountdownBroadcastSender instance;

    private Timer timer;
    private TimerTask timerTask;

    private Context context;


    private int count;

    private TimeCountdownBroadcastSender(final Context context) {
        this.context = context;

    }


    public static TimeCountdownBroadcastSender getInstance(Context context) {
        if (null == instance) {
            instance = new TimeCountdownBroadcastSender(context);
        }

        return instance;
    }


    public void start(int from) {
        this.count = from;

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (count >= 0) {
                    Intent intent = new Intent();
                    intent.putExtra(COUNT, count);
                    intent.setAction(ACTION_TIME_COUNTDOWN);
                    context.sendBroadcast(intent);
                    count--;
                } else {
                    timerTask.cancel();
                }
            }

        };

        timer.schedule(timerTask, 0,1000);
    }

    public void cancel(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        if(timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }

    }
}
