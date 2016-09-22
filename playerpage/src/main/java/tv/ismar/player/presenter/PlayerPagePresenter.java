package tv.ismar.player.presenter;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.player.PlayerPageContract;

/**
 * Created by longhai on 16-9-8.
 */
public class PlayerPagePresenter implements PlayerPageContract.Presenter {

    private final String TAG = "LH/PlayerPagePresenter";
    private PlayerPageContract.View playerView;
    private SkyService mSkyService;
    private Subscription mApiItemSubsc;
    private Subscription mApiMediaUrlSubsc;
    private Subscription mApiHistorySubsc;
    private Context mContext;

    public PlayerPagePresenter(Context context, PlayerPageContract.View view) {
        mContext = context;
        playerView = view;
        playerView.setPresenter(this);

    }

    @Override
    public void start() {
        mSkyService = SkyService.ServiceManager.getService();

    }

    @Override
    public void stop() {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        if (mApiHistorySubsc != null && !mApiHistorySubsc.isUnsubscribed()) {
            mApiHistorySubsc.unsubscribe();
        }

    }

    @Override
    public void fetchItem(String itemPk) {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        mApiItemSubsc = mSkyService.apiItem(itemPk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "apiItem:" + e.getMessage());
                        if (e.getClass() == OnlyWifiException.class) {
                            playerView.onHttpInterceptor(e);
                        } else {
                            playerView.onHttpFailure(e);
                        }
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        playerView.loadItem(itemEntity);
                    }
                });

    }

    @Override
    public void fetchMediaUrl(String clipUrl, String sign, String code) {
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        mApiMediaUrlSubsc = mSkyService.fetchMediaUrl(clipUrl, sign, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ClipEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (e.getClass() == OnlyWifiException.class) {
                            playerView.onHttpInterceptor(e);
                        } else {
                            playerView.onHttpFailure(e);
                        }
                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
                        playerView.loadClip(clipEntity);
                    }
                });

    }

    @Override
    public void sendHistory(HashMap<String, Object> history) {
        if (mApiHistorySubsc != null && !mApiHistorySubsc.isUnsubscribed()) {
            mApiHistorySubsc.unsubscribe();
        }

        mApiHistorySubsc = mSkyService.sendPlayHistory(history)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (e.getClass() == OnlyWifiException.class) {
                            playerView.onHttpInterceptor(e);
                        } else {
                            playerView.onHttpFailure(e);
                        }
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                    }
                });
    }
}
