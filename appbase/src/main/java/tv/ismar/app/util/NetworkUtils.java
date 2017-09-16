package tv.ismar.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    /** 是否已有网络连接 */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo ethNetInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        boolean isConnected =
                (mobNetInfo != null && mobNetInfo.isConnected())
                        || (wifiNetInfo != null && wifiNetInfo.isConnected())
                        || (ethNetInfo != null && ethNetInfo.isConnected());

        return isConnected;
    }

    /** 是否wifi连接 */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.getState() == NetworkInfo.State.CONNECTED;
    }

    /** 是否3G连接 */
    public static boolean isMobile(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return info != null && info.getState() == NetworkInfo.State.CONNECTED;
    }
}
