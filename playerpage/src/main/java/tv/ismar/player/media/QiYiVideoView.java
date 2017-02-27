package tv.ismar.player.media;
import cn.ismartv.truetime.TrueTime;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.qiyi.sdk.player.BitStream;
import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.IMediaPlayer;
import com.qiyi.sdk.player.ISdkError;
import com.qiyi.sdk.player.IVideoOverlay;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.VideoSurfaceView;

import java.util.ArrayList;
import java.util.List;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.network.entity.ClipEntity;

/**
 * Created by longhai on 16-10-12.
 */
public class QiYiVideoView extends VideoSurfaceView implements SurfaceHolder.Callback {

    private final String TAG = "LH/QiYiVideoView";
    private IMediaPlayer mPlayer;
    private IMedia mMedia;
    private IVideoOverlay mVideoOverlay;
    private SurfaceHolder mHolder;
    private IsmartvPlayer mIsmartvPlayer;
    private List<BitStream> bitStreamList;
    private int previewLength;

    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_COMPLETED = 5;
    public static final int STATE_BUFFERING = 6;
    private int mCurrentState = STATE_IDLE;

    public QiYiVideoView(Context context) {
        super(context);
    }

    public QiYiVideoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public QiYiVideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setPlayer(IMedia media, IVideoOverlay videoOverlay, IsmartvPlayer ismartvPlayer) {
        mMedia = media;
        mVideoOverlay = videoOverlay;
        mIsmartvPlayer = ismartvPlayer;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("LH/", "surfaceCreatedQiYi");
        if (mIsmartvPlayer == null) {
            return;
        }
        mHolder = holder;
        openVideo();
        requestLayout();
        invalidate();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("LH/", "surfaceChangedQiYi:" + width + " " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
    }

    private void openVideo() {
        mPlayer = PlayerSdk.getInstance().createMediaPlayer();
        //setVideo方法, 更名为setData, 必须调用, 需传入IMedia对象, 起播时间点修改为从IMedia对象获取, 不从setData传参
        mPlayer.setData(mMedia);
        //设置IVideoOverlay对象, 必须调用
        mPlayer.setDisplay(mVideoOverlay);

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

        mPlayer.setOnInfoListener(onInfoListener);

        if (mIsmartvPlayer.mOnDataSourceSetListener != null) {
            mIsmartvPlayer.mOnDataSourceSetListener.onSuccess();
        }
    }

    public boolean isInPlaybackState() {
        return (mPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public void prepareAsync() {
        mPlayer.prepareAsync();
        mCurrentState = STATE_PREPARING;
    }

    public void start() {
        if (mIsmartvPlayer.isInPlaybackState() && mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public void pause() {
        if (mIsmartvPlayer.isInPlaybackState() && mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void release(boolean flag) {
        mCurrentState = STATE_IDLE;
        mIsmartvPlayer = null;
        mVideoOverlay = null;
        if (mPlayer != null) {
            mPlayer.stop();
            if (flag) {
                mPlayer.release();
                mPlayer = null;
            }
            PlayerBuilder.getInstance().release();
        }
    }

    public void seekTo(int position) {
        mPlayer.seekTo(position);
        if (mIsmartvPlayer.isInPlaybackState()) {
            mIsmartvPlayer.logVideoSeek();
        }
    }

    public int getCurrentPosition() {
        if (mIsmartvPlayer.isInPlaybackState()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mIsmartvPlayer.isInPlaybackState()) {
            if (previewLength > 0) {
                return previewLength;
            }
            return mPlayer.getDuration();
        }
        return 0;
    }

    public int getAdCountDownTime() {
        Log.d(TAG, "adCountTime:" + mPlayer.getAdCountDownTime());
        return mPlayer.getAdCountDownTime();
    }

    public boolean isPlaying() {
        return mIsmartvPlayer.isInPlaybackState() && mPlayer.isPlaying();
    }

    public void switchQuality(ClipEntity.Quality quality) {
        mPlayer.switchBitStream(qualityConvertToBitStream(quality));
        mIsmartvPlayer.mQuality = quality;
    }

    public IAdController getAdController() {
        if (mPlayer == null) {
            return null;
        }
        return mPlayer.getAdController();
    }

    private IMediaPlayer.OnStateChangedListener qiyiStateChangedListener = new IMediaPlayer.OnStateChangedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            mCurrentState = STATE_PREPARED;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onPrepared();
            }
        }

        @Override
        public void onAdStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            mIsmartvPlayer.mIsPlayingAdvertisement = true;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onAdStart();
            }
            mIsmartvPlayer.logAdStart();
        }

        @Override
        public void onAdEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            mIsmartvPlayer.mIsPlayingAdvertisement = false;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onAdEnd();
            }
            mIsmartvPlayer.logAdExit();
        }

        @Override
        public void onMiddleAdStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            //中插广告开始播放
            mIsmartvPlayer.mIsPlayingAdvertisement = true;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onMiddleAdStart();
            }
            mIsmartvPlayer.logAdStart();

        }

        @Override
        public void onMiddleAdEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            //中插广告播放结束
            mIsmartvPlayer.mIsPlayingAdvertisement = false;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onMiddleAdEnd();
            }
            mIsmartvPlayer.logAdExit();

        }

        @Override
        public void onStarted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            if (mCurrentState == STATE_PAUSED) {
                mIsmartvPlayer.logVideoContinue();
            } else {
                mIsmartvPlayer.logVideoPlayStart();
            }
            mCurrentState = STATE_PLAYING;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onStarted();
            }
        }

        @Override
        public void onPaused(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            Log.i(TAG, "qiyiOnPaused:" + mCurrentState);
            if (mCurrentState == STATE_PLAYING) {
                mIsmartvPlayer.logVideoPause();
            }
            mCurrentState = STATE_PAUSED;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onPaused();
            }
        }

        @Override
        public void onCompleted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            mCurrentState = STATE_COMPLETED;
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onCompleted();
            }
        }

        @Override
        public void onStopped(IMediaPlayer iMediaPlayer) {
            if (mPlayer != null && mIsmartvPlayer != null && mIsmartvPlayer.mIsPreview) {
                mCurrentState = STATE_COMPLETED;
                if (mIsmartvPlayer.mOnStateChangedListener != null) {
                    mIsmartvPlayer.mOnStateChangedListener.onCompleted();
                }
            }

        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, ISdkError iSdkError) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return true;
            }
            mCurrentState = STATE_ERROR;
            Log.e(TAG, "QiYiPlayer onError:" + iSdkError.getCode() + " " + iSdkError.getMsgFromError());
            mIsmartvPlayer.logVideoException(iSdkError.getCode());
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onError(iSdkError.getMsgFromError());
            }
            return true;
        }
    };

    private IMediaPlayer.OnBitStreamInfoListener qiyiBitStreamInfoListener = new IMediaPlayer.OnBitStreamInfoListener() {
        @Override
        public void onPlayableBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            mIsmartvPlayer.mQualities = new ArrayList<>();
            bitStreamList = list;
            for (BitStream bitStream : list) {
                Log.i(mIsmartvPlayer.TAG, "bitStream:" + bitStream.getValue());
                // 去除对应视云“自适应”码率
                if (bitStream.getValue() > 1) {
                    mIsmartvPlayer.mQualities.add(bitStreamConvertToQuality(bitStream));
                }
            }
        }

        @Override
        public void onVipBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {
            if (mPlayer == null) {
                return;
            }

        }

        @Override
        public void onBitStreamSelected(IMediaPlayer iMediaPlayer, BitStream bitStream) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            mIsmartvPlayer.mQuality = bitStreamConvertToQuality(bitStream);
        }
    };

    private IMediaPlayer.OnPreviewInfoListener qiyiPreviewInfoListener = new IMediaPlayer.OnPreviewInfoListener() {
        @Override
        public void onPreviewInfoReady(IMediaPlayer iMediaPlayer, boolean b, int i) {
            Log.d(TAG, "QiYiOnPreview: " + b + ", length = " + i);
            if (mPlayer == null) {
                return;
            }
            if (b) {
                previewLength = i;
            }
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener qiyiVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height) {
            Log.i("LH/", "onVideoSizeChangedQiYi:" + width + " " + height);
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            if (mIsmartvPlayer.mOnVideoSizeChangedListener != null) {
                mIsmartvPlayer.mOnVideoSizeChangedListener.onVideoSizeChanged(width, height);
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener qiyiSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekCompleted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            if (mIsmartvPlayer.isInPlaybackState()) {
                mIsmartvPlayer.logVideoSeekComplete();
            }
            if (mIsmartvPlayer.mOnStateChangedListener != null) {
                mIsmartvPlayer.mOnStateChangedListener.onSeekComplete();
            }
        }
    };

    private IMediaPlayer.OnBufferChangedListener qiyiBufferChangedListener = new IMediaPlayer.OnBufferChangedListener() {
        @Override
        public void onBufferStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            if (mIsmartvPlayer.mOnBufferChangedListener != null) {
                mIsmartvPlayer.mOnBufferChangedListener.onBufferStart();
            }
            mIsmartvPlayer.mBufferStartTime = TrueTime.now().getTime();
        }

        @Override
        public void onBufferEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            if (mIsmartvPlayer.mOnBufferChangedListener != null) {
                mIsmartvPlayer.mOnBufferChangedListener.onBufferEnd();
            }
            if (mIsmartvPlayer.mFirstOpen) {
                mIsmartvPlayer.mFirstOpen = false;
                mIsmartvPlayer.logVideoPlayLoading("");
            } else {
                mIsmartvPlayer.logVideoBufferEnd();
            }
            if (mIsmartvPlayer.mIsPlayingAdvertisement) {
                mIsmartvPlayer.logAdBlockend();
            }
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public void onInfo(IMediaPlayer iMediaPlayer, int i, Object o) {
            if (mPlayer == null || mIsmartvPlayer == null) {
                return;
            }
            if (mIsmartvPlayer.mOnInfoListener != null) {
                mIsmartvPlayer.mOnInfoListener.onInfo(i, o);
            }

        }
    };

    private BitStream qualityConvertToBitStream(ClipEntity.Quality quality) {
        // 更改为new_vip分支显示样式
        switch (quality) {
            case QUALITY_LOW:
            case QUALITY_ADAPTIVE:
                return BitStream.BITSTREAM_STANDARD;
            case QUALITY_NORMAL:// 流畅
                return BitStream.BITSTREAM_HIGH;
            case QUALITY_MEDIUM:// 高清
                if (!bitStreamList.isEmpty()) {
                    if (bitStreamList.contains(BitStream.BITSTREAM_720P)) {
                        return BitStream.BITSTREAM_720P;
                    } else if (bitStreamList.contains(BitStream.BITSTREAM_720P_DOLBY)) {
                        return BitStream.BITSTREAM_720P_DOLBY;
                    } else if (bitStreamList.contains(BitStream.BITSTREAM_720P_H265)) {
                        return BitStream.BITSTREAM_720P_H265;
                    }
                }
                return BitStream.BITSTREAM_720P;
            case QUALITY_HIGH:// 超清
                if (!bitStreamList.isEmpty()) {
                    if (bitStreamList.contains(BitStream.BITSTREAM_1080P)) {
                        return BitStream.BITSTREAM_1080P;
                    } else if (bitStreamList.contains(BitStream.BITSTREAM_1080P_DOLBY)) {
                        return BitStream.BITSTREAM_1080P_DOLBY;
                    } else if (bitStreamList.contains(BitStream.BITSTREAM_1080P_H265)) {
                        return BitStream.BITSTREAM_1080P_H265;
                    }
                }
                return BitStream.BITSTREAM_1080P;
            case QUALITY_ULTRA:
                Log.e(TAG, "Only support normal, medium, high quality.");
                break;
            case QUALITY_BLUERAY:
            case QUALITY_4K:
                if (!bitStreamList.isEmpty()) {
                    if (bitStreamList.contains(BitStream.BITSTREAM_4K)) {
                        return BitStream.BITSTREAM_4K;
                    } else if (bitStreamList.contains(BitStream.BITSTREAM_4K_DOLBY)) {
                        return BitStream.BITSTREAM_4K_DOLBY;
                    } else if (bitStreamList.contains(BitStream.BITSTREAM_4K_H265)) {
                        return BitStream.BITSTREAM_4K_H265;
                    }
                }
                return BitStream.BITSTREAM_4K;
        }
        return BitStream.BITSTREAM_STANDARD;
    }

    private ClipEntity.Quality bitStreamConvertToQuality(BitStream bitStream) {
//        for (BitStream d : mBitStreamList) {
//            if (d.equals(BitStream.BITSTREAM_HIGH)) {
//                avalibleRate[0] = true;
//                // currQuality = 0;
//            } else if (d.equals(BitStream.BITSTREAM_720P)) {
//                avalibleRate[1] = true;
//                // currQuality = 1;
//            } else if (d.equals(BitStream.BITSTREAM_1080P)) {
//                avalibleRate[2] = true;
//                // currQuality = 2;
//            }
//        }
        // 更改为以上显示方式
        if (bitStream == BitStream.BITSTREAM_STANDARD) {
            return ClipEntity.Quality.QUALITY_ADAPTIVE;
        } else if (bitStream == BitStream.BITSTREAM_HIGH) {
            return ClipEntity.Quality.QUALITY_NORMAL;
        } else if (bitStream == BitStream.BITSTREAM_UNKNOWN) {
            return ClipEntity.Quality.QUALITY_ADAPTIVE;
        } else if (bitStream == BitStream.BITSTREAM_720P
                || bitStream == BitStream.BITSTREAM_720P_DOLBY
                || bitStream == BitStream.BITSTREAM_720P_H265) {
            return ClipEntity.Quality.QUALITY_MEDIUM;
        } else if (bitStream == BitStream.BITSTREAM_1080P
                || bitStream == BitStream.BITSTREAM_1080P_DOLBY
                || bitStream == BitStream.BITSTREAM_1080P_H265) {
            return ClipEntity.Quality.QUALITY_HIGH;
        } else if (bitStream == BitStream.BITSTREAM_4K
                || bitStream == BitStream.BITSTREAM_4K_DOLBY
                || bitStream == BitStream.BITSTREAM_4K_H265) {
            return ClipEntity.Quality.QUALITY_4K;
        }
        return ClipEntity.Quality.QUALITY_NORMAL;
    }
}
