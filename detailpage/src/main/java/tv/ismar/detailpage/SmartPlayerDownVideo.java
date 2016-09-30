package tv.ismar.detailpage;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.PlayCheckManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.player.AccessProxy;
import tv.ismar.player.SmartPlayer;

/**
 * Created by longhai on 16-9-28.
 */
public class SmartPlayerDownVideo {

    private static final String TAG = "LH/SmartPlayerDownVideo";

    private Context mContext;
    private SkyService mSkyService;
    private SmartPlayer mSmartPlayer;
    private ItemEntity mItemEntity;
    private ClipEntity mClipEntity;
    private String[] mPaths;

    private HistoryManager mHistoryManager;
    private History mHistory;
    private int historyPosition;
    private ClipEntity.Quality historyQuality;

    private Subscription mApiMediaUrlSubsc;
    private Subscription mApiGetAdSubsc;

    public SmartPlayerDownVideo(Context context, ItemEntity itemEntity) {
        mSkyService = SkyService.ServiceManager.getService();
        mContext = context;
        mItemEntity = itemEntity;
        reset();
    }

    private void reset() {
        historyPosition = 0;
        historyQuality = null;
    }

    public void startDownload(boolean isPreview) {
        if (mItemEntity == null) {
            Log.e(TAG, "Start download video fail.");
            return;
        }
        reset();
        if (mHistoryManager == null) {
            mHistoryManager = VodApplication.getModuleAppContext().getModuleHistoryManager();
        }
        String clipUrl = mItemEntity.getClip().getUrl();
        if (isPreview) {
            ItemEntity.Preview preview = mItemEntity.getPreview();
            getHistory(false, preview.getUrl());
        } else {
            getHistory(true, clipUrl);
        }

    }

    private void getHistory(boolean getHistory, String clipUrl) {
        if (!getHistory) {
            downClip(clipUrl);
            return;
        }
        ItemEntity.SubItem[] subItems = mItemEntity.getSubitems();
        if (subItems != null && subItems.length > 0) {
            // 获取当前要播放的电视剧Clip,有历史记录时需要从历史记录开始缓冲
            String historyUrl = Utils.getItemUrl(mItemEntity.getPk());
            String isLogin = "no";
            if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
                isLogin = "yes";
            }
            mHistory = mHistoryManager.getHistoryByUrl(historyUrl, isLogin);
            if (mHistory != null) {
                historyPosition = (int) mHistory.last_position;
                historyQuality = ClipEntity.Quality.getQuality(mHistory.last_quality);
                String subUrl = mHistory.sub_url;
                if (!Utils.isEmptyText(subUrl)) {
                    clipUrl = subUrl;
                }
            }
        }
        downClip(clipUrl);
    }

    public void stopDownload() {
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }
        if (mSmartPlayer != null) {
            mSmartPlayer.releaseDownload();
            mSmartPlayer = null;
        }

    }

    private void downClip(String clipUrl) {
        String sign = "";
        String code = "1";
        if (mApiMediaUrlSubsc != null && !mApiMediaUrlSubsc.isUnsubscribed()) {
            mApiMediaUrlSubsc.unsubscribe();
        }
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
                    }

                    @Override
                    public void onNext(ClipEntity clipEntity) {
                        String iqiyi = clipEntity.getIqiyi_4_0();
                        if (Utils.isEmptyText(iqiyi)) {
                            // 片源为视云
                            fetchAdvertisement(clipEntity);
                        }
                    }
                });

    }

    private void fetchAdvertisement(ClipEntity clipEntity) {
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }
        String adaptive = clipEntity.getAdaptive();
        String normal = clipEntity.getNormal();
        String medium = clipEntity.getMedium();
        String high = clipEntity.getHigh();
        String ultra = clipEntity.getUltra();
        String blueray = clipEntity.getBlueray();
        String _4k = clipEntity.get_4k();
        mClipEntity = new ClipEntity();
        if (!Utils.isEmptyText(adaptive)) {
            mClipEntity.setAdaptive(AccessProxy.AESDecrypt(adaptive, IsmartvActivator.getInstance().getDeviceToken()));
        }
        if (!Utils.isEmptyText(normal)) {
            mClipEntity.setNormal(AccessProxy.AESDecrypt(normal, IsmartvActivator.getInstance().getDeviceToken()));
        }
        if (!Utils.isEmptyText(medium)) {
            mClipEntity.setMedium(AccessProxy.AESDecrypt(medium, IsmartvActivator.getInstance().getDeviceToken()));
        }
        if (!Utils.isEmptyText(high)) {
            mClipEntity.setHigh(AccessProxy.AESDecrypt(high, IsmartvActivator.getInstance().getDeviceToken()));
        }
        if (!Utils.isEmptyText(ultra)) {
            mClipEntity.setUltra(AccessProxy.AESDecrypt(ultra, IsmartvActivator.getInstance().getDeviceToken()));
        }
        if (!Utils.isEmptyText(blueray)) {
            mClipEntity.setBlueray(AccessProxy.AESDecrypt(blueray, IsmartvActivator.getInstance().getDeviceToken()));
        }
        if (!Utils.isEmptyText(_4k)) {
            mClipEntity.set_4k(AccessProxy.AESDecrypt(_4k, IsmartvActivator.getInstance().getDeviceToken()));
        }
