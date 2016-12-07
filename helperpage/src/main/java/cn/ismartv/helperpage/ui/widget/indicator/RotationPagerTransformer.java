package cn.ismartv.helperpage.ui.widget.indicator;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by huaijie on 3/5/15.
 */
public class RotationPagerTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View pager, float position) {
        final float normalizedposition = Math.abs(Math.abs(position) - 1);
        pager.setScaleX(normalizedposition / 2 + 0.5f);
        pager.setScaleY(normalizedposition / 2 + 0.5f);
    }
}
