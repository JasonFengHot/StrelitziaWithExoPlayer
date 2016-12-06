package cn.ismartv.helperpage.ui.widget.indicator;

import android.support.v4.view.ViewPager;

/**
 * Created by huaijie on 2015/4/8.
 */
public interface PagerIndicator extends ViewPager.OnPageChangeListener {


    void setViewPager(ViewPager viewPager);

    void setCurrentItem(int item);


}
