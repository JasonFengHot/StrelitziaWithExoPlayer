package tv.ismar.detailpage;

import tv.ismar.app.BasePresenter;
import tv.ismar.app.BaseView;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 8/19/16.
 */
public interface DetailPageContract {
    interface View extends BaseView<Presenter> {
        void loadItem(ItemEntity itemEntity);

        void loadItemRelate(ItemEntity[] itemEntities);

        void notifyPlayCheck(int remainDay);

    }


    interface Presenter extends BasePresenter {
        void fetchItem(String pk, String deviceToken, String accessToken);

        void createBookmarks(String pk, String deviceToken, String accessToken);

        void fetchItemRelate(String pk, String deviceToken, String accessToken);

        void removeBookmarks(String pk, String deviceToken, String accessToken);

        void handleBookmark();

        void requestPlayCheck(String itemPk, String deviceToken, String accessToken);
    }
}
