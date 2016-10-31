package tv.ismar.app.network;

import android.net.Uri;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.VodApplication;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemList;
import tv.ismar.app.entity.SectionList;
import tv.ismar.app.entity.VideoEntity;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.AccountPlayAuthEntity;
import tv.ismar.app.network.entity.AccountsLoginEntity;
import tv.ismar.app.network.entity.ActiveEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.DpiEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PayLayerEntity;
import tv.ismar.app.network.entity.PayLayerPackageEntity;
import tv.ismar.app.network.entity.PayLayerVipEntity;
import tv.ismar.app.network.entity.PayVerifyEntity;
import tv.ismar.app.network.entity.VersionInfoV2Entity;
import tv.ismar.app.network.entity.YouHuiDingGouEntity;

/**
 * Created by huibin on 8/3/16.
 */
public interface SkyService {
    @FormUrlEncoded
    @POST("accounts/playauths/")
    Observable<AccountPlayAuthEntity> accountsPlayauths(
            @Field("timestamp") String timestamp,
            @Field("sign") String sign
    );

    @GET("api/tv/section/youhuidinggou/")
    Observable<YouHuiDingGouEntity> apiYouhuidinggou(

    );

    @GET
    Observable<ItemEntity> apiItemByUrl(
            @Url String urlP
    );

    @GET
    Observable<ItemList> getItemListChannel(
            @Url String url
    );

    @GET("api/item/{pk}/")
    Observable<ItemEntity> apiItem(
            @Path("pk") String pk
    );

    @GET("api/tv/sections/{channel}")
    Observable<SectionList> getSectionlist(
            @Path("channel") String channel
    );

    @GET("api/tv/retrieval/{channel}")
    Observable<ResponseBody> getFilters(
            @Path("channel") String channel
    );

    @GET("api/tv/filtrate/${content_model}/{filterCondition}/1/")
    Observable<ItemList> getFilterRequest(
            @Path("content_model") String channel,
            @Path("filterCondition") String filterCondition
    );

    @GET("/api/tv/filtrate/${movie}/{area}/{page}/")
    Observable<ItemList> getFilterRequestNodata(
            @Path("movie") String movie,
            @Path("area") String area,
            @Path("page") int page
    );

    @GET("api/tv/filtrate/{movie}/{genre}/{page}/")
    Observable<ResponseBody> fetchFiltrate(
            @Path("movie") String movie,
            @Path("genre") String genre,
            @Path("page") String page

    );

    @GET("api/tv/filtrate/${content_model}/{filterCondition}/{page}/")
    Observable<ItemList> getFilterRequestHaveData(
            @Path("content_model") String channel,
            @Path("filterCondition") String filterCondition,
            @Path("page") int page
    );

    @GET
    Observable<SectionList> getSections(
            @Url String url
    );

    @GET("api/histories/")
    Observable<Item[]> getHistoryByNet(
    );

    @POST("api/histories/empty/")
    Observable<ResponseBody> emptyHistory(
    );

    @GET("api/bookmarks/")
    Observable<Item[]> getBookmarks(
    );

    @POST("api/bookmarks/empty/")
    Observable<ResponseBody> emptyBookmarks(
    );

    @GET("api/tv/relate/{pk}/")
    Observable<Item[]> getRelatedArray(
            @Path("pk") Integer pk
    );

    @GET("api/tv/filtrate/${content_model}/{slug}*{template}/")
    Observable<ItemList> getRelatedItemByInfo(
            @Path("content_model") String content_model,
            @Path("slug") String slug,
            @Path("template") int template
    );

    @GET("api/tv/section/tvhome/")
    Observable<VideoEntity> getTvHome(
    );

    @GET("api/item/{pk}")
    Observable<Item> getClickItem(
            @Path("pk") int pk
    );

    @GET("api/subitem/{pk}/")
    Observable<ItemEntity.SubItem> apiSubItem(
            @Path("pk") int pk
    );

