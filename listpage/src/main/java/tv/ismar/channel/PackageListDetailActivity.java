package tv.ismar.channel;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.TextView;


import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemCollection;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.adapter.HGridAdapterImpl;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ScrollableSectionList;
import tv.ismar.listpage.R;


public class PackageListDetailActivity extends BaseActivity implements OnItemSelectedListener, OnItemClickListener, HGridView.OnScrollListener {

    private SectionList mSectionList;

    private ArrayList<ItemCollection> mItemCollections;

    private HGridView mHGridView;

    private HGridAdapterImpl mHGridAdapter;
    private ScrollableSectionList section_tabs;
    private LoadingDialog mLoadingDialog;
    private ItemList items;
    private int pk;
    private boolean mIsBusy = false;
    private TextView channel_label;
    private Button btn_search;
    private String itemlistUrl;
    private String lableString;
    private Button left_shadow;
    private Button right_shadow;
    private SkyService skyService;
    private TextView clear_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historycollectlist_view);
        final View background = findViewById(R.id.large_layout);
        new BitmapDecoder().decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                background.setBackgroundDrawable(bitmapDrawable);
            }
        });
        skyService=SkyService.ServiceManager.getService();
        mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        mLoadingDialog.setTvText(getResources().getString(R.string.loading));
        mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
       // DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        itemlistUrl = getIntent().getStringExtra("itemlistUrl");
        lableString = getIntent().getStringExtra("lableString");
        initView();
        getData();
    }

    @Override
    protected void onDestroy() {
     //   DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        super.onDestroy();
    }

    private void initView() {
        channel_label = (TextView) findViewById(R.id.channel_label);
        if (!TextUtils.isEmpty(lableString)) {
            channel_label.setText(lableString);
        } else {
            channel_label.setText("礼包内容");
        }
        clear_history= (TextView) findViewById(R.id.clear_history);
        clear_history.setVisibility(View.GONE);
        section_tabs = (ScrollableSectionList) findViewById(R.id.section_tabs);
        section_tabs.setVisibility(View.GONE);
        left_shadow = (Button) findViewById(R.id.left_shadow);
        right_shadow = (Button) findViewById(R.id.right_shadow);
        mHGridView = (HGridView) findViewById(R.id.h_grid_view);
        mHGridView.setOnItemClickListener(this);
        mHGridView.setOnItemSelectedListener(this);
        mHGridView.setOnScrollListener(this);
        mHGridView.leftbtn = left_shadow;
        mHGridView.rightbtn = right_shadow;
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
				 left_shadow.setVisibility(View.VISIBLE);
			}
		});
