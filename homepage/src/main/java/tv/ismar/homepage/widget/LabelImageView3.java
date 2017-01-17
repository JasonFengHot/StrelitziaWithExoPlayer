package tv.ismar.homepage.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.blankj.utilcode.utils.StringUtils;

import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.widget.AsyncImageView;
import tv.ismar.homepage.R;

public class LabelImageView3 extends AsyncImageView {

    private String title = "";
    private int textSize;
    private int textPaddingTop;
    private int textPaddingBottom;
    private int textBackColor;
    private int frontColor;
    private int modeType;
    private boolean customFocus;
    private int maxTextNum;
    private boolean drawBorder;
    private boolean needZoom;
    private Animation scaleSmallAnimation;
    private Animation scaleBigAnimation;
    private Rect mBound;
    private NinePatchDrawable mNinePatchDrawable;
    private Drawable mDrawable;
    private Rect mRect;
    private int drawablePadding;
    private Bitmap cornerBitmap;
    private Bitmap corner1, corner2, corner3, corner4, corner5, corner6;
    private Paint paint;

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
    }

    public void setNeedZoom(boolean needZoom) {
        this.needZoom = needZoom;
    }

    public void setModeType(int modeType) {
        this.modeType = modeType;
        if (modeType > 0) {
            switch (modeType) {
                case 1:
                    cornerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.entertainment_bg),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            false
                    );
                    break;
                case 2:
                    cornerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.variety_bg),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            false
                    );
                    break;
                case 3:
                    cornerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.all_match),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            false
                    );
                    break;
                case 4:
                    cornerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.living),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            false
                    );
                    break;
                case 5:
                    cornerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.beonline),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            false
                    );
                    break;
                case 6:
                    cornerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.collection),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            false
                    );
                    break;
                default:
                    cornerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.entertainment_bg),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            getResources().getDimensionPixelOffset(R.dimen.label_image_corner_size),
                            false
                    );
                    break;
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public LabelImageView3(Context context) {
        this(context, null);
    }

    public LabelImageView3(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelImageView3(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LabelImageView3);
        textSize = a.getDimensionPixelOffset(
                R.styleable.LabelImageView3_label3_textSize, 0);
        textPaddingTop = a.getDimensionPixelOffset(R.styleable.LabelImageView3_label3_textPaddingTop, 0);
        textPaddingBottom = a.getDimensionPixelOffset(R.styleable.LabelImageView3_label3_textPaddingBottom, 0);
        textBackColor = a.getColor(R.styleable.LabelImageView3_label3_textBackColor, Color.parseColor("#B2000000"));
        frontColor = a.getColor(R.styleable.LabelImageView3_label3_frontColor, 0);
        customFocus = a.getBoolean(R.styleable.LabelImageView3_label3_customFocus, false);
        needZoom = a.getBoolean(R.styleable.LabelImageView3_label3_needZoom, false);
        maxTextNum = a.getInt(R.styleable.LabelImageView3_label3_maxText, 0);
        Drawable drawable = a.getDrawable(R.styleable.LabelImageView3_label3_drawable);
        a.recycle();
        setWillNotDraw(false);
        mRect = new Rect();
        mBound = new Rect();
        paint = new Paint();
        if (drawable == null) {
            mNinePatchDrawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.vod_img_selector);
            drawablePadding = 22;
        } else {
            mDrawable = drawable;
        }

    }

    protected void onFocusChanged(boolean gainFocus, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (needZoom) {
            if (gainFocus) {
                if (getId() != R.id.vaiety_post && getId() != R.id.image_switcher) {
                    bringToFront();
                }
                drawBorder = true;
                getParent().requestLayout();
                getRootView().requestLayout();
                getRootView().invalidate();
            } else {
                drawBorder = false;
            }
        }
    }

    public void setFrontColor(int frontColor) {
        this.frontColor = frontColor;
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
//			drawBorder = true;
//			requestFocus();
//			invalidate();
//			break;
            case MotionEvent.ACTION_HOVER_MOVE:
//			drawBorder = true;
                if (isFocusable() && isFocusableInTouchMode())
                    requestFocus();
                setHovered(true);
//			invalidate();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                setHovered(false);
//			drawBorder = false;
//			invalidate();
                break;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        super.getDrawingRect(mRect);
        int width = getLayoutParams().width;
        int height = getLayoutParams().height;
        int paddingleft = getPaddingLeft();
        int paddingright = getPaddingRight();
        int paddingtop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        if (width <= 0)
            width = getWidth();
        if (height <= 0)
            height = getHeight();
        paint.setAntiAlias(true);
        // 绘制角标
        if (modeType > 0 && getDrawable() != null && cornerBitmap != null) {
            canvas.drawBitmap(cornerBitmap, width - cornerBitmap.getWidth(), paddingtop, paint);
        }
        // 绘制看点背景
        paint.setColor(Color.WHITE);
        if (!StringUtils.isEmpty(title) && title.length() > 0) {
            int shadowHeight = textPaddingTop + textPaddingBottom + textSize;
            int shadowT = height - shadowHeight;
            if (maxTextNum > 0 && title.length() > maxTextNum) {
                title = title.substring(0, maxTextNum);
            }
            paint.setColor(textBackColor);
            mRect.left = paddingleft;
            mRect.top = shadowT;
            mRect.right = width - paddingright;
            mRect.bottom = height - paddingBottom;
            canvas.drawRect(mRect, paint);
            // 看点内容
            paint.setColor(Color.WHITE);
            paint.setTextSize(textSize);
            // FontMetrics fm = paint.getFontMetrics();
            // int focusTextHeight = (int)Math.ceil(fm.descent - fm.ascent);
            float focuswidth = paint.measureText(title);
            int xfocus = (int) ((width - focuswidth) / 2);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            int baseline = shadowT - fontMetrics.top;
            canvas.drawText(title, xfocus, baseline, paint);
        }
        mRect.left = getPaddingLeft();
        mRect.top = getPaddingTop();
        mRect.right = width - getPaddingRight();
        mRect.bottom = height - getPaddingBottom();
        // 绘制遮罩效果
        if (frontColor != 0) {
            if (!customFocus) {
                paint.setColor(frontColor);
                canvas.drawRect(mRect, paint);
            }
        }

        if (drawBorder) {
            if (mNinePatchDrawable != null) {
                mBound.set(-drawablePadding + mRect.left, -drawablePadding + mRect.top, drawablePadding + mRect.right, drawablePadding + mRect.bottom);
                mNinePatchDrawable.setBounds(mBound);
                canvas.save();
                mNinePatchDrawable.draw(canvas);
                canvas.restore();
            } else if (mDrawable != null) {
                mBound.set(-1 + mRect.left, -3 + mRect.top, 1 + mRect.right, -2 + mRect.bottom);
                mDrawable.setBounds(mBound);
                canvas.save();
                mDrawable.draw(canvas);
                canvas.restore();
            }

        }

        getRootView().requestLayout();
        getRootView().invalidate();

    }

    public void setCustomFocus(boolean customFocus) {
        this.customFocus = customFocus;
        invalidate();
    }

    private void zoomIn() {
        if (scaleSmallAnimation == null) {
            scaleSmallAnimation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.anim_scale_small);
        }
        startAnimation(scaleSmallAnimation);
    }

    private void zoomOut() {
        if (scaleBigAnimation == null) {
            scaleBigAnimation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.anim_scale_big);
        }
        startAnimation(scaleBigAnimation);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

}
