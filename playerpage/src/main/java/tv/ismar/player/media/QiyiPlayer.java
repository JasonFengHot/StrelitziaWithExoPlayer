package tv.ismar.player.media;

import android.view.View;

import com.qiyi.sdk.player.BitStream;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.IMediaPlayer;
import com.qiyi.sdk.player.ISdkError;
import com.qiyi.sdk.player.IVideoOverlay;
import com.qiyi.sdk.player.PlayerSdk;

import java.util.List;

/**
 * Created by longhai on 16-9-12.
 */
public class QiyiPlayer extends IsmartvPlayer {

    private IMediaPlayer mPlayer;

    public QiyiPlayer() {
        this(PlayerBuilder.MODE_QIYI_PLAYER);
    }

    public QiyiPlayer(byte mode) {
        super(mode);
    }

    @Override
    protected void setMedia(IMedia media) {
        mSurfaceView.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);

        //创建IVideoOverlay对象, 不支持实现IVideoOverlay接口，必须调用PlaySdk.getInstance().createVideoOverlay创建
        //创建IVideoOverlay对象, 不需创建SurfaceView, 直接传入父容器即可
        IVideoOverlay videoOverlay = PlayerSdk.getInstance().createVideoOverlay(mContainer);
        //创建IVideoOverlay对象, 如需修改SurfaceView, 请继承VideoSurfaceView
        //mSurfaceView = new MyVideoSurfaceView(getApplicationContext());
        //mVideoOverlay = PlaySdk.getInstance().createVideoOverlay(mWindowedParent, mSurfaceView);
        //IMediaPlayer对象通过QiyiPlayerSdk.getInstance().createVideoPlayer()创建
        mPlayer = PlayerSdk.getInstance().createMediaPlayer();
        //setVideo方法, 更名为setData, 必须调用, 需传入IMedia对象, 起播时间点修改为从IMedia对象获取, 不从setData传参
        mPlayer.setData(media);
        //设置IVideoOverlay对象, 必须调用
        mPlayer.setDisplay(videoOverlay);

        //设置播放状态回调监听器, 需要时设置
        mPlayer.setOnStateChangedListener(qiyiStateChangedListener);

        //设置码流信息回调监听器, 需要时设置
        mPlayer.setOnBitStreamInfoListener(qiyiBitStreamInfoListener);

        //设置VIP试看信息回调监听器, 需要时设置
        mPlayer.setOnPreviewInfoListener(qiyiPreviewInfoListener);

        //设置视频分辨率回调监听器, 需要时设置
        mPlayer.setOnVideoSizeChangedListener(qiyiVideoSizeChangedListener);

        //设置seek完成监听器, 需要时设置
        mPlayer.setOnSeekCompleteListener(qiyiSeekCompleteListener);

        //设置缓冲事件监听器, 需要时设置
        mPlayer.setOnBufferChangedListener(qiyiBufferChangedListener);

        super.setMedia(media);

    }

    @Override
    public void prepareAsync() {
        //调用prepareAsync, 播放器开始准备, 必须调用
        mPlayer.prepareAsync();
        mCurrentState = STATE_PREPARING;
    }

    private IMediaPlayer.OnStateChangedListener qiyiStateChangedListener = new IMediaPlayer.OnStateChangedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mCurrentState = STATE_PREPARED;
            start();
        }

        @Override
        public void onAdStart(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onAdEnd(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onStarted(IMediaPlayer iMediaPlayer) {
            mCurrentState = STATE_PLAYING;
        }

        @Override
        public void onPaused(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onCompleted(IMediaPlayer iMediaPlayer) {
            mCurrentState = STATE_COMPLETED;
        }

        @Override
        public void onStopped(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, ISdkError iSdkError) {
            mCurrentState = STATE_ERROR;
            return false;
        }
    };

    private IMediaPlayer.OnBitStreamInfoListener qiyiBitStreamInfoListener = new IMediaPlayer.OnBitStreamInfoListener() {
        @Override
        public void onBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {

        }

        @Override
        public void onBitStreamSelected(IMediaPlayer iMediaPlayer, BitStream bitStream) {

        }
    };

    private IMediaPlayer.OnPreviewInfoListener qiyiPreviewInfoListener = new IMediaPlayer.OnPreviewInfoListener() {
        @Override
        public void onPreviewInfoReady(IMediaPlayer iMediaPlayer, boolean b, int i) {

        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener qiyiVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1) {

        }
    };

    private IMediaPlayer.OnSeekCompleteListener qiyiSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekCompleted(IMediaPlayer iMediaPlayer) {

        }
    };

    private IMediaPlayer.OnBufferChangedListener qiyiBufferChangedListener = new IMediaPlayer.OnBufferChangedListener() {
        @Override
        public void onBufferStart(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onBufferEnd(IMediaPlayer iMediaPlayer) {

        }
    };

    @Override
    public void start() {
        if (isInPlaybackState() && mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
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
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mPlayer.isPlaying();
    }

}
