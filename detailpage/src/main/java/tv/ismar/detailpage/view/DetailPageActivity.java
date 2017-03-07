package tv.ismar.detailpage.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.concurrent.TimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.R;
import tv.ismar.pay.PaymentActivity;

import static tv.ismar.app.core.PageIntentInterface.DETAIL_TYPE_ITEM;
import static tv.ismar.app.core.PageIntentInterface.DETAIL_TYPE_PKG;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_TYPE;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity{
    private static final String TAG = "DetailPageActivity";


    private Subscription apiItemSubsc;
    private String source;
    private ItemEntity mItemEntity;

    private DetailPageFragment detailPageFragment;
    private PackageDetailFragment mPackageDetailFragment;
    public LoadingDialog mLoadingDialog;
    private int itemPK;
    private Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
                showNetWorkErrorDialog(new TimeoutException());
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_detailpage);
        Intent intent = getIntent();

        itemPK = intent.getIntExtra(EXTRA_PK, -1);
        String itemJson = intent.getStringExtra(EXTRA_ITEM_JSON);
        source = intent.getStringExtra(EXTRA_SOURCE);
        int type = intent.getIntExtra(EXTRA_TYPE, 0);
        String url = intent.getStringExtra("url");

        if (TextUtils.isEmpty(itemJson) && itemPK == -1 && TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        showDialog();

        //解析来至launcher的参数
        if (!TextUtils.isEmpty(url)) {
            Log.e("launcher_url",url);
            String[] arrayTmp = url.split("/");
            itemPK = Integer.parseInt(arrayTmp[arrayTmp.length - 1]);
            switch (arrayTmp[arrayTmp.length - 2]) {
                case "item":
                    type = DETAIL_TYPE_ITEM;
                    break;
                case "package":
                    type = DETAIL_TYPE_PKG;
                    break;
            }
            Log.e("launcher_type",type+"");
        }

        if (!TextUtils.isEmpty(itemJson)) {
            mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);
            loadFragment(type);
        } else {
            fetchItem(String.valueOf(itemPK), type);
        }

    }

    public void goPlayer() {
        // TODO 进入播放器界面
//        isClickPlay = true;
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, mItemEntity.getPk());
//        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, mSubItemPk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source);

