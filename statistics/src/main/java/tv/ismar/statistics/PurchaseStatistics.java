package tv.ismar.statistics;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;

/**
 * Created by huibin on 12/26/2016.
 */

public class PurchaseStatistics {


    public void videoExpenseClick(String item, String userid, String title, String clip) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("item", item);
        dataCollectionProperties.put("userid", userid);
        dataCollectionProperties.put("title", title);
        dataCollectionProperties.put("clip", clip);
        new NetworkUtils.DataCollectionTask().execute("video_expense_click", dataCollectionProperties);
    }


    public void videoPurchaseClick(String item, String userid, String title, String clip, String type) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("item", item);
        dataCollectionProperties.put("userid", userid);
        dataCollectionProperties.put("title", title);
        dataCollectionProperties.put("clip", clip);
        dataCollectionProperties.put("type", type);
        new NetworkUtils.DataCollectionTask().execute("video_purchase_click", dataCollectionProperties);
    }

    public void videoPurchaseOk(String item, String account, String valid, String price, String userid, String type, String clip) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("item", item);
        dataCollectionProperties.put("account", account);
        dataCollectionProperties.put("valid", valid);
        dataCollectionProperties.put("price", price);
        dataCollectionProperties.put("userid", userid);
        dataCollectionProperties.put("type", type);
        dataCollectionProperties.put("clip", clip);
        new NetworkUtils.DataCollectionTask().execute("video_purchase_ok", dataCollectionProperties);
    }

    public void videoExpense(String item, String clip, String title, String userid, String to) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("item", item);
        dataCollectionProperties.put("clip", clip);
        dataCollectionProperties.put("title", title);
        dataCollectionProperties.put("userid", userid);
        dataCollectionProperties.put("to", to);
        new NetworkUtils.DataCollectionTask().execute("video_expense", dataCollectionProperties);
    }
}
