package tv.ismar.detailpage.presenter;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;

import cn.ismartv.injectdb.library.query.Select;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.database.BookmarkTable;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.detailpage.DetailPageContract;

/**
 * Created by huibin on 8/19/16.
 */
public class DetailPagePresenter implements DetailPageContract.Presenter {
    private static final String TAG = DetailPagePresenter.class.getSimpleName();
    private final DetailPageContract.View mDetailView;
    private SkyService mSkyService;
    private Subscription apiItemSubsc;
    private Subscription bookmarksSubsc;
    private Subscription itemRelateSubsc;
    private Subscription removeBookmarksSubsc;
    private Subscription playCheckSubsc;

    private ItemEntity mItemEntity = new ItemEntity();


    public DetailPagePresenter(DetailPageContract.View detailView) {
        mDetailView = detailView;

        mDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        mSkyService = SkyService.ServiceManager.getService();
    }

    @Override
    public void fetchItem(String pk, String deviceToken, String accessToken) {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }

        apiItemSubsc = mSkyService.apiItem(pk, deviceToken, accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity = itemEntity;
                        mDetailView.loadItem(itemEntity);
                    }
                });
    }


    @Override
    public void createBookmarks(String pk, String deviceToken, String accessToken) {
        if (bookmarksSubsc != null && !bookmarksSubsc.isUnsubscribed()) {
            bookmarksSubsc.unsubscribe();
        }
        bookmarksSubsc = mSkyService.apiBookmarksCreate(pk, deviceToken, accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });
    }

    @Override
    public void removeBookmarks(String pk, String deviceToken, String accessToken) {
        if (removeBookmarksSubsc != null && !removeBookmarksSubsc.isUnsubscribed()) {
            removeBookmarksSubsc.unsubscribe();
        }
        removeBookmarksSubsc = mSkyService.apiBookmarksRemove(pk, deviceToken, accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });
    }

    @Override
    public void requestPlayCheck(String itemPk, String deviceToken, String accessToken) {
        if (playCheckSubsc != null && !playCheckSubsc.isUnsubscribed()) {
            playCheckSubsc.unsubscribe();
        }

        playCheckSubsc = mSkyService.apiPlayCheck(itemPk, null, null, deviceToken, accessToken)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            int remainDay = calculateRemainDay(responseBody.string());
                            mDetailView.notifyPlayCheck(remainDay);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private int calculateRemainDay(String info) {
        int remainDay;
        switch (info) {
            case "0":
                remainDay = 0;
                break;
            default:
                PlayCheckEntity playCheckEntity = new Gson().fromJson(info, PlayCheckEntity.class);

                try {
                    remainDay = Utils.daysBetween(Utils.getTime(), playCheckEntity.getExpiry_date()) + 1;
                } catch (ParseException e) {
                    remainDay = 0;
                }
                break;
        }
        return remainDay;
    }

    @Override
    public void fetchItemRelate(String pk, String deviceToken, String accessToken) {
        if (itemRelateSubsc != null && !itemRelateSubsc.isUnsubscribed()) {
            itemRelateSubsc.unsubscribe();
        }
        itemRelateSubsc = mSkyService.apiTvRelate(pk, deviceToken, accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ItemEntity[] itemEntities) {
                        mDetailView.loadItemRelate(itemEntities);
                    }
                });
    }


    @Override
    public void stop() {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
        if (bookmarksSubsc != null && !bookmarksSubsc.isUnsubscribed()) {
            bookmarksSubsc.unsubscribe();
        }
        if (itemRelateSubsc != null && !itemRelateSubsc.isUnsubscribed()) {
            itemRelateSubsc.unsubscribe();
        }
        if (removeBookmarksSubsc != null && !removeBookmarksSubsc.isUnsubscribed()) {
            removeBookmarksSubsc.unsubscribe();
        }
        if (playCheckSubsc != null && !playCheckSubsc.isUnsubscribed()) {
            playCheckSubsc.unsubscribe();
        }
    }

    @Override
    public void handleBookmark() {
        BookmarkTable bookmarkTable = new Select().from(BookmarkTable.class).where("pk = ?", mItemEntity.getPk()).executeSingle();
        if (bookmarkTable != null) {
            bookmarkTable.delete();
        } else {
            bookmarkTable = new BookmarkTable();
            bookmarkTable.pk = mItemEntity.getPk();
            bookmarkTable.title = mItemEntity.getTitle();
            bookmarkTable.save();
        }
    }
}
