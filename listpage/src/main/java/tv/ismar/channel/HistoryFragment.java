package tv.ismar.channel;
import cn.ismartv.truetime.TrueTime;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.Utils.LogUtils;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.adapter.RecommecdItemAdapter;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Expense;
import tv.ismar.app.entity.History;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemCollection;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.entity.VideoEntity;
import tv.ismar.app.exception.ItemOfflineException;
import tv.ismar.app.exception.NetworkException;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.ZGridView;
import tv.ismar.app.ui.adapter.HGridAdapterImpl;
import tv.ismar.app.ui.view.AlertDialogFragment;
import tv.ismar.app.ui.view.MenuFragment;
import tv.ismar.app.widget.ItemOffLinePopWindow;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.app.widget.ScrollableSectionList;
import tv.ismar.listpage.R;


public class HistoryFragment extends Fragment implements ScrollableSectionList.OnSectionSelectChangedListener,
		ChannelListActivity.OnMenuToggleListener,
		MenuFragment.OnMenuItemClickedListener,
		OnItemClickListener,
		OnItemSelectedListener{

	private static final int INVALID_POSITION = -1;

	private static final String TAG = "HistoryFragment";

	private HGridView mHGridView;
	private ScrollableSectionList mScrollableSectionList;
	private TextView mChannelLabel;
	private HGridAdapterImpl mHGridAdapter;
	private SectionList mSectionList;

	private ItemCollection mTodayItemList;
	private ItemCollection mYesterdayItemList;
	private ItemCollection mEarlyItemList;
	private int mCurrentSectionPosition = 0;
	private SimpleRestClient mRestClient;

	private RelativeLayout mNoVideoContainer;

	private LoadingDialog mLoadingDialog;

	private boolean isInGetHistoryTask;
	private boolean isInGetItemTask;

	private GetHistoryTask mGetHistoryTask;

	private MenuFragment mMenuFragment;

	public final static String MENU_TAG = "HistoryMenu";

	private HashMap<String, Object> mDataCollectionProperties;

	private int mSelectedPosition = INVALID_POSITION;
	private ZGridView recommend_gridview;
	private View divider;
	private TextView recommend_txt;
	private TextView channel_label;
	private TextView collect_or_history_txt;
	private VideoEntity tvHome;
	private Item[] mHistoriesByNet;
	private Button search_btn;
	private ItemCollection mHistoryItemList;
	private Button left_shadow;
	private Button right_shadow;
	private View gideview_layuot;
	private SkyService skyService;
	private TextView clerHistory;
	private long getTodayStartPoint() {
		long currentTime = TrueTime.now().getTime();
		GregorianCalendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		currentCalendar.setTimeInMillis(currentTime);
		currentCalendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
		currentCalendar.set(GregorianCalendar.MINUTE, 0);
		currentCalendar.set(GregorianCalendar.SECOND, 0);
		return currentCalendar.getTimeInMillis();
	}

	private long getYesterdayStartPoint() {
		long todayStartPoint = getTodayStartPoint();
		return todayStartPoint - 24*3600*1000;
	}
	private void initViews(View fragmentView) {
		final View background = fragmentView.findViewById(R.id.large_layout);
		View vv = fragmentView.findViewById(R.id.tabs_layout);
		vv.setVisibility(View.GONE);
		mHGridView = (HGridView) fragmentView.findViewById(R.id.h_grid_view);
		left_shadow = (Button)fragmentView.findViewById(R.id.left_shadow);
		right_shadow = (Button)fragmentView.findViewById(R.id.right_shadow);
		gideview_layuot = fragmentView.findViewById(R.id.gideview_layuot);
		clerHistory= (TextView) fragmentView.findViewById(R.id.clear_history);

		mHGridView.leftbtn = left_shadow;
		mHGridView.rightbtn = right_shadow;
		mHGridView.list_offset=21;
		mHGridView.setOnItemClickListener(this);
		mHGridView.setOnItemSelectedListener(this);
		left_shadow.setOnHoverListener(new View.OnHoverListener() {

			@Override
			public boolean onHover(View arg0, MotionEvent arg1) {
				if(arg1.getAction() == MotionEvent.ACTION_HOVER_ENTER || arg1.getAction() == MotionEvent.ACTION_HOVER_MOVE){
					arg0.setFocusable(true);
					arg0.setFocusableInTouchMode(true);
					arg0.requestFocusFromTouch();
				}
				return false;
			}
		});

		right_shadow.setOnHoverListener(new View.OnHoverListener() {

			@Override
			public boolean onHover(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_HOVER_ENTER
						|| arg1.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
					arg0.setFocusable(true);
					arg0.setFocusableInTouchMode(true);
					arg0.requestFocusFromTouch();
				}
				return false;
			}
		});
		left_shadow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mHGridView.pageScroll(View.FOCUS_LEFT);
				mHGridView.setFocusableInTouchMode(true);
				mHGridView.setFocusable(true);
				if(left_shadow.getVisibility() != View.VISIBLE){
					View lastView = mHGridView.getChildAt(0);
					if(lastView != null){
						lastView.requestFocus();
					}
					right_shadow.setFocusable(true);
					right_shadow.requestFocus();
				}
			}
		});
		right_shadow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mHGridView.pageScroll(View.FOCUS_RIGHT);
				mHGridView.setFocusableInTouchMode(true);
				mHGridView.setFocusable(true);
				if(right_shadow.getVisibility() != View.VISIBLE){
					Log.i("historyLeft","view gone");
					View lastView = mHGridView.getChildAt(mHGridView.getChildCount()-1);
					if(lastView != null){
						lastView.requestFocus();
					}
					left_shadow.setFocusable(true);
					left_shadow.requestFocus();
				}
			}
		});
		right_shadow.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					v.setBackgroundResource(R.drawable.scroll_right_focus);
				}else{
					v.setBackgroundResource(R.drawable.scroll_right_normal);
				}
			}
		});

		left_shadow.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setBackgroundResource(R.drawable.scroll_left_focus);
				} else {
					v.setBackgroundResource(R.drawable.scroll_left_normal);
				}
			}
		});

		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		//mScrollableSectionList.setOnSectionSelectChangeListener(this);
		mScrollableSectionList.setVisibility(View.GONE);
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.vod_movielist_title_history));
		clerHistory.setText(getResources().getString(R.string.claer_histories));
		clerHistory.setVisibility(View.GONE);
		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
		collect_or_history_txt = (TextView)fragmentView.findViewById(R.id.collect_or_history_txt);
		recommend_gridview = (ZGridView)fragmentView.findViewById(R.id.recommend_gridview);
		recommend_txt = (TextView)fragmentView.findViewById(R.id.recommend_txt);

		clerHistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mHGridAdapter!=null) {
					if(!isInGetHistoryTask) {
						if(!IsmartvActivator.getInstance().isLogin()){
							DaisyUtils.getHistoryManager(getActivity()).deleteAll("no");
							reset();
						}
						else{
							DaisyUtils.getHistoryManager(getActivity()).deleteAll("yes");
							EmptyAllHistory();
						}
					}
				}
			}
		});
		clerHistory.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					clerHistory.setTextColor(getResources().getColor(R.color._ff9c3c));
				}else{
					clerHistory.setTextColor(getResources().getColor(R.color._ffffff));
				}
			}
		});
		clerHistory.setOnHoverListener(new View.OnHoverListener() {
			@Override
			public boolean onHover(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
					v.setFocusable(true);
					v.setFocusableInTouchMode(true);
					v.requestFocus();
				}
				return false;
			}
		});
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(EventProperty.TITLE, "history");
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_HISTORY_IN, properties);
	}

	private void initHistoryList(){

		//define today's ItemList
		mTodayItemList = new ItemCollection(1, 0, "today", getResources().getString(R.string.vod_movielist_today));
		//define yesterday's ItemList
		mYesterdayItemList = new ItemCollection(1, 0, "yesterday", getResources().getString(R.string.vod_movielist_yesterday));
		//define early days's ItemList
		mEarlyItemList = new ItemCollection(1, 0, "early", getResources().getString(R.string.vod_movielist_recent));

		mHistoryItemList = new ItemCollection(1,0,"1","1");
	}
	private void addHistory(Item item) {
		History history = new History();
		history.title = item.title;
		history.adlet_url = item.adlet_url;
		history.content_model = item.content_model;
		history.is_complex = item.is_complex;
		history.last_position = item.offset;
		history.last_quality = item.quality;
		if(item.expense!=null){
			history.paytype=item.expense.pay_type;
			history.cpid=item.expense.cpid;
			history.cptitle=item.expense.cptitle;
			history.cpname=item.expense.cpname;
		}
		if ("subitem".equals(item.model_name)) {
			//  history.sub_url = item.url;
			history.sub_url =  SimpleRestClient.root_url + "/api/subitem/" + item.pk + "/";
			history.url = SimpleRestClient.root_url + "/api/item/" + item.item_pk + "/";
		} else {
			history.url = item.url;
			history.sub_url =  SimpleRestClient.root_url + "/api/item/" + item.pk+ "/";
		}


		history.is_continue = true;
		if (IsmartvActivator.getInstance().isLogin())
			DaisyUtils.getHistoryManager(getActivity()).addHistory(history,
					"yes", -1);
		else
			DaisyUtils.getHistoryManager(getActivity())
					.addHistory(history, "no", -1);

	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRestClient = new SimpleRestClient();
		mLoadingDialog = new LoadingDialog(getActivity(), R.style.LoadingDialog);
		mLoadingDialog.setTvText(getResources().getString(R.string.loading));
		skyService=SkyService.ServiceManager.getService();
		initHistoryList();
		createMenu();
		mSectionList = new SectionList();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.historycollectlist_view, container, false);
		initViews(fragmentView);
		return fragmentView;
	}
	private ArrayList<ItemCollection> mItemCollections;
	private void getHistoryByNet(){
		skyService.getHistoryByNet().subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(((BaseActivity) getActivity()).new BaseObserver<Item[]>() {
					@Override
					public void onCompleted() {

					}

					@Override
					public void onNext(Item[] items) {
						mLoadingDialog.dismiss();
						if(items!=null&&items.length>0) {
							for (Item i : items) {
								addHistory(i);
							}
							mItemCollections = new ArrayList<ItemCollection>();
							int num_pages = (int) Math.ceil((float) items.length / (float) ItemCollection.NUM_PER_PAGE);
							ItemCollection itemCollection = new ItemCollection(num_pages, items.length, "1", "1");
							mItemCollections.add(itemCollection);
							mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections, false);
							mHGridAdapter.setList(mItemCollections);
							if (mHGridAdapter.getCount() > 0) {
								mHGridView.setAdapter(mHGridAdapter);
								mHGridView.setFocusable(true);
								ArrayList<Item> item = new ArrayList<Item>();
								for (Item i : items) {
									item.add(i);
								}
								mItemCollections.get(0).fillItems(0, item);
								mHGridAdapter.setList(mItemCollections);
								showData();
							}
						}else{
							no_video();
						}
					}

					@Override
					public void onError(Throwable e) {
						if(mLoadingDialog!=null)
						mLoadingDialog.dismiss();
						LogUtils.loadException("history ","history ","","",0,"","","server",e.toString());
						super.onError(e);
					}
				});
	}
	private void EmptyAllHistory(){
		skyService.emptyHistory(IsmartvActivator.getInstance().getDeviceToken()).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(((BaseActivity) getActivity()).new BaseObserver<ResponseBody>() {
					@Override
					public void onCompleted() {

					}

					@Override
					public void onNext(ResponseBody responseBody) {
						no_video();
					}
				});
	}

	class GetHistoryTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInGetHistoryTask = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				final long todayStartPoint = getTodayStartPoint();
				final long yesterdayStartPoint = getYesterdayStartPoint();
				ArrayList<History> mHistories = DaisyUtils.getHistoryManager(getActivity()).getAllHistories("no");
				Log.i(TAG,"mHistories.size:"+mHistories.size());
				if(mHistories.size()>0) {
					Collections.sort(mHistories);
					mHistoryItemList.count=0;
					for(int i=0;i<mHistories.size();++i) {
						History history = mHistories.get(i);
						Item item = getItem(history);
						mHistoryItemList.objects.put(mHistoryItemList.count++, item);
					}
					mHistoryItemList.num_pages = (int) Math.ceil((float)mHistoryItemList.count / (float)ItemCollection.NUM_PER_PAGE);
					if(mHistoryItemList.count>0){
						Arrays.fill(mHistoryItemList.hasFilledValidItem, true);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mHistoryItemList!=null&&mHistoryItemList.count>0) {
				//mScrollableSectionList.init(mSectionList, 1365,false);
				ArrayList<ItemCollection> itemCollections = new ArrayList<ItemCollection>();
				itemCollections.add(mHistoryItemList);
				mHGridAdapter = new HGridAdapterImpl(getActivity(), itemCollections,false);
				mHGridView.setAdapter(mHGridAdapter);
				mHGridView.setFocusable(true);
				showData();
			} else {
				no_video();
			}
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInGetHistoryTask = false;
		}

	}

	private Item getItem(History history) {
		Item item = new Item();
		item.adlet_url = history.adlet_url;
		item.is_complex = history.is_complex;
		item.url = history.url;
		item.content_model = history.content_model;
		item.quality = history.quality;
		item.title = history.title;
//		if(history.price==0){
//			item.expense = null;
//		}
//		else{
			item.expense = new Expense();
			if(history.price!=0)
			item.expense.price = history.price;
			if(history.cpid!=0)
			item.expense.cpid=history.cpid;
			if(history.cpname!=null)
			item.expense.cpname=history.cpname;
			if(history.cptitle!=null)
			item.expense.cptitle=history.cptitle;
			if(history.paytype!=-1)
			item.expense.pay_type=history.paytype;
//		}
		return item;
	}

	@Override
	public void onResume() {
		AppConstant.purchase_referer = "history";
		AppConstant.purchase_page = "history";
		AppConstant.purchase_channel = "";
        BaseActivity.baseChannel="";
        BaseActivity.baseSection="";
		if(IsmartvActivator.getInstance().isLogin()){
			//登录，网络获取
			Log.i("onResum","onreusm");
			mLoadingDialog.showDialog();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					getHistoryByNet();
				}
			},2000);

		}else{
			mLoadingDialog.showDialog();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mGetHistoryTask = new GetHistoryTask();
					mGetHistoryTask.execute(); //没有登录，取本地设备信息
				}
			},2000);
		}
		((ChannelListActivity)getActivity()).registerOnMenuToggleListener(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		if(mHGridAdapter!=null) {
			mHGridAdapter.cancel();
		}
		((ChannelListActivity)getActivity()).unregisterOnMenuToggleListener();
		HashMap<String, Object> properties = mDataCollectionProperties;
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_HISTORY_OUT, properties);
		mDataCollectionProperties = null;
		super.onPause();
	}

	@Override
	public void onSectionSelectChanged(int index) {
		mHGridView.jumpToSection(index);
	}
	private Item item;
	private Item netItem;
	public void getClicItem(Item mItem) {
		mLoadingDialog.showDialog();
		int pk = 0;
		if (IsmartvActivator.getInstance().isLogin()) {
			if (mItem.model_name.equals("subitem"))
				pk = mItem.item_pk;
			else
				pk = mItem.pk;
		}else{
			pk=SimpleRestClient.getItemId(mItem.url,new boolean[1]);
		}
		skyService.getClickItem(pk).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
				.subscribe(((BaseActivity) getActivity()).new BaseObserver<Item>() {
					@Override
					public void onCompleted() {
					}
					@Override
					public void onNext(Item i) {
						item = i;
						String url = IsmartvActivator.getInstance().getApiDomain()+"/api/item/" + item.pk + "/";
						History history = null;
						if (IsmartvActivator.getInstance().isLogin())
							history = DaisyUtils.getHistoryManager(getActivity()).getHistoryByUrl(url, "yes");
						else {
							history = DaisyUtils.getHistoryManager(getActivity()).getHistoryByUrl(url, "no");
						}
						if (history == null) {
							return;
						}
						// Use to data collection.
						mDataCollectionProperties = new HashMap<String, Object>();
						int id = SimpleRestClient.getItemId(url, new boolean[1]);
						mDataCollectionProperties.put("to_item", id);
						if (history.sub_url != null && item.subitems != null) {
							int sub_id = SimpleRestClient.getItemId(history.sub_url, new boolean[1]);
							mDataCollectionProperties.put("to_subitem", sub_id);
							for (Item subitem : item.subitems) {
								if (sub_id == subitem.pk) {
								//	mDataCollectionProperties.put("to_clip", subitem.clip.pk);
									break;
								}
							}
						} else {
							mDataCollectionProperties.put("to_subitem", item.clip.pk);
						}
						mDataCollectionProperties.put("to_title", item.title);
						mDataCollectionProperties.put("position", history.last_position/1000);
						String[] qualitys = new String[]{"normal", "high", "ultra", "adaptive"};
					//	mDataCollectionProperties.put("quality", qualitys[(history.quality >= 0 && history.quality < qualitys.length) ? history.quality : 0]);
						PageIntent intent = new PageIntent();
						intent.toPlayPage(getActivity(), item.pk, 0, Source.HISTORY);
					}
					@Override
					public void onError(Throwable e) {
						HttpException httpException = (HttpException) e;
						if(httpException.code() == 404){
                            ChannelListActivity activity= (ChannelListActivity) getActivity();
                            activity.historyShowItemOffLinePop();
						}else {
							super.onError(e);
						}
						if(mLoadingDialog!=null)
						mLoadingDialog.dismiss();
					}
				});
	}
	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.no_record);
		gideview_layuot.setVisibility(View.GONE);
		mScrollableSectionList.setVisibility(View.GONE);
		mHGridView.setVisibility(View.GONE);
		collect_or_history_txt.setText(getResources().getString(R.string.no_history_record));
		clerHistory.setVisibility(View.GONE);
		getTvHome();
	}
	public void showData(){
		mNoVideoContainer.setVisibility(View.GONE);
		mNoVideoContainer.setBackgroundResource(R.drawable.no_record);
		gideview_layuot.setVisibility(View.VISIBLE);
		mScrollableSectionList.setVisibility(View.VISIBLE);
		mHGridView.setVisibility(View.VISIBLE);
		collect_or_history_txt.setVisibility(View.GONE);
		clerHistory.setVisibility(View.VISIBLE);
	}

	private void showDialog(final int dialogType, final AsyncTask task, final Object[] params) {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dialogType == AlertDialogFragment.NETWORK_EXCEPTION_DIALOG && !isInGetItemTask) {
					task.execute(params);
				} else if (!isInGetHistoryTask) {
					DaisyUtils.getHistoryManager(getActivity()).deleteHistory((String) params[0], "no");
					reset();
				}
				dialog.dismiss();
			}
		});
		newFragment.setNegativeListener(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		newFragment.show(getFragmentManager(), "dialog");
	}

	private void reset() {
		mSectionList = new SectionList();
		initHistoryList();
		if(mGetHistoryTask!=null && mGetHistoryTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetHistoryTask.cancel(true);
		}
		mGetHistoryTask = new GetHistoryTask();
		mGetHistoryTask.execute();
	}

	@Override
	public void OnMenuToggle() {
		if(mMenuFragment==null) {
			createMenu();
		}
		if(mMenuFragment.isShowing()) {
			mMenuFragment.dismiss();
		} else {
			mMenuFragment.show(getFragmentManager(), MENU_TAG);
		}
	}

	private void createMenu() {
		mMenuFragment = MenuFragment.newInstance();
		mMenuFragment.setResId(R.string.vod_history_clear);
		mMenuFragment.setOnMenuItemClickedListener(this);
	}

	@Override
	public void onMenuItemClicked(MenuFragment.MenuItem item) {
		Log.i(TAG, "menu click item id: " + item.id);
		switch(item.id) {
			case 1:
				if(mHGridAdapter!=null && mSelectedPosition!=INVALID_POSITION) {
					Item selectedItem = mHGridAdapter.getItem(mSelectedPosition);
					if(!isInGetHistoryTask && selectedItem.url!=null) {
						DaisyUtils.getHistoryManager(getActivity()).deleteHistory(selectedItem.url,"no");
						reset();
					}
				}
				break;
			case 2:
				if(mHGridAdapter!=null) {
					if(!isInGetHistoryTask) {
						if("".equals(IsmartvActivator.getInstance().getAuthToken())){
							DaisyUtils.getHistoryManager(getActivity()).deleteAll("no");
							reset();
						}
						else{
							DaisyUtils.getHistoryManager(getActivity()).deleteAll("yes");
							EmptyAllHistory();
						}
					}
				}
				break;
			case 3 : startSakura(getActivity());break;
			case 4 : startPersoncenter(getActivity());break;
		}

	}

	private void RemoveHistoriesByNet(){

	}
	@Override
	public void onDetach() {
		if(mLoadingDialog.isShowing()){
			mLoadingDialog.dismiss();
		}
		if(mGetHistoryTask!=null && mGetHistoryTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mGetHistoryTask.cancel(true);
		}
		mLoadingDialog = null;
		mRestClient = null;
		mMenuFragment = null;
		super.onDetach();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
							   long id) {
		if("".equals(IsmartvActivator.getInstance().getAuthToken())){
			mSelectedPosition = position;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		mSelectedPosition = INVALID_POSITION;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		int i = parent.getId();
		if (i == R.id.h_grid_view) {
			Item item = mHGridAdapter.getItem(position);
			getClicItem(item);
		} else if (i == R.id.recommend_gridview) {
			boolean[] isSubItem = new boolean[1];
			int pk=SimpleRestClient.getItemId(tvHome.getObjects().get(position).getItem_url(),isSubItem);
			PageIntent intent=new PageIntent();
			if (tvHome.getObjects().get(position).isIs_complex()) {
				intent.toDetailPage(getActivity(),"tvhome",pk);
			} else {
				intent.toPlayPage(getActivity(),pk,0, Source.HISTORY);
			}
			mDataCollectionProperties = new HashMap<String, Object>();
			mDataCollectionProperties.put("to_title",tvHome.getObjects().get(position).getTitle());
			mDataCollectionProperties.put("to_item",pk);
			mDataCollectionProperties.put("to_subitem",0);
			mDataCollectionProperties.put("position",0);
		}

	}
	private void setTvHome(VideoEntity videoEntity) {
		tvHome=videoEntity;
		if(tvHome.getObjects()!=null&&tvHome.getObjects().size()>0){
			RecommecdItemAdapter recommendAdapter = new RecommecdItemAdapter(getActivity(), tvHome);
			recommend_gridview.setAdapter(recommendAdapter);
			recommend_gridview.setFocusable(true);
			recommend_gridview.setOnItemClickListener(this);
		}
	}
	private void getTvHome() {
		skyService.getTvHome().subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(((BaseActivity) getActivity()).new BaseObserver<VideoEntity>() {
					@Override
					public void onCompleted() {

					}
					@Override
					public void onNext(VideoEntity videoEntity) {
						setTvHome(videoEntity);
					}

					@Override
					public void onError(Throwable e) {
						if(mLoadingDialog!=null)
						mLoadingDialog.dismiss();
						super.onError(e);
					}
				});
	}


	private void startPersoncenter(Context context){
		PageIntent intent=new PageIntent();
		intent.toUserCenter(context);
	}
	private void startSakura(Context context){
		PageIntent intent=new PageIntent();
		intent.toHelpPage(context);
	}
}
