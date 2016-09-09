package tv.ismar.player;

import tv.ismar.app.BasePresenter;
import tv.ismar.app.BaseView;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by longhai on 16-9-8.
 */
public interface PlayerPageContract {

    interface View extends BaseView<Presenter> {

        void loadItem(ItemEntity itemEntity);

        void loadClip(ClipEntity clipEntity);

    }

    interface Presenter extends BasePresenter {

        void fetchItem(String itemId, String deviceToken, String accessToken);

        void fetchMediaUrl(String clipUrl, String deviceToken, String accessToken, String sign, String code);

        void fetchSubItem(String subItemId, String deviceToken, String accessToken);

    }

}
