package tv.ismar.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by beaver on 16-8-19.
 */
public class VodApplication extends Application {

    public static VodApplication get(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

}
