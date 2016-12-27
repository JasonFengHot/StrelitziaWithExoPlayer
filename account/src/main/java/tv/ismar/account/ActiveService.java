package tv.ismar.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.account.data.ResultEntity;

/**
 * Created by huibin on 12/1/16.
 */

public class ActiveService extends Service {
    private static final String TAG = "ActiveService";
    private Subscription activeSubscription;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        intervalActive();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    private void intervalActive() {
        if (activeSubscription != null && !activeSubscription.isUnsubscribed()) {
            activeSubscription.unsubscribe();
        }
        activeSubscription = Observable.interval(0, 60 * 60, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .map(new Func1<Long, ResultEntity>() {
                    @Override
                    public ResultEntity call(Long aLong) {
                        return IsmartvActivator.getInstance().execute();

                    }
                })
                .takeUntil(new Func1<ResultEntity, Boolean>() {
                    @Override
                    public Boolean call(ResultEntity responseBody) {
                        return false;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "active interval: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResultEntity responseBody) {
                        Log.i(TAG, "active interval success !!!");
                        Log.i(TAG, new Gson().toJson(responseBody));
                    }
                });
    }
}
