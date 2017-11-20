package tv.ismar.app.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.core.client.NetworkUtils;

/** Created by Beaver on 2016/4/14. */
public class SystemReporterReceiver extends BroadcastReceiver {

    private static final String TAG = "LH/ReporterReceiver";

    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    private static final String SP_FILE_NAME = "daisy_share_data";
    private static final String ACTION_REPORTER = "android.intent.action.reporter";
    private static final String eventName = "system_on";
    private static final String lastUseTime = "system_on_time";

    private static final String tv_on_time = "tv_on_time";
    private Runnable mUpLoadLogRunnable =
            new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        Thread.sleep(1 * 60 * 1000);
                        //                    synchronized (MessageQueue.async) {
                        // Thread.sleep(900000);

                        ArrayList<String> list = MessageQueue.getQueueList();
                        int i;
                        JSONArray s = new JSONArray();
                        if (list.size() > 0) {
                            for (i = 0; i < list.size(); i++) {
                                JSONObject obj;
                                try {
                                    Log.i("qazwsx", "json item==" + list.get(i).toString());
                                    obj = new JSONObject(list.get(i).toString());
                                    s.put(obj);
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            MessageQueue.remove();
                            NetworkUtils.LogSender(s.toString());
                            Log.i("qazwsx", "json array==" + s.toString());
                            Log.i("qazwsx", "remove");
                            if (i == list.size()) {}
                        } else {
                            Log.i("qazwsx", "queue is no elements");
                        }
                        //                    }

                        // NetworkUtils.LogUpLoad(getApplicationContext());
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Log.i("qazwsx", "Thread is finished!!!");
                }
            };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Action:" + action);
        SharedPreferences settings =
                context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if (!TextUtils.isEmpty(action)) {
            if (action.equals(ACTION_BOOT_COMPLETED)) {
                long lastUseTme = settings.getLong(lastUseTime, 0);
                if (lastUseTme > 0) {
                    HashMap<String, Object> properties = new HashMap<String, Object>();
                    properties.put("duration", lastUseTme / 1000); // int类型 单位 s
                    properties.put("version", Build.VERSION.RELEASE); // 软件版本号, 例如: S-1-1030-F
                    properties.put("firmware", ""); // 固件版本号, 例如: Nebula_SDV2.1_120406__C_1030-F
                    properties.put("welcome", "TV"); // 开机界面, (TV|Smart), 例如: TV
                    NetworkUtils.SaveLogToLocal(eventName, properties);
                    Log.i("reporter", "lastUseTime:" + lastUseTme);
                }
                long tvOnTime = new Date().getTime();
                Log.d(TAG, "tvOnTime:" + tvOnTime + "  lastUseTme:" + lastUseTme);
                editor.putLong(tv_on_time, new Date().getTime());
                editor.apply();
                new Thread(mUpLoadLogRunnable).start();
            } else if (action.equals(ACTION_SHUTDOWN)) {
                long lastTvOnTime = settings.getLong(tv_on_time, 0);
                long useTime = 0;
                if (lastTvOnTime > 0) {
                    useTime = new Date().getTime() - lastTvOnTime;
                }
                Log.d(TAG, "lastUseTime:" + useTime);
                editor.putLong(lastUseTime, useTime);
                editor.apply();
            } else if (action.equals(ACTION_REPORTER)) {
                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("duration", 86400000); // int类型 单位 s
                properties.put("version", Build.VERSION.RELEASE); // 软件版本号, 例如: S-1-1030-F
                properties.put("firmware", ""); // 固件版本号, 例如: Nebula_SDV2.1_120406__C_1030-F
                properties.put("welcome", "TV"); // 开机界面, (TV|Smart), 例如: TV
                NetworkUtils.SaveLogToLocal(eventName, properties);
                new Thread(mUpLoadLogRunnable).start();
            }
        }
    }
}
