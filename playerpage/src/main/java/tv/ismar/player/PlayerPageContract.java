package tv.ismar.player;

import java.util.HashMap;

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

        void fetchItem(String itemPk);

        void fetchMediaUrl(String clipUrl, String sign, String code);

        void sendHistory(HashMap<String, Object> history);

    }

}
