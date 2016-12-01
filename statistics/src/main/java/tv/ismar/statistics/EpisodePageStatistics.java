package tv.ismar.statistics;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 12/1/16.
 */

public class EpisodePageStatistics {

    public void videoEpisodeIn(ItemEntity itemEntity, String source) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put(EventProperty.TITLE, itemEntity.getTitle());
        dataCollectionProperties.put(EventProperty.ITEM, itemEntity.getPk());
        dataCollectionProperties.put(EventProperty.SOURCE, source);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DETAIL_IN, dataCollectionProperties);
    }

    public void videoEpisodeOut(ItemEntity itemEntity) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put(EventProperty.TITLE, itemEntity.getTitle());
        dataCollectionProperties.put(EventProperty.ITEM, itemEntity.getPk());
        dataCollectionProperties.put(EventProperty.TO, "return");
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DETAIL_OUT, dataCollectionProperties);
    }
}
