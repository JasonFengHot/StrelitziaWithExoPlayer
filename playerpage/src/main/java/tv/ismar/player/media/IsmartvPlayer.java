package tv.ismar.player.media;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.qiyi.sdk.player.IMedia;
import com.qiyi.sdk.player.Parameter;
import com.qiyi.sdk.player.PlayerSdk;
import com.qiyi.sdk.player.SdkVideo;

import java.util.HashMap;
import java.util.Stack;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.VodApplication;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.player.AccessProxy;

/**
 * Created by longhai on 16-9-12.
 */
public abstract class IsmartvPlayer implements IPlayer {

    protected static final String TAG = "LH/IsmartvPlayer";

    protected static byte mPlayerMode;

    public static final String AD_MODE_ONSTART = "qiantiepian";
    public static final String AD_MODE_ONPAUSE = "zanting";

    private ClipEntity mClipEntity;
    private Subscription mApiGetAdSubsc;
    protected Activity mContext;
    private ItemEntity mItemEntity;

    public IsmartvPlayer(byte mode) {
        mPlayerMode = mode;
    }

    public void setContext(Activity context) {
        mContext = context;
    }

    public void setItemEntity(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
    }

    @Override
    public void setDataSource(ClipEntity clipEntity) {
        if (clipEntity == null || mPlayerMode == 0) {
            throw new IllegalArgumentException("IsmartvPlayer setDataSource invalidate.");
        }
        switch (mPlayerMode) {
            case PlayerBuilder.MODE_SMART_PLAYER:
                // 片源为视云
                String adaptive = clipEntity.getAdaptive();
                String normal = clipEntity.getNormal();
                String medium = clipEntity.getMedium();
                String high = clipEntity.getHigh();
                String ultra = clipEntity.getUltra();
                String blueray = clipEntity.getBlueray();
                String _4k = clipEntity.get_4k();
                if (!Utils.isEmptyText(adaptive)) {
                    mClipEntity.setAdaptive(AccessProxy.AESDecrypt(adaptive, VodApplication.DEVICE_TOKEN));
                }
                if (!Utils.isEmptyText(normal)) {
                    mClipEntity.setNormal(AccessProxy.AESDecrypt(normal, VodApplication.DEVICE_TOKEN));
                }
                if (!Utils.isEmptyText(medium)) {
                    mClipEntity.setMedium(AccessProxy.AESDecrypt(medium, VodApplication.DEVICE_TOKEN));
                }
                if (!Utils.isEmptyText(high)) {
                    mClipEntity.setHigh(AccessProxy.AESDecrypt(high, VodApplication.DEVICE_TOKEN));
                }
                if (!Utils.isEmptyText(ultra)) {
                    mClipEntity.setUltra(AccessProxy.AESDecrypt(ultra, VodApplication.DEVICE_TOKEN));
                }
                if (!Utils.isEmptyText(blueray)) {
                    mClipEntity.setBlueray(AccessProxy.AESDecrypt(blueray, VodApplication.DEVICE_TOKEN));
                }
                if (!Utils.isEmptyText(_4k)) {
                    mClipEntity.set_4k(AccessProxy.AESDecrypt(_4k, VodApplication.DEVICE_TOKEN));
                }
                // 视云片源先加载广告
                fetchAdvertisement(mItemEntity, IsmartvPlayer.AD_MODE_ONSTART);
                break;
            case PlayerBuilder.MODE_QIYI_PLAYER:
                // 片源为爱奇艺
                mClipEntity.setIqiyi_4_0(clipEntity.getIqiyi_4_0());
                mClipEntity.setIs_vip(clipEntity.is_vip());

                // 初始化奇艺播放器,放在此处原因在于accessToken会发生变化,初始化成功后加载视频
                Parameter extraParams = new Parameter();
                //debug code
                final long time = System.currentTimeMillis();
                extraParams.setInitPlayerSdkAfter(0);  //SDK初始化在调用initialize之后delay一定时间开始执行, 单位为毫秒.
                extraParams.setCustomerAppVersion(String.valueOf(DeviceUtils.getVersionCode(mContext)));      //传入客户App版本号
                extraParams.setDeviceId(VodApplication.SN_TOKEN);   //传入deviceId, VIP项目必传, 登录和鉴权使用
                extraParams.setDeviceInfo(DeviceUtils.getModelName());
                PlayerSdk.getInstance().initialize(mContext, extraParams,
                        new PlayerSdk.OnInitializedListener() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "QiYiSdk init success:" + (System.currentTimeMillis() - time));
                                String[] array = mClipEntity.getIqiyi_4_0().split(":");
                                SdkVideo qiyiInfo = new SdkVideo(array[0], array[1], mClipEntity.is_vip());
                                setMedia(qiyiInfo);
                            }

                            @Override
                            public void onFailed(int what, int extra) {
                                mContext.finish();
                                Toast.makeText(mContext, "QiyiSdk init fail: what=" + what + ", extra=" + extra, Toast.LENGTH_LONG).show();
                            }
                        });
                break;
        }
    }

    @Override
    public void prepareAsync() {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seekTo(int position) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }

    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getAdCountDownTime() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isInPlaybackState() {
        return false;
    }

    protected void setMedia(String[] urls) {
    }

    protected void setMedia(IMedia media) {
    }

    private void fetchAdvertisement(ItemEntity itemEntity, final String adPid) {
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }
        mApiGetAdSubsc = SkyService.ServiceManager.getService().fetchAdvertisement(getAdInfo(itemEntity, adPid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AdElementEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "fetchAdvertisement:" + e.getMessage());
                        if (e.getClass() == OnlyWifiException.class) {

                        } else {

                        }
                    }

                    @Override
                    public void onNext(AdElementEntity[] adElementEntities) {
                        Stack<AdElementEntity> adElement = new Stack<>();
                        HashMap<String, Integer> adTimeMap = new HashMap<>();
//        adTimeMap.clear();
//        for (int i = 0; i < result.size(); i++) {
//            AdElement element = result.get(i);
//            if (element.getRoot_retcode() != 200)
//                break;
//            if (element.getRetcode() != 200) {
//                // report error log
//            } else {
//                if ("video".equals(element.getMedia_type())) {
//                    adTimeMap.put(element.getMedia_url(), element.getDuration());
//                    adsumtime += element.getDuration();
//                }
//                adElement.push(element);
//            }
//        }
//        if ("zanting".equals(adpid) && adElement.isEmpty())
//            return;
//        playAdElement();
                    }

