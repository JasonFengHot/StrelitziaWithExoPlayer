package tv.ismar.helperpage.ui.widget.indicator;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by huaijie on 2015/4/8.
 */
public class IconImageView extends ImageView {
    private boolean selected = false;


    public IconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setSelect(boolean selected) {
        if (selected) {
            scaleToLarge(this);
        } else if (this.selected) {
            scaleToNormal(this);
        }
        this.selected = selected;
        invalidate();
    }


    private void scaleToLarge(View view) {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, new float[]{1.0F, 1.5F});
        objectAnimatorX.setDuration(100L);
        objectAnimatorX.start();
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, new float[]{1.0F, 1.5F});
        objectAnimatorY.setDuration(100L);
        objectAnimatorY.start();
    }


    private void scaleToNormal(View view) {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(view, SCALE_X, new float[]{1.5F, 1.0F});
        objectAnimatorX.setDuration(100L);
        objectAnimatorX.start();
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(view, SCALE_Y, new float[]{1.5F, 1.0F});
        objectAnimatorY.setDuration(100L);
        objectAnimatorY.start();
    }


}
