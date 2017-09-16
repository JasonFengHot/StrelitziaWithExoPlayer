package tv.ismar.app.t;

import android.content.Context;
import android.util.Log;

import com.ismartv.api.AESDemo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.bean.ClipInfo;
import tv.ismar.app.core.SimpleRestClient;

// import com.pplive.android.player.PlayCodeUtil;
// import com.qiyi.sdk.player.BitStream;
// import com.qiyi.sdk.player.IMedia;
// import com.qiyi.sdk.player.SdkVideo;

public class AccessProxy {

    private static final String token = "?access_token=";
    private static final String key = "&sign=";
    private static final String keyCrypt = "smartvdefaultkey";
    private static String myDeviceType = "";
    private static String myDeviceVersion = "";
    private static String mySN = "";
    private static String userAgent = "";
    private static String result = "";

    public static void init(String deviceType, String deviceVersion, String sn) {
        if (deviceType != null) myDeviceType = deviceType.replace(" ", "_");
        if (deviceVersion != null) myDeviceVersion = deviceVersion.replace(" ", "_");
        if (sn != null) mySN = sn;
        userAgent =
                (new StringBuilder(String.valueOf(myDeviceType)))
                        .append("/")
                        .append(myDeviceVersion)
                        .append(" ")
                        .append(mySN)
                        .append(" thirdpartyid")
                        .toString();
    }

    public static ClipInfo parse(String clipUrl, String access_token, Context context) {
        getStream(getFullUrl(clipUrl, access_token));
        return jsonToObject(result);
    }

    public static String getvVideoClipInfo() {
        return result;
    }

    public static ClipInfo getIsmartvClipInfo(String content) {
        return jsonToObject(content);
    }

    //	public static IMedia getQiYiInfo(String content,BitStream definition) {
    //		IMedia qiyiInfo = null;
    //		JSONObject json;
    //		try {
    //			json = new JSONObject(content);
    //			String info = json.getString("iqiyi_4_0");
    //			String[] array = info.split(":");
    //			if(json.has("is_vip")){
    //				boolean isvip = json.getBoolean("is_vip");
    //				qiyiInfo = new SdkVideo(array[0], array[1],array[2],isvip);
    //			}else{
    //				qiyiInfo = new SdkVideo(array[0], array[1],array[2],false);
    //			}
    //			// qiyiInfo = new SdkVideo("202153901", "308529000",
    //			// "8d301d7723586e7a0e1ecb778ada0cb5",Definition.DEFINITON_1080P);
    //		} catch (Exception e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		}
    //		return qiyiInfo;
    //	}

