package tv.ismar.app.network;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.app.network.exception.OnlyMobileException;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.app.util.NetworkUtils;

/**
 * Created by huibin on 8/19/16.
 */
public class HttpTrafficInterceptor implements Interceptor {
    private static final String TAG = "HttpTrafficInterceptor";
    private TrafficType mTrafficType = TrafficType.UNLIMITED;
    private Context mContext;

    public HttpTrafficInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request();
        Response response = null;
        try {
            switch (mTrafficType) {
                case UNLIMITED:
                    response = chain.proceed(request);
                    break;
                case ONLY_WIFI:
                    if (NetworkUtils.isWifi(mContext)) {
                        response = chain.proceed(request);
                    } else {
                        throw new OnlyWifiException(request.url().toString());
                    }
                    break;
                case ONLY_MOBILE:
                    if (NetworkUtils.isMobile(mContext)) {
                        response = chain.proceed(request);
                    } else {
                        throw new OnlyMobileException(request.url().toString());
                    }
                    break;
                default:
                    response = chain.proceed(request);
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
        return response;
    }

    public void setTrafficType(TrafficType trafficType) {
        mTrafficType = trafficType;
    }

    public enum TrafficType {
        UNLIMITED,
        ONLY_WIFI,
        ONLY_MOBILE
    }
}
