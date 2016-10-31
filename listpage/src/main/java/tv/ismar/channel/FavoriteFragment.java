package tv.ismar.channel;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.adapter.RecommecdItemAdapter;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.ContentModel;
import tv.ismar.app.entity.Expense;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemCollection;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.entity.VideoEntity;
import tv.ismar.app.exception.ItemOfflineException;
import tv.ismar.app.exception.NetworkException;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.ZGridView;
import tv.ismar.app.ui.adapter.HGridAdapterImpl;
import tv.ismar.app.ui.view.AlertDialogFragment;
import tv.ismar.app.ui.view.MenuFragment;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ScrollableSectionList;
import tv.ismar.listpage.R;
import tv.ismar.player.InitPlayerTool;


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

	private ConcurrentHashMap<String, GetItemTask> mCurrentGetItemTask = new ConcurrentHashMap<String, GetItemTask>();

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
	private void initViews(View fragmentView) {
        View vv = fragmentView.findViewById(R.id.tabs_layout);
        vv.setVisibility(View.GONE);
		mHGridView = (HGridView) fragmentView.findViewById(R.id.h_grid_view);
        left_shadow = (Button)fragmentView.findViewById(R.id.left_shadow);
        right_shadow = (Button)fragmentView.findViewById(R.id.right_shadow);
        gideview_layuot = fragmentView.findViewById(R.id.gideview_layuot);
        mHGridView.leftbtn = left_shadow;
        mHGridView.rightbtn = right_shadow;
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
			}
		});
		right_shadow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mHGridView.pageScroll(View.FOCUS_RIGHT);
			}
		});

		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		mScrollableSectionList.setVisibility(View.GONE);
//		mScrollableSectionList.setOnSectionSelectChangeListener(this);

		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(getResources().getString(R.string.guide_my_favorite));

		mNoVideoContainer = (RelativeLayout) fragmentView.findViewById(R.id.no_video_container);
		recommend_gridview = (ZGridView)fragmentView.findViewById(R.id.recommend_gridview);
		recommend_txt = (TextView)fragmentView.findViewById(R.id.recommend_txt);
		collect_or_history_txt = (TextView)fragmentView.findViewById(R.id.collect_or_history_txt);
		search_btn = (Button)fragmentView.findViewById(R.id.list_view_search);
		search_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent searchIntent = new Intent();
              //  searchIntent.setClass(getActivity(), SearchActivity.class);
              //  startActivity(searchIntent);
			}
		});
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
						if(items!=null&&FavoriteList.length>0){
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
							}
						}else{
							no_video();
						}
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
           // mItemCollections.get(0).fillItems(0, FavoriteLists);
//			for(Section section:mSectionList) {
//				ItemCollection itemCollection= itemCollectionMap.get(section.slug);
//				int count = itemCollection.objects.size();
//				itemCollection.num_pages = (int)FloatMath.ceil((float)count / (float)ItemCollection.NUM_PER_PAGE);
//				section.count = count;
//				// we have already complete data collection.
//				itemCollection.hasFilledValidItem = new boolean[itemCollection.num_pages];
//				Arrays.fill(itemCollection.hasFilledValidItem, true);
//				mItemCollections.add(itemCollection);
//			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}

			isInGetFavoriteTask = false;

