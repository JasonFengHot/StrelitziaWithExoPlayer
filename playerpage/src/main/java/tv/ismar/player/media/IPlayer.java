package tv.ismar.player.media;

import tv.ismar.app.network.entity.ClipEntity;

/**
 * Created by longhai on 16-9-12.
 */
public interface IPlayer {

    /**
     * 设置播放地址
     *
     * @param clipEntity 媒体播放地址
     */
    void setDataSource(ClipEntity clipEntity, OnDataSourceSetListener onDataSourceSetListener);

    /**
     * 播放器准备
     */
    void prepareAsync();

    void start();

    void pause();

    void seekTo(int position);

    void release();

    int getCurrentPosition();

    int getDuration();

    int getAdCountDownTime();

    boolean isPlaying();

    /**
     * 是否处在可以播放状态
     */
    boolean isInPlaybackState();

    void setOnBufferChangedListener(IsmartvPlayer.OnBufferChangedListener onBufferChangedListener);

    void setOnVideoSizeChangedListener(IsmartvPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener);

    void setOnStateChangedListener(OnStateChangedListener onStateChangedListener);

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IsmartvPlayer player, int videoWidth, int videoHeight);
    }

    public interface OnBufferChangedListener {
        void onBufferStart(IsmartvPlayer player);

        void onBufferEnd(IsmartvPlayer player);
    }

    public interface OnStateChangedListener {
        void onPrepared(IsmartvPlayer player);

        void onAdStart(IsmartvPlayer player);

        void onAdEnd(IsmartvPlayer player);

        void onStarted(IsmartvPlayer player);

        void onPaused(IsmartvPlayer player);

        void onSeekComplete(IsmartvPlayer player);

        void onCompleted(IsmartvPlayer player);

        void onStopped(IsmartvPlayer player);

        boolean onError(IsmartvPlayer error);
    }

    public interface OnDataSourceSetListener {

        void onSuccess();

        void onFailed(String message);

    }

}