//                    setMedia(url);
                });

    }

    private HashMap<String, String> getAdInfo(ItemEntity itemEntity, String adpid) {
        HashMap<String, String> adParams = new HashMap<>();

        StringBuffer directorsBuffer = new StringBuffer();
        StringBuffer actorsBuffer = new StringBuffer();
        StringBuffer genresBuffer = new StringBuffer();
        ItemEntity.Attributes attributes = itemEntity.getAttributes();
        if (attributes != null) {
            String[][] directors = attributes.getDirector();
            String[][] actors = attributes.getActor();
            String[][] genres = attributes.getGenre();
            if (directors != null) {
                for (int i = 0; i < directors.length; i++) {
                    if (i == 0)
                        directorsBuffer.append("[");
                    directorsBuffer.append(directors[i][1]);
                    if (i >= 0 && i != directors.length - 1)
                        directorsBuffer.append(",");
                    if (i == directors.length - 1)
                        directorsBuffer.append("]");
                }
            }
            if (actors != null) {
                for (int i = 0; i < actors.length; i++) {
                    if (i == 0)
                        actorsBuffer.append("[");
                    actorsBuffer.append(actors[i][1]);
                    if (i >= 0 && i != actors.length - 1)
                        actorsBuffer.append(",");
                    if (i == actors.length - 1)
                        actorsBuffer.append("]");
                }
            }
            if (genres != null) {
                for (int i = 0; i < genres.length; i++) {
                    if (i == 0)
                        genresBuffer.append("[");
                    genresBuffer.append(genres[i][1]);
                    if (i >= 0 && i != genres.length - 1)
                        genresBuffer.append(",");
                    if (i == genres.length - 1)
                        genresBuffer.append("]");
                }
            }

        }
        adParams.put("channel", "");
        adParams.put("section", "");
        adParams.put("itemid", String.valueOf(itemEntity.getItemPk()));
        adParams.put("topic", "");
        adParams.put("source", "");//fromPage
        adParams.put("content_model", itemEntity.getContentModel());
        adParams.put("director", directorsBuffer.toString());
        adParams.put("actor", actorsBuffer.toString());
        adParams.put("genre", genresBuffer.toString());
        adParams.put("clipid", String.valueOf(itemEntity.getClip().getPk()));
        adParams.put("length", itemEntity.getClip().getLength());
        adParams.put("live_video", String.valueOf(itemEntity.getLiveVideo()));
        String vendor = itemEntity.getVendor();
        if (Utils.isEmptyText(vendor)) {
            adParams.put("vendor", "");
        } else {
            adParams.put("vendor", Base64.encodeToString(vendor.getBytes(), Base64.URL_SAFE));
        }
        ItemEntity.Expense expense = itemEntity.getExpense();
        if (expense == null) {
            adParams.put("expense", "false");
        } else {
            adParams.put("expense", "true");
        }
        adParams.put("sn", "");
        adParams.put("modelName", DeviceUtils.getModelName());
        adParams.put("version", String.valueOf(DeviceUtils.getVersionCode(mContext)));
        adParams.put("province", "");
        adParams.put("city", "");
        adParams.put("app", "sky");
        adParams.put("resolution", DeviceUtils.getDisplayPixelWidth(mContext) + "," + DeviceUtils.getDisplayPixelHeight(mContext));
        adParams.put("dpi", String.valueOf(DeviceUtils.getDensity(mContext)));
        adParams.put("adpid", "['" + adpid + "']");
        return adParams;
    }

}
