package tv.ismar.player.media;

import tv.ismar.player.SmartPlayer;

/**
 * Created by longhai on 16-9-12.
 */
public class DaisyPlayer extends IsmartvPlayer {

    private SmartPlayer mPlayer;

    public DaisyPlayer() {
        this(PlayerBuilder.MODE_SMART_PLAYER);
    }

    private DaisyPlayer(byte mode) {
        super(mode);
    }

    @Override
    protected void setMedia(String[] urls) {

    }
}
