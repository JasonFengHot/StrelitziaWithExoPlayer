package tv.ismar.player.media;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by longhai on 16-9-12.
 */
public class PlayerBuilder {

    private static final String TAG = "LH/PlayerBuilder";

    /**
     * 视云片源,使用SmartPlayer(底层是MediaPlayer).
     */
    public static final byte MODE_SMART_PLAYER = 0x01;

    /**
     * 奇艺片源,使用奇艺播放器
     */
    public static final byte MODE_QIYI_PLAYER = 0x02;

//    /**
//     * 视云片源,使用SmartPlayer,实现了预加载
//     */
//    public static final byte MODE_PRELOAD_PLAYER = 0x03;

    private byte mPlayerMode = -1;
    private ItemEntity mItemEntity;
    private int mStartPosition = 0;
    private boolean mIsPreview;

    // Removes the default public constructor
    private PlayerBuilder() {
    }

    // The SessionManager implements the singleton pattern
    private static volatile PlayerBuilder sInstance = null;

    /**
     * Returns a reference to the {@link PlayerBuilder}.
     *
     * @return The reference to the {@link PlayerBuilder}
     */
    public final static PlayerBuilder getInstance() {
        if (sInstance == null) {
            synchronized (PlayerBuilder.class) {
                if (sInstance == null) {
                    PlayerBuilder.sInstance = new PlayerBuilder();
                }
            }
        }
        return sInstance;
    }

    public PlayerBuilder setPlayerMode(byte playerMode) {
        mPlayerMode = playerMode;
        return this;
    }

    public PlayerBuilder setItemEntity(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
        return this;
    }

    public PlayerBuilder setStartPosition(int startPosition) {
        mStartPosition = startPosition;
        return this;
    }

    public PlayerBuilder setIsPreview(boolean isPreview) {
        mIsPreview = isPreview;
        return this;
    }

    public IsmartvPlayer build() {
        if (mPlayerMode <= 0) {
            Log.e(TAG, "Must call setPlayerMode first.");
            throw new IllegalAccessError("Must call setPlayerMode first.");
        }
        if (mItemEntity == null) {
            Log.e(TAG, "Must call setItemEntity first.");
            throw new IllegalAccessError("Must call setItemEntity first.");
        }
        IsmartvPlayer ismartvPlayer = null;
        switch (mPlayerMode) {
            case MODE_SMART_PLAYER:
                ismartvPlayer = new DaisyPlayer(MODE_SMART_PLAYER);
                break;
            case MODE_QIYI_PLAYER:
                ismartvPlayer = new QiyiPlayer();
                break;
//            case MODE_PRELOAD_PLAYER:
//                ismartvPlayer = new DaisyPlayer(MODE_PRELOAD_PLAYER);
//                break;
        }
        if (ismartvPlayer == null) {
            Log.e(TAG, "Not support player mode.");
            throw new IllegalAccessError("Not support player mode.");
        }
        Log.e(TAG, "new IsmartvPlayer success.");
        ismartvPlayer.setItemEntity(mItemEntity);
        ismartvPlayer.setStartPosition(mStartPosition);
        ismartvPlayer.setIsPreview(mIsPreview);
        return ismartvPlayer;
    }

    public void release() {
        mPlayerMode = -1;
        mItemEntity = null;
        sInstance = null;
    }

}
