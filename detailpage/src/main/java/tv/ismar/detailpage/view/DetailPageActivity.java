package tv.ismar.detailpage.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Constants;
import tv.ismar.app.widget.LabelImageView;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.databinding.ActivityDetailpageEntertainmentBinding;
import tv.ismar.detailpage.databinding.ActivityDetailpageMovieBinding;
import tv.ismar.detailpage.databinding.ActivityDetailpageNormalBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity implements DetailPageContract.View {

    private static final String TAG = "DetailPageActivity";
    public static final String EXTRA_MODEL = "content_model";

    private DetailPageViewModel mModel;
    private DetailPageContract.Presenter mPresenter;

    private ActivityDetailpageMovieBinding mMovieBinding;
    private ActivityDetailpageEntertainmentBinding mEntertainmentBinding;
    private ActivityDetailpageNormalBinding mNormalBinding;
    private String content_model;


    private int relViews;
    private int[] mRelImageViewIds = {R.id.rel_1_img, R.id.rel_2_img, R.id.rel_3_img, R.id.rel_4_img, R.id.rel_5_img, R.id.rel_6_img};
    private int[] mRelTextViewIds = {R.id.rel_1_text, R.id.rel_2_text, R.id.rel_3_text, R.id.rel_4_text, R.id.rel_5_text, R.id.rel_6_text};

    private LabelImageView[] relRelImageViews;
    private TextView[] relTextViews;

    private int mItemPk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content_model = getIntent().getStringExtra(EXTRA_MODEL);
        if (TextUtils.isEmpty(content_model)) {
            finish();
            return;
        }
        mModel = new DetailPageViewModel(this, new DetailPagePresenter(this));
        if (("variety".equals(content_model) || "entertainment".equals(content_model))) {
            relViews = 4;
            mEntertainmentBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_entertainment);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
        } else if ("movie".equals(content_model)) {
            relViews = 6;
            mMovieBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_movie);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
        } else {
            relViews = 4;
            mNormalBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_normal);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
        }
        relRelImageViews = new LabelImageView[relViews];
        relTextViews = new TextView[relViews];

        for (int i = 0; i < relViews; i++) {
            relRelImageViews[i] = (LabelImageView) findViewById(mRelImageViewIds[i]);
            relTextViews[i] = (TextView) findViewById(mRelTextViewIds[i]);
        }

        Log.i(TAG, Constants.TEST);
        getLoaderManager().initLoader(0, null, mModel);
        //700711 免费
        //706913 付费
        mItemPk = 700711;
        mPresenter.start();
        mPresenter.fetchItem(String.valueOf(mItemPk), null, null);
        mPresenter.fetchItemRelate(String.valueOf(mItemPk), null, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.requestPlayCheck(String.valueOf(mItemPk), null, null);
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
        mModel.replaceItem(itemEntity);
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

            relTextViews[i].setText(itemEntities[i].getTitle());
        }
    }

    @Override
    public void notifyPlayCheck(int remainDay) {
        mModel.notifyPlayCheck(remainDay);
    }

}
