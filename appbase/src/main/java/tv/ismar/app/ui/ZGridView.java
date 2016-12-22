package tv.ismar.app.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StateSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.EdgeEffect;
import android.widget.ListAdapter;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.R;

public class ZGridView extends AdapterView<ListAdapter> {

	public static final int AUTO_FIT = -1;
	private static final int TOUCH_MODE_ON = 0;
	private static final int TOUCH_MODE_OFF = 1;
	private int mLastTouchMode = TOUCH_MODE_UNKNOWN;
	private static final int TOUCH_MODE_UNKNOWN = -1;
	int mOverscrollDistance;
	private int mNumColumns = AUTO_FIT;
	private int mStretchMode = STRETCH_COLUMN_WIDTH;
	/**
	 * The position to resurrect the selected position to.
	 */
	int mResurrectToPosition = INVALID_POSITION;
	private SelectionNotifier mSelectionNotifier;
	/**
	 * The data set used to store unused views that should be reused during the
	 * next layout to avoid creating new ones
	 */
	final RecycleBin mRecycler = new RecycleBin();
	private ListAdapter mAdapter;
	/**
	 * Should be used by subclasses to listen to changes in the dataset
	 */
	AdapterDataSetObserver mDataSetObserver;
	boolean mDataChanged;
	int mOldItemCount;
	int mItemCount;
	/**
	 * The position within the adapter's data set of the currently selected
	 * item.
	 */
	@ViewDebug.ExportedProperty(category = "list")
	int mSelectedPosition = INVALID_POSITION;
	/**
	 * The position of the first child displayed
	 */
	@ViewDebug.ExportedProperty(category = "scrolling")
	int mFirstPosition = 0;
	long mSyncHeight;
	/**
	 * Sync based on the first child displayed
	 */
	static final int SYNC_FIRST_POSITION = 1;
	/**
	 * True if we need to sync to mSyncRowId
	 */
	boolean mNeedSync = false;

	/**
	 * Our height after the last layout
	 */
	private int mLayoutHeight;
	/**
	 * The item id of the item to select during the next layout.
	 */
	long mNextSelectedRowId = INVALID_ROW_ID;
	/**
	 * Row id to look for when data has changed
	 */
	long mSyncRowId = INVALID_ROW_ID;
	/**
	 * Position from which to start looking for mSyncRowId
	 */
	int mSyncPosition;
	/**
	 * The offset in pixels from the top of the AdapterView to the top of the
	 * view to select during the next layout.
	 */
	int mSpecificTop;
	@ViewDebug.ExportedProperty(category = "list")
	int mNextSelectedPosition = INVALID_POSITION;

	/**
	 * Sync based on the selected child
	 */
	static final int SYNC_SELECTED_POSITION = 0;

	/**
	 * Maximum amount of time to spend in {@link #findSyncPosition()}
	 */
	static final int SYNC_MAX_DURATION_MILLIS = 100;

	/**
	 * Indicates whether to sync based on the selection or position. Possible
	 * values are {@link #SYNC_SELECTED_POSITION} or
	 * {@link #SYNC_FIRST_POSITION}.
	 */
	int mSyncMode;
	/**
	 * Defines the selector's location and dimension at drawing time
	 */
	Rect mSelectorRect = new Rect();

	/**
	 * The data set used to store unused views that should be reused during the
	 * next layout to avoid creating new ones
	 */
	SparseBooleanArray mCheckStates;
	private Runnable mClearScrollingCache;
	Runnable mPositionScrollAfterLayout;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private float mVelocityScale = 1.0f;
	final boolean[] mIsScrap = new boolean[1];
	/**
	 * The last selected position we used when notifying
	 */
	int mOldSelectedPosition = INVALID_POSITION;
	/**
	 * The id of the last selected position we used when notifying
	 */
	long mOldSelectedRowId = INVALID_ROW_ID;
	/**
	 * The offset in pixels form the top of the AdapterView to the top of the
	 * currently selected view. Used to save and restore state.
	 */
	int mSelectedTop = 0;

	/**
	 * Indicates whether the list is stacked from the bottom edge or the top
	 * edge.
	 */
	boolean mStackFromBottom;
	/**
	 * The drawable used to draw the selector
	 */
	Drawable mSelector;

	/**
	 * The current position of the selector in the list.
	 */
	int mSelectorPosition = INVALID_POSITION;
	/**
	 * If mAdapter != null, whenever this is true the adapter has stable IDs.
	 */
	boolean mAdapterHasStableIds;
	/**
	 * Layout as a result of using the navigation keys
	 */
	static final int LAYOUT_MOVE_SELECTION = 6;

	/**
	 * Normal list that does not indicate choices
	 */
	public static final int CHOICE_MODE_NONE = 0;

	/**
	 * The list allows up to one choice
	 */
	public static final int CHOICE_MODE_SINGLE = 1;

	/**
	 * The list allows multiple choices
	 */
	public static final int CHOICE_MODE_MULTIPLE = 2;

	/**
	 * The list allows multiple choices in a modal selection mode
	 */
	public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
	/**
	 * The item id of the currently selected item.
	 */
	long mSelectedRowId = INVALID_ROW_ID;
	/**
	 * The listener that receives notifications when an item is selected.
	 */
	OnItemSelectedListener mOnItemSelectedListener;
	/**
	 * Indicates that this view is currently being laid out.
	 */
	boolean mInLayout = false;
	/**
	 * When set to true, calls to requestLayout() will not propagate up the
	 * parent hierarchy. This is used to layout the children during a layout
	 * pass.
	 */
	boolean mBlockLayoutRequests = false;

	int mLayoutMode = LAYOUT_NORMAL;

	/**
	 * Regular layout - usually an unsolicited layout from the view system
	 */
	static final int LAYOUT_NORMAL = 2;
	/**
	 * Handles scrolling between positions within the list.
	 */
	PositionScroller mPositionScroller;
	/**
	 * Stretches columns.
	 * 
	 * @see #setStretchMode(int)
	 */
	public static final int STRETCH_COLUMN_WIDTH = 2;
	boolean mCachingStarted;
	boolean mCachingActive;
	boolean mScrollingCacheEnabled;
	protected int mPersistentDrawingCache;
	/**
	 * Disables the transcript mode.
	 * 
	 * @see #setTranscriptMode(int)
	 */
	public static final int TRANSCRIPT_MODE_DISABLED = 0;
	/**
	 * Indicates that this list is always drawn on top of a solid, single-color,
	 * opaque background
	 */
	private int mCacheColorHint;
	private int mSelectionLeftPadding;
	private int mSelectionTopPadding;
	private int mSelectionRightPadding;
	private int mSelectionBottomPadding;

	private int mSelectionLeftPadding1;
	private int mSelectionTopPadding1;
	private int mSelectionRightPadding1;
	private int mSelectionBottomPadding1;
	/**
	 * Indicates whether the list selector should be drawn on top of the
	 * children or behind
	 */
	boolean mDrawSelectorOnTop = false;
	/**
	 * Indicates that this view supports filtering
	 */
	private boolean mTextFilterEnabled;
	/**
	 * This view is in transcript mode -- it shows the bottom of the list when
	 * the data changes
	 */
	private int mTranscriptMode;
	private int mRequestedHorizontalSpacing;
	private int mVerticalSpacing = 0;
	private int mRequestedColumnWidth;
	private int mRequestedNumColumns;
	private int mGravity = Gravity.START;
	int mTouchMode = TOUCH_MODE_REST;
	/**
	 * Indicates that we are not in the middle of a touch gesture
	 */
	static final int TOUCH_MODE_REST = -1;
	static final int TOUCH_MODE_TAP = 1;
	static final int TOUCH_MODE_DONE_WAITING = 2;

	private int mColumnWidth;
	private View mReferenceView = null;
	private View mReferenceViewInSelectedRow = null;
	Rect mListPadding = new Rect();
	int mWidthMeasureSpec = 0;
	private int mHorizontalSpacing = 0;
	public static final int NO_STRETCH = 0;
	public static final int STRETCH_SPACING = 1;
	public static final int STRETCH_SPACING_UNIFORM = 3;
	/**
	 * Maximum distance to record overscroll
	 */
	int mOverscrollMax;

	/**
	 * Content height divided by this is the overscroll limit.
	 */
	static final int OVERSCROLL_LIMIT_DIVISOR = 3;
	/**
	 * Show the first item
	 */
	static final int LAYOUT_FORCE_TOP = 1;
	private int mLastScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	/**
	 * Force the selected item to be on somewhere on the screen
	 */
	static final int LAYOUT_SET_SELECTION = 2;

	/**
	 * Show the last item
	 */
	static final int LAYOUT_FORCE_BOTTOM = 3;

	/**
	 * Make a mSelectedItem appear in a specific location and build the rest of
	 * the views from there. The top is specified by mSpecificTop.
	 */
	static final int LAYOUT_SPECIFIC = 4;

	/**
	 * Layout to sync as a result of a data change. Restore mSyncPosition to
	 * have its top at mSpecificTop
	 */
	static final int LAYOUT_SYNC = 5;
	/**
	 * Track the item count from the last time we handled a data change.
	 */
	private int mLastHandledItemCount;
	/**
	 * Controls if/how the user may choose/check items in the list
	 */
	int mChoiceMode = CHOICE_MODE_NONE;
	public static final int TRANSCRIPT_MODE_ALWAYS_SCROLL = 2;
	private boolean mForceTranscriptScroll;
	public static final int TRANSCRIPT_MODE_NORMAL = 1;
	static final int TOUCH_MODE_DOWN = 0;
	static final int TOUCH_MODE_SCROLL = 3;
	int mMotionPosition;
	View mScrollUp;
	View mScrollDown;
	boolean mIsAttached;
	private OnScrollListener mOnScrollListener;
	private Rect mTempRect = new Rect();
	protected Context mContext;
	/**
	 * Determines speed during touch scrolling
	 */
	private VelocityTracker mVelocityTracker;
	/**
	 * Indicates the view is being flung outside of normal content bounds and
	 * will spring back.
	 */
	static final int TOUCH_MODE_OVERFLING = 6;
	/**
	 * Indicates the touch gesture is an overscroll - a scroll beyond the
	 * beginning or end.
	 */
	static final int TOUCH_MODE_OVERSCROLL = 5;
	/**
	 * Indicates the view is in the process of being flung
	 */
	static final int TOUCH_MODE_FLING = 4;

	/**
	 * The X value associated with the the down motion event
	 */
	int mMotionX;

	/**
	 * The Y value associated with the the down motion event
	 */
	int mMotionY;
	/**
	 * Y value from on the previous motion event (if any)
	 */
	int mLastY;
	/**
	 * How far the finger moved before we started scrolling
	 */
	int mMotionCorrection;
	/**
	 * ID of the active pointer. This is used to retain consistency during
	 * drags/flings if multiple pointers are used.
	 */
	private int mActivePointerId = INVALID_POINTER;
	private static final int INVALID_POINTER = -1;
	/**
	 * Used for determining when to cancel out of overscroll.
	 */
	private int mDirection = 0;
	/**
	 * The last CheckForTap runnable we posted, if any
	 */
	private Runnable mPendingCheckForTap;
	/**
	 * The offset to the top of the mMotionPosition view when the down motion
	 * event was received
	 */
	int mMotionViewOriginalTop;
	/**
	 * Acts upon click
	 */
	private ZGridView.PerformClick mPerformClick;

	/**
	 * Tracks the state of the top edge glow.
	 */
	private EdgeEffect mEdgeGlowTop;
	/**
	 * Tracks the state of the bottom edge glow.
	 */
	private EdgeEffect mEdgeGlowBottom;
	private static final boolean PROFILE_SCROLLING = false;
	private boolean mScrollProfilingStarted = false;

	private static final boolean PROFILE_FLINGING = false;
	private boolean mFlingProfilingStarted = false;
	/**
	 * Delayed action for touch mode.
	 */
	private Runnable mTouchModeReset;
	/**
	 * Maximum distance to overfling during edge effects
	 */
	/**
	 * Used for smooth scrolling at a consistent rate
	 */
	static final Interpolator sLinearInterpolator = new LinearInterpolator();
	int mOverflingDistance;
	int mTouchSlop;
	/**
	 * Handles one frame of a fling
	 */
	private FlingRunnable mFlingRunnable;

	/**
	 * Responsible for fling behavior. Use {@link #start(int)} to initiate a
	 * fling. Each frame of the fling is handled in {@link #run()}. A
	 * FlingRunnable will keep re-posting itself until the fling is done.
	 *
	 */
	private class FlingRunnable implements Runnable {
		/**
		 * Tracks the decay of a fling scroll
		 */
		private final OverScroller mScroller;

		/**
		 * Y value reported by mScroller on the previous fling
		 */
		private int mLastFlingY;

		private final Runnable mCheckFlywheel = new Runnable() {
			public void run() {
				final int activeId = mActivePointerId;
				final VelocityTracker vt = mVelocityTracker;
				final OverScroller scroller = mScroller;
				if (vt == null || activeId == INVALID_POINTER) {
					return;
				}

				vt.computeCurrentVelocity(1000, mMaximumVelocity);
				final float yvel = -vt.getYVelocity(activeId);
				if (Math.abs(yvel) >= mMinimumVelocity) {
					// Keep the fling alive a little longer
					postDelayed(this, FLYWHEEL_TIMEOUT);
				} else {
					endFling();
					mTouchMode = TOUCH_MODE_SCROLL;
					reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
				}
			}
		};

		private static final int FLYWHEEL_TIMEOUT = 40; // milliseconds

		FlingRunnable() {
			mScroller = new OverScroller(getContext());
		}

		void start(int initialVelocity) {
			int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingY = initialY;
			// mScroller.setInterpolator(null);
			mScroller.fling(0, initialY, 0, initialVelocity, 0,
					Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			mTouchMode = TOUCH_MODE_FLING;
			postOnAnimation(this);

			if (PROFILE_FLINGING) {
				if (!mFlingProfilingStarted) {
					Debug.startMethodTracing("AbsListViewFling");
					mFlingProfilingStarted = true;
				}
			}
			//
			// if (mFlingStrictSpan == null) {
			// mFlingStrictSpan =
			// StrictMode.enterCriticalSpan("AbsListView-fling");
			// }
		}

		/**
		 * @return true if all list content currently fits within the view
		 *         boundaries
		 */
		private boolean contentFits() {
			final int childCount = getChildCount();
			if (childCount == 0)
				return true;
			if (childCount != mItemCount)
				return false;

			return getChildAt(0).getTop() >= mListPadding.top
					&& getChildAt(childCount - 1).getBottom() <= getHeight()
							- mListPadding.bottom;
		}

		void startSpringback() {
			if (mScroller.springBack(0, getScrollY(), 0, 0, 0, 0)) {
				mTouchMode = TOUCH_MODE_OVERFLING;
				invalidate();
				postOnAnimation(this);
			} else {
				mTouchMode = TOUCH_MODE_REST;
				reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
			}
		}

		void startOverfling(int initialVelocity) {
			// mScroller.setInterpolator(null);
			mScroller.fling(0, getScrollY(), 0, initialVelocity, 0, 0,
					Integer.MIN_VALUE, Integer.MAX_VALUE, 0, getHeight());
			mTouchMode = TOUCH_MODE_OVERFLING;
			invalidate();
			postOnAnimation(this);
		}

		void edgeReached(int delta) {
			mScroller.notifyVerticalEdgeReached(getScrollY(), 0,
					mOverflingDistance);
			final int overscrollMode = getOverScrollMode();
			if (overscrollMode == OVER_SCROLL_ALWAYS
					|| (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits())) {
				mTouchMode = TOUCH_MODE_OVERFLING;
				final int vel = (int) mScroller.getCurrVelocity();
				if (delta > 0) {
					mEdgeGlowTop.onAbsorb(vel);
				} else {
					mEdgeGlowBottom.onAbsorb(vel);
				}
			} else {
				mTouchMode = TOUCH_MODE_REST;
				if (mPositionScroller != null) {
					mPositionScroller.stop();
				}
			}
			invalidate();
			postOnAnimation(this);
		}

		void startScroll(int distance, int duration, boolean linear) {
			int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingY = initialY;
			// mScroller.setInterpolator(linear ? sLinearInterpolator : null);
			mScroller.startScroll(0, initialY, 0, distance, duration);
			mTouchMode = TOUCH_MODE_FLING;
			postOnAnimation(this);
		}

		void endFling() {
			mTouchMode = TOUCH_MODE_REST;

			removeCallbacks(this);
			removeCallbacks(mCheckFlywheel);

			reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
			clearScrollingCache();
			mScroller.abortAnimation();

			// if (mFlingStrictSpan != null) {
			// mFlingStrictSpan.finish();
			// mFlingStrictSpan = null;
			// }
		}

		void flywheelTouch() {
			postDelayed(mCheckFlywheel, FLYWHEEL_TIMEOUT);
		}

		public void run() {
			switch (mTouchMode) {
			default:
				endFling();
				return;

			case TOUCH_MODE_SCROLL:
				if (mScroller.isFinished()) {
					return;
				}
				// Fall through
			case TOUCH_MODE_FLING: {
				if (mDataChanged) {
					layoutChildren();
				}

				if (mItemCount == 0 || getChildCount() == 0) {
					endFling();
					return;
				}

				final OverScroller scroller = mScroller;
				boolean more = scroller.computeScrollOffset();
				final int y = scroller.getCurrY();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the
				// top)
				int delta = mLastFlingY - y;

				// Pretend that each frame of a fling scroll is a touch scroll
				if (delta > 0) {
					// List is moving towards the top. Use first view as
					// mMotionPosition
					mMotionPosition = mFirstPosition;
					final View firstView = getChildAt(0);
					mMotionViewOriginalTop = firstView.getTop();

					// Don't fling more than 1 screen
					delta = Math.min(getHeight() - getPaddingBottom()
							- getPaddingTop() - 1, delta);
				} else {
					// List is moving towards the bottom. Use last view as
					// mMotionPosition
					int offsetToLast = getChildCount() - 1;
					mMotionPosition = mFirstPosition + offsetToLast;

					final View lastView = getChildAt(offsetToLast);
					mMotionViewOriginalTop = lastView.getTop();

					// Don't fling more than 1 screen
					delta = Math.max(-(getHeight() - getPaddingBottom()
							- getPaddingTop() - 1), delta);
				}

				// Check to see if we have bumped into the scroll limit
				View motionView = getChildAt(mMotionPosition - mFirstPosition);
				int oldTop = 0;
				if (motionView != null) {
					oldTop = motionView.getTop();
				}

				// Don't stop just because delta is zero (it could have been
				// rounded)
				final boolean atEdge = trackMotionScroll(delta, delta);
				final boolean atEnd = atEdge && (delta != 0);
				if (atEnd) {
					if (motionView != null) {
						// Tweak the scroll for how far we overshot
						int overshoot = -(delta - (motionView.getTop() - oldTop));
						overScrollBy(0, overshoot, 0, getScrollY(), 0, 0, 0,
								mOverflingDistance, false);
					}
					if (more) {
						edgeReached(delta);
					}
					break;
				}

				if (more && !atEnd) {
					if (atEdge)
						invalidate();
					mLastFlingY = y;
					postOnAnimation(this);
				} else {
					endFling();

					if (PROFILE_FLINGING) {
						if (mFlingProfilingStarted) {
							Debug.stopMethodTracing();
							mFlingProfilingStarted = false;
						}

						// if (mFlingStrictSpan != null) {
						// mFlingStrictSpan.finish();
						// mFlingStrictSpan = null;
						// }
					}
				}
				break;
			}

			case TOUCH_MODE_OVERFLING: {
				final OverScroller scroller = mScroller;
				if (scroller.computeScrollOffset()) {
					final int scrollY = getScrollY();
					final int currY = scroller.getCurrY();
					final int deltaY = currY - scrollY;
					if (overScrollBy(0, deltaY, 0, scrollY, 0, 0, 0,
							mOverflingDistance, false)) {
						final boolean crossDown = scrollY <= 0 && currY > 0;
						final boolean crossUp = scrollY >= 0 && currY < 0;
						if (crossDown || crossUp) {
							int velocity = (int) scroller.getCurrVelocity();
							if (crossUp)
								velocity = -velocity;

							// Don't flywheel from this; we're just continuing
							// things.
							scroller.abortAnimation();
							start(velocity);
						} else {
							startSpringback();
						}
					} else {
						invalidate();
						postOnAnimation(this);
					}
				} else {
					endFling();
				}
				break;
			}
			}
		}
	}

	@Override
	public boolean verifyDrawable(Drawable dr) {
		return mSelector == dr || super.verifyDrawable(dr);
	}

	public ZGridView(Context context, AttributeSet attrs) {

		super(context, attrs);
		mContext = context;
		initView();
		// TODO Auto-generated constructor stub
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ZGridView);

