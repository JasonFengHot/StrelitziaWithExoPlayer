package tv.ismar.detailpage.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.Constants;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.LabelImageView;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.databinding.FragmentDetailpageEntertainmentSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageMovieSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageNormalSharpBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;
import tv.ismar.statistics.DetailPageStatistics;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;

public class DetailPageFragment extends Fragment implements DetailPageContract.View, View.OnHoverListener {
    private static final String TAG = "LH/DetailPageFragment";
    private static final String ARG_PK = "ARG_PK";
    private static final String ARG_CONTENT_MODEL = "ARG_CONTENT_MODEL";

    private DetailPageViewModel mModel;
    private DetailPageContract.Presenter mPresenter;

    private FragmentDetailpageMovieSharpBinding mMovieBinding;
    private FragmentDetailpageEntertainmentSharpBinding mEntertainmentBinding;
    private FragmentDetailpageNormalSharpBinding mNormalBinding;
    private DetailPagePresenter mDetailPagePresenter;

    private int relViews;
    private int[] mRelImageViewIds = {R.id.rel_1_img, R.id.rel_2_img, R.id.rel_3_img, R.id.rel_4_img, R.id.rel_5_img, R.id.rel_6_img};
    private int[] mRelTextViewIds = {R.id.rel_1_text, R.id.rel_2_text, R.id.rel_3_text, R.id.rel_4_text, R.id.rel_5_text, R.id.rel_6_text};
    private int[] mRelTextViewFocusIds = {R.id.rel_1_focus_text, R.id.rel_2_focus_text, R.id.rel_3_focus_text, R.id.rel_4_focus_text};
    private int[] mRelItemViews = {R.id.related_item_layout_1, R.id.related_item_layout_2, R.id.related_item_layout_3, R.id.related_item_layout_4};

    private LabelImageView[] relRelImageViews;
    private TextView[] relTextViews;
    private TextView[] relFocusTextViews;

    //传递参数
    private String fromPage;

    private HeadFragment headFragment;
    private String mHeadTitle;
    private volatile boolean itemIsLoad;
    private volatile boolean relateIsLoad;
    private ItemEntity mItemEntity;
    private ItemEntity[] relateItems;
    private int mRemandDay = 0;
    private BaseActivity mActivity;

    private View tmp;

    private View palyBtnView;
    private View purchaseBtnView;
    private View exposideBtnView;
    private View favoriteBtnView;
    private View moreBtnView;

    private DetailPageStatistics mPageStatistics;

    public DetailPageFragment() {
        // Required empty public constructor
    }


    public static DetailPageFragment newInstance(String fromPage, String itemJson) {
        DetailPageFragment fragment = new DetailPageFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_SOURCE, fromPage);
        args.putString(EXTRA_ITEM_JSON, itemJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageStatistics = new DetailPageStatistics();
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            fromPage = bundle.getString(EXTRA_SOURCE);
            String itemJson = bundle.getString(EXTRA_ITEM_JSON);
            mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);

        }

        if (!(getActivity() instanceof BaseActivity)) {
            getActivity().finish();
            Log.e(TAG, "Activity must be extends BaseActivity.");
            return;
        }
        mActivity = (BaseActivity) getActivity();
        mDetailPagePresenter = new DetailPagePresenter((DetailPageActivity) getActivity(), this, mItemEntity.getContentModel());
        mModel = new DetailPageViewModel(mActivity, mDetailPagePresenter);
        mDetailPagePresenter.setItemEntity(mItemEntity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        return loadItemModel(inflater, container, mItemEntity);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, Constants.TEST);
//        mPresenter.fetchItem(String.valueOf(mItemEntity.getPk()));
//        loadItem(mItemEntity);

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        mPresenter.fetchItemRelate(String.valueOf(mItemEntity.getPk()));
        loadItem(mItemEntity);
        mPageStatistics.videoDetailIn(mItemEntity, fromPage);
        mModel.notifyBookmark(true);
    }

    @Override
    public void onPause() {
        mPageStatistics.videoDetailOut(mItemEntity);
        super.onPause();
    }

    @Override
    public void onStop() {
        String sn = IsmartvActivator.getInstance().getSnToken();
        Log.i("LH/", "sn:" + sn);
        mPresenter.stop();
        super.onStop();
    }

    @Override
    public void loadItem(ItemEntity itemEntity) {
        mPresenter.requestPlayCheck(String.valueOf(mItemEntity.getPk()));
        mModel.replaceItem(itemEntity);
        itemIsLoad = true;
        hideLoading();
        mItemEntity = itemEntity;
    }

    @Override
    public void loadItemRelate(ItemEntity[] itemEntities) {
        relateItems = itemEntities;
        if (itemEntities.length < relViews) {
            for (int i = relViews-1; i >=itemEntities.length ; i--) {

            }
            for (int i = itemEntities.length==0?0:itemEntities.length; i < relViews; i++) {
                ((View) relRelImageViews[i].getParent()).setVisibility(View.INVISIBLE);
            }
        }
        for (int i = 0; i < itemEntities.length && i < relViews; i++) {
            moreBtnView.setNextFocusLeftId(View.NO_ID);
            switch (mItemEntity.getContentModel()) {
                case "movie":
                    relRelImageViews[i].setLivUrl(itemEntities[i].getList_url());
                    break;
                default:
                    relRelImageViews[i].setLivUrl(itemEntities[i].getPosterUrl());
                    break;

            }
            if (mNormalBinding != null) {
                View itemView = mNormalBinding.getRoot().findViewById(mRelItemViews[i]);
                itemView.setTag(i);
                itemView.setOnClickListener(relateItemOnClickListener);
                itemView.setOnHoverListener(this);
                final int finalI = i;
                itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            relTextViews[finalI].setSelected(true);
                        } else {
                            relTextViews[finalI].setSelected(false);
                        }
                    }
                });
