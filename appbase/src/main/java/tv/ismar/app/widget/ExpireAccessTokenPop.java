package tv.ismar.app.widget;

import android.content.Context;

/**
 * Created by huibin on 11/17/16.
 */
public class ExpireAccessTokenPop extends ModuleMessagePopWindow {
    private static ExpireAccessTokenPop ourInstance;

    public static ExpireAccessTokenPop getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new ExpireAccessTokenPop(context);
        }
        return ourInstance;
    }

    public ExpireAccessTokenPop(Context context) {
        super(context);
    }
}
