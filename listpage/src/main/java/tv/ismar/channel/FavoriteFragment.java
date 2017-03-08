package tv.ismar.channel;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.Utils.LogUtils;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.adapter.RecommecdItemAdapter;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.ContentModel;
import tv.ismar.app.entity.Expense;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemCollection;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.entity.VideoEntity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.ZGridView;
import tv.ismar.app.ui.adapter.HGridAdapterImpl;
import tv.ismar.app.ui.view.AlertDialogFragment;
import tv.ismar.app.ui.view.MenuFragment;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ScrollableSectionList;
import tv.ismar.listpage.R;


public class FavoriteFragment extends Fragment implements ScrollableSectionList.OnSectionSelectChangedListener,
		ChannelListActivity.OnMenuToggleListener,
		MenuFragment.OnMenuItemClickedListener,
		OnItemSelectedListener,
		OnItemClickListener{

	private static final int INVALID_POSITION = -1;

	private HGridView mHGridView;
	private ScrollableSectionList mScrollableSectionList;
	private TextView mChannelLabel;

	private HGridAdapterImpl mHGridAdapter;
	private ArrayList<ItemCollection> mItemCollections;
	private SectionList mSectionList;

	private int mCurrentSectionPosition = 0;

	private SimpleRestClient mRestClient;

	private ContentModel[] mContentModels;

	private RelativeLayout mNoVideoContainer;

	private boolean isInGetFavoriteTask;
	private boolean isInGetItemTask;

	private MenuFragment mMenuFragment;
	private LoadingDialog mLoadingDialog;

	private int mSelectedPosition = INVALID_POSITION;

	private HashMap<String, Object> mDataCollectionProperties;

	public final static String MENU_TAG = "FavoriteMenu";
	private ZGridView recommend_gridview;
    private View divider;
    private TextView recommend_txt;
    private TextView collect_or_history_txt;
    private VideoEntity tvHome;
    private Item[] FavoriteList;
    private Button search_btn;
    private Button left_shadow;
    private Button right_shadow;
    private View gideview_layuot;
	private SkyService skyService;
	private TextView clertFavorite;
	private void initViews(View fragmentView) {
        View vv = fragmentView.findViewById(R.id.tabs_layout);
        vv.setVisibility(View.GONE);
		mHGridView = (HGridView) fragmentView.findViewById(R.id.h_grid_view);
		clertFavorite= (TextView) fragmentView.findViewById(R.id.clear_history);
        left_shadow = (Button)fragmentView.findViewById(R.id.left_shadow);
        right_shadow = (Button)fragmentView.findViewById(R.id.right_shadow);
        gideview_layuot = fragmentView.findViewById(R.id.gideview_layuot);
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
		mScrollableSectionList.setVisibility(View.GONE);
//		mScrollableSectionList.setOnSectionSelectChangeListener(this);

		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.guide_my_favorite));
		clertFavorite.setText(getResources().getString(R.string.clear_favort));
		clertFavorite.setVisibility(View.GONE);
		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
		recommend_gridview = (ZGridView)fragmentView.findViewById(R.id.recommend_gridview);
		recommend_txt = (TextView)fragmentView.findViewById(R.id.recommend_txt);
		collect_or_history_txt = (TextView)fragmentView.findViewById(R.id.collect_or_history_txt);
		clertFavorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mHGridAdapter!=null) {
					if(mHGridAdapter!=null) {
						if(!isInGetFavoriteTask) {
							if(!IsmartvActivator.getInstance().isLogin()){
								DaisyUtils.getFavoriteManager(getActivity()).deleteAll("no");
								reset();
							}
							else{
								DaisyUtils.getFavoriteManager(getActivity()).deleteAll("yes");
								EmptyAllFavorite();
							}
						}
					}
				}
			}
		});
		clertFavorite.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					clertFavorite.setTextColor(getResources().getColor(R.color._ff9c3c));
				}else{
					clertFavorite.setTextColor(getResources().getColor(R.color._ffffff));
				}
			}
		});
		clertFavorite.setOnHoverListener(new View.OnHoverListener() {
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
		properties.put(EventProperty.TITLE, "favorite");
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_COLLECT_IN, properties);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.historycollectlist_view, container, false);
		initViews(fragmentView);
		return fragmentView;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRestClient = new SimpleRestClient();
		VodApplication application = DaisyUtils.getVodApplication(getActivity());
		mContentModels = application.mContentModel;
		mLoadingDialog = new LoadingDialog(getActivity(),R.style.LoadingDialog);
		mLoadingDialog.setTvText(getResources().getString(R.string.loading));
		mLoadingDialog.showDialog();
		skyService=SkyService.ServiceManager.getService();
		createMenu();
	}
	private void GetFavoriteByNet(){
		mLoadingDialog.show();
		skyService.getBookmarks().subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(((BaseActivity) getActivity()).new BaseObserver<Item[]>() {
					@Override
					public void onCompleted() {

					}

					@Override
					public void onNext(Item[] items) {
						mLoadingDialog.dismiss();
						try{
						if(items!=null&&items.length>0){
							mItemCollections = new ArrayList<ItemCollection>();
							int num_pages = (int) Math.ceil((float)items.length / (float)ItemCollection.NUM_PER_PAGE);
							ItemCollection itemCollection = new ItemCollection(num_pages, items.length, "1", "1");
							mItemCollections.add(itemCollection);
							mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections,false);
							mHGridAdapter.setList(mItemCollections);
							if(mHGridAdapter.getCount()>0){
								mHGridView.setAdapter(mHGridAdapter);
								mHGridView.setFocusable(true);
								//mHGridView.setHorizontalFadingEdgeEnabled(true);
								//mHGridView.setFadingEdgeLength(144);
								ArrayList<Item> item  = new ArrayList<Item>();
								for(Item i:items){
									item.add(i);
								}
								mItemCollections.get(0).fillItems(0, item);
								mHGridAdapter.setList(mItemCollections);
								showData();
							}
						}else{
							no_video();
						}
					}catch (Exception e){
							LogUtils.loadException("favorite ","favorite ","","",0,"","","client",e.toString());
						}
					}

					@Override
					public void onError(Throwable e) {
						LogUtils.loadException("favorite ","favorite ","","",0,"","","",e.toString());
						mLoadingDialog.dismiss();
						super.onError(e);
					}
				});
	}
    ArrayList<Item> FavoriteLists;
    GetFavoriteTask getFavoriteTask;


	class GetFavoriteTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInGetFavoriteTask = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if(getActivity() == null)
				return null;
			ArrayList<Favorite> favorites = DaisyUtils.getFavoriteManager(getActivity()).getAllFavorites("no");
			mSectionList = new SectionList();
            FavoriteLists = new ArrayList<Item>();

            int i=0;
			HashMap<String, ItemCollection> itemCollectionMap = new HashMap<String, ItemCollection>();
			for(Favorite favorite: favorites) {
				String content_model = favorite.content_model;
				Item item = getItem(favorite);
				if(item!=null) {
					FavoriteLists.add(item);
				}
			}
			mItemCollections = new ArrayList<ItemCollection>();
            int num_pages = (int) Math.ceil((float)FavoriteLists.size() / (float)ItemCollection.NUM_PER_PAGE);
            ItemCollection itemCollection = new ItemCollection(num_pages, FavoriteLists.size(), "1", "1");
            mItemCollections.add(itemCollection);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}

			isInGetFavoriteTask = false;

			mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections,false);
            mHGridAdapter.setList(mItemCollections);
            if(mHGridAdapter.getCount()==0) {
                no_video();
                return;
            }else{
				showData();
			}
			mHGridView.setAdapter(mHGridAdapter);
			mHGridView.setFocusable(true);
            mItemCollections.get(0).fillItems(0, FavoriteLists);
            mHGridAdapter.setList(mItemCollections);
		//	mHGridView.setHorizontalFadingEdgeEnabled(true);
			//mHGridView.setFadingEdgeLength(144);
			int num_rows = mHGridView.getRows();
			int totalColumnsOfSectionX = (int) Math.ceil((float)mItemCollections.get(mCurrentSectionPosition).count / (float) num_rows);
