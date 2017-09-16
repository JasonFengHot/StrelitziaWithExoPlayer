package tv.ismar.app;

import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;

/** Created by huibin on 17-3-8. */
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
        mApiItemSubsc =
                SkyService.ServiceManager.getService()
                        .apiItem(itemPk)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                new Observer<ItemEntity>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable throwable) {}

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
        mApiMediaUrlSubsc =
                SkyService.ServiceManager.getService()
                        .fetchMediaUrl(clipUrl, sign, code)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                new Observer<ClipEntity>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable throwable) {}

                                    @Override
                                    public void onNext(ClipEntity clipEntity) {
                                        String normal = clipEntity.getNormal();
                                        String medium = clipEntity.getMedium();
                                        String high = clipEntity.getHigh();
                                        String ultra = clipEntity.getUltra();
                                        String blueray = clipEntity.getBlueray();
                                        String _4k = clipEntity.get_4k();
                                        try {
                                            File file = File.createTempFile("video", ".m3u8");
                                            BufferedSink bufferedSink =
                                                    Okio.buffer(Okio.sink(file));

                                            bufferedSink.writeUtf8("#EXTM3U\n");

                                            if (!Utils.isEmptyText(normal)) {
                                                clipEntity.setNormal(
                                                        (AccessProxy.AESDecrypt(
                                                                normal,
                                                                IsmartvActivator.getInstance()
                                                                        .getDeviceToken())));
                                                bufferedSink.writeUtf8(
                                                        "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1000000\n");
                                                bufferedSink.writeUtf8(
                                                        clipEntity.getNormal() + "\n");
                                            }
                                            if (!Utils.isEmptyText(medium)) {
                                                clipEntity.setMedium(
                                                        (AccessProxy.AESDecrypt(
                                                                medium,
                                                                IsmartvActivator.getInstance()
                                                                        .getDeviceToken())));
                                                bufferedSink.writeUtf8(
                                                        "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2000000\n");
                                                bufferedSink.writeUtf8(
                                                        clipEntity.getMedium() + "\n");
                                            }
                                            if (!Utils.isEmptyText(high)) {
                                                clipEntity.setHigh(
                                                        (AccessProxy.AESDecrypt(
                                                                high,
                                                                IsmartvActivator.getInstance()
                                                                        .getDeviceToken())));
                                                bufferedSink.writeUtf8(
                                                        "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=3000000\n");
                                                bufferedSink.writeUtf8(clipEntity.getHigh() + "\n");
                                            }
                                            if (!Utils.isEmptyText(ultra)) {
                                                clipEntity.setUltra(
                                                        (AccessProxy.AESDecrypt(
                                                                ultra,
                                                                IsmartvActivator.getInstance()
                                                                        .getDeviceToken())));
                                                bufferedSink.writeUtf8(
                                                        "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=4000000\n");
                                                bufferedSink.writeUtf8(
                                                        clipEntity.getUltra() + "\n");
                                            }
                                            if (!Utils.isEmptyText(blueray)) {
                                                clipEntity.setBlueray(
                                                        (AccessProxy.AESDecrypt(
                                                                blueray,
                                                                IsmartvActivator.getInstance()
                                                                        .getDeviceToken())));
                                                bufferedSink.writeUtf8(
                                                        "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=5000000\n");
                                                bufferedSink.writeUtf8(
                                                        clipEntity.getBlueray() + "\n");
                                            }
                                            if (!Utils.isEmptyText(_4k)) {
                                                clipEntity.set_4k(
                                                        (AccessProxy.AESDecrypt(
                                                                _4k,
                                                                IsmartvActivator.getInstance()
                                                                        .getDeviceToken())));
                                                bufferedSink.writeUtf8(
                                                        "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=6000000\n");
                                                bufferedSink.writeUtf8(clipEntity.get_4k() + "\n");
                                            }
                                            bufferedSink.close();
                                            clipEntity.setM3u8(file.getAbsolutePath());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        Log.d(TAG, "url: " + clipEntity.getHighest());
                                        Logger.d(clipEntity);
                                        callback.success(clipEntity);
                                    }
                                });
    }

    public interface Callback {
        void success(ClipEntity clipEntity);
    }
}
