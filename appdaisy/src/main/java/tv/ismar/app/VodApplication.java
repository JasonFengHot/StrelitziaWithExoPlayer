package tv.ismar.app;

import android.content.Context;

import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.network.HttpParamsInterceptor;
import tv.ismar.app.network.HttpTrafficInterceptor;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Picasso picasso = new Picasso.Builder(this).executor(executorService).build();
        Picasso.setSingletonInstance(picasso);

        mHttpTrafficInterceptor = new HttpTrafficInterceptor(this);
        mHttpTrafficInterceptor.setTrafficType(HttpTrafficInterceptor.TrafficType.UNLIMITED);
        mHttpParamsInterceptor = new HttpParamsInterceptor.Builder()
                .addParam("device_token", "1")
                .addParam("access_token", "2")
                .build();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("DroidSansFallback.ttf")
                        .setDefaultFontPath("MONACO.TTF")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
        VipMark.getInstance();
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
