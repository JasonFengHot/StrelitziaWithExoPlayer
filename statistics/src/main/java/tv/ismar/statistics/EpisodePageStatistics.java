package tv.ismar.statistics;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 12/1/16.
 */

public class EpisodePageStatistics {

    public void videoEpisodeIn(ItemEntity itemEntity) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put(EventProperty.ITEM, itemEntity.getPk());
        dataCollectionProperties.put(EventProperty.TITLE, itemEntity.getTitle());
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DRAMALIST_IN, dataCollectionProperties);
    }

    public void videoEpisodeOut(ItemEntity itemEntity,int subitem,String to) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put(EventProperty.ITEM, itemEntity.getPk());
        dataCollectionProperties.put(EventProperty.TITLE, itemEntity.getTitle());
        dataCollectionProperties.put(EventProperty.SUBITEM, itemEntity.getSubitems()[subitem].getPk());
        dataCollectionProperties.put(EventProperty.TO, to);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DRAMALIST_OUT, dataCollectionProperties);
    }
}
