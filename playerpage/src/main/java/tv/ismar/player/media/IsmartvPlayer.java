package tv.ismar.player.media;

import android.app.Activity;
import android.text.TextUtils;
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
import java.util.Stack;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.VodApplication;
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

    protected static byte mPlayerMode;

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

    private Subscription mApiGetAdSubsc;
    protected ClipEntity mClipEntity;
    protected Activity mContext;
    protected ItemEntity mItemEntity;
    protected PlayerSync mPlayerSync;

    protected int mAdvertisementTime[];
    protected int mCurrentQuality;
    private ClipEntity.Quality mQuality;
    protected boolean mIsPlayingAdvertisement;
    protected SurfaceView mSurfaceView;
    protected FrameLayout mContainer;

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
        mClipEntity = new ClipEntity();
        mOnDataSourceSetListener = onDataSourceSetListener;
        switch (mPlayerMode) {
            case PlayerBuilder.MODE_SMART_PLAYER:
                // 片源为视云
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
                Log.d(TAG, mClipEntity.toString());
                //TODO 视云片源先加载广告
//                fetchAdvertisement(mItemEntity, IsmartvPlayer.AD_MODE_ONSTART);
                String mediaUrl = getInitQuality();
                mIsPlayingAdvertisement = false;
                String[] paths = new String[]{mediaUrl};
                setMedia(paths);
                break;
            case PlayerBuilder.MODE_QIYI_PLAYER:
                // 片源为爱奇艺
                mClipEntity.setIqiyi_4_0(clipEntity.getIqiyi_4_0());
                mClipEntity.setIs_vip(clipEntity.is_vip());

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
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }

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

    private void fetchAdvertisement(ItemEntity itemEntity, final String adPid) {
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }
        mApiGetAdSubsc = SkyService.ServiceManager.getService().fetchAdvertisement(getAdParam(itemEntity, adPid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mOnDataSourceSetListener != null) {
                            mOnDataSourceSetListener.onFailed(e.getMessage());
                        }
                        e.printStackTrace();
                        if (e.getClass() == OnlyWifiException.class) {
                        } else {
                        }
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        boolean isPlayingPauseAd = false;
                        boolean hasAd = false;
                        String[] paths = null;
                        HashMap<String, Integer> adlogs = new HashMap<>();
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
                                            adlogs.put(paths[i], element.getMedia_id());
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
                        String mediaUrl = getInitQuality();
                        if (hasAd) {
                            mIsPlayingAdvertisement = true;
                            paths[paths.length - 1] = mediaUrl;
                        } else {
                            paths = new String[]{mediaUrl};
                        }
                        setMedia(paths);
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

    private HashMap<String, String> getAdParam(ItemEntity itemEntity, String adpid) {
        HashMap<String, String> adParams = new HashMap<>();

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
                    directorsBuffer.append(directors[i][1]);
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
                    actorsBuffer.append(actors[i][1]);
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
                    genresBuffer.append(genres[i][1]);
                    if (i >= 0 && i != genres.length - 1)
                        genresBuffer.append(",");
                    if (i == genres.length - 1)
                        genresBuffer.append("]");
                }
            }

        }
        adParams.put("channel", "");
        adParams.put("section", "");
        adParams.put("itemid", String.valueOf(itemEntity.getItemPk()));
        adParams.put("topic", "");
        adParams.put("source", "");//fromPage
        adParams.put("content_model", itemEntity.getContentModel());
        adParams.put("director", directorsBuffer.toString());
        adParams.put("actor", actorsBuffer.toString());
        adParams.put("genre", genresBuffer.toString());
        adParams.put("clipid", String.valueOf(itemEntity.getClip().getPk()));
        adParams.put("length", itemEntity.getClip().getLength());
        adParams.put("live_video", String.valueOf(itemEntity.getLiveVideo()));
        String vendor = itemEntity.getVendor();
        if (Utils.isEmptyText(vendor)) {
            adParams.put("vendor", "");
        } else {
            adParams.put("vendor", Base64.encodeToString(vendor.getBytes(), Base64.URL_SAFE));
        }
        ItemEntity.Expense expense = itemEntity.getExpense();
        if (expense == null) {
            adParams.put("expense", "false");
        } else {
            adParams.put("expense", "true");
        }
        adParams.put("sn", "");
        adParams.put("modelName", DeviceUtils.getModelName());
        adParams.put("version", String.valueOf(DeviceUtils.getVersionCode(mContext)));
        adParams.put("province", "");
        adParams.put("city", "");
        adParams.put("app", "sky");
        adParams.put("resolution", DeviceUtils.getDisplayPixelWidth(mContext) + "," + DeviceUtils.getDisplayPixelHeight(mContext));
        adParams.put("dpi", String.valueOf(DeviceUtils.getDensity(mContext)));
        adParams.put("adpid", "['" + adpid + "']");
        return adParams;
    }

    private String getInitQuality() {
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

}
