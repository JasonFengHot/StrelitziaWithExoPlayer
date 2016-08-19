package tv.ismar.detailpage;

import tv.ismar.app.BasePresenter;
import tv.ismar.app.BaseView;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 8/19/16.
 */
public interface DetailPageContract {
    interface View extends BaseView<Presenter>{
        void loadItem(ItemEntity itemEntity);
    }


    interface Presenter extends BasePresenter{
        void  fetchItem(String pk);
    }
}
