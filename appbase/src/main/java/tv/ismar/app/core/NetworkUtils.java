package tv.ismar.app.core;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.core.preferences.AccountSharedPrefs;

public class NetworkUtils {
    private static String UA = "A11/V1 Unknown";

    private static final String TAG = "NetworkUtils";

    private static final String URL = "http://127.0.0.1:21098/log/track/";

    private static final int BUFFERSIZE = 1024;
    DataCollectionTask mDataCollectionTask;
    public static final int CONNET_TIME_OUT = 3000;
    public static final int READ_TIME_OUT = 10000;
    



    /**
     * 记录日志信息到本地
     */

    public static boolean SaveLogToLocal(String eventName,
                                         HashMap<String, Object> propertiesMap) {
        try {
            String jsonContent = getContentJson(eventName, propertiesMap);
//            synchronized (MessageQueue.async) {
                MessageQueue.addQueue(jsonContent);
//            }
            return true;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    /**
     * LogSender 上报日志记录
     *
     * @param eventName     上报的日志类型名。
     * @param propertiesMap 键值对，属性值
     * @return true、false 是否成功
     */
    public static Boolean LogSender(String Content) {
        try {
            String jsonContent = base64Code(Content);
//            String url = "http://ismartv.calla.tvxio.com/log";
//            String url = "http://192.168.1.119:8099/m3u8parse/parseM3u8";
            String host =  AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.LOG_DOMAIN);
            String url = "http://" + host+ "/log";
            java.net.URL connURL = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) connURL
                    .openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setConnectTimeout(CONNET_TIME_OUT);
            httpConn.setReadTimeout(READ_TIME_OUT);
            httpConn.setDoOutput(true);

            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Host", host);
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            // httpConn.setRequestProperty("User-Agent",
            // "ideatv_A21/S0054.38 TD04007053");
            httpConn.setRequestProperty("User-Agent",
                    VodUserAgent.getHttpUserAgent());
            httpConn.setRequestProperty("Pragma:", "no-cache");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty("Content-Encoding", "gzip");
            httpConn.setUseCaches(false);
            httpConn.connect();
            DataOutputStream out = new DataOutputStream(
                    httpConn.getOutputStream());

            String content = "sn=" + SimpleRestClient.sn_token + "&modelname="
                    + VodUserAgent.getModelName() + "&data="
                    + URLEncoder.encode(jsonContent, "UTF-8") + "&deviceToken="
                    + SimpleRestClient.device_token + "&acessToken="
                    + SimpleRestClient.access_token;
            out.writeBytes(content);
            // ///gzip
            // out.write(MessageGZIP.compressToByte(content));
            out.flush();
            out.close(); // flush and close
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream(), "UTF-8"));
            String line;
            int code = httpConn.getResponseCode();
            Log.i("LogSender", "LogSender code==" + code);
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            httpConn.disconnect();
            return true;
        } catch (MalformedURLException e) {
            Log.e(TAG, "" + " MalformedURLException " + e.toString());
            return false;
        } catch (IOException e) {
            Log.e(TAG, "" + " IOException " + e.toString());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "" + " Exception " + e.toString());
            return false;
        }

    }





    public static String getContentJson(String eventName,
                                         HashMap<String, Object> propertiesMap) throws JSONException {
        JSONObject propertiesJson = new JSONObject();
//        propertiesJson.put("time", TrueTime.now().getTime() / 1000);
        propertiesJson.put("time", System.currentTimeMillis()/ 1000);
        if (propertiesMap != null) {
            Set<String> set = propertiesMap.keySet();
            for (String key : set) {
                propertiesJson.put(key, propertiesMap.get(key));
            }
        }
        JSONObject logJson = new JSONObject();
        logJson.put("event", eventName);
        logJson.put("properties", propertiesJson);
//        Log.d(TAG, " Log data For Test === " + logJson.toString());
        return logJson.toString();
    }

    private static String base64Code(String date) {
        try {
            // return
            // Base64.encodeToString(date.getBytes("UTF-8"),Base64.NO_PADDING|Base64.NO_WRAP);
            return Base64
                    .encodeToString(date.getBytes("UTF-8"), Base64.URL_SAFE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static class DataCollectionTask extends
            AsyncTask<Object, Void, Void> {

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Object... params) {
            if (params != null && params.length > 0) {
                String eventName = (String) params[0];
                HashMap<String, Object> properties = null;
                if (params.length > 1 && params[1] != null) {
                    properties = (HashMap<String, Object>) params[1];
                }

                String jsonContent;
                try {
                    jsonContent = getContentJson(eventName, properties);
//                    synchronized (MessageQueue.async) {
                        MessageQueue.addQueue(jsonContent);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

    }



    /**
     * 设备启动
     */
    public static final String SYSTEM_ON = "system_on";
    /**
     * 播放器打开
     */
    public static final String VIDEO_START = "video_start";
    /**
     * 开始播放缓冲结束
     */
    public static final String VIDEO_PLAY_LOAD = "video_play_load";
    /**
     * 切换码流
     */
    public static final String VIDEO_SWITCH_STREAM = "video_switch_stream";
    /**
     * 开始播放
     */
    public static final String VIDEO_PLAY_START = "video_play_start";
    /**
     * 播放暂停
     */
    public static final String VIDEO_PLAY_PAUSE = "video_play_pause";
    /**
     * 播放继续
     */
    public static final String VIDEO_PLAY_CONTINUE = "video_play_continue";
    /**
     * 播放快进/快退
     */
    public static final String VIDEO_PLAY_SEEK = "video_play_seek";
    /**
     * 播放快进/快退缓冲结束
     */
    public static final String VIDEO_PLAY_SEEK_BLOCKEND = "video_play_seek_blockend";
    /**
     * 播放缓冲结束
     */
    public static final String VIDEO_PLAY_BLOCKEND = "video_play_blockend";
    /**
     * 播放时网速
     */
    public static final String VIDEO_PLAY_SPEED = "video_play_speed";
    /**
     * 播放时下载速度慢
     */
    public static final String VIDEO_LOW_SPEED = "video_low_speed";
    /**
     * 播放器退出
     */
    public static final String VIDEO_EXIT = "video_exit";
    /**
     * 视频收藏
     */
    public static final String VIDEO_COLLECT = "video_collect";
    /**
     * 进入收藏界面
     */
    public static final String VIDEO_COLLECT_IN = "video_collect_in";
    /**
     * 退出收藏界面
     */
    public static final String VIDEO_COLLECT_OUT = "video_collect_out";
    /**
     * 视频存入历史
     */
    public static final String VIDEO_HISTORY = "video_history";
    /**
     * 进入播放历史界面
     */
    public static final String VIDEO_HISTORY_IN = "video_history_in";
    /**
     * 退出播放历史界面
     */
    public static final String VIDEO_HISTORY_OUT = "video_history_out";
    /**
     * 视频评分
     */
    public static final String VIDEO_SCORE = "video_score";
    /**
     * 视频评论
     */
    public static final String VIDEO_COMMENT = "video_comment";

    /**
     * 启动某视频频道
     */
    public static final String VIDEO_CHANNEL_IN = "video_channel_in";

    /**
     * 退出某视频频道
     */
    public static final String VIDEO_CHANNEL_OUT = "video_channel_out";

    /**
     * 进入分类浏览
     */
    public static final String VIDEO_CATEGORY_IN = "video_category_in";

    /**
     * 退出分类浏览
     */
    public static final String VIDEO_CATEGORY_OUT = "video_category_out";

    /**
     * 进入媒体详情页
     */
    public static final String VIDEO_DETAIL_IN = "video_detail_in";

    /**
     * 退出媒体详情页
     */
    public static final String VIDEO_DETAIL_OUT = "video_detail_out";
    /**
     * 在详情页进入关联
     */
    public static final String VIDEO_RELATE = "video_relate";

    /**
     * 进入关联界面
     */
    public static final String VIDEO_RELATE_IN = "video_relate_in";
    /**
     * 退出关联界面
     */
    public static final String VIDEO_RELATE_OUT = "video_relate_out";
    /**
     * 进入专题浏览
     */
    public static final String VIDEO_TOPIC_IN = "video_topic_in";
    /**
     * 退出专题浏览
     */
    public static final String VIDEO_TOPIC_OUT = "video_topic_out";
    /**
     * 视频预约
     */
    public static final String VIDEO_NOTIFY = "video_notify";
    /**
     * 点击视频购买
     */
    public static final String VIDEO_EXPENSE_CLICK = "video_expense_click";
    /**
     * 视频购买
     */
    public static final String VIDEO_EXPENSE = "video_expense";
    /**
     * 搜索
     */
    public static final String VIDEO_SEARCH = "video_search";
    /**
     * 搜索结果命中
     */
    public static final String VIDEO_SEARCH_ARRIVE = "video_search_arrive";
    /**
     * 播放器异常
     */
    public static final String VIDEO_EXCEPT = "video_except";
    /**
     * 栏目页异常
     */
    public static final String CATEGORY_EXCEPT = "category_except";
    /**
     * 详情页异常
     */
    public static final String DETAIL_EXCEPT = "detail_except";
    /**
     * 用户点击某个推荐影片
     */
    public static final String LAUNCHER_VOD_CLICK = "launcher_vod_click";
    /**
     * 预告片播放
     */
    public static final String LAUNCHER_VOD_TRAILER_PLAY = "launcher_vod_trailer_play";
    /**
     * 用户登录
     */
    public static final String USER_LOGIN = "user_login";
    /**
     * 进入筛选界面
     */
    public static final String VIDEO_FILTER_IN = "video_filter_in";
    /**
     * 退出筛选界面
     */
    public static final String VIDEO_FILTER_OUT = "video_filter_out";
    /**
     * 使用筛选
     */
    public static final String VIDEO_FILTER = "video_filter";
    /**
     * 进入我的频道
     */
    public static final String VIDEO_MYCHANNEL_IN = "video_mychannel_in";
    /**
     * 退出我的频道
     */
    public static final String VIDEO_MYCHANNEL_OUT = "video_mychannel_out";
    /**
     * 进入剧集列表界面
     */
    public static final String VIDEO_DRAMALIST_IN = "video_dramalist_in";
    /**
     * 退出剧集列表界面
     */
    public static final String VIDEO_DRAMALIST_OUT = "video_dramalist_out";

    public static final String FRONT_PAGE_VIDEO = "frontpagevideo";
    /**
     * 用户点击推荐影片
     */
    public static final String HOMEPAGE_VOD_CLICK = "homepage_vod_click";
    /**
     * 广告播放缓冲结束
     */
    public static final String AD_PLAY_LOAD = "ad_play_load";
    /**
     * 广告播放卡顿
     */
    public static final String AD_PLAY_BLOCKEND = "ad_play_blockend";
    /**
     * 广告播放结束
     */
    public static final String AD_PLAY_EXIT = "ad_play_exit";
    /**
     * 暂停广告播放
     */
    public static final String PAUSE_AD_PLAY = "pause_ad_play";
    /**
     * 暂停广告下载
     */
    public static final String PAUSE_AD_DOWNLOAD = "pause_ad_download";
    /**
     * 暂停广告异常
     */
    public static final String PAUSE_AD_EXCEPT = "pause_ad_except";
    /**
     * 应用启动
     */
    public static final String APP_START = "app_start";
    /**
     * 应用退出
     */
    public static final String APP_EXIT = "app_exit";

    public static final String BOOT_AD_PLAY = "boot_ad_play";

    public static final String BOOT_AD_DOWNLOAD = "boot_ad_download";

    public static final String BOOT_AD_EXCEPT = "boot_ad_except";
}
