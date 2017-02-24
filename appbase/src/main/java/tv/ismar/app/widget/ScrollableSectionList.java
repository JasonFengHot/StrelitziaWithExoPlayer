package tv.ismar.app.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.Timer;
import java.util.TimerTask;

import tv.ismar.app.R;
import tv.ismar.app.core.Util;
import tv.ismar.app.entity.Section;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.ui.HGridView;


public class ScrollableSectionList extends HorizontalScrollView {

    private static final String TAG = "ScrollableSectionList";

    private boolean isChangeBarStyle = false;
    public LinearLayout mContainer;

    /*
     * current selected section index. don't change this value directly.Always use ScrollableSectionList.changeSelection(int position) to change this value.
     */
    private int mSelectPosition = 0;
    private int lastSelectPosition = 0;

    private OnSectionSelectChangedListener mSectionSelectChangedListener;

    private Rect mTempRect = new Rect();

    private boolean mSmoothScrollingEnabled = true;

//	private boolean isSectionWidthResized = false;

    private static final int LABEL_TEXT_COLOR_NOFOCUSED = 0xffffffff;
    private static final int test123 = 0xff0069b3;
    private static final int LABEL_TEXT_COLOR_FOCUSED = 0xffF8F8FF;
    private static final int LABEL_TEXT_COLOR_FOCUSED1 = 0xffffba00;
    private static final int LABEL_TEXT_COLOR_CLICKED = 0xff00a8ff;
    private static final int LABEL_TEXT_BACKGROUND_COLOR_FOCUSED = 0xffe5aa50;

    private static final int LABEL_TEXT_BACKGROUND_SELECTED_NOFOCUSED = 0x80e5aa50;

    private static final int LABEL_TEXT_BACKGROUND_NOSELECTED_NOFOCUSED = 0x00000000;

    private int tabWidth;
    private Context mContext;
    private int tabSpace;
    private int mLeftPosition = -1;
    private int mRightPosition = -1;

    public ProgressBar percentageBar;
    public String title;
    public String channel;
    private boolean isPortrait = false;
    float rate;
    private int tabMargin;
    public ImageView arrow_left,shade_arrow_left;
    public ImageView arrow_right,shade_arrow_right;
    Animation scaleSmallAnimation;
    Animation scaleBigAnimation;
    private int initTab=1;

    public void setIsPortrait(boolean isPortrait) {
        this.isPortrait = isPortrait;
        if (isPortrait) {
            tabMargin = getResources().getDimensionPixelSize(R.dimen.list_section_tabMargin);
        } else {
            tabMargin = getResources().getDimensionPixelSize(R.dimen.list_section_tabMargin);
        }
        tabWidth = Util.getDisplayPixelWidth(mContext) - 2 * tabMargin;
    }

    public int getSelectPosition() {
        return mSelectPosition;
    }

    public ScrollableSectionList(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initialize();
    }

    public ScrollableSectionList(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initialize();
    }

    public ScrollableSectionList(Context context) {
        super(context);
        mContext = context;
        initialize();
    }

    private void initialize() {
//        this.setFadingEdgeLength(100);
        this.setHorizontalFadingEdgeEnabled(false);
//        getResources().getDimensionPixelSize(R.dimen.channel_section_tabs_W)
        setOverScrollMode(OVER_SCROLL_NEVER);
        tabSpace = getResources().getDimensionPixelSize(R.dimen.list_section_tabSpace);
    }

    public HGridView mGridView;

