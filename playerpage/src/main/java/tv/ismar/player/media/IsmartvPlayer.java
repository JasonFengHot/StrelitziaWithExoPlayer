package tv.ismar.player.media;

import android.app.Activity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.ismartv.turetime.TrueTime;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.core.Md5;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.player.AccessProxy;
import tv.ismar.player.view.PlayerSync;

/**
 * Created by longhai on 16-9-12.
 */
public abstract class IsmartvPlayer implements IPlayer {

    protected static final String TAG = "LH/IsmartvPlayer";

    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_COMPLETED = 5;
    public static final int STATE_BUFFERING = 6;
    protected int mCurrentState = STATE_IDLE;
    protected byte mPlayerMode;

    protected Activity mContext;
    protected ItemEntity mItemEntity;
    protected ClipEntity mClipEntity;
    protected PlayerSync mPlayerSync;

    // 视云
    protected HashMap<String, Integer> mAdIdMap = new HashMap<>();
    protected int mAdvertisementTime[];

    protected ClipEntity.Quality mQuality;
    protected List<ClipEntity.Quality> mQualities;
    protected boolean mIsPlayingAdvertisement;
    protected SurfaceView mSurfaceView;
    protected FrameLayout mContainer;
    protected int mStartPosition;

    // 奇艺播放器播放电视剧时,无需再次初始化
    private boolean isQiyiSdkInit = false;

    protected OnDataSourceSetListener mOnDataSourceSetListener;
    protected OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    protected OnBufferChangedListener mOnBufferChangedListener;
    protected OnStateChangedListener mOnStateChangedListener;

    public IsmartvPlayer(byte mode) {
        mPlayerMode = mode;
        mPlayerSync = new PlayerSync();
    }

    public byte getPlayerMode() {
        return mPlayerMode;
    }

    public void setContext(Activity context) {
        mContext = context;
    }

