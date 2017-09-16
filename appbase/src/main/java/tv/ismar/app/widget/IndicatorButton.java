package tv.ismar.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.app.R;

/** Created by beaver on 16-8-19. */
public class IndicatorButton extends FrameLayout {

    private static final String TAG = "LH/IndicatorButton";
    private final String TAG_IMG = "INDICATOR_IMAGE";
    private final String TAG_TXT = "INDICATOR_TEXT";
    private ImageView indicatorImage;
    private TextView indicatorText;
    private Drawable indicatorDrawable;
    private int textSize;
    private int textColor;
    private String content;
    private boolean wrapContent;
    private int defaultTextSize = 18;

    public IndicatorButton(Context context) {
        this(context, null);
    }

    public IndicatorButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorButton);
        indicatorDrawable = typedArray.getDrawable(R.styleable.IndicatorButton_ib_drawable);
        textSize =
                typedArray.getDimensionPixelSize(
                        R.styleable.IndicatorButton_ib_textSize, defaultTextSize);
        textColor = typedArray.getColor(R.styleable.IndicatorButton_ib_textColor, Color.WHITE);
        content = typedArray.getString(R.styleable.IndicatorButton_ib_text);
        wrapContent = typedArray.getBoolean(R.styleable.IndicatorButton_ib_wrapContent, false);
        typedArray.recycle();

        initChildView(context);
    }

    public void setIndicatorDrawable(Drawable drawable) {
        indicatorDrawable = drawable;
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    public void setTextSize(int dimensionSize) {
        textSize = dimensionSize;
    }

    public void setIndicatorText(String text) {
        content = text;
    }

    public void setWrapContent(boolean flag) {
        wrapContent = flag;
    }

    private void initChildView(Context context) {
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        // show background indicator
        indicatorImage = new ImageView(context);
        indicatorImage.setTag(TAG_IMG);
        indicatorImage.setLayoutParams(layoutParams);
        indicatorImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        indicatorImage.setFocusable(false);
        indicatorImage.setFocusableInTouchMode(false);
        addView(indicatorImage);

        // show text
        indicatorText = new TextView(context);
        indicatorText.setTag(TAG_TXT);
        indicatorText.setLayoutParams(layoutParams);
        indicatorText.setFocusable(false);
        indicatorText.setFocusableInTouchMode(false);
        indicatorText.setSingleLine(true);
        indicatorText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        indicatorText.setTextColor(textColor);
        indicatorText.setText(content);
        addView(indicatorText);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            setIndicatorBack();
        } else {
            clearBack();
        }
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        if (hovered) {
            setIndicatorBack();
        } else {
            clearBack();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setIndicatorBack();
                return true;
            case MotionEvent.ACTION_UP:
                clearBack();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void setIndicatorBack() {
        if (indicatorImage == null || indicatorText == null) {
            Log.e(TAG, "Can not find child imageView or textView.");
            return;
        }
        if (wrapContent) {
            int width = indicatorText.getWidth();
            int height = indicatorText.getHeight();
            Log.i(TAG, "wrapContent:" + wrapContent + " width:" + width);
            if (width > 0) {
                ((FrameLayout.LayoutParams) indicatorImage.getLayoutParams()).width = width;
                ((FrameLayout.LayoutParams) indicatorImage.getLayoutParams()).height = height;
            }
        }
        indicatorImage.setImageDrawable(indicatorDrawable);
    }

    public void clearBack() {
        indicatorImage.setImageDrawable(new ColorDrawable(0));
    }
}
