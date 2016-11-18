package tv.ismar.searchpage.weight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import tv.ismar.searchpage.R;

/**
 * Created by coco on 2016/2/16.
 */


public class ItemContainer extends RelativeLayout {

    private Rect mBound;
    private NinePatchDrawable mDrawable;
    private Rect mRect;
    private boolean isDrawBorder = false;

    public ItemContainer(Context context) {
        super(context);
        init();
    }

    public void setDrawBorder(boolean isDrawBorder) {
        this.isDrawBorder = isDrawBorder;
    }

    public ItemContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ItemContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        setWillNotDraw(false);
        mRect = new Rect();
        mBound = new Rect();
        mDrawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.key_focus);//nav_focused_2,poster_shadow_4
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDrawBorder) {
            System.out.println("HomeItemContainer focus : true ");
            super.getDrawingRect(mRect);
            int margin= (int) getResources().getDimension(R.dimen.focus_rect);
            mBound.set(-margin + mRect.left, -margin + mRect.top, margin+ mRect.right, mRect.bottom + margin);
            mDrawable.setBounds(mBound);
            canvas.save();
            mDrawable.draw(canvas);
            canvas.restore();
        }
        super.onDraw(canvas);
    }

//    @Override
//    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
//        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//        if (gainFocus) {
//            isDrawBorder = true;
////			bringToFront();
////			getRootView().requestLayout();
////			getRootView().invalidate();
////			zoomOut();
//        } else {
//            isDrawBorder = false;
////			zoomIn();
//        }
//    }

}
