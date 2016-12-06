package cn.ismartv.helperpage.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Spinner;

/**
 * Created by huaijie on 1/9/15.
 */
public class SakuraSpinner extends Spinner {
    public SakuraSpinner(Context context) {
        super(context);
    }

    public SakuraSpinner(Context context, int mode) {
        super(context, mode);
    }

    public SakuraSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SakuraSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SakuraSpinner(Context context, AttributeSet attrs, int defStyle, int mode) {
        super(context, attrs, defStyle, mode);
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE)) {
            setClickable(true);
            setFocusableInTouchMode(true);
            setFocusable(true);
            requestFocusFromTouch();
            requestFocus();

        } else {
            clearFocus();
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
