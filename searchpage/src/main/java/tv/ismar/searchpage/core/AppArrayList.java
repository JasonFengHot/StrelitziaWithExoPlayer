package tv.ismar.searchpage.core;

import java.util.ArrayList;

import tv.ismar.searchpage.data.http.AppSearchObjectEntity;

/**
 * Created by huaijie on 3/16/16.
 */
public class AppArrayList extends ArrayList<AppSearchObjectEntity> {


    @Override
    public boolean add(AppSearchObjectEntity object) {
        int hasItemIndex = -1;

        for (int i = 0; i < size(); i++) {
            if (get(i).getCaption().equals(object.getCaption())) {
                hasItemIndex = i;
            }
        }

        if (hasItemIndex != -1) {
            if (object.isLocal()) {
                get(hasItemIndex).setIsLocal(true);

            }
            return false;
        } else {
            return super.add(object);
        }
    }
}
