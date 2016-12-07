package tv.ismar.searchpage;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.models.ActorRelateRequestParams;
import tv.ismar.app.models.AttributesEntity;
import tv.ismar.app.models.Expense;
import tv.ismar.app.models.PersonEntitiy;
import tv.ismar.app.models.SemanticSearchResponseEntity;
import tv.ismar.app.models.SemantichObjectEntity;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.searchpage.weight.MyDialog;
import tv.ismar.searchpage.weight.ReflectionTransformationBuilder;
import tv.ismar.searchpage.weight.RotateTextView;

/**
 * Created by huaijie on 2/22/16.
 */
public class FilmStarActivity extends BaseActivity implements OnFocusChangeListener, View.OnClickListener, OnHoverListener{
    private static final String TAG = "FilmStarActivity";


    private long pk;
    private TextView filmStartitle;
    private LinearLayout indicatorListLayout;
    private LinearLayout vodListView;
    private TvHorizontalScrollView vodHorizontalScrollView;

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

    private View vodItemClickedView;
//    private View currentFocuedIndicatorView;

    private ImageView mContentBackgroundView;
    private MyDialog errorDialog;
    private String title;


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
        if (vodItemClickedView != null) {
            vodItemClickedView.requestFocusFromTouch();
            vodItemClickedView.requestFocus();
        }
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
        vodListView = (LinearLayout) findViewById(R.id.vod_list_view_new);
        vodHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.vod_scrollview_new);
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
        vodHorizontalScrollView.setCoverOffset(25);
        horizontalScrollView.setLeftArrow(indicatorArrowLeft);
        horizontalScrollView.setRightArrow(indicatorArrowRight);
        vodHorizontalScrollView.setLeftArrow(contentArrowLeft);
        vodHorizontalScrollView.setRightArrow(contentArrowRight);