//			mScrollableSectionList.init(mSectionList, 1365,false);

			mHGridAdapter = new HGridAdapterImpl(getActivity(), mItemCollections,false);
            mHGridAdapter.setList(mItemCollections);
            if(mHGridAdapter.getCount()==0) {
                no_video();
                return;
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

	class GetItemTask extends AsyncTask<Item, Void, Integer> {

		private static final int ITEM_OFFLINE = 0;
		private static final int ITEM_SUCCESS_GET = 1;
		private static final int NETWORK_EXCEPTION = 2;

		private Item item;

		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInGetItemTask = true;
		}

		@Override
		protected void onCancelled() {
			mCurrentGetItemTask.remove(item.url);
		}

		@Override
		protected Integer doInBackground(Item... params) {
			item = params[0];
			mCurrentGetItemTask.put(item.url, this);
			Item i;
//			try {
//			//	i = mRestClient.getItem(item.url);
//                return ITEM_SUCCESS_GET;
//			} catch (ItemOfflineException e) {
//				e.printStackTrace();
//				return ITEM_OFFLINE;
//			} catch (JsonSyntaxException e) {
//				e.printStackTrace();
//				return NETWORK_EXCEPTION;
//			} catch (NetworkException e) {
//				e.printStackTrace();
//				return NETWORK_EXCEPTION;
//			}
//			if(i==null) {
//				return NETWORK_EXCEPTION;
//			} else {
//				return ITEM_SUCCESS_GET;
//			}
            return ITEM_SUCCESS_GET;
		}

		@Override
		protected void onPostExecute(Integer result) {
			mCurrentGetItemTask.remove(item.url);
			if(result== ITEM_OFFLINE) {
				showDialog(AlertDialogFragment.ITEM_OFFLINE_DIALOG, null, new Object[]{item.url});
			} else if(result == NETWORK_EXCEPTION) {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, new GetItemTask(), new Item[]{item});
			} else {
				// Use to data collection.
				mDataCollectionProperties = new HashMap<String, Object>();
				boolean[] isSubItem = new boolean[1];
				int id = SimpleRestClient.getItemId(item.url, isSubItem);
				if(isSubItem[0]) {
					mDataCollectionProperties.put("to_subitem", id);
				} else {
					mDataCollectionProperties.put("to_item", id);
				}
				mDataCollectionProperties.put("to_title", item.title);

				// start new Activity.
				Intent intent = new Intent();
				if(item.is_complex) {
//					if("variety".equals(item.content_model)){
//						intent.setAction("tv.ismar.daisy.EntertainmentItem");
//						intent.putExtra("channel", "娱乐综艺");
//					}else {
//						intent.setAction("tv.ismar.daisy.Item");
//					}
//					intent.putExtra("url", item.url);
//					startActivity(intent);
                    if (("variety".equals(item.content_model)||"entertainment".equals(item.content_model))&&item.expense!=null) {
                        intent.setAction("tv.ismar.daisy.Item");
                        intent.putExtra("title", "娱乐综艺");
                        intent.putExtra("url", SimpleRestClient.sRoot_url+"/api/item/"+id+"/");
                        intent.putExtra("fromPage","favorite");
                        startActivity(intent);
                    }else{
                      DaisyUtils.gotoSpecialPage(getActivity(),item.content_model,SimpleRestClient.sRoot_url+"/api/item/"+id+"/","favorite");
                    }

				} else {
					InitPlayerTool tool = new InitPlayerTool(getActivity());
                    tool.fromPage = "favorite";
					tool.setonAsyncTaskListener(new InitPlayerTool.onAsyncTaskHandler() {

						@Override
						public void onPreExecute(Intent intent) {
							// TODO Auto-generated method stub
							if(mLoadingDialog != null)
							mLoadingDialog.show();
						}

						@Override
						public void onPostExecute() {
							// TODO Auto-generated method stub
							if(mLoadingDialog != null)
							mLoadingDialog.dismiss();
						}
					});
					tool.initClipInfo(SimpleRestClient.sRoot_url+"/api/item/"+id+"/", InitPlayerTool.FLAG_URL);
				}
			}

			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInGetItemTask = false;
		}

	}
	public void getClikItem(Item item){
		PageIntent pageIntent=new PageIntent();
		mCurrentGetItemTask.remove(item.url);
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
		//	pageIntent.toPlayPage(getActivity(),item.pk,item.,"favorite");
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
		((ChannelListActivity)getActivity()).registerOnMenuToggleListener(this);
		new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_COLLECT_IN);
		if("".equals(SimpleRestClient.access_token)){
			getFavoriteTask = new GetFavoriteTask();
			getFavoriteTask.execute();
		}
		else
			GetFavoriteByNet();
		super.onResume();
	}
	@Override
	public void onPause() {
		if(mHGridAdapter!=null) {
			mHGridAdapter.cancel();
		}

		ConcurrentHashMap<String, GetItemTask> currentGetItemTask = mCurrentGetItemTask;
		for(String url: currentGetItemTask.keySet()) {
			currentGetItemTask.get(url).cancel(true);
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
					if("".equals(SimpleRestClient.access_token)){
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
//		case 3 : SakuraUtils.startSakura(getActivity());break;
		case 4 : startPersoncenter();break;
		}
	}
	private void EmptyAllFavorite(){
		skyService.emptyBookmarks().subscribeOn(Schedulers.io())
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
		//	new GetItemTask().execute(item);
			getClikItem(item);

		} else if (i == R.id.recommend_gridview) {
			if (tvHome.getObjects().get(position).isIs_complex()) {
				boolean[] isSubItem = new boolean[1];
				int pk=SimpleRestClient.getItemId(tvHome.getObjects().get(position).getItem_url(),isSubItem);
				PageIntent intent=new PageIntent();
				intent.toDetailPage(getActivity(),"tvhome",pk);
			} else {
				InitPlayerTool tool = new InitPlayerTool(getActivity());
				tool.fromPage = "favorite";
				tool.initClipInfo(tvHome.getObjects().get(position).getItem_url(), InitPlayerTool.FLAG_URL);
			}

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
				});
	}

	   private void startPersoncenter(){
//		   Intent intent = new Intent(getActivity(),UserCenterActivity.class);
//		   startActivity(intent);
	   }
}
