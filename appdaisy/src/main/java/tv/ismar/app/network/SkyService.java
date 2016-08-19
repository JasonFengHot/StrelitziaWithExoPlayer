package tv.ismar.app.network;

import android.net.Uri;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 8/3/16.
 */
public interface SkyService {

    @GET
    Observable<ItemEntity> apiItemByUrl(
            @Url String url
    );


    @GET("/api/item/{pk}/")
    Observable<ItemEntity> apiItem(
            @Path("pk") String pk,
            @Query("access_token") String accessToken,
            @Query("device_token") String deviceToken
    );

    @FormUrlEncoded
    @POST("/api/bookmarks/create/")
    Observable<ResponseBody> apiBookmarksCreate(
            @Field("item") String item,
            @Field("access_token") String accessToken,
            @Field("device_token") String deviceToken
    );

    @FormUrlEncoded
    @POST("/api/bookmarks/remove/")
    Observable<ResponseBody> apiBookmarksRemove(
            @Field("item") String item,
            @Field("access_token") String accessToken,
            @Field("device_token") String deviceToken
    );

    @GET("/api/tv/relate/{pk}/")
    Observable<ItemEntity[]> apiTvRelate(
            @Query("pk") String item,
            @Query("access_token") String accessToken,
            @Query("device_token") String deviceToken
    );

    @FormUrlEncoded
    @POST("/api/play/check/")
    Observable<ResponseBody> apiPlayCheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem,
            @Field("device_token") String deviceToken,
            @Field("access_token") String accessToken
    );

//    @GET
//    Observable<HomePagerEntity> fetchHomePage(
//            @Url String url
//    );
//
//    @GET("/api/dpi/")
//    Observable<List<DpiEntity>> fetchDpi(
//
//    );

//    @FormUrlEncoded
//    @POST("/api/play/check/")
//    Call<ResponseBody> playCheck(
//            @Field("item") String item,
//            @Field("package") String pkg,
//            @Field("subitem") String subItem,
//            @Field("device_token") String deviceToken,
//            @Field("access_token") String accessToken
//    );
//
//    @GET("/api/package/relate/{pkg}/")
//    Observable<Item[]> packageRelate(
//            @Path("pkg")
//            String pkg,
//            @Query("device_token")
//            String deviceToken,
//            @Query("access_token")
//            String accessToken);

    class ServiceManager {
        private static ServiceManager serviceManager;
        private static final int DEFAULT_CONNECT_TIMEOUT = 6;
        private static final int DEFAULT_READ_TIMEOUT = 15;
        private SkyService mSkyService;

        private ServiceManager() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient mClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(""))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            mSkyService = retrofit.create(SkyService.class);
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

        public static SkyService getService() {
            if (serviceManager == null) {
                serviceManager = new ServiceManager();
            }
            return serviceManager.mSkyService;
        }
    }
}
