package tv.ismar.searchpage.core.handler;

/**
 * Created by huaijie on 4/27/16.
 */
public class OtherTvInstructionHandler {

    private static final String TAG = "TvInstructionHandler";

    public OtherTvInstructionHandler(String settingType, TvInstructionCallback tvInstructionCallback) {
        tvInstructionCallback.onCallback(settingType);
    }
}
