package tv.ismar.app.player;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.HashMap;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.History;
import tv.ismar.app.entity.Item;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.reporter.IsmartvMedia;
import tv.ismar.app.util.Utils;

public class CallaPlay {

    private HashMap<String, Object> properties = new HashMap<String, Object>();
    private String eventName = "";

    // 日志发送和原先Daisy项目相同，由于现有播放器没有使用Item对象
    private HashMap<String, Object> getPublicParams(IsmartvMedia media, int speed, String sid, String playerFlag) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(EventProperty.ITEM, media.getPk());
        if (media.getSubItemPk() > 0 && media.getPk() != media.getSubItemPk()) {
            tempMap.put(EventProperty.SUBITEM, media.getSubItemPk());
        }
        tempMap.put(EventProperty.TITLE, media.getTitle());
        tempMap.put(EventProperty.CLIP, media.getClipPk());
        tempMap.put(EventProperty.QUALITY, switchQuality(media.getQuality()));
        tempMap.put(EventProperty.CHANNEL, media.getChannel());
        tempMap.put(EventProperty.SPEED, speed + "KByte/s");
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerFlag);
        return tempMap;
    }

    /**
     * 播放器打开 video_start
     *
     * @param media  (媒体)       Item
     *               quality (视频清晰度 normal   medium  high  ultra  adaptive) STRING
     * @param userId (用户ID) STRING
     * @param speed  (网速, 单位KB/s) INTEGER
     * @return HashMap
     */
    public HashMap<String, Object> videoStart(IsmartvMedia media, String userId, int speed, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        userId = TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername()) ? userId : IsmartvActivator.getInstance().getUsername();
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put("userid", userId);
        tempMap.put("source", media.getSource());
        tempMap.put("section", media.getSection());
        eventName = NetworkUtils.VIDEO_START;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return properties;
    }

    /**
     * 开始播放缓冲结束 video_play_load
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度)normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param duration (缓存时间,单位s)INTEGER
     * @param speed    (网速,单位KB/s)INTEGER
     * @param mediaIP  (媒体IP)STRING
     * @return HashMap
     */
    public HashMap<String, Object> videoPlayLoad(IsmartvMedia media,
                                                 long duration, int speed, String mediaIP, String sid, String playerUrl, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaIP);
        tempMap.put("play_url", playerUrl);
        eventName = NetworkUtils.VIDEO_PLAY_LOAD;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }


    /**
     * 开始播放 video_play_start
     *
     * @param media (媒体)Item
     *              quality (视频清晰度) normal |  medium | high | ultra | adaptive) STRING
     * @param speed (网速, 单位KB/s) INTEGER
     * @return HashMap
     */

    public HashMap<String, Object> videoPlayStart(IsmartvMedia media, int speed, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        eventName = NetworkUtils.VIDEO_PLAY_START;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放暂停 video_play_pause
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度)normal |  medium | high | ultra | adaptive) STRING
     * @param position (位置，单位s) INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     * @return HashMap
     */
    public HashMap<String, Object> videoPlayPause(IsmartvMedia media, int speed, Integer position, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.POSITION, position / 1000);
        //tempMap.put("speed", speed);
        eventName = NetworkUtils.VIDEO_PLAY_PAUSE;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放继续 video_play_continue
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度:    normal |  medium | high | ultra | adaptive) STRING
     * @param position (位置，单位s)  INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     * @return HashMap
     */

    public HashMap<String, Object> videoPlayContinue(IsmartvMedia media, int speed, Integer position, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.POSITION, position / 1000);
        eventName = NetworkUtils.VIDEO_PLAY_CONTINUE;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放快进/快退 video_play_seek
     *
     * @param media    (媒体)Item
     *                 quality  (视频清晰度:     normal |  medium | high | ultra | adaptive) STRING
     * @param position (目标位置,单位s) INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     * @return HashMap
     */

    public HashMap<String, Object> videoPlaySeek(IsmartvMedia media, int speed, Integer position, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.POSITION, position / 1000);
        eventName = NetworkUtils.VIDEO_PLAY_SEEK;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放快进/快退缓冲结束 video_play_seek_blockend
     *
     * @param media    (媒体) Item
     *                 quality  (视频清晰度:      normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param position (缓冲位置，单位s)  INTEGER
     * @param speed    (网速, 单位KB/s) INTEGER
     * @param duration (缓存时间,单位s)  INTEGER
     * @param mediaIP  (媒体IP)STRING
     * @return HashMap
     */

    public HashMap<String, Object> videoPlaySeekBlockend(IsmartvMedia media, int speed, Integer position, long duration, String mediaIP, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaIP);
        eventName = NetworkUtils.VIDEO_PLAY_SEEK_BLOCKEND;
        properties = tempMap;
        //new LogTask().execute();

        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放缓冲结束 video_play_blockend
     *
     * @param media    (媒体) Item
     *                 quality  (视频清晰度:      normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param speed    (网速, 单位KB/s) INTEGER
     * @param duration (缓存时间,单位s)  INTEGER
     * @param mediaIP  (媒体IP)STRING
     * @return HashMap
     */
    public HashMap<String, Object> videoPlayBlockend(IsmartvMedia media, int speed, long duration, String mediaIP, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaIP);
        eventName = NetworkUtils.VIDEO_PLAY_BLOCKEND;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放时网速 video_play_speed
     *
     * @param media   (媒体) INTEGER
     *                quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed   (网速, 单位KB/s) INTEGER
     * @param mediaIP (媒体IP) STRING
     * @return HashMap
     */
    public HashMap<String, Object> videoPlaySpeed(IsmartvMedia media, int speed, String mediaIP, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.MEDIAIP, mediaIP);
        eventName = NetworkUtils.VIDEO_PLAY_SPEED;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放时下载速度慢  video_low_speed
     *
     * @param media   (媒体) INTEGER
     *                quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed   (网速, 单位KB/s) INTEGER
     * @param mediaIP (媒体IP) STRING
     * @return HashMap
     */

    public HashMap<String, Object> videoLowSpeed(IsmartvMedia media, int speed, String mediaIP, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.MEDIAIP, mediaIP);
        eventName = NetworkUtils.VIDEO_LOW_SPEED;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放器退出 video_exit
     *
     * @param media (媒体) INTEGER
     *              quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed (网速, 单位KB/s) INTEGER
     * @param to    (去向：detail | end) STRING
     * @return HashMap
     */

    public HashMap<String, Object> videoExit(IsmartvMedia media, int speed, String to, Integer position, long duration, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.TO, to);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.SECTION, media.getSection());
        tempMap.put(EventProperty.SOURCE, media.getSource());
        eventName = NetworkUtils.VIDEO_EXIT;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放器异常 videoExcept
     *
     * @param code     (异常码servertimeout|servertimeout|noplayaddress|mediaexception|mediatimeout|filenotfound|nodetail|debuggingexception|noextras) STRING
     * @param content  (异常内容)                                                                                                                    STRING
     * @param media    (媒体) INTEGER
     *                 quality  (视频清晰度:     normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param position (播放位置，单位s) INTEGER
     * @return HashMap
     */

    public HashMap<String, Object> videoExcept(String code, String content, IsmartvMedia media, int speed, String sid, Integer position, String playerFlag) {
        if (media == null) {
            return null;
        }
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.CODE, code == null ? "" : code);
        tempMap.put(EventProperty.CONTENT, content == null ? "" : content);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.PLAYER_FLAG, playerFlag);
        eventName = NetworkUtils.VIDEO_EXCEPT;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;
    }


    /**
     * 切换码流 video_switch_stream
     *
     * @param media   (媒体) INTEGER
     *                quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param mode    (切换模式：auto | manual) STRING
     * @param speed   (网速, 单位KB/s) INTEGER
     * @param userid  STRING
     * @param mediaip STRING
     * @return HashMap
     */

    public HashMap<String, Object> videoSwitchStream(IsmartvMedia media, String mode, int speed, String userid, String mediaip, String sid, String playerFlag) {
        if (media == null) {
            return null;
        }
        userid = TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername()) ? userid : IsmartvActivator.getInstance().getUsername();
        HashMap<String, Object> tempMap = getPublicParams(media, speed, sid, playerFlag);
        tempMap.put(EventProperty.MODE, mode);
        tempMap.put("userid", userid);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.LOCATION, "detail");
        eventName = NetworkUtils.VIDEO_SWITCH_STREAM;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    public void ad_play_load(IsmartvMedia media, long duration, String mediaip, int ad_id, String mediaflag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SOURCE, media.getSource());
        tempMap.put(EventProperty.CHANNEL, media.getChannel());
        tempMap.put(EventProperty.SECTION, media.getSection());
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.ITEM, media.getPk());
        tempMap.put(EventProperty.AD_ID, ad_id);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.AD_PLAY_LOAD;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void ad_play_blockend(IsmartvMedia media, long duration, String mediaip, int ad_id, String mediaflag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SOURCE, media.getSource());
        tempMap.put(EventProperty.CHANNEL, media.getChannel());
        tempMap.put(EventProperty.SECTION, media.getSection());
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.ITEM, media.getPk());
        tempMap.put(EventProperty.AD_ID, ad_id);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.AD_PLAY_BLOCKEND;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void ad_play_exit(IsmartvMedia media, long duration, String mediaip, int ad_id, String mediaflag) {
        if (media == null) {
            return;
        }
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SOURCE, media.getSource());
        tempMap.put(EventProperty.CHANNEL, media.getChannel());
        tempMap.put(EventProperty.SECTION, media.getSection());
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.ITEM, media.getPk());
        tempMap.put(EventProperty.AD_ID, ad_id);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.AD_PLAY_EXIT;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void pause_ad_play(String title, int media_id, String media_url, long duration, String mediaflag) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.MEDIA_ID, media_id);
        tempMap.put(EventProperty.MEDIA_URL, media_url);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.PAUSE_AD_PLAY;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void pause_ad_download(String title, int media_id, String media_url, String mediaflag) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.MEDIA_ID, media_id);
        tempMap.put(EventProperty.MEDIA_URL, media_url);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.PAUSE_AD_DOWNLOAD;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void pause_ad_except(Integer errcode, String errorContent) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.CODE, errcode);
        tempMap.put(EventProperty.CONTENT, errorContent);
        String eventName = NetworkUtils.PAUSE_AD_EXCEPT;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void homepage_vod_click(int pk, String title, String channel, Integer position, String type) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.PK, pk);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CHANNEL, channel);
        tempMap.put(EventProperty.POSITION, position);
        tempMap.put(EventProperty.TYPE, type);
        eventName = NetworkUtils.HOMEPAGE_VOD_CLICK;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void app_start(String sn, String device, String size, String os_version, int app_version, long sd_size, long sd_free_size, String userid, String province, String city, String isp, String source, String Mac, String title, String packageName) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SN, sn);
        tempMap.put(EventProperty.DEVICE, device);
        tempMap.put(EventProperty.SIZE, size);
        tempMap.put(EventProperty.OS_VERSION, os_version);
        tempMap.put(EventProperty.SD_SIZE, sd_size);
        tempMap.put(EventProperty.SD_FREE_SIZE, sd_free_size);
        tempMap.put("userid", userid);
        tempMap.put(EventProperty.PROVINCE, province);
        tempMap.put(EventProperty.CITY, city);
        tempMap.put(EventProperty.ISP, isp);
        tempMap.put(EventProperty.SOURCE, source == null ? "" : source);
        tempMap.put(EventProperty.MAC, Mac);
        tempMap.put(EventProperty.VERSION, app_version);
        tempMap.put("title", title);
        tempMap.put("code", packageName);
        eventName = NetworkUtils.APP_START;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void launcher_vod_click(String type, int pk, String title, int position) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("type", type);
        tempMap.put("pk", pk);
        tempMap.put("title", title);
        tempMap.put("position", position);
        eventName = NetworkUtils.LAUNCHER_VOD_CLICK;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);

    }

    public void app_exit(long duration, int version) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.VERSION, version);
        eventName = NetworkUtils.APP_EXIT;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void boot_ad_play(String title, String mediaId, String mediaUrl, String duration) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("title", title);
        hashMap.put("media_id", mediaId);
        hashMap.put("media_url", mediaUrl);
        hashMap.put("duration", duration);
        eventName = NetworkUtils.BOOT_AD_PLAY;
        properties = hashMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);

    }

    /**
     * 开屏广告下载
     *
     * @param title    广告物料id, 例如: 391004135
     * @param mediaId  广告物料id, 例如: 391004135
     * @param mediaUrl 广告物料地址, 例如: http://www.ismartv.cn/test.jpg
     */
    public void bootAdvDownload(String title, String mediaId, String mediaUrl) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("title", title);
        hashMap.put("media_id", mediaId);
        hashMap.put("media_url", mediaUrl);
        eventName = NetworkUtils.BOOT_AD_DOWNLOAD;
        properties = hashMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /**
     * 开屏广告异常
     *
     * @param code    异常码, 例如: 264
     * @param content 异常内容, 例如: 264
     */
    public void bootAdvExcept(String code, String content) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("code", code);
        hashMap.put("content", content);
        eventName = NetworkUtils.BOOT_AD_EXCEPT;
        properties = hashMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /**
     * 导视预告片播放
     *
     * @param url url 预告片地址, 例如: http://v.ismartv.cn/upload/topvideo/no10_20120411.mp4
     */
    public void homepage_vod_trailer_play(String url, String channel) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("url", url);
        hashMap.put("channel", channel);
        eventName = NetworkUtils.HOMEPAGE_VOD_TRAILER_PLAY;
        properties = hashMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /**
     * 首页异常退出
     */
    public void exception_except(String referer, String page, String channel,
                                 String tab, int item, String url,
                                 int version, String code, String detail) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
