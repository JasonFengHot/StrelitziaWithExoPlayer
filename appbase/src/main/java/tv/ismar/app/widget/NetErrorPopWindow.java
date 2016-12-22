package tv.ismar.app.widget;

import android.content.Context;

/**
 * Created by huibin on 11/17/16.
 */
public class NetErrorPopWindow extends ModuleMessagePopWindow {
    private static NetErrorPopWindow ourInstance;

    public static NetErrorPopWindow getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new NetErrorPopWindow(context);
        }
        return ourInstance;
    }

    public NetErrorPopWindow(Context context) {
       super(context);
    }
}
