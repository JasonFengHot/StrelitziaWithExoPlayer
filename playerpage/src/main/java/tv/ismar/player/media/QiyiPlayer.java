package tv.ismar.player.media;

import com.qiyi.sdk.player.IMediaPlayer;

import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by longhai on 16-9-12.
 */
public class QiyiPlayer extends IsmartvPlayer {

    private IMediaPlayer mPlayer;

    public QiyiPlayer(ItemEntity item) {
        this(item, MODE_QIYI_PLAYER);
    }

    public QiyiPlayer(ItemEntity item, byte mode) {
        super(item, mode);
    }

    @Override
    public void setMedia() {
        super.setMedia();
    }
}
