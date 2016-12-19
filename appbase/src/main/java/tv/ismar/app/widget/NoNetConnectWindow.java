package tv.ismar.app.widget;

import android.content.Context;

/**
 * Created by liucan on 2016/12/16.
 */

public class NoNetConnectWindow extends NoNetModuleMessagePop {
    private static NoNetConnectWindow ourInstance;
    public static NoNetConnectWindow getInstance(Context context){
        if (ourInstance == null) {
            ourInstance = new NoNetConnectWindow(context);
        }
        return ourInstance;
    }
    public NoNetConnectWindow(Context context){
        super(context);
    }

}