//			mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
		}

	}

	public Item getItem(Favorite favorite) {
		Item item = new Item();
		item.url = favorite.url;
		item.title = favorite.title;
		item.adlet_url = favorite.adlet_url;
		item.content_model = favorite.content_model;
		item.is_complex = favorite.is_complex;
		item.quality = favorite.quality;
		item.expense=new Expense();
		if(favorite.cpid!=0) {
			item.expense.cpid = favorite.cpid;
		}
		if(favorite.cptitle!=null) {
			item.expense.cptitle = favorite.cptitle;
		}
		if(favorite.cpname!=null) {
			item.expense.cpname = favorite.cpname;
		}
		if(favorite.paytype!=0) {
			item.expense.pay_type = favorite.paytype;
		}
		return item;
	}


	public void getClikItem(Item item){
		PageIntent pageIntent=new PageIntent();
		mDataCollectionProperties = new HashMap<String, Object>();
		boolean[] isSubItem = new boolean[1];
		int id = SimpleRestClient.getItemId(item.url, isSubItem);
		if(isSubItem[0]) {
			mDataCollectionProperties.put("to_subitem", id);
		} else {
			mDataCollectionProperties.put("to_item", id);
		}
		mDataCollectionProperties.put("to_title", item.title);
		if(item.is_complex) {
			pageIntent.toDetailPage(getActivity(),"favorite",id);
		} else {
			pageIntent.toPlayPage(getActivity(),id,0,Source.FAVORITE);
		}
		if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
			isInGetItemTask = false;
	}

	@Override
	public void onSectionSelectChanged(int index) {
		mHGridView.jumpToSection(index);
	}
	@Override
	public void onResume() {
		AppConstant.purchase_referer = "favorite";
		AppConstant.purchase_page = "favorite";
		AppConstant.purchase_channel = "";
		BaseActivity.baseChannel="";
		BaseActivity.baseSection="";
		((ChannelListActivity)getActivity()).registerOnMenuToggleListener(this);
		if(!IsmartvActivator.getInstance().isLogin()){
					getFavoriteTask = new GetFavoriteTask();
					getFavoriteTask.execute();
		}
		else {
					GetFavoriteByNet();
		}
		super.onResume();
	}
	@Override
	public void onPause() {
		if(mHGridAdapter!=null) {
			mHGridAdapter.cancel();
		}

		((ChannelListActivity)getActivity()).unregisterOnMenuToggleListener();
		HashMap<String, Object> properties = mDataCollectionProperties;
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_COLLECT_OUT, properties);
		mDataCollectionProperties = null;
		super.onPause();
	}

	private void no_video() {
		mNoVideoContainer.setVisibility(View.VISIBLE);
		mNoVideoContainer.setBackgroundResource(R.drawable.no_record);
        gideview_layuot.setVisibility(View.GONE);
//		mScrollableSectionList.setVisibility(View.GONE);
		mHGridView.setVisibility(View.GONE);
		clertFavorite.setVisibility(View.GONE);
		collect_or_history_txt.setText(getResources().getString(R.string.no_collect_record));
		getTvHome();
	}

	private void showDialog(final int dialogType, final AsyncTask task, final Object[] params)  {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(dialogType==AlertDialogFragment.NETWORK_EXCEPTION_DIALOG && !isInGetItemTask) {
					task.execute(params);
				} else if(!isInGetFavoriteTask) {
					DaisyUtils.getFavoriteManager(getActivity()).deleteFavoriteByUrl((String)params[0],"no");
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
//		mScrollableSectionList.reset();
		getFavoriteTask = new GetFavoriteTask();
		getFavoriteTask.execute();
	}

	private void createMenu() {
		mMenuFragment = MenuFragment.newInstance();
		mMenuFragment.setResId(R.string.vod_bookmark_clear);
		mMenuFragment.setOnMenuItemClickedListener(this);
	}

	@Override
	public void onMenuItemClicked(MenuFragment.MenuItem item) {
		switch(item.id) {
		case 1:
			if(mHGridAdapter!=null) {
				if(mSelectedPosition!=INVALID_POSITION) {
					Item selectedItem = mHGridAdapter.getItem(mSelectedPosition);
					if(!isInGetFavoriteTask && selectedItem!=null && selectedItem.url!=null) {
						DaisyUtils.getFavoriteManager(getActivity()).deleteFavoriteByUrl(selectedItem.url,"no");
						reset();
					}
				}
			}
			break;
		case 2:
			if(mHGridAdapter!=null) {
				if(!isInGetFavoriteTask) {
					if("".equals(IsmartvActivator.getInstance().getAuthToken())){
						DaisyUtils.getFavoriteManager(getActivity()).deleteAll("no");
						reset();
					}
					else{
						DaisyUtils.getFavoriteManager(getActivity()).deleteAll("yes");
						EmptyAllFavorite();
					}
				}
			}
			break;
		case 3 : startSakura(getActivity());break;
		case 4 : startPersoncenter(getActivity());break;
		}
	}
	private void EmptyAllFavorite(){
		skyService.emptyBookmarks(IsmartvActivator.getInstance().getDeviceToken()).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(((BaseActivity) getActivity()).new BaseObserver<ResponseBody>() {
					@Override
					public void onCompleted() {

					}

					@Override
					public void onNext(ResponseBody responseBody) {
						no_video();
					}

					@Override
					public void onError(Throwable e) {
						LogUtils.loadException("favorite ","favorite ","","emptyall",0,"","","server",e.toString());
						mLoadingDialog.dismiss();
						super.onError(e);
					}
				});
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
	@Override
	public void onDetach() {
        if(getFavoriteTask != null && !getFavoriteTask.isCancelled())
        	getFavoriteTask.cancel(true);
		if(mLoadingDialog.isShowing()){
			mLoadingDialog.dismiss();
		}
		mLoadingDialog = null;
		mSectionList = null;
//		mScrollableSectionList = null;
		mRestClient = null;
		mMenuFragment = null;
		super.onDetach();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int i = parent.getId();
		if (i == R.id.h_grid_view) {
			Item item = mHGridAdapter.getItem(position);
			getClikItem(item);
		} else if (i == R.id.recommend_gridview) {
			boolean[] isSubItem = new boolean[1];
			int pk=SimpleRestClient.getItemId(tvHome.getObjects().get(position).getItem_url(),isSubItem);
			PageIntent intent=new PageIntent();
			if (tvHome.getObjects().get(position).isIs_complex()) {
				intent.toDetailPage(getActivity(),"tvhome",pk);
			} else {
				intent.toPlayPage(getActivity(),pk,0, Source.FAVORITE);
			}
			mDataCollectionProperties = new HashMap<String, Object>();
			mDataCollectionProperties.put("to_title",tvHome.getObjects().get(position).getTitle());
			mDataCollectionProperties.put("to_item",pk);
			mDataCollectionProperties.put("to_subitem",0);
			mDataCollectionProperties.put("position",0);
		}
	}
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if(!SimpleRestClient.isLogin()){
			mSelectedPosition = position;
			// When selected column has changed, we need to update the ScrollableSectionList
			int sectionIndex = mHGridAdapter.getSectionIndex(position);
			int rows = mHGridView.getRows();
			int itemCount = 0;
			for(int i=0; i < sectionIndex; i++) {
				itemCount += mHGridAdapter.getSectionCount(i);

			}
			int columnOfX = (position - itemCount) / rows + 1;
			int totalColumnOfSectionX = (int)(Math.ceil((float)mHGridAdapter.getSectionCount(sectionIndex) / (float) rows));
			int percentage = (int) ((float)columnOfX / (float)totalColumnOfSectionX * 100f);
//			mScrollableSectionList.setPercentage(sectionIndex, percentage);
		}
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		mSelectedPosition = INVALID_POSITION;
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
	private void getTvHome(){
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
						LogUtils.loadException("favorite ","favorite ","","getTvhome",0,"","","server",e.toString());
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
	public void showData(){
		mNoVideoContainer.setVisibility(View.GONE);
		mNoVideoContainer.setBackgroundResource(R.drawable.no_record);
		gideview_layuot.setVisibility(View.VISIBLE);
		mScrollableSectionList.setVisibility(View.VISIBLE);
		mHGridView.setVisibility(View.VISIBLE);
		collect_or_history_txt.setVisibility(View.GONE);
		clertFavorite.setVisibility(View.VISIBLE);
	}
}
