package tv.ismar.player.media;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.core.Md5;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.player.AccessProxy;
import tv.ismar.player.view.AdImageDialog;
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
    protected byte mPlayerMode;

    protected Activity mContext;
    protected ItemEntity mItemEntity;
    protected ClipEntity mClipEntity;
    protected PlayerSync mPlayerSync;
    private Subscription mApiGetAdSubsc;

    // 视云
    protected HashMap<String, Integer> mAdIdMap = new HashMap<>();
    protected int mAdvertisementTime[];

    protected ClipEntity.Quality mQuality;
    protected List<ClipEntity.Quality> mQualities;
    protected boolean mIsPlayingAdvertisement;
    protected SurfaceView mSurfaceView;
    protected FrameLayout mContainer;

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
                fetchAdvertisement(mItemEntity, AD_MODE_ONSTART);
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
    public void release() {
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
        if (mOnDataSourceSetListener != null) {
            mOnDataSourceSetListener.onSuccess();
        }

    }

    protected String getSmartQualityUrl(ClipEntity.Quality quality) {
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
        int quality = -1;
        if (mPlayerFlag.equals(PLAYER_FLAG_SMART)) {
            quality = getQualityIndex(getCurrentQuality());
        }
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoStart(mMedia, quality, sn, speed, sid, mPlayerFlag);
    }

    protected void logVideoPlayLoading(int speed, String mediaIp, String mediaUrl) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        int quality = -1;
        if (mPlayerFlag.equals(PLAYER_FLAG_SMART)) {
            quality = getQualityIndex(getCurrentQuality());
        }
        mPlayerSync.videoPlayLoad(
                mMedia,
                quality,
                (System.currentTimeMillis() - mPlayerOpenTime),
                speed, mediaIp, sid, mediaUrl, mPlayerFlag);
    }

    protected void logVideoPlayStart(int speed, String mediaIp) {
        mPlayerSync.videoPlayStart(mMedia, getQualityIndex(getCurrentQuality()), speed, mediaIp, mPlayerFlag);
    }

    protected void logVideoPause(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlayPause(mMedia, getQualityIndex(getCurrentQuality()), speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoContinue(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlayContinue(mMedia, getQualityIndex(getCurrentQuality()), speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeek(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlaySeek(mMedia, getQualityIndex(getCurrentQuality()), speed, getCurrentPosition(), sid, mPlayerFlag);
    }

    protected void logVideoSeekComplete(int speed, String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoPlaySeekBlockend(
                mMedia,
                getQualityIndex(getCurrentQuality()),
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
                getQualityIndex(getCurrentQuality()),
                speed,
                getCurrentPosition(),
                mediaIp, sid, mPlayerFlag);
    }

    protected void logVideoExit(int speed) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoExit(
                mMedia,
                getQualityIndex(getCurrentQuality()),
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
                getQualityIndex(getCurrentQuality()), getCurrentPosition(),
                mPlayerFlag);
    }

    protected void logVideoSwitchQuality(String mediaIp) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + System.currentTimeMillis());
        mPlayerSync.videoSwitchStream(mMedia, getQualityIndex(getCurrentQuality()), "manual",
                null, sn, mediaIp, sid, mPlayerFlag);
    }

    private String initSmartQuality() {
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
            mQuality = mQualities.get(0);
            defaultQualityUrl = getSmartQualityUrl(mQuality);
        }
        return defaultQualityUrl;
    }

    private void fetchAdvertisement(final ItemEntity itemEntity, final String adPid) {
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }
        SkyService skyService = SkyService.ServiceManager.getAdService();
        mApiGetAdSubsc = skyService.fetchAdvertisement(getAdParam(itemEntity, adPid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (e.getClass() == OnlyWifiException.class) {
                            if (mOnDataSourceSetListener != null) {
                                mOnDataSourceSetListener.onFailed("Network error.");
                            }
                        } else {
                            String mediaUrl = initSmartQuality();
                            if (!Utils.isEmptyText(mediaUrl)) {
                                String[] paths = new String[]{mediaUrl};
                                setMedia(paths);
                            }

                            Log.e(TAG, "Get advertisement " + e.getMessage());
                        }
                        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
                            mApiGetAdSubsc.unsubscribe();
                        }

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        boolean isPlayingPauseAd = false;
                        boolean hasAd = false;
                        String[] paths = null;
                        try {
                            String result = responseBody.string();
                            List<AdElementEntity> adElementEntityList = getAdInfo(result, adPid);
                            if (adElementEntityList != null && !adElementEntityList.isEmpty()) {
                                if (adPid.equals(AD_MODE_ONPAUSE)) {
                                    // 视频暂停广告
                                    AdImageDialog adImageDialog = new AdImageDialog(mContext, mPlayerSync,
                                            adElementEntityList);
                                    adImageDialog.show();
                                    try {
                                        adImageDialog.show();
                                    } catch (android.view.WindowManager.BadTokenException e) {
                                        Log.i(TAG, "Pause advertisement dialog show error.");
                                        e.printStackTrace();
                                    }
                                    isPlayingPauseAd = true;
                                } else {
                                    paths = new String[adElementEntityList.size() + 1];
                                    int i = 0;
                                    for (AdElementEntity element : adElementEntityList) {
                                        if ("video".equals(element.getMedia_type())) {
                                            mAdvertisementTime[i] = element.getDuration();
                                            paths[i] = element.getMedia_url();
                                            mAdIdMap.put(paths[i], element.getMedia_id());
                                            i++;
                                        }
                                    }
                                    hasAd = true;
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        if (isPlayingPauseAd) {
                            return;
                        }
                        String mediaUrl = initSmartQuality();
                        if (!Utils.isEmptyText(mediaUrl)) {
                            if (hasAd) {
                                mIsPlayingAdvertisement = true;
                                paths[paths.length - 1] = mediaUrl;
                            } else {
                                mIsPlayingAdvertisement = false;
                                paths = new String[]{mediaUrl};
                            }
                            setMedia(paths);
                        } else {
                            if (mOnDataSourceSetListener != null) {
                                mOnDataSourceSetListener.onFailed("Media address error.");
                            }
                        }

                        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
                            mApiGetAdSubsc.unsubscribe();
                        }
                    }
                });

    }

    private List<AdElementEntity> getAdInfo(String result, String adPid) throws JSONException {
        List<AdElementEntity> adElementEntities = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(result);
        int retcode = jsonObject.getInt("retcode");
        if (retcode == 200) {
            JSONObject body = jsonObject.getJSONObject("ads");
            JSONArray arrays = body.getJSONArray(adPid);
            for (int i = 0; i < arrays.length(); i++) {
                JSONObject element = arrays.getJSONObject(i);
                AdElementEntity ad = new AdElementEntity();
                int elementRetCode = element.getInt("retcode");
                if (elementRetCode == 200) {
                    ad.setRetcode(elementRetCode);
                    ad.setRetmsg(element.getString("retmsg"));
                    ad.setTitle(element.getString("title"));
                    ad.setDescription(element.getString("description"));
                    ad.setMedia_url(element.getString("media_url"));
                    ad.setMedia_id(element.getInt("media_id"));
                    ad.setMd5(element.getString("md5"));
                    ad.setMedia_type(element.getString("media_type"));
                    ad.setSerial(element.getInt("serial"));
                    ad.setStart(element.getInt("start"));
                    ad.setEnd(element.getInt("end"));
                    ad.setDuration(element.getInt("duration"));
                    ad.setReport_url(element.getString("report_url"));
                    adElementEntities.add(ad);
                }
            }
            Collections.sort(adElementEntities, new Comparator<AdElementEntity>() {
                @Override
                public int compare(AdElementEntity lhs, AdElementEntity rhs) {
                    return rhs.getSerial() > lhs.getSerial() ? 1 : -1;
                }
            });
        }
        return adElementEntities;
    }

    private HashMap<String, Object> getAdParam(ItemEntity itemEntity, String adpid) {
        HashMap<String, Object> adParams = new HashMap<>();
        adParams.put("adpid", "['" + adpid + "']");
        adParams.put("sn", IsmartvActivator.getInstance().getSnToken());
        adParams.put("modelName", DeviceUtils.getModelName());
        adParams.put("version", String.valueOf(DeviceUtils.getVersionCode(mContext)));
        adParams.put("province", "SH");
        adParams.put("city", "SH");
        adParams.put("app", "sky");
        adParams.put("resolution", DeviceUtils.getDisplayPixelWidth(mContext) + "," + DeviceUtils.getDisplayPixelHeight(mContext));
        adParams.put("dpi", String.valueOf(DeviceUtils.getDensity(mContext)));

        StringBuffer directorsBuffer = new StringBuffer();
        StringBuffer actorsBuffer = new StringBuffer();
        StringBuffer genresBuffer = new StringBuffer();
        ItemEntity.Attributes attributes = itemEntity.getAttributes();
        if (attributes != null) {
            String[][] directors = attributes.getDirector();
            String[][] actors = attributes.getActor();
            String[][] genres = attributes.getGenre();
            if (directors != null) {
                for (int i = 0; i < directors.length; i++) {
                    if (i == 0)
                        directorsBuffer.append("[");
                    directorsBuffer.append(directors[i][0]);
                    if (i >= 0 && i != directors.length - 1)
                        directorsBuffer.append(",");
                    if (i == directors.length - 1)
                        directorsBuffer.append("]");
                }
            }
            if (actors != null) {
                for (int i = 0; i < actors.length; i++) {
                    if (i == 0)
                        actorsBuffer.append("[");
                    actorsBuffer.append(actors[i][0]);
                    if (i >= 0 && i != actors.length - 1)
                        actorsBuffer.append(",");
                    if (i == actors.length - 1)
                        actorsBuffer.append("]");
                }
            }
            if (genres != null) {
                for (int i = 0; i < genres.length; i++) {
                    if (i == 0)
                        genresBuffer.append("[");
                    genresBuffer.append(genres[i][0]);
                    if (i >= 0 && i != genres.length - 1)
                        genresBuffer.append(",");
                    if (i == genres.length - 1)
                        genresBuffer.append("]");
                }
            }

        }
        adParams.put("channel", "");
        adParams.put("section", "");
        adParams.put("itemid", itemEntity.getItemPk());
        adParams.put("topic", "");
        adParams.put("source", "list");//fromPage
        adParams.put("content_model", itemEntity.getContentModel());
        adParams.put("director", directorsBuffer.toString());
        adParams.put("actor", actorsBuffer.toString());
        adParams.put("genre", genresBuffer.toString());
        adParams.put("clipid", itemEntity.getClip().getPk());
        adParams.put("length", Integer.valueOf(itemEntity.getClip().getLength()));
        adParams.put("live_video", itemEntity.getLiveVideo());
        String vendor = itemEntity.getVendor();
        if (Utils.isEmptyText(vendor)) {
            adParams.put("vendor", "");
        } else {
            adParams.put("vendor", Base64.encodeToString(vendor.getBytes(), Base64.URL_SAFE));
        }
        ItemEntity.Expense expense = itemEntity.getExpense();
        if (expense == null) {
            adParams.put("expense", false);
        } else {
            adParams.put("expense", true);
        }
        return adParams;
    }

}
