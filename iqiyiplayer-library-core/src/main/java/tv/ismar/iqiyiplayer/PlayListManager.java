package tv.ismar.iqiyiplayer;

import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.SdkVideo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DEBUG CODE, simulate playlist
 */
public class PlayListManager {
    public static final IMedia MEDIA_FRONT_TEST = new SdkVideo("204122401", "528965000", false);
    public static final IMedia MEDIA_FRONT_AD = new SdkVideo("113177501", "117962900", false);
    public static final IMedia MEDIA_STANDARD = new SdkVideo("202321601", "372295700", false);  //流畅码流
    public static final IMedia MEDIA_HIGH = new SdkVideo("202321601", "372296300", false);  //高清码流
    public static final IMedia MEDIA_720P = new SdkVideo("203034301", "523100200", false);  //720P码流
    public static final IMedia MEDIA_1080P = new SdkVideo("202203201", "377866500", false);  //1080P码流
    public static final IMedia MEDIA_4K = new SdkVideo("223725200", "223725200", false);  //4K码流
    public static final IMedia MEDIA_4K2 = new SdkVideo("384598500", "384598500", false);  //4K码流

    public static final IMedia MEDIA_AD = new SdkVideo("202321601", "372295700", false);  //AD

    public static final IMedia MEDIA_VIPBS = new SdkVideo("203143601", "533451900", false);  //vip码流

    public static final IMedia MEDIA_H265 = new SdkVideo("202203201", "377866500", false);  //H265码流
    public static final IMedia MEDIA_DOLBY = new SdkVideo("202168401", "310271100", false);  //杜比码流

    public static final IMedia MEDIA_VIP_MINUTE1 = new SdkVideo("102366900", "102366900", true);  //VIP, 分钟试看
    public static final IMedia MEDIA_VIP_MINUTE2 = new SdkVideo("414197100", "414197100", true);  //VIP, 分钟试看
    public static final IMedia MEDIA_VIP_MINUTE3 = new SdkVideo("558518700", "558518700", true);  //VIP, 王牌逗王牌
    public static final IMedia MEDIA_VIP_MINUTE4 = new SdkVideo("557730800", "557730800", true);  //VIP, 冰川时代5
    //    public static final IMedia MEDIA_VIP_MINUTE5 = new SdkVideo("203964801", "556313800", true);  //VIP, 美人为馅第一集
    public static final IMedia MEDIA_VIP_MINUTE6 = new SdkVideo("549432100", "549432100", true);  //VIP, 七月与安生
    public static final IMedia MEDIA_VIP_MINUTE7 = new SdkVideo("546830300", "546830300", true);  //VIP, 反贪风暴2
    public static final IMedia MEDIA_VIP_MINUTE8 = new SdkVideo("561026100", "561026100", true);  //VIP, 六弄咖啡馆
    public static final IMedia MEDIA_VIP_MINUTE9 = new SdkVideo("556429500", "556429500", true);  //VIP, 海底总动员2（国语）

    public static final IMedia MEDIA_VIP_WHOLE1 = new SdkVideo("102155301", "99920700", true);  //VIP, 整集试看
    public static final IMedia MEDIA_VIP_WHOLE2 = new SdkVideo("102155301", "99920600", true);

    public static final IMedia MEDIA_VIP_CANNOTPREVIEW1 = new SdkVideo("203853401", "483867500", true);  //VIP, 不能试看

    public static final IMedia MEDIA_CAROUSEL_MOVIE = new SdkVideo("380078422", false, IMedia.LIVE_TYPE_CAROUSEL);
    public static final IMedia MEDIA_CAROUSEL_SERIES = new SdkVideo("380078622", false, IMedia.LIVE_TYPE_CAROUSEL);
    public static final IMedia MEDIA_CAROUSEL_DOCUMENTARY = new SdkVideo("380078922", false, IMedia.LIVE_TYPE_CAROUSEL);
    public static final IMedia MEDIA_CAROUSEL_AGES_THEATER = new SdkVideo("380115322", false, IMedia.LIVE_TYPE_CAROUSEL);

