package tv.ismar.player.media;

/**
 * Created by longhai on 16-9-12.
 */
public interface IPlayer {

    /**
     * 设置播放地址
     *
     * @param url    媒体播放地址
     * @param is_vip 设置是否vip,只有是奇艺片源时,设置此参数才有效
     * @return 设置是否成功, 主要用于判断创建播放器对象时是否传入Mode
     */
    boolean setDataSource(String url, boolean is_vip);

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
