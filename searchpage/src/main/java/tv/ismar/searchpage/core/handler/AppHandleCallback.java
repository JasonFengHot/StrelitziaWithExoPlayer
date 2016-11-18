package tv.ismar.searchpage.core.handler;

import tv.ismar.searchpage.data.http.AppSearchResponseEntity;

/**
 * Created by huaijie on 1/28/16.
 */
public interface AppHandleCallback {
    void onAppHandleSuccess(AppSearchResponseEntity entity, String jsonData);
}
