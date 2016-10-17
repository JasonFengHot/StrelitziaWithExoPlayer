package tv.ismar.detailpage.view;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.databinding.FragmentDetailpageEntertainmentSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageMovieSharpBinding;
import tv.ismar.detailpage.databinding.FragmentDetailpageNormalSharpBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;

public class DetailPageFragment extends Fragment implements DetailPageContract.View {

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

    private LabelImageView[] relRelImageViews;
    private TextView[] relTextViews;
    private TextView[] relFocusTextViews;
    private LoadingDialog mLoadingDialog;

    private int mItemPk;
    private String content_model;

    private HeadFragment headFragment;
    private String mHeadTitle;
    private volatile boolean itemIsLoad;
    private volatile boolean relateIsLoad;
    private ItemEntity mItemEntity;
    private ItemEntity[] relateItems;
    private int mRemandDay = 0;
    private BaseActivity mActivity;


    public DetailPageFragment() {
        // Required empty public constructor
    }

    public static DetailPageFragment newInstance(int pk, String content_model) {
        DetailPageFragment fragment = new DetailPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PK, pk);
        args.putString(ARG_CONTENT_MODEL, content_model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItemPk = getArguments().getInt(ARG_PK);
            content_model = getArguments().getString(ARG_CONTENT_MODEL);
        }
        if (!(getActivity() instanceof BaseActivity)) {
            getActivity().finish();
            Log.e(TAG, "Activity must be extends BaseActivity.");
            return;
        }
        mActivity = (BaseActivity) getActivity();
        mDetailPagePresenter = new DetailPagePresenter(mActivity, this, content_model);
        mDetailPagePresenter.setActivity((DetailPageActivity) getActivity());
        mModel = new DetailPageViewModel(mActivity, mDetailPagePresenter);

        mLoadingDialog = new LoadingDialog(mActivity, R.style.LoadingDialog);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                mActivity.finish();
            }
        });
        mLoadingDialog.showDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View contentView;
        mHeadTitle = getModelType(content_model);
        if (("variety".equals(content_model) || "entertainment".equals(content_model))) {
            relViews = 4;
            mEntertainmentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_entertainment_sharp, container, false);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
            contentView = mEntertainmentBinding.getRoot();
        } else if ("movie".equals(content_model)) {
            relViews = 6;
            mMovieBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_movie_sharp, container, false);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
            contentView = mMovieBinding.getRoot();
        } else {
            relViews = 4;
            relFocusTextViews = new TextView[relViews];
            mNormalBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detailpage_normal_sharp, container, false);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
            contentView = mNormalBinding.getRoot();
        }
        relRelImageViews = new LabelImageView[relViews];
        relTextViews = new TextView[relViews];

        for (int i = 0; i < relViews; i++) {
            relRelImageViews[i] = (LabelImageView) contentView.findViewById(mRelImageViewIds[i]);
            relTextViews[i] = (TextView) contentView.findViewById(mRelTextViewIds[i]);
            if (!content_model.equals("variety") && !content_model.equals("entertainment") && !content_model.equals("movie")) {
                relFocusTextViews[i] = (TextView) contentView.findViewById(mRelTextViewFocusIds[i]);
            }
        }
        headFragment = (HeadFragment) getChildFragmentManager().findFragmentById(R.id.detail_head);
        headFragment.setHeadTitle(mHeadTitle);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, Constants.TEST);
        mPresenter.start();
        mPresenter.fetchItem(String.valueOf(mItemPk));
        mPresenter.fetchItemRelate(String.valueOf(mItemPk));
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.requestPlayCheck(String.valueOf(mItemPk));
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
        mPresenter.requestPlayCheck(String.valueOf(mItemPk));
        mModel.replaceItem(itemEntity);
        itemIsLoad = true;
        hideLoading();

        mItemEntity = itemEntity;
    }

    @Override
    public void loadItemRelate(ItemEntity[] itemEntities) {
        relateItems = itemEntities;
        for (int i = 0; i < itemEntities.length && i < relViews; i++) {
            switch (content_model) {
                case "movie":
                    relRelImageViews[i].setLivUrl(itemEntities[i].getList_url());
                    break;
                default:
                    relRelImageViews[i].setLivUrl(itemEntities[i].getPosterUrl());
                    break;

            }
            relRelImageViews[i].setTag(i);
            relRelImageViews[i].setOnClickListener(relateItemOnClickListener);
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
            relTextViews[i].setText(itemEntities[i].getTitle());

            if (!content_model.equals("variety") && !content_model.equals("entertainment") && !content_model.equals("movie")) {
                relFocusTextViews[i].setText(itemEntities[i].getFocus());
            } else {
                relRelImageViews[i].setLivLabelText(itemEntities[i].getFocus());
            }


        }
        relateIsLoad = true;
        hideLoading();
    }



    @Override
    public void notifyPlayCheck(PlayCheckEntity playCheckEntity) {
        mModel.notifyPlayCheck(playCheckEntity);
        mRemandDay = playCheckEntity.getRemainDay();
    }

    @Override
    public void notifyBookmark(boolean mark, boolean isSuccess) {
        mModel.notifyBookmark(mark, isSuccess);
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
    public void setPresenter(DetailPageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onHttpFailure(Throwable e) {

    }

    @Override
    public void onHttpInterceptor(Throwable e) {

    }

    private void hideLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing() && itemIsLoad && relateIsLoad) {
            mLoadingDialog.dismiss();
        }

        mModel.showLayout();
    }

    private View.OnClickListener relateItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ItemEntity item = relateItems[(int) v.getTag()];
            new PageIntent().toDetailPage(getContext(), item.getContentModel(), item.getPk(), Source.RELATED.getValue());
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
}
