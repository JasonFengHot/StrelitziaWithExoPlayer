package tv.ismar.app.core.client;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.preferences.AccountSharedPrefs;

/**
 * Created by huaijie on 5/28/15.
 */
public class IsmartvUrlClient extends Thread {
    private static final String TAG = "IsmartvClient:";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int DEFAULT_TIMEOUT = 2;

    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;

    private static final int FAILURE_4XX = 0x0004;
    private static final int FAILURE_5XX = 0x0005;


    private String url;
    private String params;
    private CallBack callback;
    private Method method;
    private MessageHandler messageHandler;

    private static Context mContext;

    private ErrorHandler errorHandler = ErrorHandler.SEND_BROADCAST;

    public static void initializeWithContext(Context context) {
        mContext = context;
    }

    public IsmartvUrlClient() {
        messageHandler = new MessageHandler(this);
    }

    @Override
    public void run() {

        switch (method) {
            case GET:
                doGet();
                break;
            case POST:
                doPost();
                break;
        }
    }


    public interface CallBack {
        void onSuccess(String result);

        void onFailed(Exception exception);
    }


    static class MessageHandler extends Handler {
        WeakReference<IsmartvUrlClient> weakReference;

        public MessageHandler(IsmartvUrlClient client) {
            weakReference = new WeakReference<IsmartvUrlClient>(client);
        }

        @Override
        public void handleMessage(Message msg) {
            IsmartvUrlClient client = weakReference.get();
            if (client != null) {
                switch (msg.what) {
                    case SUCCESS:
                        client.callback.onSuccess((String) msg.obj);
                        break;
                    case FAILURE:
                        client.callback.onFailed((Exception) msg.obj);
                        switch (client.errorHandler) {
                            case LOG_MESSAGE:
                                break;
                            case SEND_BROADCAST:
                                client.sendConnectErrorBroadcast(((Exception) msg.obj).getMessage());
                                break;
                        }
                        break;
                    case FAILURE_4XX:
                        client.callback.onFailed((Exception) msg.obj);
                        break;
                    case FAILURE_5XX:
                        client.callback.onFailed((Exception) msg.obj);
                        switch (client.errorHandler) {
                            case LOG_MESSAGE:
                                break;
                            case SEND_BROADCAST:
                                client.sendConnectErrorBroadcast(((Exception) msg.obj).getMessage());
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public void doRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {
        hashMap.put("access_token", SimpleRestClient.access_token);
        if (SimpleRestClient.device_token == null || "".equals(SimpleRestClient.device_token)) {
            VodApplication.setDevice_Token();
        }
        hashMap.put("device_token", SimpleRestClient.device_token);
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        this.params = stringBuffer.toString();
        this.url = api;
        this.callback = callback;
        this.method = method;
        start();
    }


    public void doNormalRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        this.params = stringBuffer.toString();
        this.url = api;
        this.callback = callback;
        this.method = method;
        start();
    }

    public void doRequest(String api, CallBack callback) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("device_token", SimpleRestClient.device_token);
        hashMap.put("access_token", SimpleRestClient.access_token);
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        this.params = stringBuffer.toString();
        this.url = api;
        this.callback = callback;
        this.method = Method.GET;
        start();
    }


    public void doAdvertisementRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {

        hashMap.put("channel", " ");
        hashMap.put("section", " ");
        hashMap.put("itemid", " ");
        hashMap.put("topic", " ");
        hashMap.put("source", "power");
        hashMap.put("genre", " ");
        hashMap.put("content_model", " ");
        hashMap.put("director", " ");
        hashMap.put("actor", " ");
        hashMap.put("clipid", " ");
        hashMap.put("live_video", " ");
        hashMap.put("vendor", " ");
        hashMap.put("expense", " ");
        hashMap.put("length", " ");
        hashMap.put("modelName", VodUserAgent.getModelName());
        hashMap.put("sn", SimpleRestClient.sn_token);
        hashMap.put("access_token", SimpleRestClient.access_token);
        hashMap.put("device_token", SimpleRestClient.device_token);
        hashMap.put("version", String.valueOf(SimpleRestClient.appVersion));
        hashMap.put("province", AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.PROVINCE_PY));
        hashMap.put("city", "");
        hashMap.put("app", "sky");
        hashMap.put("resolution", SimpleRestClient.screenWidth + "," + SimpleRestClient.screenHeight);
        hashMap.put("dpi", String.valueOf(SimpleRestClient.densityDpi));


        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        this.params = stringBuffer.toString();
        this.url = api;
        this.callback = callback;
        this.method = method;
        this.errorHandler = ErrorHandler.LOG_MESSAGE;
        start();
    }


    public enum Method {
        GET,
        POST
    }

    public enum ErrorHandler {
        LOG_MESSAGE,
        SEND_BROADCAST
    }

    private void doGet() {
        Message message = messageHandler.obtainMessage();
        try {
            String api = url + "?" + params;
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).build();
            Request request = new Request.Builder()
                    .url(api)
                    .build();

            Response response;
            response = client.newCall(request).execute();
            String result = "";
            if (response.body() != null) {
                result = response.body().string();
            }
            Log.i(TAG, "---> BEGIN\n" +
                            "\t<--- Request URL: " + "\t" + api + "\n" +
                            "\t<--- Request Method: " + "\t" + "GET" + "\n" +
                            "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                            "\t<--- Response Result: " + "\t" + result + "\n" +
                            "\t---> END"
            );
            new JsonParser().parse(result);
            if (response.code() >= 400 && response.code() < 500) {
                message.what = FAILURE_4XX;
                message.obj = new IOException("网络请求客户端错误!!!");
            } else if (response.code() >= 500) {
                message.what = FAILURE_5XX;
                message.obj = new IOException("网络请求服务端错误!!!");

            } else {
                message.what = SUCCESS;
                message.obj = result;
            }
        } catch (Exception e) {
            message.what = FAILURE;
            message.obj = new IOException("网络请求错误!!!");
        }
        messageHandler.sendMessage(message);
    }

    private void doPost() {
        Message message = messageHandler.obtainMessage();
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
            RequestBody body = RequestBody.create(JSON, params);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response;
            response = client.newCall(request).execute();
            String result = response.body().string();
            new JsonParser().parse(result);
            Log.i(TAG, "---> BEGIN\n" +
                            "\t<--- Request URL: " + "\t" + url + "\n" +
                            "\t<--- Request Method: " + "\t" + "POST" + "\n" +
                            "\t<--- Request Params: " + "\t" + params + "\n" +
                            "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                            "\t<--- Response Result: " + "\t" + result + "\n" +
                            "\t---> END"
            );

            if (response.code() >= 400 && response.code() < 500) {
                message.what = FAILURE_4XX;
                message.obj = new IOException("网络请求客户端错误!!!");
            } else if (response.code() >= 500) {
                message.what = FAILURE_5XX;
                message.obj = new IOException("网络请求服务端错误!!!");

            } else {
                message.what = SUCCESS;
                message.obj = result;
            }
        } catch (Exception e) {

            message.what = FAILURE;
            message.obj = new IOException("网络请求错误!!!");
        }
        messageHandler.sendMessage(message);
    }

    private void sendConnectErrorBroadcast(String msg) {
        if (mContext != null) {
            Intent intent = new Intent();
            intent.putExtra("data", msg);
            intent.setAction(BaseActivity.ACTION_CONNECT_ERROR);
            mContext.sendBroadcast(intent);
        }
    }
}
