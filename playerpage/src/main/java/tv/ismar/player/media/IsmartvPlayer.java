package tv.ismar.player.media;

import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;

/**
 * Created by longhai on 16-9-12.
 */
public abstract class IsmartvPlayer implements IPlayer {

    protected static final String TAG = "LH/IsmartvPlayer";

    /**
     * 视云片源,使用SmartPlayer(底层是MediaPlayer).
     */
    public static final byte MODE_SMART_PLAYER = 0x01;

    /**
     * 奇艺片源,使用奇艺播放器
     */
    public static final byte MODE_QIYI_PLAYER = 0x02;

    protected static byte mPlayerMode;

    public static final String AD_MODE_ONSTART = "qiantiepian";
    public static final String AD_MODE_ONPAUSE = "zanting";

    public IsmartvPlayer( byte mode) {
        mPlayerMode = mode;
    }

    @Override
    public boolean setDataSource(String url, boolean is_vip) {
        if (Utils.isEmptyText(url) || mPlayerMode == 0) {
            throw new IllegalArgumentException("IsmartvPlayer setDataSource invalidate.");
        }
        if (mPlayerMode == MODE_SMART_PLAYER) {
            getAdvertisement(AD_MODE_ONSTART);
        } else if (mPlayerMode == MODE_QIYI_PLAYER) {

        }
        return true;
    }

    @Override
    public void prepareAsync() {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seekTo(int position) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getAdCountDownTime() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isInPlaybackState() {
        return false;
    }

    protected void getAdvertisement(String adMode) {
    }

    protected void setMedia() {

    }
}
