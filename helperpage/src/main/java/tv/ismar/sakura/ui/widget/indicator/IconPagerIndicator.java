package tv.ismar.helperpage.ui.widget.indicator;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import tv.ismar.helperpage.R;


/**
 * Created by huaijie on 2015/4/8.
 */
public class IconPagerIndicator extends LinearLayout implements PagerIndicator, View.OnClickListener {
    private static final String TAG = "IconPagerIndicator";


    private ViewPager viewPager;

    private IconImageView[] icons = new IconImageView[3];

    public IconPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

    }


    @Override
    public void setViewPager(ViewPager viewPager) {
        if (viewPager == this.viewPager) {
            return;
        }

        if (this.viewPager != null) {
            this.viewPager.setOnPageChangeListener(null);
        }


        PagerAdapter adapter = viewPager.getAdapter();

        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(this);


        initializeView();
    }


    private void initializeView() {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.sakura_indicator_icon_layout, null);

        IconImageView nodeIcon = (IconImageView) view.findViewById(R.id.icon_node);
        IconImageView feedbackIcon = (IconImageView) view.findViewById(R.id.icon_feedback);
        IconImageView helpIcon = (IconImageView) view.findViewById(R.id.icon_help);

        nodeIcon.setImageResource(R.drawable.sakura_tab_node);
        feedbackIcon.setImageResource(R.drawable.sakura_tab_feedback);
        helpIcon.setImageResource(R.drawable.sakura_tab_help);

        icons[0] = nodeIcon;
        icons[1] = feedbackIcon;
        icons[2] = helpIcon;

        

        for (IconImageView imageView : icons)
            imageView.setOnClickListener(this);

        addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        icons[0].setSelect(true);
        requestLayout();

    }

    @Override
    public void setCurrentItem(int item) {
        if (viewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        viewPager.setCurrentItem(item);
    }


    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: " + position);
        for (int i = 0; i < icons.length; i++) {
            if (position == i) {
                icons[i].setSelect(true);
            } else {
                icons[i].setSelect(false);


                
            }

        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < icons.length; i++) {
            if (icons[i].getId() == view.getId()) {
                setCurrentItem(i);
            }
        }
    }
}