//                relRelImageViews[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        int position = (int) v.getTag();
//                        if (hasFocus) {
//                            relTextViews[position].setSelected(true);
//                        } else {
//                            relTextViews[position].setSelected(false);
//                        }
//                    }
//                });

            } else {
                relRelImageViews[i].setTag(i);
                relRelImageViews[i].setOnClickListener(relateItemOnClickListener);
                relRelImageViews[i].setOnHoverListener(this);
                relRelImageViews[i].setNextFocusDownId(relRelImageViews[i].getId());

                relRelImageViews[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        int position = (int) v.getTag();
                        if (hasFocus) {
                            relTextViews[position].setSelected(true);
                        } else {
                            relTextViews[position].setSelected(false);
                        }
                    }
                });
            }


            ItemEntity.Expense expense = itemEntities[i].getExpense();
            if (expense != null && !Utils.isEmptyText(expense.getCptitle())) {
                relRelImageViews[i].setLivVipPosition(LabelImageView.LEFTTOP);
                String imageUrl = VipMark.getInstance().getImage(mActivity, expense.getPay_type(), expense.getCpid());
                relRelImageViews[i].setLivVipUrl(imageUrl);
            }
            String scoreStr = itemEntities[i].getBeanScore();
            if (!Utils.isEmptyText(scoreStr)) {
                float score = 0;
                try {
                    score = Float.parseFloat(scoreStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (score > 0) {
                    relRelImageViews[i].setLivRate(score);
                }
            }
//            relTextViews[i].setMarqueeRepeatLimit(-1);
//            relTextViews[i].setEllipsize(TextUtils.TruncateAt.MARQUEE);
            relTextViews[i].setText(itemEntities[i].getTitle());

            if (mMovieBinding != null || mEntertainmentBinding != null) {
                relRelImageViews[i].setLivLabelText(itemEntities[i].getFocus());

            } else {
                relFocusTextViews[i].setText(itemEntities[i].getFocus());
            }
        }
        relateIsLoad = true;
        hideLoading();
        if (mMovieBinding != null && mMovieBinding.detailBtnLinear != null)
            mMovieBinding.detailBtnLinear.setVisibility(View.VISIBLE);
    }


    @Override
    public void notifyPlayCheck(PlayCheckEntity playCheckEntity) {
        mModel.notifyPlayCheck(playCheckEntity);
        mRemandDay = playCheckEntity.getRemainDay();

        boolean isBuy;
        if (playCheckEntity.getRemainDay() == 0) {
            isBuy = false;// 过期了。认为没购买
        } else {
            isBuy = true;// 购买了，剩余天数大于0
        }
        ((DetailPageActivity)getActivity()).playCheckResult(isBuy);
    }

    @Override
    public void notifyBookmark(boolean mark, boolean isSuccess) {
        mModel.notifyBookmark(isSuccess);
        if (mark) {
            if (isSuccess) {
                showToast(getString(R.string.vod_bookmark_add_success));
            } else {
                showToast(getString(R.string.vod_bookmark_add_unsuccess));
            }
        } else {
            if (isSuccess) {
                showToast(getString(R.string.vod_bookmark_remove_success));
            } else {
                showToast(getString(R.string.vod_bookmark_remove_unsuccess));
            }
        }
    }

    @Override
    public void onError() {
        if (((DetailPageActivity) getActivity()).mLoadingDialog != null && ((DetailPageActivity) getActivity()).mLoadingDialog.isShowing()) {
            ((DetailPageActivity) getActivity()).mLoadingDialog.dismiss();
        }
    }

    @Override
    public void setPresenter(DetailPageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void hideLoading() {
        if (((DetailPageActivity) getActivity()).mLoadingDialog != null && ((DetailPageActivity) getActivity()).mLoadingDialog.isShowing() && itemIsLoad && relateIsLoad) {
            ((DetailPageActivity) getActivity()).mLoadingDialog.dismiss();
        }

        mModel.showLayout();
    }

    private View.OnClickListener relateItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ItemEntity item = relateItems[(int) v.getTag()];
            mPageStatistics.videoRelateClick(mItemEntity.getPk(), item);
            new PageIntent().toDetailPage(getContext(), Source.RELATED.getValue(), item.getPk());
        }
    };

    private String getModelType(String content_model) {
        String resourceType = null;
        if (content_model.equals("movie")) {
            resourceType = "电影";
            // teleplay 为电视剧trailer
        } else if (content_model.equals("teleplay")) {
            resourceType = "电视剧";
            // variety 为综艺
        } else if (content_model.equals("variety")) {
            resourceType = "综艺";
            // documentary 为纪录片
        } else if (content_model.equals("documentary")) {
            resourceType = "纪录片";
            // entertainment 为娱乐
        } else if (content_model.equals("entertainment")) {
            resourceType = "娱乐";
            // trailer 为片花
        } else if (content_model.equals("trailer")) {
            resourceType = "片花";
            // music 为音乐
        } else if (content_model.equals("music")) {
            resourceType = "音乐";
            // comic 为喜剧
        } else if (content_model.equals("comic")) {
            resourceType = "喜剧";
            // sport 为体育
        } else if (content_model.equals("sport")) {
            resourceType = "体育";
        }
        return resourceType;
    }

    private void showToast(String text) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.simple_toast, null);
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(mActivity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private View loadItemModel(LayoutInflater inflater, ViewGroup container, ItemEntity itemEntity) {
        View contentView;
        String content_model = itemEntity.getContentModel();
        mHeadTitle = getModelType(content_model);
        if ((("variety".equals(content_model) && mItemEntity.getExpense() == null)) || ("entertainment".equals(content_model) && mItemEntity.getExpense() == null)) {
            relViews = 4;
            mEntertainmentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_entertainment_sharp, container, false);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
            contentView = mEntertainmentBinding.getRoot();
            tmp = mEntertainmentBinding.tmp;

            palyBtnView = mEntertainmentBinding.detailBtnPlay;
//            purchaseBtnView = mEntertainmentBinding.
            exposideBtnView = mEntertainmentBinding.detailBtnDrama;
            favoriteBtnView = mEntertainmentBinding.detailBtnCollect;
            moreBtnView = mEntertainmentBinding.detailRelativeButton;
            palyBtnView.setNextFocusDownId(R.id.detail_relative_button);
        } else if ("movie".equals(content_model)) {
            relViews = 6;
            mMovieBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_movie_sharp, container, false);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
            contentView = mMovieBinding.getRoot();
            tmp = mMovieBinding.tmp;
            palyBtnView = mMovieBinding.detailBtnPlay;
            purchaseBtnView = mMovieBinding.detailBtnBuy;
//            exposideBtnView = mMovieBinding.detailBtnDrama;
            favoriteBtnView = mMovieBinding.detailBtnCollect;
            moreBtnView = mMovieBinding.detailRelativeButton;
        } else {
            relViews = 4;
            relFocusTextViews = new TextView[relViews];
            mNormalBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_normal_sharp, container, false);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
            contentView = mNormalBinding.getRoot();
            tmp = mNormalBinding.tmp;
            palyBtnView = mNormalBinding.detailBtnPlay;
            purchaseBtnView = mNormalBinding.detailBtnBuy;
            exposideBtnView = mNormalBinding.detailBtnDrama;
            favoriteBtnView = mNormalBinding.detailBtnCollect;
            moreBtnView = mNormalBinding.detailRelativeButton;
        }

        palyBtnView.setOnHoverListener(this);
        if (purchaseBtnView != null) {
            purchaseBtnView.setOnHoverListener(this);
        }

        if (exposideBtnView != null) {
            exposideBtnView.setOnHoverListener(this);
        }
        favoriteBtnView.setOnHoverListener(this);
        moreBtnView.setOnHoverListener(this);

        relRelImageViews = new LabelImageView[relViews];
        relTextViews = new TextView[relViews];

        for (int i = 0; i < relViews; i++) {
            relRelImageViews[i] = (LabelImageView) contentView.findViewById(mRelImageViewIds[i]);
            relRelImageViews[i].setVisibility(View.VISIBLE);
            relTextViews[i] = (TextView) contentView.findViewById(mRelTextViewIds[i]);
            if (!(content_model.equals("variety") && itemEntity.getExpense() == null) &&
                    !(content_model.equals("entertainment") && itemEntity.getExpense() == null)
                    && !content_model.equals("movie")) {
                relFocusTextViews[i] = (TextView) contentView.findViewById(mRelTextViewFocusIds[i]);
            }
        }
        headFragment = new HeadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", HeadFragment.HEADER_DETAILPAGE);
        bundle.putString("channel_name", mHeadTitle);
        headFragment = new HeadFragment();
        headFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().add(R.id.detail_head, headFragment).commit();
        return contentView;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                tmp.requestFocusFromTouch();
                break;
        }

        return false;
    }
}
