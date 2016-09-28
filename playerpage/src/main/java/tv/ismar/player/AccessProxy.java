package tv.ismar.player;

import android.util.Base64;

import com.qiyi.sdk.player.BitStream;
import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.SdkVideo;

import org.json.JSONObject;

import tv.ismar.account.core.rsa.SkyAESTool2;

public class AccessProxy {

    public static IMedia getQiYiInfo(String content, BitStream definition) {
        IMedia qiyiInfo = null;
        JSONObject json;
        try {
            json = new JSONObject(content);
            String info = json.getString("iqiyi_4_0");
            String[] array = info.split(":");
            if (json.has("is_vip")) {
                boolean isvip = json.getBoolean("is_vip");
                qiyiInfo = new SdkVideo(array[0], array[1], array[2], isvip);
            } else {
                qiyiInfo = new SdkVideo(array[0], array[1], array[2], false);
            }
            // qiyiInfo = new SdkVideo("202153901", "308529000",
            // "8d301d7723586e7a0e1ecb778ada0cb5",Definition.DEFINITON_1080P);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return qiyiInfo;
    }

    public static String AESDecrypt(String url, String device_token) {
        return SkyAESTool2.decrypt(device_token.substring(0, 16), Base64.decode(url, Base64.URL_SAFE));
    }

}
