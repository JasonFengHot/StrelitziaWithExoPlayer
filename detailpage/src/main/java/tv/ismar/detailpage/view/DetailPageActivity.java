package tv.ismar.detailpage.view;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;

import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Constants;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.databinding.ActivityDetailpageMovieBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends Activity implements DetailPageContract.View {

    private static final String TAG = "DetailPageActivity";

    private DetailPageViewModel mModel;
    private DetailPageContract.Presenter mPresenter;


    private ActivityDetailpageMovieBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_movie);
        mModel = new DetailPageViewModel(this, new DetailPagePresenter(this));
        mBinding.setTasks(mModel);
        mBinding.setActionHandler(mPresenter);

        Log.i(TAG, Constants.TEST);
    }


    @Override
    public void setPresenter(DetailPageContract.Presenter presenter) {
        mPresenter = presenter;
    }


    @Override
    public void loadItem(ItemEntity itemEntity) {

    }

    @Override
    public void loadItemRelate(ItemEntity[] itemEntities) {

    }

}
