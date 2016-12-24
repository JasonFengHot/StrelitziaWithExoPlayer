package tv.ismar.statistics;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;

/**
 * Created by huibin on 12/24/2016.
 */

public class UpgradeStatistics {

    public void upgradeDownload(String current, String firmware) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("current", current);
        dataCollectionProperties.put("firmware", firmware);
        new NetworkUtils.DataCollectionTask().execute("upgrade_download", dataCollectionProperties);
    }

    public void upgradeInstall(String firmware) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("firmware", firmware);
        new NetworkUtils.DataCollectionTask().execute("upgrade_install", dataCollectionProperties);
    }
}
