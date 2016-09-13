package tv.ismar.player.media;

import android.media.AudioManager;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import tv.ismar.player.SmartPlayer;

/**
 * Created by longhai on 16-9-12.
 */
public class DaisyPlayer extends IsmartvPlayer implements SurfaceHolder.Callback {

    private SmartPlayer mPlayer;
    private String[] mPaths;
    private SurfaceHolder mHolder;

    private IsmartvMedia mMedia;
    private boolean mIsPlayingAdvertisement = false;
    private boolean mIsSeekBuffer;
    private int mCurrentQuality;
    private int mSpeed;
    private String mMediaIp;
    private String mCurrentUrl;
    private long mPlayerOpenTime = 0;
    private long mBufferStartTime;
    private HashMap<String, Integer> mAdIdMap = new HashMap<>();
    private String mSid;
    private static final String PLAYER_FLAG = "bestv";
    private boolean mStartPlaying = true; // 进入播放器缓冲结束

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
            mCurrentState = STATE_PREPARED;
            start();
        }
    };

    private SmartPlayer.OnVideoSizeChangedListener smartVideoSizeChangedListener = new SmartPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(SmartPlayer smartPlayer, int i, int i1) {

        }
    };

    private SmartPlayer.OnCompletionListenerUrl smartCompletionListenerUrl = new SmartPlayer.OnCompletionListenerUrl() {
        @Override
        public void onCompletion(SmartPlayer smartPlayer, String s) {
            mCurrentState = STATE_COMPLETED;
        }
    };

    private SmartPlayer.OnInfoListener smartInfoListener = new SmartPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(SmartPlayer smartPlayer, int i, int i1) {

            return false;
        }
    };

    private SmartPlayer.OnSeekCompleteListener smartSeekCompleteListener = new SmartPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(SmartPlayer smartPlayer) {

        }
    };

    private SmartPlayer.OnTsInfoListener onTsInfoListener = new SmartPlayer.OnTsInfoListener() {
        @Override
        public void onTsInfo(SmartPlayer smartPlayer, Map<String, String> map) {

        }
    };

    private SmartPlayer.OnM3u8IpListener onM3u8IpListener = new SmartPlayer.OnM3u8IpListener() {
        @Override
        public void onM3u8TsInfo(SmartPlayer smartPlayer, String s) {

        }
    };

    private SmartPlayer.OnErrorListener smartErrorListener = new SmartPlayer.OnErrorListener() {
        @Override
        public boolean onError(SmartPlayer smartPlayer, int i, int i1) {
            mCurrentState = STATE_ERROR;
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
            mCurrentState = STATE_PLAYING;
        }
    }

    @Override
    public void pause() {
        if (isInPlaybackState() && mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();

            mCurrentState = STATE_PAUSED;
        }
    }

    @Override
    public void release() {
        super.release();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;

            mCurrentState = STATE_IDLE;
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
        if (TextUtils.isEmpty(mCurrentUrl)) {
            return -1;
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

}
