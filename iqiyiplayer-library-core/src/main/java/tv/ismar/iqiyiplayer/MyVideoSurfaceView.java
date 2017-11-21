package tv.ismar.iqiyiplayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;

import com.qiyi.sdk.player.VideoSurfaceView;

/**
 * demo code
 */

public class MyVideoSurfaceView extends VideoSurfaceView {

    // begin {@
    private boolean mIgnoreWindowChange;

    public MyVideoSurfaceView(Context context) {
        super(context);
        init();
    }

    public MyVideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyVideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        getHolder().setFormat(PixelFormat.RGBA_8888);
    }

    public boolean getIgnoreWindowChange() {
        return mIgnoreWindowChange;
    }

    public void setIgnoreWindowChange(boolean ignoreWindowChange) {
        mIgnoreWindowChange = ignoreWindowChange;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (!mIgnoreWindowChange) {
            super.onWindowVisibilityChanged(visibility);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!mIgnoreWindowChange) {
            super.onDetachedFromWindow();
        }
    }
    // end @}

}
