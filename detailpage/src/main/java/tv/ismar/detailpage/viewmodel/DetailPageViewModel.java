package tv.ismar.detailpage.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;

import tv.ismar.detailpage.DetailPageContract;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageViewModel extends BaseObservable{
    private Context mContext;
    private final DetailPageContract.Presenter mPresenter;

    public DetailPageViewModel(Context context, DetailPageContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;

    }

    


}
