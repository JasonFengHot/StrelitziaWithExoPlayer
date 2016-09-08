package tv.ismar.player.presenter;

import tv.ismar.player.PlayerPageContract;

/**
 * Created by longhai on 16-9-8.
 */
public class PlayerPagePresenter implements PlayerPageContract.Presenter {

    private PlayerPageContract.View playerView;

    public PlayerPagePresenter(PlayerPageContract.View view) {
        playerView = view;
        playerView.setPresenter(this);

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void fetchItem(String itemId, String deviceToken, String accessToken) {

    }

    @Override
    public void fetchSubItem(String subItemId, String deviceToken, String accessToken) {

    }


}