    public void init(SectionList sectionLists, int totalWidth, boolean isChangeBarStyle,int initTab) {
     //   rate = DaisyUtils.getVodApplication(getContext()).getRate(getContext());
        mContainer = new LinearLayout(getContext());
        mContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        this.isChangeBarStyle = isChangeBarStyle;
        mContainer.setLayoutParams(new LayoutParams(totalWidth, getResources().
                getDimensionPixelSize(R.dimen.channel_sectiom_tabs_text_H)));
        SectionList sectionList = new SectionList();
        Section filter = new Section();
        filter.count = 0;
        filter.title = "筛选";
        // sectionList.
        for (Section s : sectionLists) {
            if (s.count != 0) {
                sectionList.add(s);
            }
        }
        if (scaleSmallAnimation == null) {
            scaleSmallAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.section_tab_small);
        }
        if (scaleBigAnimation == null) {
            scaleBigAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.section_tab_big);
        }
        FrameLayout sectionFilter = getSectionFilterLabel();
        sectionFilter.setFocusable(true);
        sectionFilter.setOnFocusChangeListener(mOnFocusChangeListener);
        sectionFilter.setOnClickListener(mOnClickListener);
        sectionFilter.setOnHoverListener(mOnTouchListener);
        sectionFilter.setTag(0);
        sectionFilter.setId(R.layout.section_list_item + 1);
        mContainer.addView(sectionFilter, 0);

        for (int i = 0; i < sectionList.size(); i++) {
            FrameLayout sectionHolder = getSectionLabelLayout(sectionList.get(i));
            sectionHolder.setOnFocusChangeListener(mOnFocusChangeListener);
            sectionHolder.setOnClickListener(mOnClickListener);
            sectionHolder.setOnHoverListener(mOnTouchListener);
            sectionHolder.setId(R.layout.section_list_item + 2 + i);
            sectionHolder.setTag(i + 1);
            mContainer.addView(sectionHolder, i + 1);

            if (i == sectionList.size() - 1) {
                sectionHolder.setNextFocusRightId(-1);
            }
        }

        this.addView(mContainer);
        this.initTab = initTab;
        View childView = mContainer.getChildAt(initTab);
        if (childView != null) {
            changeSelection(initTab);
            childView.requestFocus();
        }

        if(!TextUtils.isEmpty(channel) && channel.equals("payment")){
            mContainer.getChildAt(0).setVisibility(View.GONE);
        }

    }

    private FrameLayout getSectionLabelLayout(Section section) {
        FrameLayout sectionHolder = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.section_list_item, null);
        LinearLayout.LayoutParams layoutParams;
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        sectionHolder.setPadding(tabSpace, 0, tabSpace, 0);
        sectionHolder.setLayoutParams(layoutParams);
        sectionHolder.setFocusable(true);
        TextView label = (TextView) sectionHolder.findViewById(R.id.section_label);
        ((LayoutParams) label.getLayoutParams()).setMargins(tabSpace, 0, tabSpace, 0);
        label.setText(section.title);
        return sectionHolder;
    }

    private FrameLayout getSectionFilterLabel() {
        FrameLayout sectionHolder = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.section_list_item, null);
        LinearLayout.LayoutParams layoutParams;
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        sectionHolder.setLayoutParams(layoutParams);
        sectionHolder.setFocusable(true);
        TextView label = (TextView) sectionHolder.findViewById(R.id.section_label);
        ((LayoutParams) label.getLayoutParams()).setMargins(tabSpace, 0, tabSpace, 0);
        label.setText("筛选");
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelOffset(R.dimen.text_size_36sp));
        label.setTag("filter");
        return sectionHolder;
    }

    private int getTextWidth(TextView textView){
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(spec, spec);
        int measuredWidth = textView.getMeasuredWidth();
        return measuredWidth;
    }
    private int getTextHeight(TextView textView){
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(spec, spec);
        int measuredHeight=textView.getMeasuredHeight();
        return measuredHeight;
    }

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (left_shadow != null && (left_shadow.isHovered() || right_shadow.isHovered())) {
                return;
            }
            int index = (Integer) v.getTag();
