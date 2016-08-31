package tv.ismar.detailpage.view;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.util.Constants;
import tv.ismar.app.util.Utils;
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
    private int[] mRelTextViewFocusIds = {R.id.rel_1_focus_text, R.id.rel_2_focus_text, R.id.rel_3_focus_text, R.id.rel_4_focus_text};

    private LabelImageView[] relRelImageViews;
    private TextView[] relTextViews;
    private TextView[] relFocusTextViews;

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
            mItemPk = 705116;
            mEntertainmentBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_entertainment);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
        } else if ("movie".equals(content_model)) {
            relViews = 6;
            mItemPk = 707744;
            mMovieBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_movie);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
        } else {
            relViews = 4;
            mItemPk = 705229;
            mNormalBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_normal);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
        }
        relRelImageViews = new LabelImageView[relViews];
        relTextViews = new TextView[relViews];
        relFocusTextViews = new TextView[relViews];

        for (int i = 0; i < relViews; i++) {
            relRelImageViews[i] = (LabelImageView) findViewById(mRelImageViewIds[i]);
            relTextViews[i] = (TextView) findViewById(mRelTextViewIds[i]);
            if (!content_model.equals("variety") && !content_model.equals("entertainment") && !content_model.equals("movie")) {
                relFocusTextViews[i] = (TextView) findViewById(mRelTextViewFocusIds[i]);
            }
        }

        Log.i(TAG, Constants.TEST);
        getLoaderManager().initLoader(0, null, mModel);
        mPresenter.start();
        mPresenter.fetchItem(String.valueOf(mItemPk), null, null);
        mPresenter.fetchItemRelate(String.valueOf(mItemPk), null, null);

        // 购买按钮等设置
        Drawable drawaleBuy = getResources().getDrawable(R.mipmap.daisy_left_buy);
        Drawable drawalePlay = getResources().getDrawable(R.mipmap.daisy_left_play);
        Drawable drawaleCollect = getResources().getDrawable(R.mipmap.daisy_left_collect);
        Drawable drawaleDrama = getResources().getDrawable(R.mipmap.daisy_left_drama);
        if (drawaleBuy != null) {
            drawaleBuy.setBounds(0, 0,
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size),
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size));
        }
        if (drawalePlay != null) {
            drawalePlay.setBounds(0, 0,
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size),
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size));
        }
        if (drawaleCollect != null) {
            drawaleCollect.setBounds(0, 0,
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size),
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size));
        }
        if (drawaleDrama != null) {
            drawaleDrama.setBounds(0, 0,
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size),
                    getResources().getDimensionPixelSize(R.dimen.detailpage_button_drawable_left_size));
        }
        Button detail_btn_play = (Button) findViewById(R.id.detail_btn_play);
        Button detail_btn_buy = (Button) findViewById(R.id.detail_btn_buy);
        Button detail_btn_collect = (Button) findViewById(R.id.detail_btn_collect);
        Button detail_btn_drama = (Button) findViewById(R.id.detail_btn_drama);
        if (detail_btn_play != null) {
            detail_btn_play.setCompoundDrawables(drawalePlay, null, null, null);
        }
        if (detail_btn_buy != null) {
            detail_btn_buy.setCompoundDrawables(drawaleBuy, null, null, null);
        }
        if (detail_btn_collect != null) {
            detail_btn_collect.setCompoundDrawables(drawaleCollect, null, null, null);
        }
        if (detail_btn_drama != null) {
            detail_btn_drama.setCompoundDrawables(drawaleDrama, null, null, null);
        }

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
    }

    @Override
    public void notifyPlayCheck(PlayCheckEntity playCheckEntity) {
        mModel.notifyPlayCheck(playCheckEntity);
    }
}
