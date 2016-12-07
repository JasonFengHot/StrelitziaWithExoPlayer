package tv.ismar.helperpage.ui.widget.indicator;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * Created by huaijie on 3/6/15.
 */
public class ViewPagerScroller extends Scroller {
    private int DEFAULT_SCROLL_DURATION = 2000;

    public ViewPagerScroller(Context context) {
        super(context);
    }

    public void setScrollDuration(int duration) {
        this.DEFAULT_SCROLL_DURATION = duration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, DEFAULT_SCROLL_DURATION);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, DEFAULT_SCROLL_DURATION);
    }

    public void initViewPagerScroll(ViewPager viewPager) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
