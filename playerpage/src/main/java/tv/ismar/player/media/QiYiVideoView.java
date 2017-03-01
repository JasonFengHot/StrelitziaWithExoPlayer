package tv.ismar.player.media;

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
import tv.ismar.account.IsmartvActivator;
import tv.ismar.account.core.Md5;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.reporter.IsmartvMedia;

/**
 * Created by longhai on 16-10-12.
 */
public class QiYiVideoView extends VideoSurfaceView implements SurfaceHolder.Callback {

    private final String TAG = "LH/QiYiVideoView";
    private IMediaPlayer mPlayer;
    private IMedia mMedia;
    private IVideoOverlay mVideoOverlay;
    private SurfaceHolder mHolder;
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

    private IPlayer.OnDataSourceSetListener mOnDataSourceSetListener;
    private IPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private IPlayer.OnBufferChangedListener mOnBufferChangedListener;
    private IPlayer.OnStateChangedListener mOnStateChangedListener;
    private IPlayer.OnInfoListener mOnInfoListener;
    private ClipEntity.Quality mQuality;
    private List<ClipEntity.Quality> mQualities;
    private boolean mIsPlayingAdvertisement;
    private boolean mIsPreview;// 奇艺预览结束时onStop调用， 没有onCompleted事件

    // log
    private IsmartvMedia mLogMedia;
    private static final String PLAYER_FLAG_QIYI = "qiyi";
    // 日志上报相关
    private int mSpeed = 0;
    private String mMediaIp = "";
    private int mMediaId = 0;
    private long mPlayerOpenTime = 0;
    private long mBufferStartTime;
    private boolean mFirstOpen = true; // 进入播放器缓冲结束

    public ClipEntity.Quality getmQuality() {
        return mQuality;
    }

    public List<ClipEntity.Quality> getmQualities() {
        return mQualities;
    }

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

    public QiYiVideoView(Context context) {
        super(context);
    }

