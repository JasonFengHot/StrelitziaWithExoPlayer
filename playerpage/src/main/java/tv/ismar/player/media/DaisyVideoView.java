package tv.ismar.player.media;

import cn.ismartv.truetime.TrueTime;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.player.SmartPlayer;

/**
 * Created by beaver on 16-12-20.
 */

public class DaisyVideoView extends SurfaceView {
    private String TAG = "LH/DaisyVideoView";
    private IsmartvPlayer mIsmartvPlayer;
    private String[] paths;
    private Uri mUri;
    private int mDuration;

    private final String ERROR_DEFAULT_MSG = "播放器错误";
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private String snToken;
    private SmartPlayer player = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mSeekWhenPrepared; // recording the seek position while
    private AdErrorListener mAdErrorListener;
    private boolean isFirstSeek;

    private Context mContext;

    // 日志上报相关
    private String mCurrentMediaUrl;

    public DaisyVideoView(Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public DaisyVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public DaisyVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i("@@@@", "onMeasure");
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                // Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                // Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else {
                // Log.i("@@@", "aspect ratio is correct: " +
                // width+"/"+height+"="+
                // mVideoWidth+"/"+mVideoHeight);
            }
        }
        // Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(DaisyVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(DaisyVideoView.class.getName());
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            /*
             * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:
            /*
             * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public void setVideoPaths(String[] paths, IsmartvPlayer player) {
        this.paths = paths;
        this.mIsmartvPlayer = player;
        mUri = Uri.parse(paths[paths.length - 1]);
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void playIndex(int index) {
        if (player != null) {
            try {
                player.playUrl(index);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void setSnToken(String snToken) {
        this.snToken = snToken;
    }

    private boolean isCanWriteSD() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
        release(false);
        try {
            player = new SmartPlayer();
            player.setSn(snToken);
            player.setScreenOnWhilePlaying(true);
            if (isCanWriteSD())
                player.setSDCardisAvailable(true);
            else
                player.setSDCardisAvailable(false);
            mDuration = -1;
            player.setOnPreparedListenerUrl(smartPreparedListenerUrl);
            player.setOnVideoSizeChangedListener(smartVideoSizeChangedListener);
            player.setOnSeekCompleteListener(smartSeekCompleteListener);
            player.setOnErrorListener(smartErrorListener);
            player.setOnInfoListener(smartInfoListener);
            player.setOnTsInfoListener(smartTsInfoListener);
            player.setOnM3u8IpListener(smartM3u8IpListener);
            player.setOnCompletionListenerUrl(smartCompletionListenerUrl);
            player.setDataSource(paths);
            player.setDisplay(mSurfaceHolder);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setScreenOnWhilePlaying(true);

            if (mIsmartvPlayer.mOnDataSourceSetListener != null) {
                mIsmartvPlayer.mOnDataSourceSetListener.onSuccess();
            }
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onError(ERROR_DEFAULT_MSG);
            }
        } catch (Exception ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onError(ERROR_DEFAULT_MSG);
            }
        }
    }

    public boolean isDownloadError(){
        if(player == null){
            return false;
        }
        return player.IsDownloadError();
    }

    public void bufferOnSharpS3Release(){
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        player = null;
    }

    public boolean isInPlaybackState() {
        return (player != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public void prepareAsync() {
        player.prepareAsync();
        mCurrentState = STATE_PREPARING;
    }

    public void start() {
        if (isInPlaybackState()) {
            player.start();
            if (mCurrentState == STATE_PAUSED) {
                mIsmartvPlayer.logVideoContinue();
            } else {
                mIsmartvPlayer.logVideoPlayStart();
            }
            mCurrentState = STATE_PLAYING;
            if (!mIsmartvPlayer.mIsPlayingAdvertisement && mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onStarted();
            }
        }
        mTargetState = STATE_PLAYING;

    }

    public void pause() {
        if (isInPlaybackState()) {
            if (player.isPlaying()) {
                player.pause();
                if (mCurrentState == STATE_PLAYING) {
                    mIsmartvPlayer.logVideoPause();
                }
                mCurrentState = STATE_PAUSED;
                if (mIsmartvPlayer.mOnStateChangedListener != null) {
                    mIsmartvPlayer.mOnStateChangedListener.onPaused();
                }

            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void stopPlayback() {
        if (player != null) {
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            player.stop();
            player.release();
            player = null;

        }
    }

    public void release(boolean cleartargetstate) {
        if (player != null) {
            player.reset();
            player.release();
            player = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }

            PlayerBuilder.getInstance().release();
        }
    }

    public void seekTo(final int msec) {
        if (isInPlaybackState()) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    player.seekTo(msec);
                }
            }.start();
            mSeekWhenPrepared = 0;
            mIsmartvPlayer.logVideoSeek();
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = player.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && player.isPlaying();
    }

    public int getCurrentPlayUrl() {
        return player.getCurrentPlayUrl();
    }

    private SmartPlayer.OnPreparedListenerUrl smartPreparedListenerUrl = new SmartPlayer.OnPreparedListenerUrl() {
        @Override
        public void onPrepared(SmartPlayer smartPlayer, String s) {
            Log.i(TAG, "onPrepared state url ==" + s + " " + mIsmartvPlayer.mIsPlayingAdvertisement);
            if (mSurfaceHolder == null) {
                return;
            }
            mCurrentState = STATE_PREPARED;
            player = smartPlayer;
            mCurrentMediaUrl = s;
            mIsmartvPlayer.mMediaIp = getMediaIp(mCurrentMediaUrl);

            long delayTime = 0;
            if (mIsmartvPlayer.mStartPosition > 0 && !mIsmartvPlayer.mIsPlayingAdvertisement) {
                player.seekTo(mIsmartvPlayer.mStartPosition);
                delayTime = 500;
                isFirstSeek = true;
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsmartvPlayer == null || mSurfaceHolder == null) {
                        return;
                    }
                    if (mIsmartvPlayer.mOnStateChangedListener != null) {
                        mIsmartvPlayer.mOnStateChangedListener.onPrepared();
                    }
                    if (mIsmartvPlayer.mIsPlayingAdvertisement && !mIsmartvPlayer.mAdIdMap.isEmpty()) {
                        mIsmartvPlayer.mMediaId = mIsmartvPlayer.mAdIdMap.get(mCurrentMediaUrl);
                        mIsmartvPlayer.logAdStart();
                    }
                }
            }, delayTime);

        }
    };

    private SmartPlayer.OnVideoSizeChangedListener smartVideoSizeChangedListener = new SmartPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(SmartPlayer smartPlayer, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged:" + width + " " + height);
            if (getHolder() == null || !getHolder().getSurface().isValid()) {// 视频加载中即将播放时按返回键退出
                Log.i(TAG, "surface destroyed");
                return;
            }
            mVideoWidth = smartPlayer.getVideoWidth();
            mVideoHeight = smartPlayer.getVideoHeight();
//            if (mVideoWidth != 0 && mVideoHeight != 0) {
//                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
//                requestLayout();
//            }
            int[] outputSize = computeVideoSize(width, height);
            Log.i(TAG, "outSize:" + Arrays.toString(outputSize));
            getHolder().setFixedSize(outputSize[0], outputSize[1]);
            smartPlayer.setDisplay(getHolder());
            if (mIsmartvPlayer.mOnVideoSizeChangedListener != null) {
                mIsmartvPlayer.mOnVideoSizeChangedListener.onVideoSizeChanged(width, height);
            }
        }
    };

    private SmartPlayer.OnCompletionListenerUrl smartCompletionListenerUrl = new SmartPlayer.OnCompletionListenerUrl() {
        @Override
        public void onCompletion(SmartPlayer smartPlayer, String s) {
            Log.i(TAG, "onCompletion:" + s + " isPlayingAd:" + mIsmartvPlayer.mIsPlayingAdvertisement);
            player = smartPlayer;
            int currentIndex = smartPlayer.getCurrentPlayUrl();
            Log.i(TAG, "onCompletion state url index==" + currentIndex);

            if (mIsmartvPlayer.mIsPlayingAdvertisement && !mIsmartvPlayer.mAdIdMap.isEmpty()) {
                mIsmartvPlayer.mMediaIp = getMediaIp(s);
                mIsmartvPlayer.mMediaId = mIsmartvPlayer.mAdIdMap.get(s);
                mIsmartvPlayer.mAdIdMap.remove(s);
                if (mIsmartvPlayer.mIsPlayingAdvertisement) {
                    mIsmartvPlayer.logAdExit();
                }
                if (mIsmartvPlayer.mAdIdMap.isEmpty()) {
                    if (mIsmartvPlayer.mOnStateChangedListener != null) {
                        mIsmartvPlayer.mOnStateChangedListener.onAdEnd();
                    }
                    mIsmartvPlayer.mIsPlayingAdvertisement = false;
                }
                if (currentIndex >= 0 && currentIndex < paths.length - 1) { // 如果当前播放的为第一个影片的话，则准备播放第二个影片。
                    try {
                        currentIndex++;
                        smartPlayer.playUrl(currentIndex); // 准备播放第二个影片，传入参数为1，第二个影片在数组中的下标。
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            smartPlayer.playUrl(currentIndex);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            Log.e(TAG, "smartPlayer play next video IOException.");
                            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                                mIsmartvPlayer.mOnStateChangedListener.onError("播放器错误");
                            }
                        }
                    }
                }
            } else {
                if (mIsmartvPlayer.mOnStateChangedListener != null) {
                    mIsmartvPlayer.mOnStateChangedListener.onCompleted();
                }
            }
        }
    };

    private SmartPlayer.OnInfoListener smartInfoListener = new SmartPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(SmartPlayer smartPlayer, int i, int i1) {
            Log.v(TAG, "onInfo i=" + i + "<>j=" + i1);
            if (player == null) {
                return false;
            }
            switch (i) {
                case SmartPlayer.MEDIA_INFO_BUFFERING_START:
                case 809:
                    if (mIsmartvPlayer.mOnBufferChangedListener != null) {
                        mIsmartvPlayer.mOnBufferChangedListener.onBufferStart();
                    }
                    mIsmartvPlayer.mBufferStartTime = TrueTime.now().getTime();
                    break;
                case SmartPlayer.MEDIA_INFO_BUFFERING_END:
                case 3:
                    if (mIsmartvPlayer.mOnBufferChangedListener != null) {
                        mIsmartvPlayer.mOnBufferChangedListener.onBufferEnd();
                    }
                    if (mIsmartvPlayer.mFirstOpen) {
                        // 第一次缓冲结束，播放器开始播放
                        if (mIsmartvPlayer.mIsPlayingAdvertisement) {
                            // 广告开始
                            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                                mIsmartvPlayer.mOnStateChangedListener.onAdStart();
                            }
                        }
                        mIsmartvPlayer.logVideoPlayLoading(mCurrentMediaUrl);
                        mIsmartvPlayer.mFirstOpen = false;
                    } else if (mIsmartvPlayer.mIsPlayingAdvertisement && !mIsmartvPlayer.mAdIdMap.isEmpty()) {
                        mIsmartvPlayer.logAdBlockend();
                    } else {
                        mIsmartvPlayer.logVideoBufferEnd();
                    }
                    break;
            }
            return false;
        }
    };

    private SmartPlayer.OnSeekCompleteListener smartSeekCompleteListener = new SmartPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(SmartPlayer smartPlayer) {
            if (player == null) {
                return;
            }
            if (isFirstSeek) {
                isFirstSeek = false;
                return;
            }
            if (isInPlaybackState()) {
                mIsmartvPlayer.logVideoSeekComplete();
            }
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onSeekComplete();
            }
        }
    };

    private SmartPlayer.OnTsInfoListener smartTsInfoListener = new SmartPlayer.OnTsInfoListener() {
        @Override
        public void onTsInfo(SmartPlayer smartPlayer, Map<String, String> map) {
            if (player == null) {
                return;
            }
            String spd = map.get("TsDownLoadSpeed");
            mIsmartvPlayer.mSpeed = Integer.parseInt(spd);
            mIsmartvPlayer.mSpeed = mIsmartvPlayer.mSpeed / (1024 * 8);
            mIsmartvPlayer.mMediaIp = map.get(SmartPlayer.DownLoadTsInfo.TsIpAddr);
        }
    };

    private SmartPlayer.OnM3u8IpListener smartM3u8IpListener = new SmartPlayer.OnM3u8IpListener() {
        @Override
        public void onM3u8TsInfo(SmartPlayer smartPlayer, String s) {
            if (player == null) {
                return;
            }
            mIsmartvPlayer.mMediaIp = s;
        }
    };

    private SmartPlayer.OnErrorListener smartErrorListener = new SmartPlayer.OnErrorListener() {
        @Override
        public boolean onError(SmartPlayer smartPlayer, int i, int i1) {
            Log.i(TAG, "onError:" + i + " " + i1);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mIsmartvPlayer.logVideoException(String.valueOf(i));

            if (mIsmartvPlayer.mIsPlayingAdvertisement && mAdErrorListener != null) {
                mAdErrorListener.onAdError(mCurrentMediaUrl);
                return true;
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
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onError(errorMsg);
            }
            return true;
        }
    };

    public interface AdErrorListener {

        void onAdError(String url);

    }

    public void setAdErrorListener(AdErrorListener adErrorListener) {
        mAdErrorListener = adErrorListener;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (player != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            mSurfaceHolder.setFixedSize(mSurfaceWidth, mSurfaceHeight);
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
//            release(true);
        }
    };

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
