package tv.ismar.homepage.widget;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

/** Created by huaijie on 6/15/15. */
public class ItemViewFocusChangeListener implements View.OnFocusChangeListener {
    @Override
    public void onFocusChange(View itemView, boolean hasFocus) {
        if (hasFocus) {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation =
                    new ScaleAnimation(
                            1,
                            1.05f,
                            1,
                            1.05f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
            scaleAnimation.setDuration(200);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            itemView.startAnimation(animationSet);
        } else {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation =
                    new ScaleAnimation(
                            1.05f,
                            1f,
                            1.05f,
                            1f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
            scaleAnimation.setDuration(200);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            itemView.startAnimation(animationSet);
        }
    }
}
