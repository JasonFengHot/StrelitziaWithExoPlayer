package tv.ismar.player;

import tv.ismar.app.BasePresenter;
import tv.ismar.app.BaseView;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by longhai on 16-9-8.
 */
public interface PlayerPageContract {

    interface View extends BaseView<Presenter> {

        void loadItem(ItemEntity itemEntity);

        void loadClip(ClipEntity clipEntity);

        void loadAdvertisement(AdElementEntity adElementEntity);

    }

    interface Presenter extends BasePresenter {

        void fetchItem(String itemId);

        void fetchMediaUrl(String clipUrl, String sign, String code);

        void fetchAdvertisement(ItemEntity itemEntity, String params);

    }

}
