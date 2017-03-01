package tv.ismar.player.media;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.core.Md5;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.reporter.IsmartvMedia;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.player.SmartPlayer;

/**
 * Created by beaver on 16-12-20.
 */

public class DaisyVideoView extends SurfaceView {
    private String TAG = "LH/DaisyVideoView";
    private String[] paths;
    private int mDuration;

    private final String ERROR_DEFAULT_MSG = "播放器错误";
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
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

    private IPlayer.OnDataSourceSetListener mOnDataSourceSetListener;
    private IPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private IPlayer.OnBufferChangedListener mOnBufferChangedListener;
    private IPlayer.OnStateChangedListener mOnStateChangedListener;
    private IPlayer.OnInfoListener mOnInfoListener;
    private boolean mIsPlayingAdvertisement;
    private int mStartPosition;

    // log
    private IsmartvMedia mLogMedia;
    private static final String PLAYER_FLAG_SMART = "bestv";
    // 日志上报相关
    private int mSpeed = 0;
    private String mMediaIp = "";
    private int mMediaId = 0;
    private long mPlayerOpenTime = 0;
    private long mBufferStartTime;
    private boolean mFirstOpen = true; // 进入播放器缓冲结束

    public boolean ismIsPlayingAdvertisement() {
        return mIsPlayingAdvertisement;
    }

    public void setmOnDataSourceSetListener(IPlayer.OnDataSourceSetListener mOnDataSourceSetListener) {
        this.mOnDataSourceSetListener = mOnDataSourceSetListener;
    }

    public void setmOnVideoSizeChangedListener(IPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener) {
        this.mOnVideoSizeChangedListener = mOnVideoSizeChangedListener;
    }

    public void setmOnBufferChangedListener(IPlayer.OnBufferChangedListener mOnBufferChangedListener) {
        this.mOnBufferChangedListener = mOnBufferChangedListener;
    }

    public void setmOnStateChangedListener(IPlayer.OnStateChangedListener mOnStateChangedListener) {
        this.mOnStateChangedListener = mOnStateChangedListener;
    }

    public void setmOnInfoListener(IPlayer.OnInfoListener mOnInfoListener) {
        this.mOnInfoListener = mOnInfoListener;
    }

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

    public void setVideoPaths(String[] paths, int startPosition, IsmartvMedia logMedia, boolean isSwitchQuality) {
        if (mFirstOpen) {
            mPlayerOpenTime = TrueTime.now().getTime();
            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.videoStart(mLogMedia, sn, mSpeed, sid, PLAYER_FLAG_SMART);
        }
        this.paths = paths;
        mLogMedia = logMedia;
        if (paths.length > 1) {
            mIsPlayingAdvertisement = true;
        } else {
            mIsPlayingAdvertisement = false;
        }
        mStartPosition = startPosition;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();

        if (isSwitchQuality) {
            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.videoSwitchStream(mLogMedia, "manual",
                    mSpeed, sn, mMediaIp, sid, PLAYER_FLAG_SMART);
        }

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
        if (paths == null || mSurfaceHolder == null) {
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

            if (mOnDataSourceSetListener != null) {
                mOnDataSourceSetListener.onSuccess();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onError(ERROR_DEFAULT_MSG);
            }
        }
    }

    public boolean isDownloadError() {
        if (player == null) {
            return false;
        }
        return player.IsDownloadError();
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
        if (isInPlaybackState() && !player.isPlaying()) {
            player.start();
            CallaPlay callaPlay = new CallaPlay();
            if (mCurrentState == STATE_PAUSED) {
                String sn = IsmartvActivator.getInstance().getSnToken();
                String sid = Md5.md5(sn + TrueTime.now().getTime());
                callaPlay.videoPlayContinue(mLogMedia, mSpeed, getCurrentPosition(), sid, PLAYER_FLAG_SMART);
            } else {
                callaPlay.videoPlayStart(mLogMedia, mSpeed, mMediaIp, PLAYER_FLAG_SMART);
            }
            mCurrentState = STATE_PLAYING;
            if (!mIsPlayingAdvertisement && mOnStateChangedListener != null) {
                mOnStateChangedListener.onStarted();
            }
        }
        mTargetState = STATE_PLAYING;

    }

