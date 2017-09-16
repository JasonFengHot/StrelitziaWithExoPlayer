package tv.ismar.searchpage.weight;

/** Created by zhangjiqiang on 15-7-12. */
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/** Created by zhangjiqiang on 2015/7/12. */
public class RotateTextView extends TextView {
    private static final int DEFAULT_DEGREES = 45;
    private int mDegrees = DEFAULT_DEGREES;
    private float rate;

    public RotateTextView(Context context) {
        super(context, null);
    }

    public RotateTextView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.textViewStyle);
        this.setGravity(Gravity.CENTER);
        //        rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (mDegrees == 45) {
            canvas.translate(getCompoundPaddingLeft() + 11, getExtendedPaddingTop() - 14);
        } else if (mDegrees == -45) {
            canvas.translate(getCompoundPaddingLeft() - 11, getExtendedPaddingTop() - 8);
        } else {
            canvas.translate(getCompoundPaddingLeft() - 11, getExtendedPaddingTop() - 14);
        }
        canvas.rotate(mDegrees, this.getWidth() / 2f, this.getHeight() / 2f);
        super.onDraw(canvas);
        canvas.restore();
    }

    public void setDegrees(int degrees) {
        mDegrees = degrees;
    }
}
