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
    void setDataSource(ClipEntity clipEntity);

    /**
     * 播放器准备
     */
    void prepareAsync();

    void start();

    void pause();

    void seekTo(int position);

    void stop();

    void release();

    int getCurrentPosition();

    int getDuration();

    int getAdCountDownTime();

    boolean isPlaying();

    /**
     * 是否处在可以播放状态
     */
    boolean isInPlaybackState();

}