    public static String getVideoInfo(String clipUrl, String access_token) {
        getStream(getFullUrl(clipUrl, access_token));
        JSONObject json;
        String info = "";
        try {
            if (!"".equals(result)) {
                json = new JSONObject(result);
                if (json.has("iqiyi_4_0")) {
                    return "iqiyi";
                } else {
                    info = "ismartv";
                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return info;
    }

    public static void destroy() {}

    private static String getFullUrl(String url, String access_token) {
        StringBuffer buffer = new StringBuffer(String.valueOf(url));
        if (SimpleRestClient.device_token == null || "".equals(SimpleRestClient.device_token)) {
            //            VodApplication.setDevice_Token();
        }
        buffer.append("?access_token=")
                .append(IsmartvActivator.getInstance().getAuthToken())
                .append("&device_token=")
                .append(SimpleRestClient.device_token)
                .append("&sign=")
                .append(getAES(access_token))
                .append("&code=1")
                .toString();
        return buffer.toString();
    }

    private static String getAES(String access_token) {
        String result = null;
        String contents =
                (new StringBuilder(String.valueOf(new Date(TrueTime.now().getTime()).getTime())))
                        .append(mySN)
                        .toString();
        if (access_token != null && access_token.length() > 0) {
            if (access_token.length() > 15) {
                result = AESDemo.encrypttoStr(contents, access_token.substring(0, 16));
            } else {
                int leng = 16 - access_token.length();
                for (int i = 0; i < leng; i++)
                    access_token =
                            (new StringBuilder(String.valueOf(access_token)))
                                    .append("0")
                                    .toString();

                result = AESDemo.encrypttoStr(contents, access_token.substring(0, 16));
            }
        } else {
            result = AESDemo.encrypttoStr(contents, keyCrypt); // 1422928853725001122334455
        }
        return result;
    }

    private static void getStream(String full_url) {
        int i = 0;
        HttpURLConnection httpConn = null;
        InputStreamReader inputStreamReader = null;
        do
            try {
                URL connURL = new URL(full_url);
                Log.v("aaaa", "url = " + full_url);
                httpConn = (HttpURLConnection) connURL.openConnection();
                httpConn.setRequestProperty("Accept", "application/json");
                httpConn.setRequestProperty("User-Agent", userAgent);
                httpConn.setConnectTimeout(10000);
                httpConn.setReadTimeout(10000);
                httpConn.connect();
                inputStreamReader = new InputStreamReader(httpConn.getInputStream(), "UTF-8");
                result = readJSONString(inputStreamReader);
                inputStreamReader.close();
                httpConn.disconnect();
                break;
            } catch (Exception e) {
                i++;
                result = "";
                Log.e("Exception", e.toString());
            }
        while (i < 3);
    }

    private static String readJSONString(InputStreamReader requestReader) {
        StringBuffer json = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(requestReader);
            while ((line = reader.readLine()) != null) json.append(line);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return json.toString();
    }

    private static ClipInfo jsonToObject(String myjson) {
        ClipInfo ci = null;
        if (myjson == null || myjson.length() <= 0) return ci;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(myjson);
            ci = new ClipInfo();
            String adaptive = "";
            String high = "";
            String low = "";
            String ultra = "";
            String medium = "";
            String normal = "";
            String iqiyi_4_0 = "";
            if (jsonObject.has("adaptive")) adaptive = jsonObject.getString("adaptive");
            if (jsonObject.has("high")) high = jsonObject.getString("high");
            if (jsonObject.has("low")) low = jsonObject.getString("low");
            if (jsonObject.has("ultra")) ultra = jsonObject.getString("ultra");
            if (jsonObject.has("medium")) medium = jsonObject.getString("medium");
            if (jsonObject.has("normal")) normal = jsonObject.getString("normal");
            if (jsonObject.has("iqiyi_4_0")) iqiyi_4_0 = jsonObject.getString("iqiyi_4_0");
            if (adaptive != "null") {
                adaptive = AES_decrypt(adaptive);
            }
            if (jsonObject.has("is_vip")) {
                ci.setIs_vip(jsonObject.getBoolean("is_vip"));
            }
            if (high != "null") {
                high = AES_decrypt(high);
            }
            if (low != "null") {
                low = AES_decrypt(low);
            }
            if (ultra != "null") {
                ultra = AES_decrypt(ultra);
            }
            if (medium != "null") {
                medium = AES_decrypt(medium);
            }
            if (normal != "null") {
                normal = AES_decrypt(normal);
            }
            ci.setAdaptive(getURLStr(adaptive));
            ci.setHigh(getURLStr(high));
            ci.setLow(getURLStr(low));
            ci.setUltra(getURLStr(ultra));
            ci.setMedium(getURLStr(medium));
            ci.setNormal(getURLStr(normal));
            ci.setIqiyi_4_0(iqiyi_4_0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ci;
    }

    private static String AES_decrypt(String url) {
        //		return AESOperator.getInstance().AES_decrypt(SimpleRestClient.device_token, url);
        return null;
    }

    private static String getURLStr(String url) {
        if (url != null && url != "null") {
            if (url.startsWith("ppvod")) {
                url = pptvplay(url);
                return url;
            }
            url.startsWith("uusee");
        } else {
            return null;
        }
        return url;
    }

    private static String pptvplay(String url) {
        String uri = null;
        if (url == null || url == "null") return uri;
        //		uri = PlayCodeUtil.getVideoUrlM3u8(url);
        return uri;
    }
}
