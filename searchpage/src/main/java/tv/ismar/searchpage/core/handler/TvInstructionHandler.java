package tv.ismar.searchpage.core.handler;

import android.util.Log;

import com.google.gson.JsonObject;

/**
 * Created by huaijie on 4/27/16.
 */
public class TvInstructionHandler {
    private static final String TAG = "TvInstructionHandler";

    public TvInstructionHandler(JsonObject o, TvInstructionCallback tvInstructionCallback) {
        Log.i(TAG, "json object : " + o.toString());
        String settingType = o.getAsJsonObject().get("object").getAsJsonObject().get("settingtype").toString().replace("\"", "");
        tvInstructionCallback.onCallback(settingType);
    }
}
