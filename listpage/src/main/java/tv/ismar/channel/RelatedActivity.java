package tv.ismar.channel;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.Utils.LogUtils;
import tv.ismar.adapter.RelatedAdapter;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Attribute;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.entity.Section;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.ui.ZGridView;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.app.widget.ScrollableSectionList;
import tv.ismar.listpage.R;
import tv.ismar.view.RelateScrollableSectionList;


public class RelatedActivity extends BaseActivity implements RelateScrollableSectionList.OnSectionSelectChangedListener, OnItemClickListener {

    private static final String TAG = "RelatedActivity";

    private RelateScrollableSectionList mSectionTabs;

    //private GridView mItemListGrid;
    private ZGridView mItemListGrid;

    private List<Item> mRelatedItem;

    private Item mItem;

    private SectionList mVirtualSectionList;

    private SimpleRestClient mSimpleRestClient;

    private RelatedAdapter mAdapter;

    private LoadingDialog mLoadingDialog;

    private GetRelatedTask mGetRelatedTask;
    private GetRelatedItemByInfo mGetRelatedItemByInfoTask;
    private ImageView arrow_left;
    private ImageView arrow_right;
    private HashMap<String, Object> mDataCollectionProperties = new HashMap<String, Object>();

    private String mSection;
    private boolean isPortrait = false;
    private static final int LABEL_TEXT_COLOR_FOCUSED1 = 0xffffba00;
    private static final int LABEL_TEXT_COLOR_NOFOCUSED = 0xffffffff;
    private BitmapDecoder bitmapDecoder;
    private SkyService skyService;
    private void initViews() {
        mSectionTabs = (RelateScrollableSectionList) findViewById(R.id.related_section_tabs);
        mSectionTabs.setOnSectionSelectChangeListener(this);
        if (!isPortrait) {
            mItemListGrid = (ZGridView) findViewById(R.id.related_list);
        } else {
            mItemListGrid = (ZGridView) findViewById(R.id.prelated_list);
        }
        mItemListGrid.setVisibility(View.VISIBLE);
        mItemListGrid.setOnItemClickListener(this);
        mItemListGrid.setFocusable(true);
        mItemListGrid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.i("testHGRIDVIEW", "focus");
                    if (mSectionTabs != null && mSectionTabs.sectionWhenGoto != null) {
                        mSectionTabs.currentState = ScrollableSectionList.STATE_GOTO_GRIDVIEW;
                        mSectionTabs.sectionWhenGoto.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                        mSectionTabs.sectionWhenGoto.setBackgroundResource(R.drawable.gotogridview);
                    }
                } else {
                    Log.i("testHGRIDVIEW", "lostfocus");
                    mItemListGrid.setSelection(AdapterView.INVALID_POSITION);
                    if (mSectionTabs != null)
                        mSectionTabs.currentState = ScrollableSectionList.STATE_LEAVE_GRIDVIEW;
                }
            }
        });
        arrow_left = (ImageView) findViewById(R.id.arrow_left);
        arrow_right = (ImageView) findViewById(R.id.arrow_right);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.related_view);
        final View background = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
            	background.setBackgroundDrawable(bitmapDrawable);
            }
        });
        skyService=SkyService.ServiceManager.getService();
        mSimpleRestClient = new SimpleRestClient();
        mLoadingDialog = new LoadingDialog(this,R.style.LoadingDialog);
        mLoadingDialog.setTvText(getResources().getString(R.string.loading));
        mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
        mLoadingDialog.showDialog();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            mSection = intent.getStringExtra(EventProperty.SECTION);
            try {

                mItem = (Item) bundle.getSerializable("item");
                if (mItem ==null){
                    mItem = new Gson().fromJson(intent.getStringExtra("item_json"), Item.class);
                }
                if (mItem != null) {
                    if ("movie".equals(mItem.content_model))
                        isPortrait = true;
                }
                initViews();
                Object relatedlistObj = bundle.getSerializable("related_item");
                if (relatedlistObj==null){
                    relatedlistObj = Arrays.asList(new Gson().fromJson(intent.getStringExtra("related_item_json"), Item[].class));
                }

                if (relatedlistObj != null) {
                    mRelatedItem = (List<Item>) relatedlistObj;
                }
                if (mRelatedItem == null || mRelatedItem.size() == 0) {
                //    mGetRelatedTask = new GetRelatedTask();
                //    mGetRelatedTask.execute(mItem.pk);
                    getRelatedArray(mItem.pk);
                } else {
                    initLayout();
                }
             //   DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
            } catch (Exception e) {
                e.printStackTrace();
                showToast(getResources().getString(R.string.no_related_video));
                this.finish();
            }