    //    public static final IMedia MEDIA_INTERTRUST = new SdkVideo("363769500", "363769500", true, IMedia.DRM_TYPE_INTERTRUST, 0, null);
    public static final IMedia MEDIA_INTERTRUST = new SdkVideo("495860000", "495860000", true, IMedia.DRM_TYPE_INTERTRUST, 0, null);
    public static final IMedia MEDIA_INTERTRUST2 = new SdkVideo("495147300", "495147300", false, IMedia.DRM_TYPE_INTERTRUST, 0, null);
    public static final IMedia MEDIA_INTERTRUST3 = new SdkVideo("180919201", "510994700", false, IMedia.DRM_TYPE_INTERTRUST, 0, null);
    private int mIndex;
    private List<IMedia> mPlaylist = new ArrayList<IMedia>();
    private AtomicBoolean mInitialized = new AtomicBoolean(false);

    private void preparePlaylist() {
        mPlaylist = new ArrayList<IMedia>();
        mPlaylist.add(MEDIA_INTERTRUST);
        mPlaylist.add(MEDIA_FRONT_TEST);
        mPlaylist.add(MEDIA_FRONT_AD);
        mPlaylist.add(MEDIA_STANDARD);
        mPlaylist.add(MEDIA_HIGH);
        mPlaylist.add(MEDIA_720P);
        mPlaylist.add(MEDIA_1080P);
        mPlaylist.add(MEDIA_4K);
        mPlaylist.add(MEDIA_4K2);
        mPlaylist.add(MEDIA_INTERTRUST);
        mPlaylist.add(MEDIA_INTERTRUST2);
        mPlaylist.add(MEDIA_AD);
        mPlaylist.add(MEDIA_VIP_MINUTE3);
        mPlaylist.add(MEDIA_VIP_MINUTE4);
        mPlaylist.add(MEDIA_VIP_MINUTE6);
        mPlaylist.add(MEDIA_VIP_MINUTE7);
        mPlaylist.add(MEDIA_VIP_MINUTE8);
        mPlaylist.add(MEDIA_VIP_MINUTE9);
        mPlaylist.add(MEDIA_VIPBS);
        mPlaylist.add(MEDIA_DOLBY);
        mPlaylist.add(MEDIA_H265);
        mPlaylist.add(MEDIA_VIP_MINUTE2);
        mPlaylist.add(MEDIA_VIP_WHOLE1);
        mPlaylist.add(MEDIA_VIP_WHOLE2);
        mPlaylist.add(MEDIA_VIP_CANNOTPREVIEW1);
        mPlaylist.add(MEDIA_CAROUSEL_SERIES);
        mPlaylist.add(MEDIA_CAROUSEL_MOVIE);
        mPlaylist.add(MEDIA_CAROUSEL_DOCUMENTARY);
        mPlaylist.add(MEDIA_CAROUSEL_AGES_THEATER);
    }

    public synchronized void initialize() {
        preparePlaylist();
        mIndex = 0;
        mInitialized.set(true);
    }

    public synchronized IMedia getCurrent() {
        if (!mInitialized.get()) {
            throw new IllegalStateException("getCurrent: please invoke initialize() before using it");
        }
        if (mPlaylist.isEmpty() || mIndex < 0 || mIndex >= mPlaylist.size()) {
            return null;
        }
        return mPlaylist.get(mIndex);
    }

    public synchronized boolean moveToNext() {
        mIndex++;
        if (mIndex >= mPlaylist.size()) {
            mIndex = 0;
        }
        return mIndex < mPlaylist.size();
    }

    public synchronized void reset() {
        mIndex = 0;
        mPlaylist.clear();
        mInitialized.set(false);
    }
}
