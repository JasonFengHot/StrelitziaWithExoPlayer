package tv.ismar.app;

import android.content.Context;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.app.network.HttpParamsInterceptor;
import tv.ismar.app.network.HttpTrafficInterceptor;

/**
 * Created by beaver on 16-8-19.
 */
public class VodApplication extends Application {
    private static HttpTrafficInterceptor mHttpTrafficInterceptor;
    private static HttpParamsInterceptor mHttpParamsInterceptor;

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        mHttpTrafficInterceptor = new HttpTrafficInterceptor(this);
        mHttpTrafficInterceptor.setTrafficType(HttpTrafficInterceptor.TrafficType.UNLIMITED);
        mHttpParamsInterceptor = new HttpParamsInterceptor.Builder()
                .addParam("device_token", "1")
                .addParam("access_token", "2")
                .build();
    }

    public static VodApplication get(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

    public static HttpTrafficInterceptor getHttpTrafficInterceptor() {
        return mHttpTrafficInterceptor;
    }

    public static HttpParamsInterceptor getHttpParamsInterceptor() {
        return mHttpParamsInterceptor;
    }
}