//            Log.i("LH/","onFocusChange:"+index + " mSelected:"+mSelectPosition + " hovered:"+v.isHovered()+ " hasFocus:"+hasFocus);
            TextView label = (TextView) v.findViewById(R.id.section_label);
            int textWidth = getTextWidth(label);
            int textHeight=getTextHeight(label);
            ImageView section_image = (ImageView) v.findViewById(R.id.section_image);
            if (textWidth > 0) {
                ((LayoutParams) section_image.getLayoutParams()).width = textWidth + tabSpace * 2;
            }
            if(textHeight>0){
           //   ((LayoutParams) section_image.getLayoutParams()).height = textHeight*2;
            }
            if (hasFocus) {
                if (isFromArrow) {
                    isFromArrow = false;
                } else {
                    if (v.isHovered()) {
                        Log.i("Scrollsection","isHover:"+v.isHovered());
                        return;
                    }
                }
//				if(sectionhovered != null){
//					((RelativeLayout) sectionhovered.getParent()).setHovered(false);
//				}
//				if(sectionhovered != null){
//					sectionhovered.setBackgroundResource(android.R.color.transparent);
//				}
                if (index == mSelectPosition) {
                    label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                    section_image.setImageResource(R.drawable.sectionfocus);
                     v.startAnimation(scaleBigAnimation);
                    Log.i("Scrollsection","focus scaleBig");
                    return;
                } else {
                    if (currentState == STATE_LEAVE_GRIDVIEW) {

                        currentState = STATE_SECTION;
                        mContainer.getChildAt(mSelectPosition).requestFocus();
                    } else if (currentState == STATE_SECTION) {
//                        if(mHandler!=null){
//                            if(mHandler.hasMessages(START_CLICK)){
//                                mHandler.removeMessages(START_CLICK);
//                            }
//                        }
                        label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
//                        label.setTextSize(textsize);
                        v.performClick();
                    }
                }

            } else {
                if(v.isHovered()){
                    v.setHovered(false);
                }
                Log.i("Scrollsection","nofocus scaleSmall");
                v.startAnimation(scaleSmallAnimation);
               v.clearAnimation();
//                Log.i("LH/", "sectionfocus:"+index+" "+mSelectPosition);
                if (index == mSelectPosition) {
                    sectionWhenGoto = label;
                  section_image.setImageResource(R.drawable.gotogridview);
                 //   v.startAnimation(scaleBigAnimation);
                    return;
                }
                label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                section_image.setImageResource(android.R.color.transparent);
            }
        }
    };

    // Activity onResume方法执行时调用
    public void setFilterBack(View v) {
        currentState = STATE_LEAVE_GRIDVIEW;
        mSelectPosition = 1;
        TextView label = (TextView) v.findViewById(R.id.section_label);
        label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
        ImageView section_image = (ImageView) v.findViewById(R.id.section_image);
        ((LayoutParams) section_image.getLayoutParams()).width = v.getWidth();
        section_image.setImageResource(R.drawable.gotogridview);

        if (mContainer.getChildCount() > 0) {
            View filterView = mContainer.getChildAt(0);
            TextView firstLabel = (TextView) filterView.findViewById(R.id.section_label);
            ImageView first_image = (ImageView) filterView.findViewById(R.id.section_image);
            firstLabel.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
            first_image.setImageResource(android.R.color.transparent);
        }
        v.performClick();
    }

    public boolean isRelated = false;
    public TextView sectionWhenGoto;
    private TextView sectionhovered;
    private boolean isOnHovered;
    public static int STATE_GOTO_GRIDVIEW = 2;
    public static int STATE_SECTION = 3;
    public static int STATE_LEAVE_GRIDVIEW = 4;
    public int currentState = STATE_SECTION;
    public static int START_CLICK = 1;
//    public Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//                   View v = (View) msg.obj;
//                   v.performClick();
//            }
//    };

    int temp = 0;
    // 切换tab时调用
    private void setSectionTabProperty(View currentView, View lastSelectedView) {
        TextView lastLabel = (TextView) lastSelectedView.findViewById(R.id.section_label);
        ImageView last_section_image = (ImageView) lastSelectedView.findViewById(R.id.section_image);
        lastLabel.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
      //  lastLabel.setTextSize(getResources().getDimensionPixelSize(R.dimen.list_section_tabSize));
        last_section_image.setImageResource(android.R.color.transparent);
//        if(initTab > 1 && temp++ ==0) {
//            mContainer.requestFocus();
//            mContainer.invalidate();
//            currentView = mContainer.getChildAt(0);
//        }
        TextView label = (TextView) currentView.findViewById(R.id.section_label);
        label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
       // label.setTextSize(getResources().getDimensionPixelSize(R.dimen.list_section_tabSize));
        ImageView section_image = (ImageView) currentView.findViewById(R.id.section_image);
        ((LayoutParams) section_image.getLayoutParams()).width = currentView.getWidth();
        if (currentState == STATE_SECTION||currentState==STATE_LEAVE_GRIDVIEW) {
            Log.i("Scrollsection","scaleBig : "+currentState);
            section_image.setImageResource(R.drawable.sectionfocus);
           currentView.startAnimation(scaleBigAnimation);
        } else {
            Log.i("Scrollsection","scaleSmall : "+currentState);
            section_image.setImageResource(R.drawable.gotogridview);
            currentView.startAnimation(scaleSmallAnimation);
             lastSelectedView.clearAnimation();
        }
        lastSelectedView.startAnimation(scaleSmallAnimation);
        lastSelectedView.clearAnimation();
        autoScroll((Integer) currentView.getTag());
    }

    // 定时器
    private Timer sensorTimer;
    private MyTimerTask myTimerTask;

    class MyTimerTask extends TimerTask {

        private int hoverOnArrow; // 0表示左侧箭头，1表示右侧箭头

        MyTimerTask(int arrow) {
            this.hoverOnArrow = arrow;
        }

        @Override
        public void run() {
//            if (hoverOnArrow == 2) {
//                cancelTimer();
//            }

            findLeftRightChildPosition();
            updateHoverHandler.sendEmptyMessage(hoverOnArrow);

//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    switch (hoverOnArrow) {
//
//                    }
//
//                }
//            });

        }

    }

    private Handler updateHoverHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
