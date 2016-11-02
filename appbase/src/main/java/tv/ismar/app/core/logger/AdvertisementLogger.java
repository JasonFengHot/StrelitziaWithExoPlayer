package tv.ismar.app.core.logger;

import android.util.Base64;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.ismartv.truetime.TrueTime;

/**
 * Created by huaijie on 11/16/15.
 */
public class AdvertisementLogger extends AppLogger {
    public static final String BOOT_ADV_DOWNLOAD_EXCEPTION_CODE = "801";
    public static final String BOOT_ADV_DOWNLOAD_EXCEPTION_STRING = "获取广告物料失败";
    public static final String BOOT_ADV_PLAY_EXCEPTION_CODE = "802";
    public static final String BOOT_ADV_PLAY_EXCEPTION_STRING = "展示广告失败";

    private static void advLog(HashMap<String, String> params) {
        params.put("time", String.valueOf(TrueTime.now().getTime()));
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }

        if (stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }
        log(Base64.encodeToString(stringBuffer.toString().getBytes(), Base64.URL_SAFE));
    }

//    /**
//     * 开屏广告播放
//     *
//     * @param title    广告标题, 例如: 百年润发
//     * @param mediaId  广告id, 例如: 391004135
//     * @param mediaUrl 广告地址, 例如: http://www.ismartv.cn/test.jpg
//     * @param duration 播放时长, 单位s, 例如: 5
//     */
//    public static void bootAdvPlay(String title, String mediaId, String mediaUrl, String duration) {
//        HashMap<String, String> hashMap = new HashMap<String, String>();
//        hashMap.put("title", title);
//        hashMap.put("media_id", mediaId);
//        hashMap.put("media_url", mediaUrl);
//        hashMap.put("duration", duration);
//        advLog(hashMap);
//    }
//
//    /**
//     * 开屏广告下载
//     *
//     * @param title    广告物料id, 例如: 391004135
//     * @param mediaId  广告物料id, 例如: 391004135
//     * @param mediaUrl 广告物料地址, 例如: http://www.ismartv.cn/test.jpg
//     */
//    public static void bootAdvDownload(String title, String mediaId, String mediaUrl) {
//        HashMap<String, String> hashMap = new HashMap<String, String>();
//        hashMap.put("title", title);
//        hashMap.put("media_id", mediaId);
//        hashMap.put("media_url", mediaUrl);
//        advLog(hashMap);
//    }
//
//    /**
//     * 开屏广告异常
//     *
//     * @param code    异常码, 例如: 264
//     * @param content 异常内容, 例如: 264
//     */
//    public static void bootAdvExcept(String code, String content) {
//        HashMap<String, String> hashMap = new HashMap<String, String>();
//        hashMap.put("code", code);
//        hashMap.put("content", content);
//        advLog(hashMap);
//    }


}