    public void setItemEntity(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    public void setContainer(FrameLayout container) {
        mContainer = container;
    }

    public void setStartPosition(int startPosition){
        mStartPosition = startPosition;
    }

    @Override
    public void setDataSource(ClipEntity clipEntity, ClipEntity.Quality initQuality,
                              List<AdElementEntity> adList, // 视云影片需要添加是否有广告
                              OnDataSourceSetListener onDataSourceSetListener) {
        if (clipEntity == null || mPlayerMode == 0) {
            throw new IllegalArgumentException("IsmartvPlayer setDataSource invalidate.");
        }
        mMedia = new IsmartvMedia(mItemEntity.getItemPk(), mItemEntity.getPk());
        mPlayerOpenTime = TrueTime.now().getTime();
        mClipEntity = new ClipEntity();
        mOnDataSourceSetListener = onDataSourceSetListener;
        switch (mPlayerMode) {
            case PlayerBuilder.MODE_SMART_PLAYER:
                // 片源为视云
                mPlayerFlag = PLAYER_FLAG_SMART;

                String adaptive = clipEntity.getAdaptive();
                String normal = clipEntity.getNormal();
                String medium = clipEntity.getMedium();
                String high = clipEntity.getHigh();
                String ultra = clipEntity.getUltra();
                String blueray = clipEntity.getBlueray();
                String _4k = clipEntity.get_4k();
                if (!Utils.isEmptyText(adaptive)) {
                    mClipEntity.setAdaptive(AccessProxy.AESDecrypt(adaptive, IsmartvActivator.getInstance().getDeviceToken()));
                }
                if (!Utils.isEmptyText(normal)) {
                    mClipEntity.setNormal(AccessProxy.AESDecrypt(normal, IsmartvActivator.getInstance().getDeviceToken()));
                }
                if (!Utils.isEmptyText(medium)) {
                    mClipEntity.setMedium(AccessProxy.AESDecrypt(medium, IsmartvActivator.getInstance().getDeviceToken()));
                }
                if (!Utils.isEmptyText(high)) {
                    mClipEntity.setHigh(AccessProxy.AESDecrypt(high, IsmartvActivator.getInstance().getDeviceToken()));
                }
                if (!Utils.isEmptyText(ultra)) {
                    mClipEntity.setUltra(AccessProxy.AESDecrypt(ultra, IsmartvActivator.getInstance().getDeviceToken()));
                }
                if (!Utils.isEmptyText(blueray)) {
                    mClipEntity.setBlueray(AccessProxy.AESDecrypt(blueray, IsmartvActivator.getInstance().getDeviceToken()));
                }
                if (!Utils.isEmptyText(_4k)) {
                    mClipEntity.set_4k(AccessProxy.AESDecrypt(_4k, IsmartvActivator.getInstance().getDeviceToken()));
                }
//                Log.d(TAG, mClipEntity.toString());

                String mediaUrl = initSmartQuality(initQuality);
                if (!Utils.isEmptyText(mediaUrl)) {
                    String[] paths;
                    if (adList != null && !adList.isEmpty()) {
                        paths = new String[adList.size() + 1];
                        mAdvertisementTime = new int[adList.size()];
                        int i = 0;
                        for (AdElementEntity element : adList) {
                            if ("video".equals(element.getMedia_type())) {
                                mAdvertisementTime[i] = element.getDuration();
                                paths[i] = element.getMedia_url();
                                mAdIdMap.put(paths[i], element.getMedia_id());
                                i++;
                            }
                        }
                        paths[paths.length - 1] = mediaUrl;
                        mIsPlayingAdvertisement = true;
                    } else {
                        paths = new String[]{mediaUrl};
                        mIsPlayingAdvertisement = false;
                    }
                    setMedia(paths);
                } else {
                    if (mOnDataSourceSetListener != null) {
                        mOnDataSourceSetListener.onFailed("Media address error.");
                    }
                }
                break;
            case PlayerBuilder.MODE_QIYI_PLAYER:
                // 片源为爱奇艺
                mPlayerFlag = PLAYER_FLAG_QIYI;

                mClipEntity.setIqiyi_4_0(clipEntity.getIqiyi_4_0());
                mClipEntity.setIs_vip(clipEntity.is_vip());

                if (isQiyiSdkInit) {
                    String[] array = mClipEntity.getIqiyi_4_0().split(":");
                    SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], mClipEntity.is_vip(), mStartPosition);
                    setMedia(qiyiInfo);
                    return;
                }
                // 初始化奇艺播放器,放在此处原因在于accessToken会发生变化,初始化成功后加载视频
                Parameter extraParams = new Parameter();
                //debug code
                final long time = TrueTime.now().getTime();
                extraParams.setInitPlayerSdkAfter(0);  //SDK初始化在调用initialize之后delay一定时间开始执行, 单位为毫秒.
                extraParams.setCustomerAppVersion(String.valueOf(DeviceUtils.getVersionCode(mContext)));      //传入客户App版本号
                extraParams.setDeviceId(IsmartvActivator.getInstance().getSnToken());   //传入deviceId, VIP项目必传, 登录和鉴权使用
                extraParams.setDeviceInfo(DeviceUtils.getModelName());
                PlayerSdk.getInstance().initialize(mContext, extraParams,
                        new PlayerSdk.OnInitializedListener() {
                            @Override
                            public void onSuccess() {
                                isQiyiSdkInit = true;
                                Log.i(TAG, "QiYiSdk init success:" + (TrueTime.now().getTime() - time));
                                String[] array = mClipEntity.getIqiyi_4_0().split(":");
                                SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], mClipEntity.is_vip(),mStartPosition);
                                setMedia(qiyiInfo);
                            }

                            @Override
                            public void onFailed(int what, int extra) {
                                if (mOnDataSourceSetListener != null) {
                                    mOnDataSourceSetListener.onFailed("QiyiSdk init fail what = " + what + " extra = " + extra);
                                }
                                Toast.makeText(mContext, "QiyiSdk init fail: what=" + what + ", extra=" + extra, Toast.LENGTH_LONG).show();
                            }
                        });
                break;
        }
    }

    @Override
    public void release() {
        isQiyiSdkInit = false;
        mCurrentState = STATE_IDLE;
        mContext = null;
        mItemEntity = null;
        mClipEntity = null;
        mOnDataSourceSetListener = null;
        mOnVideoSizeChangedListener = null;
        mOnBufferChangedListener = null;
        mOnStateChangedListener = null;
        isQiyiSdkInit = false;
    }

    @Override
    public ClipEntity.Quality getCurrentQuality() {
        return mQuality;
    }

    @Override
    public List<ClipEntity.Quality> getQulities() {
        return mQualities;
    }

    @Override
    public boolean isInPlaybackState() {
        return mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING;
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        mOnVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    @Override
    public void setOnBufferChangedListener(OnBufferChangedListener onBufferChangedListener) {
        mOnBufferChangedListener = onBufferChangedListener;
    }

    @Override
    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        mOnStateChangedListener = onStateChangedListener;
    }

    // 调用视云播放器
    protected void setMedia(String[] urls) {
        mCurrentState = STATE_IDLE;
    }

    // 调用奇艺播放器
    protected void setMedia(IMedia media) {
        mCurrentState = STATE_IDLE;
    }

    protected String getSmartQualityUrl(ClipEntity.Quality quality) {
        if (quality == null) {
            return "";
        }
        String qualityUrl = null;
        switch (quality) {
            case QUALITY_LOW:
                return mClipEntity.getLow();
            case QUALITY_ADAPTIVE:
                return mClipEntity.getAdaptive();
            case QUALITY_NORMAL:
                return mClipEntity.getNormal();
            case QUALITY_MEDIUM:
                return mClipEntity.getMedium();
            case QUALITY_HIGH:
                return mClipEntity.getHigh();
            case QUALITY_ULTRA:
                return mClipEntity.getUltra();
            case QUALITY_BLUERAY:
                return mClipEntity.getBlueray();
            case QUALITY_4K:
                return mClipEntity.get_4k();
        }
        return qualityUrl;
    }

    private int getQualityIndex(ClipEntity.Quality quality) {
        if (quality == null) {
            return -1;
        }
        switch (quality) {
            case QUALITY_LOW:
                return 0;
            case QUALITY_ADAPTIVE:
                return 1;
            case QUALITY_NORMAL:
                return 2;
            case QUALITY_MEDIUM:
                return 3;
            case QUALITY_HIGH:
                return 4;
            case QUALITY_ULTRA:
                return 5;
            case QUALITY_BLUERAY:
                return 6;
            case QUALITY_4K:
                return 7;
            default:
                return -1;
        }
    }

    // 日志上报相关
    protected static final String PLAYER_FLAG_SMART = "bestv";
    protected static final String PLAYER_FLAG_QIYI = "qiyi";
    private String mPlayerFlag;
    private IsmartvMedia mMedia;
    private long mPlayerOpenTime = 0;
    protected long mBufferStartTime;
    protected boolean mFirstOpen = true; // 进入播放器缓冲结束

    protected void logAdStart(String mediaIp, int mediaId) {
        // 播放广告
        mPlayerSync.ad_play_load(
                mMedia,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                mediaIp,
                mediaId,
                mPlayerFlag);
    }

    protected void logAdBlockend(String mediaIp, int mediaId) {
        mPlayerSync.ad_play_blockend(
                mMedia,
                (TrueTime.now().getTime() - mBufferStartTime),
                mediaIp,
                mediaId,
                mPlayerFlag);
    }

    protected void logAdExit(String mediaIp, int mediaId) {
        mPlayerSync.ad_play_exit(
                mMedia,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                mediaIp,
                mediaId,
                mPlayerFlag);
    }

    protected void logVideoStart(int speed) {
        int quality = -1;
        if (mPlayerFlag.equals(PLAYER_FLAG_SMART)) {
            quality = getQualityIndex(getCurrentQuality());
        }
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoStart(mMedia, quality, sn, speed, sid, mPlayerFlag);
    }

    protected void logVideoPlayLoading(int speed, String mediaIp, String mediaUrl) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        int quality = -1;
        if (mPlayerFlag.equals(PLAYER_FLAG_SMART)) {
            quality = getQualityIndex(getCurrentQuality());
        }
        mPlayerSync.videoPlayLoad(
                mMedia,
                quality,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                speed, mediaIp, sid, mediaUrl, mPlayerFlag);
    }

    protected void logVideoPlayStart(int speed, String mediaIp) {
        mPlayerSync.videoPlayStart(mMedia, getQualityIndex(getCurrentQuality()), speed, mediaIp, mPlayerFlag);
    }

    protected void logVideoPause(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoPlayPause(mMedia, getQualityIndex(getCurrentQuality()), speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoContinue(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoPlayContinue(mMedia, getQualityIndex(getCurrentQuality()), speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeek(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoPlaySeek(mMedia, getQualityIndex(getCurrentQuality()), speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeekComplete(int speed, String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoPlaySeekBlockend(
                mMedia,
                getQualityIndex(getCurrentQuality()),
                speed,
                getCurrentPosition(),
                (TrueTime.now().getTime() - mBufferStartTime),
                mediaIp, sid, mPlayerFlag);
    }

    protected void logVideoBufferEnd(int speed, String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoPlayBlockend(
                mMedia,
                getQualityIndex(getCurrentQuality()),
                speed,
                getCurrentPosition(),
                mediaIp, sid, mPlayerFlag);
    }

    protected void logVideoExit(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoExit(
                mMedia,
                getQualityIndex(getCurrentQuality()),
                speed,
                "detail",
                getCurrentPosition(),
                (TrueTime.now().getTime() - mPlayerOpenTime),
                sid,
                mPlayerFlag);
    }

    protected void logVideoException(String code, int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoExcept(
                "mediaexception", code,
                mMedia, speed, sid,
                getQualityIndex(getCurrentQuality()), getCurrentPosition(),
                mPlayerFlag);
    }

    protected void logVideoSwitchQuality(String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mPlayerSync.videoSwitchStream(mMedia, getQualityIndex(getCurrentQuality()), "manual",
                null, sn, mediaIp, sid, mPlayerFlag);
    }

    private String initSmartQuality(ClipEntity.Quality initQuality) {
        if (mClipEntity == null) {
            return null;
        }
        String defaultQualityUrl = null;
        mQualities = new ArrayList<>();
        String low = mClipEntity.getLow();
        if (!Utils.isEmptyText(low)) {
            mQualities.add(ClipEntity.Quality.QUALITY_LOW);
        }
        String adaptive = mClipEntity.getAdaptive();
        if (!Utils.isEmptyText(adaptive)) {
            mQualities.add(ClipEntity.Quality.QUALITY_ADAPTIVE);
        }
        String normal = mClipEntity.getNormal();
        if (!Utils.isEmptyText(normal)) {
            mQualities.add(ClipEntity.Quality.QUALITY_NORMAL);
        }
        String medium = mClipEntity.getMedium();
        if (!Utils.isEmptyText(medium)) {
            mQualities.add(ClipEntity.Quality.QUALITY_MEDIUM);
        }
        String high = mClipEntity.getHigh();
        if (!Utils.isEmptyText(high)) {
            mQualities.add(ClipEntity.Quality.QUALITY_HIGH);
        }
        String ultra = mClipEntity.getUltra();
        if (!Utils.isEmptyText(ultra)) {
            mQualities.add(ClipEntity.Quality.QUALITY_ULTRA);
        }
        String blueray = mClipEntity.getBlueray();
        if (!Utils.isEmptyText(blueray)) {
            mQualities.add(ClipEntity.Quality.QUALITY_BLUERAY);
        }
        String _4k = mClipEntity.get_4k();
        if (!Utils.isEmptyText(_4k)) {
            mQualities.add(ClipEntity.Quality.QUALITY_4K);
        }
        if (!mQualities.isEmpty()) {
            if (initQuality != null) {
                mQuality = initQuality;
            } else {
                mQuality = mQualities.get(0);
            }
            defaultQualityUrl = getSmartQualityUrl(mQuality);
            if (Utils.isEmptyText(defaultQualityUrl)) {
                Log.i(TAG, "Get init quality error, use default quality.");
                defaultQualityUrl = getSmartQualityUrl(mQualities.get(0));
            }
        }
        return defaultQualityUrl;
    }

    public boolean isPlayingAd() {
        return mIsPlayingAdvertisement;
    }

    protected int[] computeVideoSize(int videoWidth, int videoHeight) {
        if (mContext == null) {
            return null;
        }
        int[] size = new int[2];
        int screenWidth = DeviceUtils.getDisplayPixelWidth(mContext);
        int screenHeight = DeviceUtils.getDisplayPixelHeight(mContext);
        double dw = screenWidth;
        double dh = screenHeight;
        if (videoWidth == videoHeight) {
            if (dw > dh) {
                dw = screenHeight;
            } else {
                dh = screenWidth;
            }
        } else {
            double dar = dw / dh;
            double ar = videoWidth / videoHeight;
            if (dar < ar) {
                double widthScale = videoWidth / dw;
                dh = videoHeight / widthScale;
            } else {
                double heightScale = videoHeight / dh;
                dw = videoWidth / heightScale;
            }
        }
        size[0] = (int) Math.ceil(dw);
        size[1] = (int) Math.ceil(dh);
        return size;
    }

}