//                case 2:
//                    if (view != null) {
//                        autoScroll((Integer) view.getTag());
//                    }
//                    break;
                case 0:
                    if (mLeftPosition >= 0) {
                        autoScroll(mLeftPosition);
                    }
                    break;
                case 1:
                    if (mRightPosition >= 0) {
                        autoScroll(mRightPosition);
                    }
                    break;
            }
        }
    };

    private OnHoverListener mOnTouchListener = new OnHoverListener() {

        @Override
        public boolean onHover(final View v, MotionEvent keycode) {
            // TODO Autogenerated method stub
            final int index = (Integer) v.getTag();
            final TextView label = (TextView) v.findViewById(R.id.section_label);
            final ImageView section_image = (ImageView) v.findViewById(R.id.section_image);
            ((LayoutParams) section_image.getLayoutParams()).width = v.getWidth();
            switch (keycode.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    int tabRightX = tabMargin + tabWidth;
                    int[] currentPos = new int[2];
                    v.getLocationOnScreen(currentPos);
                    int currentWidth = v.getWidth();
                    if (currentPos[0] + currentWidth > tabRightX || currentPos[0] + 1 < tabMargin) {
                        if (index == 0 || index == mContainer.getChildCount() - 1) {
                           Log.i("LH/","currentPos:"+currentPos[0]+" tabMargin:"+tabMargin);
                            // TODO
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    autoScroll(index);
                                    onHoverSet(v, label, section_image, index);
                                }
                            }, 500);
                        }
                        return true;
                    }
                    onHoverSet(v, label, section_image, index);

                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (index == mSelectPosition) {
                        section_image.setImageResource(R.drawable.gotogridview);
                        return false;
                    }
                    label.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                    section_image.setImageResource(android.R.color.transparent);
                    v.setHovered(false);
                default:
                    break;
            }
            return false;
        }
    };

    private void onHoverSet(View v, TextView label, ImageView section_image, int index) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.setHovered(true);
        v.requestFocus();
        isOnHovered = true;
        sectionhovered = label;
        sectionhovered.setTag(index);
        if (index < mContainer.getChildCount() - 1) {
            v.setNextFocusRightId(v.getId() + 1);
        }
        if (index > 0) {
            v.setNextFocusLeftId(v.getId() - 1);
        }

        label.setTextColor(getResources().getColor(R.color._ff9c3c));
        if (index != mSelectPosition) {
            section_image.setImageResource(R.drawable.section_indicator_selected);

            View lastSelectedView = mContainer.getChildAt(mSelectPosition);
            ImageView last_section_image = (ImageView) lastSelectedView.findViewById(R.id.section_image);
            ((LayoutParams) last_section_image.getLayoutParams()).width = lastSelectedView.getWidth();
            last_section_image.setImageResource(R.drawable.gotogridview);
        } else {
            section_image.setImageResource(R.drawable.sectionfocus);
            label.setTextColor(getResources().getColor(R.color._ffffff));
        }

    }

    private void cancelTimer() {
        if (myTimerTask != null) {
            myTimerTask.cancel();
            myTimerTask = null;
        }
        if (sensorTimer != null) {
            sensorTimer.cancel();
            sensorTimer = null;
            System.gc();
        }
        if(updateHoverHandler != null){
            updateHoverHandler.removeCallbacksAndMessages(null);
        }
    }

    private void setsectionview(View v) {
        int index = (Integer) v.getTag();
        if (index == 0) {
            percentageBar.setProgress(0);
            View lastSelectedView = mContainer.getChildAt(mSelectPosition);
            setSectionTabProperty(v, lastSelectedView);
            changeSelection(index);
        } else {
            if (index != mSelectPosition) {
                percentageBar.setProgress(0);
                View lastSelectedView = mContainer.getChildAt(mSelectPosition);
                setSectionTabProperty(v, lastSelectedView);
                changeSelection(index);
            }
        }
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            int index = (Integer) v.getTag();
//			Log.i("LH/ScroSectionList","index:"+index);
            Log.i("Scrollsection","index: "+index+"  mSelection: "+mSelectPosition);
            if (index == 0) {

                setsectionview(v);
                Intent intent = new Intent();
                intent.putExtra("title", title);
                intent.putExtra("channel", channel);
                if (isPortrait)
                    intent.putExtra("isPortrait", true);
                else
                    intent.putExtra("isPortrait", false);
                intent.setAction("tv.ismar.daisy.Filter");
                sectionWhenGoto = (TextView) v.findViewById(R.id.section_label);
                getContext().startActivity(intent);
            } else {
                if (index != mSelectPosition) {
                    setsectionview(v);
                    if (mSectionSelectChangedListener != null) {
                        mSectionSelectChangedListener.onSectionSelectChanged(index - 1);
                    }
                }
            }

        }
    };

    /**
     * use to modify the special position's section percentage progress bar.
     *
     * @param position   the section index which you want to modify
     * @param percentage the percentage,should be a 100 based integer.
     */
    public void setPercentage(int position, int percentage) {
        View sectionHolder = mContainer.getChildAt(position);
        //ProgressBar percentageBar = (ProgressBar) sectionHolder.findViewById(R.id.section_percentage);
        if (position == 0) {
            return;
        }
        if (position != mSelectPosition) {
            View lastSectionHolder = mContainer.getChildAt(mSelectPosition);

            changeSelection(position);

            //	percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbg));
            // percentageBar.setBackgroundColor(test123);
            setSectionTabProperty(sectionHolder, lastSectionHolder);
            //  View vs = mContainer.getChildAt(mSelectPosition);
            // setSectionTabProperty(sectionHolder, vs);

//            if(lastSelectPosition!=mSelectPosition){
//                View lastSelectedView1 = mContainer.getChildAt(lastSelectPosition);
//                setSectionTabProperty(sectionHolder,lastSelectedView1);
//            }
//            lastSelectPosition = position;
            percentageBar.setProgress(0);
        }

        percentageBar.setProgress(percentage);

    }

    public void setOnSectionSelectChangeListener(OnSectionSelectChangedListener listener) {
        mSectionSelectChangedListener = listener;
    }

    /**
     * indicate that section is changed by user click. usually use to update itemList of the section.
     *
     * @author bob
     */
    public interface OnSectionSelectChangedListener {
        public void onSectionSelectChanged(int index);
    }

    //	@Override
