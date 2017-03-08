package tv.ismar.app;

import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;

/**
 * Created by huibin on 17-3-8.
 */

public class PlayerHelper {

    private static final String TAG = "PlayerHelper";
    private Subscription mApiItemSubsc;
    private Subscription mApiMediaUrlSubsc;
    private Callback callback;

    public PlayerHelper(int itemPk, Callback callback) {
        this.callback = callback;
        fetchPlayerItem(String.valueOf(itemPk));
    }


    public void fetchPlayerItem(String itemPk) {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        mApiItemSubsc = SkyService.ServiceManager.getService().apiItem(itemPk)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        fetchMediaUrl(itemEntity.getClip().getUrl(), "", "1");
                    }
                });

    }

    public void fetchMediaUrl(String clipUrl, String sign, String code) {
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        if (Utils.isEmptyText(clipUrl)) {
            Log.e(TAG, "clipUrl is null.");
            return;
        }
        mApiMediaUrlSubsc = SkyService.ServiceManager.getService().fetchMediaUrl(clipUrl, sign, code)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ClipEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
                        fetchM3u8(clipEntity);
                    }
                });

    }


    private void fetchM3u8(ClipEntity entity) {

        Observable.just(entity)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<ClipEntity, ClipEntity>() {
                    @Override
                    public ClipEntity call(ClipEntity clipEntity) {
                        String normal = clipEntity.getNormal();
                        String medium = clipEntity.getMedium();
                        String high = clipEntity.getHigh();
                        String ultra = clipEntity.getUltra();
                        String blueray = clipEntity.getBlueray();
                        String _4k = clipEntity.get_4k();
                        if (!Utils.isEmptyText(normal)) {
                            clipEntity.setNormal(download(AccessProxy.AESDecrypt(normal, IsmartvActivator.getInstance().getDeviceToken())));
                        }
                        if (!Utils.isEmptyText(medium)) {
                            clipEntity.setMedium(download(AccessProxy.AESDecrypt(medium, IsmartvActivator.getInstance().getDeviceToken())));
                        }
                        if (!Utils.isEmptyText(high)) {
                            clipEntity.setHigh(download(AccessProxy.AESDecrypt(high, IsmartvActivator.getInstance().getDeviceToken())));
                        }
                        if (!Utils.isEmptyText(ultra)) {
                            clipEntity.setUltra(download(AccessProxy.AESDecrypt(ultra, IsmartvActivator.getInstance().getDeviceToken())));
                        }
                        if (!Utils.isEmptyText(blueray)) {
                            clipEntity.setBlueray(download(AccessProxy.AESDecrypt(blueray, IsmartvActivator.getInstance().getDeviceToken())));
                        }
                        if (!Utils.isEmptyText(_4k)) {
                            clipEntity.set_4k(download(AccessProxy.AESDecrypt(_4k, IsmartvActivator.getInstance().getDeviceToken())));
                        }
                        return clipEntity;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ClipEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
                        Logger.d(clipEntity);
                        callback.success(clipEntity);
                    }
                });
    }


    public String download(String uri) {
        try {
            Response<ResponseBody> response = SkyService.ServiceManager.getService().m3u8(uri).execute();
            File file = File.createTempFile("video_", ".m3u8");
            BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
            bufferedSink.writeAll(response.body().source());
            bufferedSink.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());

        }
        return "";
    }

    public interface Callback {
        void success(ClipEntity clipEntity);
    }

}
