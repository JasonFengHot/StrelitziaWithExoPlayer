package tv.ismar.detailpage;

import android.app.Activity;
import android.content.Context;

import tv.ismar.app.BasePresenter;
import tv.ismar.app.BaseView;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;

/**
 * Created by huibin on 8/19/16.
 */
public interface DetailPageContract {
    interface View extends BaseView<Presenter> {
        void loadItem(ItemEntity itemEntity);

        void loadItemRelate(ItemEntity[] itemEntities);

        void notifyPlayCheck(PlayCheckEntity playCheckEntity);

        void notifyBookmark(boolean mark, boolean isSuccess);

        Activity getActivity();

        void onError();
    }


    interface Presenter extends BasePresenter {
        void fetchItem(String pk);

        void createBookmarks(String pk);

        void fetchItemRelate(String pk);

        void removeBookmarks(String pk);

        void handleBookmark();

        void handlePlay();

        void handlePurchase();

        void requestPlayCheck(String itemPk);

        void handleMoreRelate();

        void handleEpisode();
    }
}