//        referer，string枚举类型，进入当前页面的入口，见下述列表，值的内容为列表中的英文部分
//        page，string枚举类型，表示页面的类型，见下述列表，值的内容为列表中的英文部分
//        channel，string枚举类型，页面所属频道，见下述列表，值的内容为列表中的英文部分。如果不属于任何一个频道，则该字段为空字符串。page字段和channel合起来共同确定一个页面。
//        tab，string枚举类型，用于表示出错的具体标签页，例如频道的列表页中每个section为一个标签页。如果没有细分标签页，则值为空字符串。
//        item，int类型，如果页面的异常发生在某个视频、产品包上，则item值为异常视频item或者产品包的ID，否则该值为空字符串
//        url，string类型，该页面发生异常的区域对应的服务器数据接口的URL及参数，对于不需要请求服务器数据的异常，设置为空值。
//        version，string类型，表示视云客户端的版本号。
//        code，string枚举类型，表示异常产生的原因。可选的值为：server（表示服务端、网络、或CDN的各类错误或异常）、data（表示拿到的数据错误或不完整）、system（电视的系统级异常导致）、client（视云客户端产生的异常导致）、unknown（未知原因的异常）
//        detail
        hashMap.put("referer", referer);
        hashMap.put("page", page);
        hashMap.put("channel", channel);
        hashMap.put("tab", tab);
        hashMap.put("item", item);
        hashMap.put("url", url);
        hashMap.put("version", version);
        hashMap.put("code", code);
        hashMap.put("detail", detail);
        eventName = NetworkUtils.EXCEPTION_EXIT;
        properties = hashMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void addHistory(History history) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        int item_id = Utils.getItemPk(history.url);
        tempMap.put(EventProperty.ITEM, item_id);
        if (history.sub_url != null) {
            int sub_id = Utils.getItemPk(history.sub_url);
            tempMap.put(EventProperty.SUBITEM, sub_id);
        }
        tempMap.put(EventProperty.TITLE, history.title);
        tempMap.put(EventProperty.POSITION, history.last_position / 1000);
        tempMap.put("userid", TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername()) ? IsmartvActivator.getInstance().getDeviceToken() : IsmartvActivator.getInstance().getUsername());
        eventName = NetworkUtils.VIDEO_HISTORY;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);

    }

    private String switchQuality(Integer currQuality) {
        String quality = "";
        switch (currQuality) {
            case 0:
                quality = "low";
                break;
            case 1:
                quality = "adaptive";
                break;
            case 2:
                quality = "normal";
                break;
            case 3:
                quality = "medium";
                break;
            case 4:
                quality = "high";
                break;
            case 5:
                quality = "ultra";
                break;
            case 6:
                quality = "blueray";
                break;
            case 7:
                quality = "4k";
                break;
        }
        return quality;
    }

    private class LogTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            return NetworkUtils.SaveLogToLocal(eventName, properties);
        }

    }
}