    public void pause() {
        if (isInPlaybackState()) {
            if (player.isPlaying()) {
                player.pause();
                if (mCurrentState == STATE_PLAYING) {
                    CallaPlay callaPlay = new CallaPlay();
                    String sn = IsmartvActivator.getInstance().getSnToken();
                    String sid = Md5.md5(sn + TrueTime.now().getTime());
                    callaPlay.videoPlayPause(mLogMedia, mSpeed, getCurrentPosition(), sid, PLAYER_FLAG_SMART);
                }
                mCurrentState = STATE_PAUSED;
                if (mOnStateChangedListener != null) {
                    mOnStateChangedListener.onPaused();
                }

            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void stopPlayback(boolean flag) {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            PlayerBuilder.getInstance().release();
        }
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        if (flag) {
            mAdErrorListener = null;
            mOnDataSourceSetListener = null;
            mOnVideoSizeChangedListener = null;
            mOnBufferChangedListener = null;
            mOnStateChangedListener = null;
            mOnInfoListener = null;
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
            mBufferStartTime = TrueTime.now().getTime();
            mSeekWhenPrepared = 0;
            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.videoPlaySeek(mLogMedia, mSpeed, getCurrentPosition(), sid, PLAYER_FLAG_SMART);
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
            Log.i(TAG, "onPrepared state url ==" + s);
            if (mSurfaceHolder == null) {
                return;
            }
            mCurrentState = STATE_PREPARED;
            player = smartPlayer;
            mCurrentMediaUrl = s;
            mMediaIp = getMediaIp(mCurrentMediaUrl);

            long delayTime = 0;
            if (mStartPosition > 0 && !mIsPlayingAdvertisement) {
                player.seekTo(mStartPosition);
                delayTime = 500;
                isFirstSeek = true;
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mSurfaceHolder == null) {
                        return;
                    }
                    if (mOnStateChangedListener != null) {
                        mOnStateChangedListener.onPrepared();
                    }
                    if (mIsPlayingAdvertisement && mLogMedia != null && !mLogMedia.getAdIdMap().isEmpty()) {
                        mMediaId = mLogMedia.getAdIdMap().get(mCurrentMediaUrl);
                        CallaPlay callaPlay = new CallaPlay();
                        callaPlay.ad_play_load(
                                mLogMedia,
                                (TrueTime.now().getTime() - mPlayerOpenTime),
                                mMediaIp,
                                mMediaId,
                                PLAYER_FLAG_SMART);
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
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(width, height);
            }
        }
    };

    private SmartPlayer.OnCompletionListenerUrl smartCompletionListenerUrl = new SmartPlayer.OnCompletionListenerUrl() {
        @Override
        public void onCompletion(SmartPlayer smartPlayer, String s) {
            Log.i(TAG, "onCompletion:" + s + " isPlayingAd:" + mIsPlayingAdvertisement);
            player = smartPlayer;
            int currentIndex = smartPlayer.getCurrentPlayUrl();
            Log.i(TAG, "onCompletion state url index==" + currentIndex);

            if (mIsPlayingAdvertisement && mLogMedia != null && !mLogMedia.getAdIdMap().isEmpty()) {
                mMediaIp = getMediaIp(s);
                mMediaId = mLogMedia.getAdIdMap().get(s);
                mLogMedia.getAdIdMap().remove(s);
                if (mIsPlayingAdvertisement) {
                    CallaPlay callaPlay = new CallaPlay();
                    callaPlay.ad_play_exit(
                            mLogMedia,
                            (TrueTime.now().getTime() - mPlayerOpenTime),
                            mMediaIp,
                            mMediaId,
                            PLAYER_FLAG_SMART);
                }
                if (mLogMedia.getAdIdMap().isEmpty()) {
                    if (mOnStateChangedListener != null) {
                        mOnStateChangedListener.onAdEnd();
                    }
                    mIsPlayingAdvertisement = false;
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
                            if (mOnStateChangedListener != null) {
                                mOnStateChangedListener.onError("播放器错误");
                            }
                        }
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
            Log.v(TAG, "onInfo i=" + i + "<>j=" + i1);
            if (player == null) {
                return false;
            }
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
                    CallaPlay callaPlay = new CallaPlay();
                    if (mFirstOpen) {
                        // 第一次缓冲结束，播放器开始播放
                        if (mIsPlayingAdvertisement) {
                            // 广告开始
                            if (mOnStateChangedListener != null) {
                                mOnStateChangedListener.onAdStart();
                            }
                        }
                        mFirstOpen = false;
                        String sn = IsmartvActivator.getInstance().getSnToken();
                        String sid = Md5.md5(sn + TrueTime.now().getTime());
                        callaPlay.videoPlayLoad(
                                mLogMedia,
                                (TrueTime.now().getTime() - mPlayerOpenTime),
                                mSpeed, mMediaIp, sid, mCurrentMediaUrl, PLAYER_FLAG_SMART);
                    } else if (mIsPlayingAdvertisement && mLogMedia != null && !mLogMedia.getAdIdMap().isEmpty()) {
                        callaPlay.ad_play_blockend(
                                mLogMedia,
                                (TrueTime.now().getTime() - mBufferStartTime),
                                mMediaIp, mMediaId, PLAYER_FLAG_SMART);
                    } else {
                        String sn = IsmartvActivator.getInstance().getSnToken();
                        String sid = Md5.md5(sn + TrueTime.now().getTime());
                        callaPlay.videoPlayBlockend(
                                mLogMedia,
                                mSpeed, getCurrentPosition(),
                                mMediaIp, sid, PLAYER_FLAG_SMART);
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
                String sn = IsmartvActivator.getInstance().getSnToken();
                String sid = Md5.md5(sn + TrueTime.now().getTime());
                CallaPlay callaPlay = new CallaPlay();
                callaPlay.videoPlaySeekBlockend(
                        mLogMedia,
                        mSpeed,
                        getCurrentPosition(),
                        (TrueTime.now().getTime() - mBufferStartTime),
                        mMediaIp, sid, PLAYER_FLAG_SMART);
            }
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onSeekComplete();
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
            try {
                mSpeed = Integer.parseInt(spd) / (1024 * 8);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            mMediaIp = map.get(SmartPlayer.DownLoadTsInfo.TsIpAddr);
        }
    };

    private SmartPlayer.OnM3u8IpListener smartM3u8IpListener = new SmartPlayer.OnM3u8IpListener() {
        @Override
        public void onM3u8TsInfo(SmartPlayer smartPlayer, String s) {
            if (player == null) {
                return;
            }
            mMediaIp = s;
        }
    };

    private SmartPlayer.OnErrorListener smartErrorListener = new SmartPlayer.OnErrorListener() {
        @Override
        public boolean onError(SmartPlayer smartPlayer, int i, int i1) {
            Log.i(TAG, "onError:" + i + " " + i1);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.videoExcept(
                    "mediaexception", String.valueOf(i),
                    mLogMedia, mSpeed, sid,
                    getCurrentPosition(), PLAYER_FLAG_SMART);

            if (mIsPlayingAdvertisement && mAdErrorListener != null) {
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
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onError(errorMsg);
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
            release(true);
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

    public void logVideoExit(int exitPosition, String source) {
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        CallaPlay callaPlay = new CallaPlay();
        callaPlay.videoExit(
                mLogMedia,
                mSpeed,
                source,
                exitPosition,
                (TrueTime.now().getTime() - mPlayerOpenTime),
                sid,
                PLAYER_FLAG_SMART);
    }
}
