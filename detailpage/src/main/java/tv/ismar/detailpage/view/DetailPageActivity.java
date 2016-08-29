package tv.ismar.detailpage.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Constants;
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

    private List<ItemEntity> itemEntityList = new ArrayList<>();

    private ActivityDetailpageMovieBinding mMovieBinding;
    private ActivityDetailpageEntertainmentBinding mEntertainmentBinding;
    private ActivityDetailpageNormalBinding mNormalBinding;



    private int mItemPk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        super.onCreate(savedInstanceState);
        String content_model = getIntent().getStringExtra(EXTRA_MODEL);
        if (TextUtils.isEmpty(content_model)) {
            finish();
            return;
        }
        mModel = new DetailPageViewModel(this, new DetailPagePresenter(this));
        if (("variety".equals(content_model) || "entertainment".equals(content_model))) {
            mEntertainmentBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_entertainment);
            mEntertainmentBinding.setTasks(mModel);
            mEntertainmentBinding.setActionHandler(mPresenter);
        } else if ("movie".equals(content_model)) {
            mMovieBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_movie);
            mMovieBinding.setTasks(mModel);
            mMovieBinding.setActionHandler(mPresenter);
        } else {
            mNormalBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_normal);
            mNormalBinding.setTasks(mModel);
            mNormalBinding.setActionHandler(mPresenter);
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
        if (itemEntities != null && itemEntities.length > 0) {
            itemEntityList.clear();
            for (int i = 0; i < itemEntities.length; i++) {
                itemEntityList.add(itemEntities[i]);
                if (i == 5) {
                    break;
                }
            }
        }
    }

    @Override
    public void notifyPlayCheck(int remainDay) {
        mModel.notifyPlayCheck(remainDay);
    }

}
