package tv.ismar.app.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

import tv.ismar.app.core.DaisyUtils;


/**
 * Created by zhangjiqiang on 15-6-18.
 */
public class MyViewGroup extends RadioGroup {
    private final static int VIEW_MARGIN_X = 31;
    private final static int VIEW_MARGIN_Y = 40;
    private float rate;
    public MyViewGroup(Context context) {
        super(context);
        rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
    }

//    public MyViewGroup(Context context, AttributeSet attrs, int defStyle) {
//
//        super(context, attrs, defStyle);
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int index = 0; index < getChildCount(); index++) {

            final View child = getChildAt(index);

            // measure

            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
//@Override
//protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//    int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
//    int childCount = getChildCount();
//    int x = 0;
//    int y = 0;
//    int row = 0;
//
//    for (int index = 0; index < childCount; index++) {
//        final View child = getChildAt(index);
//        if (child.getVisibility() != View.GONE) {
//            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
//            // 此处增加onlayout中的换行判断，用于计算所需的高度
//            int width = child.getMeasuredWidth();
//            int height = child.getMeasuredHeight();
//            x += width ;
//            y = row * (height) + height ;
//            if (x > maxWidth) {
//                x = width ;
//                row++;
//                y = row * (height ) + height ;
//            }
//        }
//    }
//    // 设置容器所需的宽度和高度
//    setMeasuredDimension(maxWidth, y);
//    super.onMeasure(maxWidth, y);
//}
//    @Override
//    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
//
//
//
//        final int count = getChildCount();
//
//        int row = 0;// which row lay you view relative to parent
//
//        int lengthX = arg1; // right position of child relative to parent
//
//        int lengthY = arg2; // bottom position of child relative to parent
//
//        for (int i = 0; i < count; i++) {
//
//            final View child = this.getChildAt(i);
//
//            int width = child.getMeasuredWidth();
//
//            int height = child.getMeasuredHeight();
//
//
//            int textLength = (int) ((TextView)child).getPaint().measureText(((TextView)child).getText().toString());
//            lengthX += textLength + VIEW_MARGIN_X;
//            lengthY = row * (height + VIEW_MARGIN_Y) + VIEW_MARGIN_Y + height
//                    + arg2;
//
//            // if it can't drawing on a same line , skip to next line
//
//            if (lengthX > arg3) {
//
//                lengthX = width + VIEW_MARGIN_X + arg1;
//
//                row++;
//
//                lengthY = row * (height + VIEW_MARGIN_Y) + VIEW_MARGIN_Y + height
//                        + arg2;
//
//            }
//
//            child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
//
//        }
//    }
@Override
protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int childCount = getChildCount();
    int maxWidth = r - l;
    int x = 0;
    int y = 0;
    int row = 0;
    for (int i = 0; i < childCount; i++) {
        final View child = this.getChildAt(i);
        if (child.getVisibility() != View.GONE) {
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            x += width+VIEW_MARGIN_X/1;
            y = row * (height + (int)(VIEW_MARGIN_Y/1) ) + height;
            if (x > maxWidth) {
                x = width + (int)(VIEW_MARGIN_X/1);
                row++;
                y = row * (height + (int)(VIEW_MARGIN_Y/1)) + height  ;
            }
            child.layout(x - width, y - height, x, y);
        }
    }
}

    private int getTextLength(String displayText){
        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
// Define the string.

// Measure the width of the text string.
        float textWidth = mTextPaint.measureText(displayText);
        return (int) textWidth;
    }


}
