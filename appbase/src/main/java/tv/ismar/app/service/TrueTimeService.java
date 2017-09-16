package tv.ismar.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.ismartv.truetime.TrueTimeRx;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/** Created by huibin on 12/9/16. */
public class TrueTimeService extends Service {
    private Subscription mSubscription;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }

        initTrueTime(this);

        return super.onStartCommand(intent, flags, startId);
    }

    private void initTrueTime(final Context context) {
        final List<String> ntpHosts = Arrays.asList("http://sky.tvxio.com/api/currenttime/");
        mSubscription =
                Observable.interval(0, 1, TimeUnit.HOURS)
                        .observeOn(Schedulers.io())
                        .map(
                                new Func1<Long, Object>() {
                                    @Override
                                    public Object call(Long aLong) {
                                        TrueTimeRx.clearCachedInfo(context);
                                        TrueTimeRx.build()
                                                .withConnectionTimeout(31_428)
                                                //
                                                // .withRetryCount(100)
                                                .withSharedPreferences(context)
                                                .withLoggingEnabled(true)
                                                .initialize(ntpHosts)
                                                .subscribe(
                                                        new Observer<Date>() {
                                                            @Override
                                                            public void onCompleted() {
                                                                Intent intent = new Intent();
                                                                intent.setAction(
                                                                        "cn.ismartv.truetime.sync");
                                                                sendBroadcast(intent);
                                                            }

                                                            @Override
                                                            public void onError(
                                                                    Throwable throwable) {
                                                                throwable.printStackTrace();
                                                            }

                                                            @Override
                                                            public void onNext(Date date) {}
                                                        });
                                        return null;
                                    }
                                })
                        .takeUntil(
                                new Func1<Object, Boolean>() {
                                    @Override
                                    public Boolean call(Object o) {
                                        return false;
                                    }
                                })
                        .subscribe(
                                new Observer<Object>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }

                                    @Override
                                    public void onNext(Object o) {}
                                });
    }
}
