package tv.ismar.searchpage.core.handler;

import tv.ismar.searchpage.data.http.SemanticSearchResponseEntity;

/**
 * Created by huaijie on 1/27/16.
 */
public interface HandleCallback {
    void onHandleSuccess(SemanticSearchResponseEntity entity, String rawText);
}
