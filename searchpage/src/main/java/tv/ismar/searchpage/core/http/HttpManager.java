package tv.ismar.searchpage.core.http;

import android.net.Uri;

import java.util.concurrent.TimeUnit;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.gson.GsonConverterFactory;
import tv.ismar.searchpage.MainApplication;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by huaijie on 1/15/16.
 */
public class HttpManager {
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;

    private static HttpManager ourInstance = new HttpManager();
    private static final String MEDIA_LILY_HOST = "http://media.lily.tvxio.com";


    public OkHttpClient client;

    public Retrofit resetAdapter_APP_UPDATE;
    public Retrofit resetAdapter_SKY;
    public Retrofit resetAdapter_MEDIA_LILY;

    public static HttpManager getInstance() {
        return ourInstance;
    }

    private HttpManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

//        resetAdapter_APP_UPDATE = new Retrofit.Builder()
//                .client(client)
//                .baseUrl(appendProtocol(MainApplication.getAppUpdateDomain()))
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();

        resetAdapter_SKY = new Retrofit.Builder()
                .client(client)
                .baseUrl(appendProtocol(MainApplication.getApiDomain()))
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        resetAdapter_MEDIA_LILY = new Retrofit.Builder()
                .client(client)
                .baseUrl(MEDIA_LILY_HOST)
                .build();
    }


    private String appendProtocol(String host) {
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
