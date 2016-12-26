package tv.ismar.statistics;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 12/1/16.
 */

public class DetailPageStatistics {

    public void videoDetailIn(ItemEntity itemEntity, String source) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put(EventProperty.TITLE, itemEntity.getTitle());
        dataCollectionProperties.put(EventProperty.ITEM, itemEntity.getPk());
        dataCollectionProperties.put(EventProperty.SOURCE, source);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DETAIL_IN, dataCollectionProperties);
    }

    public void videoDetailOut(ItemEntity itemEntity,String to) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put(EventProperty.ITEM, itemEntity.getPk());
        dataCollectionProperties.put(EventProperty.SUBITEM, itemEntity.getItemPk());
        dataCollectionProperties.put(EventProperty.TITLE, itemEntity.getTitle());
        dataCollectionProperties.put(EventProperty.TO,to);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_DETAIL_OUT, dataCollectionProperties);
    }

    public void videoRelateClick(int itemPK, ItemEntity relatedItemEntity) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(EventProperty.ITEM, itemPK);
        properties.put(EventProperty.TO_ITEM, relatedItemEntity.getPk());
        properties.put(EventProperty.TO_TITLE, relatedItemEntity.getTitle());
        properties.put(EventProperty.TO_SUBITEM, relatedItemEntity.getItemPk());
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.VIDEO_RELATE, properties);
    }
    public void packageDetailIn(String  title, String source) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put(EventProperty.TITLE, title);
        dataCollectionProperties.put(EventProperty.SOURCE, source);
        new NetworkUtils.DataCollectionTask().execute(NetworkUtils.PACKAGE_DETAIL_IN, dataCollectionProperties);
    }
}
