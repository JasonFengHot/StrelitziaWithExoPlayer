package tv.ismar.searchpage.core.handler;

import java.util.List;

import tv.ismar.searchpage.data.http.IndicatorResponseEntity;

/**
 * Created by huaijie on 2016/1/30.
 */
public interface MultiHandlerCallback {
    void onMultiHandle(List<IndicatorResponseEntity> list, String rawText);
}
