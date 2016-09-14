package tv.ismar.player.media;

import android.media.AudioManager;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.player.SmartPlayer;
import tv.ismar.player.view.AdImageDialog;

/**
 * Created by longhai on 16-9-12.
 */
public class DaisyPlayer extends IsmartvPlayer implements SurfaceHolder.Callback {

    private SmartPlayer mPlayer;
    private String[] mPaths;
    private SurfaceHolder mHolder;
    private String mCurrentMediaUrl;

    private Subscription mApiGetAdSubsc;
    private HashMap<String, Integer> mAdIdMap = new HashMap<>();

    private int mAdvertisementTime[];
    private int mSpeed;
    private String mMediaIp;

    public DaisyPlayer() {
        this(PlayerBuilder.MODE_SMART_PLAYER);
    }

    private DaisyPlayer(byte mode) {
        super(mode);
    }

    @Override
    protected void setMedia(String[] urls) {
        mPaths = urls;
        mSurfaceView.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.GONE);
        mHolder = mSurfaceView.getHolder();//SurfaceHolder是SurfaceView的控制接口
        mHolder.addCallback(this); //因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
        logVideoStart(mSpeed);
        super.setMedia(urls);

    }

    @Override
    public void prepareAsync() {
        //调用prepareAsync, 播放器开始准备, 必须调用
        mPlayer.prepareAsync();
        mCurrentState = STATE_PREPARING;
    }

    private SmartPlayer.OnPreparedListenerUrl smartPreparedListenerUrl = new SmartPlayer.OnPreparedListenerUrl() {
        @Override
        public void onPrepared(SmartPlayer smartPlayer, String s) {
            mCurrentMediaUrl = s;
            mCurrentState = STATE_PREPARED;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onPrepared();
            }
            if (mIsPlayingAdvertisement && !mAdIdMap.isEmpty()) {
                logAdStart(getMediaIp(mCurrentMediaUrl), mAdIdMap.get(mCurrentMediaUrl));
            }
        }
    };

    private SmartPlayer.OnVideoSizeChangedListener smartVideoSizeChangedListener = new SmartPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(SmartPlayer smartPlayer, int i, int i1) {
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(i, i1);
            }
        }
    };

    private SmartPlayer.OnCompletionListenerUrl smartCompletionListenerUrl = new SmartPlayer.OnCompletionListenerUrl() {
        @Override
        public void onCompletion(SmartPlayer smartPlayer, String s) {
            mCurrentState = STATE_COMPLETED;
            if (mIsPlayingAdvertisement && !mAdIdMap.isEmpty()) {
                mAdIdMap.remove(s);
                if (mAdIdMap.isEmpty()) {
                    mIsPlayingAdvertisement = false;
                    if (mOnStateChangedListener != null) {
                        mOnStateChangedListener.onAdEnd();
                    }
                    logAdExit(getMediaIp(s), mAdIdMap.get(s));
                }
            }
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onCompleted();
            }
        }
    };

    private SmartPlayer.OnInfoListener smartInfoListener = new SmartPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(SmartPlayer smartPlayer, int i, int i1) {
            switch (i) {
                case SmartPlayer.MEDIA_INFO_BUFFERING_START:
                    if (mOnBufferChangedListener != null) {
                        mOnBufferChangedListener.onBufferStart();
                    }
                    mBufferStartTime = System.currentTimeMillis();
                    break;
                case SmartPlayer.MEDIA_INFO_BUFFERING_END:
                case 3:
                    if (mOnBufferChangedListener != null) {
                        mOnBufferChangedListener.onBufferEnd();
                    }
                    if (mFirstOpen) {
                        // 第一次缓冲结束，播放器开始播放
                        if (mIsPlayingAdvertisement) {
                            // 广告开始
                            if (mOnStateChangedListener != null) {
                                mOnStateChangedListener.onAdStart();
                            }
                        }
                        logVideoPlayLoading(mSpeed, mMediaIp, mCurrentMediaUrl);
                        logVideoPlayStart(mSpeed, mMediaIp);
                        mFirstOpen = false;
                    } else if (mIsPlayingAdvertisement && !mAdIdMap.isEmpty()) {
                        logAdBlockend(getMediaIp(mCurrentMediaUrl), mAdIdMap.get(mCurrentMediaUrl));
                    } else {
                        logVideoBufferEnd(mSpeed, mMediaIp);
                    }
                    break;
            }
            return false;
        }
    };

    private SmartPlayer.OnSeekCompleteListener smartSeekCompleteListener = new SmartPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(SmartPlayer smartPlayer) {
            if (isInPlaybackState()) {
                logVideoSeekComplete(mSpeed, mMediaIp);
            }
            if (mOnStateChangedListener != null) {
                if (!mIsPlayingAdvertisement) {
                    mOnStateChangedListener.onSeekComplete();
                }
            }
        }
    };

    private SmartPlayer.OnTsInfoListener onTsInfoListener = new SmartPlayer.OnTsInfoListener() {
        @Override
        public void onTsInfo(SmartPlayer smartPlayer, Map<String, String> map) {
            String spd = map.get("TsDownLoadSpeed");
            mSpeed = Integer.parseInt(spd);
            mSpeed = mSpeed / (1024 * 8);
            mMediaIp = map.get(SmartPlayer.DownLoadTsInfo.TsIpAddr);
        }
    };

    private SmartPlayer.OnM3u8IpListener onM3u8IpListener = new SmartPlayer.OnM3u8IpListener() {
        @Override
        public void onM3u8TsInfo(SmartPlayer smartPlayer, String s) {
            mMediaIp = s;
        }
    };

    private SmartPlayer.OnErrorListener smartErrorListener = new SmartPlayer.OnErrorListener() {
        @Override
        public boolean onError(SmartPlayer smartPlayer, int i, int i1) {
            mCurrentState = STATE_ERROR;
            Log.e(TAG, "SmartPlayer onError:" + i + " " + i1);
            logVideoException(String.valueOf(i), mSpeed);
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onError("SmartPlayer error " + i);
            }
            return false;
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder != null) {
            mPlayer = new SmartPlayer();
            mPlayer.setOnPreparedListenerUrl(smartPreparedListenerUrl);
            mPlayer.setOnVideoSizeChangedListener(smartVideoSizeChangedListener);
            mPlayer.setOnCompletionListenerUrl(smartCompletionListenerUrl);
            mPlayer.setOnInfoListener(smartInfoListener);
            mPlayer.setOnSeekCompleteListener(smartSeekCompleteListener);
            mPlayer.setOnTsInfoListener(onTsInfoListener);
            mPlayer.setOnM3u8IpListener(onM3u8IpListener);
            mPlayer.setOnErrorListener(smartErrorListener);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setScreenOnWhilePlaying(true);
            mPlayer.setDataSource(mPaths);
            mPlayer.setDisplay(holder);

            if (mOnDataSourceSetListener != null) {
                mOnDataSourceSetListener.onSuccess();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isInPlaybackState() && mPlayer != null && mPlayer.isPlaying()) {
            pause();
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState() && mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
            if (mCurrentState == STATE_PAUSED) {
                logVideoContinue(mSpeed);
            }
            mCurrentState = STATE_PLAYING;

            if (mOnStateChangedListener != null) {
                if (!mIsPlayingAdvertisement) {
                    mOnStateChangedListener.onStarted();
                }
            }
        }
    }

    @Override
    public void pause() {
        if (isInPlaybackState() && mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            logVideoPause(mSpeed);
            mCurrentState = STATE_PAUSED;

            if (mOnStateChangedListener != null) {
                if (!mIsPlayingAdvertisement) {
                    mOnStateChangedListener.onPaused();
                }
            }
        }
    }

    @Override
    public void release() {
        super.release();
        logVideoExit(mSpeed);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;

            mCurrentState = STATE_IDLE;
        }
    }

    @Override
    public void seekTo(int position) {
        mPlayer.seekTo(position);
        if (isInPlaybackState()) {
            logVideoSeek(mSpeed);
        }
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mPlayer.getCurrentPosition();
        }
        return super.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mPlayer.getDuration();
        }
        return super.getDuration();
    }

    @Override
    public int getAdCountDownTime() {
        if (mAdvertisementTime == null || !mIsPlayingAdvertisement) {
            return 0;
        }
        int totalAdTime = 0;
        int currentAd = mPlayer.getCurrentPlayUrl();
        if (currentAd == mPaths.length - 1) {
            return 0;
        } else if (currentAd == mAdvertisementTime.length - 1) {
            totalAdTime = mAdvertisementTime[mAdvertisementTime.length - 1];
        } else {
            for (int i = currentAd; i < mAdvertisementTime.length; i++) {
                totalAdTime += mAdvertisementTime[i];
            }
        }
        return totalAdTime - getCurrentPosition();
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mPlayer.isPlaying();
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
                        // TODO
                        String mediaUrl = getInitQuality();
                        if (hasAd) {
                            mIsPlayingAdvertisement = true;
                            paths[paths.length - 1] = mediaUrl;
                        } else {
                            mIsPlayingAdvertisement = false;
                            paths = new String[]{mediaUrl};
                        }
                        setMedia(paths);

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

    /**
     * 获取媒体IP
     */
    private String getMediaIp(String str) {
        String ip = "";
        String tmp = str.substring(7, str.length());
        int index = tmp.indexOf("/");
        ip = tmp.substring(0, index);
        return ip;
    }
}
