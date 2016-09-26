package tv.ismar.detailpage.view;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
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
import tv.ismar.detailpage.databinding.ActivityDetailpageEntertainmentSharpBinding;
import tv.ismar.detailpage.databinding.ActivityDetailpageMovieSharpBinding;
import tv.ismar.detailpage.databinding.ActivityDetailpageNormalSharpBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_MODEL;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity implements DetailPageContract.View {
    private static final String TAG = "DetailPageActivity";

    private DetailPageViewModel mModel;
    private DetailPageContract.Presenter mPresenter;

    private ActivityDetailpageMovieSharpBinding mMovieBinding;
    private ActivityDetailpageEntertainmentSharpBinding mEntertainmentBinding;
    private ActivityDetailpageNormalSharpBinding mNormalBinding;
    private DetailPagePresenter mDetailPagePresenter;
    private String content_model;


    private int relViews;
    private int[] mRelImageViewIds = {R.id.rel_1_img, R.id.rel_2_img, R.id.rel_3_img, R.id.rel_4_img, R.id.rel_5_img, R.id.rel_6_img};
    private int[] mRelTextViewIds = {R.id.rel_1_text, R.id.rel_2_text, R.id.rel_3_text, R.id.rel_4_text, R.id.rel_5_text, R.id.rel_6_text};
    private int[] mRelTextViewFocusIds = {R.id.rel_1_focus_text, R.id.rel_2_focus_text, R.id.rel_3_focus_text, R.id.rel_4_focus_text};

    private LabelImageView[] relRelImageViews;
    private TextView[] relTextViews;
    private TextView[] relFocusTextViews;

    private int mItemPk;

    private HeadFragment headFragment;
    private String mHeadTitle;

    private LoadingDialog mLoadingDialog;

    private volatile boolean itemIsLoad;
    private volatile boolean relateIsLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                DetailPageActivity.this.finish();
            }
        });
        mLoadingDialog.showDialog();

        content_model = getIntent().getStringExtra(EXTRA_MODEL);
        mItemPk = getIntent().getIntExtra(EXTRA_PK, -1);
        if (TextUtils.isEmpty(content_model) || mItemPk == -1) {
            finish();
            return;
        }
        mDetailPagePresenter = new DetailPagePresenter(DetailPageActivity.this, this, content_model);
        mModel = new DetailPageViewModel(this, mDetailPagePresenter);

        mHeadTitle = getModelType(content_model);

        if (("variety".equals(content_model) || "entertainment".equals(content_model))) {
            relViews = 4;
            mEntertainmentBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_entertainment_sharp);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
        } else if ("movie".equals(content_model)) {
            relViews = 6;
            mMovieBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_movie_sharp);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
        } else {
            relViews = 4;
            relFocusTextViews = new TextView[relViews];
            mNormalBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_normal_sharp);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
        }
        relRelImageViews = new LabelImageView[relViews];
        relTextViews = new TextView[relViews];

        for (int i = 0; i < relViews; i++) {
            relRelImageViews[i] = (LabelImageView) findViewById(mRelImageViewIds[i]);
            relTextViews[i] = (TextView) findViewById(mRelTextViewIds[i]);
            if (!content_model.equals("variety") && !content_model.equals("entertainment") && !content_model.equals("movie")) {
                relFocusTextViews[i] = (TextView) findViewById(mRelTextViewFocusIds[i]);
            }
        }
        headFragment = (HeadFragment) getSupportFragmentManager().findFragmentById(R.id.detail_head);
        headFragment.setHeadTitle(mHeadTitle);

        Log.i(TAG, Constants.TEST);
        mPresenter.start();
        mPresenter.fetchItem(String.valueOf(mItemPk));
        mPresenter.fetchItemRelate(String.valueOf(mItemPk));

    }


    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.requestPlayCheck(String.valueOf(mItemPk));
    }

    @Override
    protected void onStop() {
        mPresenter.stop();
        super.onStop();
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

    @Override
    public void loadItem(ItemEntity itemEntity) {
        mPresenter.requestPlayCheck(String.valueOf(mItemPk));
        mModel.replaceItem(itemEntity);
        itemIsLoad = true;
        hideLoading();
    }


    @Override
    public void loadItemRelate(ItemEntity[] itemEntities) {
        for (int i = 0; i < itemEntities.length && i < relViews; i++) {
            switch (content_model) {
                case "movie":
                    relRelImageViews[i].setLivUrl(itemEntities[i].getList_url());
                    break;
                default:
                    relRelImageViews[i].setLivUrl(itemEntities[i].getPosterUrl());
                    break;

            }
            relRelImageViews[i].setTag(itemEntities[i]);
            relRelImageViews[i].setOnClickListener(relateItemOnClickListener);

            ItemEntity.Expense expense = itemEntities[i].getExpense();
            if (expense != null && !Utils.isEmptyText(expense.getCptitle())) {
                relRelImageViews[i].setLivVipPosition(LabelImageView.LEFTTOP);
                String imageUrl = VipMark.getInstance().getImage(this, expense.getPay_type(), expense.getCpid());
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

    private void hideLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing() && itemIsLoad && relateIsLoad) {
            mLoadingDialog.dismiss();
        }

        mModel.showLayout();
    }

    private View.OnClickListener relateItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ItemEntity item = (ItemEntity) v.getTag();
            new PageIntent().toDetailPage(getContext(), item.getContentModel(), item.getPk());
        }
    };

    @Override
    public void notifyPlayCheck(PlayCheckEntity playCheckEntity) {
        mModel.notifyPlayCheck(playCheckEntity);
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
    public Context getContext() {
        return this;
    }

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
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.simple_toast, null);
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
