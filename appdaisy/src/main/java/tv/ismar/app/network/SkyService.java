package tv.ismar.app.network;

import android.net.Uri;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
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
import tv.ismar.app.VodApplication;
import tv.ismar.app.network.entity.ActiveEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.DpiEntity;
import tv.ismar.app.network.entity.ItemEntity;

/**
 * Created by huibin on 8/3/16.
 */
public interface SkyService {

    @GET
    Observable<ItemEntity> apiItemByUrl(
            @Url String url
    );


    @GET("api/item/{pk}/")
    Observable<ItemEntity> apiItem(
            @Path("pk") String pk
    );

    @FormUrlEncoded
    @POST("api/bookmarks/create/")
    Observable<ResponseBody> apiBookmarksCreate(
            @Field("item") String item
    );

    @FormUrlEncoded
    @POST("api/bookmarks/remove/")
    Observable<ResponseBody> apiBookmarksRemove(
            @Field("item") String item
    );

    @GET("api/tv/relate/{pk}/")
    Observable<ItemEntity[]> apiTvRelate(
            @Path("pk") String item
    );

    @FormUrlEncoded
    @POST("api/play/check/")
    Observable<ResponseBody> apiPlayCheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );

    @FormUrlEncoded
    @POST("/trust/security/active/")
    Observable<ActiveEntity> securityActive(
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("kind") String kind,
            @Field("version") String version,
            @Field("sign") String sign,
            @Field("fingerprint") String fingerprint,
            @Field("api_version") String api_version,
            @Field("info") String deviceInfo
    );

    @FormUrlEncoded
    @POST("/trust/get_licence/")
    Observable<ResponseBody> getLicence(
            @Field("fingerprint") String fingerprint,
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("code") String code
    );

    @GET("/api/dpi/")
    Observable<List<DpiEntity>> fetchDpi(

    );

    @GET
    Observable<ClipEntity> fetchMediaUrl(
            @Url String clipUrl,
            @Query("device_token") String deviceToken,
            @Query("access_token") String access_token,
            @Query("sign") String sign,
            @Query("code") String code
    );

//    @GET
//    Observable<HomePagerEntity> fetchHomePage(
//            @Url String url
//    );
//


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
                    .addNetworkInterceptor(VodApplication.getHttpParamsInterceptor())
                    .addNetworkInterceptor(VodApplication.getHttpTrafficInterceptor())
                    .addInterceptor(interceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol("http://sky.tvxio.bestv.com.cn/v3_0/SKY2/tou/"))
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
