package tv.ismar.player.media;
import cn.ismartv.truetime.TrueTime;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;
import com.qiyi.tvapi.type.DrmType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.core.Md5;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.reporter.IsmartvMedia;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.player.AccessProxy;

/**
 * Created by longhai on 16-9-12.
 */
public abstract class IsmartvPlayer implements IPlayer {

    protected static final String TAG = "LH/IsmartvPlayer";
    protected byte mPlayerMode;
    private int mDrmType;

    protected Activity mContext;
    protected ItemEntity mItemEntity;
    protected ClipEntity mClipEntity;
    protected CallaPlay mCallPlay = new CallaPlay();
    public String mCurrentMediaUrl;

    // 日志上报相关
    public int mSpeed = 0;
    public String mMediaIp = "";
    public int mMediaId = 0;

    // 视云
    protected HashMap<String, Integer> mAdIdMap = new HashMap<>();
    protected int mAdvertisementTime[];

    protected ClipEntity.Quality mQuality;
    protected List<ClipEntity.Quality> mQualities;
    protected boolean mIsPlayingAdvertisement;
    protected FrameLayout mContainer;
    protected DaisyVideoView mDaisyVideoView;
    protected int mStartPosition;
    protected boolean mIsPreview;

    // 奇艺播放器播放电视剧时,无需再次初始化
    private boolean isQiyiSdkInit = false;
    private String mUser;

    protected IPlayer.OnDataSourceSetListener mOnDataSourceSetListener;
    protected IPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    protected IPlayer.OnBufferChangedListener mOnBufferChangedListener;
    protected IPlayer.OnStateChangedListener mOnStateChangedListener;
    protected IPlayer.OnInfoListener mOnInfoListener;

