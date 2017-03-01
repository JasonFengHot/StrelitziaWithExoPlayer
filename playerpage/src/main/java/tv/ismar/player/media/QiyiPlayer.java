package tv.ismar.player.media;

import com.qiyi.sdk.player.IAdController;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.IVideoOverlay;
import com.qiyi.sdk.player.PlayerSdk;

import java.util.List;

import tv.ismar.app.network.entity.ClipEntity;

/**
 * Created by longhai on 16-9-12.
 */
public class QiyiPlayer extends IsmartvPlayer {

    private QiYiVideoView videoSurfaceView;

    public QiyiPlayer() {
        super(PlayerBuilder.MODE_QIYI_PLAYER);
    }

    @Override
    protected void setMedia(IMedia media) {
        super.setMedia(media);
        if (mContainer == null) {
            return;
        }
        //创建IVideoOverlay对象, 不支持实现IVideoOverlay接口，必须调用PlaySdk.getInstance().createVideoOverlay创建
        //创建IVideoOverlay对象, 不需创建SurfaceView, 直接传入父容器即可
        videoSurfaceView = new QiYiVideoView(mContext);
        videoSurfaceView.setmOnDataSourceSetListener(mOnDataSourceSetListener);
        videoSurfaceView.setmOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        videoSurfaceView.setmOnBufferChangedListener(mOnBufferChangedListener);
        videoSurfaceView.setmOnStateChangedListener(mOnStateChangedListener);
        videoSurfaceView.setmOnInfoListener(mOnInfoListener);

        IVideoOverlay videoOverlay = PlayerSdk.getInstance().createVideoOverlay(mContainer, videoSurfaceView);
        videoSurfaceView.setPlayer(media, videoOverlay, mLogMedia, mIsPreview);
        //创建IVideoOverlay对象, 如需修改SurfaceView, 请继承VideoSurfaceView
        //mSurfaceView = new MyVideoSurfaceView(getApplicationContext());
        //mVideoOverlay = PlaySdk.getInstance().createVideoOverlay(mWindowedParent, mSurfaceView);
        //IMediaPlayer对象通过QiyiPlayerSdk.getInstance().createVideoPlayer()创建

    }

    @Override
    public boolean isInPlaybackState() {
        if (videoSurfaceView == null) {
            return false;
        }
        return videoSurfaceView.isInPlaybackState();
    }

    @Override
    public void prepareAsync() {
        //调用prepareAsync, 播放器开始准备, 必须调用
        if (videoSurfaceView == null) {
            return;
        }
        videoSurfaceView.prepareAsync();
    }


    @Override
    public void start() {
        if (videoSurfaceView == null) {
            return;
        }
        videoSurfaceView.start();
    }

    @Override
    public void pause() {
        if (videoSurfaceView == null) {
            return;
        }
        videoSurfaceView.pause();
    }

    @Override
    public void stopPlayBack() {
        if (videoSurfaceView == null) {
            return;
        }
        videoSurfaceView.release(true);
        videoSurfaceView = null;
        super.stopPlayBack();
    }

    @Override
    public void seekTo(int position) {
        if (videoSurfaceView == null) {
            return;
        }
        videoSurfaceView.seekTo(position);
    }

    @Override
    public int getCurrentPosition() {
        if (videoSurfaceView == null) {
            return 0;
        }
        return videoSurfaceView.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (videoSurfaceView == null) {
            return 0;
        }
        return videoSurfaceView.getDuration();
    }

    @Override
    public int getAdCountDownTime() {
        if (videoSurfaceView == null) {
            return 0;
        }
        return videoSurfaceView.getAdCountDownTime();
    }

    @Override
    public boolean isPlaying() {
        if (videoSurfaceView == null) {
            return false;
        }
        return videoSurfaceView.isPlaying();
    }

    @Override
    public void switchQuality(ClipEntity.Quality quality) {
        if (videoSurfaceView == null) {
            return;
        }
        videoSurfaceView.switchQuality(quality);
    }

    @Override
    public IAdController getAdController() {
        if (videoSurfaceView == null) {
            return null;
        }
        return videoSurfaceView.getAdController();
    }

    @Override
    public ClipEntity.Quality getCurrentQuality() {
        if (videoSurfaceView == null) {
            return null;
        }
        return videoSurfaceView.getmQuality();
    }

    @Override
    public List<ClipEntity.Quality> getQulities() {
        if (videoSurfaceView == null) {
            return null;
        }
        return videoSurfaceView.getmQualities();
    }

    @Override
    public void logVideoExit(int exitPosition, String source) {
        if (videoSurfaceView != null) {
            videoSurfaceView.logVideoExit(exitPosition, source);
        }
    }
}
