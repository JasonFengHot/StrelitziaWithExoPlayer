package tv.ismar.searchpage;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.models.ActorRelateRequestParams;
import tv.ismar.app.models.AttributesEntity;
import tv.ismar.app.models.PersonEntitiy;
import tv.ismar.app.models.SearchItemCollection;
import tv.ismar.app.models.SemanticSearchResponseEntity;
import tv.ismar.app.models.SemantichObjectEntity;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.adapter.HGridSearchAdapterImpl;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.searchpage.weight.MyDialog;

/**
 * Created by huaijie on 2/22/16.
 */
public class FilmStarActivity extends BaseActivity implements OnFocusChangeListener, View.OnClickListener, OnHoverListener{
    private static final String TAG = "FilmStarActivity";


    private long pk;
    private TextView filmStartitle;
    private LinearLayout indicatorListLayout;
    private HGridView vodHorizontalScrollView;

    private ImageView contentArrowLeft;
    private ImageView contentArrowRight;

    private TextView actorView;
    private TextView directorView;
    private TextView areaView;
    private TextView descriptionView;
    private ImageView indicatorArrowLeft;
    private ImageView indicatorArrowRight;


    //    private MessagePopWindow networkEorrorPopupWindow;
    private ViewGroup contentView;
    private View indicatorSelectedView;

    private TvHorizontalScrollView horizontalScrollView;
    private ImageView dividerLine;
    private ImageView focusTranslate;

//    private View currentFocuedIndicatorView;

    private ImageView mContentBackgroundView;
    private MyDialog errorDialog;
    private String title;
    private HGridSearchAdapterImpl searchAdapter;
    private ArrayList<SemantichObjectEntity> vodList;
    private boolean mIsBusy = false;
    private ArrayList<SearchItemCollection> mItemCollections;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ScreenManager.getScreenManager().pushActivity(this);
//        networkEorrorPopupWindow = new MessagePopWindow(this, "网络异常，请检查网络", null);
        contentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_filmstar, null);
        setContentView(contentView);

        initViews();
        Intent intent = getIntent();
        pk = intent.getLongExtra("pk", 0);
//        pk = 2857;
        title = intent.getStringExtra("title");
