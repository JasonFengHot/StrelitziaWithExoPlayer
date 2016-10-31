package tv.ismar.app.player;

import android.os.AsyncTask;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Item;
import tv.ismar.app.network.entity.EventProperty;


public class CallaPlay {

    private HashMap<String, Object> properties = new HashMap<String, Object>();
    private String eventName = "";

    /**
     * 播放器打开 video_start
     *
     * @param item(媒体id)        INTEGER
     * @param subitem(子媒体id,可空) INTEGER
     * @param title(名称)         STRING
     * @param clip              (视频id) INTEGER
     * @param quality(视频清晰度:    normal |  medium | high | ultra | adaptive) STRING
     * @param userid            (用户ID) STRING
     * @param speed             (网速, 单位Kbits/s) INTEGER
     * @return HashMap<String,Object>
     */
    public HashMap<String, Object> videoStart(Item item, Integer subitem, String title, Integer quality, Integer userid, Integer speed, String section, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item.pk);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, item.clip.pk);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        tempMap.put("userid", userid);
        tempMap.put(EventProperty.CHANNEL, item.content_model);
        tempMap.put(EventProperty.SECTION, section);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.SID, sid);
        tempMap.put("source", item.fromPage);
        tempMap.put("section", item.slug);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_START;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return properties;
    }

    /**
     * 开始播放缓冲结束 video_play_load
     *
     * @param item(媒体id)          INTEGER
     * @param subitem(子媒体id,可空)   INTEGER
     * @param title(名称)           STRING
     * @param clip                (视频id) INTEGER
     * @param quality(视频清晰度:      normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param duration(缓存时间,单位s)  INTEGER
     * @param speed               (网速, 单位Kbits/s) INTEGER
     * @param mediaip（媒体IP）STRING
     * @return HashMap<String,Object>
     */
    public HashMap<String, Object> videoPlayLoad(Integer item, Integer subitem, String title, Integer clip, Integer quality,
                                                 long duration, Integer speed, String mediaip, String sid,String playerurl,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put("play_url", playerurl);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_PLAY_LOAD;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }


    /**
     * 开始播放 video_play_start
     *
     * @param item(媒体id)        INTEGER
     * @param subitem(子媒体id,可空) INTEGER
     * @param title(名称)         STRING
     * @param clip              (视频id) INTEGER
     * @param quality(视频清晰度:    normal |  medium | high | ultra | adaptive) STRING
     * @param speed             (网速, 单位Kbits/s) INTEGER
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoPlayStart(Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer speed,String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        //tempMap.put("speed", speed);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_PLAY_START;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放暂停 video_play_pause
     *
     * @param item(媒体id)        INTEGER
     * @param subitem(子媒体id,可空) INTEGER
     * @param title(名称)         STRING
     * @param clip              (视频id) INTEGER
     * @param quality(视频清晰度:    normal |  medium | high | ultra | adaptive) STRING
     * @param position(位置，单位s)  INTEGER
     * @param speed             (网速, 单位Kbits/s) INTEGER
     * @return HashMap<String,Object>
     */
    public HashMap<String, Object> videoPlayPause(Integer item, Integer subitem, String title, Integer clip, Integer currQuality, Integer speed, Integer position, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(currQuality));
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
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
     * @param item(媒体id)        INTEGER
     * @param subitem(子媒体id,可空) INTEGER
     * @param title(名称)         STRING
     * @param clip              (视频id) INTEGER
     * @param quality(视频清晰度:    normal |  medium | high | ultra | adaptive) STRING
     * @param position(位置，单位s)  INTEGER
     * @param speed             (网速, 单位Kbits/s) INTEGER
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoPlayContinue(Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer speed, Integer position, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        tempMap.put("speed", speed + "KByte/s");
        //tempMap.put("speed", speed);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_PLAY_CONTINUE;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放快进/快退 video_play_seek
     *
     * @param item(媒体id)         INTEGER
     * @param subitem(子媒体id,可空)  INTEGER
     * @param title(名称)          STRING
     * @param clip               (视频id) INTEGER
     * @param quality(视频清晰度:     normal |  medium | high | ultra | adaptive) STRING
     * @param position(目标位置,单位s) INTEGER
     * @param speed              (网速, 单位Kbits/s) INTEGER
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoPlaySeek(Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer speed, Integer position, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        tempMap.put("speed", speed + "KByte/s");
        //tempMap.put("speed", speed);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_PLAY_SEEK;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放快进/快退缓冲结束 video_play_seek_blockend
     *
     * @param item                (媒体id) INTEGER
     * @param subitem(子媒体id,可空)   INTEGER
     * @param title(名称)           STRING
     * @param clip                (视频id) INTEGER
     * @param quality(视频清晰度:      normal |  medium | high | ultra | adaptive | adaptive_norma l | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param position(缓冲位置，单位s)  INTEGER
     * @param speed               (网速, 单位Kbits/s) INTEGER
     * @param duration(缓存时间,单位s)  INTEGER
     * @param mediaip（媒体IP）STRING
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoPlaySeekBlockend(Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer speed, Integer position, long duration, String mediaip, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        //tempMap.put("speed", speed);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_PLAY_SEEK_BLOCKEND;
        properties = tempMap;
        //new LogTask().execute();

        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放缓冲结束 video_play_blockend
     *
     * @param item                (媒体id) INTEGER
     * @param subitem(子媒体id,可空)   INTEGER
     * @param title(名称)           STRING
     * @param clip                (视频id) INTEGER
     * @param quality(视频清晰度:      normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param position(缓冲位置，单位s)  INTEGER
     * @param speed               (网速, 单位Kbits/s) INTEGER
     * @param duration(缓存时间,单位s)  INTEGER
     * @param mediaip（媒体IP）STRING
     * @return HashMap<String,Object>
     */
    public HashMap<String, Object> videoPlayBlockend(Integer item, Integer subitem, String title, Integer clip, Integer quality,
                                                     Integer speed, Integer position, long duration, String mediaip, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        //tempMap.put("speed", speed);
        //tempMap.put("position", position/1000);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_PLAY_BLOCKEND;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放时网速 video_play_speed
     *
     * @param item    (媒体id) INTEGER
     * @param subitem (子媒体id, 可空) INTEGER
     * @param title   (名称) STRING
     * @param clip    (视频id) INTEGER
     * @param quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed   (网速, 单位Kbits/s) INTEGER
     * @param mediaip (媒体IP) STRING
     * @return HashMap<String,Object>
     */


    public HashMap<String, Object> videoPlaySpeed(Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer speed, String mediaip, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        tempMap.put(EventProperty.SPEED, speed + "KByte/s");
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_PLAY_SPEED;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放时下载速度慢  video_low_speed
     *
     * @param item    (媒体id) INTEGER
     * @param subitem (子媒体id, 可空) INTEGER
     * @param title   (名称) STRING
     * @param clip    (视频id) INTEGER
     * @param quality (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed   (网速, 单位Kbits/s) INTEGER
     * @param mediaip (媒体IP) STRING
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoLowSpeed(Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer speed, String mediaip, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        //tempMap.put("speed", speed);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_LOW_SPEED;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放器退出 video_exit
     *
     * @param item         (媒体id) INTEGER
     * @param subitem      (子媒体id, 可空) INTEGER
     * @param title        (名称) STRING
     * @param clip         (视频id) INTEGER
     * @param quality      (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param speed        (网速, 单位Kbits/s) INTEGER
     * @param to(去向：detail | end) STRING
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoExit(Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer speed, String to, Integer position, long duration, String section, String sid, String source, String channel,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        //tempMap.put("speed", speed);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.TO, to);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.SECTION, section);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.SOURCE, source);
        tempMap.put(EventProperty.CHANNEL, channel);
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_EXIT;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

    }

    /**
     * 播放缓冲结束 videoExcept
     *
     * @param code(异常码servertimeout|servertimeout|noplayaddress|mediaexception|mediatimeout|filenotfound|nodetail|debuggingexception|noextras) STRING
     * @param content(异常内容)                                                                                                                    STRING
     * @param item                                                                                                                             (媒体id) INTEGER
     * @param subitem(子媒体id,可空)                                                                                                                INTEGER
     * @param title(名称)                                                                                                                        STRING
     * @param clip                                                                                                                             (视频id) INTEGER
     * @param quality(视频清晰度:                                                                                                                   normal |  medium | high | ultra | adaptive | adaptive_normal | adaptive_medium | adaptive_high | adaptive_ultra) STRING
     * @param position(播放位置，单位s)                                                                                                               INTEGER
     * @param speed                                                                                                                            (网速, 单位Kbits/s) INTEGER
     * @param mediaip（媒体IP）STRING
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoExcept(String code, String content, Integer item, Integer subitem, String title, Integer clip, Integer quality, Integer position,String playerflag) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.CODE, code);
        tempMap.put(EventProperty.CONTENT, content);
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.POSITION, position / 1000);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_EXCEPT;
        properties = tempMap;
        //new LogTask().execute();
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;
    }


    /**
     * 切换码流 video_switch_stream
     *
     * @param item           (媒体id) INTEGER
     * @param subitem        (子媒体id, 可空) INTEGER
     * @param title          (名称) STRING
     * @param clip           (视频id) INTEGER
     * @param quality        (视频清晰度: normal |  medium | high | ultra | adaptive) STRING
     * @param mode(切换模式：auto | manual) STRIN
     * @param speed          (网速, 单位Kbits/s) INTEGER
     * @param userid         STRING
     * @param mediaip        STRING
     * @return HashMap<String,Object>
     */

    public HashMap<String, Object> videoSwitchStream(Integer item, Integer subitem, String title, Integer clip, Integer quality, String mode, Integer speed, String userid, String mediaip, String sid,String playerflag) {

        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.ITEM, item);
        if (subitem != null)
            tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CLIP, clip);
        tempMap.put(EventProperty.QUALITY, switchQuality(quality));
        //tempMap.put("speed", speed);
        tempMap.put("speed", speed + "KByte/s");
        tempMap.put(EventProperty.MODE, mode);
        tempMap.put("userid", userid);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.SID, sid);
        tempMap.put(EventProperty.LOCATION, "detail");
        tempMap.put(EventProperty.PLAYER_FLAG, playerflag);
        eventName = NetworkUtils.VIDEO_SWITCH_STREAM;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
        return tempMap;

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

    public void ad_play_load(String source,String channel,String section,long duration,String mediaip,int itemid,int ad_id,String mediaflag) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SOURCE, source);
        tempMap.put(EventProperty.CHANNEL, channel);
        tempMap.put(EventProperty.SECTION, section);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.ITEM, itemid);
        tempMap.put(EventProperty.AD_ID, ad_id);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.AD_PLAY_LOAD;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void ad_play_blockend(String source,String channel,String section,long duration,String mediaip,int itemid,int ad_id,String mediaflag) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SOURCE, source);
        tempMap.put(EventProperty.CHANNEL, channel);
        tempMap.put(EventProperty.SECTION, section);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.ITEM, itemid);
        tempMap.put(EventProperty.AD_ID, ad_id);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.AD_PLAY_BLOCKEND;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void ad_play_exit(String source,String channel,String section,long duration,String mediaip,int itemid,int ad_id,String mediaflag) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SOURCE, source);
        tempMap.put(EventProperty.CHANNEL, channel);
        tempMap.put(EventProperty.SECTION, section);
        tempMap.put(EventProperty.DURATION, duration / 1000);
        tempMap.put(EventProperty.MEDIAIP, mediaip);
        tempMap.put(EventProperty.ITEM, itemid);
        tempMap.put(EventProperty.AD_ID, ad_id);
        tempMap.put(EventProperty.PLAYER_FLAG, mediaflag);
        eventName = NetworkUtils.AD_PLAY_EXIT;
        properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    public void pause_ad_play(String title,int media_id,String media_url,long duration,String mediaflag) {
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

    public void pause_ad_download(String title,int media_id,String media_url,String mediaflag) {
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
    }

    public void app_start(String sn,String device,String size,String os_version,long sd_size,long sd_free_size,String userid,String province,String city,String isp,String source,String Mac) {
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.SN, sn);
        tempMap.put(EventProperty.DEVICE, device);
        tempMap.put(EventProperty.SIZE, size);
        tempMap.put(EventProperty.OS_VERSION, os_version);
        tempMap.put(EventProperty.SD_SIZE, sd_size);
        tempMap.put(EventProperty.SD_FREE_SIZE, sd_free_size);
        tempMap.put(EventProperty.USER_ID, userid);
        tempMap.put(EventProperty.PROVINCE, province);
        tempMap.put(EventProperty.CITY, city);
        tempMap.put(EventProperty.ISP, isp);
        tempMap.put(EventProperty.SOURCE, source);
        tempMap.put(EventProperty.MAC, Mac);
        eventName = NetworkUtils.APP_START;
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

    public void boot_ad_play(String title, String mediaId, String mediaUrl, String duration){
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
    public  void bootAdvExcept(String code, String content) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("code", code);
        hashMap.put("content", content);
        eventName = NetworkUtils.BOOT_AD_EXCEPT;
        properties = hashMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }



    private String switchQuality(Integer currQuality) {
        String quality = "";
        switch (currQuality) {
            case 0:
                quality = "normal";
                break;
            case 1:
                quality = "medium";
                break;
            case 2:
                quality = "high";
                break;
            case 3:
                quality = "adaptive";
                break;
            default:
                quality = "";
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
