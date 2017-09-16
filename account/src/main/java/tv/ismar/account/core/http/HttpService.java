package tv.ismar.account.core.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import tv.ismar.account.data.ResultEntity;

/** Created by huaijie on 1/14/16. */
public interface HttpService {

    @Headers({"Pragma: no-cache", "Cache-Control: no-cache"})
    @FormUrlEncoded
    @POST("/trust/security/active/")
    Call<ResultEntity> trustSecurityActive(
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("kind") String kind,
            @Field("version") String version,
            @Field("sign") String sign,
            @Field("fingerprint") String fingerprint,
            @Field("api_version") String api_version,
            @Field("info") String deviceInfo);

    @FormUrlEncoded
    @POST("/trust/get_licence/")
    Call<ResponseBody> trustGetlicence(
            @Field("fingerprint") String fingerprint,
            @Field("sn") String sn,
            @Field("manufacture") String manufacture,
            @Field("code") String code);
}
