package tv.ismar.player.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;

import tv.ismar.player.presenter.PlayerPagePresenter;

/**
 * Created by longhai on 16-9-8.
 */
public class PlayerPageViewModel extends BaseObservable {

    private Context mContext;
    private PlayerPagePresenter mPresenter;

    public PlayerPageViewModel(Context context, PlayerPagePresenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }

}
