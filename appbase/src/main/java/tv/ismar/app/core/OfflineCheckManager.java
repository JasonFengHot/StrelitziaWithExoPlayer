package tv.ismar.app.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.app.core.client.HttpManager;

/**
 * Created by huibin on 6/8/16.
 */
public class OfflineCheckManager {
    private static OfflineCheckManager ourInstance = new OfflineCheckManager();

    private Callback mCallback;

    public static OfflineCheckManager getInstance() {
        return ourInstance;
    }

    private OfflineCheckManager() {
    }


    public void checkItem(String url, Callback callback) {
        mCallback = callback;
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient httpClient = HttpManager.getInstance().mClient;
        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                Log.i("LH/", "checkItem:onFailure ");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.offline();
                    }
                });
            }

            @Override
            public void onResponse(Response response) {
                if(response.body() == null){
                    mCallback.netError();
                    return;
                }
                String result = null;
                try {
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallback.netError();
                    return;
                }
                Log.i("LH/", "checkItem:onResponse " + result);
                OfflineCheckEntity offlineCheckEntity = new Gson().fromJson(result, OfflineCheckEntity.class);
                if (!offlineCheckEntity.isOffline()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.online();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.offline();
                        }
                    });
                }

            }
        });
    }

    interface Callback {
        void online();

        void offline();

        void netError();
    }

    private class OfflineCheckEntity {
        private String detail;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public boolean isOffline() {
            if ("Not found".equalsIgnoreCase(detail)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
