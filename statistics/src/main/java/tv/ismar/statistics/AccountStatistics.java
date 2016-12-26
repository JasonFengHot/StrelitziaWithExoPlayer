package tv.ismar.statistics;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;

/**
 * Created by huibin on 12/26/2016.
 */

public class AccountStatistics {

    public void userLogin(String userid) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("userid", userid);
        new NetworkUtils.DataCollectionTask().execute("user_login", dataCollectionProperties);
    }
}