//        // 只有在预加载成功的情况下进入播放器无需重新getItem, playCheck等
//        if (mHasPreLoad && mItemEntity != null && mClipEntity != null) {
//            String itemJson = new Gson().toJson(mItemEntity);
//            String clipJson = new Gson().toJson(mClipEntity);
//            intent.putExtra(PlayerActivity.DETAIL_PAGE_ITEM, itemJson);
//            intent.putExtra(PlayerActivity.DETAIL_PAGE_CLIP, clipJson);
//            intent.putExtra(PlayerActivity.HISTORY_POSITION, historyPosition);
//            if (historyQuality != null) {
//                intent.putExtra(PlayerActivity.HISTORY_QUALITY, historyQuality.getValue());
//            }
//            intent.putExtra(PlayerActivity.DETAIL_PAGE_PATHS, mPaths);
//            if (mAdList != null && !mAdList.isEmpty()) {
//                String adLists = new Gson().toJson(mAdList);
//                Log.i("LH/", "adLists:" + adLists);
//                intent.putExtra(PlayerActivity.DETAIL_PAGE_AD_LISTS, adLists);
//            }
//        }
        startActivity(intent);

    }

    public void fetchItem(final String pk, final int type) {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
        String opt = "";
        switch (type) {
            case DETAIL_TYPE_ITEM:
                opt = "item";
                break;
            case DETAIL_TYPE_PKG:
                opt = "package";
                break;
        }

        apiItemSubsc = mSkyService.apiOptItem(pk, opt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(mLoadingDialog!=null)
                        mLoadingDialog.dismiss();
                        if (e instanceof HttpException&&((HttpException)e).code() == 404) {
                            showItemOffLinePop();
                        }else{
                            super.onError(e);
                        }
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity = itemEntity;
                        loadFragment(type);
                    }
                });
    }

    private void loadFragment(int type) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (type) {
            case PageIntent.DETAIL_TYPE_ITEM:
                String itemJson = new Gson().toJson(mItemEntity);
                detailPageFragment = DetailPageFragment.newInstance(source, itemJson);
                fragmentTransaction.replace(R.id.activity_detail_container, detailPageFragment);
                fragmentTransaction.commit();
                break;
            case PageIntent.DETAIL_TYPE_PKG:
                String packJson = new Gson().toJson(mItemEntity);
                mPackageDetailFragment = PackageDetailFragment.newInstance(source, packJson);
                fragmentTransaction.replace(R.id.activity_detail_container, mPackageDetailFragment);
                fragmentTransaction.commit();
                break;
        }

    }

    public void showDialog() {
        handler.sendEmptyMessageDelayed(0,15000);
        start_time=System.currentTimeMillis();
        mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        mLoadingDialog.showDialog();
    }

    @Override
    protected void onResume() {
//        isClickPlay = false;
//        isActivityStoped = false;
//        mHasPreLoad = false;
        super.onResume();
        AppConstant.purchase_referer = "video";
        AppConstant.purchase_page = "detail";

    }

    @Override
    protected void onStop() {
        if (apiItemSubsc != null && apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
//        if (apiClipSubsc != null && !apiClipSubsc.isUnsubscribed()) {
//            apiClipSubsc.unsubscribe();
//        }
//        if (!isClickPlay && mSmartPlayer != null) {
//            mSmartPlayer.release();
//            mSmartPlayer = null;
//        }
//        isActivityStoped = true;
        super.onStop();
    }

//    /**
//     * 以下为详情页预加载功能实现
//     */
//
//    private Subscription apiClipSubsc;
//    private ClipEntity mClipEntity;
//    private HistoryManager historyManager;
//    private History mHistory;
//    private Advertisement mAdvertisement;
//    private boolean isClickPlay;// 点击播放按钮，不释放SmartPlayer,其余情况必须先释放
//    private boolean isActivityStoped;
//    private int historyPosition;
//    private String[] mPaths;// SmartPlayer提供get方法后可去掉
//    private List<AdElementEntity> mAdList;
//    private ClipEntity.Quality historyQuality;
//    private int mSubItemPk;
//    private boolean mHasPreLoad;
//
//    private boolean canPreLoading() {
//        return !isClickPlay && !isActivityStoped;
//    }
//
//    // 试看不需要预加载功能
//    public void playCheckResult(boolean permission) {
//        Log.i(TAG, "playCheckResult:" + permission);
//        if (permission || mItemEntity.getExpense() == null) {
//            fetchClip();
//        }
//
//    }

//    @Override
//    public void loadPauseAd(List<AdElementEntity> adList) {
//        // do nothing
//    }
//
//    @Override
//    public void loadVideoStartAd(List<AdElementEntity> adList) {
//        if (!canPreLoading()) {
//            return;
//        }
//        mAdList = adList;
//        initSmartPlayer(adList);
//    }
//
//    private void fetchClip() {
//        if (!canPreLoading()) {
//            return;
//        }
//        if (apiClipSubsc != null && !apiClipSubsc.isUnsubscribed()) {
//            apiClipSubsc.unsubscribe();
//        }
//        mAdvertisement = new Advertisement(this);
//        mAdvertisement.setOnVideoPlayListener(this);
//        if (historyManager == null) {
//            historyManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
//        }
//        String historyUrl = Utils.getItemUrl(mItemEntity.getItemPk());
//        String isLogin = "no";
//        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
//            isLogin = "yes";
//        }
//        mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);
//
//        String sign = "";
//        String code = "1";
//        ItemEntity.Clip clip = mItemEntity.getClip();
//        // Get history clip
//        ItemEntity[] subItems = mItemEntity.getSubitems();
//        if (subItems != null && subItems.length > 0) {
//            // 传入的subItemPk值大于0表示指定播放某一集
//            // 点击播放按钮时，如果有历史记录，应该播放历史记录的subItemPk,默认播放第一集
//            mSubItemPk = subItems[0].getPk();
//            if (mHistory != null) {
//                int sub_item_pk = Utils.getItemPk(mHistory.sub_url);
//                Log.i(TAG, "CheckHistory_sub_item_pk:" + sub_item_pk);
//                if (sub_item_pk > 0) {
//                    mSubItemPk = sub_item_pk;
//                }
//            }
//            // 获取当前要播放的电视剧Clip
//            for (int i = 0; i < subItems.length; i++) {
//                int _subItemPk = subItems[i].getPk();
//                if (mSubItemPk == _subItemPk) {
//                    clip = subItems[i].getClip();
//                    break;
//                }
//            }
//        }
//        if (clip != null && clip.getUrl() != null) {
//            if (mHistory != null) {
//                historyPosition = (int) mHistory.last_position;
//            }
//            DBQuality dbQuality = historyManager.getQuality();
//            if (dbQuality != null) {
//                historyQuality = ClipEntity.Quality.getQuality(dbQuality.quality);
//            }
//            apiClipSubsc = mSkyService.fetchMediaUrl(clip.getUrl(), sign, code)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<ClipEntity>() {
//                        @Override
//                        public void onCompleted() {
//
//                        }
//
//                        @Override
//                        public void onError(Throwable throwable) {
//                            Log.e(TAG, "fetchClip onError");
//                        }
//
//                        @Override
//                        public void onNext(ClipEntity clipEntity) {
//                            mClipEntity = clipEntity;
//                            String iqiyi = mClipEntity.getIqiyi_4_0();
//                            if (!Utils.isEmptyText(iqiyi)) {
//                                // 片源为爱奇艺
//                                return;
//                            }
//                            // 获取前贴片广告
//                            if (!canPreLoading()) {
//                                return;
//                            }
//                            mAdvertisement.fetchVideoStartAd(mItemEntity, Advertisement.AD_MODE_ONSTART, source);
//                        }
//                    });
//        }
//
//    }
//
//    private void initSmartPlayer(List<AdElementEntity> adList) {
//        Log.i(TAG, "initSmartPlayer:" + mSmartPlayer);
//        if (mClipEntity == null || mSmartPlayer != null) {
//            return;
//        }
////        String adaptive = mClipEntity.getAdaptive();
//        String normal = mClipEntity.getNormal();
//        String medium = mClipEntity.getMedium();
//        String high = mClipEntity.getHigh();
//        String ultra = mClipEntity.getUltra();
//        String blueray = mClipEntity.getBlueray();
//        String _4k = mClipEntity.get_4k();
////        if (!Utils.isEmptyText(adaptive)) {
////            mClipEntity.setAdaptive(AccessProxy.AESDecrypt(adaptive, IsmartvActivator.getInstance().getDeviceToken()));
////        }
//        if (!Utils.isEmptyText(normal)) {
//            mClipEntity.setNormal(AccessProxy.AESDecrypt(normal, IsmartvActivator.getInstance().getDeviceToken()));
//        }
//        if (!Utils.isEmptyText(medium)) {
//            mClipEntity.setMedium(AccessProxy.AESDecrypt(medium, IsmartvActivator.getInstance().getDeviceToken()));
//        }
//        if (!Utils.isEmptyText(high)) {
//            mClipEntity.setHigh(AccessProxy.AESDecrypt(high, IsmartvActivator.getInstance().getDeviceToken()));
//        }
//        if (!Utils.isEmptyText(ultra)) {
//            mClipEntity.setUltra(AccessProxy.AESDecrypt(ultra, IsmartvActivator.getInstance().getDeviceToken()));
//        }
//        if (!Utils.isEmptyText(blueray)) {
//            mClipEntity.setBlueray(AccessProxy.AESDecrypt(blueray, IsmartvActivator.getInstance().getDeviceToken()));
//        }
//        if (!Utils.isEmptyText(_4k)) {
//            mClipEntity.set_4k(AccessProxy.AESDecrypt(_4k, IsmartvActivator.getInstance().getDeviceToken()));
//        }
//        Log.d(TAG, mClipEntity.toString());
//        String mediaUrl = initSmartQuality(historyQuality);
//        if (!Utils.isEmptyText(mediaUrl)) {
//            String[] paths;
//            if (adList != null && !adList.isEmpty()) {
//                paths = new String[adList.size() + 1];
//                int i = 0;
//                for (AdElementEntity element : adList) {
//                    if ("video".equals(element.getMedia_type())) {
//                        paths[i] = element.getMedia_url();
//                        i++;
//                    }
//                }
//                paths[paths.length - 1] = mediaUrl;
//            } else {
//                paths = new String[]{mediaUrl};
//            }
//            Log.i("LH/", "paths:" + Arrays.toString(paths));
//            mSmartPlayer = new SmartPlayer();
//            mSmartPlayer.setDataSource(paths);
//            if (historyPosition > 0 && paths.length > 1) {// 大于1表示有广告
//                mSmartPlayer.seekTo(historyPosition);
//            }
//            mSmartPlayer.prepareAsync();
//            mPaths = paths;
//            mHasPreLoad = true;
//        }
//
//    }
//
//    private String initSmartQuality(ClipEntity.Quality initQuality) {
//        if (mClipEntity == null) {
//            return null;
//        }
//        String defaultQualityUrl = null;
//        List<ClipEntity.Quality> qualityList = new ArrayList<>();
//        String low = mClipEntity.getLow();
//        if (!Utils.isEmptyText(low)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_LOW);
//        }
//        String adaptive = mClipEntity.getAdaptive();
//        if (!Utils.isEmptyText(adaptive)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_ADAPTIVE);
//        }
//        String normal = mClipEntity.getNormal();
//        if (!Utils.isEmptyText(normal)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_NORMAL);
//        }
//        String medium = mClipEntity.getMedium();
//        if (!Utils.isEmptyText(medium)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_MEDIUM);
//        }
//        String high = mClipEntity.getHigh();
//        if (!Utils.isEmptyText(high)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_HIGH);
//        }
//        String ultra = mClipEntity.getUltra();
//        if (!Utils.isEmptyText(ultra)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_ULTRA);
//        }
//        String blueray = mClipEntity.getBlueray();
//        if (!Utils.isEmptyText(blueray)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_BLUERAY);
//        }
//        String _4k = mClipEntity.get_4k();
//        if (!Utils.isEmptyText(_4k)) {
//            qualityList.add(ClipEntity.Quality.QUALITY_4K);
//        }
//
//        if (!qualityList.isEmpty()) {
//            ClipEntity.Quality quality;
//            if (initQuality != null) {
//                quality = initQuality;
//            } else {
//                quality = qualityList.get(qualityList.size() - 1);
//            }
//            defaultQualityUrl = getSmartQualityUrl(quality);
//            if (Utils.isEmptyText(defaultQualityUrl)) {// 不同影片，分辨率差异，找不到取最后一个
//                defaultQualityUrl = getSmartQualityUrl(qualityList.get(qualityList.size() - 1));
//            }
//        }
//        return defaultQualityUrl;
//    }
//
//    protected String getSmartQualityUrl(ClipEntity.Quality quality) {
//        if (quality == null) {
//            return "";
//        }
//        String qualityUrl = null;
//        switch (quality) {
//            case QUALITY_LOW:
//                return mClipEntity.getLow();
//            case QUALITY_ADAPTIVE:
//                return mClipEntity.getAdaptive();
//            case QUALITY_NORMAL:
//                return mClipEntity.getNormal();
//            case QUALITY_MEDIUM:
//                return mClipEntity.getMedium();
//            case QUALITY_HIGH:
//                return mClipEntity.getHigh();
//            case QUALITY_ULTRA:
//                return mClipEntity.getUltra();
//            case QUALITY_BLUERAY:
//                return mClipEntity.getBlueray();
//            case QUALITY_4K:
//                return mClipEntity.get_4k();
//        }
//        return qualityUrl;
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Intent intent=new Intent();
        intent.putExtra("pk",itemPK);
        setResult(1,intent);
        handler.removeMessages(0);
        handler=null;
        super.onDestroy();
    }
}
