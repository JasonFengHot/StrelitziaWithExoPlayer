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
    public static String SN_TOKEN = "me_uslkvhq2";
    public static String DEVICE_TOKEN = "ZmZZPCQgcrhDgk2uLrTBXcq42dW1ewNUciZVBIrKmH0=";
//    public static String deviceToken = "__Ntksg9LjmpHH4Bx6wkjNKk8v6zzhQYu-erQaGzc7D0lUKTjwbH8GimsLJuRLEhaP";

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
                .addParam("device_token", DEVICE_TOKEN)
                .addParam("access_token", "2")
                .build();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("DroidSansFallback.ttf")
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
