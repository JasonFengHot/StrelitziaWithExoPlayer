package tv.ismar.homepage.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
//import org.apache.commons.lang3.StringUtils;
import com.blankj.utilcode.utils.StringUtils;

import tv.ismar.app.widget.AsyncImageView;
import tv.ismar.homepage.R;

public class LabelImageView3 extends AsyncImageView {

	private String focustitle = "";
	private int focustitlesize;
	private float focuspaddingtop;
	private int textBack;
	private float focustitlepaddingtop;
	private int frontcolor;
	private int modetype;
	private int carouse_color;
	private boolean customfocus;
	private boolean customselected;
	private int maxfocustitle;
    private boolean drawBorder;

	public void setDrawBorder(boolean drawBorder) {
		this.drawBorder = drawBorder;
	}

	public void setNeedzoom(boolean needzoom) {
		this.needzoom = needzoom;
	}

	private boolean needzoom;
	private Animation scaleSmallAnimation;
	private Animation scaleBigAnimation;
	private Rect mBound;
	private NinePatchDrawable mDrawable;
	private Rect mRect;

	public void setModetype(int modetype) {
		this.modetype = modetype;
	}

	public String getFocustitle() {
		return focustitle;
	}

	public void setFocustitle(String focustitle) {
		this.focustitle = focustitle;
	}

	public int getFocustitlesize() {
		return focustitlesize;
	}

	public void setFocustitlesize(int focustitlesize) {
		this.focustitlesize = focustitlesize;
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
		focuspaddingtop = a.getFloat(
				R.styleable.LabelImageView3_focuspaddingtop3, 0.85f);
		focustitlesize = a.getDimensionPixelOffset(
				R.styleable.LabelImageView3_focustextsize3, 0);
		focustitlepaddingtop = a.getFloat(
				R.styleable.LabelImageView3_focustextpaddingtop3, 0.97f);
		frontcolor = a.getInt(R.styleable.LabelImageView3_frontcolor3, 0);
		carouse_color = context.getResources().getColor(R.color.carousel_focus);
		customfocus = a.getBoolean(R.styleable.LabelImageView3_customfocus3,
				false);
		needzoom = a.getBoolean(R.styleable.LabelImageView3_needzoom3, false);
		maxfocustitle = a.getInt(R.styleable.LabelImageView3_maxfocustitle3, 0);
		a.recycle();
		setWillNotDraw(false);
		mRect = new Rect();
		mBound = new Rect();
		mDrawable = (NinePatchDrawable) getResources().getDrawable(
				R.drawable.vod_gv_selector);
		textBack = getResources().getColor(R.color.color_alpha_black);
	}

	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (needzoom) {
			if (gainFocus) {
				if(getId() != R.id.vaiety_post && getId() != R.id.image_switcher){
					bringToFront();
				}
				drawBorder = true;
				getRootView().requestLayout();
				getRootView().invalidate();
				zoomOut();
			} else {
				drawBorder = false;
				zoomIn();
			}
		}
	}

	public void setFrontcolor(int frontcolor) {
		this.frontcolor = frontcolor;
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
			if(isFocusable() && isFocusableInTouchMode())
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
		int paddingright = getPaddingRight();
		int paddingtop = getPaddingTop();
		int paddingBottom = getPaddingBottom();
		if (width <= 0)
			width = getWidth();
		if (height <= 0)
			height = getHeight();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		// 绘制角标
		if (modetype > 0 && getDrawable() != null) {
			int resId = R.drawable.entertainment_bg;
			switch (modetype) {
			case 1:
				resId = R.drawable.entertainment_bg;
				break;
			case 2:
				resId = R.drawable.variety_bg;
				break;
			case 3:
				resId = R.drawable.all_match;
				break;
			case 4:
				resId = R.drawable.living;
				break;
			case 5:
				resId = R.drawable.beonline;
				break;
			case 6:
				resId = R.drawable.collection;
				break;
			}
//			InputStream is = getResources().openRawResource(resId);
			Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),resId);
			canvas.drawBitmap(mBitmap, width - mBitmap.getWidth(), paddingtop, paint);
		}
		// 绘制看点背景
		paint.setColor(Color.WHITE);
		if (!StringUtils.isEmpty(focustitle) && focustitle.length() > 0) {
			int shadowPt = getResources().getDimensionPixelOffset(R.dimen.home_label_img_text_pt);
			int shadowPb = getResources().getDimensionPixelOffset(R.dimen.home_label_img_text_pb);
			int shadowT = height - (shadowPt + shadowPb + focustitlesize);
			if (maxfocustitle > 0 && focustitle.length() > maxfocustitle) {
				focustitle = focustitle.substring(0, maxfocustitle);
            }
			paint.setColor(textBack);
			canvas.drawRect(new Rect(getPaddingLeft(),
					shadowT, width - paddingright,
					height - paddingBottom), paint);
			// 看点内容
			paint.setColor(Color.WHITE);
			paint.setTextSize(focustitlesize);
			// FontMetrics fm = paint.getFontMetrics();
			// int focusTextHeight = (int)Math.ceil(fm.descent - fm.ascent);
			float focuswidth = paint.measureText(focustitle);
			int xfocus = (int) ((width - focuswidth) / 2);
			canvas.drawText(focustitle, xfocus,
					(int) (focustitlepaddingtop * height), paint);
		}
		// 绘制遮罩效果
		if (frontcolor != 0) {
			if (!customselected) {
				paint.setColor(frontcolor);
				canvas.drawRect(mRect, paint);
			}
		}

		// if (customfocus) {
		if (drawBorder) {
			mBound.set(-21+mRect.left, -21+mRect.top, 21+mRect.right, mRect.bottom+21);
			mDrawable.setBounds(mBound);
			canvas.save();
			mDrawable.draw(canvas);
			canvas.restore();
		}
		// }
		getRootView().requestLayout();
		getRootView().invalidate();
	}

	public void setCustomfocus(boolean customfocus) {
		this.customselected = customfocus;
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

}