    public QiYiVideoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public QiYiVideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setPlayer(IMedia media, IVideoOverlay videoOverlay, IsmartvMedia logMedia, boolean isPreview) {
        mPlayerOpenTime = TrueTime.now().getTime();
        mMedia = media;
        mVideoOverlay = videoOverlay;
        mLogMedia = logMedia;
        mIsPreview = isPreview;
        mHolder = getHolder();
        mHolder.addCallback(this);

        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        CallaPlay callaPlay = new CallaPlay();
        callaPlay.videoStart(mLogMedia, sn, mSpeed, sid, PLAYER_FLAG_QIYI);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("LH/", "surfaceCreatedQiYi");
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

        if (mOnDataSourceSetListener != null) {
            mOnDataSourceSetListener.onSuccess();
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
        if (isInPlaybackState() && !mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public void pause() {
        if (isInPlaybackState() && mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void release(boolean flag) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            PlayerBuilder.getInstance().release();
        }
        mCurrentState = STATE_IDLE;
        if (flag) {
            mOnDataSourceSetListener = null;
            mOnVideoSizeChangedListener = null;
            mOnBufferChangedListener = null;
            mOnStateChangedListener = null;
            mOnInfoListener = null;
        }
    }

    public void seekTo(int position) {
        if (isInPlaybackState()) {
            mPlayer.seekTo(position);
            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.videoPlaySeek(mLogMedia, mSpeed, getCurrentPosition(), sid, PLAYER_FLAG_QIYI);
        }
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (isInPlaybackState()) {
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
        return isInPlaybackState() && mPlayer.isPlaying();
    }

    public void switchQuality(ClipEntity.Quality quality) {
        mPlayer.switchBitStream(qualityConvertToBitStream(quality));
        mQuality = quality;
        String sn = IsmartvActivator.getInstance().getSnToken();
        String sid = Md5.md5(sn + TrueTime.now().getTime());
        CallaPlay callaPlay = new CallaPlay();
        callaPlay.videoSwitchStream(mLogMedia, "manual",
                mSpeed, sn, mMediaIp, sid, PLAYER_FLAG_QIYI);
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
            if (mPlayer == null) {
                return;
            }
            mCurrentState = STATE_PREPARED;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onPrepared();
            }
        }

        @Override
        public void onAdStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            mIsPlayingAdvertisement = true;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onAdStart();
            }
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.ad_play_load(
                    mLogMedia,
                    (TrueTime.now().getTime() - mPlayerOpenTime),
                    mMediaIp, mMediaId, PLAYER_FLAG_QIYI);
        }

        @Override
        public void onAdEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            mIsPlayingAdvertisement = false;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onAdEnd();
            }
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.ad_play_exit(
                    mLogMedia,
                    (TrueTime.now().getTime() - mPlayerOpenTime),
                    mMediaIp, mMediaId, PLAYER_FLAG_QIYI);
        }

        @Override
        public void onMiddleAdStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            //中插广告开始播放
            mIsPlayingAdvertisement = true;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onMiddleAdStart();
            }
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.ad_play_load(
                    mLogMedia,
                    (TrueTime.now().getTime() - mPlayerOpenTime),
                    mMediaIp, mMediaId, PLAYER_FLAG_QIYI);

        }

        @Override
        public void onMiddleAdEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            //中插广告播放结束
            mIsPlayingAdvertisement = false;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onMiddleAdEnd();
            }
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.ad_play_exit(
                    mLogMedia,
                    (TrueTime.now().getTime() - mPlayerOpenTime),
                    mMediaIp, mMediaId, PLAYER_FLAG_QIYI);

        }

        @Override
        public void onStarted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (mCurrentState == STATE_PAUSED) {
                String sn = IsmartvActivator.getInstance().getSnToken();
                String sid = Md5.md5(sn + TrueTime.now().getTime());
                CallaPlay callaPlay = new CallaPlay();
                callaPlay.videoPlayContinue(mLogMedia, mSpeed, getCurrentPosition(), sid, PLAYER_FLAG_QIYI);
            } else {
                CallaPlay callaPlay = new CallaPlay();
                callaPlay.videoPlayStart(mLogMedia, mSpeed, mMediaIp, PLAYER_FLAG_QIYI);
            }
            mCurrentState = STATE_PLAYING;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onStarted();
            }
        }

        @Override
        public void onPaused(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            Log.i(TAG, "qiyiOnPaused:" + mCurrentState);
            if (mCurrentState == STATE_PLAYING) {
                String sn = IsmartvActivator.getInstance().getSnToken();
                String sid = Md5.md5(sn + TrueTime.now().getTime());
                CallaPlay callaPlay = new CallaPlay();
                callaPlay.videoPlayPause(mLogMedia, mSpeed, getCurrentPosition(), sid, PLAYER_FLAG_QIYI);
            }
            mCurrentState = STATE_PAUSED;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onPaused();
            }
        }

        @Override
        public void onCompleted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            mCurrentState = STATE_COMPLETED;
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onCompleted();
            }
        }

        @Override
        public void onStopped(IMediaPlayer iMediaPlayer) {
            if (mPlayer != null && mIsPreview) {
                mCurrentState = STATE_COMPLETED;
                if (mOnStateChangedListener != null) {
                    mOnStateChangedListener.onCompleted();
                }
            }

        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, ISdkError iSdkError) {
            if (mPlayer == null) {
                return true;
            }
            mCurrentState = STATE_ERROR;
            Log.e(TAG, "QiYiPlayer onError:" + iSdkError.getCode() + " " + iSdkError.getMsgFromError());

            String sn = IsmartvActivator.getInstance().getSnToken();
            String sid = Md5.md5(sn + TrueTime.now().getTime());
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.videoExcept(
                    "mediaexception", iSdkError.getCode(),
                    mLogMedia, mSpeed, sid,
                    getCurrentPosition(), PLAYER_FLAG_QIYI);

            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onError(iSdkError.getMsgFromError());
            }
            return true;
        }
    };

    private IMediaPlayer.OnBitStreamInfoListener qiyiBitStreamInfoListener = new IMediaPlayer.OnBitStreamInfoListener() {
        @Override
        public void onPlayableBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {
            if (mPlayer == null) {
                return;
            }
            mQualities = new ArrayList<>();
            bitStreamList = list;
            for (BitStream bitStream : list) {
                Log.i(TAG, "bitStream:" + bitStream.getValue());
                // 去除对应视云“自适应”码率
                if (bitStream.getValue() > 1) {
                    mQualities.add(bitStreamConvertToQuality(bitStream));
                }
            }
        }

        @Override
        public void onVipBitStreamListUpdate(IMediaPlayer iMediaPlayer, List<BitStream> list) {
            Log.i(TAG, "bitStream:onVipBitStreamListUpdate");

        }

        @Override
        public void onBitStreamSelected(IMediaPlayer iMediaPlayer, BitStream bitStream) {
            if (mPlayer == null) {
                return;
            }
            mQuality = bitStreamConvertToQuality(bitStream);
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
            if (mPlayer == null) {
                return;
            }
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(width, height);
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener qiyiSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekCompleted(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (isInPlaybackState()) {
                String sn = IsmartvActivator.getInstance().getSnToken();
                String sid = Md5.md5(sn + TrueTime.now().getTime());
                CallaPlay callaPlay = new CallaPlay();
                callaPlay.videoPlaySeekBlockend(
                        mLogMedia, mSpeed, getCurrentPosition(),
                        (TrueTime.now().getTime() - mBufferStartTime),
                        mMediaIp, sid, PLAYER_FLAG_QIYI);
            }
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onSeekComplete();
            }
        }
    };

    private IMediaPlayer.OnBufferChangedListener qiyiBufferChangedListener = new IMediaPlayer.OnBufferChangedListener() {
        @Override
        public void onBufferStart(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (mOnBufferChangedListener != null) {
                mOnBufferChangedListener.onBufferStart();
            }
            mBufferStartTime = TrueTime.now().getTime();
        }

        @Override
        public void onBufferEnd(IMediaPlayer iMediaPlayer) {
            if (mPlayer == null) {
                return;
            }
            if (mOnBufferChangedListener != null) {
                mOnBufferChangedListener.onBufferEnd();
            }
            CallaPlay callaPlay = new CallaPlay();
            if (mFirstOpen) {
                mFirstOpen = false;
                String sn = IsmartvActivator.getInstance().getSnToken();
                String sid = Md5.md5(sn + TrueTime.now().getTime());
                callaPlay.videoPlayLoad(
                        mLogMedia,
                        (TrueTime.now().getTime() - mPlayerOpenTime),
                        mSpeed, mMediaIp, sid, "", PLAYER_FLAG_QIYI);
            } else {
                String sn = IsmartvActivator.getInstance().getSnToken();
                String sid = Md5.md5(sn + TrueTime.now().getTime());
                callaPlay.videoPlayBlockend(
                        mLogMedia,
                        mSpeed, getCurrentPosition(),
                        mMediaIp, sid, PLAYER_FLAG_QIYI);
            }
            if (mIsPlayingAdvertisement) {
                callaPlay.ad_play_blockend(
                        mLogMedia,
                        (TrueTime.now().getTime() - mBufferStartTime),
                        mMediaIp, mMediaId, PLAYER_FLAG_QIYI);
            }
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public void onInfo(IMediaPlayer iMediaPlayer, int i, Object o) {
            if (mPlayer == null) {
                return;
            }
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(i, o);
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
                PLAYER_FLAG_QIYI);
    }
}
