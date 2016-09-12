package tv.ismar.player.media;

import android.util.Log;
import android.widget.Toast;

import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.IMediaPlayer;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;

/**
 * Created by longhai on 16-9-12.
 */
public class QiyiPlayer extends IsmartvPlayer {

    private IMediaPlayer mPlayer;

    public QiyiPlayer() {
        this(PlayerBuilder.MODE_QIYI_PLAYER);
    }

    public QiyiPlayer(byte mode) {
        super(mode);

    }

    @Override
    protected void setMedia(IMedia media) {

    }
}