//        btn_search = (Button) findViewById(R.id.list_view_search);
//        btn_search.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                PageIntent intent=new PageIntent();
//                intent.toSearch(PackageListDetailActivity.this);
//            }
//        });
    }

    private void getData() {
        pk = getIntent().getIntExtra("pk", -1);
        mLoadingDialog.showDialog();
        getPackageList();
    }

    private OnCancelListener mLoadingCancelListener = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            finish();
            dialog.dismiss();
        }
    };
    private void getPackageList(){
        skyService.getPackageList(itemlistUrl).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemList>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ItemList itemList) {
                        if (itemList != null) {
                            mLoadingDialog.dismiss();
                            mItemCollections = new ArrayList<ItemCollection>();
                            int num_pages = (int) Math.ceil((float) itemList.count / (float) ItemCollection.NUM_PER_PAGE);
                            ItemCollection itemCollection = new ItemCollection(num_pages, itemList.count, "1", "1");
                            mItemCollections.add(itemCollection);

                            mHGridAdapter = new HGridAdapterImpl(PackageListDetailActivity.this, mItemCollections, false);
                            mHGridAdapter.setList(mItemCollections);
                            if (mHGridAdapter.getCount() > 0) {
                                mHGridView.setAdapter(mHGridAdapter);
                                mHGridView.setFocusable(true);
                                mHGridView.requestFocus();
                                mItemCollections.get(0).fillItems(0, itemList.objects);
                                mHGridAdapter.setList(mItemCollections);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mLoadingDialog.dismiss();
                        super.onError(e);
                    }
                });
    }
    private int index;
    private void getPackageListItem(int composedIndex){
        index=composedIndex;
        int[] sectionAndPage = getSectionAndPageFromIndex(index);
        int page = sectionAndPage[1] + 1;
        String url=itemlistUrl+page+"/";
        skyService.getPackageListItem(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemList>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ItemList itemList) {
                        if (itemList != null && itemList.objects != null) {
                            int sectionIndex = getSectionAndPageFromIndex(index)[0];
                            int page = getSectionAndPageFromIndex(index)[1];
                            ItemCollection itemCollection = mItemCollections.get(sectionIndex);
                            itemCollection.fillItems(page, itemList.objects);
                            mHGridAdapter.setList(mItemCollections);
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }

    private int getIndexFromSectionAndPage(int sectionIndex, int page) {
        return sectionIndex * 10000 + page;
    }

    private int[] getSectionAndPageFromIndex(int index) {
        int[] sectionAndPage = new int[2];
        sectionAndPage[0] = index / 10000;
        sectionAndPage[1] = index - index / 10000 * 10000;
        return sectionAndPage;
    }
    @Override
    public void onScrollStateChanged(HGridView view, int scrollState) {
        // TODO Auto-generated method stub
        if (scrollState == HGridView.OnScrollListener.SCROLL_STATE_FOCUS_MOVING) {
            mIsBusy = true;
        } else if (scrollState == HGridView.OnScrollListener.SCROLL_STATE_IDLE) {
            mIsBusy = false;
        }
    }

    @Override
    public void onScroll(HGridView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
        if (!mIsBusy) {
            // We put the composed index which need to loading to this list. and check with
            // mCurrentLoadingTask soon after
            ArrayList<Integer> needToLoadComposedIndex = new ArrayList<Integer>();
            // The index of child in HGridView
            int index = 0;
            int sectionIndex = mHGridAdapter.getSectionIndex(firstVisibleItem);
            int itemCount = 0;
            for (int i = 0; i < sectionIndex; i++) {
                itemCount += mHGridAdapter.getSectionCount(i);
            }
            // The index of current section.
            int indexOfSection = firstVisibleItem - itemCount;

            while (index < visibleItemCount) {
                final ItemCollection itemCollection = mItemCollections.get(sectionIndex);
                int num_pages = itemCollection.num_pages;
                int page = indexOfSection / ItemCollection.NUM_PER_PAGE;
                if (!itemCollection.isItemReady(indexOfSection)) {
                    int composedIndex = getIndexFromSectionAndPage(sectionIndex, page);
                    needToLoadComposedIndex.add(composedIndex);
                }

                if (page < num_pages - 1) {
                    // Go to next page of this section.
                    index += (page + 1) * ItemCollection.NUM_PER_PAGE - indexOfSection;
                    indexOfSection = (page + 1) * ItemCollection.NUM_PER_PAGE;
                } else {
                    // This page is already the last page of current section.
                    index += mHGridAdapter.getSectionCount(sectionIndex) - indexOfSection;
                    indexOfSection = 0;
                    sectionIndex++;
                }
            }
            if (needToLoadComposedIndex.isEmpty()) {
                return;
            }

            // Check the composedIndex in mCurrentLoadingTask if it existed do nothing, else start a task.
            // cancel other task that not in needToLoadComposedIndex list.
//            final ConcurrentHashMap<Integer, GetItemListTask> currentLoadingTask = mCurrentLoadingTask;
//
//            for (Integer i : currentLoadingTask.keySet()) {
//                if (!needToLoadComposedIndex.contains(i)) {
//                    currentLoadingTask.get(i).cancel(true);
//                }
//            }
//
            for (int i = 0; i < needToLoadComposedIndex.size(); i++) {
                int composedIndex = needToLoadComposedIndex.get(i);
                getPackageListItem(composedIndex);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        Item item = mHGridAdapter.getItem(position);
        if (item != null) {
                PageIntent intent=new PageIntent();
                intent.toPackageDetail(this,"package",item.pk);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mIsBusy = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mIsBusy = true;
        super.onPause();
    }
}
