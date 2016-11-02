package tv.ismar.homepage.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by huaijie on 6/29/15.
 */
public class ChildThumbImageView extends ImageView {
    public boolean isZoom;

    public ChildThumbImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //放大
    public void zoomInImage() {
    	 setPadding(0, 0, 0, 0);
//        this.isZoom = true;
//        AnimationSet animationSet = new AnimationSet(true);
//        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 1.53f,
//                Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 1f);
//        scaleAnimation.setDuration(200);
//        animationSet.addAnimation(scaleAnimation);
//        animationSet.setFillAfter(true);
//        startAnimation(animationSet);
    }

    public void zoomNormalImage() {
    	  setPadding(0, 30, 0, -30);
//        this.isZoom = false;
//        AnimationSet animationSet = new AnimationSet(true);
//        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1f, 1.53f, 1f,
//                Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 1f);
//        scaleAnimation.setDuration(200);
//        animationSet.addAnimation(scaleAnimation);
//        animationSet.setFillAfter(true);
//        startAnimation(animationSet);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            zoomInImage();
        } else {
            zoomNormalImage();
        }
    }

}