		Drawable d = a.getDrawable(R.styleable.ZGridView_listSelector);
		mSelectionLeftPadding = a.getDimensionPixelOffset(
				R.styleable.ZGridView_selectorLeftPadding, 0);// 24
		mSelectionTopPadding = a.getDimensionPixelOffset(
				R.styleable.ZGridView_selectorTopPadding, 0);// 24
		mSelectionRightPadding = a.getDimensionPixelOffset(
				R.styleable.ZGridView_selectorRightPadding, 0);// 24;
		mSelectionBottomPadding = a.getDimensionPixelOffset(
				R.styleable.ZGridView_selectorBottomPadding, 0);
		if (d != null) {
			setSelector(d);
		}
		mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
		mMinimumVelocity = ViewConfiguration.get(mContext)
				.getScaledMinimumFlingVelocity();
		mMaximumVelocity = ViewConfiguration.get(mContext)
				.getScaledMaximumFlingVelocity();
		mOverscrollDistance = ViewConfiguration.get(mContext)
				.getScaledOverscrollDistance();
		mOverflingDistance = ViewConfiguration.get(mContext)
				.getScaledOverflingDistance();
		mDrawSelectorOnTop = a.getBoolean(
				R.styleable.ZGridView_drawSelectorOnTop, false);

		boolean stackFromBottom = a.getBoolean(
				R.styleable.ZGridView_stackFromBottom, false);
		setStackFromBottom(stackFromBottom);

		boolean scrollingCacheEnabled = a.getBoolean(
				R.styleable.ZGridView_scrollingCache, true);
		setScrollingCacheEnabled(scrollingCacheEnabled);

		boolean useTextFilter = a.getBoolean(
				R.styleable.ZGridView_textFilterEnabled, false);
		setTextFilterEnabled(useTextFilter);

		int transcriptMode = a.getInt(R.styleable.ZGridView_transcriptMode,
				TRANSCRIPT_MODE_DISABLED);
		setTranscriptMode(transcriptMode);

		int color = a.getColor(R.styleable.ZGridView_cacheColorHint, 0);
		setCacheColorHint(color);

		boolean enableFastScroll = a.getBoolean(
				R.styleable.ZGridView_fastScrollEnabled, false);
		// setFastScrollEnabled(enableFastScroll);

		boolean smoothScrollbar = a.getBoolean(
				R.styleable.ZGridView_smoothScrollbar, true);
		// setSmoothScrollbarEnabled(smoothScrollbar);

		// setChoiceMode(a.getInt(R.styleable.ZGridView_choiceMode,
		// CHOICE_MODE_NONE));
		// setFastScrollAlwaysVisible(a.getBoolean(
		// R.styleable.AbsListView_fastScrollAlwaysVisible, false));

		// ////////////////////////////////////////////////////abslistview
		int hSpacing = a.getDimensionPixelOffset(
				R.styleable.ZGridView_horizontalSpace, 0);

		setHorizontalSpacing(hSpacing);

		int vSpacing = a.getDimensionPixelOffset(
				R.styleable.ZGridView_verticalSpace, 0);
		setVerticalSpacing(vSpacing);

		int index = a.getInt(R.styleable.ZGridView_stretchMode,
				STRETCH_COLUMN_WIDTH);
		if (index >= 0) {
			setStretchMode(index);
		}

		int columnWidth = a.getDimensionPixelOffset(
				R.styleable.ZGridView_columnWidth, -1);
		if (columnWidth > 0) {
			setColumnWidth(columnWidth);
		}

		int numColumns = a.getInt(R.styleable.ZGridView_numColumns, 1);
		setNumColumns(numColumns);

		// index = a.getInt(R.styleable.ZGridView_gravity, -1);
		// if (index >= 0) {
		// setGravity(index);
		// }
		setVerticalScrollBarEnabled(true);
		// initializeScrollbars(a);
		a.recycle();

	}

