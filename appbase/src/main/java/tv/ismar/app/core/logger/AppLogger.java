package tv.ismar.app.core.logger;

import android.net.Uri;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.db.Empty;
import tv.ismar.app.util.HardwareUtils;

/** Created by huaijie on 11/16/15. */
public class AppLogger {

    static Retrofit buildRetrofit() {
        String advHost =
                "http://"
                        + AccountSharedPrefs.getInstance()
                                .getSharedPrefs(AccountSharedPrefs.LOG_DOMAIN);
        //        String advHost = "http://10.254.0.100:8080";
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
        Retrofit retrofit =
                new Retrofit.Builder()
                        .client(client)
                        .baseUrl(appendProtocol(advHost))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
        return retrofit;
    }

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

    static void log(String data) {
        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
        String sn = accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.SN_TOKEN);
        String deviceToken = accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.DEVICE_TOKEN);
        String accessToken = accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.ACESS_TOKEN);
        String modelName = HardwareUtils.getModelName();
        String userAgent = VodUserAgent.getHttpUserAgent();
        String contentEncoding = "gzip";

        Retrofit retrofit = buildRetrofit();
        LogRequest logRequest = retrofit.create(LogRequest.class);
        Call<Empty> call =
                logRequest.doRequest(
                        userAgent, contentEncoding, sn, deviceToken, accessToken, modelName, data);
        call.enqueue(
                new Callback<Empty>() {
                    @Override
                    public void onResponse(Call<Empty> call, Response<Empty> response) {}

                    @Override
                    public void onFailure(Call<Empty> call, Throwable t) {}
                });
    }

    interface LogRequest {
        @FormUrlEncoded
        @POST("log")
        Call<Empty> doRequest(
                @Header("User-Agent") String userAgent,
                @Header("Content-Encoding") String contentEncoding,
                @Field("sn") String sn,
                @Field("deviceToken") String deviceToken,
                @Field("acessToken") String acessToken,
                @Field("modelname") String modelName,
                @Field("data") String data);
    }
}