//        horizontalScrollView.setOnNothingSeleted(this);
//        vodHorizontalScrollView.setOnNothingSeleted(this);

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
                vodHorizontalScrollView.pageScroll(View.FOCUS_RIGHT);
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
                        }
                });
    }

    private void fillVodIndicator() {

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
                            fillVodList(entity.getFacet().get(0).getObjects());
                    }
                });

    }

    private void fillVodList(List<SemantichObjectEntity> list) {
        vodListView.removeAllViews();
        vodHorizontalScrollView.scrollTo(0, 0);
        for (int i = 0; i < list.size(); i++) {
            LinearLayout itemView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.item_vod_star, null);
            itemView.setOnHoverListener(FilmStarActivity.this);
            itemView.setOnFocusChangeListener(new OnVodItemFocusedListener());
            TextView itemVodTitle = (TextView) itemView.findViewById(R.id.item_vod_title_new);
            ImageView itemVodImage = (ImageView) itemView.findViewById(R.id.item_vod_image_new);
            TextView ItemBeanScore = (TextView) itemView.findViewById(R.id.ItemBeanScore);
            RotateTextView expense_txt = (RotateTextView) itemView.findViewById(R.id.expense_txt);
            TextView itemFocus = (TextView) itemView.findViewById(R.id.item_vod_focus_new);
            expense_txt.setDegrees(315);
            itemVodTitle.setText(list.get(i).getTitle());
            Transformation mTransformation = new ReflectionTransformationBuilder()
                    .setIsHorizontal(true)
                    .build();
            String verticalUrl = list.get(i).getVertical_url();
            String horizontalUrl = list.get(i).getPoster_url();
            String scoreValue = list.get(i).getBean_score();
            Expense expense = list.get(i).getExpense();
            String focusValue = list.get(i).getFocus();

            if (!TextUtils.isEmpty(verticalUrl)) {
                if (!TextUtils.isEmpty(scoreValue)) {
                    ItemBeanScore.setVisibility(View.VISIBLE);
                    ItemBeanScore.setText(scoreValue);
                }
                if(expense!=null){
                    if(expense.cptitle!=null){
                        expense_txt.setText(expense.cptitle);
                        expense_txt.setVisibility(View.VISIBLE);
                        if(expense.pay_type==1){
                            expense_txt.setBackgroundResource(R.drawable.list_single_buy);
                        }else if((expense.cpname).startsWith("ismar")){
                            expense_txt.setBackgroundResource(R.drawable.list_ismar);
                        }else if("iqiyi".equals(expense.cpname)){
                            expense_txt.setBackgroundResource(R.drawable.list_lizhi);
                        }
                    } else {
                        expense_txt.setVisibility(View.GONE);
                    }

                }
                if (!TextUtils.isEmpty(focusValue)) {
                    itemFocus.setVisibility(View.VISIBLE);
                    itemFocus.setText(focusValue);
                }
                Picasso.with(this)
                        .load(verticalUrl)
                        .memoryPolicy(MemoryPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .error(R.drawable.vertical_preview_bg)
                        .placeholder(R.drawable.vertical_preview_bg)
                        .transform(mTransformation)
                        .into(itemVodImage);
            } else {
                Picasso.with(this)
                        .load(horizontalUrl)
                        .memoryPolicy(MemoryPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .error(R.drawable.vertical_preview_bg)
                        .placeholder(R.drawable.vertical_preview_bg)
                        .transform(mTransformation)
                        .into(itemVodImage);
            }

            itemView.setTag(list.get(i));
            itemView.setTag(R.layout.item_vod_star, i);
            itemView.setNextFocusUpId(focusTranslate.getId());
            if (i == list.size() - 1) {
                itemView.setNextFocusRightId(itemView.getId());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vodItemClickedView = v;
                    PageIntent pageIntent=new PageIntent();
                    SemantichObjectEntity semantichObjectEntity= (SemantichObjectEntity) v.getTag();
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
                    JasmineUtil.video_search_arrive(title,"text", (int) pk,0,itemTitle);
                }
            });

            int padding = (int) getResources().getDimension(R.dimen.filmStar_item_horizontal_space);
            int verticalPadding = (int) getResources().getDimension(R.dimen.filmStar_item_vertical_space);
            int topPadding = (int) getResources().getDimension(R.dimen.filmStar_item_top_space);


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.setMargins(padding, topPadding, padding, verticalPadding);
            itemView.setNextFocusDownId(itemView.getId());
            vodListView.addView(itemView, layoutParams);
        }
        vodListView.getChildAt(0).requestFocus();
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

//    @Override
//    public void onTheFirst() {
////        dividerLine.requestFocus();
//    }
//
//    @Override
//    public void onTheLast() {
////        dividerLine.requestFocus();
//    }


    class OnVodItemFocusedListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            View imageLayout = v.findViewById(R.id.item_vod_image_layout_new);
            View vodTitle = v.findViewById(R.id.item_vod_title_new);
            if (hasFocus) {
                SemantichObjectEntity entity = (SemantichObjectEntity) v.getTag();
                AttributesEntity attributesEntity = entity.getAttributes();
                String description = entity.getDescription();
                setFilmAttr(attributesEntity, description);
                vodTitle.setSelected(true);
                JasmineUtil.scaleOut1(v);
            } else {
                vodTitle.setSelected(false);
                JasmineUtil.scaleIn1(v);
            }
            if (hasFocus) {
                imageLayout.setSelected(true);
            } else {
                imageLayout.setSelected(false);
            }
        }
    }

    class OnIndicatorItemFocusedListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            ImageView bg = (ImageView) v.findViewById(R.id.indicator_bg_new);
            TextView textView = (TextView) v.findViewById(R.id.title_new);
            if (hasFocus) {
                focusTranslate.setFocusable(false);
                if (indicatorSelectedView == v) {
                    textView.setTextColor(getResources().getColor(R.color.word_selected));
                } else {
                    textView.setTextColor(getResources().getColor(R.color.word_focus));
                }
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
}
