package tv.ismar.app.core;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;

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

        SkyService skyService = SkyService.ServiceManager.getService();
        skyService.apiCheckItem(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i("LH/", "checkItem:onFailure ");
                        mCallback.offline();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String result;
                        try {
                            result = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                            mCallback.netError();
                            return;
                        }
                        Log.i("LH/", "checkItem:onResponse " + result);
                        OfflineCheckEntity offlineCheckEntity = new Gson().fromJson(result, OfflineCheckEntity.class);
                        if (!offlineCheckEntity.isOffline()) {
                            mCallback.online();
                        } else {
                            mCallback.offline();
                        }
                    }
                });
    }


    public void checkItem(int pk, Callback callback) {
        mCallback = callback;

        SkyService skyService = SkyService.ServiceManager.getService();
        skyService.apiItemIsOffline(String.valueOf(pk))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<OfflineCheckEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i("LH/", "checkItem:onFailure ");
                        mCallback.offline();
                    }

                    @Override
                    public void onNext(OfflineCheckEntity offlineCheckEntity) {
                        if (!offlineCheckEntity.isOffline()) {
                            mCallback.online();
                        } else {
                            mCallback.offline();
                        }
                    }
                });
    }

    public void checkPkg(int pk, Callback callback) {
        mCallback = callback;

        SkyService skyService = SkyService.ServiceManager.getService();
        skyService.apiPKGIsOffline(String.valueOf(pk))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<OfflineCheckEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i("LH/", "checkItem:onFailure ");
                        mCallback.offline();
                    }

                    @Override
                    public void onNext(OfflineCheckEntity offlineCheckEntity) {
                        if (!offlineCheckEntity.isOffline()) {
                            mCallback.online();
                        } else {
                            mCallback.offline();
                        }
                    }
                });
    }

    interface Callback {
        void online();

        void offline();

        void netError();
    }

    public class OfflineCheckEntity {
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
