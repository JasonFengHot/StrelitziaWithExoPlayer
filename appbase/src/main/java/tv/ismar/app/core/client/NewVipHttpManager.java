package tv.ismar.app.core.client;

import android.net.Uri;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tv.ismar.app.core.SimpleRestClient;


/**
 * Created by huaijie on 1/15/16.
 */
public class NewVipHttpManager {
    private static final int DEFAULT_CONNECT_TIMEOUT = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5;

    private static NewVipHttpManager ourInstance = new NewVipHttpManager();


    private OkHttpClient client;

    public Retrofit resetAdapter_SKY;

    public static NewVipHttpManager getInstance() {
        return ourInstance;
    }

    private NewVipHttpManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();


        resetAdapter_SKY = new Retrofit.Builder()
                .client(client)
                .baseUrl(appendProtocol(SimpleRestClient.root_url))
                .addConverterFactory(GsonConverterFactory.create())
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
