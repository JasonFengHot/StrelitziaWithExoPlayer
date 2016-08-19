package tv.ismar.detailpage.presenter;

import tv.ismar.detailpage.DetailPageContract;

/**
 * Created by huibin on 8/19/16.
 */
public class DetailPagePresenter implements DetailPageContract.Presenter {

    private final  DetailPageContract.View mDetailView;

    public DetailPagePresenter(DetailPageContract.View detailView) {
        mDetailView = detailView;

        mDetailView.setPresenter(this);
    }

    @Override
    public void fetchItem(String pk) {

    }
}
