package tv.ismar.player.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.player.SmartPlayer;

/**
 * Created by longhai on 16-9-12.
 */
public class DaisyPlayer extends IsmartvPlayer implements SurfaceHolder.Callback {

    private static final String ERROR_DEFAULT_MSG = "播放器错误";
    private SmartPlayer mPlayer;
    private String[] mPaths;
    private SurfaceHolder mHolder;
    private String mCurrentMediaUrl;

    private int mSpeed;
    private String mMediaIp;
    private static boolean isSurfaceInit;
    private SurfaceView surfaceView;

    public DaisyPlayer() {
        this(PlayerBuilder.MODE_SMART_PLAYER);
    }

    private DaisyPlayer(byte mode) {
        super(mode);
    }

    @Override
    protected void setMedia(String[] urls) {
        mPaths = urls;
        mContainer.setVisibility(View.VISIBLE);
        surfaceView = new SurfaceView(mContext);
        mContainer.addView(surfaceView);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        logVideoStart(mSpeed);
        Log.i(TAG, "setMedia:" + isSurfaceInit + " " + mHolder);
        if (isSurfaceInit && mHolder != null && mHolder.getSurface().isValid()) {
            openVideo();
            if (mOnDataSourceSetListener != null) {
                mOnDataSourceSetListener.onSuccess();
            }
        }
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
            long delayTime = 0;
            if (mStartPosition > 0 && !mIsPlayingAdvertisement) {
                mPlayer.seekTo(mStartPosition);
                delayTime = 500;
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnStateChangedListener != null) {
                        mOnStateChangedListener.onPrepared();
                    }
                    if (mIsPlayingAdvertisement && !mAdIdMap.isEmpty()) {
                        logAdStart(getMediaIp(mCurrentMediaUrl), mAdIdMap.get(mCurrentMediaUrl));
                    }
                }
            }, delayTime);

        }
    };

    private SmartPlayer.OnVideoSizeChangedListener smartVideoSizeChangedListener = new SmartPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(SmartPlayer smartPlayer, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged:" + width + " " + height);
            if (mHolder == null || mPlayer == null) {
                return;
            }
            int[] outputSize = computeVideoSize(width, height);
            Log.i(TAG, "outSize:" + Arrays.toString(outputSize));
            mHolder.setFixedSize(outputSize[0], outputSize[1]);
            mPlayer.setDisplay(mHolder);
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(width, height);
            }
        }
    };

    private SmartPlayer.OnCompletionListenerUrl smartCompletionListenerUrl = new SmartPlayer.OnCompletionListenerUrl() {
        @Override
        public void onCompletion(SmartPlayer smartPlayer, String s) {
            Log.i(TAG, "onCompletion:" + s);
            mCurrentState = STATE_COMPLETED;
            if (mIsPlayingAdvertisement && !mAdIdMap.isEmpty()) {
                int mediaId = mAdIdMap.get(s);
                mAdIdMap.remove(s);
                if (mAdIdMap.isEmpty()) {
                    if (mOnStateChangedListener != null) {
                        mOnStateChangedListener.onAdEnd();
                    }
                    logAdExit(getMediaIp(s), mediaId);
                    mIsPlayingAdvertisement = false;
                }
                int currentIndex = smartPlayer.getCurrentPlayUrl();
                try {
                    if (currentIndex >= 0 && currentIndex < mPaths.length - 1) { // 如果当前播放的为第一个影片的话，则准备播放第二个影片。
                        currentIndex++;
                        smartPlayer.playUrl(currentIndex); // 准备播放第二个影片，传入参数为1，第二个影片在数组中的下标。会再一次调用onPrepared
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "smartPlayer play next video IOException.");
                    if (mOnStateChangedListener != null) {
                        mOnStateChangedListener.onError(ERROR_DEFAULT_MSG);
                    }
                }
            } else {
                if (mOnStateChangedListener != null) {
                    mOnStateChangedListener.onCompleted();
                }
            }
        }
    };

    private SmartPlayer.OnInfoListener smartInfoListener = new SmartPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(SmartPlayer smartPlayer, int i, int i1) {
            switch (i) {
                case SmartPlayer.MEDIA_INFO_BUFFERING_START:
                case 809:
                    if (mOnBufferChangedListener != null) {
                        mOnBufferChangedListener.onBufferStart();
                    }
                    mBufferStartTime = TrueTime.now().getTime();
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
            if(smartPlayer.isPlaying()){
                smartPlayer.pause();
            }
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
            if (mPlayer != null) {
                int currentPlayIndex = mPlayer.getCurrentPlayUrl();
                if (mPaths != null && currentPlayIndex != mPaths.length - 1) {
                    if (mPlayer != null) {
                        mPlayer.stop();
                        mPlayer.reset();
                        mPlayer.release();
                        mPlayer = null;
                        mCurrentState = STATE_IDLE;
                    }
                    try {
                        Log.d(TAG, "Play ad error.");
                        String[] tempPaths = Arrays.copyOfRange(mPaths, currentPlayIndex, mPaths.length + 1);
                        setMedia(tempPaths);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    try {
//                        // TODO surface has released
//                        mPlayer.playUrl(currentPlayIndex + 1);
//                        return false;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
            String errorMsg = ERROR_DEFAULT_MSG;
            switch (i) {
                case SmartPlayer.PROXY_DOWNLOAD_M3U8_ERROR:
                    errorMsg = "视频文件下载失败";
                    break;
                case SmartPlayer.PROXY_PARSER_M3U8_ERROR:
                    errorMsg = "视频文件解析失败";
                    break;
                case MediaPlayer.MEDIA_ERROR_IO:
                    errorMsg = "网络错误";
                    break;
            }
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onError(errorMsg);
            }
            return true;
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mHolder = holder;
        if (holder != null && !isSurfaceInit) {
            if (mOnDataSourceSetListener != null) {
                openVideo();
                mOnDataSourceSetListener.onSuccess();
            }
            isSurfaceInit = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged:" + width + " " + height);
        if (mPlayer != null) {
            mPlayer.setDisplay(mHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mHolder.removeCallback(this);
        isSurfaceInit = false;
        if (isInPlaybackState() && mPlayer != null && mPlayer.isPlaying()) {
            release(true);
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

            if (!mIsPlayingAdvertisement && mOnStateChangedListener != null) {
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
    public void release(boolean flag) {
        super.release(flag);
        logVideoExit(mSpeed);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            if (flag) {
                mPlayer.release();
                mPlayer = null;
            }
            mCurrentState = STATE_IDLE;
            PlayerBuilder.getInstance().release();
        }
        if(mContainer != null){
            mContainer.removeAllViews();
            mContainer.setVisibility(View.GONE);
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
        return totalAdTime * 1000 - getCurrentPosition();
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mPlayer.isPlaying();
    }

    @Override
    public void switchQuality(ClipEntity.Quality quality) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
            mCurrentState = STATE_IDLE;
        }
        String mediaUrl = getSmartQualityUrl(quality);
        if (!Utils.isEmptyText(mediaUrl)) {
            String[] paths = new String[]{mediaUrl};
            mQuality = quality;
            setMedia(paths);
        }
    }

    private void openVideo() {
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
        mPlayer.setDisplay(mHolder);
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
