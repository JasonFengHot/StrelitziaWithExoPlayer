package tv.ismar.app.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by huibin on 8/19/16.
 */
public class HttpTrafficInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) {
        Response response = new Response.Builder()
                .build();

        return response;
    }
}
