package tv.ismar.player.media;

import android.media.AudioManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.Map;

import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.player.SmartPlayer;

/**
 * Created by longhai on 16-9-12.
 */
public class DaisyPlayer extends IsmartvPlayer implements SurfaceHolder.Callback {

    private SmartPlayer mPlayer;
    private String[] mPaths;
    private SurfaceHolder mHolder;
    private String mCurrentMediaUrl;

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
                mOnStateChangedListener.onSeekComplete();
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
                mOnStateChangedListener.onStarted();
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
                mOnStateChangedListener.onPaused();
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
        return 0;
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mPlayer.getDuration();
        }
        return 0;
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

    @Override
    public void switchQuality(ClipEntity.Quality quality) {
        String mediaUrl = getQualityUrl(quality);
        if (!Utils.isEmptyText(mediaUrl)) {
            String[] paths = new String[]{mediaUrl};
            setMedia(paths);
        }
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

    private String getQualityUrl(ClipEntity.Quality quality) {
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
}
