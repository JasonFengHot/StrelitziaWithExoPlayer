package tv.ismar.app.core.client;

import android.net.Uri;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.entity.Item;
import tv.ismar.app.network.entity.DpiEntity;

/**
 * Created by huibin on 8/3/16.
 */
public interface SkyService {

    @GET
    Observable<Item> fetchItemByUrl(
            @Url String url
    );

    @GET
    Observable<HomePagerEntity> fetchHomePage(
            @Url String url
    );

    @GET("api/dpi/")
    Observable<List<DpiEntity>> fetchDpi(

    );

    @FormUrlEncoded
    @POST("api/play/check/")
    Call<ResponseBody> playCheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem,
            @Field("device_token") String deviceToken,
            @Field("access_token") String accessToken
    );

    @GET("api/package/relate/{pkg}/")
    Observable<Item[]> packageRelate(
            @Path("pkg")
            String pkg,
            @Query("device_token")
            String deviceToken,
            @Query("access_token")
            String accessToken);

    class Factory {
        public static SkyService create(String baseUrl) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(baseUrl))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(HttpClient.getInstance().mClient)
                    .build();
            return retrofit.create(SkyService.class);
        }

        private static String appendProtocol(String host) {
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


    class HttpClient {
        private static HttpClient mHttpClient;
        private OkHttpClient mClient;

        private static final int DEFAULT_CONNECT_TIMEOUT = 6;
        private static final int DEFAULT_READ_TIMEOUT = 15;

        private HttpClient() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            mClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .build();
        }

        public static HttpClient getInstance() {
            if (mHttpClient == null) {
                mHttpClient = new HttpClient();
            }
            return mHttpClient;
        }
    }
}
