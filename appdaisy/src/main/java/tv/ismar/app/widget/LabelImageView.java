package tv.ismar.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import tv.ismar.app.R;

/**
 * Created by beaver on 16-8-23.
 */
public class LabelImageView extends FrameLayout {

    private final String TAG = "LH/LabelImageView";

    private String livUrl;
    private Drawable livSelectorDrawable;
    private Drawable livErrorDrawable;
    private int livContentPadding;
    private int livLabelHeight;
    private String livLabelText;
    private int livLabelColor;
    private int livLabelSize;
    private int livLabelBackColor;
    private int livVipPosition;
    private String livVipUrl;
    private int livVipSize;
    private float livRate;
    private int livRateColor;
    private int livRateSize;
    private int livRateMarginRight;
    private int livRateMarginBottom;

    private ImageView imageView, vipImageView;
    private TextView textView;

    private int LEFTTOP = 0;
    private int RIGHTTOP = 1;
    private int GONE = -1;
    private Rect mBound;

    private Context mContext;

    public LabelImageView(Context context) {
        this(context, null);
    }

    public LabelImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LabelImageView);
        livUrl = typedArray.getString(R.styleable.LabelImageView_livUrl);
        livSelectorDrawable = typedArray.getDrawable(R.styleable.LabelImageView_livSelectorDrawable);
        livErrorDrawable = typedArray.getDrawable(R.styleable.LabelImageView_livErrorDrawable);
        livContentPadding = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livContentPadding, dp2px(5));
        livLabelHeight = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livLabelHeight, dp2px(30));
        livLabelText = typedArray.getString(R.styleable.LabelImageView_livLabelText);
        livLabelColor = typedArray.getColor(R.styleable.LabelImageView_livLabelColor, Color.WHITE);
        livLabelSize = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livLabelSize, sp2px(16));
        livLabelBackColor = typedArray.getColor(R.styleable.LabelImageView_livLabelBackColor, Color.parseColor("#33000000"));
        livVipPosition = typedArray.getInt(R.styleable.LabelImageView_livVipPosition, GONE);
        livVipUrl = typedArray.getString(R.styleable.LabelImageView_livVipUrl);
        livVipSize = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livVipSize, dp2px(40));
        livRate = typedArray.getFloat(R.styleable.LabelImageView_livRate, 0);
        livRateColor = typedArray.getColor(R.styleable.LabelImageView_livRateColor, Color.parseColor("#ff9000"));
        livRateSize = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livRateSize, sp2px(14));
        livRateMarginRight = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livRateMarginRight, dp2px(5));
        livRateMarginBottom = typedArray.getDimensionPixelSize(R.styleable.LabelImageView_livRateMarginBottom, dp2px(5));
        typedArray.recycle();

        initView();
        setWillNotDraw(false);
    }

    private void initView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(imageView);

        if (livVipPosition > 0) {
            FrameLayout.LayoutParams ltparams;
            if (livVipSize == 0) {
                ltparams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            } else {
                ltparams = new FrameLayout.LayoutParams(livVipSize, livVipSize);
            }
            if (livVipPosition == 0) {
                ltparams.gravity = Gravity.LEFT | Gravity.TOP;
            } else if (livVipPosition == 1) {
                ltparams.gravity = Gravity.RIGHT | Gravity.TOP;
            }
            vipImageView = new ImageView(mContext);
            vipImageView.setLayoutParams(ltparams);
            vipImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(vipImageView);

        }

        FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, livLabelHeight);
        labelParams.gravity = Gravity.BOTTOM;
        textView = new TextView(mContext);
        textView.setLayoutParams(labelParams);
        textView.setBackgroundColor(livLabelBackColor);
        textView.setGravity(Gravity.CENTER);
        textView.setText(livLabelText);
        textView.setTextSize(livLabelSize);
        textView.setTextColor(livLabelColor);
        addView(textView);

        if (livRate > 0) {
            FrameLayout.LayoutParams rateParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rateParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            rateParams.bottomMargin = livRateMarginBottom;
            rateParams.rightMargin = livRateMarginRight;
            TextView rateTextView = new TextView(mContext);
            rateTextView.setText(String.valueOf(livRate));
            rateTextView.setTextColor(livRateColor);
            rateTextView.setTextSize(livRateSize);
            addView(rateTextView);
        }

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Log.i(TAG, "draw");
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawLine(-10, -10, 12, 12, paint);
    }

    private void setBackgroundBorder(boolean focus) {
        if (focus) {
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getWidth() + dp2px(2), getHeight()+dp2px(2));
//            imageView.setLayoutParams(layoutParams);
//            imageView.setBackgroundDrawable(livSelectorDrawable);
//            setBackgroundResource(R.mipmap.vod_item_selected);
        } else {
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getWidth(), getHeight());
//            imageView.setLayoutParams(layoutParams);
//            imageView.setBackgroundDrawable(new ColorDrawable(0));
        }
        invalidate();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        Log.i(TAG, "onFocusChanged:" + gainFocus);
        if (gainFocus) {
            setBackgroundBorder(true);
        } else {
            setBackgroundBorder(false);
        }
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
        Log.i(TAG, "onHoverChanged:" + hovered);
        if (hovered) {
            setBackgroundBorder(true);
        } else {
            setBackgroundBorder(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundBorder(true);
                return true;
            case MotionEvent.ACTION_UP:
                setBackgroundBorder(false);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void asyncLoadImage() {
        Log.i(TAG, "asyncLoadImage:" + livUrl);
        if (imageView != null) {
            if (TextUtils.isEmpty(livUrl)) {
                imageView.setImageDrawable(livErrorDrawable);
            } else {
                Picasso.with(mContext).load(livUrl)
                        .placeholder(livErrorDrawable)
                        .error(livErrorDrawable)
                        .into(imageView);
            }
        }
    }

    private void asyncLoadVipImage() {
        if (vipImageView != null && !TextUtils.isEmpty(livVipUrl)) {
            Picasso.with(mContext).load(livVipUrl)
                    .into(vipImageView);
        }
    }

    public String getLivUrl() {
        return livUrl;
    }

    public void setLivUrl(String livUrl) {
        this.livUrl = livUrl;
        asyncLoadImage();
    }

    public Drawable getLivSelectorDrawable() {
        return livSelectorDrawable;
    }

    public void setLivSelectorDrawable(Drawable livSelectorDrawable) {
        this.livSelectorDrawable = livSelectorDrawable;
    }

    public Drawable getLivErrorDrawable() {
        return livErrorDrawable;
    }

    public void setLivErrorDrawable(Drawable livErrorDrawable) {
        this.livErrorDrawable = livErrorDrawable;
    }

    public int getLivContentPadding() {
        return livContentPadding;
    }

    public void setLivContentPadding(int livContentPadding) {
        this.livContentPadding = livContentPadding;
    }

    public int getLivLabelHeight() {
        return livLabelHeight;
    }

    public void setLivLabelHeight(int livLabelHeight) {
        this.livLabelHeight = livLabelHeight;
    }

    public String getLivLabelText() {
        return livLabelText;
    }

    public void setLivLabelText(String livLabelText) {
        this.livLabelText = livLabelText;
    }

    public int getLivLabelColor() {
        return livLabelColor;
    }

    public void setLivLabelColor(int livLabelColor) {
        this.livLabelColor = livLabelColor;
    }

    public int getLivLabelSize() {
        return livLabelSize;
    }

    public void setLivLabelSize(int livLabelSize) {
        this.livLabelSize = livLabelSize;
    }

    public int getLivLabelBackColor() {
        return livLabelBackColor;
    }

    public void setLivLabelBackColor(int livLabelBackColor) {
        this.livLabelBackColor = livLabelBackColor;
    }

    public int getLivVipPosition() {
        return livVipPosition;
    }

    public void setLivVipPosition(int livVipPosition) {
        this.livVipPosition = livVipPosition;
    }

    public String getLivVipUrl() {
        return livVipUrl;
    }

    public void setLivVipUrl(String livVipUrl) {
        this.livVipUrl = livVipUrl;
        asyncLoadVipImage();
    }

    public int getLivVipSize() {
        return livVipSize;
    }

    public void setLivVipSize(int livVipSize) {
        this.livVipSize = livVipSize;
    }

    public float getLivRate() {
        return livRate;
    }

    public void setLivRate(float livRate) {
        this.livRate = livRate;
    }

    public int getLivRateColor() {
        return livRateColor;
    }

    public void setLivRateColor(int livRateColor) {
        this.livRateColor = livRateColor;
    }

    public int getLivRateSize() {
        return livRateSize;
    }

    public void setLivRateSize(int livRateSize) {
        this.livRateSize = livRateSize;
    }

    public int getLivRateMarginRight() {
        return livRateMarginRight;
    }

    public void setLivRateMarginRight(int livRateMarginRight) {
        this.livRateMarginRight = livRateMarginRight;
    }

    public int getLivRateMarginBottom() {
        return livRateMarginBottom;
    }

    public void setLivRateMarginBottom(int livRateMarginBottom) {
        this.livRateMarginBottom = livRateMarginBottom;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
