package tv.ismar.player.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.player.PlayerPageContract;
import tv.ismar.player.R;
import tv.ismar.player.databinding.ActivityPlayerBinding;
import tv.ismar.player.presenter.PlayerPagePresenter;
import tv.ismar.player.viewmodel.PlayerPageViewModel;

public class PlayerActivity extends BaseActivity implements PlayerPageContract.View {

    private final String TAG = "LH/PlayerActivity";

    private String itemId;
    private String subItemId;

    private PlayerPageViewModel mModel;
    private PlayerPageContract.Presenter mPresenter;
    private PlayerPagePresenter mPlayerPagePresenter;
    private ActivityPlayerBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemId = getIntent().getStringExtra("itemId");
        subItemId = getIntent().getStringExtra("subItemId");
        if (TextUtils.isEmpty(itemId)) {
            finish();
            Log.i(TAG, "itemId can't be null.");
            return;
        }

        mPlayerPagePresenter = new PlayerPagePresenter(this);
        mModel = new PlayerPageViewModel(this, mPlayerPagePresenter);

        setContentView(R.layout.activity_player);
    }

    @Override
    public void loadItem(ItemEntity itemEntity) {

    }

    @Override
    public void setPresenter(PlayerPageContract.Presenter presenter) {

    }

    @Override
    public void onHttpFailure(Throwable e) {

    }

    @Override
    public void onHttpInterceptor(Throwable e) {

    }
}
