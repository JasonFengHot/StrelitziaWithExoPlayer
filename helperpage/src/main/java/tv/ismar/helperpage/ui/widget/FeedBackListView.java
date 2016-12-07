package tv.ismar.helperpage.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by huaijie on 1/13/15.
 */
public class FeedBackListView extends ListView {
    public FeedBackListView(Context context) {
        super(context);
    }

    public FeedBackListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FeedBackListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return true;
    }
}
