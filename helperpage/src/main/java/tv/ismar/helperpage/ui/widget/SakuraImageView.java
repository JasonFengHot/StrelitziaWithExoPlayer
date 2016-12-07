package tv.ismar.helperpage.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by huaijie on 2015/4/8.
 */
public class SakuraImageView extends ImageView {
    public SakuraImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        if (hovered) {
            requestFocus();
        } else {
            clearFocus();
        }
    }

}
