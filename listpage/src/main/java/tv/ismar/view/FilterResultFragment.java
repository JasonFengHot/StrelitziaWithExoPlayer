package tv.ismar.view;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.Utils.LogUtils;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemCollection;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.exception.ItemOfflineException;
import tv.ismar.app.exception.NetworkException;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.adapter.HGridFilterAdapterImpl;
import tv.ismar.app.ui.view.AlertDialogFragment;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.listpage.R;

/** Created by zhangjiqiang on 15-6-23. */
public class FilterResultFragment extends BackHandledFragment
        implements AdapterView.OnItemSelectedListener,
                AdapterView.OnItemClickListener,
                HGridView.OnScrollListener {

    private static final int NODATA = -1;
    public String mChannel;
    public String content_model;
    public String conditions;
    public String filterCondition;
    public boolean isPortrait = false;
    private View fragmentView;
    private LoadingDialog mLoadingDialog;
    private SimpleRestClient mRestClient;
    private ArrayList<ItemCollection> mItemCollections;
    private HGridView mHGridView;
    private HGridFilterAdapterImpl mHGridAdapter;
    private ItemList items;
    private boolean mIsBusy = false;
    private boolean isInitTaskLoading;
    private String url;
    private InitItemTask mInitTask;
    private ConcurrentHashMap<Integer, GetItemListTask> mCurrentLoadingTask =
            new ConcurrentHashMap<Integer, GetItemListTask>();
    private ProgressBar percentageBar;
    private boolean isNoData = false;
    private Button left_shadow;
    private Button right_shadow;
    private float rate;
    private SkyService skyService;
    private DialogInterface.OnCancelListener mLoadingCancelListener =
            new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    getFragmentManager().popBackStack();
                    dialog.dismiss();
                }
            };
    private View.OnHoverListener onHoverListener =
            new View.OnHoverListener() {

                @Override
                public boolean onHover(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_HOVER_ENTER:
                        case MotionEvent.ACTION_HOVER_MOVE:
                            v.setFocusable(true);
                            v.setFocusableInTouchMode(true);
                            v.requestFocus();
                            break;
                        case MotionEvent.ACTION_HOVER_EXIT:
                            break;
                    }
                    return false;
                }
            };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rate = DaisyUtils.getVodApplication(getActivity()).getRate(getActivity());
        if (fragmentView == null) {
            mLoadingDialog = new LoadingDialog(getActivity(), R.style.LoadingDialog);
            mLoadingDialog.setTvText(getResources().getString(R.string.loading));
            mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
            mRestClient = new SimpleRestClient();

            if (!isPortrait)
                fragmentView = inflater.inflate(R.layout.filter_result_list_view, container, false);
            else
                fragmentView =
                        inflater.inflate(
                                R.layout.filter_portraitresult_list_view, container, false);
            skyService = SkyService.ServiceManager.getService();
            doFilterRequest();
        }
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(EventProperty.TITLE, "shaixuanjiegou");
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_FILTER, properties);
        return fragmentView;
    }

    private void initView(View fragmentView, boolean hasResult) {
        if (hasResult) {
            percentageBar = (ProgressBar) fragmentView.findViewById(R.id.filter_percentage);
            LinearLayout layout =
                    (LinearLayout) fragmentView.findViewById(R.id.filter_condition_layout);
            buildFilterListView(layout, conditions);
            mHGridView = (HGridView) fragmentView.findViewById(R.id.filter_grid);
            left_shadow = (Button) fragmentView.findViewById(R.id.left_shadow);
            right_shadow = (Button) fragmentView.findViewById(R.id.right_shadow);
            if (!isPortrait) {
                mHGridView.list_offset = 38;
            } else {
                mHGridView.list_offset = 15;
            }
        } else {
            left_shadow = (Button) fragmentView.findViewById(R.id.recommend_left_shadow);
            right_shadow = (Button) fragmentView.findViewById(R.id.recommend_right_shadow);
            View noresult_layout = fragmentView.findViewById(R.id.noresult_layout);
            View result_layout = fragmentView.findViewById(R.id.result_layout);
            noresult_layout.setVisibility(View.VISIBLE);
            result_layout.setVisibility(View.GONE);
            percentageBar =
                    (ProgressBar) fragmentView.findViewById(R.id.recommend_filter_percentage);
            mHGridView = (HGridView) fragmentView.findViewById(R.id.recommend_filter_grid);
            Button filterBtn = (Button) fragmentView.findViewById(R.id.refilter_btn);
            filterBtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getFragmentManager().popBackStack();
                        }
                    });
            filterBtn.setOnHoverListener(onHoverListener);
            isNoData = true;
            doFilterRequest();
            if (isPortrait) {
                mHGridView.list_offset = 18;
            } else {
                mHGridView.list_offset = 1;
            }
        }
        mHGridView.portraitflg = isPortrait;
        mHGridView.leftbtn = left_shadow;
        mHGridView.rightbtn = right_shadow;
        mHGridView.setOnItemClickListener(this);
        mHGridView.setOnItemSelectedListener(this);
        mHGridView.setOnScrollListener(this);
        right_shadow.setOnHoverListener(onHoverListener);
        left_shadow.setOnHoverListener(onHoverListener);
        right_shadow.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            v.setBackgroundResource(R.drawable.scroll_right_focus);
                        } else {
                            v.setBackgroundResource(R.drawable.scroll_right_normal);
                        }
                    }
                });

        left_shadow.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            v.setBackgroundResource(R.drawable.scroll_left_focus);
                        } else {
                            v.setBackgroundResource(R.drawable.scroll_left_normal);
                        }
                    }
                });

        right_shadow.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mHGridView.pageScroll(View.FOCUS_RIGHT);
                        mHGridView.setFocusableInTouchMode(true);
                        mHGridView.setFocusable(true);
                    }
                });
        left_shadow.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mHGridView.pageScroll(View.FOCUS_LEFT);
                        mHGridView.setFocusableInTouchMode(true);
                        mHGridView.setFocusable(true);
                    }
                });
    }

    private void buildFilterListView(ViewGroup container, String str) {
        if (str.length() > 0) {
            String[] labels = str.split("!");
            int count = labels.length;
            for (int i = 0; i < count; i++) {
                TextView label = new TextView(getActivity());

                label.setText(labels[i]);
                label.setTextColor(0xffffffff);
                label.setBackgroundResource(R.drawable.filter_btn_focused);
                label.setTextSize(30 / rate);
                label.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                (int)
                                        (getResources()
                                                        .getDimensionPixelSize(
                                                                R.dimen.filter_pro_hgride_text_W)
                                                / 1),
                                (int)
                                        (getResources()
                                                        .getDimensionPixelSize(
                                                                R.dimen.filter_pro_hgride_text_H)
                                                / 1));
                params.gravity = Gravity.CENTER_VERTICAL;
                params.rightMargin =
                        (int)
                                (getResources()
                                                .getDimensionPixelSize(
                                                        R.dimen.filter_pro_hgride_text_MR)
                                        / 1);
                // params.topMargin = 11;
                container.addView(label, params);
            }
        } else {
            TextView label = new TextView(getActivity());

            label.setText("不限");
            label.setTextColor(0xffffffff);
            label.setBackgroundResource(R.drawable.filter_btn_focused);
            label.setTextSize(30 / rate);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            (int)
                                    (getResources()
                                                    .getDimensionPixelSize(
                                                            R.dimen.filter_pro_hgride_text_W)
                                            / 1),
                            (int)
                                    (getResources()
                                                    .getDimensionPixelSize(
                                                            R.dimen.filter_pro_hgride_text_H)
                                            / 1));
            params.gravity = Gravity.CENTER_VERTICAL;
            params.rightMargin =
                    (int)
                            (getResources().getDimensionPixelSize(R.dimen.filter_pro_hgride_text_MR)
                                    / 1);
            // params.topMargin = 11;
            container.addView(label, params);
        }
    }

    @Override
    public void onPause() {
        mIsBusy = true;
        if (mHGridAdapter != null) {
            mHGridAdapter.cancel();
        }

        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        final ConcurrentHashMap<Integer, GetItemListTask> currentLoadingTask = mCurrentLoadingTask;
        for (Integer index : currentLoadingTask.keySet()) {
            currentLoadingTask.get(index).cancel(true);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        mIsBusy = false;
        super.onResume();
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

    public void getItemList(final Integer index) {
        int[] sectionAndPage = getSectionAndPageFromIndex(index);
        int page = sectionAndPage[1] + 1;
        if (!isNoData) {
            skyService
                    .getFilterRequestHaveData(content_model, filterCondition, page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            ((BaseActivity) getActivity()).new BaseObserver<ItemList>() {
                                @Override
                                public void onCompleted() {}

                                @Override
                                public void onNext(ItemList itemList) {
                                    if (itemList != null && itemList.objects != null) {
                                        mLoadingDialog.dismiss();
                                        int sectionIndex = getSectionAndPageFromIndex(index)[0];
                                        int page = getSectionAndPageFromIndex(index)[1];
                                        ItemCollection itemCollection =
                                                mItemCollections.get(sectionIndex);
                                        itemCollection.fillItems(page, itemList.objects);
                                        mHGridAdapter.setList(mItemCollections);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    LogUtils.loadException(
                                            "channel ",
                                            "result ",
                                            "",
                                            "getItemList",
                                            0,
                                            "",
                                            "",
                                            "server",
                                            e.toString());
                                    super.onError(e);
                                }
                            });
        } else {
            skyService
                    .getFilterRequestNodata(
                            "movie", "area*10022$10261$10263$10378$10479$10483$10484$10494", 1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            ((BaseActivity) getActivity()).new BaseObserver<ItemList>() {
                                @Override
                                public void onCompleted() {}

                                @Override
                                public void onNext(ItemList itemList) {
                                    if (itemList != null && itemList.objects != null) {
                                        int sectionIndex = getSectionAndPageFromIndex(index)[0];
                                        int page = getSectionAndPageFromIndex(index)[1];
                                        ItemCollection itemCollection =
                                                mItemCollections.get(sectionIndex);
                                        itemCollection.fillItems(page, itemList.objects);
                                        mHGridAdapter.setList(mItemCollections);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    LogUtils.loadException(
                                            "channel ",
                                            "result ",
                                            "",
                                            "getItemList",
                                            0,
                                            "",
                                            "",
                                            "server",
                                            e.toString());
                                    super.onError(e);
                                }
                            });
        }
    }

    private void doFilterRequest() {
        if (!isNoData) {
            skyService
                    .getFilterRequest(content_model, filterCondition)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            ((BaseActivity) getActivity()).new BaseObserver<ItemList>() {
                                @Override
                                public void onCompleted() {}

                                @Override
                                public void onNext(ItemList itemList) {
                                    items = itemList;
                                    try {
                                        if (items != null && items.count > 0) {
                                            mItemCollections = new ArrayList<ItemCollection>();
                                            int num_pages =
                                                    (int)
                                                            Math.ceil(
                                                                    (float) items.count
                                                                            / (float)
                                                                                    ItemCollection
                                                                                            .NUM_PER_PAGE);
                                            ItemCollection itemCollection =
                                                    new ItemCollection(
                                                            num_pages, items.count, "1", "1");
                                            mItemCollections.add(itemCollection);
                                            initView(fragmentView, true);
                                        } else {
                                            initView(fragmentView, false);
                                        }
                                        if (items != null && mItemCollections != null) {
                                            mHGridAdapter =
                                                    new HGridFilterAdapterImpl(
                                                            getActivity(), mItemCollections, false);
                                            mHGridAdapter.setIsPortrait(isPortrait);
                                            mHGridAdapter.setList(mItemCollections);
                                            if (mHGridAdapter.getCount() > 0) {
                                                mHGridView.setAdapter(mHGridAdapter);
                                                mHGridView.setFocusable(true);
                                                //
                                                // mHGridView.setHorizontalFadingEdgeEnabled(true);
                                                // mHGridView.setFadingEdgeLength(144);
                                                mItemCollections.get(0).fillItems(0, items.objects);
                                                mHGridAdapter.setList(mItemCollections);
                                            }
                                        }
                                    } catch (Exception e) {
                                        LogUtils.loadException(
                                                "channel ",
                                                "result ",
                                                "",
                                                "getItemList",
                                                0,
                                                "",
                                                "",
                                                "client",
                                                e.toString());

                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    LogUtils.loadException(
                                            "channel ",
                                            "result ",
                                            "",
                                            "getItemList",
                                            0,
                                            "",
                                            "",
                                            "server",
                                            e.toString());
                                    super.onError(e);
                                }
                            });
        } else {
            String url = "area*10022$10261$10263$10378$10479$10483$10484$10494";
            skyService
                    .getFilterRequestNodata("movie", url, 1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            ((BaseActivity) getActivity()).new BaseObserver<ItemList>() {
                                @Override
                                public void onCompleted() {}

                                @Override
                                public void onNext(ItemList itemList) {
                                    items = itemList;
                                    try {
                                        if (items != null && items.count > 0) {
                                            mItemCollections = new ArrayList<ItemCollection>();
                                            int num_pages =
                                                    (int)
                                                            Math.ceil(
                                                                    (float) items.count
                                                                            / (float)
                                                                                    ItemCollection
                                                                                            .NUM_PER_PAGE);
                                            ItemCollection itemCollection =
                                                    new ItemCollection(
                                                            num_pages, items.count, "1", "1");
                                            mItemCollections.add(itemCollection);
                                            mHGridAdapter =
                                                    new HGridFilterAdapterImpl(
                                                            getActivity(), mItemCollections, false);
                                            mHGridAdapter.setIsPortrait(isPortrait);
                                            mHGridAdapter.setList(mItemCollections);
                                            if (mHGridAdapter.getCount() > 0) {
                                                mHGridView.setAdapter(mHGridAdapter);
                                                mHGridView.setFocusable(true);
                                                //
                                                // mHGridView.setHorizontalFadingEdgeEnabled(true);
                                                // mHGridView.setFadingEdgeLength(144);
                                                mItemCollections.get(0).fillItems(0, items.objects);
                                                mHGridAdapter.setList(mItemCollections);
                                            }
                                        }
                                    } catch (Exception e) {
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    super.onError(e);
                                }
                            });
        }
    }

    public void showDialog() {
        AlertDialogFragment newFragment =
                AlertDialogFragment.newInstance(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG);
        newFragment.setPositiveListener(
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        doFilterRequest();
                        dialog.dismiss();
                    }
                });
        newFragment.setNegativeListener(
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getFragmentManager().popBackStack();
                        dialog.dismiss();
                    }
                });
        FragmentManager manager = getFragmentManager();

        if (manager != null) {
            newFragment.show(manager, "dialog");
        }
    }

    @Override
    public void onDestroyView() {
        if (mInitTask != null && mInitTask.getStatus() != AsyncTask.Status.FINISHED) {
            mInitTask.cancel(true);
        }
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Item item = mHGridAdapter.getItem(position);
        if (item != null) {
            boolean[] isSubItem = new boolean[1];
            int id = SimpleRestClient.getItemId(item.url, isSubItem);
            PageIntent pageIntent = new PageIntent();
            if (item.is_complex) {
                pageIntent.toDetailPage(getActivity(), "retrieval", id);
            } else {
                pageIntent.toPlayPage(getActivity(), id, 0, Source.LIST);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        int sectionIndex = mHGridAdapter.getSectionIndex(position);
        int rows = mHGridView.getRows();
        int itemCount = 0;
        for (int i = 0; i < sectionIndex; i++) {
            itemCount += mHGridAdapter.getSectionCount(i);
        }
        int columnOfX = (position - itemCount) / rows + 1;
        int totalColumnOfSectionX =
                (int)
                        (Math.ceil(
                                (float) mHGridAdapter.getSectionCount(sectionIndex)
                                        / (float) rows));
        int percentage = (int) ((float) columnOfX / (float) totalColumnOfSectionX * 100f);
        Log.i("asdfghjkl", "percentage==" + percentage);
        // percentageBar.setProgressDrawable(getResources().getDrawable(R.drawable.section_percentage_selected));
        percentageBar.setProgress(percentage);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    @Override
    public void onScrollStateChanged(HGridView view, int scrollState) {
        if (scrollState == HGridView.OnScrollListener.SCROLL_STATE_FOCUS_MOVING) {
            mIsBusy = true;
        } else if (scrollState == HGridView.OnScrollListener.SCROLL_STATE_IDLE) {
            mIsBusy = false;
        }
    }

    @Override
    public void onScroll(
            HGridView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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

            // Check the composedIndex in mCurrentLoadingTask if it existed do nothing, else start a
            // task.
            // cancel other task that not in needToLoadComposedIndex list.
            final ConcurrentHashMap<Integer, GetItemListTask> currentLoadingTask =
                    mCurrentLoadingTask;

            for (Integer i : currentLoadingTask.keySet()) {
                if (!needToLoadComposedIndex.contains(i)) {
                    currentLoadingTask.get(i).cancel(true);
                }
            }

            for (int i = 0; i < needToLoadComposedIndex.size(); i++) {
                int composedIndex = needToLoadComposedIndex.get(i);
                if (!currentLoadingTask.containsKey(composedIndex)) {
                    //  new GetItemListTask().execute(composedIndex);
                    getItemList(composedIndex);
                }
            }
        }
    }

    class InitItemTask extends AsyncTask<Void, Void, Integer> {

        private static final int RESULT_NETWORKEXCEPTION = -1;
        private static final int RESUTL_CANCELED = -2;
        private static final int RESULT_SUCCESS = 0;

        @Override
        protected void onPreExecute() {
            if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
            isInitTaskLoading = true;
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                items = mRestClient.getItemList(url);
                if (items != null && items.count > 0) {
                    mItemCollections = new ArrayList<ItemCollection>();
                    int num_pages =
                            (int)
                                    Math.ceil(
                                            (float) items.count
                                                    / (float) ItemCollection.NUM_PER_PAGE);
                    ItemCollection itemCollection =
                            new ItemCollection(num_pages, items.count, "1", "1");
                    mItemCollections.add(itemCollection);
                } else {
                    return NODATA;
                }
                if (isCancelled()) {
                    return RESUTL_CANCELED;
                } else {
                    return RESULT_SUCCESS;
                }
            } catch (NetworkException e) {
                e.printStackTrace();
                return RESULT_NETWORKEXCEPTION;
            } catch (ItemOfflineException e) {
                e.printStackTrace();
                return RESULT_NETWORKEXCEPTION;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
            isInitTaskLoading = false;
            if (result == NODATA) {
                // 无数据
                initView(fragmentView, false);
                cancel(true);
                return;
            } else if (result == RESULT_SUCCESS && !isNoData) {
                initView(fragmentView, true);
            }
            try {
                if (items != null) {
                    mHGridAdapter =
                            new HGridFilterAdapterImpl(getActivity(), mItemCollections, false);
                    mHGridAdapter.setIsPortrait(isPortrait);
                    mHGridAdapter.setList(mItemCollections);
                    if (mHGridAdapter.getCount() > 0) {
                        mHGridView.setAdapter(mHGridAdapter);
                        mHGridView.setFocusable(true);
                        //   mHGridView.setHorizontalFadingEdgeEnabled(true);
                        // mHGridView.setFadingEdgeLength(144);
                        mItemCollections.get(0).fillItems(0, items.objects);
                        mHGridAdapter.setList(mItemCollections);
                    }

                } else {
                    showDialog();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class GetItemListTask extends AsyncTask<Object, Void, ItemList> {
        private Integer index;

        @Override
        protected void onCancelled() {
            mCurrentLoadingTask.remove(index);
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ItemList doInBackground(Object... params) {
            try {
                index = (Integer) params[0];

                mCurrentLoadingTask.put(index, this);
                int[] sectionAndPage = getSectionAndPageFromIndex(index);
                int page = sectionAndPage[1] + 1;
                String url;
                if (!isNoData)
                    url =
                            SimpleRestClient.root_url
                                    + "/api/tv/filtrate/"
                                    + "$"
                                    + content_model
                                    + "/"
                                    + filterCondition
                                    + "/"
                                    + page;
                else
                    url =
                            SimpleRestClient.root_url
                                    + "/api/tv/filtrate/"
                                    + "$"
                                    + "movie"
                                    + "/"
                                    + "area*10022$10261$10263$10378$10479$10483$10484$10494/"
                                    + page;

                ItemList itemList = mRestClient.getItemList(url);
                if (isCancelled()) {
                    return null;
                } else {
                    return itemList;
                }
            } catch (Exception e) {

                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ItemList itemList) {
            mCurrentLoadingTask.remove(index);
            if (itemList != null && itemList.objects != null) {
                int sectionIndex = getSectionAndPageFromIndex(index)[0];
                int page = getSectionAndPageFromIndex(index)[1];
                ItemCollection itemCollection = mItemCollections.get(sectionIndex);
                itemCollection.fillItems(page, itemList.objects);
                mHGridAdapter.setList(mItemCollections);
            } else {
                showDialog();
            }
        }
    }
}