    @GET("api/{opt}/{pk}/")
    Observable<ItemEntity> apiOptItem(
            @Path("pk") String pk,
            @Path("opt") String opt

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
    @POST("trust/security/active/")
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
    @POST("trust/get_licence/")
    Observable<ResponseBody> getLicence(
            @Field("fingerprint") String fingerprint,
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("code") String code
    );

    @GET("api/dpi/")
    Observable<List<DpiEntity>> fetchDpi(

    );

    @GET
    Observable<ClipEntity> fetchMediaUrl(
            @Url String clipUrl,
            @Query("sign") String sign,
            @Query("code") String code
    );

    @FormUrlEncoded
    @POST("api/get/ad/")
    Observable<ResponseBody> fetchAdvertisement(
            @FieldMap HashMap<String, Object> paramsMap
    );

    @FormUrlEncoded
    @POST
    Observable<ResponseBody> sendPlayerLog(
            @Url String logUrl,
            @Field("sn") String sn,
            @Field("modelname") String modelname,
            @Field("data") String data
    );

    @FormUrlEncoded
    @POST("api/histories/create/")
    Observable<ResponseBody> sendPlayHistory(
            @FieldMap HashMap<String, Object> paramsMap
    );


    @FormUrlEncoded
    @POST("accounts/login/")
    Observable<AccountsLoginEntity> accountsLogin(
            @Field("username") String userName,
            @Field("auth_number") String authNumber
    );

    @FormUrlEncoded
    @POST("accounts/auth/")
    Observable<ResponseBody> accountsAuth(
            @Field("username") String userName
    );

    @GET("accounts/balance/")
    Observable<AccountBalanceEntity> accountsBalance(
    );


    @GET("api/histories/")
    Observable<ResponseBody> apiHistories(
    );

    @GET("api/paylayer/{item_id}/")
    Observable<PayLayerEntity> apiPaylayer(
            @Path("item_id") String itemId
    );

    @GET("api/paylayer/vip/{cpid}/")
    Observable<PayLayerVipEntity> apiPaylayerVip(
            @Path("cpid") String cpid,
            @Query("item_id") String itemId
    );

    @GET("api/paylayer/package/{package_id}/")
    Observable<PayLayerPackageEntity> apiPaylayerPackage(
            @Path("package_id") String packageId
    );

    @POST("accounts/combine/")
    Observable<ResponseBody> accountsCombine(
            @Field("sharp_bestv") String sharpBestv,
            @Field("timestamp") String timestamp,
            @Field("sign") String sign
    );


    @FormUrlEncoded
    @POST("api/play/check/")
    Call<ResponseBody> playcheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );


    @FormUrlEncoded
    @POST("api/order/purchase/")
    Call<ResponseBody> orderpurchase(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );

    @FormUrlEncoded
    @POST("api/order/create/")
    Observable<ResponseBody> apiOrderCreate(
            @Field("wares_id") String waresId,
            @Field("wares_type") String waresType,
            @Field("source") String source,
            @Field("timestamp") String timestamp,
            @Field("sign") String sign
    );


    @FormUrlEncoded
    @POST("https://order.tvxio.com/api/pay/verify/")
    Observable<PayVerifyEntity> apiPayVerify(
            @Field("card_secret") String card_secret,
            @Field("app_name") String app_name,
            @Field("user") String user,
            @Field("user_id") String user_id,
            @Field("timestamp") String timestamp,
            @Field("sid") String sid
    );


//    @GET
//    Observable<HomePagerEntity> fetchHomePage(
//            @Url String url
//    );
//


    @FormUrlEncoded
    @POST("api/play/check/")
    Call<ResponseBody> playCheck(
            @Field("item") String item,
            @Field("package") String pkg,
            @Field("subitem") String subItem
    );

    @GET("api/v2/upgrade/")
    Observable<VersionInfoV2Entity> appUpgrade(
            @Query("sn") String sn,
            @Query("manu") String manu,
            @Query("app") String app,
            @Query("modelname") String modelName,
            @Query("loc") String location,
            @Query("ver") int versionCode
    );

    @GET
    @Streaming
    Observable<ResponseBody> download(
            @Url String url,
            @Header("RANGE") String range
    );
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
        private volatile static ServiceManager serviceManager;
        private static final int DEFAULT_CONNECT_TIMEOUT = 6;
        private static final int DEFAULT_READ_TIMEOUT = 15;
        private SkyService mSkyService;
        private SkyService adSkyService;
        private SkyService upgradeService;


        private ServiceManager() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            final OkHttpClient mClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(VodApplication.getHttpParamsInterceptor())
                    .addNetworkInterceptor(VodApplication.getHttpTrafficInterceptor())
                    .addInterceptor(interceptor)
                    .build();

            final CountDownLatch latch = new CountDownLatch(1);
            final String[] domain = new String[3];
            new Thread() {
                @Override
                public void run() {
                    domain[0] = IsmartvActivator.getInstance().getApiDomain();
                    domain[1] = IsmartvActivator.getInstance().getAdDomain();
                    domain[2] = IsmartvActivator.getInstance().getUpgradeDomain();
                    latch.countDown();
                }
            }.start();

            try {
                latch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[0]))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            mSkyService = retrofit.create(SkyService.class);

            Retrofit adRetrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[1]))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            adSkyService = adRetrofit.create(SkyService.class);

            Retrofit upgradeRetrofit = new Retrofit.Builder()
                    .baseUrl(appendProtocol(domain[2]))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();

            upgradeService = upgradeRetrofit.create(SkyService.class);

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
            synchronized (ServiceManager.class) {
                if (serviceManager == null) {
                    serviceManager = new ServiceManager();
                }
            }
            return serviceManager.mSkyService;
        }

        public static SkyService getAdService() {
            synchronized (ServiceManager.class) {
                if (serviceManager == null) {
                    serviceManager = new ServiceManager();
                }
            }
            return serviceManager.adSkyService;
        }

        public static SkyService getUpgradeService() {
            synchronized (ServiceManager.class) {
                if (serviceManager == null) {
                    serviceManager = new ServiceManager();
                }
            }
            return serviceManager.upgradeService;
        }
    }

}
