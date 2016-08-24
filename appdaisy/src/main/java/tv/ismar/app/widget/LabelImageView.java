package tv.ismar.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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

    private ImageView imageView;
    private TextView textView;

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
//        String livUrl = typedArray.getString(R.styleable.LabelImageView_liv_url);
//        Drawable livSelectorDrawable;
//        Drawable livErrorDrawable;
//        int livContentPadding;
//        int livLabelHeight;
//        int livLabelColor;
//        int livLabelSize;
//        int livLabelBackColor;
//        int livVipPosition;
//         String livVipUrl;
//         int livVipSize;
//         float livRate;
//         int livRateColor;
//         int livRateSize;
//         int livRateMarginRight;
//         int livRateMarginBottom;

        typedArray.recycle();

        setPadding(livContentPadding, livContentPadding, livContentPadding, livContentPadding);
        initView();
    }

    private void initView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(layoutParams);
        asyncLoadImage();

//        if (leftTopDrawable != null) {
//            FrameLayout.LayoutParams ltparams = new FrameLayout.LayoutParams(drawableSize, drawableSize);
//            ltparams.gravity = Gravity.LEFT | Gravity.TOP;
//            ImageView ltImage = new ImageView(mContext);
//            ltImage.setLayoutParams(ltparams);
//        }

//        FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, labelHeight);
//        labelParams.gravity = Gravity.BOTTOM;
//        textView = new TextView(mContext);
//        textView.setLayoutParams(labelParams);
//        textView.setBackgroundColor(labelBackColor);
//        textView.setGravity(Gravity.CENTER);
//        textView.setText(labelText);
//        textView.setTextSize(labelTextSize);
//        textView.setTextColor(labelTextColor);
//
//        if (rate > 0) {
//            FrameLayout.LayoutParams rateParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//            rateParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
//            rateParams.bottomMargin = rateMarginBottom;
//            rateParams.rightMargin = rateMarginRight;
//            TextView rateTextView = new TextView(mContext);
//            rateTextView.setText(String.valueOf(rate));
//            rateTextView.setTextColor(rateColor);
//            rateTextView.setTextSize(rateSize);
//        }

    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//        if (gainFocus) {
//            setBackgroundDrawable(selectorDrawable);
//        } else {
//            setBackgroundColor(Color.TRANSPARENT);
//        }
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
//        if (hovered) {
//            setBackgroundDrawable(selectorDrawable);
//        } else {
//            setBackgroundColor(Color.TRANSPARENT);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                setBackgroundDrawable(selectorDrawable);
                break;
            case MotionEvent.ACTION_UP:
                setBackgroundColor(Color.TRANSPARENT);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void asyncLoadImage() {
        if (imageView != null) {
//            Picasso.with(mContext).load(url)
//                    .placeholder(errDrawable)
//                    .error(errDrawable)
//                    .centerCrop()
//                    .into(imageView);
        }
    }

    public String getLivUrl() {
        return livUrl;
    }

    public void setLivUrl(String livUrl) {
        this.livUrl = livUrl;
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