//        String title = "刘德华";
        filmStartitle.setText(title);
        fetchPersonBG(String.valueOf(pk));
        fetchActorRelate(pk);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsBusy = false;
    }


    private void initViews() {
        filmStartitle = (TextView) findViewById(R.id.film_star_title_new);
        indicatorListLayout = (LinearLayout) findViewById(R.id.film_list_indicator_new);
        focusTranslate = (ImageView) findViewById(R.id.focus_translate_new);

        focusTranslate.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.i(TAG, "focusTranslate  focused");
                    if (indicatorSelectedView != null) {
                        indicatorSelectedView.requestFocusFromTouch();
                        indicatorSelectedView.requestFocus();
                    } else {
                        indicatorListLayout.getChildAt(0).requestFocusFromTouch();
                        indicatorListLayout.getChildAt(0).requestFocus();
                    }
                }
            }
        });

        actorView = (TextView) findViewById(R.id.actor_new);
        directorView = (TextView) findViewById(R.id.director_new);
        areaView = (TextView) findViewById(R.id.area_new);
        descriptionView = (TextView) findViewById(R.id.description_new);
        contentArrowLeft = (ImageView) findViewById(R.id.content_arrow_left_new);
        contentArrowRight = (ImageView) findViewById(R.id.content_arrow_right_new);
        indicatorArrowLeft = (ImageView) findViewById(R.id.indicator_left_new);
        indicatorArrowRight = (ImageView) findViewById(R.id.indicator_right_new);
        horizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.scrollview_new);
        vodHorizontalScrollView = (HGridView) findViewById(R.id.vod_scrollview_new);
        dividerLine = (ImageView) findViewById(R.id.divider_line_new);
        mContentBackgroundView = (ImageView) findViewById(R.id.content_bg_new);
        contentArrowRight.setOnFocusChangeListener(this);
        contentArrowLeft.setOnFocusChangeListener(this);
        indicatorArrowLeft.setOnFocusChangeListener(this);
        indicatorArrowRight.setOnFocusChangeListener(this);
        indicatorArrowLeft.setOnHoverListener(this);
        indicatorArrowRight.setOnHoverListener(this);
        contentArrowRight.setOnHoverListener(this);
        contentArrowLeft.setOnHoverListener(this);
        horizontalScrollView.setCoverOffset(25);
        horizontalScrollView.setLeftArrow(indicatorArrowLeft);
        horizontalScrollView.setRightArrow(indicatorArrowRight);
        indicatorListLayout.setNextFocusDownId(R.id.vod_scrollview_new);
        vodHorizontalScrollView.leftbtn=contentArrowLeft;
        vodHorizontalScrollView.rightbtn=contentArrowRight;

        indicatorArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                horizontalScrollView.pageScroll(View.FOCUS_LEFT);
            }
        });
        indicatorArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                horizontalScrollView.pageScroll(View.FOCUS_RIGHT);
            }
        });
        contentArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vodHorizontalScrollView.pageScroll(View.FOCUS_LEFT);
            }
        });
        contentArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vodHorizontalScrollView.setSelection(vodHorizontalScrollView.getFirstPosition()+4);
                vodHorizontalScrollView.pageScroll(View.FOCUS_RIGHT);
            }
        });
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        vodHorizontalScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    PageIntent pageIntent=new PageIntent();
                    SemantichObjectEntity semantichObjectEntity= vodList.get(position);
                    String contentModel =semantichObjectEntity.getContent_model();
                    String itemTitle=semantichObjectEntity.getTitle();
                    long pk=Long.valueOf(semantichObjectEntity.getPk());
                    if(contentModel.equals("music")||(contentModel.equals("sport")&&semantichObjectEntity.getExpense()==null)||contentModel.equals("game")){
                        pageIntent.toPlayPage(FilmStarActivity.this, (int) pk,0,Source.SEARCH);
                    }else if("person".equals(contentModel)){
                        pageIntent.toFilmStar(FilmStarActivity.this,itemTitle,pk);
                    }else{
                        pageIntent.toDetailPage(FilmStarActivity.this, Source.SEARCH.getValue(), (int) pk);
                    }
                    baseSection="";
                    baseChannel=contentModel.equals("person")?"star":contentModel;
                    JasmineUtil.video_search_arrive(title,contentModel.equals("person")?"star":contentModel, (int) pk,0,itemTitle);
            }
        });

        vodHorizontalScrollView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    SemantichObjectEntity entity =vodList.get(position);
                    AttributesEntity attributesEntity = entity.getAttributes();
                    String description = entity.getDescription();
                    setFilmAttr(attributesEntity, description);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        vodHorizontalScrollView.setOnScrollListener(new HGridView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(HGridView view, int scrollState) {
                if(scrollState== HGridView.OnScrollListener.SCROLL_STATE_FOCUS_MOVING) {
                    mIsBusy = true;
                } else if(scrollState == HGridView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mIsBusy = false;
                }
            }

            @Override
            public void onScroll(HGridView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(!mIsBusy) {
                    // We put the composed index which need to loading to this list. and check with
                    // mCurrentLoadingTask soon after
                    ArrayList<Integer> needToLoadComposedIndex = new ArrayList<Integer>();
                    // The index of child in HGridView
                    int index = 0;
                    int sectionIndex = searchAdapter.getSectionIndex(firstVisibleItem);
                    int itemCount = 0;
                    for(int i=0; i < sectionIndex;i++) {
                        itemCount += searchAdapter.getSectionCount(i);
                    }
                    // The index of current section.
                    int indexOfSection = firstVisibleItem - itemCount;

                    while(index < visibleItemCount) {
                        final SearchItemCollection itemCollection = mItemCollections.get(sectionIndex);
                        int num_pages = itemCollection.num_pages;
                        int page = indexOfSection / SearchItemCollection.NUM_PER_PAGE;
                        if(!itemCollection.isItemReady(indexOfSection)) {
                            int composedIndex = getIndexFromSectionAndPage(sectionIndex, page);
                            needToLoadComposedIndex.add(composedIndex);
                        }

                        if(page<num_pages - 1) {
                            // Go to next page of this section.
                            index += (page+1) * SearchItemCollection.NUM_PER_PAGE - indexOfSection;
                            indexOfSection = (page + 1) * SearchItemCollection.NUM_PER_PAGE;
                        } else {
                            // This page is already the last page of current section.
                            index += searchAdapter.getSectionCount(sectionIndex) - indexOfSection;
                            indexOfSection = 0;
                            sectionIndex++;
                        }
                    }
                }
            }
        });

    }

    private void fetchPersonBG(final String personId) {
        mSkyService.apiFetchPersonBG(personId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PersonEntitiy>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(PersonEntitiy personEntitiy) {
                        if(personEntitiy==null){
                            JasmineUtil.loadException("search","","","",0, IsmartvActivator.getInstance().getApiDomain()+"api/person/"+personId+"/", DeviceUtils.getVersionCode(FilmStarActivity.this),"data","");
                            return;
                        }
                        setPersonBG(personEntitiy.getImage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        JasmineUtil.loadException("search","","","",0, IsmartvActivator.getInstance().getApiDomain()+"api/person/"+personId+"/", DeviceUtils.getVersionCode(FilmStarActivity.this),"server",e.getMessage());
                        super.onError(e);
                    }
                });

    }

    private void setPersonBG(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {

        } else {
            Picasso.with(this).load(imageUrl).into(mContentBackgroundView);
        }
    }

    private void fetchActorRelate(final long pk) {
        ActorRelateRequestParams params = new ActorRelateRequestParams();
        params.setActor_id(pk);
        params.setPage_no(1);
        params.setPage_count(100);
        mSkyService.apiFetchActorRelate(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<SemanticSearchResponseEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(SemanticSearchResponseEntity semanticSearchResponseEntity) {
                            if(semanticSearchResponseEntity==null){
                               JasmineUtil.loadException("search","","","", (int) pk, IsmartvActivator.getInstance().getApiDomain()+"api/tv/actorrelate/", DeviceUtils.getVersionCode(FilmStarActivity.this),"data","");
                               return;
                            }
                            SemanticSearchResponseEntity entity = semanticSearchResponseEntity;
                            indicatorListLayout.removeAllViews();
                            horizontalScrollView.scrollTo(0, 0);
                            int i = 0;
                            for (SemanticSearchResponseEntity.Facet facet : entity.getFacet()) {
                                LinearLayout itemView = (LinearLayout) LayoutInflater.from(FilmStarActivity.this).inflate(R.layout.item_film_star_indicator, null);
                                itemView.setNextFocusUpId(itemView.getId());
                                TextView indicatorTitle = (TextView) itemView.findViewById(R.id.title_new);

                                indicatorTitle.setText(facet.getName());
                                itemView.setOnFocusChangeListener(new OnIndicatorItemFocusedListener());
                                itemView.setOnHoverListener(FilmStarActivity.this);
                                itemView.setOnClickListener(FilmStarActivity.this);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                int margin=(int) getResources().getDimension(R.dimen.tab_margin_l);
                                layoutParams.setMargins(margin,margin,margin,margin);
                                itemView.setTag(facet.getContent_type());
                                itemView.setTag(R.id.filmStar_indicator_item_new, i);
                                if (i == 0) {
                                    indicatorSelectedView = itemView;
                                    indicatorSelectedView.setSelected(true);
                                    indicatorTitle.setTextColor(getResources().getColor(R.color.word_selected));
                                    itemView.setNextFocusLeftId(itemView.getId());
                                    indicatorListLayout.addView(itemView, layoutParams);
                                } else {
                                    if (i == entity.getFacet().size() - 1) {
                                        itemView.setNextFocusRightId(itemView.getId());
                                    }
                                    indicatorListLayout.addView(itemView, layoutParams);
                                }
                                i = i + 1;
                            }

                            if (entity.getFacet().size() > 4) {
                                indicatorArrowRight.setVisibility(View.VISIBLE);
                            }

                            fetchActorRelateByType(pk, entity.getFacet().get(0).getContent_type());
                            indicatorListLayout.getChildAt(0).requestFocus();
                        }

                    @Override
                    public void onError(Throwable e) {
                        JasmineUtil.loadException("search","","","", (int) pk, IsmartvActivator.getInstance().getApiDomain()+"api/tv/actorrelate/", DeviceUtils.getVersionCode(FilmStarActivity.this),"server",e.getMessage());
                        super.onError(e);
                    }
                });
    }


    private void fetchActorRelateByType(final long pk, final String type) {
        ActorRelateRequestParams params = new ActorRelateRequestParams();
        params.setActor_id(pk);
        params.setPage_no(1);
        params.setPage_count(30);
        params.setContent_type(type);
        mSkyService.apiFetchActorRelate(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<SemanticSearchResponseEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(SemanticSearchResponseEntity semanticSearchResponseEntity) {
                        if(semanticSearchResponseEntity==null){
                            JasmineUtil.loadException("search","","",type, (int) pk, IsmartvActivator.getInstance().getApiDomain()+"api/tv/actorrelate/", DeviceUtils.getVersionCode(FilmStarActivity.this),"data","");
                            return;
                        }
                            SemanticSearchResponseEntity entity = semanticSearchResponseEntity;
                            fillVodList((ArrayList<SemantichObjectEntity>) entity.getFacet().get(0).getObjects());
                    }

                    @Override
                    public void onError(Throwable e) {
                        JasmineUtil.loadException("search","","",type, (int) pk, IsmartvActivator.getInstance().getApiDomain()+"api/tv/actorrelate/", DeviceUtils.getVersionCode(FilmStarActivity.this),"server",e.getMessage());
                        super.onError(e);
                    }
                });

    }

    private void fillVodList(ArrayList<SemantichObjectEntity> list) {
        vodList = list;
        mItemCollections = new ArrayList<>();
        int num_pages = (int) Math.ceil((float)list.size() / SearchItemCollection.NUM_PER_PAGE);
        SearchItemCollection searchItemCollection=new SearchItemCollection(num_pages, list.size(), "1");
        mItemCollections.add(searchItemCollection);
        searchAdapter = new HGridSearchAdapterImpl(this, mItemCollections,false);
        searchAdapter.setList(mItemCollections);
        vodHorizontalScrollView.setAdapter(searchAdapter);
        vodHorizontalScrollView.setFocusable(true);
        mItemCollections.get(0).fillItems(0, list);
        searchAdapter.setList(mItemCollections);

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int i = v.getId();
        if (i == R.id.content_arrow_left_new || i == R.id.content_arrow_right_new || i == R.id.indicator_left_new || i == R.id.indicator_right_new) {
            if (hasFocus) {
                v.requestFocusFromTouch();
                JasmineUtil.scaleOut(v);
            } else {
                JasmineUtil.scaleIn(v);
            }

        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.filmStar_indicator_item_new) {
            if (indicatorSelectedView != null) {
                TextView textView = (TextView) indicatorSelectedView.findViewById(R.id.title_new);
                textView.setTextColor(getResources().getColor(R.color.word_nomal));
            } else {
                TextView textView = (TextView) v.findViewById(R.id.title_new);
                textView.setTextColor(getResources().getColor(R.color.word_selected));
            }
            indicatorSelectedView = v;
            indicatorSelectedView.setSelected(true);
            String type = (String) v.getTag();
            fetchActorRelateByType(pk, type);
            JasmineUtil.video_search(type,title);
        }
    }


    private void setFilmAttr(AttributesEntity attributesEntity, String description) {
        actorView.setText("演员 : ");
        directorView.setText("导演 : ");
        areaView.setText("国家/地区 : ");
        descriptionView.setText("影片介绍 : ");
        if (attributesEntity.getActor() != null && attributesEntity.getActor().length != 0) {
            for (Object[] strings : attributesEntity.getActor()) {
                actorView.setVisibility(View.VISIBLE);
                actorView.append(strings[1] + " ");
            }
        } else if (attributesEntity.getAttendee() != null && attributesEntity.getAttendee().length != 0) {
            for (Object[] strings : attributesEntity.getAttendee()) {
                actorView.setVisibility(View.VISIBLE);
                actorView.append(strings[1] + " ");
            }
        } else {
            actorView.setVisibility(View.GONE);
        }

        if (attributesEntity.getDirector() != null && attributesEntity.getDirector().length != 0) {
            for (Object[] strings : attributesEntity.getDirector()) {
                directorView.setVisibility(View.VISIBLE);
                directorView.append(strings[1] + " ");
            }
        } else {
            directorView.setVisibility(View.GONE);
        }

        if (attributesEntity.getArea() != null && attributesEntity.getArea().length != 0) {
            areaView.setVisibility(View.VISIBLE);
            areaView.append(attributesEntity.getArea()[1] + " ");
        } else {
            areaView.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(description)) {
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.append(description + " ");
        } else {
            descriptionView.setVisibility(View.GONE);
        }
    }


    public void showNetworkErrorPop() {
        errorDialog = new MyDialog(this,"此分类内容已下架！");
        errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
//        networkEorrorPopupWindow = new MessagePopWindow(this, "网络异常，请检查网络", null);
        contentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!errorDialog.isShowing()) {
//                    networkEorrorPopupWindow.showAtLocation(contentView, Gravity.CENTER, new MessagePopWindow.ConfirmListener() {
//                                @Override
//                                public void confirmClick(View view) {
//                                    networkEorrorPopupWindow.dismiss();
//                                    ScreenManager.getScreenManager().popAllActivityExceptOne(null);
//                                }
//                            },
//                            null
//                    );

                    errorDialog.show();
                }
            }
        }, 1000);
    }



    @Override
    public boolean onHover(View v, MotionEvent keycode) {
        switch (keycode.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (!v.isFocused()) {
                    if(v.getId()==R.id.content_arrow_left_new||v.getId()==R.id.content_arrow_right_new)
                        v.setFocusable(true);
                    v.requestFocusFromTouch();
                    v.requestFocus();
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                dividerLine.requestFocus();
                break;
        }
        return true;
    }


    class OnIndicatorItemFocusedListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            ImageView bg = (ImageView) v.findViewById(R.id.indicator_bg_new);
            TextView textView = (TextView) v.findViewById(R.id.title_new);
            if (hasFocus) {
                focusTranslate.setFocusable(false);
//                if (indicatorSelectedView == v) {
//                    textView.setTextColor(getResources().getColor(R.color.word_selected));
//                } else {
                    textView.setTextColor(getResources().getColor(R.color.word_focus));
//                }
                bg.setVisibility(View.VISIBLE);
                JasmineUtil.scaleOut(v);
//                currentFocuedIndicatorView = v;
            } else {
                focusTranslate.setFocusable(true);
                if (indicatorSelectedView == v) {
                    textView.setTextColor(getResources().getColor(R.color.word_selected));
                } else {
                    textView.setTextColor(getResources().getColor(R.color.word_nomal));
                }

                bg.setVisibility(View.INVISIBLE);
                JasmineUtil.scaleIn(v);

            }
        }
    }

    @Override
    protected void onPause() {
        mIsBusy = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        baseChannel="";
        baseSection="";
        super.onDestroy();
    }
    private int getIndexFromSectionAndPage(int sectionIndex, int page) {
        return sectionIndex * 10000 + page;
    }
}
