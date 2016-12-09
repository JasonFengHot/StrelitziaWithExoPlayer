package tv.ismar.player.presenter;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.Utils;
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
    private BaseActivity mActivity;

    public PlayerPagePresenter(BaseActivity activity, PlayerPageContract.View view) {
        mActivity = activity;
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
    public void fetchPlayerItem(String itemPk) {
        if (mApiItemSubsc != null && !mApiItemSubsc.isUnsubscribed()) {
            mApiItemSubsc.unsubscribe();
        }
        mApiItemSubsc = mSkyService.apiItem(itemPk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        playerView.loadPlayerItem(itemEntity);
                    }
                });

    }

    @Override
    public void fetchMediaUrl(String clipUrl, String sign, String code) {
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        if(Utils.isEmptyText(clipUrl)){
            Log.e(TAG, "clipUrl is null.");
            return;
        }
        mApiMediaUrlSubsc = mSkyService.fetchMediaUrl(clipUrl, sign, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<ClipEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
                        playerView.loadPlayerClip(clipEntity);
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
                .subscribe(mActivity.new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String result = responseBody.string();
                            Log.i(TAG, "SendHistory:" + result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