    public IsmartvPlayer(byte mode) {
        mPlayerMode = mode;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public byte getPlayerMode() {
        return mPlayerMode;
    }

    public boolean isDownloadError(){
        return false;
    }

    public void setContext(Activity context) {
        mContext = context;
    }

    public void setItemEntity(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
    }

    public void setContainer(FrameLayout container) {
        mContainer = container;
    }

    public void setDaisyVideoView(DaisyVideoView daisyVideoView) {
        mDaisyVideoView = daisyVideoView;
    }

    public void setStartPosition(int startPosition) {
        mStartPosition = startPosition;
    }

    public void setIsPreview(boolean isPreview) {
        this.mIsPreview = isPreview;
    }

    public void bufferOnSharpS3Release(){

    }

    @Override
    public void setDataSource(IsmartvMedia media, ClipEntity clipEntity, ClipEntity.Quality initQuality,
                              List<AdElementEntity> adList, // 视云影片需要添加是否有广告
                              OnDataSourceSetListener onDataSourceSetListener) {
        if (clipEntity == null || mPlayerMode == 0) {
            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            mCallPlay.videoExcept(
                    "noplayaddress", "noplayaddress",
                    mMedia, mSpeed, sid,
                    getQualityIndex(getCurrentQuality()), getCurrentPosition(),
                    mPlayerFlag);
            throw new IllegalArgumentException("IsmartvPlayer setDataSource invalidate.");
        }
        mOnDataSourceSetListener = onDataSourceSetListener;
        if(media == null){
            mMedia = new IsmartvMedia(mItemEntity.getItemPk(), mItemEntity.getPk());
            Log.e(TAG, "IsmartvMedia null, it's used to report log.");
        } else {
            mMedia = media;
        }
        mPlayerOpenTime = TrueTime.now().getTime();

        switch (mPlayerMode) {
            case PlayerBuilder.MODE_SMART_PLAYER:
                // 片源为视云
                mPlayerFlag = PLAYER_FLAG_SMART;
                mClipEntity = new ClipEntity();

//                String adaptive = clipEntity.getAdaptive();
                String normal = clipEntity.getNormal();
                String medium = clipEntity.getMedium();
                String high = clipEntity.getHigh();
                String ultra = clipEntity.getUltra();
                String blueray = clipEntity.getBlueray();
                String _4k = clipEntity.get_4k();
//                if (!Utils.isEmptyText(adaptive)) {
//                    mClipEntity.setAdaptive(AccessProxy.AESDecrypt(adaptive, IsmartvActivator.getInstance().getDeviceToken()));
//                }
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
//            case PlayerBuilder.MODE_PRELOAD_PLAYER:
//                mPlayerFlag = PLAYER_FLAG_SMART;
//                mClipEntity = clipEntity;
//                initSmartQuality(initQuality);
//                setMedia(new String[1]);
//                break;
            case PlayerBuilder.MODE_QIYI_PLAYER:
                // 片源为爱奇艺
                mPlayerFlag = PLAYER_FLAG_QIYI;
                mClipEntity = new ClipEntity();

                mClipEntity.setIqiyi_4_0(clipEntity.getIqiyi_4_0());
                Log.d(TAG, "setIqiyi_4_0: " + mClipEntity.getIqiyi_4_0());
                mClipEntity.setIs_vip(clipEntity.is_vip());

                mDrmType = DrmType.DRM_NONE;
                if (mClipEntity.is_drm()) {
                    mDrmType = DrmType.DRM_INTERTRUST;
                }
                if (isQiyiSdkInit) {
                    String[] array = mClipEntity.getIqiyi_4_0().split(":");
                    SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], mClipEntity.is_vip(), mDrmType, mStartPosition, null);
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
                extraParams.addAdsHint(Parameter.HINT_TYPE_SKIP_AD, "下"); // 跳过悦享看广告
                extraParams.addAdsHint(Parameter.HINT_TYPE_HIDE_PAUSE_AD, "下"); // 跳过暂停广告
                extraParams.addAdsHint(Parameter.HINT_TYPE_SHOW_CLICK_THROUGH_AD, "右"); // 前贴,中插广告跳转页面
                PlayerSdk.getInstance().initialize(mContext, extraParams,
                        new PlayerSdk.OnInitializedListener() {
                            @Override
                            public void onSuccess() {
                                String zdevice_token = IsmartvActivator.getInstance().getZDeviceToken();
                                String zuser_token = IsmartvActivator.getInstance().getZUserToken();
                                // 先判断是否为付费影片，如果是付费则判断mUser
                                if(mItemEntity.getExpense() != null && !TextUtils.isEmpty(mUser)){
                                    if (mUser.equals("device")) {
                                        PlayerSdk.getInstance().login(zdevice_token);
                                    } else if (mUser.equals("account")) {
                                        PlayerSdk.getInstance().login(zuser_token);
                                    }
                                } else {
                                    if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken()) && !Utils.isEmptyText(zuser_token)) {
                                        PlayerSdk.getInstance().login(zuser_token);
                                    } else {
                                        PlayerSdk.getInstance().login(zdevice_token);
                                    }
                                }

                                if (mClipEntity != null) {
                                    // 此为异步回调
                                    isQiyiSdkInit = true;
                                    Log.i(TAG, "QiYiSdk init success:" + (TrueTime.now().getTime() - time));
                                    String[] array = mClipEntity.getIqiyi_4_0().split(":");
                                    SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], mClipEntity.is_vip(), mDrmType, mStartPosition, null);
                                    setMedia(qiyiInfo);
                                }
                            }

