package tv.ismar.player.media;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;
import com.qiyi.tvapi.type.DrmType;

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

    // 视云
    protected int mAdvertisementTime[];
    protected IsmartvMedia mLogMedia;

    protected FrameLayout mContainer;
    protected DaisyVideoView mDaisyVideoView; // 夏普585某些机型退出时黑屏一下问题
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

    public boolean isDownloadError() {
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

    protected String initSmartQuality(ClipEntity.Quality initQuality) {
        return null;
    }

    @Override
    public void setDataSource(IsmartvMedia media, ClipEntity clipEntity, ClipEntity.Quality initQuality,
                              List<AdElementEntity> adList, // 视云影片需要添加是否有广告
                              OnDataSourceSetListener onDataSourceSetListener) {
        if (media == null) {
            mLogMedia = new IsmartvMedia(mItemEntity.getItemPk(), mItemEntity.getPk());
            Log.e(TAG, "IsmartvMedia null, it's used to report log.");
        } else {
            mLogMedia = media;
        }
        if (clipEntity == null || mPlayerMode == 0) {
            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            String playerFlag = "bestv";
            if (mPlayerMode == PlayerBuilder.MODE_QIYI_PLAYER) {
                playerFlag = "qiyi";
            }
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.videoExcept(
                    "noplayaddress", "noplayaddress",
                    mLogMedia, 0, sid, 0, playerFlag);
            throw new IllegalArgumentException("IsmartvPlayer setDataSource invalidate.");
        }
        mOnDataSourceSetListener = onDataSourceSetListener;

        switch (mPlayerMode) {
            case PlayerBuilder.MODE_SMART_PLAYER:
                // 片源为视云
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
                        HashMap<String, Integer> adIdMap = new HashMap<>();
                        int i = 0;
                        for (AdElementEntity element : adList) {
                            if ("video".equals(element.getMedia_type())) {
                                mAdvertisementTime[i] = element.getDuration();
                                paths[i] = element.getMedia_url();
                                adIdMap.put(paths[i], element.getMedia_id());
                                i++;
                            }
                        }
                        mLogMedia.setAdIdMap(adIdMap);
                        paths[paths.length - 1] = mediaUrl;
                    } else {
                        paths = new String[]{mediaUrl};
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
                                if (mItemEntity.getExpense() != null && !TextUtils.isEmpty(mUser)) {
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
//        if (mPlayerMode == PlayerBuilder.MODE_QIYI_PLAYER) {
//            // Receiver not registered: com.qiyi.video.utils.NetWorkManager$NetWorkConnectionReceiver@44668530
//            PlayerSdk.getInstance().release();
//        }
        isQiyiSdkInit = false; // 考虑到用户登录
        mContext = null;
        mItemEntity = null;
        mClipEntity = null;
        mDaisyVideoView = null;
        mContainer = null;

        mOnDataSourceSetListener = null;
        mOnVideoSizeChangedListener = null;
        mOnBufferChangedListener = null;
        mOnStateChangedListener = null;
        mOnInfoListener = null;

    }

    @Override
    public ClipEntity.Quality getCurrentQuality() {
        return null;
    }

    @Override
    public List<ClipEntity.Quality> getQulities() {
        return null;
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
        if (mDaisyVideoView == null || mContainer == null) {
            return;
        }
        mDaisyVideoView.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.GONE);
    }

    // 调用奇艺播放器
    protected void setMedia(IMedia media) {
        if (mDaisyVideoView == null || mContainer == null) {
            return;
        }
        mDaisyVideoView.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);
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

    protected int getQualityIndex(ClipEntity.Quality quality) {
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
    public void logVideoExit(int exitPosition, String source) {
    }

}
