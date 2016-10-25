package tv.ismar.app.core.client;


import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;


/**
 * Created by huaijie on 1/18/16.
 */
public class HttpManager {
    private static final int DEFAULT_CONNECT_TIMEOUT = 6;
    private static final int DEFAULT_READ_TIMEOUT = 15;

    private static final String SKY_HOST = "http://media.lily.tvxio.com";

    public Retrofit mSkyRetrofit;
    public Retrofit mCacheSkyRetrofit;
    public OkHttpClient mClient;
    public OkHttpClient mCacheClient;
    public Retrofit media_lily_Retrofit;

    private static HttpManager ourInstance ;

    private static Context mContext;

    public static HttpManager getInstance() {
        if (ourInstance==null){
            ourInstance = new HttpManager();
        }
        return ourInstance;
    }

    public static void initialize(Context context){
        mContext = context;
    }

    private HttpManager() {
        getHttp();
    }

    public static void clear(){
        ourInstance = null;
    }

    public void getHttp() {
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        mClient = new OkHttpClient.Builder()
//                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
//                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
//                .addInterceptor(interceptor)
//                .build();
//
//        File cacheFile = new File(mContext.getCacheDir(), "okhttp_cache");
//        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
//        mCacheClient = new OkHttpClient.Builder()
//                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
//                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
//                .addInterceptor(interceptor)
//                .addInterceptor(CACHE_CONTROL_INTERCEPTOR)
//                .addNetworkInterceptor(CACHE_CONTROL_INTERCEPTOR)
//                .cache(cache)
//                .build();
//
//
//        media_lily_Retrofit = new Retrofit.Builder()
//                .client(mClient)
//                .baseUrl(SKY_HOST)
//                .build();
//
//        mSkyRetrofit = new Retrofit.Builder()
//                .client(mClient)
//                .baseUrl(appendProtocol(SimpleRestClient.root_url))
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        mCacheSkyRetrofit = new Retrofit.Builder()
//                .client(mCacheClient)
//                .baseUrl(appendProtocol(SimpleRestClient.root_url))
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();

    }


    private final Interceptor CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetUtils.isConnected(mContext)) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }
            Response originalResponse = chain.proceed(request);
            if (NetUtils.isConnected(mContext)) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            } else {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                        .removeHeader("Pragma")
                        .build();
            }
        }
    };

    public static String appendProtocol(String host) {
        Uri uri = Uri.parse(host);
        String url = uri.toString();
        if (!uri.toString().startsWith("http://") && !uri.toString().startsWith("https://")) {
            url = "http://" + host;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }
}