//            HashMap<String, Object> properties = new HashMap<String, Object>();
//            properties.put(EventProperty.ITEM, mItem.pk);
//            properties.put(EventProperty.TITLE, "relate");
//            new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RELATE_IN, properties);
        }
    }

    private void initSectionTabs() {
        mVirtualSectionList = new SectionList();
        Section firstSection = new Section();
        firstSection.title = getResources().getString(R.string.same_category_clip);
        firstSection.slug = "default";
        firstSection.count = mRelatedItem.size();
        mVirtualSectionList.add(firstSection);
        if (mItem.attributes != null && mItem.attributes.map != null) {
            Object actorInfoObj = mItem.attributes.map.get("actor");
            Object directorInfoObj = mItem.attributes.map.get("director");
            if (actorInfoObj != null) {
                Attribute.Info[] actorInfo = (Attribute.Info[]) actorInfoObj;
                for (Attribute.Info actor : actorInfo) {
                    Section actorSection = new Section();
                    actorSection.title = actor.name;
                    actorSection.slug = "actor";
                    actorSection.template = actor.id;
                    actorSection.count = 1;
                    mVirtualSectionList.add(actorSection);
                }
            }
            if (directorInfoObj != null) {
                Attribute.Info[] directorInfo = (Attribute.Info[]) directorInfoObj;
                for (Attribute.Info director : directorInfo) {
                    Section directionSection = new Section();
                    directionSection.title = director.name;
                    directionSection.slug = "director";
                    directionSection.template = director.id;
                    directionSection.count = 1;
                    mVirtualSectionList.add(directionSection);
                }
            }
        }
    }

    class GetRelatedTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                int pk = params[0];
                Item[] relatedArray = mSimpleRestClient.getRelatedItem("/api/tv/relate/" + pk + "/");
                if (relatedArray != null && relatedArray.length > 0) {
                    mRelatedItem = new ArrayList<Item>(Arrays.asList(relatedArray));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mRelatedItem != null && mRelatedItem.size() > 0) {
                try {
                    initLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showToast(getResources().getString(R.string.no_related_video));
                RelatedActivity.this.finish();
            }
        }

    }
    private void getRelatedArray(Integer pk){
        skyService.getRelatedArray(pk).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Item[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(Item[] items) {
                        mLoadingDialog.dismiss();
                        if (items != null && items.length > 0) {
                            mRelatedItem = new ArrayList<Item>(Arrays.asList(items));
                        }
                        if (mRelatedItem != null && mRelatedItem.size() > 0) {
                            try {
                                initLayout();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            showToast(getResources().getString(R.string.no_related_video));
                            RelatedActivity.this.finish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.loadException("channel ","related ","","getRelatedArray",0,"","","server",e.toString());
                        super.onError(e);
                        mLoadingDialog.dismiss();
                    }
                });
    }
    private void initLayout() {
        // Data collection.
        mDataCollectionProperties.put(EventProperty.ITEM, mItem.pk);
        mDataCollectionProperties.put(EventProperty.TITLE, mItem.title);
//        if (mItem.clip != null) {
//            mDataCollectionProperties.put(EventProperty.CLIP, mItem.clip.pk);
//        } else {
//            mDataCollectionProperties.put(EventProperty.CLIP, "");
//        }
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RELATE_IN, mDataCollectionProperties);

        initSectionTabs();
        SectionList mTmpSectionList = new SectionList();

        for (Section s : mVirtualSectionList) {
            if (s.count != 0) {
                mTmpSectionList.add(s);
            }
        }
        //mVirtualSectionList = mTmpSectionList;
//		if(mVirtualSectionList.size()>5)
//			arrow_right.setVisibility(View.VISIBLE);
//		else{
//			mSectionTabs.left = null;
//			mSectionTabs.right = null;
//		}
        mSectionTabs.init(mVirtualSectionList, getResources().getDimensionPixelSize(R.dimen.gridview_channel_section_tabs_width), true);
        buildGridView();
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private void buildGridView() {
        mAdapter = new RelatedAdapter(this, mRelatedItem, isPortrait);
        //mAdapter.setList(mRelatedItem);
        mItemListGrid.setAdapter(mAdapter);
        //	mItemListGrid.setNumColumns(4);
        mItemListGrid.setFocusable(true);
        //	mItemListGrid.setHorizontalFadingEdgeEnabled(true);
        //	mItemListGrid.setFadingEdgeLength(144);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        final HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(mDataCollectionProperties);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RELATE_OUT, properties);
        mDataCollectionProperties.remove(EventProperty.TO_ITEM);
        mDataCollectionProperties.remove(EventProperty.TO_TITLE);
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if(bitmapDecoder != null && bitmapDecoder.isAlive()){
        	bitmapDecoder.interrupt();
        }
        if (mGetRelatedTask != null && mGetRelatedTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetRelatedTask.cancel(true);
        }
        if (mGetRelatedItemByInfoTask != null && mGetRelatedItemByInfoTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetRelatedItemByInfoTask.cancel(true);
        }
        mGetRelatedTask = null;
        mAdapter = null;
        mVirtualSectionList = null;
        mRelatedItem = null;
        mSectionTabs = null;
        mSimpleRestClient = null;
        mLoadingDialog = null;
      //  DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        super.onDestroy();
    }

    @Override
    public void onSectionSelectChanged(int index) {
        Section section = mVirtualSectionList.get(index);
        if (mGetRelatedItemByInfoTask != null && mGetRelatedItemByInfoTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetRelatedItemByInfoTask.cancel(true);
        }
//        mGetRelatedItemByInfoTask = new GetRelatedItemByInfo();
//        mGetRelatedItemByInfoTask.execute(section);
        getRelatedItemByInfo(section);
    }

    class GetRelatedItemByInfo extends AsyncTask<Section, Void, Void> {

        @Override
        protected Void doInBackground(Section... params) {
//            try {
//                Section section = params[0];
//                String url = null;
//                if (section.slug.equals("default")) {
//                    url = "/api/tv/relate/" + mItem.pk + "/";
//                    Item[] itemArray = mSimpleRestClient.getRelatedItem(url);
//                    if (itemArray != null) {
//                        mRelatedItem = new ArrayList<Item>(Arrays.asList(itemArray));
//                    }
//                } else {
//                    url = mSimpleRestClient.root_url + "/api/tv/filtrate/$" + mItem.content_model + "/" + section.slug + "*" + section.template + "/";
//                    ItemList itemList = mSimpleRestClient.getItemList(url);
//                    if (itemList != null) {
//                        mRelatedItem = itemList.objects;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mRelatedItem != null) {
                mAdapter.cancel();
                buildGridView();
            }
        }

    }
    private void getRelatedItemByInfo(Section section){
        if (section.slug.equals("default")) {
            skyService.getRelatedArray(mItem.pk).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<Item[]>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(Item[] items) {
                            if (items != null) {
                                mRelatedItem = new ArrayList<Item>(Arrays.asList(items));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                        }
                    });
        } else {
            skyService.getRelatedItemByInfo(mItem.content_model,section.slug,section.template ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new BaseObserver<ItemList>() {
                @Override
                public void onCompleted() {

                }
                @Override
                public void onNext(ItemList itemList) {
                    if (itemList != null) {
                        mRelatedItem = itemList.objects;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Item item = mAdapter.getItem(position);
        mDataCollectionProperties.put(EventProperty.TO_ITEM, item.pk);
        mDataCollectionProperties.put(EventProperty.TO_TITLE, item.title);
//        if (item.clip != null) {
//            mDataCollectionProperties.put(EventProperty.CLIP, item.clip);
//        } else {
//            mDataCollectionProperties.put(EventProperty.CLIP, "");
//        }
//		Intent intent = new Intent("tv.ismar.daisy.Item");
//		intent.putExtra("url", item.item_url);
//        intent.putExtra(EventProperty.SECTION, mSection);
//		startActivity(intent);
        PageIntent pageIntent=new PageIntent();
        boolean[] isSubItem = new boolean[1];
        int pk = SimpleRestClient.getItemId(item.item_url, isSubItem);
        if (item.is_complex) {
            if (item.expense != null && (item.content_model.equals("variety") || item.content_model.equals("entertainment"))) {
                item.content_model = "music";
            }
            pageIntent.toDetailPage(RelatedActivity.this,"related",pk);
        } else {
            pageIntent.toPlayPage(RelatedActivity.this,pk,0, Source.LIST);
        }
    }


    private OnCancelListener mLoadingCancelListener = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            RelatedActivity.this.finish();
        }
    };

    private void showToast(String text) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.simple_toast, (ViewGroup) findViewById(R.id.simple_toast_root));
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}