                            @Override
                            public void onFailed(int what, int extra) {
                                if (mOnDataSourceSetListener != null) {
                                    mOnDataSourceSetListener.onFailed("QiyiSdk init fail what = " + what + " extra = " + extra);
                                }
                            }
                        });
                break;
        }
    }

    @Override
    public void prepareAsync() {
    }

    public void stopPlayBack() {
        isQiyiSdkInit = false;
        mContext = null;
        mItemEntity = null;
        mClipEntity = null;
        mOnDataSourceSetListener = null;
        mOnVideoSizeChangedListener = null;
        mOnBufferChangedListener = null;
        mOnStateChangedListener = null;
        mOnInfoListener = null;
        isQiyiSdkInit = false;
        mDaisyVideoView = null;
        mContainer = null;

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

    @Override
    public void setOnInfoListener(OnInfoListener onInfoListener) {
        mOnInfoListener = onInfoListener;
    }

    // 调用视云播放器
    protected void setMedia(String[] urls) {
    }

    // 调用奇艺播放器
    protected void setMedia(IMedia media) {
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

    public IAdController getAdController() {
        return null;
    }

    // 日志上报相关
    protected static final String PLAYER_FLAG_SMART = "bestv";
    protected static final String PLAYER_FLAG_QIYI = "qiyi";
    private String mPlayerFlag;
    private IsmartvMedia mMedia;
    private long mPlayerOpenTime = 0;
    protected long mBufferStartTime;
    protected boolean mFirstOpen = true; // 进入播放器缓冲结束

    protected void logAdStart() {
        // 播放广告
        mCallPlay.ad_play_load(
                mMedia,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                mMediaIp,
                mMediaId,
                mPlayerFlag);
    }

    protected void logAdBlockend() {
        mCallPlay.ad_play_blockend(
                mMedia,
                (TrueTime.now().getTime() - mBufferStartTime),
                mMediaIp,
                mMediaId,
                mPlayerFlag);
    }

    protected void logAdExit() {
        mCallPlay.ad_play_exit(
                mMedia,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                mMediaIp,
                mMediaId,
                mPlayerFlag);
    }

    protected void logVideoStart() {
        int quality = -1;
        if (mPlayerFlag.equals(PLAYER_FLAG_SMART)) {
            quality = getQualityIndex(getCurrentQuality());
        }
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoStart(mMedia, quality, sn, mSpeed, sid, mPlayerFlag);
    }

    protected void logVideoPlayLoading(String mediaUrl) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        int quality = -1;
        if (mPlayerFlag.equals(PLAYER_FLAG_SMART)) {
            quality = getQualityIndex(getCurrentQuality());
        }
        mCallPlay.videoPlayLoad(
                mMedia,
                quality,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                mSpeed, mMediaIp, sid, mediaUrl, mPlayerFlag);
    }

    protected void logVideoPlayStart() {
        mCallPlay.videoPlayStart(mMedia, getQualityIndex(getCurrentQuality()), mSpeed, mMediaIp, mPlayerFlag);
    }

    protected void logVideoPause() {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoPlayPause(mMedia, getQualityIndex(getCurrentQuality()), mSpeed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoContinue() {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoPlayContinue(mMedia, getQualityIndex(getCurrentQuality()), mSpeed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeek() {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoPlaySeek(mMedia, getQualityIndex(getCurrentQuality()), mSpeed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeekComplete() {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoPlaySeekBlockend(
                mMedia,
                getQualityIndex(getCurrentQuality()),
                mSpeed,
                getCurrentPosition(),
                (TrueTime.now().getTime() - mBufferStartTime),
                mMediaIp, sid, mPlayerFlag);
    }

    protected void logVideoBufferEnd() {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoPlayBlockend(
                mMedia,
                getQualityIndex(getCurrentQuality()),
                mSpeed,
                getCurrentPosition(),
                mMediaIp, sid, mPlayerFlag);
    }

    public void logVideoExit(int exitPosition, String source) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoExit(
                mMedia,
                getQualityIndex(getCurrentQuality()),
                mSpeed,
                source,
                exitPosition,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                sid,
                mPlayerFlag);
    }

    protected void logVideoException(String code) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoExcept(
                "mediaexception", code,
                mMedia, mSpeed, sid,
                getQualityIndex(getCurrentQuality()), getCurrentPosition(),
                mPlayerFlag);
    }

    protected void logVideoSwitchQuality() {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        mCallPlay.videoSwitchStream(mMedia, getQualityIndex(getCurrentQuality()), "manual",
                mSpeed, sn, mMediaIp, sid, mPlayerFlag);
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
                mQuality = mQualities.get(mQualities.size() - 1);
            }
            defaultQualityUrl = getSmartQualityUrl(mQuality);
            Log.i(TAG, "initDefaultQualityUrl:" + defaultQualityUrl);
            if (Utils.isEmptyText(defaultQualityUrl)) {
                Log.i(TAG, "Get init quality error, use default quality.");
                defaultQualityUrl = getSmartQualityUrl(mQualities.get(mQualities.size() - 1));
                mQuality = mQualities.get(mQualities.size() - 1);
            }
        }
        return defaultQualityUrl;
    }

    public boolean isPlayingAd() {
        return mIsPlayingAdvertisement;
    }

}