//                Log.d(TAG, mClipEntity.toString());
        SkyService skyService = SkyService.ServiceManager.getAdService();
        mApiGetAdSubsc = skyService.fetchAdvertisement(getAdParam(mItemEntity, "qiantiepian"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
                            mApiGetAdSubsc.unsubscribe();
                        }

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        List<String> adPaths = new ArrayList<String>();
                        try {
                            String result = responseBody.string();
                            List<AdElementEntity> adElementEntityList = getAdInfo(result, "qiantiepian");
                            if (adElementEntityList != null && !adElementEntityList.isEmpty()) {
                                for (AdElementEntity element : adElementEntityList) {
                                    if ("video".equals(element.getMedia_type())) {
                                        adPaths.add(element.getMedia_url());
                                    }
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        String mediaUrl = initSmartQuality(historyQuality);
                        if (!Utils.isEmptyText(mediaUrl)) {
                            if (!adPaths.isEmpty()) {
                                mPaths = new String[adPaths.size() + 1];
                                for (int i = 0; i < adPaths.size(); i++) {
                                    mPaths[i] = adPaths.get(i);
                                }
                                mPaths[mPaths.length - 1] = mediaUrl;
                                // 有广告情况下历史位置设置为0
                                historyPosition = 0;
                            } else {
                                mPaths = new String[]{mediaUrl};
                            }
                            mSmartPlayer = new SmartPlayer(true);
                            mSmartPlayer.startDownload(IsmartvActivator.getInstance().getSnToken(), mPaths, historyPosition);
                        } else {
                            Log.e(TAG, "Video address error.");
                        }

                        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
                            mApiGetAdSubsc.unsubscribe();
                        }
                    }
                });
    }

    private List<AdElementEntity> getAdInfo(String result, String adPid) throws JSONException {
        List<AdElementEntity> adElementEntities = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(result);
        int retcode = jsonObject.getInt("retcode");
        if (retcode == 200) {
            JSONObject body = jsonObject.getJSONObject("ads");
            JSONArray arrays = body.getJSONArray(adPid);
            for (int i = 0; i < arrays.length(); i++) {
                JSONObject element = arrays.getJSONObject(i);
                AdElementEntity ad = new AdElementEntity();
                int elementRetCode = element.getInt("retcode");
                if (elementRetCode == 200) {
                    ad.setRetcode(elementRetCode);
                    ad.setRetmsg(element.getString("retmsg"));
                    ad.setTitle(element.getString("title"));
                    ad.setDescription(element.getString("description"));
                    ad.setMedia_url(element.getString("media_url"));
                    ad.setMedia_id(element.getInt("media_id"));
                    ad.setMd5(element.getString("md5"));
                    ad.setMedia_type(element.getString("media_type"));
                    ad.setSerial(element.getInt("serial"));
                    ad.setStart(element.getInt("start"));
                    ad.setEnd(element.getInt("end"));
                    ad.setDuration(element.getInt("duration"));
                    ad.setReport_url(element.getString("report_url"));
                    adElementEntities.add(ad);
                }
            }
            Collections.sort(adElementEntities, new Comparator<AdElementEntity>() {
                @Override
                public int compare(AdElementEntity lhs, AdElementEntity rhs) {
                    return rhs.getSerial() > lhs.getSerial() ? 1 : -1;
                }
            });
        }
        return adElementEntities;
    }

    private HashMap<String, Object> getAdParam(ItemEntity itemEntity, String adpid) {
        HashMap<String, Object> adParams = new HashMap<>();
        adParams.put("adpid", "['" + adpid + "']");
        adParams.put("sn", IsmartvActivator.getInstance().getSnToken());
        adParams.put("modelName", DeviceUtils.getModelName());
        adParams.put("version", String.valueOf(DeviceUtils.getVersionCode(mContext)));
        adParams.put("province", "SH");
        adParams.put("city", "SH");
        adParams.put("app", "sky");
        adParams.put("resolution", DeviceUtils.getDisplayPixelWidth(mContext) + "," + DeviceUtils.getDisplayPixelHeight(mContext));
        adParams.put("dpi", String.valueOf(DeviceUtils.getDensity(mContext)));

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
                    directorsBuffer.append(directors[i][0]);
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
                    actorsBuffer.append(actors[i][0]);
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
                    genresBuffer.append(genres[i][0]);
                    if (i >= 0 && i != genres.length - 1)
                        genresBuffer.append(",");
                    if (i == genres.length - 1)
                        genresBuffer.append("]");
                }
            }

        }
        adParams.put("channel", "");
        adParams.put("section", "");
        adParams.put("itemid", itemEntity.getItemPk());
        adParams.put("topic", "");
        adParams.put("source", "list");//fromPage
        adParams.put("content_model", itemEntity.getContentModel());
        adParams.put("director", directorsBuffer.toString());
        adParams.put("actor", actorsBuffer.toString());
        adParams.put("genre", genresBuffer.toString());
        adParams.put("clipid", itemEntity.getClip().getPk());
        adParams.put("length", Integer.valueOf(itemEntity.getClip().getLength()));
        adParams.put("live_video", itemEntity.getLiveVideo());
        String vendor = itemEntity.getVendor();
        if (Utils.isEmptyText(vendor)) {
            adParams.put("vendor", "");
        } else {
            adParams.put("vendor", Base64.encodeToString(vendor.getBytes(), Base64.URL_SAFE));
        }
        ItemEntity.Expense expense = itemEntity.getExpense();
        if (expense == null) {
            adParams.put("expense", false);
        } else {
            adParams.put("expense", true);
        }
        return adParams;
    }

    private String initSmartQuality(ClipEntity.Quality initQuality) {
        if (mClipEntity == null) {
            return null;
        }
        List<ClipEntity.Quality> qualityList = new ArrayList<>();
        String defaultQualityUrl = null;
        String low = mClipEntity.getLow();
        if (!Utils.isEmptyText(low)) {
            qualityList.add(ClipEntity.Quality.QUALITY_LOW);
        }
        String adaptive = mClipEntity.getAdaptive();
        if (!Utils.isEmptyText(adaptive)) {
            qualityList.add(ClipEntity.Quality.QUALITY_ADAPTIVE);
        }
        String normal = mClipEntity.getNormal();
        if (!Utils.isEmptyText(normal)) {
            qualityList.add(ClipEntity.Quality.QUALITY_NORMAL);
        }
        String medium = mClipEntity.getMedium();
        if (!Utils.isEmptyText(medium)) {
            qualityList.add(ClipEntity.Quality.QUALITY_MEDIUM);
        }
        String high = mClipEntity.getHigh();
        if (!Utils.isEmptyText(high)) {
            qualityList.add(ClipEntity.Quality.QUALITY_HIGH);
        }
        String ultra = mClipEntity.getUltra();
        if (!Utils.isEmptyText(ultra)) {
            qualityList.add(ClipEntity.Quality.QUALITY_ULTRA);
        }
        String blueray = mClipEntity.getBlueray();
        if (!Utils.isEmptyText(blueray)) {
            qualityList.add(ClipEntity.Quality.QUALITY_BLUERAY);
        }
        String _4k = mClipEntity.get_4k();
        if (!Utils.isEmptyText(_4k)) {
            qualityList.add(ClipEntity.Quality.QUALITY_4K);
        }
        if (!qualityList.isEmpty()) {
            if (initQuality != null) {
                defaultQualityUrl = getSmartQualityUrl(initQuality);
            } else {
                defaultQualityUrl = getSmartQualityUrl(qualityList.get(0));
            }
        }
        return defaultQualityUrl;
    }

    private String getSmartQualityUrl(ClipEntity.Quality quality) {
        if (quality == null) {
            return "";
        }
        String qualityUrl = null;
        switch (quality) {
            case QUALITY_LOW:
                return mClipEntity.getLow();
            case QUALITY_ADAPTIVE:
                return mClipEntity.getAdaptive();
            case QUALITY_NORMAL:
                return mClipEntity.getNormal();
            case QUALITY_MEDIUM:
                return mClipEntity.getMedium();
            case QUALITY_HIGH:
                return mClipEntity.getHigh();
            case QUALITY_ULTRA:
                return mClipEntity.getUltra();
            case QUALITY_BLUERAY:
                return mClipEntity.getBlueray();
            case QUALITY_4K:
                return mClipEntity.get_4k();
        }
        return qualityUrl;
    }

}
