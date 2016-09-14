package tv.ismar.player.media;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.core.Md5;
import tv.ismar.app.VodApplication;
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

    public static final String AD_MODE_ONSTART = "qiantiepian";
    public static final String AD_MODE_ONPAUSE = "zanting";
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_COMPLETED = 5;
    public static final int STATE_BUFFERING = 6;
    protected int mCurrentState = STATE_IDLE;
    protected static byte mPlayerMode;

    protected Activity mContext;
    protected ItemEntity mItemEntity;
    protected ClipEntity mClipEntity;
    protected PlayerSync mPlayerSync;

    protected int mCurrentQuality;
    protected boolean mIsPlayingAdvertisement;
    protected SurfaceView mSurfaceView;
    protected FrameLayout mContainer;

    private ClipEntity.Quality mQuality;
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

    @Override
    public void setDataSource(ClipEntity clipEntity, OnDataSourceSetListener onDataSourceSetListener) {
        if (clipEntity == null || mPlayerMode == 0) {
            throw new IllegalArgumentException("IsmartvPlayer setDataSource invalidate.");
        }
        mMedia = new IsmartvMedia(mItemEntity.getItemPk(), mItemEntity.getPk());
        mPlayerOpenTime = System.currentTimeMillis();
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
                String mediaUrl = getInitQuality();
                String[] paths = new String[]{mediaUrl};
                setMedia(paths);
                break;
            case PlayerBuilder.MODE_QIYI_PLAYER:
                // 片源为爱奇艺
                mPlayerFlag = PLAYER_FLAG_QIYI;

                mClipEntity.setIqiyi_4_0(clipEntity.getIqiyi_4_0());
                mClipEntity.setIs_vip(clipEntity.is_vip());

                if (isQiyiSdkInit) {
                    String[] array = mClipEntity.getIqiyi_4_0().split(":");
                    SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], mClipEntity.is_vip());
                    setMedia(qiyiInfo);
                    return;
                }
                // 初始化奇艺播放器,放在此处原因在于accessToken会发生变化,初始化成功后加载视频
                Parameter extraParams = new Parameter();
                //debug code
                final long time = System.currentTimeMillis();
                extraParams.setInitPlayerSdkAfter(0);  //SDK初始化在调用initialize之后delay一定时间开始执行, 单位为毫秒.
                extraParams.setCustomerAppVersion(String.valueOf(DeviceUtils.getVersionCode(mContext)));      //传入客户App版本号
                extraParams.setDeviceId(IsmartvActivator.getInstance().getSnToken());   //传入deviceId, VIP项目必传, 登录和鉴权使用
                extraParams.setDeviceInfo(DeviceUtils.getModelName());
                PlayerSdk.getInstance().initialize(mContext, extraParams,
                        new PlayerSdk.OnInitializedListener() {
                            @Override
                            public void onSuccess() {
                                isQiyiSdkInit = true;
                                Log.i(TAG, "QiYiSdk init success:" + (System.currentTimeMillis() - time));
                                String[] array = mClipEntity.getIqiyi_4_0().split(":");
                                SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], mClipEntity.is_vip());
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
    public void prepareAsync() {
    }

    @Override
    public void start() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void seekTo(int position) {
    }

    @Override
    public void release() {
        isQiyiSdkInit = false;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getAdCountDownTime() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
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
        if (mOnDataSourceSetListener != null) {
            mOnDataSourceSetListener.onSuccess();
        }

    }

    protected String getInitQuality() {
        if (mClipEntity == null) {
            return null;
        }
        if (mQuality != null) {
            return setQuality(mQuality);
        }
        String[] urls = new String[8];
        urls[0] = mClipEntity.getLow();
        if (!TextUtils.isEmpty(urls[0])) {
            mCurrentQuality = 0;
            mQuality = ClipEntity.Quality.QUALITY_LOW;
            return urls[0];
        }
        urls[1] = mClipEntity.getAdaptive();
        if (!TextUtils.isEmpty(urls[1])) {
            mCurrentQuality = 1;
            mQuality = ClipEntity.Quality.QUALITY_ADAPTIVE;
            return urls[1];
        }
        urls[2] = mClipEntity.getNormal();
        if (!TextUtils.isEmpty(urls[2])) {
            mCurrentQuality = 2;
            mQuality = ClipEntity.Quality.QUALITY_NORMAL;
            return urls[2];
        }
        urls[3] = mClipEntity.getMedium();
        if (!TextUtils.isEmpty(urls[3])) {
            mCurrentQuality = 3;
            mQuality = ClipEntity.Quality.QUALITY_MEDIUM;
            return urls[3];
        }
        urls[4] = mClipEntity.getHigh();
        if (!TextUtils.isEmpty(urls[4])) {
            mCurrentQuality = 4;
            mQuality = ClipEntity.Quality.QUALITY_HIGH;
            return urls[4];
        }
        urls[5] = mClipEntity.getUltra();
        if (!TextUtils.isEmpty(urls[5])) {
            mCurrentQuality = 5;
            mQuality = ClipEntity.Quality.QUALITY_ULTRA;
            return urls[5];
        }
        urls[6] = mClipEntity.getBlueray();
        if (!TextUtils.isEmpty(urls[6])) {
            mCurrentQuality = 6;
            mQuality = ClipEntity.Quality.QUALITY_BLUERAY;
            return urls[6];
        }
        urls[7] = mClipEntity.get_4k();
        if (!TextUtils.isEmpty(urls[7])) {
            mCurrentQuality = 7;
            mQuality = ClipEntity.Quality.QUALITY_4K;
            return urls[7];
        }
        return null;
    }

    private String setQuality(ClipEntity.Quality quality) {
        if (mIsPlayingAdvertisement) {
            if (VodApplication.DEBUG) {
                Log.e(TAG, "Can't change quality when playing advertisement.");
            }
            return null;
        }
        String qualityUrl = null;
        switch (quality) {
            case QUALITY_LOW:
                qualityUrl = mClipEntity.getLow();
                mCurrentQuality = 0;
                break;
            case QUALITY_ADAPTIVE:
                qualityUrl = mClipEntity.getAdaptive();
                mCurrentQuality = 1;
                break;
            case QUALITY_NORMAL:
                qualityUrl = mClipEntity.getNormal();
                mCurrentQuality = 2;
                break;
            case QUALITY_MEDIUM:
                qualityUrl = mClipEntity.getMedium();
                mCurrentQuality = 3;
                break;
            case QUALITY_HIGH:
                qualityUrl = mClipEntity.getHigh();
                mCurrentQuality = 4;
                break;
            case QUALITY_ULTRA:
                qualityUrl = mClipEntity.getUltra();
                mCurrentQuality = 5;
                break;
            case QUALITY_BLUERAY:
                qualityUrl = mClipEntity.getBlueray();
                mCurrentQuality = 6;
                break;
            case QUALITY_4K:
                qualityUrl = mClipEntity.get_4k();
                mCurrentQuality = 7;
                break;
        }
        if (!TextUtils.isEmpty(qualityUrl)) {
            mQuality = quality;
        }
        return qualityUrl;

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
                (System.currentTimeMillis() - mPlayerOpenTime),
                mediaIp,
                mediaId,
                mPlayerFlag);
    }

    protected void logAdBlockend(String mediaIp, int mediaId) {
        mPlayerSync.ad_play_blockend(
                mMedia,
                (System.currentTimeMillis() - mBufferStartTime),
                mediaIp,
                mediaId,
                mPlayerFlag);
    }

    protected void logAdExit(String mediaIp, int mediaId) {
        mPlayerSync.ad_play_exit(
                mMedia,
                (System.currentTimeMillis() - mPlayerOpenTime),
                mediaIp,
                mediaId,
                mPlayerFlag);
    }

    protected void logVideoStart(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoStart(mMedia, mCurrentQuality, sn, speed, sid, mPlayerFlag);
    }

    protected void logVideoPlayLoading(int speed, String mediaIp, String mediaUrl) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlayLoad(
                mMedia,
                mCurrentQuality,
                (System.currentTimeMillis() - mPlayerOpenTime),
                speed, mediaIp, sid, mediaUrl, mPlayerFlag);
    }

    protected void logVideoPlayStart(int speed, String mediaIp) {
        mPlayerSync.videoPlayStart(mMedia, mCurrentQuality, speed, mediaIp, mPlayerFlag);
    }

    protected void logVideoPause(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlayPause(mMedia, mCurrentQuality, speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoContinue(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlayContinue(mMedia, mCurrentQuality, speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeek(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlaySeek(mMedia, mCurrentQuality, speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeekComplete(int speed, String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlaySeekBlockend(
                mMedia,
                mCurrentQuality,
                speed,
                getCurrentPosition(),
                (System.currentTimeMillis() - mBufferStartTime),
                mediaIp, sid, mPlayerFlag);
    }

    protected void logVideoBufferEnd(int speed, String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlayBlockend(
                mMedia,
                mCurrentQuality,
                speed,
                getCurrentPosition(),
                mediaIp, sid, mPlayerFlag);
    }

    protected void logVideoExit(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoExit(
                mMedia,
                mCurrentQuality,
                speed,
                "detail",
                getCurrentPosition(),
                (System.currentTimeMillis() - mPlayerOpenTime),
                sid,
                mPlayerFlag);
    }

    protected void logVideoException(String code, int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoExcept(
                "mediaexception", code,
                mMedia, speed, sid,
                mCurrentQuality, getCurrentPosition(),
                mPlayerFlag);
    }

    protected void logVideoSwitchQuality(String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoSwitchStream(mMedia, mCurrentQuality, "manual",
                null, sn, mediaIp, sid, mPlayerFlag);
    }

}