//	public boolean arrowScroll(int direction) {
//		if(direction==View.FOCUS_RIGHT){
//			View currentFocused = findFocus();
//			if(currentFocused==null || currentFocused.getTag()==null) {
//				return super.arrowScroll(direction);
//			}
//			int index = (Integer) currentFocused.getTag();
//			if(index < mContainer.getChildCount()-1){
//				return super.arrowScroll(direction);
//			} else {
//				//if currentFocused is the last element of the list. just do nothing.
//				return true;
//			}
//		} else if(direction==View.FOCUS_LEFT){
//			View currentFocused = findFocus();
//			if(currentFocused==null || currentFocused.getTag()==null) {
//				return super.arrowScroll(direction);
//			}
//			int index = (Integer) currentFocused.getTag();
//			if(index > 0 ){
//				return super.arrowScroll(direction);
//			} else {
//				//if currentFocused is the last element of the list. just do nothing.
//				return true;
//			}
//		} else {
//			return super.arrowScroll(direction);
//		}
//	}
//	public View left;
//	public View right;
//	public View parent;
    private boolean isFromArrow;

    public boolean arrowScroll(int direction) {
        int position = mSelectPosition;
        if (isOnHovered) {
            position = (int) sectionhovered.getTag();
//			Log.i("LH/","Hoveredposition:"+position);
            sectionhovered.setHovered(false);
            ((FrameLayout) sectionhovered.getParent()).setHovered(false);
            isOnHovered = false;
        }
//		Log.i("LH/","scrollArrow:"+position);

        currentState = STATE_SECTION;
        switch (direction) {
            case View.FOCUS_LEFT:
                isFromArrow = true;
                if (position > 0) {
                    mContainer.getChildAt(position - 1).requestFocus();
                } else if (position == 0) {
                    isFromArrow = false;
                    mContainer.getChildAt(0).performClick();
                }
                break;
            case View.FOCUS_RIGHT:
                isFromArrow = true;
                if (position < mContainer.getChildCount() - 1) {
                    mContainer.getChildAt(position + 1).requestFocus();
                } else if (position == mContainer.getChildCount() - 1) {
                    isFromArrow = false;
                    mContainer.getChildAt(position).performClick();
                }
                break;
        }
        return true;
    }

    /**
     * @return whether the descendant of this scroll view is scrolled off
     * screen.
     */
    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0);
    }

    /**
     * @return whether the descendant of this scroll view is within delta
     * pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreen(View descendant, int delta) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.right + delta) >= getScrollX()
                && (mTempRect.left - delta) <= (getScrollX() + getWidth());
    }

    /**
     * Smooth scroll by a X delta
     *
     * @param delta the number of pixels to scroll by on the X axis
     */
    private void doScrollX(int delta) {
        if (delta != 0) {
            if (mSmoothScrollingEnabled) {
                smoothScrollBy(delta, 0);
            } else {
                scrollBy(delta, 0);
            }
        }

    }

    private void autoScroll(int currentPosition) {
        if (mContainer.getChildCount() <= 2 || currentPosition < 0) {
            return;
        }
//        Log.i("LH/", "autoScroll:" + currentPosition);
        int tabRightX = tabMargin + tabWidth;

        if (currentPosition == 0) {
            if (arrow_left != null) {
                arrow_left.setVisibility(View.INVISIBLE);
                shade_arrow_left.setVisibility(View.INVISIBLE);
                Log.i("tabarrow","invisible");
            }
            View currentView = mContainer.getChildAt(currentPosition);
            int[] currentPos = new int[2];
            currentView.getLocationOnScreen(currentPos);
            if (currentPos[0] < tabMargin) {
                smoothScrollBy(currentPos[0] - tabMargin, 0);
            }
            return;
        }
        if (currentPosition == mContainer.getChildCount() - 1) {
            if (arrow_right != null) {
                arrow_right.setVisibility(View.INVISIBLE);
                shade_arrow_right.setVisibility(View.INVISIBLE);
            }
            View currentView = mContainer.getChildAt(currentPosition);
            int[] currentPos = new int[2];
            currentView.getLocationOnScreen(currentPos);
            if (currentPos[0] + currentView.getWidth() > tabRightX) {
                smoothScrollBy(currentPos[0] + currentView.getWidth() - tabRightX, 0);
            }
            return;
        }

        View currentView = mContainer.getChildAt(currentPosition);
        int[] currentPos = new int[2];
        currentView.getLocationOnScreen(currentPos);
        int currentWidth = currentView.getWidth();
//        Log.i("LH/", "currentPos:" + currentPos[0] + "-" + currentWidth);

        if (currentPos[0] + currentWidth >= tabRightX-tabSpace) {
            View nextView = mContainer.getChildAt(currentPosition + 1);
            int[] nextPos = new int[2];
            nextView.getLocationOnScreen(nextPos);
            int nextWidth = nextView.getWidth();
//            Log.i("LH/", "nextRect:" + nextPos[0] + "-" + nextWidth);
            int nextViewCenterX = nextPos[0] + nextWidth / 2;
//            Log.i("LH/", "nextViewCenterX:" + nextViewCenterX + "\ntabRightX:" + tabRightX);
            if (currentPosition == mContainer.getChildCount() - 2) {
                smoothScrollBy((nextPos[0] + nextWidth) - tabRightX, 0);
                if (arrow_right != null) {
                    arrow_right.setVisibility(View.INVISIBLE);
                    shade_arrow_right.setVisibility(View.INVISIBLE);
                    cancelTimer();
                }
            } else {
                smoothScrollBy(nextViewCenterX - tabRightX, 0);
                if (arrow_right != null && arrow_right.getVisibility() != View.VISIBLE) {
                    arrow_right.setVisibility(View.VISIBLE);
                    shade_arrow_right.setVisibility(View.VISIBLE);
                }
            }
            if (arrow_left != null && arrow_left.getVisibility() != View.VISIBLE) {
                arrow_left.setVisibility(View.VISIBLE);
                shade_arrow_left.setVisibility(View.VISIBLE);
                Log.i("tabarrow","visisble");
            }
        } else if (currentPos[0] <= tabMargin) {
            View frontView = mContainer.getChildAt(currentPosition - 1);
            int[] frontPos = new int[2];
            frontView.getLocationOnScreen(frontPos);
//            Rect frontRect = new Rect();
//            frontView.getGlobalVisibleRect(frontRect);
            int frontWidth = frontView.getWidth();
//            Log.i("LH/", "frontRect:" + frontPos[0] + "-" + frontWidth);
            int frontViewCenterX = frontPos[0] + frontWidth / 2;
//            Log.i("LH/", "frontViewCenterX:" + frontViewCenterX + "\ntabRightX:" + tabMargin);
            if (currentPosition == 1) {
                smoothScrollBy(frontPos[0] - tabMargin, 0);
                if (arrow_left != null) {
                    arrow_left.setVisibility(View.INVISIBLE);
                    shade_arrow_left.setVisibility(View.INVISIBLE);
                    cancelTimer();
                }
            } else {
                smoothScrollBy(frontViewCenterX - tabMargin, 0);
                if (arrow_left != null && arrow_left.getVisibility() != View.VISIBLE) {
                    arrow_left.setVisibility(View.VISIBLE);
                    shade_arrow_left.setVisibility(View.VISIBLE);
                }
            }
            if (arrow_right != null && arrow_right.getVisibility() != View.VISIBLE) {
                arrow_right.setVisibility(View.VISIBLE);
                shade_arrow_right.setVisibility(View.VISIBLE);
            }
        }

    }

    private void findLeftRightChildPosition() {
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            View view = mContainer.getChildAt(i);
            int[] currentPos = new int[2];
            view.getLocationOnScreen(currentPos);
            int viewLeftX = currentPos[0];
            int viewRightX = viewLeftX + view.getWidth();
            if (viewLeftX == tabMargin) {
                mLeftPosition = i;
            } else if (viewLeftX < tabMargin && viewRightX >= tabMargin) {
                mLeftPosition = i;
            } else if (viewRightX == tabMargin + tabWidth) {
                mRightPosition = i;
            } else if (viewRightX > tabMargin + tabWidth && viewLeftX <= tabMargin + tabWidth) {
                mRightPosition = i;
            }
        }

    }

    public void setArrowDirection(int arrowHoverPosition) {
        // 屏蔽鼠标在半显示文字上的移动
        if (arrowHoverPosition < 0) {
            cancelTimer();
            return;
        }
        if (sensorTimer == null) {
            sensorTimer = new Timer();
            myTimerTask = new MyTimerTask(arrowHoverPosition);
            sensorTimer.schedule(myTimerTask, 500, 500);
        }

    }

//    /**
//     * @return whether the descendant of this scroll view is scrolled off
//     *  screen.
//     */
//    private boolean isOffScreen(View descendant) {
//        return !isWithinDeltaOfScreen(descendant, 0);
//    }
//    
//    

    /*
     * use to change the mSelectPosition.
     */
    private void changeSelection(int position) {
        if (position < 0 || position >= mContainer.getChildCount()) {
            return;
        }
        mSelectPosition = position;
//		View section = mContainer.getChildAt(position);
//		final int maxJump = getMaxScrollAmount();
//
//		if(isWithinDeltaOfScreen(section, maxJump)) {
//			section.getDrawingRect(mTempRect);
//			offsetDescendantRectToMyCoords(section, mTempRect);
//			int delta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
//			doScrollX(delta);
//		}
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }
//
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
//
//    }

    public Button left_shadow;
    public Button right_shadow;

    public void reset() {
        removeAllViews();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
