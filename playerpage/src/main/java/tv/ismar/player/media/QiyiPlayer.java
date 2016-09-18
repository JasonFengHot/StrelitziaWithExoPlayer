package tv.ismar.player.media;

import android.util.Log;
import android.view.View;

import com.qiyi.sdk.player.BitStream;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.IMediaPlayer;
import com.qiyi.sdk.player.ISdkError;
import com.qiyi.sdk.player.IVideoOverlay;
import com.qiyi.sdk.player.PlayerSdk;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.network.entity.ClipEntity;

/**
 * Created by longhai on 16-9-12.
 */
public class QiyiPlayer extends IsmartvPlayer {

    private IMediaPlayer mPlayer;
    private int previewLength;

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

        logVideoStart(0);
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
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onPrepared();
            }
        }

        @Override
        public void onAdStart(IMediaPlayer iMediaPlayer) {
            mIsPlayingAdvertisement = true;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onAdStart();
            }
            logAdStart("", 0);
        }

        @Override
        public void onAdEnd(IMediaPlayer iMediaPlayer) {
            mIsPlayingAdvertisement = false;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onAdEnd();
            }
            logAdExit("", 0);
        }

        @Override
        public void onStarted(IMediaPlayer iMediaPlayer) {
            mCurrentState = STATE_PLAYING;
            logVideoPlayStart(0, "");
            if (mCurrentState == STATE_PAUSED) {
                logVideoContinue(0);
            }
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onStarted();
            }
        }

        @Override
        public void onPaused(IMediaPlayer iMediaPlayer) {
            logVideoPause(0);
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onPaused();
            }
        }

        @Override
        public void onCompleted(IMediaPlayer iMediaPlayer) {
            mCurrentState = STATE_COMPLETED;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onCompleted();
            }
        }

        @Override
        public void onStopped(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, ISdkError iSdkError) {
            mCurrentState = STATE_ERROR;
            Log.e(TAG, "QiYiPlayer onError:" + iSdkError.getCode() + " " + iSdkError.getMsgFromError());
            logVideoException(iSdkError.getCode(), 0);
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onError("QiYiPlayer error " + iSdkError.getCode());
            }
            return false;
        }
    };

    private IMediaPlayer.OnBitStreamInfoListener qiyiBitStreamInfoListener = new IMediaPlayer.OnBitStreamInfoListener() {
        @Override
        public void onBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {
            mQualities = new ArrayList<>();
            for (BitStream bitStream : list) {
                mQualities.add(bitStreamConvertToQuality(bitStream));
            }
        }

        @Override
        public void onBitStreamSelected(IMediaPlayer iMediaPlayer, BitStream bitStream) {
            mQuality = bitStreamConvertToQuality(bitStream);
        }
    };

    private ClipEntity.Quality bitStreamConvertToQuality(BitStream bitStream) {
        if (bitStream == BitStream.BITSTREAM_STANDARD) {
            return ClipEntity.Quality.QUALITY_NORMAL;
        } else if (bitStream == BitStream.BITSTREAM_HIGH) {
            return ClipEntity.Quality.QUALITY_MEDIUM;
        } else if (bitStream == BitStream.BITSTREAM_UNKNOWN) {
            return ClipEntity.Quality.QUALITY_ADAPTIVE;
        } else if (bitStream == BitStream.BITSTREAM_720P
                || bitStream == BitStream.BITSTREAM_720P_DOLBY
                || bitStream == BitStream.BITSTREAM_720P_H265) {
            return ClipEntity.Quality.QUALITY_HIGH;
        } else if (bitStream == BitStream.BITSTREAM_1080P
                || bitStream == BitStream.BITSTREAM_1080P_DOLBY
                || bitStream == BitStream.BITSTREAM_1080P_H265) {
            return ClipEntity.Quality.QUALITY_ULTRA;
        } else if (bitStream == BitStream.BITSTREAM_4K
                || bitStream == BitStream.BITSTREAM_4K_DOLBY
                || bitStream == BitStream.BITSTREAM_4K_H265) {
            return ClipEntity.Quality.QUALITY_4K;
        }
        return ClipEntity.Quality.QUALITY_NORMAL;
    }

    private IMediaPlayer.OnPreviewInfoListener qiyiPreviewInfoListener = new IMediaPlayer.OnPreviewInfoListener() {
        @Override
        public void onPreviewInfoReady(IMediaPlayer iMediaPlayer, boolean b, int i) {
            Log.d(TAG, "QiYiOnPreview: " + b + ", length = " + i);
            if (b) {
                previewLength = i;
            }
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener qiyiVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1) {
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(i, i1);
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener qiyiSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekCompleted(IMediaPlayer iMediaPlayer) {
            if (isInPlaybackState()) {
                logVideoSeekComplete(0, "");
            }
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onSeekComplete();
            }
        }
    };

    private IMediaPlayer.OnBufferChangedListener qiyiBufferChangedListener = new IMediaPlayer.OnBufferChangedListener() {
        @Override
        public void onBufferStart(IMediaPlayer iMediaPlayer) {
            if (mOnBufferChangedListener != null) {
                mOnBufferChangedListener.onBufferStart();
            }
            mBufferStartTime = System.currentTimeMillis();
        }

        @Override
        public void onBufferEnd(IMediaPlayer iMediaPlayer) {
            if (mOnBufferChangedListener != null) {
                mOnBufferChangedListener.onBufferEnd();
            }
            if (mFirstOpen) {
                mFirstOpen = false;
                logVideoPlayLoading(0, "", "");
            } else {
                logVideoBufferEnd(0, "");
            }
            if (mIsPlayingAdvertisement) {
                logAdBlockend("", 0);
            }
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
        logVideoExit(0);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;

            mCurrentState = STATE_IDLE;
            PlayerSdk.getInstance().release();
        }
    }

    @Override
    public void seekTo(int position) {
        mPlayer.seekTo(position);
        if (isInPlaybackState()) {
            logVideoSeek(0);
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
            if (previewLength > 0) {
                return previewLength;
            }
            return mPlayer.getDuration();
        }
        return super.getDuration();
    }

    @Override
    public int getAdCountDownTime() {
        return mPlayer.getAdCountDownTime();
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mPlayer.isPlaying();
    }

}
