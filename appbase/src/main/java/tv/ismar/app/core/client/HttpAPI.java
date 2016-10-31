package tv.ismar.app.core.client;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tv.ismar.app.core.usercenter.AccountPlayAuthEntity;
import tv.ismar.app.entity.ChannelEntity;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.network.entity.AccountBalanceEntity;

/**
 * Created by huaijie on 1/18/16.
 */
public class HttpAPI {

    public interface WeatherAPI {
        @GET("{geoId}.xml")
        Call<ResponseBody> doRequest(
                @Path("geoId") String geoId
        );
    }

    public interface AccountsPlayauths {
        @FormUrlEncoded
        @POST("accounts/playauths/")
        Call<AccountPlayAuthEntity> doRequest(
                @Field("device_token") String deviceToken,
                @Field("access_token") String accessToken,
                @Field("timestamp") String timestamp,
                @Field("sign") String sign
        );
    }

    public interface AccountsBalance {
        @GET("accounts/balance/")
        Call<AccountBalanceEntity> doRequest(
                @Query("device_token") String deviceToken,
                @Query("access_token") String accessToken
        );
    }

    public interface TvChannels {
        @Headers("Cache-Control: public, max-age=5")
        @GET("api/tv/channels/")
        Call<ChannelEntity[]> doRequest();
    }


    public interface TvHomepageTop {
        @Headers("Cache-Control: public, max-age=5")
        @GET("api/tv/homepage/top/")
        Call<HomePagerEntity> doRequest();
    }

    public interface CdnInfo {
        @GET("http://wx.api.tvxio.com/shipinkefu/getCdninfo")
        Call<ResponseBody> doRequest(
                @Query("actiontype") String actionType
        );
    }

    public interface IpLookup {
        @GET("http://lily.tvxio.com/iplookup")
        Call<ResponseBody> doRequest();
    }
}
