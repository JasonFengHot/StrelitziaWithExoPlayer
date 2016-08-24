package tv.ismar.app;

import android.content.Context;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;

/**
 * Created by beaver on 16-8-19.
 */
public class VodApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }

    public static VodApplication get(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

}
