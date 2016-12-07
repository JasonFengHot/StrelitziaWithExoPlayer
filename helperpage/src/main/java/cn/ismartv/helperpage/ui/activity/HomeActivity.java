package cn.ismartv.helperpage.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import cn.ismartv.helperpage.R;
import cn.ismartv.helperpage.ui.adapter.IndicatorAdapter;
import cn.ismartv.helperpage.ui.fragment.FeedbackFragment;
import cn.ismartv.helperpage.ui.fragment.HelpFragment;
import cn.ismartv.helperpage.ui.fragment.NodeFragment;
import cn.ismartv.helperpage.ui.widget.indicator.IconPagerIndicator;
import cn.ismartv.helperpage.ui.widget.indicator.RotationPagerTransformer;
import cn.ismartv.helperpage.ui.widget.indicator.ViewPagerScroller;

/**
 * Created by huaijie on 2015/4/7.
 */
public class HomeActivity extends FragmentActivity {
    private IndicatorAdapter indicatorAdapter;
    private ArrayList<Fragment> fragments;

    private ViewPager viewPager;
    private IconPagerIndicator pagerIndicator;
    private NodeFragment nodeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_home);


        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerIndicator = (IconPagerIndicator) findViewById(R.id.indicator);
        nodeFragment=new NodeFragment();
        fragments = new ArrayList<Fragment>();
        fragments.add(nodeFragment);
        fragments.add(new FeedbackFragment());
        fragments.add(new HelpFragment());

        ViewPagerScroller scroller = new ViewPagerScroller(this);
        scroller.setScrollDuration(1500);
        scroller.initViewPagerScroll(viewPager);

        viewPager.setPageTransformer(false, new RotationPagerTransformer());
        indicatorAdapter = new IndicatorAdapter(getSupportFragmentManager(), fragments);

        viewPager.setAdapter(indicatorAdapter);
        pagerIndicator.setViewPager(viewPager);

        pagerIndicator.setCurrentItem(position);
    }
}
