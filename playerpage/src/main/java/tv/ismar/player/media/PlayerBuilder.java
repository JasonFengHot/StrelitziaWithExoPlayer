package tv.ismar.player.media;

import android.app.Activity;
import android.util.Log;
import android.view.SurfaceView;
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

    private byte mPlayerMode = -1;
    private Activity mContext;
    private ItemEntity mItemEntity;
    private SurfaceView mSurfaceView;
    private FrameLayout mContainer;
    private int mStartPosition = 0;

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

    public PlayerBuilder setActivity(Activity context) {
        mContext = context;
        return this;
    }

    public PlayerBuilder setItemEntity(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
        return this;
    }

    public PlayerBuilder setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        return this;
    }

    public PlayerBuilder setContainer(FrameLayout container) {
        mContainer = container;
        return this;
    }

    public PlayerBuilder setStartPosition(int startPosition) {
        mStartPosition = startPosition;
        return this;
    }

    public IsmartvPlayer build() {
        if (mPlayerMode <= 0) {
            Log.e(TAG, "Must call setPlayerMode first.");
            throw new IllegalAccessError("Must call setPlayerMode first.");
        }
        if (mContext == null) {
            Log.e(TAG, "Must call setActivity first.");
            throw new IllegalAccessError("Must call setActivity first.");
        }
        if (mItemEntity == null) {
            Log.e(TAG, "Must call setItemEntity first.");
            throw new IllegalAccessError("Must call setItemEntity first.");
        }
        if (mSurfaceView == null) {
            Log.e(TAG, "Must call setSurfaceView first.");
            throw new IllegalAccessError("Must call setSurfaceView first.");
        }
        if (mContainer == null) {
            Log.e(TAG, "Must call setContainer first.");
            throw new IllegalAccessError("Must call setContainer first.");
        }
        IsmartvPlayer ismartvPlayer = null;
        switch (mPlayerMode) {
            case MODE_SMART_PLAYER:
                ismartvPlayer = new DaisyPlayer();
                break;
            case MODE_QIYI_PLAYER:
                ismartvPlayer = new QiyiPlayer();
                break;
        }
        if (ismartvPlayer == null) {
            Log.e(TAG, "Not support player mode.");
            throw new IllegalAccessError("Not support player mode.");
        }
        Log.e(TAG, "new IsmartvPlayer success.");
        ismartvPlayer.setContext(mContext);
        ismartvPlayer.setItemEntity(mItemEntity);
        ismartvPlayer.setSurfaceView(mSurfaceView);
        ismartvPlayer.setContainer(mContainer);
        ismartvPlayer.setStartPosition(mStartPosition);
        return ismartvPlayer;
    }

    public void release() {
        mPlayerMode = -1;
        mContext = null;
        mItemEntity = null;
        mSurfaceView = null;
        mContainer = null;
        sInstance = null;
    }

}
