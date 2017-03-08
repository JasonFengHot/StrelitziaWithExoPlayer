package tv.ismar.app;

import android.util.Base64;

import tv.ismar.account.core.rsa.SkyAESTool2;

public class AccessProxy {


    public static String AESDecrypt(String url, String device_token) {
        return SkyAESTool2.decrypt(device_token.substring(0, 16), Base64.decode(url, Base64.URL_SAFE));
    }

}
