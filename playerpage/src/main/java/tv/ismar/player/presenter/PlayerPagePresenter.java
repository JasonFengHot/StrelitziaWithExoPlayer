package tv.ismar.player.presenter;

import android.util.Log;

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
    private ItemEntity mItemEntity;

    public PlayerPagePresenter(PlayerPageContract.View view) {
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
    }

    @Override
    public void fetchItem(String itemId, String deviceToken, String accessToken) {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        mApiItemSubsc = mSkyService.apiItem(itemId)
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
                        mItemEntity = itemEntity;
                        playerView.loadItem(itemEntity);
                    }
                });

    }

    @Override
    public void fetchMediaUrl(String clipUrl, String deviceToken, String accessToken, String sign, String code) {
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        mApiMediaUrlSubsc = mSkyService.fetchMediaUrl(clipUrl, deviceToken, accessToken, sign, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ClipEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "fetchMediaUrl:" + e.getMessage());
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
    public void fetchSubItem(String subItemId, String deviceToken, String accessToken) {

    }


}
