package tv.ismar.detailpage.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Constants;
import tv.ismar.app.widget.HorizontalSpacesItemDecoration;
import tv.ismar.app.widget.LabelImageAdapter;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.databinding.ActivityDetailpageMovieBinding;
import tv.ismar.detailpage.presenter.DetailPagePresenter;
import tv.ismar.detailpage.viewmodel.DetailPageViewModel;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity implements DetailPageContract.View {

    private static final String TAG = "DetailPageActivity";

    private DetailPageViewModel mModel;
    private DetailPageContract.Presenter mPresenter;
    private RecyclerView detail_movie_recycler;
    private LabelImageAdapter mAdapter;

    private List<ItemEntity> itemEntityList = new ArrayList<>();

    private ActivityDetailpageMovieBinding mBinding;
    private int mItemPk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detailpage_movie);
        mModel = new DetailPageViewModel(this, new DetailPagePresenter(this));
        mBinding.setTasks(mModel);
        mBinding.setActionHandler(mPresenter);

        detail_movie_recycler = mBinding.detailMovieRecycler;
        mAdapter = new LabelImageAdapter(this, detail_movie_recycler, itemEntityList);
        detail_movie_recycler.setAdapter(mAdapter);
        detail_movie_recycler.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        detail_movie_recycler.setLayoutManager(layoutManager);
        HorizontalSpacesItemDecoration decoration = new HorizontalSpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.label_image_padding));
        detail_movie_recycler.addItemDecoration(decoration);

        Log.i(TAG, Constants.TEST);
        getLoaderManager().initLoader(0, null, mModel);
        //700711 免费
        //706913 付费
        mItemPk = 700711;
        mPresenter.start();
        mPresenter.fetchItem(String.valueOf(mItemPk), null, null);
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
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyPlayCheck(int remainDay) {
        mModel.notifyPlayCheck(remainDay);
    }

}