	/**
	 * Get a view and have it show the data associated with the specified
	 * position. This is called when we have already discovered that the view is
	 * not available for reuse in the recycle bin. The only choices left are
	 * converting an old view or making a new one.
	 *
	 * @param position
	 *            The position to display
	 * @param isScrap
	 *            Array of at least 1 boolean, the first entry will become true
	 *            if the returned view was taken from the scrap heap, false if
	 *            otherwise.
	 *
	 * @return A view displaying the data associated with the specified position
	 */
	View obtainView(int position, boolean[] isScrap) {
		isScrap[0] = false;
		View scrapView;

		scrapView = mRecycler.getTransientStateView(position);
		if (scrapView != null) {
			return scrapView;
		}

		scrapView = mRecycler.getScrapView(position);

		View child;
		if (scrapView != null) {
			child = mAdapter.getView(position, scrapView, this);

			// if (child.getImportantForAccessibility() ==
			// IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
			// child.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
			// }

			if (child != scrapView) {
				mRecycler.addScrapView(scrapView, position);
				if (mCacheColorHint != 0) {
					child.setDrawingCacheBackgroundColor(mCacheColorHint);
				}
			} else {
				isScrap[0] = true;
				// child.dispatchFinishTemporaryDetach();
			}
		} else {
			child = mAdapter.getView(position, null, this);

			// if (child.getImportantForAccessibility() ==
			// IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
			// child.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
			// }

			if (mCacheColorHint != 0) {
				child.setDrawingCacheBackgroundColor(mCacheColorHint);
			}
		}

		if (mAdapterHasStableIds) {
			final ZGridView.LayoutParams vlp = (LayoutParams) child
					.getLayoutParams();
			ZGridView.LayoutParams lp;
			if (vlp == null) {
				lp = new ZGridView.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
			} else if (!checkLayoutParams(vlp)) {
				lp = (ZGridView.LayoutParams) generateLayoutParams(vlp);
			} else {
				lp = (ZGridView.LayoutParams) vlp;
			}
			lp.itemId = mAdapter.getItemId(position);
			child.setLayoutParams(lp);
		}

		return child;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		drawSelector(canvas);
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mInLayout = true;
		if (changed) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				getChildAt(i).forceLayout();
			}
			mRecycler.markChildrenDirty();
		}

		// if (mFastScroller != null && mItemCount != mOldItemCount) {
		// mFastScroller.onItemCountChanged(mOldItemCount, mItemCount);
		// }

		layoutChildren();
		mInLayout = false;

		mOverscrollMax = (b - t) / OVERSCROLL_LIMIT_DIVISOR;
	}

	int findSyncPosition() {
		int count = mItemCount;

		if (count == 0) {
			return INVALID_POSITION;
		}

		long idToMatch = mSyncRowId;
		int seed = mSyncPosition;

		// If there isn't a selection don't hunt for it
		if (idToMatch == INVALID_ROW_ID) {
			return INVALID_POSITION;
		}

		// Pin seed to reasonable values
		seed = Math.max(0, seed);
		seed = Math.min(count - 1, seed);

		long endTime = SystemClock.uptimeMillis() + SYNC_MAX_DURATION_MILLIS;

		long rowId;

		// first position scanned so far
		int first = seed;

		// last position scanned so far
		int last = seed;

		// True if we should move down on the next iteration
		boolean next = false;

		// True when we have looked at the first item in the data
		boolean hitFirst;

		// True when we have looked at the last item in the data
		boolean hitLast;

		// Get the item ID locally (instead of getItemIdAtPosition), so
		// we need the adapter
		ListAdapter adapter = getAdapter();
		if (adapter == null) {
			return INVALID_POSITION;
		}

		while (SystemClock.uptimeMillis() <= endTime) {
			rowId = adapter.getItemId(seed);
			if (rowId == idToMatch) {
				// Found it!
				return seed;
			}

			hitLast = last == count - 1;
			hitFirst = first == 0;

			if (hitLast && hitFirst) {
				// Looked at everything
				break;
			}

			if (hitFirst || (next && !hitLast)) {
				// Either we hit the top, or we are trying to move down
				last++;
				seed = last;
				// Try going up next time
				next = false;
			} else if (hitLast || (!next && !hitFirst)) {
				// Either we hit the bottom, or we are trying to move up
				first--;
				seed = first;
				// Try going down next time
				next = true;
			}

		}

		return INVALID_POSITION;
	}

	protected void handleDataChanged() {
		int count = mItemCount;
		int lastHandledItemCount = mLastHandledItemCount;
		mLastHandledItemCount = mItemCount;

		if (mChoiceMode != CHOICE_MODE_NONE && mAdapter != null
				&& mAdapter.hasStableIds()) {
			// confirmCheckedPositionsById();
		}

		// TODO: In the future we can recycle these views based on stable ID
		// instead.
		mRecycler.clearTransientStateViews();

		if (count > 0) {
			int newPos;
			int selectablePos;

			// Find the row we are supposed to sync to
			if (mNeedSync) {
				// Update this first, since setNextSelectedPositionInt inspects
				// it
				mNeedSync = false;
				// mPendingSync = null;

				if (mTranscriptMode == TRANSCRIPT_MODE_ALWAYS_SCROLL) {
					mLayoutMode = LAYOUT_FORCE_BOTTOM;
					return;
				} else if (mTranscriptMode == TRANSCRIPT_MODE_NORMAL) {
					if (mForceTranscriptScroll) {
						mForceTranscriptScroll = false;
						mLayoutMode = LAYOUT_FORCE_BOTTOM;
						return;
					}
					final int childCount = getChildCount();
					final int listBottom = getHeight() - getPaddingBottom();
					final View lastChild = getChildAt(childCount - 1);
					final int lastBottom = lastChild != null ? lastChild
							.getBottom() : listBottom;
					if (mFirstPosition + childCount >= lastHandledItemCount
							&& lastBottom <= listBottom) {
						mLayoutMode = LAYOUT_FORCE_BOTTOM;
						return;
					}
					// Something new came in and we didn't scroll; give the user
					// a clue that
					// there's something new.
					awakenScrollBars();
				}

				switch (mSyncMode) {
				case SYNC_SELECTED_POSITION:
					if (isInTouchMode()) {
						// We saved our state when not in touch mode. (We know
						// this because
						// mSyncMode is SYNC_SELECTED_POSITION.) Now we are
						// trying to
						// restore in touch mode. Just leave mSyncPosition as it
						// is (possibly
						// adjusting if the available range changed) and return.
						mLayoutMode = LAYOUT_SYNC;
						mSyncPosition = Math.min(Math.max(0, mSyncPosition),
								count - 1);

						return;
					} else {
						// See if we can find a position in the new data with
						// the same
						// id as the old selection. This will change
						// mSyncPosition.
						newPos = findSyncPosition();
						if (newPos >= 0) {
							// Found it. Now verify that new selection is still
							// selectable
							selectablePos = lookForSelectablePosition(newPos,
									true);
							if (selectablePos == newPos) {
								// Same row id is selected
								mSyncPosition = newPos;

								if (mSyncHeight == getHeight()) {
									// If we are at the same height as when we
									// saved state, try
									// to restore the scroll position too.
									mLayoutMode = LAYOUT_SYNC;
								} else {
									// We are not the same height as when the
									// selection was saved, so
									// don't try to restore the exact position
									mLayoutMode = LAYOUT_SET_SELECTION;
								}

								// Restore selection
								setNextSelectedPositionInt(newPos);
								return;
							}
						}
					}
					break;
				case SYNC_FIRST_POSITION:
					// Leave mSyncPosition as it is -- just pin to available
					// range
					mLayoutMode = LAYOUT_SYNC;
					mSyncPosition = Math.min(Math.max(0, mSyncPosition),
							count - 1);

					return;
				}
			}

			if (!isInTouchMode()) {
				// We couldn't find matching data -- try to use the same
				// position
				newPos = getSelectedItemPosition();

				// Pin position to the available range
				if (newPos >= count) {
					newPos = count - 1;
				}
				if (newPos < 0) {
					newPos = 0;
				}

				// Make sure we select something selectable -- first look down
				selectablePos = lookForSelectablePosition(newPos, true);

				if (selectablePos >= 0) {
					setNextSelectedPositionInt(selectablePos);
					return;
				} else {
					// Looking down didn't work -- try looking up
					selectablePos = lookForSelectablePosition(newPos, false);
					if (selectablePos >= 0) {
						setNextSelectedPositionInt(selectablePos);
						return;
					}
				}
			} else {

				// We already know where we want to resurrect the selection
				if (mResurrectToPosition >= 0) {
					return;
				}
			}

		}

		// Nothing is selected. Give up and reset everything.
		mLayoutMode = mStackFromBottom ? LAYOUT_FORCE_BOTTOM : LAYOUT_FORCE_TOP;
		mSelectedPosition = INVALID_POSITION;
		mSelectedRowId = INVALID_ROW_ID;
		mNextSelectedPosition = INVALID_POSITION;
		mNextSelectedRowId = INVALID_ROW_ID;
		mNeedSync = false;
		// mPendingSync = null;
		mSelectorPosition = INVALID_POSITION;
		checkSelectionChanged();
	}

	protected void layoutChildren() {
		final boolean blockLayoutRequests = mBlockLayoutRequests;
		if (!blockLayoutRequests) {
			mBlockLayoutRequests = true;
		}

		try {

			invalidate();

			if (mAdapter == null) {
				resetList();
				// invokeOnItemScrollListener();
				return;
			}

			final int childrenTop = mListPadding.top;
			final int childrenBottom = getBottom() - getTop()
					- mListPadding.bottom;

			int childCount = getChildCount();
			int index;
			int delta = 0;

			View sel;
			View oldSel = null;
			View oldFirst = null;
			View newSel = null;

			// Remember stuff we will need down below
			switch (mLayoutMode) {
			case LAYOUT_SET_SELECTION:
				index = mNextSelectedPosition - mFirstPosition;
				if (index >= 0 && index < childCount) {
					newSel = getChildAt(index);
				}
				break;
			case LAYOUT_FORCE_TOP:
			case LAYOUT_FORCE_BOTTOM:
			case LAYOUT_SPECIFIC:
			case LAYOUT_SYNC:
				break;
			case LAYOUT_MOVE_SELECTION:
				if (mNextSelectedPosition >= 0) {
					delta = mNextSelectedPosition - mSelectedPosition;
				}
				break;
			default:
				// Remember the previously selected view
				index = mSelectedPosition - mFirstPosition;
				if (index >= 0 && index < childCount) {
					oldSel = getChildAt(index);
				}

				// Remember the previous first child
				oldFirst = getChildAt(0);
			}

			boolean dataChanged = mDataChanged;
			if (dataChanged) {
				handleDataChanged();
			}

			// Handle the empty set by removing all views that are visible
			// and calling it a day
			if (mItemCount == 0) {
				resetList();
				// invokeOnItemScrollListener();
				return;
			}

			setSelectedPositionInt(mNextSelectedPosition);

			// Pull all children into the RecycleBin.
			// These views will be reused if possible
			final int firstPosition = mFirstPosition;
			final RecycleBin recycleBin = mRecycler;

			if (dataChanged) {
				for (int i = 0; i < childCount; i++) {
					recycleBin.addScrapView(getChildAt(i), firstPosition + i);
				}
			} else {
				recycleBin.fillActiveViews(childCount, firstPosition);
			}

			// Clear out old views
			// removeAllViewsInLayout();
			detachAllViewsFromParent();
			recycleBin.removeSkippedScrap();

			switch (mLayoutMode) {
			case LAYOUT_SET_SELECTION:
				if (newSel != null) {
					sel = fillFromSelection(newSel.getTop(), childrenTop,
							childrenBottom);
				} else {
					sel = fillSelection(childrenTop, childrenBottom);
				}
				break;
			case LAYOUT_FORCE_TOP:
				mFirstPosition = 0;
				sel = fillFromTop(childrenTop);
				adjustViewsUpOrDown();
				break;
			case LAYOUT_FORCE_BOTTOM:
				sel = fillUp(mItemCount - 1, childrenBottom);
				adjustViewsUpOrDown();
				break;
			case LAYOUT_SPECIFIC:
				sel = fillSpecific(mSelectedPosition, mSpecificTop);
				break;
			case LAYOUT_SYNC:
				sel = fillSpecific(mSyncPosition, mSpecificTop);
				break;
			case LAYOUT_MOVE_SELECTION:
				// Move the selection relative to its old position
				sel = moveSelection(delta, childrenTop, childrenBottom);
				break;
			default:
				if (childCount == 0) {
					if (!mStackFromBottom) {
						setSelectedPositionInt(mAdapter == null
								|| isInTouchMode() ? INVALID_POSITION : 0);
						sel = fillFromTop(childrenTop);
					} else {
						final int last = mItemCount - 1;
						setSelectedPositionInt(mAdapter == null
								|| isInTouchMode() ? INVALID_POSITION : last);
						sel = fillFromBottom(last, childrenBottom);
					}
				} else {
					if (mSelectedPosition >= 0
							&& mSelectedPosition < mItemCount) {
						sel = fillSpecific(mSelectedPosition,
								oldSel == null ? childrenTop : oldSel.getTop());
					} else if (mFirstPosition < mItemCount) {
						sel = fillSpecific(
								mFirstPosition,
								oldFirst == null ? childrenTop : oldFirst
										.getTop());
					} else {
						sel = fillSpecific(0, childrenTop);
					}
				}
				break;
			}

			// Flush any cached views that did not get reused above
			recycleBin.scrapActiveViews();

			if (sel != null) {
				positionSelector(INVALID_POSITION, sel);
				mSelectedTop = sel.getTop();
			} else if (mTouchMode > TOUCH_MODE_DOWN
					&& mTouchMode < TOUCH_MODE_SCROLL) {
				View child = getChildAt(mMotionPosition - mFirstPosition);
				if (child != null)
					positionSelector(mMotionPosition, child);
			} else {
				mSelectedTop = 0;
				mSelectorRect.setEmpty();
			}

			mLayoutMode = LAYOUT_NORMAL;
			mDataChanged = false;
			if (mPositionScrollAfterLayout != null) {
				post(mPositionScrollAfterLayout);
				mPositionScrollAfterLayout = null;
			}
			mNeedSync = false;
			setNextSelectedPositionInt(mSelectedPosition);

			updateScrollIndicators();

			if (mItemCount > 0) {
				checkSelectionChanged();
			}

			invokeOnItemScrollListener();
		} finally {
			if (!blockLayoutRequests) {
				mBlockLayoutRequests = false;
			}
		}
	}

	void updateScrollIndicators() {
		if (mScrollUp != null) {
			boolean canScrollUp;
			// 0th element is not visible
			canScrollUp = mFirstPosition > 0;

			// ... Or top of 0th element is not visible
			if (!canScrollUp) {
				if (getChildCount() > 0) {
					View child = getChildAt(0);
					canScrollUp = child.getTop() < mListPadding.top;
				}
			}

			mScrollUp
					.setVisibility(canScrollUp ? View.VISIBLE : View.INVISIBLE);
		}

		if (mScrollDown != null) {
			boolean canScrollDown;
			int count = getChildCount();

			// Last item is not visible
			canScrollDown = (mFirstPosition + count) < mItemCount;

			// ... Or bottom of the last element is not visible
			if (!canScrollDown && count > 0) {
				View child = getChildAt(count - 1);
				canScrollDown = child.getBottom() > getPaddingBottom()
						- mListPadding.bottom;
			}

			mScrollDown.setVisibility(canScrollDown ? View.VISIBLE
					: View.INVISIBLE);
		}
	}

	void positionSelector(int position, View sel) {
		if (position != INVALID_POSITION) {
			mSelectorPosition = position;
		}

		final Rect selectorRect = mSelectorRect;
		selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(),
				sel.getBottom());
		positionSelector(selectorRect.left, selectorRect.top,
				selectorRect.right, selectorRect.bottom);

		refreshDrawableState();

	}

	private void positionSelector(int l, int t, int r, int b) {
		mSelectorRect.set(l - mSelectionLeftPadding, t - mSelectionTopPadding,
				r + mSelectionRightPadding, b + mSelectionBottomPadding);

	}

	private View fillFromBottom(int lastPosition, int nextBottom) {
		lastPosition = Math.max(lastPosition, mSelectedPosition);
		lastPosition = Math.min(lastPosition, mItemCount - 1);

		final int invertedPosition = mItemCount - 1 - lastPosition;
		lastPosition = mItemCount - 1
				- (invertedPosition - (invertedPosition % mNumColumns));

		return fillUp(lastPosition, nextBottom);
	}

	private View moveSelection(int delta, int childrenTop, int childrenBottom) {
		final int fadingEdgeLength = getVerticalFadingEdgeLength();
		final int selectedPosition = mSelectedPosition;
		final int numColumns = mNumColumns;
		final int verticalSpacing = mVerticalSpacing;

		int oldRowStart;
		int rowStart;
		int rowEnd = -1;

		if (!mStackFromBottom) {
			oldRowStart = (selectedPosition - delta)
					- ((selectedPosition - delta) % numColumns);

			rowStart = selectedPosition - (selectedPosition % numColumns);
		} else {
			int invertedSelection = mItemCount - 1 - selectedPosition;

			rowEnd = mItemCount - 1
					- (invertedSelection - (invertedSelection % numColumns));
			rowStart = Math.max(0, rowEnd - numColumns + 1);

			invertedSelection = mItemCount - 1 - (selectedPosition - delta);
			oldRowStart = mItemCount - 1
					- (invertedSelection - (invertedSelection % numColumns));
			oldRowStart = Math.max(0, oldRowStart - numColumns + 1);
		}

		final int rowDelta = rowStart - oldRowStart;

		final int topSelectionPixel = getTopSelectionPixel(childrenTop,
				fadingEdgeLength, rowStart);
		final int bottomSelectionPixel = getBottomSelectionPixel(
				childrenBottom, fadingEdgeLength, numColumns, rowStart);

		// Possibly changed again in fillUp if we add rows above this one.
		mFirstPosition = rowStart;

		View sel;
		View referenceView;

		if (rowDelta > 0) {
			/*
			 * Case 1: Scrolling down.
			 */

			final int oldBottom = mReferenceViewInSelectedRow == null ? 0
					: mReferenceViewInSelectedRow.getBottom();

			sel = makeRow(mStackFromBottom ? rowEnd : rowStart, oldBottom
					+ verticalSpacing, true);
			referenceView = mReferenceView;

			adjustForBottomFadingEdge(referenceView, topSelectionPixel,
					bottomSelectionPixel);
		} else if (rowDelta < 0) {
			/*
			 * Case 2: Scrolling up.
			 */
			final int oldTop = mReferenceViewInSelectedRow == null ? 0
					: mReferenceViewInSelectedRow.getTop();

			sel = makeRow(mStackFromBottom ? rowEnd : rowStart, oldTop
					- verticalSpacing, false);
			referenceView = mReferenceView;

			adjustForTopFadingEdge(referenceView, topSelectionPixel,
					bottomSelectionPixel);
		} else {
			/*
			 * Keep selection where it was
			 */
			final int oldTop = mReferenceViewInSelectedRow == null ? 0
					: mReferenceViewInSelectedRow.getTop();

			sel = makeRow(mStackFromBottom ? rowEnd : rowStart, oldTop, true);
			referenceView = mReferenceView;
		}

		if (!mStackFromBottom) {
			fillUp(rowStart - numColumns, referenceView.getTop()
					- verticalSpacing);
			adjustViewsUpOrDown();
			fillDown(rowStart + numColumns, referenceView.getBottom()
					+ verticalSpacing);
		} else {
			fillDown(rowEnd + numColumns, referenceView.getBottom()
					+ verticalSpacing);
			adjustViewsUpOrDown();
			fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
		}

		return sel;
	}

	private View fillDown(int pos, int nextTop) {
		View selectedView = null;

		int end = (getBottom() - getTop());
		// if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
		// end -= mListPadding.bottom;
		// }

		while (nextTop < end && pos < mItemCount) {
			View temp = makeRow(pos, nextTop, true);
			if (temp != null) {
				selectedView = temp;
			}

			// mReferenceView will change with each call to makeRow()
			// do not cache in a local variable outside of this loop
			nextTop = mReferenceView.getBottom() + mVerticalSpacing;

			pos += mNumColumns;
		}

		setVisibleRangeHint(mFirstPosition, mFirstPosition + getChildCount()
				- 1);
		return selectedView;
	}

	void setVisibleRangeHint(int start, int end) {
		// if (mRemoteAdapter != null) {
		// mRemoteAdapter.setVisibleRangeHint(start, end);
		// }
	}

	private void adjustForTopFadingEdge(View childInSelectedRow,
			int topSelectionPixel, int bottomSelectionPixel) {
		// Some of the newly selected item extends above the top of the list
		if (childInSelectedRow.getTop() < topSelectionPixel) {
			// Find space required to bring the top of the selected item
			// fully into view
			int spaceAbove = topSelectionPixel - childInSelectedRow.getTop();

			// Find space available below the selection into which we can
			// scroll downwards
			int spaceBelow = bottomSelectionPixel
					- childInSelectedRow.getBottom();
			int offset = Math.min(spaceAbove, spaceBelow);

			// Now offset the selected item to get it into view
			offsetChildrenTOpAndBottom(offset);
		}
	}

	private void adjustForBottomFadingEdge(View childInSelectedRow,
			int topSelectionPixel, int bottomSelectionPixel) {
		// Some of the newly selected item extends below the bottom of the
		// list
		if (childInSelectedRow.getBottom() > bottomSelectionPixel) {

			// Find space available above the selection into which we can
			// scroll upwards
			int spaceAbove = childInSelectedRow.getTop() - topSelectionPixel;

			// Find space required to bring the bottom of the selected item
			// fully into view
			int spaceBelow = childInSelectedRow.getBottom()
					- bottomSelectionPixel;
			int offset = Math.min(spaceAbove, spaceBelow);

			// Now offset the selected item to get it into view
			offsetChildrenTOpAndBottom(-offset);

		}
	}

	private void offsetChildrenTOpAndBottom(int offset) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View v = getChildAt(i);
			v.offsetTopAndBottom(offset);
		}
	}

	public boolean isLayoutRtl() {
		return false;
	}

	private View makeRow(int startPos, int y, boolean flow) {
		final int columnWidth = mColumnWidth;
		final int horizontalSpacing = mHorizontalSpacing;

		final boolean isLayoutRtl = isLayoutRtl();

		int last;
		int nextLeft;

		if (isLayoutRtl) {
			nextLeft = getWidth()
					- mListPadding.right
					- columnWidth
					- ((mStretchMode == STRETCH_SPACING_UNIFORM) ? horizontalSpacing
							: 0);
		} else {
			nextLeft = mListPadding.left
					+ ((mStretchMode == STRETCH_SPACING_UNIFORM) ? horizontalSpacing
							: 0);
		}

		if (!mStackFromBottom) {
			last = Math.min(startPos + mNumColumns, mItemCount);
		} else {
			last = startPos + 1;
			startPos = Math.max(0, startPos - mNumColumns + 1);

			if (last - startPos < mNumColumns) {
				final int deltaLeft = (mNumColumns - (last - startPos))
						* (columnWidth + horizontalSpacing);
				nextLeft += (isLayoutRtl ? -1 : +1) * deltaLeft;
			}
		}

		View selectedView = null;
		final int selectedPosition = mSelectedPosition;

		View child = null;
		for (int pos = startPos; pos < last; pos++) {
			// is this the selected item?
			boolean selected = pos == selectedPosition;
			// does the list view have focus or contain focus

			final int where = flow ? -1 : pos - startPos;
			child = makeAndAddView(pos, y, flow, nextLeft, selected, where);

			nextLeft += (isLayoutRtl ? -1 : +1) * columnWidth;
			if (pos < last - 1) {
				nextLeft += horizontalSpacing;
			}

			if (selected) {
				selectedView = child;
			}
		}

		mReferenceView = child;

		if (selectedView != null) {
			mReferenceViewInSelectedRow = mReferenceView;
		}

		return selectedView;
	}

	private View makeAndAddView(int position, int y, boolean flow,
			int childrenLeft, boolean selected, int where) {
		View child;

		if (!mDataChanged) {
			// Try to use an existing view for this position
			child = mRecycler.getActiveView(position);
			if (child != null) {
				// Found it -- we're using an existing child
				// This just needs to be positioned
				setupChild(child, position, y, flow, childrenLeft, selected,
						true, where);
				return child;
			}
		}

		// Make a new view for this position, or convert an unused view if
		// possible
		child = obtainView(position, mIsScrap);

		// This needs to be positioned and measured
		setupChild(child, position, y, flow, childrenLeft, selected,
				mIsScrap[0], where);

		return child;
	}

	private void setupChild(View child, int position, int y, boolean flow,
			int childrenLeft, boolean selected, boolean recycled, int where) {
		boolean isSelected = selected && shouldShowSelector();
		final boolean updateChildSelected = isSelected != child.isSelected();
		final int mode = mTouchMode;
		final boolean isPressed = mode > TOUCH_MODE_DOWN
				&& mode < TOUCH_MODE_SCROLL && mMotionPosition == position;
		final boolean updateChildPressed = isPressed != child.isPressed();

		boolean needToMeasure = !recycled || updateChildSelected
				|| child.isLayoutRequested();

		// Respect layout params that are already in the view. Otherwise make
		// some up...
		ZGridView.LayoutParams p = (ZGridView.LayoutParams) child
				.getLayoutParams();
		if (p == null) {
			p = new ZGridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		p.viewType = mAdapter.getItemViewType(position);

		if (recycled && !p.forceAdd) {
			attachViewToParent(child, where, p);
		} else {
			p.forceAdd = false;
			addViewInLayout(child, where, p, true);
		}

		if (updateChildSelected) {
			child.setSelected(isSelected);
			if (isSelected) {
				requestFocus();
			}
		}

		if (updateChildPressed) {
			child.setPressed(isPressed);
		}

		if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
			if (child instanceof Checkable) {
				((Checkable) child).setChecked(mCheckStates.get(position));
			} else if (getContext().getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				child.setActivated(mCheckStates.get(position));
			}
		}

		if (needToMeasure) {
			int childHeightSpec = ViewGroup.getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0,
					p.height);

			int childWidthSpec = ViewGroup.getChildMeasureSpec(MeasureSpec
					.makeMeasureSpec(mColumnWidth, MeasureSpec.EXACTLY), 0,
					p.width);
			child.measure(childWidthSpec, childHeightSpec);
		} else {
			cleanupLayoutState(child);
		}

		final int w = child.getMeasuredWidth();
		final int h = child.getMeasuredHeight();

		int childLeft;
		final int childTop = flow ? y : y - h;

		final int layoutDirection = getLayoutDirection();
		final int absoluteGravity = Gravity.getAbsoluteGravity(mGravity,
				layoutDirection);
		switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
		case Gravity.LEFT:
			childLeft = childrenLeft;
			break;
		case Gravity.CENTER_HORIZONTAL:
			childLeft = childrenLeft + ((mColumnWidth - w) / 2);
			break;
		case Gravity.RIGHT:
			childLeft = childrenLeft + mColumnWidth - w;
			break;
		default:
			childLeft = childrenLeft;
			break;
		}

		if (needToMeasure) {
			final int childRight = childLeft + w;
			final int childBottom = childTop + h;
			child.layout(childLeft, childTop, childRight, childBottom);
		} else {
			child.offsetLeftAndRight(childLeft - child.getLeft());
			child.offsetTopAndBottom(childTop - child.getTop());
		}

		if (mCachingStarted) {
			child.setDrawingCacheEnabled(true);
		}

		if (recycled
				&& (((ZGridView.LayoutParams) child.getLayoutParams()).scrappedFromPosition) != position) {
			child.jumpDrawablesToCurrentState();
		}
	}

	private int getBottomSelectionPixel(int childrenBottom,
			int fadingEdgeLength, int numColumns, int rowStart) {
		// Last pixel we can draw the selection into
		int bottomSelectionPixel = childrenBottom;
		if (rowStart + numColumns - 1 < mItemCount - 1) {
			bottomSelectionPixel -= fadingEdgeLength;
		}
		return bottomSelectionPixel;
	}

	private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength,
			int rowStart) {
		// first pixel we can draw the selection into
		int topSelectionPixel = childrenTop;
		if (rowStart > 0) {
			topSelectionPixel += fadingEdgeLength;
		}
		return topSelectionPixel;
	}

	private View fillSpecific(int position, int top) {
		final int numColumns = mNumColumns;

		int motionRowStart;
		int motionRowEnd = -1;

		if (!mStackFromBottom) {
			motionRowStart = position - (position % numColumns);
		} else {
			final int invertedSelection = mItemCount - 1 - position;

			motionRowEnd = mItemCount - 1
					- (invertedSelection - (invertedSelection % numColumns));
			motionRowStart = Math.max(0, motionRowEnd - numColumns + 1);
		}

		final View temp = makeRow(mStackFromBottom ? motionRowEnd
				: motionRowStart, top, true);

		// Possibly changed again in fillUp if we add rows above this one.
		mFirstPosition = motionRowStart;

		final View referenceView = mReferenceView;
		// We didn't have anything to layout, bail out
		if (referenceView == null) {
			return null;
		}

		final int verticalSpacing = mVerticalSpacing;

		View above;
		View below;

		if (!mStackFromBottom) {
			above = fillUp(motionRowStart - numColumns, referenceView.getTop()
					- verticalSpacing);
			adjustViewsUpOrDown();
			below = fillDown(motionRowStart + numColumns,
					referenceView.getBottom() + verticalSpacing);
			// Check if we have dragged the bottom of the grid too high
			final int childCount = getChildCount();
			if (childCount > 0) {
				correctTooHigh(numColumns, verticalSpacing, childCount);
			}
		} else {
			below = fillDown(motionRowEnd + numColumns,
					referenceView.getBottom() + verticalSpacing);
			adjustViewsUpOrDown();
			above = fillUp(motionRowStart - 1, referenceView.getTop()
					- verticalSpacing);
			// Check if we have dragged the bottom of the grid too high
			final int childCount = getChildCount();
			if (childCount > 0) {
				correctTooLow(numColumns, verticalSpacing, childCount);
			}
		}

		if (temp != null) {
			return temp;
		} else if (above != null) {
			return above;
		} else {
			return below;
		}
	}

	private void correctTooHigh(int numColumns, int verticalSpacing,
			int childCount) {
		// First see if the last item is visible
		final int lastPosition = mFirstPosition + childCount - 1;
		if (lastPosition == mItemCount - 1 && childCount > 0) {
			// Get the last child ...
			final View lastChild = getChildAt(childCount - 1);

			// ... and its bottom edge
			final int lastBottom = lastChild.getBottom();
			// This is bottom of our drawable area
			final int end = (getBottom() - getTop()) - mListPadding.bottom;

			// This is how far the bottom edge of the last view is from the
			// bottom of the
			// drawable area
			int bottomOffset = end - lastBottom;

			final View firstChild = getChildAt(0);
			final int firstTop = firstChild.getTop();

			// Make sure we are 1) Too high, and 2) Either there are more rows
			// above the
			// first row or the first row is scrolled off the top of the
			// drawable area
			if (bottomOffset > 0
					&& (mFirstPosition > 0 || firstTop < mListPadding.top)) {
				if (mFirstPosition == 0) {
					// Don't pull the top too far down
					bottomOffset = Math.min(bottomOffset, mListPadding.top
							- firstTop);
				}

				// Move everything down
				offsetChildrenTOpAndBottom(bottomOffset);
				if (mFirstPosition > 0) {
					// Fill the gap that was opened above mFirstPosition with
					// more rows, if
					// possible
					fillUp(mFirstPosition - (mStackFromBottom ? 1 : numColumns),
							firstChild.getTop() - verticalSpacing);
					// Close up the remaining gap
					adjustViewsUpOrDown();
				}
			}
		}
	}

	private void correctTooLow(int numColumns, int verticalSpacing,
			int childCount) {
		if (mFirstPosition == 0 && childCount > 0) {
			// Get the first child ...
			final View firstChild = getChildAt(0);

			// ... and its top edge
			final int firstTop = firstChild.getTop();

			// This is top of our drawable area
			final int start = mListPadding.top;

			// This is bottom of our drawable area
			final int end = (getBottom() - getTop()) - mListPadding.bottom;

			// This is how far the top edge of the first view is from the top of
			// the
			// drawable area
			int topOffset = firstTop - start;
			final View lastChild = getChildAt(childCount - 1);
			final int lastBottom = lastChild.getBottom();
			final int lastPosition = mFirstPosition + childCount - 1;

			// Make sure we are 1) Too low, and 2) Either there are more rows
			// below the
			// last row or the last row is scrolled off the bottom of the
			// drawable area
			if (topOffset > 0
					&& (lastPosition < mItemCount - 1 || lastBottom > end)) {
				if (lastPosition == mItemCount - 1) {
					// Don't pull the bottom too far up
					topOffset = Math.min(topOffset, lastBottom - end);
				}

				// Move everything up
				offsetChildrenTOpAndBottom(-topOffset);
				if (lastPosition < mItemCount - 1) {
					// Fill the gap that was opened below the last position with
					// more rows, if
					// possible
					fillDown(lastPosition
							+ (!mStackFromBottom ? 1 : numColumns),
							lastChild.getBottom() + verticalSpacing);
					// Close up the remaining gap
					adjustViewsUpOrDown();
				}
			}
		}
	}

	/**
	 * Fills the list from pos up to the top of the list view.
	 *
	 * @param pos
	 *            The first position to put in the list
	 *
	 * @param nextBottom
	 *            The location where the bottom of the item associated with pos
	 *            should be drawn
	 *
	 * @return The view that is currently selected
	 */
	private View fillUp(int pos, int nextBottom) {
		View selectedView = null;

		int end = 0;
		// if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
		// end = mListPadding.top;
		// }

		while (nextBottom > end && pos >= 0) {

			View temp = makeRow(pos, nextBottom, false);
			if (temp != null) {
				selectedView = temp;
			}

			nextBottom = mReferenceView.getTop() - mVerticalSpacing;

			mFirstPosition = pos;

			pos -= mNumColumns;
		}

		if (mStackFromBottom) {
			mFirstPosition = Math.max(0, pos + 1);
		}

		setVisibleRangeHint(mFirstPosition, mFirstPosition + getChildCount()
				- 1);
		return selectedView;
	}

	/**
	 * Make sure views are touching the top or bottom edge, as appropriate for
	 * our gravity
	 */
	private void adjustViewsUpOrDown() {
		final int childCount = getChildCount();

		if (childCount > 0) {
			int delta;
			View child;

			if (!mStackFromBottom) {
				// Uh-oh -- we came up short. Slide all views up to make them
				// align with the top
				child = getChildAt(0);
				delta = child.getTop() - mListPadding.top;
				if (mFirstPosition != 0) {
					// It's OK to have some space above the first item if it is
					// part of the vertical spacing
					delta -= mVerticalSpacing;
				}
				if (delta < 0) {
					// We only are looking to see if we are too low, not too
					// high
					delta = 0;
				}
			} else {
				// we are too high, slide all views down to align with bottom
				child = getChildAt(childCount - 1);
				delta = child.getBottom() - (getHeight() - mListPadding.bottom);

				if (mFirstPosition + childCount < mItemCount) {
					// It's OK to have some space below the last item if it is
					// part of the vertical spacing
					delta += mVerticalSpacing;
				}

				if (delta > 0) {
					// We only are looking to see if we are too high, not too
					// low
					delta = 0;
				}
			}

			if (delta != 0) {
				offsetChildrenTOpAndBottom(-delta);
			}
		}
	}

	int reconcileSelectedPosition() {
		int position = mSelectedPosition;
		if (position < 0) {
			position = mResurrectToPosition;
		}
		position = Math.max(0, position);
		position = Math.min(position, mItemCount - 1);
		return position;
	}

	private View fillFromTop(int nextTop) {
		mFirstPosition = Math.min(mFirstPosition, mSelectedPosition);
		mFirstPosition = Math.min(mFirstPosition, mItemCount - 1);
		if (mFirstPosition < 0) {
			mFirstPosition = 0;
		}
		mFirstPosition -= mFirstPosition % mNumColumns;
		return fillDown(mFirstPosition, nextTop);
	}

	private View fillSelection(int childrenTop, int childrenBottom) {
		final int selectedPosition = reconcileSelectedPosition();
		final int numColumns = mNumColumns;
		final int verticalSpacing = mVerticalSpacing;

		int rowStart;
		int rowEnd = -1;

		if (!mStackFromBottom) {
			rowStart = selectedPosition - (selectedPosition % numColumns);
		} else {
			final int invertedSelection = mItemCount - 1 - selectedPosition;

			rowEnd = mItemCount - 1
					- (invertedSelection - (invertedSelection % numColumns));
			rowStart = Math.max(0, rowEnd - numColumns + 1);
		}

		final int fadingEdgeLength = getVerticalFadingEdgeLength();
		final int topSelectionPixel = getTopSelectionPixel(childrenTop,
				fadingEdgeLength, rowStart);

		final View sel = makeRow(mStackFromBottom ? rowEnd : rowStart,
				topSelectionPixel, true);
		mFirstPosition = rowStart;

		final View referenceView = mReferenceView;

		if (!mStackFromBottom) {
			fillDown(rowStart + numColumns, referenceView.getBottom()
					+ verticalSpacing);
			pinToBottom(childrenBottom);
			fillUp(rowStart - numColumns, referenceView.getTop()
					- verticalSpacing);
			adjustViewsUpOrDown();
		} else {
			final int bottomSelectionPixel = getBottomSelectionPixel(
					childrenBottom, fadingEdgeLength, numColumns, rowStart);
			final int offset = bottomSelectionPixel - referenceView.getBottom();
			offsetChildrenTOpAndBottom(offset);
			fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
			pinToTop(childrenTop);
			fillDown(rowEnd + numColumns, referenceView.getBottom()
					+ verticalSpacing);
			adjustViewsUpOrDown();
		}

		return sel;
	}

	private void pinToTop(int childrenTop) {
		if (mFirstPosition == 0) {
			final int top = getChildAt(0).getTop();
			final int offset = childrenTop - top;
			if (offset < 0) {
				offsetChildrenTOpAndBottom(offset);
			}
		}
	}

	private void pinToBottom(int childrenBottom) {
		final int count = getChildCount();
		if (mFirstPosition + count == mItemCount) {
			final int bottom = getChildAt(count - 1).getBottom();
			final int offset = childrenBottom - bottom;
			if (offset > 0) {
				offsetChildrenTOpAndBottom(offset);
			}
		}
	}

	private View fillFromSelection(int selectedTop, int childrenTop,
			int childrenBottom) {
		final int fadingEdgeLength = getVerticalFadingEdgeLength();
		final int selectedPosition = mSelectedPosition;
		final int numColumns = mNumColumns;
		final int verticalSpacing = mVerticalSpacing;

		int rowStart;
		int rowEnd = -1;

		if (!mStackFromBottom) {
			rowStart = selectedPosition - (selectedPosition % numColumns);
		} else {
			int invertedSelection = mItemCount - 1 - selectedPosition;

			rowEnd = mItemCount - 1
					- (invertedSelection - (invertedSelection % numColumns));
			rowStart = Math.max(0, rowEnd - numColumns + 1);
		}

		View sel;
		View referenceView;

		int topSelectionPixel = getTopSelectionPixel(childrenTop,
				fadingEdgeLength, rowStart);
		int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom,
				fadingEdgeLength, numColumns, rowStart);

		sel = makeRow(mStackFromBottom ? rowEnd : rowStart, selectedTop, true);
		// Possibly changed again in fillUp if we add rows above this one.
		mFirstPosition = rowStart;

		referenceView = mReferenceView;
		// adjustForTopFadingEdge(referenceView, topSelectionPixel,
		// bottomSelectionPixel);
		// adjustForBottomFadingEdge(referenceView, topSelectionPixel,
		// bottomSelectionPixel);

		if (!mStackFromBottom) {
			fillUp(rowStart - numColumns, referenceView.getTop()
					- verticalSpacing);
			// adjustViewsUpOrDown();
			fillDown(rowStart + numColumns, referenceView.getBottom()
					+ verticalSpacing);
		} else {
			fillDown(rowEnd + numColumns, referenceView.getBottom()
					+ verticalSpacing);
			// adjustViewsUpOrDown();
			fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
		}

		return sel;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Sets up mListPadding
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final Rect listPadding = mListPadding;
		listPadding.left = 0 + getPaddingLeft();
		listPadding.top = 0 + getPaddingTop();
		listPadding.right = 0 + getPaddingRight();
		listPadding.bottom = 0 + getPaddingBottom();

		// Check if our previous measured size was at a point where we should
		// scroll later.
		// if (mTranscriptMode == TRANSCRIPT_MODE_NORMAL) {
		// final int childCount = getChildCount();
		// final int listBottom = getHeight() - getPaddingBottom();
		// final View lastChild = getChildAt(childCount - 1);
		// final int lastBottom = lastChild != null ? lastChild.getBottom() :
		// listBottom;
		// mForceTranscriptScroll = mFirstPosition + childCount >=
		// mLastHandledItemCount &&
		// lastBottom <= listBottom;
		// }

		// /////////////////abs
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode == MeasureSpec.UNSPECIFIED) {
			if (mColumnWidth > 0) {
				widthSize = mColumnWidth + mListPadding.left
						+ mListPadding.right;
			} else {
				widthSize = mListPadding.left + mListPadding.right;
			}
			widthSize += getVerticalScrollbarWidth();
		}

		int childWidth = widthSize - mListPadding.left - mListPadding.right;
		boolean didNotInitiallyFit = determineColumns(childWidth);

		int childHeight = 0;
		int childState = 0;

		mItemCount = mAdapter == null ? 0 : mAdapter.getCount();
		final int count = mItemCount;
		if (count > 0) {
			final View child = obtainView(0, mIsScrap);

			ZGridView.LayoutParams p = (ZGridView.LayoutParams) child
					.getLayoutParams();
			if (p == null) {
				p = new ZGridView.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				child.setLayoutParams(p);
			}
			p.viewType = mAdapter.getItemViewType(0);
			p.forceAdd = true;

			int childHeightSpec = getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0,
					p.height);
			int childWidthSpec = getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(mColumnWidth,
							MeasureSpec.EXACTLY), 0, p.width);
			child.measure(childWidthSpec, childHeightSpec);

			childHeight = child.getMeasuredHeight();
			childState = combineMeasuredStates(childState,
					child.getMeasuredState());

			if (mRecycler.shouldRecycleViewType(0)) {
				mRecycler.addScrapView(child, -1);
			}
		}

		if (heightMode == MeasureSpec.UNSPECIFIED) {
			heightSize = mListPadding.top + mListPadding.bottom + childHeight
					+ getVerticalFadingEdgeLength() * 2;
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			int ourSize = mListPadding.top + mListPadding.bottom;

			final int numColumns = mNumColumns;
			for (int i = 0; i < count; i += numColumns) {
				ourSize += childHeight;
				if (i + numColumns < count) {
					ourSize += mVerticalSpacing;
				}
				if (ourSize >= heightSize) {
					ourSize = heightSize;
					break;
				}
			}
			heightSize = ourSize;
		}

		if (widthMode == MeasureSpec.AT_MOST
				&& mRequestedNumColumns != AUTO_FIT) {
			int ourSize = (mRequestedNumColumns * mColumnWidth)
					+ ((mRequestedNumColumns - 1) * mHorizontalSpacing)
					+ mListPadding.left + mListPadding.right;
			if (ourSize > widthSize || didNotInitiallyFit) {
				widthSize |= MEASURED_STATE_TOO_SMALL;
			}
		}

		setMeasuredDimension(widthSize, heightSize);
		mWidthMeasureSpec = widthMeasureSpec;
	}

	private boolean determineColumns(int availableSpace) {
		final int requestedHorizontalSpacing = mRequestedHorizontalSpacing;
		final int stretchMode = mStretchMode;
		final int requestedColumnWidth = mRequestedColumnWidth;
		boolean didNotInitiallyFit = false;

		if (mRequestedNumColumns == AUTO_FIT) {
			if (requestedColumnWidth > 0) {
				// Client told us to pick the number of columns
				mNumColumns = (availableSpace + requestedHorizontalSpacing)
						/ (requestedColumnWidth + requestedHorizontalSpacing);
			} else {
				// Just make up a number if we don't have enough info
				mNumColumns = 2;
			}
		} else {
			// We picked the columns
			mNumColumns = mRequestedNumColumns;
		}

		if (mNumColumns <= 0) {
			mNumColumns = 1;
		}

		switch (stretchMode) {
		case NO_STRETCH:
			// Nobody stretches
			mColumnWidth = requestedColumnWidth;
			mHorizontalSpacing = requestedHorizontalSpacing;
			break;

		default:
			int spaceLeftOver = availableSpace
					- (mNumColumns * requestedColumnWidth)
					- ((mNumColumns - 1) * requestedHorizontalSpacing);

			if (spaceLeftOver < 0) {
				didNotInitiallyFit = true;
			}

			switch (stretchMode) {
			case STRETCH_COLUMN_WIDTH:
				// Stretch the columns
				mColumnWidth = requestedColumnWidth + spaceLeftOver
						/ mNumColumns;
				mHorizontalSpacing = requestedHorizontalSpacing;
				break;

			case STRETCH_SPACING:
				// Stretch the spacing between columns
				mColumnWidth = requestedColumnWidth;
				if (mNumColumns > 1) {
					mHorizontalSpacing = requestedHorizontalSpacing
							+ spaceLeftOver / (mNumColumns - 1);
				} else {
					mHorizontalSpacing = requestedHorizontalSpacing
							+ spaceLeftOver;
				}
				break;

			case STRETCH_SPACING_UNIFORM:
				// Stretch the spacing between columns
				mColumnWidth = requestedColumnWidth;
				if (mNumColumns > 1) {
					mHorizontalSpacing = requestedHorizontalSpacing
							+ spaceLeftOver / (mNumColumns + 1);
				} else {
					mHorizontalSpacing = requestedHorizontalSpacing
							+ spaceLeftOver;
				}
				break;
			}

			break;
		}
		return didNotInitiallyFit;
	}

	public void setGravity(int gravity) {
		if (mGravity != gravity) {
			mGravity = gravity;
			requestLayoutIfNecessary();
		}
	}

	public void setColumnWidth(int columnWidth) {
		if (columnWidth != mRequestedColumnWidth) {
			mRequestedColumnWidth = columnWidth;
			requestLayoutIfNecessary();
		}
	}

	public void setNumColumns(int numColumns) {
		if (numColumns != mRequestedNumColumns) {
			mRequestedNumColumns = numColumns;
			requestLayoutIfNecessary();
		}
	}

	public void setStretchMode(int stretchMode) {
		if (stretchMode != mStretchMode) {
			mStretchMode = stretchMode;
			requestLayoutIfNecessary();
		}
	}

	public void setVerticalSpacing(int verticalSpacing) {
		if (verticalSpacing != mVerticalSpacing) {
			mVerticalSpacing = verticalSpacing;
			requestLayoutIfNecessary();
		}
	}

	public void setHorizontalSpacing(int horizontalSpacing) {
		if (horizontalSpacing != mRequestedHorizontalSpacing) {
			mRequestedHorizontalSpacing = horizontalSpacing;
			requestLayoutIfNecessary();
		}
	}

	public void setTextFilterEnabled(boolean textFilterEnabled) {
		mTextFilterEnabled = textFilterEnabled;
	}

	void requestLayoutIfNecessary() {
		if (getChildCount() > 0) {
			resetList();
			requestLayout();
			invalidate();
		}
	}

	public void setStackFromBottom(boolean stackFromBottom) {
		if (mStackFromBottom != stackFromBottom) {
			mStackFromBottom = stackFromBottom;
			requestLayoutIfNecessary();
		}
	}

	/**
	 * Indicates whether this view is in a state where the selector should be
	 * drawn. This will happen if we have focus but are not in touch mode, or we
	 * are in the middle of displaying the pressed state for an item.
	 *
	 * @return True if the selector should be shown
	 */
	boolean shouldShowSelector() {
		return (hasFocus() && !isInTouchMode())
				|| touchModeDrawsInPressedState();

	}

	// protected boolean shouldShowSelector() {
	// return hasFocus();
	// }
	public void setTranscriptMode(int mode) {
		mTranscriptMode = mode;
	}

	private void drawSelector(Canvas canvas) {
		if ((shouldShowSelector() && mSelector != null && !mSelectorRect
				.isEmpty())
				|| (hover && mSelector != null && !mSelectorRect.isEmpty())) {
			final Drawable selector = mSelector;
			selector.setBounds(mSelectorRect);
			selector.draw(canvas);
		}
	}

	boolean touchModeDrawsInPressedState() {
		// FIXME use isPressed for this
		switch (mTouchMode) {
		case TOUCH_MODE_TAP:
		case TOUCH_MODE_DONE_WAITING:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Controls whether the selection highlight drawable should be drawn on top
	 * of the item or behind it.
	 *
	 * @param onTop
	 *            If true, the selector will be drawn on the item it is
	 *            highlighting. The default is false.
	 *
	 * @attr ref android.R.styleable#AbsListView_drawSelectorOnTop
	 */
	public void setDrawSelectorOnTop(boolean onTop) {
		mDrawSelectorOnTop = onTop;
	}

	/**
	 * Set a Drawable that should be used to highlight the currently selected
	 * item.
	 *
	 * @param resID
	 *            A Drawable resource to use as the selection highlight.
	 *
	 * @attr ref android.R.styleable#AbsListView_listSelector
	 */
	public void setSelector(int resID) {
		setSelector(getResources().getDrawable(resID));
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mSelector != null) {
			mSelector.setState(getDrawableState());
		}
	}

	public void setSelector(Drawable sel) {
		if (mSelector != null) {
			mSelector.setCallback(null);
			unscheduleDrawable(mSelector);
		}
		mSelector = sel;
		Rect padding = new Rect();
		sel.getPadding(padding);
		mSelectionLeftPadding1 = padding.left;
		mSelectionTopPadding1 = padding.top;
		mSelectionRightPadding1 = padding.right;
		mSelectionBottomPadding1 = padding.bottom;

		// mSelectionLeftPadding = 0;
		// mSelectionTopPadding = 0;
		// mSelectionRightPadding = 0;
		// mSelectionBottomPadding = 0;
		sel.setCallback(this);
		updateSelectorState();
	}

	void updateSelectorState() {
		if (mSelector != null) {
			if (shouldShowSelector()) {
				mSelector.setState(getDrawableState());
			} else {
				mSelector.setState(StateSet.NOTHING);
			}
		}
	}

	/**
	 * Returns the selector {@link Drawable} that is
	 * used to draw the selection in the list.
	 *
	 * @return the drawable used to display the selector
	 */
	public Drawable getSelector() {
		return mSelector;
	}

	public void setCacheColorHint(int color) {
		if (color != mCacheColorHint) {
			mCacheColorHint = color;
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				getChildAt(i).setDrawingCacheBackgroundColor(color);
			}
			mRecycler.setCacheColorHint(color);

		}
	}

	private void initView() {
		// setFocusable(true);
		setClickable(true);
		setFocusableInTouchMode(true);
		setWillNotDraw(false);
		setAlwaysDrawnWithCacheEnabled(false);
		setScrollingCacheEnabled(true);
		// mGroupFlags |= FLAG_CLIP_CHILDREN;
		// mGroupFlags |= FLAG_CLIP_TO_PADDING;
		// mGroupFlags |= FLAG_ANIMATION_DONE;
		// mGroupFlags |= FLAG_ANIMATION_CACHE;
		// mGroupFlags |= FLAG_ALWAYS_DRAWN_WITH_CACHE;
		//
		// if (getContext().getApplicationInfo().targetSdkVersion >=
		// Build.VERSION_CODES.HONEYCOMB) {
		// mGroupFlags |= FLAG_SPLIT_MOTION_EVENTS;
		// }
	}

	public void setScrollingCacheEnabled(boolean enabled) {
		if (mScrollingCacheEnabled && !enabled) {
			clearScrollingCache();
		}
		mScrollingCacheEnabled = enabled;
	}

	private void clearScrollingCache() {
		if (!isHardwareAccelerated()) {
			if (mClearScrollingCache == null) {
				mClearScrollingCache = new Runnable() {
					public void run() {
						if (mCachingStarted) {
							mCachingStarted = mCachingActive = false;
							setChildrenDrawnWithCacheEnabled(false);
							if ((mPersistentDrawingCache & PERSISTENT_SCROLLING_CACHE) == 0) {
								setChildrenDrawingCacheEnabled(false);
							}
							if (!isAlwaysDrawnWithCacheEnabled()) {
								invalidate();
							}
						}
					}
				};
			}
			post(mClearScrollingCache);
		}
	}

	@Override
	public ListAdapter getAdapter() {
		// TODO Auto-generated method stub
		return mAdapter;
	}

	@Override
	public void requestLayout() {
		if (!mBlockLayoutRequests && !mInLayout) {
			super.requestLayout();
		}
	}

	@Override
	public View getSelectedView() {
		// TODO Auto-generated method stub
		if (mAdapter!= null && mAdapter.getCount() > 0 && mSelectedPosition >= 0) {
			return getChildAt(mSelectedPosition - mFirstPosition);
		} else {
			return null;
		}
	}

	/**
	 * The list is empty. Clear everything out.
	 */
	void resetList() {
		removeAllViewsInLayout();
		mFirstPosition = 0;
		mDataChanged = false;
		mPositionScrollAfterLayout = null;
		mNeedSync = false;
		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;
		setSelectedPositionInt(INVALID_POSITION);
		setNextSelectedPositionInt(INVALID_POSITION);
		mSelectedTop = 0;
		mSelectorPosition = INVALID_POSITION;
		mSelectorRect.setEmpty();
		invalidate();
	}

	/**
	 * Find a position that can be selected (i.e., is not a separator).
	 *
	 * @param position
	 *            The starting position to look at.
	 * @param lookDown
	 *            Whether to look down for other positions.
	 * @return The next selectable position starting at position and then
	 *         searching either up or down. Returns {@link #INVALID_POSITION} if
	 *         nothing can be found.
	 */
	int lookForSelectablePosition(int position, boolean lookDown) {
		return position;
	}

	/**
	 * Utility to keep mSelectedPosition and mSelectedRowId in sync
	 *
	 * @param position
	 *            Our current position
	 */
	public void setSelectedPositionInt(int position) {
		mSelectedPosition = position;
		mSelectedRowId = getItemIdAtPosition(position);
	}

	/**
	 * Utility to keep mNextSelectedPosition and mNextSelectedRowId in sync
	 *
	 * @param position
	 *            Intended value for mSelectedPosition the next time we go
	 *            through layout
	 */
	void setNextSelectedPositionInt(int position) {
		mNextSelectedPosition = position;
		mNextSelectedRowId = getItemIdAtPosition(position);
		// If we are trying to sync to the selection, update that too
		// if (mNeedSync && mSyncMode == SYNC_SELECTED_POSITION && position >=
		// 0) {
		// mSyncPosition = position;
		// mSyncRowId = mNextSelectedRowId;
		// }
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		if (mAdapter != null && mDataSetObserver != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		resetList();
		mRecycler.clear();
		mAdapter = adapter;

		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		// AbsListView#setAdapter will update choice mode states.

		// super.setAdapter(adapter);

		if (mAdapter != null) {
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
			mDataChanged = true;
			checkFocus();

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

			mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());

			int position;
			if (mStackFromBottom) {
				position = lookForSelectablePosition(mItemCount - 1, false);
			} else {
				position = lookForSelectablePosition(0, true);
			}
			setSelectedPositionInt(position);
			setNextSelectedPositionInt(position);
			checkSelectionChanged();
		} else {
			checkFocus();
			// Nothing selected
			checkSelectionChanged();
		}

		requestLayout();
	}

	void checkSelectionChanged() {
		if ((mSelectedPosition != mOldSelectedPosition)
				|| (mSelectedRowId != mOldSelectedRowId)) {
			selectionChanged();
			mOldSelectedPosition = mSelectedPosition;
			mOldSelectedRowId = mSelectedRowId;
		}
	}

	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been selected.
	 *
	 * @param listener
	 *            The callback that will run
	 */
	// public void setOnItemSelectedListener(OnItemSelectedListener listener) {
	// mOnItemSelectedListener = listener;
	// }
	//
	// public final OnItemSelectedListener getOnItemSelectedListener() {
	// return mOnItemSelectedListener;
	// }
	void selectionChanged() {
		mOnItemSelectedListener = getOnItemSelectedListener();
		if (mOnItemSelectedListener != null) {
			if (mInLayout || mBlockLayoutRequests) {
				// If we are in a layout traversal, defer notification
				// by posting. This ensures that the view tree is
				// in a consistent state and is able to accomodate
				// new layout or invalidate requests.
				if (mSelectionNotifier == null) {
					mSelectionNotifier = new SelectionNotifier();
				}
				post(mSelectionNotifier);
			} else {
				fireOnSelected();
				performAccessibilityActionsOnSelected();
			}
		}
	}

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub

		setNextSelectedPositionInt(position);
		mLayoutMode = LAYOUT_SET_SELECTION;
		layoutChildren();
		//requestLayout();
	}

	private void performAccessibilityActionsOnSelected() {

		final int position = getSelectedItemPosition();
		if (position >= 0) {
			// we fire selection events here not in View
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
		}

	}

	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		@Override
		public void onChanged() {
			mDataChanged = true;
			mOldItemCount = mItemCount;
			mItemCount = getAdapter().getCount();

			// Detect the case where a cursor that was previously invalidated
			// has
			// been repopulated with new data.
			if (getAdapter().hasStableIds() && mInstanceState != null
					&& mOldItemCount == 0 && mItemCount > 0) {
				onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			} else {
				rememberSyncState();
			}
			checkFocus();
			requestLayout();
		}

		@Override
		public void onInvalidated() {

			super.onInvalidated();
			// requestLayout();
		}

		public void clearSavedState() {
			mInstanceState = null;
		}
	}

	/**
	 * 1173 * Remember enough information to restore the screen state when the
	 * data has 1174 * changed. 1175 * 1176
	 */
	void rememberSyncState() {
		if (getChildCount() > 0) {
			mNeedSync = true;
			mSyncHeight = mLayoutHeight;
			if (mSelectedPosition >= 0) {
				// Sync the selection state
				View v = getChildAt(mSelectedPosition - mFirstPosition);
				mSyncRowId = mNextSelectedRowId;
				mSyncPosition = mNextSelectedPosition;
				if (v != null) {
					mSpecificTop = v.getTop();
				}
				mSyncMode = SYNC_SELECTED_POSITION;
			} else {
				// Sync the based on the offset of the first view
				View v = getChildAt(0);
				ListAdapter adapter = getAdapter();
				if (mFirstPosition >= 0 && mFirstPosition < adapter.getCount()) {
					mSyncRowId = adapter.getItemId(mFirstPosition);
				} else {
					mSyncRowId = NO_ID;
				}
				mSyncPosition = mFirstPosition;
				if (v != null) {
					mSpecificTop = v.getTop();
				}
				mSyncMode = SYNC_FIRST_POSITION;
			}
		}
	}

	private void checkFocus() {

	}

	/**
	 * The RecycleBin facilitates reuse of views across layouts. The RecycleBin
	 * has two levels of storage: ActiveViews and ScrapViews. ActiveViews are
	 * those views which were onscreen at the start of a layout. By
	 * construction, they are displaying current information. At the end of
	 * layout, all views in ActiveViews are demoted to ScrapViews. ScrapViews
	 * are old views that could potentially be used by the adapter to avoid
	 * allocating views unnecessarily.
	 *
	 * @see AbsListView#setRecyclerListener(RecyclerListener)
	 * @see RecyclerListener
	 */
	class RecycleBin {
		private RecyclerListener mRecyclerListener;

		/**
		 * The position of the first view stored in mActiveViews.
		 */
		private int mFirstActivePosition;

		/**
		 * Views that were on screen at the start of layout. This array is
		 * populated at the start of layout, and at the end of layout all view
		 * in mActiveViews are moved to mScrapViews. Views in mActiveViews
		 * represent a contiguous range of Views, with position of the first
		 * view store in mFirstActivePosition.
		 */
		private View[] mActiveViews = new View[0];

		/**
		 * Unsorted views that can be used by the adapter as a convert view.
		 */
		private ArrayList<View>[] mScrapViews;

		private int mViewTypeCount;

		private ArrayList<View> mCurrentScrap;

		private ArrayList<View> mSkippedScrap;

		private SparseArray<View> mTransientStateViews;

		public void setViewTypeCount(int viewTypeCount) {
			if (viewTypeCount < 1) {
				throw new IllegalArgumentException(
						"Can't have a viewTypeCount < 1");
			}
			// noinspection unchecked
			ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
			for (int i = 0; i < viewTypeCount; i++) {
				scrapViews[i] = new ArrayList<View>();
			}
			mViewTypeCount = viewTypeCount;
			mCurrentScrap = scrapViews[0];
			mScrapViews = scrapViews;
		}

		public void markChildrenDirty() {
			if (mViewTypeCount == 1) {
				final ArrayList<View> scrap = mCurrentScrap;
				final int scrapCount = scrap.size();
				for (int i = 0; i < scrapCount; i++) {
					scrap.get(i).forceLayout();
				}
			} else {
				final int typeCount = mViewTypeCount;
				for (int i = 0; i < typeCount; i++) {
					final ArrayList<View> scrap = mScrapViews[i];
					final int scrapCount = scrap.size();
					for (int j = 0; j < scrapCount; j++) {
						scrap.get(j).forceLayout();
					}
				}
			}
			if (mTransientStateViews != null) {
				final int count = mTransientStateViews.size();
				for (int i = 0; i < count; i++) {
					mTransientStateViews.valueAt(i).forceLayout();
				}
			}
		}

		public boolean shouldRecycleViewType(int viewType) {
			return viewType >= 0;
		}

		/**
		 * Clears the scrap heap.
		 */
		void clear() {
			if (mViewTypeCount == 1) {
				final ArrayList<View> scrap = mCurrentScrap;
				final int scrapCount = scrap.size();
				for (int i = 0; i < scrapCount; i++) {
					removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
				}
			} else {
				final int typeCount = mViewTypeCount;
				for (int i = 0; i < typeCount; i++) {
					final ArrayList<View> scrap = mScrapViews[i];
					final int scrapCount = scrap.size();
					for (int j = 0; j < scrapCount; j++) {
						removeDetachedView(scrap.remove(scrapCount - 1 - j),
								false);
					}
				}
			}
			if (mTransientStateViews != null) {
				mTransientStateViews.clear();
			}
		}

		/**
		 * Fill ActiveViews with all of the children of the AbsListView.
		 *
		 * @param childCount
		 *            The minimum number of views mActiveViews should hold
		 * @param firstActivePosition
		 *            The position of the first view that will be stored in
		 *            mActiveViews
		 */
		void fillActiveViews(int childCount, int firstActivePosition) {
			if (mActiveViews.length < childCount) {
				mActiveViews = new View[childCount];
			}
			mFirstActivePosition = firstActivePosition;

			final View[] activeViews = mActiveViews;
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				ZGridView.LayoutParams lp = (ZGridView.LayoutParams) child
						.getLayoutParams();
				// Don't put header or footer views into the scrap heap
				if (lp != null
						&& lp.viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
					// Note: We do place AdapterView.ITEM_VIEW_TYPE_IGNORE in
					// active views.
					// However, we will NOT place them into scrap views.
					activeViews[i] = child;
				}
			}
		}

		/**
		 * Get the view corresponding to the specified position. The view will
		 * be removed from mActiveViews if it is found.
		 *
		 * @param position
		 *            The position to look up in mActiveViews
		 * @return The view if it is found, null otherwise
		 */
		View getActiveView(int position) {
			int index = position - mFirstActivePosition;
			final View[] activeViews = mActiveViews;
			if (index >= 0 && index < activeViews.length) {
				final View match = activeViews[index];
				activeViews[index] = null;
				return match;
			}
			return null;
		}

		View getTransientStateView(int position) {
			if (mTransientStateViews == null) {
				return null;
			}
			final int index = mTransientStateViews.indexOfKey(position);
			if (index < 0) {
				return null;
			}
			final View result = mTransientStateViews.valueAt(index);
			mTransientStateViews.removeAt(index);
			return result;
		}

		/**
		 * Dump any currently saved views with transient state.
		 */
		void clearTransientStateViews() {
			if (mTransientStateViews != null) {
				mTransientStateViews.clear();
			}
		}

		/**
		 * @return A view from the ScrapViews collection. These are unordered.
		 */
		View getScrapView(int position) {
			if (mViewTypeCount == 1) {
				return retrieveFromScrap(mCurrentScrap, position);
			} else {
				int whichScrap = mAdapter.getItemViewType(position);
				if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
					return retrieveFromScrap(mScrapViews[whichScrap], position);
				}
			}
			return null;
		}

		/**
		 * Put a view into the ScrapViews list. These views are unordered.
		 *
		 * @param scrap
		 *            The view to add
		 */
		void addScrapView(View scrap, int position) {
			ZGridView.LayoutParams lp = (ZGridView.LayoutParams) scrap
					.getLayoutParams();
			if (lp == null) {
				return;
			}

			// Don't put header or footer views or views that should be ignored
			// into the scrap heap
			int viewType = lp.viewType;
			// final boolean scrapHasTransientState = scrap.hasTransientState();
			if (!shouldRecycleViewType(viewType)) {
				if (viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
					if (mSkippedScrap == null) {
						mSkippedScrap = new ArrayList<View>();
					}
					mSkippedScrap.add(scrap);
				}
				// if (scrapHasTransientState) {
				// if (mTransientStateViews == null) {
				// mTransientStateViews = new SparseArray<View>();
				// }
				// dispatchStartTemporaryDetach(scrap);
				// mTransientStateViews.put(position, scrap);
				// }
				return;
			}

			dispatchStartTemporaryDetach(scrap);
			if (mViewTypeCount == 1) {
				mCurrentScrap.add(scrap);
			} else {
				mScrapViews[viewType].add(scrap);
			}

			scrap.setAccessibilityDelegate(null);
			if (mRecyclerListener != null) {
				mRecyclerListener.onMovedToScrapHeap(scrap);
			}
		}

		/**
		 * Finish the removal of any views that skipped the scrap heap.
		 */
		void removeSkippedScrap() {
			if (mSkippedScrap == null) {
				return;
			}
			final int count = mSkippedScrap.size();
			for (int i = 0; i < count; i++) {
				removeDetachedView(mSkippedScrap.get(i), false);
			}
			mSkippedScrap.clear();
		}

		/**
		 * Move all views remaining in mActiveViews to mScrapViews.
		 */
		void scrapActiveViews() {
			final View[] activeViews = mActiveViews;
			final boolean hasListener = mRecyclerListener != null;
			final boolean multipleScraps = mViewTypeCount > 1;

			ArrayList<View> scrapViews = mCurrentScrap;
			final int count = activeViews.length;
			for (int i = count - 1; i >= 0; i--) {
				final View victim = activeViews[i];
				if (victim != null) {
					final ZGridView.LayoutParams lp = (ZGridView.LayoutParams) victim
							.getLayoutParams();
					int whichScrap = lp.viewType;

					activeViews[i] = null;

					// final boolean scrapHasTransientState = victim
					// .hasTransientState();
					if (!shouldRecycleViewType(whichScrap)) {
						// Do not move views that should be ignored
						if (whichScrap != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
							removeDetachedView(victim, false);
						}
						// if (scrapHasTransientState) {
						// if (mTransientStateViews == null) {
						// mTransientStateViews = new SparseArray<View>();
						// }
						// mTransientStateViews.put(mFirstActivePosition + i,
						// victim);
						// }
						continue;
					}

					if (multipleScraps) {
						scrapViews = mScrapViews[whichScrap];
					}
					dispatchStartTemporaryDetach(victim);
					lp.scrappedFromPosition = mFirstActivePosition + i;
					scrapViews.add(victim);

					victim.setAccessibilityDelegate(null);
					if (hasListener) {
						mRecyclerListener.onMovedToScrapHeap(victim);
					}
				}
			}

			pruneScrapViews();
		}

		public void dispatchStartTemporaryDetach(View child) {
			child.onStartTemporaryDetach();
			if (child instanceof ViewGroup) {
				final int count = ((ViewGroup) child).getChildCount();
				for (int i = 0; i < count; i++) {
					((ViewGroup) child).getChildAt(i).onStartTemporaryDetach();
				}
			}

		}

		/**
		 * Makes sure that the size of mScrapViews does not exceed the size of
		 * mActiveViews. (This can happen if an adapter does not recycle its
		 * views).
		 */
		private void pruneScrapViews() {
			final int maxViews = mActiveViews.length;
			final int viewTypeCount = mViewTypeCount;
			final ArrayList<View>[] scrapViews = mScrapViews;
			for (int i = 0; i < viewTypeCount; ++i) {
				final ArrayList<View> scrapPile = scrapViews[i];
				int size = scrapPile.size();
				final int extras = size - maxViews;
				size--;
				for (int j = 0; j < extras; j++) {
					removeDetachedView(scrapPile.remove(size--), false);
				}
			}
			//
			// if (mTransientStateViews != null) {
			// for (int i = 0; i < mTransientStateViews.size(); i++) {
			// final View v = mTransientStateViews.valueAt(i);
			// if (!v.hasTransientState()) {
			// mTransientStateViews.removeAt(i);
			// i--;
			// }
			// }
			// }
		}

		/**
		 * Puts all views in the scrap heap into the supplied list.
		 */
		void reclaimScrapViews(List<View> views) {
			if (mViewTypeCount == 1) {
				views.addAll(mCurrentScrap);
			} else {
				final int viewTypeCount = mViewTypeCount;
				final ArrayList<View>[] scrapViews = mScrapViews;
				for (int i = 0; i < viewTypeCount; ++i) {
					final ArrayList<View> scrapPile = scrapViews[i];
					views.addAll(scrapPile);
				}
			}
		}

		/**
		 * Updates the cache color hint of all known views.
		 *
		 * @param color
		 *            The new cache color hint.
		 */
		void setCacheColorHint(int color) {
			if (mViewTypeCount == 1) {
				final ArrayList<View> scrap = mCurrentScrap;
				final int scrapCount = scrap.size();
				for (int i = 0; i < scrapCount; i++) {
					scrap.get(i).setDrawingCacheBackgroundColor(color);
				}
			} else {
				final int typeCount = mViewTypeCount;
				for (int i = 0; i < typeCount; i++) {
					final ArrayList<View> scrap = mScrapViews[i];
					final int scrapCount = scrap.size();
					for (int j = 0; j < scrapCount; j++) {
						scrap.get(j).setDrawingCacheBackgroundColor(color);
					}
				}
			}
			// Just in case this is called during a layout pass
			final View[] activeViews = mActiveViews;
			final int count = activeViews.length;
			for (int i = 0; i < count; ++i) {
				final View victim = activeViews[i];
				if (victim != null) {
					victim.setDrawingCacheBackgroundColor(color);
				}
			}
		}

	}

	private class SelectionNotifier implements Runnable {
		public void run() {
			if (mDataChanged) {
				// Data has changed between when this SelectionNotifier
				// was posted and now. We need to wait until the AdapterView
				// has been synched to the new data.
				if (getAdapter() != null) {
					post(this);
				}
			} else {
				fireOnSelected();
				performAccessibilityActionsOnSelected();
			}
		}
	}

	private void fireOnSelected() {

		mOnItemSelectedListener = getOnItemSelectedListener();
		if (mOnItemSelectedListener == null) {
			return;
		}
		final int selection = mNextSelectedPosition;
		if (selection >= 0) {
			View v = getSelectedView();
			mOnItemSelectedListener.onItemSelected(this, v, selection,
					getAdapter().getItemId(selection));
		} else {
			mOnItemSelectedListener.onNothingSelected(this);
		}
	}

	static View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
		int size = scrapViews.size();
		if (size > 0) {
			// See if we still have a view for this position.
			for (int i = 0; i < size; i++) {
				View view = scrapViews.get(i);
				if (((ZGridView.LayoutParams) view.getLayoutParams()).scrappedFromPosition == position) {
					scrapViews.remove(i);
					return view;
				}
			}
			return scrapViews.remove(size - 1);
		} else {
			return null;
		}
	}

	/**
	 * AbsListView extends LayoutParams to provide a place to hold the view
	 * type.
	 */
	public static class LayoutParams extends ViewGroup.LayoutParams {
		/**
		 * View type for this view, as returned by
		 * {@link Adapter#getItemViewType(int) }
		 */
		@ViewDebug.ExportedProperty(category = "list", mapping = {
				@ViewDebug.IntToString(from = ITEM_VIEW_TYPE_IGNORE, to = "ITEM_VIEW_TYPE_IGNORE"),
				@ViewDebug.IntToString(from = ITEM_VIEW_TYPE_HEADER_OR_FOOTER, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER") })
		int viewType;

		/**
		 * When this boolean is set, the view has been added to the AbsListView
		 * at least once. It is used to know whether headers/footers have
		 * already been added to the list view and whether they should be
		 * treated as recycled views or not.
		 */
		@ViewDebug.ExportedProperty(category = "list")
		boolean recycledHeaderFooter;

		/**
		 * When an AbsListView is measured with an AT_MOST measure spec, it
		 * needs to obtain children views to measure itself. When doing so, the
		 * children are not attached to the window, but put in the recycler
		 * which assumes they've been attached before. Setting this flag will
		 * force the reused view to be attached to the window rather than just
		 * attached to the parent.
		 */
		@ViewDebug.ExportedProperty(category = "list")
		boolean forceAdd;

		/**
		 * The position the view was removed from when pulled out of the scrap
		 * heap.
		 *
		 * @hide
		 */
		int scrappedFromPosition;

		/**
		 * The ID the view represents
		 */
		long itemId = -1;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(int w, int h, int viewType) {
			super(w, h);
			this.viewType = viewType;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new ZGridView.LayoutParams(getContext(), attrs);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof ZGridView.LayoutParams;
	}

	protected void keyPressed() {
		// We will support longClick in the future.
		if (!isEnabled() || !isClickable()) {
			return;
		}
		if (mSelector != null && isFocused() && mSelectorRect != null
				&& !mSelectorRect.isEmpty()) {
			final View v = getChildAt(mSelectedPosition - mFirstPosition);
			if (v != null) {
				if (v.hasFocusable())
					return;
				v.setPressed(true);
			}
			setPressed(true);
		}
	}

	private boolean commonKey(int keyCode, int count, KeyEvent event) {
		if (mAdapter == null) {
			return false;
		}

		if (mDataChanged) {
			layoutChildren();
		}

		hover = false;
		boolean handled = false;
		int action = event.getAction();

		if (action != KeyEvent.ACTION_UP) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| arrowScroll(FOCUS_LEFT);
				}
				break;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| arrowScroll(FOCUS_RIGHT);
				}
				break;

			case KeyEvent.KEYCODE_DPAD_UP:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| arrowScroll(FOCUS_UP);
				} else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
					handled = resurrectSelectionIfNeeded()
							|| fullScroll(FOCUS_UP);
				}
				break;

			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| arrowScroll(FOCUS_DOWN);
				} else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
					handled = resurrectSelectionIfNeeded()
							|| fullScroll(FOCUS_DOWN);
				}
				break;

			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded();
					if (!handled && event.getRepeatCount() == 0
							&& getChildCount() > 0) {
						keyPressed();
						handled = true;
					}
				}
				break;

			case KeyEvent.KEYCODE_PAGE_UP:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| pageScroll(FOCUS_UP);
				} else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
					handled = resurrectSelectionIfNeeded()
							|| fullScroll(FOCUS_UP);
				}
				break;

			case KeyEvent.KEYCODE_PAGE_DOWN:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| pageScroll(FOCUS_DOWN);
				} else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
					handled = resurrectSelectionIfNeeded()
							|| fullScroll(FOCUS_DOWN);
				}
				break;

			case KeyEvent.KEYCODE_MOVE_HOME:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| fullScroll(FOCUS_UP);
				}
				break;

			case KeyEvent.KEYCODE_MOVE_END:
				if (event.hasNoModifiers()) {
					handled = resurrectSelectionIfNeeded()
							|| fullScroll(FOCUS_DOWN);
				}
				break;

			case KeyEvent.KEYCODE_TAB:
				// XXX Sometimes it is useful to be able to TAB through the
				// items in
				// a GridView sequentially. Unfortunately this can create an
				// asymmetry in TAB navigation order unless the list selection
				// always reverts to the top or bottom when receiving TAB focus
				// from
				// another widget. Leaving this behavior disabled for now but
				// perhaps it should be configurable (and more comprehensive).

				break;
			}
		}

		if (action == KeyEvent.ACTION_UP) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (!isEnabled()) {
					return true;
				}
				if (isClickable() && isPressed() && mSelectedPosition >= 0
						&& mAdapter != null
						&& mSelectedPosition < mAdapter.getCount()) {
					final View v = getChildAt(mSelectedPosition
							- mFirstPosition);
					if (v != null) {
						performItemClick(v, mSelectedPosition, 0);
						v.setPressed(false);
					}
					setPressed(false);
					return true;
				}
				break;
			}
		}
		if (handled) {
			return true;
		}

		// if (sendToTextFilter(keyCode, count, event)) {
		// return true;
		// }

		switch (action) {
		case KeyEvent.ACTION_DOWN:
			return super.onKeyDown(keyCode, event);
		case KeyEvent.ACTION_UP:
			return super.onKeyUp(keyCode, event);
		case KeyEvent.ACTION_MULTIPLE:
			return super.onKeyMultiple(keyCode, count, event);
		default:
			return false;
		}
	}

	private Rect mTouchFrame;

	public int pointToPosition(int x, int y) {
		Rect frame = mTouchFrame;
		if (frame == null) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}

		final int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(frame);
				if (frame.contains(x, y)) {
					return mFirstPosition + i;
				}
			}
		}
		return INVALID_POSITION;
	}

	private float mLastMotionX;
	private float mLastMotionY;
	private int TOUCH_STATE = -1;

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// // TODO Auto-generated method stub
	// final float x1 = event.getX();
	// final float y1 = event.getY();
	// switch (event.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// mLastMotionX = event.getX();
	// mLastMotionY = event.getY();
	// break;
	// case MotionEvent.ACTION_UP:
	// int deltaX = (int) (mLastMotionX - x1);
	// int deltaY = (int) (mLastMotionY - y1);
	// mLastMotionX = x1;
	// mLastMotionY = y1;
	// if (deltaX > 50) {
	// TOUCH_STATE = FOCUS_RIGHT;
	// }
	// if (deltaX < -50) {
	// TOUCH_STATE = FOCUS_LEFT;
	// }
	// if(deltaY > 50){
	// TOUCH_STATE = FOCUS_DOWN;
	// pageScroll(FOCUS_DOWN);
	// }
	// if(deltaY < -50){
	// TOUCH_STATE = FOCUS_UP;
	// pageScroll(FOCUS_UP);
	// }
	// if (TOUCH_STATE != FOCUS_LEFT && TOUCH_STATE != FOCUS_RIGHT &&
	// TOUCH_STATE != FOCUS_DOWN && TOUCH_STATE!=FOCUS_UP) {
	// if(Math.abs(deltaX)<50&&Math.abs(deltaY)<50){
	// final int x = (int) event.getX();
	// final int y = (int) event.getY();
	// int motionPosition = pointToPosition(x, y);
	// if(motionPosition!=INVALID_POSITION){
	// final View v = getChildAt(motionPosition - mFirstPosition);
	// performItemClick(v, motionPosition - mFirstPosition, 0);
	// }
	// }
	//
	// }
	// TOUCH_STATE = -1;
	// break;
	// default:
	// break;
	// }
	// return true;
	// }
	private void initOrResetVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTrackerIfNotExists() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	final class CheckForTap implements Runnable {
		public void run() {
			if (mTouchMode == TOUCH_MODE_DOWN) {
				mTouchMode = TOUCH_MODE_TAP;
				final View child = getChildAt(mMotionPosition - mFirstPosition);
				if (child != null && !child.hasFocusable()) {
					mLayoutMode = LAYOUT_NORMAL;

					if (!mDataChanged) {
						child.setPressed(true);
						setPressed(true);
						layoutChildren();
						positionSelector(mMotionPosition, child);
						refreshDrawableState();

						final int longPressTimeout = ViewConfiguration
								.getLongPressTimeout();
						final boolean longClickable = isLongClickable();

						if (mSelector != null) {
							Drawable d = mSelector.getCurrent();
							if (d != null && d instanceof TransitionDrawable) {
								if (longClickable) {
									((TransitionDrawable) d)
											.startTransition(longPressTimeout);
								} else {
									((TransitionDrawable) d).resetTransition();
								}
							}
						}

						mTouchMode = TOUCH_MODE_DONE_WAITING;

					} else {
						mTouchMode = TOUCH_MODE_DONE_WAITING;
					}
				}
			}
		}
	}

	private void createScrollingCache() {
		if (mScrollingCacheEnabled && !mCachingStarted
				&& !isHardwareAccelerated()) {
			setChildrenDrawnWithCacheEnabled(true);
			setChildrenDrawingCacheEnabled(true);
			mCachingStarted = mCachingActive = true;
		}
	}

	protected boolean performButtonActionOnTouchDown(MotionEvent event) {
		if ((event.getButtonState() & MotionEvent.BUTTON_SECONDARY) != 0) {
			if (showContextMenu()) {
				return true;
			}
		}
		return false;
	}

	private boolean hover = false;

	@Override
	protected boolean dispatchHoverEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(onhoverlistener != null){
			onhoverlistener.onHover(this, event);
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:
//			hover = true;
//			break;
		case MotionEvent.ACTION_HOVER_MOVE:
			int position1 = pointToPosition((int) event.getX(),
					(int) event.getY());
			if (position1 >= 0) {
				hover = true;
				if (getId() == R.id.drama_zgridview
						&& getChildAt(position1 - mFirstPosition) != null) {
					getChildAt(position1 - mFirstPosition).findViewById(R.id.btn_count)
							.requestFocusFromTouch();

				} else {
					setFocusable(true);
					setFocusableInTouchMode(true);
					setSelection(position1);
					requestFocusFromTouch();
				}
			}else{
				hover = false;
				mSelectorRect.setEmpty();
				invalidate();
			}
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
//			clearFocus();
			hover = false;
			break;
		}
		return false;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mAdapter != null && mDataSetObserver == null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

			// Data may have changed while we were detached. Refresh.
			mDataChanged = true;
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
		}
		mIsAttached = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mRecycler.clear();

		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
			mDataSetObserver = null;
		}

		if (mFlingRunnable != null) {
			removeCallbacks(mFlingRunnable);
		}

		if (mPositionScroller != null) {
			mPositionScroller.stop();
		}

		if (mClearScrollingCache != null) {
			removeCallbacks(mClearScrollingCache);
		}

		if (mPerformClick != null) {
			removeCallbacks(mPerformClick);
		}

		if (mTouchModeReset != null) {
			removeCallbacks(mTouchModeReset);
			mTouchModeReset = null;
		}
		mIsAttached = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!isEnabled()) {
			return isClickable() || isLongClickable();
		}

		if (mPositionScroller != null) {
			mPositionScroller.stop();
		}

		final int action = ev.getAction();

		View v;

		initVelocityTrackerIfNotExists();
		mVelocityTracker.addMovement(ev);

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			switch (mTouchMode) {
			case TOUCH_MODE_OVERFLING: {
				mFlingRunnable.endFling();
				if (mPositionScroller != null) {
					mPositionScroller.stop();
				}
				mTouchMode = TOUCH_MODE_OVERSCROLL;
				mMotionX = (int) ev.getX();
				mMotionY = mLastY = (int) ev.getY();
				mMotionCorrection = 0;
				mActivePointerId = ev.getPointerId(0);
				mDirection = 0;
				break;
			}

			default: {
				mActivePointerId = ev.getPointerId(0);
				final int x = (int) ev.getX();
				final int y = (int) ev.getY();
				int motionPosition = pointToPosition(x, y);
				if (!mDataChanged) {
					if ((mTouchMode != TOUCH_MODE_FLING)
							&& (motionPosition >= 0)
							&& (getAdapter().isEnabled(motionPosition))) {
						mTouchMode = TOUCH_MODE_DOWN;
						if (mPendingCheckForTap == null) {
							mPendingCheckForTap = new CheckForTap();

						}
						postDelayed(mPendingCheckForTap,
								ViewConfiguration.getTapTimeout());
					} else {
						if (mTouchMode == TOUCH_MODE_FLING) {

							createScrollingCache();
							mTouchMode = TOUCH_MODE_SCROLL;
							mMotionCorrection = 0;
							motionPosition = findMotionRow(y);
							mFlingRunnable.flywheelTouch();
						}
					}
				}

				if (motionPosition >= 0) {
					v = getChildAt(motionPosition - mFirstPosition);
					mMotionViewOriginalTop = v.getTop();
				}
				mMotionX = x;
				mMotionY = y;
				mMotionPosition = motionPosition;
				mLastY = Integer.MIN_VALUE;
				break;
			}
			}

			if (performButtonActionOnTouchDown(ev)) {
				if (mTouchMode == TOUCH_MODE_DOWN) {
					removeCallbacks(mPendingCheckForTap);
				}
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			int pointerIndex = ev.findPointerIndex(mActivePointerId);
			if (pointerIndex == -1) {
				pointerIndex = 0;
				mActivePointerId = ev.getPointerId(pointerIndex);
			}
			final int y = (int) ev.getY(pointerIndex);

			if (mDataChanged) {
				layoutChildren();
			}

			switch (mTouchMode) {
			case TOUCH_MODE_DOWN:
			case TOUCH_MODE_TAP:
			case TOUCH_MODE_DONE_WAITING:
				startScrollIfNeeded(y);
				break;
			case TOUCH_MODE_SCROLL:
			case TOUCH_MODE_OVERSCROLL:
				scrollIfNeeded(y);
				break;
			}
			break;
		}

		case MotionEvent.ACTION_UP: {
			switch (mTouchMode) {
			case TOUCH_MODE_DOWN:
			case TOUCH_MODE_TAP:
			case TOUCH_MODE_DONE_WAITING:
				final int motionPosition = mMotionPosition;
				final View child = getChildAt(motionPosition - mFirstPosition);

				final float x = ev.getX();
				final boolean inList = x > mListPadding.left
						&& x < getWidth() - mListPadding.right;

				if (child != null && !child.hasFocusable() && inList) {
					if (mTouchMode != TOUCH_MODE_DOWN) {
						child.setPressed(false);
					}

					if (mPerformClick == null) {
						mPerformClick = new PerformClick();
					}

					final ZGridView.PerformClick performClick = mPerformClick;
					performClick.mClickMotionPosition = motionPosition;
					performClick.rememberWindowAttachCount();

					mResurrectToPosition = motionPosition;

					if (mTouchMode == TOUCH_MODE_DOWN
							|| mTouchMode == TOUCH_MODE_TAP) {
						final Handler handler = getHandler();
						if (handler != null) {
							if (mTouchMode == TOUCH_MODE_DOWN) {
								handler.removeCallbacks(mPendingCheckForTap);
							}

						}
						mLayoutMode = LAYOUT_NORMAL;
						if (!mDataChanged && mAdapter.isEnabled(motionPosition)) {
							mTouchMode = TOUCH_MODE_TAP;
							setSelectedPositionInt(mMotionPosition);
							layoutChildren();
							child.setPressed(true);
							positionSelector(mMotionPosition, child);
							setPressed(true);
							if (mSelector != null) {
								Drawable d = mSelector.getCurrent();
								if (d != null
										&& d instanceof TransitionDrawable) {
									((TransitionDrawable) d).resetTransition();
								}
							}
							if (mTouchModeReset != null) {
								removeCallbacks(mTouchModeReset);
							}
							mTouchModeReset = new Runnable() {
								@Override
								public void run() {
									mTouchMode = TOUCH_MODE_REST;
									child.setPressed(false);
									setPressed(false);
									if (!mDataChanged) {
										performClick.run();
									}
								}
							};
							postDelayed(mTouchModeReset,
									ViewConfiguration.getPressedStateDuration());
						} else {
							mTouchMode = TOUCH_MODE_REST;
							updateSelectorState();
						}
						return true;
					} else if (!mDataChanged
							&& mAdapter.isEnabled(motionPosition)) {
						performClick.run();
					}
				}
				mTouchMode = TOUCH_MODE_REST;
				updateSelectorState();
				break;
			case TOUCH_MODE_SCROLL:
				final int childCount = getChildCount();
				if (childCount > 0) {
					final int firstChildTop = getChildAt(0).getTop();
					final int lastChildBottom = getChildAt(childCount - 1)
							.getBottom();
					final int contentTop = mListPadding.top;
					final int contentBottom = getHeight() - mListPadding.bottom;
					if (mFirstPosition == 0 && firstChildTop >= contentTop
							&& mFirstPosition + childCount < mItemCount
							&& lastChildBottom <= getHeight() - contentBottom) {
						mTouchMode = TOUCH_MODE_REST;
						reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
					} else {
						final VelocityTracker velocityTracker = mVelocityTracker;
						velocityTracker.computeCurrentVelocity(1000,
								mMaximumVelocity);

						final int initialVelocity = (int) (velocityTracker
								.getYVelocity(mActivePointerId) * mVelocityScale);
						if (Math.abs(initialVelocity) > mMinimumVelocity
								&& !((mFirstPosition == 0 && firstChildTop == contentTop
										- mOverscrollDistance) || (mFirstPosition
										+ childCount == mItemCount && lastChildBottom == contentBottom
										+ mOverscrollDistance))) {
							if (mFlingRunnable == null) {
								mFlingRunnable = new FlingRunnable();
							}
							reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);

							mFlingRunnable.start(-initialVelocity);
						} else {
							mTouchMode = TOUCH_MODE_REST;
							reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
							if (mFlingRunnable != null) {
								mFlingRunnable.endFling();
							}
							if (mPositionScroller != null) {
								mPositionScroller.stop();
							}
						}
					}
				} else {
					mTouchMode = TOUCH_MODE_REST;
					reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
				}
				break;

			case TOUCH_MODE_OVERSCROLL:
				if (mFlingRunnable == null) {
					mFlingRunnable = new FlingRunnable();
				}
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				final int initialVelocity = (int) velocityTracker
						.getYVelocity(mActivePointerId);

				reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
				if (Math.abs(initialVelocity) > mMinimumVelocity) {
					mFlingRunnable.startOverfling(-initialVelocity);
				} else {
					mFlingRunnable.startSpringback();
				}

				break;
			}

			setPressed(false);

			if (mEdgeGlowTop != null) {
				mEdgeGlowTop.onRelease();
				mEdgeGlowBottom.onRelease();
			}
			invalidate();
			recycleVelocityTracker();

			mActivePointerId = INVALID_POINTER;

			if (PROFILE_SCROLLING) {
				if (mScrollProfilingStarted) {
					Debug.stopMethodTracing();
					mScrollProfilingStarted = false;
				}
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
			switch (mTouchMode) {
			case TOUCH_MODE_OVERSCROLL:
				if (mFlingRunnable == null) {
					mFlingRunnable = new FlingRunnable();
				}
				mFlingRunnable.startSpringback();
				break;
			case TOUCH_MODE_OVERFLING:
				break;

			default:
				mTouchMode = TOUCH_MODE_REST;
				setPressed(false);
				View motionView = this.getChildAt(mMotionPosition
						- mFirstPosition);
				if (motionView != null) {
					motionView.setPressed(false);
				}
				clearScrollingCache();
				recycleVelocityTracker();
			}

			if (mEdgeGlowTop != null) {
				mEdgeGlowTop.onRelease();
				mEdgeGlowBottom.onRelease();
			}
			mActivePointerId = INVALID_POINTER;
			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {
			onSecondaryPointerUp(ev);
			final int x = mMotionX;
			final int y = mMotionY;
			final int motionPosition = pointToPosition(x, y);
			if (motionPosition >= 0) {
				v = getChildAt(motionPosition - mFirstPosition);
				mMotionViewOriginalTop = v.getTop();
				mMotionPosition = motionPosition;
			}
			mLastY = y;
			break;
		}

		case MotionEvent.ACTION_POINTER_DOWN: {
			final int index = ev.getActionIndex();
			final int id = ev.getPointerId(index);
			final int x = (int) ev.getX(index);
			final int y = (int) ev.getY(index);
			mMotionCorrection = 0;
			mActivePointerId = id;
			mMotionX = x;
			mMotionY = y;
			final int motionPosition = pointToPosition(x, y);
			if (motionPosition >= 0) {
				v = getChildAt(motionPosition - mFirstPosition);
				mMotionViewOriginalTop = v.getTop();
				mMotionPosition = motionPosition;
			}
			mLastY = y;
			break;
		}
		}

		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		View v;

		if (mPositionScroller != null) {
			mPositionScroller.stop();
		}

		if (!mIsAttached) {
			// Something isn't right.
			// Since we rely on being attached to get data set change
			// notifications,
			// don't risk doing anything where we might try to resync and find
			// things
			// in a bogus state.
			return false;
		}
		//
		// if (mFastScroller != null) {
		// boolean intercepted = mFastScroller.onInterceptTouchEvent(ev);
		// if (intercepted) {
		// return true;
		// }
		// }

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			int touchMode = mTouchMode;
			if (touchMode == TOUCH_MODE_OVERFLING
					|| touchMode == TOUCH_MODE_OVERSCROLL) {
				mMotionCorrection = 0;
				return true;
			}

			final int x = (int) ev.getX();
			final int y = (int) ev.getY();
			mActivePointerId = ev.getPointerId(0);

			int motionPosition = findMotionRow(y);
			if (touchMode != TOUCH_MODE_FLING && motionPosition >= 0) {
				// User clicked on an actual view (and was not stopping a
				// fling).
				// Remember where the motion event started
				v = getChildAt(motionPosition - mFirstPosition);
				mMotionViewOriginalTop = v.getTop();
				mMotionX = x;
				mMotionY = y;
				mMotionPosition = motionPosition;
				mTouchMode = TOUCH_MODE_DOWN;
				clearScrollingCache();
			}
			mLastY = Integer.MIN_VALUE;
			initOrResetVelocityTracker();
			mVelocityTracker.addMovement(ev);
			if (touchMode == TOUCH_MODE_FLING) {
				return true;
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			switch (mTouchMode) {
			case TOUCH_MODE_DOWN:
				int pointerIndex = ev.findPointerIndex(mActivePointerId);
				if (pointerIndex == -1) {
					pointerIndex = 0;
					mActivePointerId = ev.getPointerId(pointerIndex);
				}
				final int y = (int) ev.getY(pointerIndex);
				initVelocityTrackerIfNotExists();
				mVelocityTracker.addMovement(ev);
				if (startScrollIfNeeded(y)) {
					return true;
				}
				break;
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			mTouchMode = TOUCH_MODE_REST;
			mActivePointerId = INVALID_POINTER;
			recycleVelocityTracker();
			reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {
			onSecondaryPointerUp(ev);
			break;
		}
		}

		return false;
	}

	private boolean startScrollIfNeeded(int y) {
		// Check if we have moved far enough that it looks more like a
		// scroll than a tap
		final int deltaY = y - mMotionY;
		final int distance = Math.abs(deltaY);
		final boolean overscroll = getScrollY() != 0;
		if (overscroll || distance > mTouchSlop) {
			createScrollingCache();
			if (overscroll) {
				mTouchMode = TOUCH_MODE_OVERSCROLL;
				mMotionCorrection = 0;
			} else {
				mTouchMode = TOUCH_MODE_SCROLL;
				mMotionCorrection = deltaY > 0 ? mTouchSlop : -mTouchSlop;
			}
			// final Handler handler = getHandler();
			// // Handler should not be null unless the AbsListView is not
			// attached to a
			// // window, which would make it very hard to scroll it... but the
			// monkeys
			// // say it's possible.
			// if (handler != null) {
			// handler.removeCallbacks(mPendingCheckForLongPress);
			// }
			setPressed(false);
			View motionView = getChildAt(mMotionPosition - mFirstPosition);
			if (motionView != null) {
				motionView.setPressed(false);
			}
			reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			// Time to start stealing events! Once we've stolen them, don't let
			// anyone
			// steal from us
			final ViewParent parent = getParent();
			if (parent != null) {
				parent.requestDisallowInterceptTouchEvent(true);
			}
			scrollIfNeeded(y);
			return true;
		}

		return false;
	}

	private void scrollIfNeeded(int y) {
		final int rawDeltaY = y - mMotionY;
		final int deltaY = rawDeltaY - mMotionCorrection;
		int incrementalDeltaY = mLastY != Integer.MIN_VALUE ? y - mLastY
				: deltaY;

		if (mTouchMode == TOUCH_MODE_SCROLL) {
			if (PROFILE_SCROLLING) {
				if (!mScrollProfilingStarted) {
					Debug.startMethodTracing("AbsListViewScroll");
					mScrollProfilingStarted = true;
				}
			}

			// if (mScrollStrictSpan == null) {
			// // If it's non-null, we're already in a scroll.
			// mScrollStrictSpan =
			// StrictMode.enterCriticalSpan("AbsListView-scroll");
			// }

			if (y != mLastY) {
				// We may be here after stopping a fling and continuing to
				// scroll.
				// If so, we haven't disallowed intercepting touch events yet.
				// Make sure that we do so in case we're in a parent that can
				// intercept.
				if (Math.abs(rawDeltaY) > mTouchSlop) {
					final ViewParent parent = getParent();
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
				}

				final int motionIndex;
				if (mMotionPosition >= 0) {
					motionIndex = mMotionPosition - mFirstPosition;
				} else {
					// If we don't have a motion position that we can reliably
					// track,
					// pick something in the middle to make a best guess at
					// things below.
					motionIndex = getChildCount() / 2;
				}

				int motionViewPrevTop = 0;
				View motionView = this.getChildAt(motionIndex);
				if (motionView != null) {
					motionViewPrevTop = motionView.getTop();
				}

				// No need to do all this work if we're not going to move anyway
				boolean atEdge = false;
				if (incrementalDeltaY != 0) {
					atEdge = trackMotionScroll(deltaY, incrementalDeltaY);
				}

				// Check to see if we have bumped into the scroll limit
				motionView = this.getChildAt(motionIndex);
				if (motionView != null) {
					// Check if the top of the motion view is where it is
					// supposed to be
					final int motionViewRealTop = motionView.getTop();
					if (atEdge) {
						// Apply overscroll

						int overscroll = -incrementalDeltaY
								- (motionViewRealTop - motionViewPrevTop);
						overScrollBy(0, overscroll, 0, getScrollY(), 0, 0, 0,
								mOverscrollDistance, true);
						if (Math.abs(mOverscrollDistance) == Math
								.abs(getScrollY())) {
							// Don't allow overfling if we're at the edge.
							if (mVelocityTracker != null) {
								mVelocityTracker.clear();
							}
						}

						final int overscrollMode = getOverScrollMode();
						if (overscrollMode == OVER_SCROLL_ALWAYS
								|| (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits())) {
							mDirection = 0; // Reset when entering overscroll.
							mTouchMode = TOUCH_MODE_OVERSCROLL;
							if (rawDeltaY > 0) {
								mEdgeGlowTop.onPull((float) overscroll
										/ getHeight());
								if (!mEdgeGlowBottom.isFinished()) {
									mEdgeGlowBottom.onRelease();
								}
								// invalidate(mEdgeGlowTop.getBounds(false));
							} else if (rawDeltaY < 0) {
								mEdgeGlowBottom.onPull((float) overscroll
										/ getHeight());
								if (!mEdgeGlowTop.isFinished()) {
									mEdgeGlowTop.onRelease();
								}
								// invalidate(mEdgeGlowBottom.getBounds(true));
							}
						}
					}
					mMotionY = y;
				}
				mLastY = y;
			}
		} else if (mTouchMode == TOUCH_MODE_OVERSCROLL) {
			if (y != mLastY) {
				final int oldScroll = getScrollY();
				final int newScroll = oldScroll - incrementalDeltaY;
				int newDirection = y > mLastY ? 1 : -1;

				if (mDirection == 0) {
					mDirection = newDirection;
				}

				int overScrollDistance = -incrementalDeltaY;
				if ((newScroll < 0 && oldScroll >= 0)
						|| (newScroll > 0 && oldScroll <= 0)) {
					overScrollDistance = -oldScroll;
					incrementalDeltaY += overScrollDistance;
				} else {
					incrementalDeltaY = 0;
				}

				if (overScrollDistance != 0) {
					overScrollBy(0, overScrollDistance, 0, getScrollY(), 0, 0,
							0, mOverscrollDistance, true);
					final int overscrollMode = getOverScrollMode();
					if (overscrollMode == OVER_SCROLL_ALWAYS
							|| (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits())) {
						if (rawDeltaY > 0) {
							mEdgeGlowTop.onPull((float) overScrollDistance
									/ getHeight());
							if (!mEdgeGlowBottom.isFinished()) {
								mEdgeGlowBottom.onRelease();
							}
							// invalidate(mEdgeGlowTop.getBounds(false));
						} else if (rawDeltaY < 0) {
							mEdgeGlowBottom.onPull((float) overScrollDistance
									/ getHeight());
							if (!mEdgeGlowTop.isFinished()) {
								mEdgeGlowTop.onRelease();
							}
							// invalidate(mEdgeGlowBottom.getBounds(true));
						}
					}
				}

				if (incrementalDeltaY != 0) {
					// Coming back to 'real' list scrolling
					if (getScrollY() != 0) {
						// mScrollY = 0;
						setScrollY(0);
						// invalidateParentIfNeeded();
					}

					trackMotionScroll(incrementalDeltaY, incrementalDeltaY);

					mTouchMode = TOUCH_MODE_SCROLL;

					// We did not scroll the full amount. Treat this essentially
					// like the
					// start of a new touch scroll
					final int motionPosition = findClosestMotionRow(y);

					mMotionCorrection = 0;
					View motionView = getChildAt(motionPosition
							- mFirstPosition);
					mMotionViewOriginalTop = motionView != null ? motionView
							.getTop() : 0;
					mMotionY = y;
					mMotionPosition = motionPosition;
				}
				mLastY = y;
				mDirection = newDirection;
			}
		}
	}

	/**
	 * Find the row closest to y. This row will be used as the motion row when
	 * scrolling.
	 * 
	 * @param y
	 *            Where the user touched
	 * @return The position of the first (or only) item in the row closest to y
	 */
	int findClosestMotionRow(int y) {
		final int childCount = getChildCount();
		if (childCount == 0) {
			return INVALID_POSITION;
		}

		final int motionRow = findMotionRow(y);
		return motionRow != INVALID_POSITION ? motionRow : mFirstPosition
				+ childCount - 1;
	}

	int findMotionRow(int y) {
		final int childCount = getChildCount();
		if (childCount > 0) {

			final int numColumns = mNumColumns;
			if (!mStackFromBottom) {
				for (int i = 0; i < childCount; i += numColumns) {
					if (y <= getChildAt(i).getBottom()) {
						return mFirstPosition + i;
					}
				}
			} else {
				for (int i = childCount - 1; i >= 0; i -= numColumns) {
					if (y >= getChildAt(i).getTop()) {
						return mFirstPosition + i;
					}
				}
			}
		}
		return INVALID_POSITION;
	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		if (getScrollY() != scrollY) {
			onScrollChanged(getScrollX(), scrollY, getScrollX(), getScrollY());
			// mScrollY = scrollY;
			setScrollY(scrollY);
			// invalidateParentIfNeeded();

			awakenScrollBars();
		}
	}

	/**
	 * @return true if all list content currently fits within the view
	 *         boundaries
	 */
	private boolean contentFits() {
		final int childCount = getChildCount();
		if (childCount == 0)
			return true;
		if (childCount != mItemCount)
			return false;

		return getChildAt(0).getTop() >= mListPadding.top
				&& getChildAt(childCount - 1).getBottom() <= getHeight()
						- mListPadding.bottom;
	}

	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mMotionX = (int) ev.getX(newPointerIndex);
			mMotionY = (int) ev.getY(newPointerIndex);
			mMotionCorrection = 0;
			mActivePointerId = ev.getPointerId(newPointerIndex);
		}
	}

	/**
	 * Smoothly scroll by distance pixels over duration milliseconds.
	 * 
	 * @param distance
	 *            Distance to scroll in pixels.
	 * @param duration
	 *            Duration of the scroll animation in milliseconds.
	 */
	public void smoothScrollBy(int distance, int duration) {
		smoothScrollBy(distance, duration, false);
	}

	void smoothScrollBy(int distance, int duration, boolean linear) {
		if (mFlingRunnable == null) {
			mFlingRunnable = new FlingRunnable();
		}

		// No sense starting to scroll if we're not going anywhere
		final int firstPos = mFirstPosition;
		final int childCount = getChildCount();
		final int lastPos = firstPos + childCount;
		final int topLimit = getPaddingTop();
		final int bottomLimit = getHeight() - getPaddingBottom();

		if (distance == 0
				|| mItemCount == 0
				|| childCount == 0
				|| (firstPos == 0 && getChildAt(0).getTop() == topLimit && distance < 0)
				|| (lastPos == mItemCount
						&& getChildAt(childCount - 1).getBottom() == bottomLimit && distance > 0)) {
			mFlingRunnable.endFling();
			if (mPositionScroller != null) {
				mPositionScroller.stop();
			}
		} else {
			reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
			mFlingRunnable.startScroll(distance, duration, linear);
		}
	}

	class PositionScroller implements Runnable {
		private static final int SCROLL_DURATION = 200;

		private static final int MOVE_DOWN_POS = 1;
		private static final int MOVE_UP_POS = 2;
		private static final int MOVE_DOWN_BOUND = 3;
		private static final int MOVE_UP_BOUND = 4;
		private static final int MOVE_OFFSET = 5;

		private int mMode;
		private int mTargetPos;
		private int mBoundPos;
		private int mLastSeenPos;
		private int mScrollDuration;
		private final int mExtraScroll;

		private int mOffsetFromTop;

		PositionScroller() {
			mExtraScroll = ViewConfiguration.get(mContext)
					.getScaledFadingEdgeLength();
		}

		void start(final int position) {
			stop();

			if (mDataChanged) {
				// Wait until we're back in a stable state to try this.
				mPositionScrollAfterLayout = new Runnable() {
					@Override
					public void run() {
						start(position);
					}
				};
				return;
			}

			final int childCount = getChildCount();
			if (childCount == 0) {
				// Can't scroll without children.
				return;
			}

			final int firstPos = mFirstPosition;
			final int lastPos = firstPos + childCount - 1;

			int viewTravelCount;
			int clampedPosition = Math.max(0,
					Math.min(getCount() - 1, position));
			if (clampedPosition < firstPos) {
				viewTravelCount = firstPos - clampedPosition + 1;
				mMode = MOVE_UP_POS;
			} else if (clampedPosition > lastPos) {
				viewTravelCount = clampedPosition - lastPos + 1;
				mMode = MOVE_DOWN_POS;
			} else {
				scrollToVisible(clampedPosition, INVALID_POSITION,
						SCROLL_DURATION);
				return;
			}

			if (viewTravelCount > 0) {
				mScrollDuration = SCROLL_DURATION / viewTravelCount;
			} else {
				mScrollDuration = SCROLL_DURATION;
			}
			mTargetPos = clampedPosition;
			mBoundPos = INVALID_POSITION;
			mLastSeenPos = INVALID_POSITION;

			postOnAnimation(this);
		}

		void start(final int position, final int boundPosition) {
			stop();

			if (boundPosition == INVALID_POSITION) {
				start(position);
				return;
			}

			if (mDataChanged) {
				// Wait until we're back in a stable state to try this.
				mPositionScrollAfterLayout = new Runnable() {
					@Override
					public void run() {
						start(position, boundPosition);
					}
				};
				return;
			}

			final int childCount = getChildCount();
			if (childCount == 0) {
				// Can't scroll without children.
				return;
			}

			final int firstPos = mFirstPosition;
			final int lastPos = firstPos + childCount - 1;

			int viewTravelCount;
			int clampedPosition = Math.max(0,
					Math.min(getCount() - 1, position));
			if (clampedPosition < firstPos) {
				final int boundPosFromLast = lastPos - boundPosition;
				if (boundPosFromLast < 1) {
					// Moving would shift our bound position off the screen.
					// Abort.
					return;
				}

				final int posTravel = firstPos - clampedPosition + 1;
				final int boundTravel = boundPosFromLast - 1;
				if (boundTravel < posTravel) {
					viewTravelCount = boundTravel;
					mMode = MOVE_UP_BOUND;
				} else {
					viewTravelCount = posTravel;
					mMode = MOVE_UP_POS;
				}
			} else if (clampedPosition > lastPos) {
				final int boundPosFromFirst = boundPosition - firstPos;
				if (boundPosFromFirst < 1) {
					// Moving would shift our bound position off the screen.
					// Abort.
					return;
				}

				final int posTravel = clampedPosition - lastPos + 1;
				final int boundTravel = boundPosFromFirst - 1;
				if (boundTravel < posTravel) {
					viewTravelCount = boundTravel;
					mMode = MOVE_DOWN_BOUND;
				} else {
					viewTravelCount = posTravel;
					mMode = MOVE_DOWN_POS;
				}
			} else {
				scrollToVisible(clampedPosition, boundPosition, SCROLL_DURATION);
				return;
			}

			if (viewTravelCount > 0) {
				mScrollDuration = SCROLL_DURATION / viewTravelCount;
			} else {
				mScrollDuration = SCROLL_DURATION;
			}
			mTargetPos = clampedPosition;
			mBoundPos = boundPosition;
			mLastSeenPos = INVALID_POSITION;

			postOnAnimation(this);
		}

		void startWithOffset(int position, int offset) {
			startWithOffset(position, offset, SCROLL_DURATION);
		}

		void startWithOffset(final int position, int offset, final int duration) {
			stop();

			if (mDataChanged) {
				// Wait until we're back in a stable state to try this.
				final int postOffset = offset;
				mPositionScrollAfterLayout = new Runnable() {
					@Override
					public void run() {
						startWithOffset(position, postOffset, duration);
					}
				};
				return;
			}

			final int childCount = getChildCount();
			if (childCount == 0) {
				// Can't scroll without children.
				return;
			}

			offset += getPaddingTop();

			mTargetPos = Math.max(0, Math.min(getCount() - 1, position));
			mOffsetFromTop = offset;
			mBoundPos = INVALID_POSITION;
			mLastSeenPos = INVALID_POSITION;
			mMode = MOVE_OFFSET;

			final int firstPos = mFirstPosition;
			final int lastPos = firstPos + childCount - 1;

			int viewTravelCount;
			if (mTargetPos < firstPos) {
				viewTravelCount = firstPos - mTargetPos;
			} else if (mTargetPos > lastPos) {
				viewTravelCount = mTargetPos - lastPos;
			} else {
				// On-screen, just scroll.
				final int targetTop = getChildAt(mTargetPos - firstPos)
						.getTop();
				smoothScrollBy(targetTop - offset, duration, true);
				return;
			}

			// Estimate how many screens we should travel
			final float screenTravelCount = (float) viewTravelCount
					/ childCount;
			mScrollDuration = screenTravelCount < 1 ? duration
					: (int) (duration / screenTravelCount);
			mLastSeenPos = INVALID_POSITION;

			postOnAnimation(this);
		}

		/**
		 * Scroll such that targetPos is in the visible padded region without
		 * scrolling boundPos out of view. Assumes targetPos is onscreen.
		 */
		void scrollToVisible(int targetPos, int boundPos, int duration) {
			final int firstPos = mFirstPosition;
			final int childCount = getChildCount();
			final int lastPos = firstPos + childCount - 1;
			final int paddedTop = mListPadding.top;
			final int paddedBottom = getHeight() - mListPadding.bottom;
			if (boundPos < firstPos || boundPos > lastPos) {
				// boundPos doesn't matter, it's already offscreen.
				boundPos = INVALID_POSITION;
			}

			final View targetChild = getChildAt(targetPos - firstPos);
			final int targetTop = targetChild.getTop();
			final int targetBottom = targetChild.getBottom();
			int scrollBy = 0;

			if (targetBottom > paddedBottom) {
				scrollBy = targetBottom - paddedBottom;
			}
			if (targetTop < paddedTop) {
				scrollBy = targetTop - paddedTop;
			}

			if (scrollBy == 0) {
				return;
			}

			if (boundPos >= 0) {
				final View boundChild = getChildAt(boundPos - firstPos);
				final int boundTop = boundChild.getTop();
				final int boundBottom = boundChild.getBottom();
				final int absScroll = Math.abs(scrollBy);

				if (scrollBy < 0 && boundBottom + absScroll > paddedBottom) {
					// Don't scroll the bound view off the bottom of the screen.
					scrollBy = Math.max(0, boundBottom - paddedBottom);
				} else if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
					// Don't scroll the bound view off the top of the screen.
					scrollBy = Math.min(0, boundTop - paddedTop);
				}
			}

			smoothScrollBy(scrollBy, duration);
		}

		void stop() {
			removeCallbacks(this);
		}

		public void run() {
			final int listHeight = getHeight();
			final int firstPos = mFirstPosition;

			switch (mMode) {
			case MOVE_DOWN_POS: {
				final int lastViewIndex = getChildCount() - 1;
				final int lastPos = firstPos + lastViewIndex;

				if (lastViewIndex < 0) {
					return;
				}

				if (lastPos == mLastSeenPos) {
					// No new views, let things keep going.
					postOnAnimation(this);
					return;
				}

				final View lastView = getChildAt(lastViewIndex);
				final int lastViewHeight = lastView.getHeight();
				final int lastViewTop = lastView.getTop();
				final int lastViewPixelsShowing = listHeight - lastViewTop;
				final int extraScroll = lastPos < mItemCount - 1 ? Math.max(
						mListPadding.bottom, mExtraScroll)
						: mListPadding.bottom;

				final int scrollBy = lastViewHeight - lastViewPixelsShowing
						+ extraScroll;
				smoothScrollBy(scrollBy, mScrollDuration, true);

				mLastSeenPos = lastPos;
				if (lastPos < mTargetPos) {
					postOnAnimation(this);
				}
				break;
			}

			case MOVE_DOWN_BOUND: {
				final int nextViewIndex = 1;
				final int childCount = getChildCount();

				if (firstPos == mBoundPos || childCount <= nextViewIndex
						|| firstPos + childCount >= mItemCount) {
					return;
				}
				final int nextPos = firstPos + nextViewIndex;

				if (nextPos == mLastSeenPos) {
					// No new views, let things keep going.
					postOnAnimation(this);
					return;
				}

				final View nextView = getChildAt(nextViewIndex);
				final int nextViewHeight = nextView.getHeight();
				final int nextViewTop = nextView.getTop();
				final int extraScroll = Math.max(mListPadding.bottom,
						mExtraScroll);
				if (nextPos < mBoundPos) {
					smoothScrollBy(
							Math.max(0, nextViewHeight + nextViewTop
									- extraScroll), mScrollDuration, true);

					mLastSeenPos = nextPos;

					postOnAnimation(this);
				} else {
					if (nextViewTop > extraScroll) {
						smoothScrollBy(nextViewTop - extraScroll,
								mScrollDuration, true);
					}
				}
				break;
			}

			case MOVE_UP_POS: {
				if (firstPos == mLastSeenPos) {
					// No new views, let things keep going.
					postOnAnimation(this);
					return;
				}

				final View firstView = getChildAt(0);
				if (firstView == null) {
					return;
				}
				final int firstViewTop = firstView.getTop();
				final int extraScroll = firstPos > 0 ? Math.max(mExtraScroll,
						mListPadding.top) : mListPadding.top;

				smoothScrollBy(firstViewTop - extraScroll, mScrollDuration,
						true);

				mLastSeenPos = firstPos;

				if (firstPos > mTargetPos) {
					postOnAnimation(this);
				}
				break;
			}

			case MOVE_UP_BOUND: {
				final int lastViewIndex = getChildCount() - 2;
				if (lastViewIndex < 0) {
					return;
				}
				final int lastPos = firstPos + lastViewIndex;

				if (lastPos == mLastSeenPos) {
					// No new views, let things keep going.
					postOnAnimation(this);
					return;
				}

				final View lastView = getChildAt(lastViewIndex);
				final int lastViewHeight = lastView.getHeight();
				final int lastViewTop = lastView.getTop();
				final int lastViewPixelsShowing = listHeight - lastViewTop;
				final int extraScroll = Math
						.max(mListPadding.top, mExtraScroll);
				mLastSeenPos = lastPos;
				if (lastPos > mBoundPos) {
					smoothScrollBy(-(lastViewPixelsShowing - extraScroll),
							mScrollDuration, true);
					postOnAnimation(this);
				} else {
					final int bottom = listHeight - extraScroll;
					final int lastViewBottom = lastViewTop + lastViewHeight;
					if (bottom > lastViewBottom) {
						smoothScrollBy(-(bottom - lastViewBottom),
								mScrollDuration, true);
					}
				}
				break;
			}

			case MOVE_OFFSET: {
				if (mLastSeenPos == firstPos) {
					// No new views, let things keep going.
					postOnAnimation(this);
					return;
				}

				mLastSeenPos = firstPos;

				final int childCount = getChildCount();
				final int position = mTargetPos;
				final int lastPos = firstPos + childCount - 1;

				int viewTravelCount = 0;
				if (position < firstPos) {
					viewTravelCount = firstPos - position + 1;
				} else if (position > lastPos) {
					viewTravelCount = position - lastPos;
				}

				// Estimate how many screens we should travel
				final float screenTravelCount = (float) viewTravelCount
						/ childCount;

				final float modifier = Math.min(Math.abs(screenTravelCount),
						1.f);
				if (position < firstPos) {
					final int distance = (int) (-getHeight() * modifier);
					final int duration = (int) (mScrollDuration * modifier);
					smoothScrollBy(distance, duration, true);
					postOnAnimation(this);
				} else if (position > lastPos) {
					final int distance = (int) (getHeight() * modifier);
					final int duration = (int) (mScrollDuration * modifier);
					smoothScrollBy(distance, duration, true);
					postOnAnimation(this);
				} else {
					// On-screen, just scroll.
					final int targetTop = getChildAt(position - firstPos)
							.getTop();
					final int distance = targetTop - mOffsetFromTop;
					final int duration = (int) (mScrollDuration * ((float) Math
							.abs(distance) / getHeight()));
					smoothScrollBy(distance, duration, true);
				}
				break;
			}

			default:
				break;
			}
		}
	}
	private View downbtn=null;
	private View upbtn=null;
	private int count = 0;
   public void setDownView(View v){
	   downbtn = v;
   }
   public void setUpView(View v){
	   upbtn = v;
   }
	public boolean pageScroll(int direction) {
//		if(count==0){
//			mSelectedPosition = 0;
//		}
//		count++;
//		if(mSelectedPosition<0){
//			mSelectedPosition = 0;
//		}
		if(mSelectedPosition==-1){
			mSelectedPosition = 0;
		}
		int nextPage = -1;     
		if (direction == FOCUS_UP) {
			nextPage = Math.max(0, mSelectedPosition - getChildCount());
		} else if (direction == FOCUS_DOWN) {
			nextPage = Math.min(mItemCount - 1, mSelectedPosition
					+ getChildCount());
		}
		
			if (nextPage >= 0) {
				//upbtn.setVisibility(View.VISIBLE);
				setSelectionInt(nextPage);
				invokeOnItemScrollListener();
				awakenScrollBars();
				if(downbtn!=null&&upbtn!=null){
					if(mFirstPosition==0){
						upbtn.setVisibility(View.INVISIBLE);
						downbtn.setVisibility(View.VISIBLE);
						}
						else if(mFirstPosition>0&&mFirstPosition+getChildCount()<mItemCount){
						upbtn.setVisibility(View.VISIBLE);
						downbtn.setVisibility(View.VISIBLE);
						}
						else if(mFirstPosition+getChildCount()==mItemCount){
						upbtn.setVisibility(View.VISIBLE);
						downbtn.setVisibility(View.INVISIBLE);
						}
				}
				return true;
			}
		return false;
	}

	boolean resurrectSelectionIfNeeded() {
		if (mSelectedPosition < 0 && resurrectSelection()) {
			updateSelectorState();
			return true;
		}
		return false;
	}

	boolean resurrectSelection() {
		final int childCount = getChildCount();

		if (childCount <= 0) {
			return false;
		}

		int selectedTop = 0;
		int selectedPos;
		int childrenTop = mListPadding.top;
		int childrenBottom = getBottom() - getTop() - mListPadding.bottom;
		final int firstPosition = mFirstPosition;
		final int toPosition = mResurrectToPosition;
		boolean down = true;

		if (toPosition >= firstPosition
				&& toPosition < firstPosition + childCount) {
			selectedPos = toPosition;

			final View selected = getChildAt(selectedPos - mFirstPosition);
			selectedTop = selected.getTop();
			int selectedBottom = selected.getBottom();

			// We are scrolled, don't get in the fade
			if (selectedTop < childrenTop) {
				selectedTop = childrenTop + getVerticalFadingEdgeLength();
			} else if (selectedBottom > childrenBottom) {
				selectedTop = childrenBottom - selected.getMeasuredHeight()
						- getVerticalFadingEdgeLength();
			}
		} else {
			if (toPosition < firstPosition) {
				// Default to selecting whatever is first
				selectedPos = firstPosition;
				for (int i = 0; i < childCount; i++) {
					final View v = getChildAt(i);
					final int top = v.getTop();

					if (i == 0) {
						// Remember the position of the first item
						selectedTop = top;
						// See if we are scrolled at all
						if (firstPosition > 0 || top < childrenTop) {
							// If we are scrolled, don't select anything that is
							// in the fade region
							childrenTop += getVerticalFadingEdgeLength();
						}
					}
					if (top >= childrenTop) {
						// Found a view whose top is fully visisble
						selectedPos = firstPosition + i;
						selectedTop = top;
						break;
					}
				}
			} else {
				final int itemCount = mItemCount;
				down = false;
				selectedPos = firstPosition + childCount - 1;

				for (int i = childCount - 1; i >= 0; i--) {
					final View v = getChildAt(i);
					final int top = v.getTop();
					final int bottom = v.getBottom();

					if (i == childCount - 1) {
						selectedTop = top;
						if (firstPosition + childCount < itemCount
								|| bottom > childrenBottom) {
							childrenBottom -= getVerticalFadingEdgeLength();
						}
					}

					if (bottom <= childrenBottom) {
						selectedPos = firstPosition + i;
						selectedTop = top;
						break;
					}
				}
			}
		}

		mResurrectToPosition = INVALID_POSITION;
		// removeCallbacks(mFlingRunnable);
		// if (mPositionScroller != null) {
		// mPositionScroller.stop();
		// }
		mTouchMode = TOUCH_MODE_REST;
		clearScrollingCache();
		mSpecificTop = selectedTop;
		selectedPos = lookForSelectablePosition(selectedPos, down);
		if (selectedPos >= firstPosition
				&& selectedPos <= getLastVisiblePosition()) {
			mLayoutMode = LAYOUT_SPECIFIC;
			updateSelectorState();
			setSelectionInt(selectedPos);
			invokeOnItemScrollListener();
		} else {
			selectedPos = INVALID_POSITION;
		}
		reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);

		return selectedPos >= 0;
	}

	void reportScrollStateChange(int newState) {
		if (newState != mLastScrollState) {
			if (mOnScrollListener != null) {
				mLastScrollState = newState;
				mOnScrollListener.onScrollStateChanged(this, newState);

			}
		}
	}

	public boolean arrowScroll(int direction) {
		final int selectedPosition = mSelectedPosition;
		final int numColumns = mNumColumns;

		int startOfRowPos;
		int endOfRowPos;

		boolean moved = false;

		if (!mStackFromBottom) {
			startOfRowPos = (selectedPosition / numColumns) * numColumns;
			endOfRowPos = Math.min(startOfRowPos + numColumns - 1,
					mItemCount - 1);
		} else {
			final int invertedSelection = mItemCount - 1 - selectedPosition;
			endOfRowPos = mItemCount - 1 - (invertedSelection / numColumns)
					* numColumns;
			startOfRowPos = Math.max(0, endOfRowPos - numColumns + 1);
		}

		switch (direction) {
		case FOCUS_UP:
			if (startOfRowPos > 0) {
				mLayoutMode = LAYOUT_MOVE_SELECTION;
				setSelectionInt(Math.max(0, selectedPosition - numColumns));
				moved = true;
			}
			else{
				Log.i("dashuai", "first");
			}
			break;
		case FOCUS_DOWN:
			if (endOfRowPos < mItemCount - 1) {
				mLayoutMode = LAYOUT_MOVE_SELECTION;
				setSelectionInt(Math.min(selectedPosition + numColumns,
						mItemCount - 1));
				moved = true;
			}
			else{
				Log.i("dashuai", "last");
			}
			break;
		case FOCUS_LEFT:
			if (selectedPosition > startOfRowPos) {
				mLayoutMode = LAYOUT_MOVE_SELECTION;
				setSelectionInt(Math.max(0, selectedPosition - 1));
				moved = true;
			}
			break;
		case FOCUS_RIGHT:
			if (selectedPosition < endOfRowPos) {
				mLayoutMode = LAYOUT_MOVE_SELECTION;
				setSelectionInt(Math.min(selectedPosition + 1, mItemCount - 1));
				moved = true;
			}
			break;
		}

		if (moved) {
			playSoundEffect(SoundEffectConstants
					.getContantForFocusDirection(direction));
			invokeOnItemScrollListener();
		}

		if (moved) {
			awakenScrollBars();
		}

		return moved;
	}

	boolean fullScroll(int direction) {
		boolean moved = false;
		if (direction == FOCUS_UP) {
			mLayoutMode = LAYOUT_SET_SELECTION;
			setSelectionInt(0);
			invokeOnItemScrollListener();
			moved = true;
		} else if (direction == FOCUS_DOWN) {
			mLayoutMode = LAYOUT_SET_SELECTION;
			setSelectionInt(mItemCount - 1);
			invokeOnItemScrollListener();
			moved = true;
		}

		if (moved) {
			awakenScrollBars();
		}

		return moved;
	}

	void setSelectionInt(int position) {
		int previousSelectedPosition = mNextSelectedPosition;

		// if (mPositionScroller != null) {
		// mPositionScroller.stop();
		// }

		setNextSelectedPositionInt(position);
		layoutChildren();

		final int next = mStackFromBottom ? mItemCount - 1
				- mNextSelectedPosition : mNextSelectedPosition;
		final int previous = mStackFromBottom ? mItemCount - 1
				- previousSelectedPosition : previousSelectedPosition;

		final int nextRow = next / mNumColumns;
		final int previousRow = previous / mNumColumns;
		if (nextRow != previousRow) {
			awakenScrollBars();
		}

	}

	private void finishGlows() {
		if (mEdgeGlowTop != null) {
			mEdgeGlowTop.finish();
			mEdgeGlowBottom.finish();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		final int touchMode = isInTouchMode() ? TOUCH_MODE_ON : TOUCH_MODE_OFF;

		if (!hasWindowFocus) {
			setChildrenDrawingCacheEnabled(false);
			if (mFlingRunnable != null) {
				removeCallbacks(mFlingRunnable);
				// let the fling runnable report it's new state which
				// should be idle
				mFlingRunnable.endFling();
				if (mPositionScroller != null) {
					mPositionScroller.stop();
				}
				if (getScrollY() != 0) {
					setScrollY(0);
					// invalidateParentCaches();
					finishGlows();
					invalidate();
				}
			}
			// Always hide the type filter
			// dismissPopup();

			if (touchMode == TOUCH_MODE_OFF) {
				// Remember the last selected element
				mResurrectToPosition = mSelectedPosition;
			}
		} else {
			// if (mFiltered && !mPopupHidden) {
			// // Show the type filter only if a filter is in effect
			// showPopup();
			// }

			// If we changed touch mode since the last time we had focus
			if (touchMode != mLastTouchMode
					&& mLastTouchMode != TOUCH_MODE_UNKNOWN) {
				// If we come back in trackball mode, we bring the selection
				// back
				if (touchMode == TOUCH_MODE_OFF) {
					// This will trigger a layout
					resurrectSelection();

					// If we come back in touch mode, then we want to hide the
					// selector
				} else {
					hideSelector();
					mLayoutMode = LAYOUT_NORMAL;
					layoutChildren();
				}
			}
		}

		mLastTouchMode = touchMode;
	}

	void hideSelector() {
		if (mSelectedPosition != INVALID_POSITION) {
			if (mLayoutMode != LAYOUT_SPECIFIC) {
				mResurrectToPosition = mSelectedPosition;
			}
			if (mNextSelectedPosition >= 0
					&& mNextSelectedPosition != mSelectedPosition) {
				mResurrectToPosition = mNextSelectedPosition;
			}
			setSelectedPositionInt(INVALID_POSITION);
			setNextSelectedPositionInt(INVALID_POSITION);
			mSelectedTop = 0;
		}
	}

	/**
	 * Interface definition for a callback to be invoked when the list or grid
	 * has been scrolled.
	 */
	public interface OnScrollListener {

		/**
		 * The view is not scrolling. Note navigating the list using the
		 * trackball counts as being in the idle state since these transitions
		 * are not animated.
		 */
		public static int SCROLL_STATE_IDLE = 0;

		/**
		 * The user is scrolling using touch, and their finger is still on the
		 * screen
		 */
		public static int SCROLL_STATE_TOUCH_SCROLL = 1;

		/**
		 * The user had previously been scrolling using touch and had performed
		 * a fling. The animation is now coasting to a stop
		 */
		public static int SCROLL_STATE_FLING = 2;

		/**
		 * Callback method to be invoked while the grid view is being scrolled.
		 * If the view is being scrolled, this method will be called before the
		 * next frame of the scroll is rendered. In particular, it will be
		 * called before any calls to
		 * {@link Adapter#getView(int, View, ViewGroup)}.
		 * 
		 * @param view
		 *            The view whose scroll state is being reported
		 * 
		 * @param scrollState
		 *            The current scroll state. One of
		 *            {@link #SCROLL_STATE_IDLE},
		 *            {@link #SCROLL_STATE_TOUCH_SCROLL} or
		 *            {@link #SCROLL_STATE_IDLE}.
		 */
		public static int SCROLL_STATE_FOCUS_MOVING = 4;

		public void onScrollStateChanged(ZGridView view, int scrollState);

		/**
		 * Callback method to be invoked when the list or grid has been
		 * scrolled. This will be called after the scroll has completed
		 * 
		 * @param view
		 *            The view whose scroll state is being reported
		 * @param firstVisibleItem
		 *            the index of the first visible cell (ignore if
		 *            visibleItemCount == 0)
		 * @param visibleItemCount
		 *            the number of visible cells
		 * @param totalItemCount
		 *            the number of items in the list adaptor
		 */
		public void onScroll(ZGridView view, int firstVisibleItem,
							 int visibleItemCount, int totalItemCount);
	}

	/**
	 * Set the listener that will receive notifications every time the list
	 * scrolls.
	 * 
	 * @param l
	 *            the scroll listener
	 */
	public void setOnScrollListener(OnScrollListener l) {
		mOnScrollListener = l;
		invokeOnItemScrollListener();
	}

	/**
	 * Notify our scroll listener (if there is one) of a change in scroll state
	 */
	void invokeOnItemScrollListener() {
		// if (mFastScroller != null) {
		// mFastScroller.onScroll(this, mFirstPosition, getChildCount(),
		// mItemCount);
		// }
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(this, mFirstPosition, getChildCount(),
					mItemCount);
		}
		// onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation
		// does not use these.
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return commonKey(keyCode, 1, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return commonKey(keyCode, repeatCount, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return commonKey(keyCode, 1, event);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		int closestChildIndex = -1;
		if (gainFocus && previouslyFocusedRect != null) {
			 previouslyFocusedRect.offset(getScrollX(), getScrollY());

			// figure out which item should be selected based on previously
			// focused rect
			Rect otherRect = mTempRect;
			int minDistance = Integer.MAX_VALUE;
			final int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				// only consider view's on appropriate edge of grid
				if (!isCandidateSelection(i, direction)&&!isCandidate) {
					continue;
				}

				final View other = getChildAt(i);
				other.getDrawingRect(otherRect);
				offsetDescendantRectToMyCoords(other, otherRect);
				int distance = getDistance(previouslyFocusedRect, otherRect,
						direction);

				if (distance < minDistance) {
					minDistance = distance;
					closestChildIndex = i;
				}
			}
		}

		if (closestChildIndex >= 0) {
			setSelection(closestChildIndex + mFirstPosition);
		} else {
			requestLayout();
		}
		
	}

	public OnFocusChangeListener mFocusListener;
	public boolean isCandidate=false;
	private boolean isCandidateSelection(int childIndex, int direction) {
		final int count = getChildCount();
		final int invertedIndex = count - 1 - childIndex;

		int rowStart;
		int rowEnd;

		if (!mStackFromBottom) {
			rowStart = childIndex - (childIndex % mNumColumns);
			rowEnd = Math.max(rowStart + mNumColumns - 1, count);
		} else {
			rowEnd = count - 1
					- (invertedIndex - (invertedIndex % mNumColumns));
			rowStart = Math.max(0, rowEnd - mNumColumns + 1);
		}

		switch (direction) {
		case View.FOCUS_RIGHT:
			// coming from left, selection is only valid if it is on left
			// edge
			return childIndex == rowStart;
		case View.FOCUS_DOWN:
			// coming from top; only valid if in top row
			return rowStart == 0;
		case View.FOCUS_LEFT:
			// coming from right, must be on right edge
			return childIndex == rowEnd;
		case View.FOCUS_UP:
			// coming from bottom, need to be in last row
			return rowEnd == count - 1;
		case View.FOCUS_FORWARD:
			// coming from top-left, need to be first in top row
			return childIndex == rowStart && rowStart == 0;
		case View.FOCUS_BACKWARD:
			// coming from bottom-right, need to be last in bottom row
			return childIndex == rowEnd && rowEnd == count - 1;
		default:
			throw new IllegalArgumentException("direction must be one of "
					+ "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, "
					+ "FOCUS_FORWARD, FOCUS_BACKWARD}.");
		}
	}

	static int getDistance(Rect source, Rect dest, int direction) {
		int sX, sY; // source x, y
		int dX, dY; // dest x, y
		switch (direction) {
		case View.FOCUS_RIGHT:
			sX = source.right;
			sY = source.top + source.height() / 2;
			dX = dest.left;
			dY = dest.top + dest.height() / 2;
			break;
		case View.FOCUS_DOWN:
			sX = source.left + source.width() / 2;
			sY = source.bottom;
			dX = dest.left + dest.width() / 2;
			dY = dest.top;
			break;
		case View.FOCUS_LEFT:
			sX = source.left;
			sY = source.top + source.height() / 2;
			dX = dest.right;
			dY = dest.top + dest.height() / 2;
			break;
		case View.FOCUS_UP:
			sX = source.left + source.width() / 2;
			sY = source.top;
			dX = dest.left + dest.width() / 2;
			dY = dest.bottom;
			break;
		case View.FOCUS_FORWARD:
		case View.FOCUS_BACKWARD:
			sX = source.right + source.width() / 2;
			sY = source.top + source.height() / 2;
			dX = dest.left + dest.width() / 2;
			dY = dest.top + dest.height() / 2;
			break;
		default:
			throw new IllegalArgumentException("direction must be one of "
					+ "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, "
					+ "FOCUS_FORWARD, FOCUS_BACKWARD}.");
		}
		int deltaX = dX - sX;
		int deltaY = dY - sY;
		return deltaY * deltaY + deltaX * deltaX;
	}

	/**
	 * A base class for Runnables that will check that their view is still
	 * attached to the original window as when the Runnable was created.
	 * 
	 */
	private class WindowRunnnable {
		private int mOriginalAttachCount;

		public void rememberWindowAttachCount() {
			mOriginalAttachCount = getWindowAttachCount();
		}

		public boolean sameWindow() {
			return hasWindowFocus()
					&& getWindowAttachCount() == mOriginalAttachCount;
		}
	}

	private class PerformClick extends WindowRunnnable implements Runnable {
		int mClickMotionPosition;

		public void run() {
			// The data has changed since we posted this action in the event
			// queue,
			// bail out before bad things happen
			if (mDataChanged)
				return;

			final ListAdapter adapter = mAdapter;
			final int motionPosition = mClickMotionPosition;
			if (adapter != null && mItemCount > 0
					&& motionPosition != INVALID_POSITION
					&& motionPosition < adapter.getCount() && sameWindow()) {
				final View view = getChildAt(motionPosition - mFirstPosition);
				// If there is no view, something bad happened (the view
				// scrolled off the
				// screen, etc.) and we should cancel the click
				if (view != null) {
					performItemClick(view, motionPosition,
							adapter.getItemId(motionPosition));
				}
			}
		}
	}

	/**
	 * Track a motion scroll
	 * 
	 * @param deltaY
	 *            Amount to offset mMotionView. This is the accumulated delta
	 *            since the motion began. Positive numbers mean the user's
	 *            finger is moving down the screen.
	 * @param incrementalDeltaY
	 *            Change in deltaY from the previous event.
	 * @return true if we're already at the beginning/end of the list and have
	 *         nothing to do.
	 */
	boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
		final int childCount = getChildCount();
		if (childCount == 0) {
			return true;
		}

		final int firstTop = getChildAt(0).getTop();
		final int lastBottom = getChildAt(childCount - 1).getBottom();

		final Rect listPadding = mListPadding;

		// "effective padding" In this case is the amount of padding that
		// affects
		// how much space should not be filled by items. If we don't clip to
		// padding
		// there is no effective padding.
		int effectivePaddingTop = 0;
		int effectivePaddingBottom = 0;
		// if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
		// effectivePaddingTop = listPadding.top;
		// effectivePaddingBottom = listPadding.bottom;
		// }

		// FIXME account for grid vertical spacing too?
		final int spaceAbove = effectivePaddingTop - firstTop;
		final int end = getHeight() - effectivePaddingBottom;
		final int spaceBelow = lastBottom - end;

		final int height = getHeight() - getPaddingBottom() - getPaddingTop();
		if (deltaY < 0) {
			deltaY = Math.max(-(height - 1), deltaY);
		} else {
			deltaY = Math.min(height - 1, deltaY);
		}

		if (incrementalDeltaY < 0) {
			incrementalDeltaY = Math.max(-(height - 1), incrementalDeltaY);
		} else {
			incrementalDeltaY = Math.min(height - 1, incrementalDeltaY);
		}

		final int firstPosition = mFirstPosition;

		// Update our guesses for where the first and last views are
		// if (firstPosition == 0) {
		// mFirstPositionDistanceGuess = firstTop - listPadding.top;
		// } else {
		// mFirstPositionDistanceGuess += incrementalDeltaY;
		// }
		// if (firstPosition + childCount == mItemCount) {
		// mLastPositionDistanceGuess = lastBottom + listPadding.bottom;
		// } else {
		// mLastPositionDistanceGuess += incrementalDeltaY;
		// }

		final boolean cannotScrollDown = (firstPosition == 0
				&& firstTop >= listPadding.top && incrementalDeltaY >= 0);
		final boolean cannotScrollUp = (firstPosition + childCount == mItemCount
				&& lastBottom <= getHeight() - listPadding.bottom && incrementalDeltaY <= 0);

		if (cannotScrollDown || cannotScrollUp) {
			return incrementalDeltaY != 0;
		}

		final boolean down = incrementalDeltaY < 0;

		final boolean inTouchMode = isInTouchMode();
		// if (inTouchMode) {
		// hideSelector();
		// }

		// final int headerViewsCount = getHeaderViewsCount();
		// final int footerViewsStart = mItemCount - getFooterViewsCount();

		int start = 0;
		int count = 0;

		if (down) {
			int top = -incrementalDeltaY;
			// if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK)
			// {
			// top += listPadding.top;
			// }
			for (int i = 0; i < childCount; i++) {
				final View child = getChildAt(i);
				if (child.getBottom() >= top) {
					break;
				} else {
					count++;
					int position = firstPosition + i;

					mRecycler.addScrapView(child, position);

				}
			}
		} else {
			int bottom = getHeight() - incrementalDeltaY;
			// if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK)
			// {
			// bottom -= listPadding.bottom;
			// }
			for (int i = childCount - 1; i >= 0; i--) {
				final View child = getChildAt(i);
				if (child.getTop() <= bottom) {
					break;
				} else {
					start = i;
					count++;
					int position = firstPosition + i;
					// if (position >= headerViewsCount && position <
					// footerViewsStart) {
					mRecycler.addScrapView(child, position);
					// }
				}
			}
		}

		// mMotionViewNewTop = mMotionViewOriginalTop + deltaY;

		mBlockLayoutRequests = true;

		if (count > 0) {
			detachViewsFromParent(start, count);
			mRecycler.removeSkippedScrap();
		}

		// invalidate before moving the children to avoid unnecessary invalidate
		// calls to bubble up from the children all the way to the top
		if (!awakenScrollBars()) {
			invalidate();
		}

		offsetChildrenTopAndBottom(incrementalDeltaY);
		if (down) {
			mFirstPosition += count;
		}

		final int absIncrementalDeltaY = Math.abs(incrementalDeltaY);
		if (spaceAbove < absIncrementalDeltaY
				|| spaceBelow < absIncrementalDeltaY) {
			fillGap(down);
		}

		if (!inTouchMode && mSelectedPosition != INVALID_POSITION) {
			final int childIndex = mSelectedPosition - mFirstPosition;
			if (childIndex >= 0 && childIndex < getChildCount()) {
				positionSelector(mSelectedPosition, getChildAt(childIndex));
			}
		} else if (mSelectorPosition != INVALID_POSITION) {
			final int childIndex = mSelectorPosition - mFirstPosition;
			if (childIndex >= 0 && childIndex < getChildCount()) {
				positionSelector(INVALID_POSITION, getChildAt(childIndex));
			}
		} else {
			mSelectorRect.setEmpty();
		}

		mBlockLayoutRequests = false;

		invokeOnItemScrollListener();

		return false;
	}

	// public void offsetChildrenTopAndBottom(int offset) {
	// final int count = getChildCount();
	//
	//
	// for (int i = 0; i < count; i++) {
	// final View v = getChildAt(i);
	// // v.mTop += offset;
	// int top = v.getTop()+offset;
	// int bottom = v.getBottom() + offset;
	// setTop(top);
	// setBottom(bottom);
	// if (v.mDisplayList != null) {
	// v.mDisplayList.offsetTopBottom(offset);
	// v.invalidateViewProperty(false, false);
	// }
	//
	// }
	// }
	public void offsetChildrenTopAndBottom(int offset) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View v = getChildAt(i);
			v.offsetTopAndBottom(offset);
		}
	}

	@Override
	public void setOverScrollMode(int mode) {
		if (mode != OVER_SCROLL_NEVER) {
			if (mEdgeGlowTop == null) {
				Context context = getContext();
				mEdgeGlowTop = new EdgeEffect(context);
				mEdgeGlowBottom = new EdgeEffect(context);
			}
		} else {
			mEdgeGlowTop = null;
			mEdgeGlowBottom = null;
		}
		super.setOverScrollMode(mode);
	}

	void fillGap(boolean down) {
		final int numColumns = mNumColumns;
		final int verticalSpacing = mVerticalSpacing;

		final int count = getChildCount();

		if (down) {
			int paddingTop = 0;
			// if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK)
			// {
			// paddingTop = getListPaddingTop();
			// }
			final int startOffset = count > 0 ? getChildAt(count - 1)
					.getBottom() + verticalSpacing : paddingTop;
			int position = mFirstPosition + count;
			if (mStackFromBottom) {
				position += numColumns - 1;
			}
			fillDown(position, startOffset);
			correctTooHigh(numColumns, verticalSpacing, getChildCount());
		} else {
			int paddingBottom = 0;
			// if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK)
			// {
			// paddingBottom = getListPaddingBottom();
			// }
			final int startOffset = count > 0 ? getChildAt(0).getTop()
					- verticalSpacing : getHeight() - paddingBottom;
			int position = mFirstPosition;
			if (!mStackFromBottom) {
				position -= numColumns;
			} else {
				position--;
			}
			fillUp(position, startOffset);
			correctTooLow(numColumns, verticalSpacing, getChildCount());
		}
	}
	
	private OnHoverListener onhoverlistener;
	public void setOnHoverListener(OnHoverListener l){
		onhoverlistener = l;
	}
	public interface OnHoverListener {
		public boolean onHover(View v, MotionEvent event);
	}
}
