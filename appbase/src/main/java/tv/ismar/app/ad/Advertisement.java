package tv.ismar.app.ad;

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
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.Source;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.SPUtils;
import tv.ismar.app.util.Utils;

/**
 * Created by longhai on 16-10-24.
 */

public class Advertisement {

    private static final String TAG = "LH/Advertisement";

    public static final String AD_MODE_ONSTART = "qiantiepian"; // 视频播放前(视频广告)
    public static final String AD_MODE_ONPAUSE = "zanting";     // 视频暂停时(图片广告)
    public static final String AD_MODE_APPSTART = "kaishi";             // 进入首页前(图片,视频广告)

    private Subscription mApiVideoStartSubsc;
    private Subscription mApiAppStartSubsc;
    private Context mContext;

    private OnVideoPlayAdListener mOnVideoPlayAdListener;
    private OnAppStartAdListener mOnAppStartAdListener;

    public Advertisement(Context context) {
        mContext = context;
    }

    public void setOnVideoPlayListener(OnVideoPlayAdListener onVideoPlayAdListener) {
        mOnVideoPlayAdListener = onVideoPlayAdListener;
    }

    public void setOnAppStartListener(OnAppStartAdListener onAppStartAdListener) {
        mOnAppStartAdListener = onAppStartAdListener;
    }

    public void stopSubscription() {
        if (mApiVideoStartSubsc != null && !mApiVideoStartSubsc.isUnsubscribed()) {
            mApiVideoStartSubsc.unsubscribe();
        }
        if (mApiAppStartSubsc != null && !mApiAppStartSubsc.isUnsubscribed()) {
            mApiAppStartSubsc.unsubscribe();
        }
    }

    public interface OnVideoPlayAdListener {

        public void loadPauseAd(List<AdElementEntity> adList);

        public void loadVideoStartAd(List<AdElementEntity> adList);
    }

    public interface OnAppStartAdListener {

        public void loadAppStartAd(List<AdElementEntity> adList);

    }

    public void fetchVideoStartAd(final ItemEntity itemEntity, final String adPid, String source) {
        if (mApiVideoStartSubsc != null && !mApiVideoStartSubsc.isUnsubscribed()) {
            mApiVideoStartSubsc.unsubscribe();
        }
        SkyService skyService = SkyService.ServiceManager.getAdService();
        mApiVideoStartSubsc = skyService.fetchAdvertisement(getPlayerAdParam(itemEntity, adPid, source))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mApiVideoStartSubsc != null && !mApiVideoStartSubsc.isUnsubscribed()) {
                            mApiVideoStartSubsc.unsubscribe();
                        }

                        if (mOnVideoPlayAdListener != null) {
                            if (adPid.equals(Advertisement.AD_MODE_ONPAUSE)) {
                                mOnVideoPlayAdListener.loadPauseAd(null);
                            } else {
                                mOnVideoPlayAdListener.loadVideoStartAd(null);
                            }
                        }

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (mApiVideoStartSubsc != null && !mApiVideoStartSubsc.isUnsubscribed()) {
                            mApiVideoStartSubsc.unsubscribe();
                        }

                        String result = null;
                        try {
                            result = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (Utils.isEmptyText(result) || mOnVideoPlayAdListener == null) {
                            if (adPid.equals(Advertisement.AD_MODE_ONPAUSE)) {
                                mOnVideoPlayAdListener.loadPauseAd(null);
                            } else {
                                mOnVideoPlayAdListener.loadVideoStartAd(null);
                            }
                            return;
                        }

                        List<AdElementEntity> adElementEntityList = getAdInfo(result, adPid);
                        if (adPid.equals(Advertisement.AD_MODE_ONPAUSE)) {
                            mOnVideoPlayAdListener.loadPauseAd(adElementEntityList);
                        } else {
                            mOnVideoPlayAdListener.loadVideoStartAd(adElementEntityList);
                        }
                    }
                });

    }

    public void fetchAppStartAd(final String adPid) {
        if (mApiAppStartSubsc != null && !mApiAppStartSubsc.isUnsubscribed()) {
            mApiAppStartSubsc.unsubscribe();
        }
        SkyService skyService = SkyService.ServiceManager.getAdService();
        mApiAppStartSubsc = skyService.fetchAdvertisement(getAppStartParam(adPid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mApiAppStartSubsc != null && !mApiAppStartSubsc.isUnsubscribed()) {
                            mApiAppStartSubsc.unsubscribe();
                        }

                        if (mOnAppStartAdListener != null) {
                            mOnAppStartAdListener.loadAppStartAd(null);
                        }

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (mApiAppStartSubsc != null && !mApiAppStartSubsc.isUnsubscribed()) {
                            mApiAppStartSubsc.unsubscribe();
                        }

                        String result = null;
                        try {
                            result = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (mOnAppStartAdListener == null) {
                            Log.d(TAG, "mOnAppStartAdListener null.");
                            return;
                        }
                        if (Utils.isEmptyText(result)) {
                            mOnAppStartAdListener.loadAppStartAd(null);
                            return;
                        }

                        List<AdElementEntity> adElementEntityList = getAdInfo(result, adPid);
                        mOnAppStartAdListener.loadAppStartAd(adElementEntityList);
                    }
                });

    }

    private HashMap<String, Object> getPlayerAdParam(ItemEntity itemEntity, String adpid, String source) {
        Log.i(TAG, "pro:" + SPUtils.getValue(InitializeProcess.PROVINCE, ""));
        Log.i(TAG, "proPy:" + SPUtils.getValue(InitializeProcess.PROVINCE_PY, ""));
        Log.i(TAG, "city:" + SPUtils.getValue(InitializeProcess.CITY, ""));
        HashMap<String, Object> adParams = new HashMap<>();
        adParams.put("adpid", "['" + adpid + "']");
        adParams.put("sn", IsmartvActivator.getInstance().getSnToken());
        adParams.put("modelName", DeviceUtils.getModelName());
        adParams.put("version", String.valueOf(DeviceUtils.getVersionCode(mContext)));
        adParams.put("province", SPUtils.getValue(InitializeProcess.PROVINCE_PY, ""));
        adParams.put("city", "");
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
        Log.i(TAG, "GetAdParam-Source:" + source);
        if (Utils.isEmptyText(source)) {
            source = Source.UNKNOWN.getValue();
        }
        adParams.put("source", source);//fromPage
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

    private HashMap<String, Object> getAppStartParam(String adpid) {
        HashMap<String, Object> adParams = new HashMap<>();
        adParams.put("adpid", "['" + adpid + "']");
        adParams.put("sn", IsmartvActivator.getInstance().getSnToken());
        adParams.put("modelName", DeviceUtils.getModelName());
        adParams.put("version", String.valueOf(DeviceUtils.getVersionCode(mContext)));
        adParams.put("province", SPUtils.getValue(InitializeProcess.PROVINCE_PY, ""));
        adParams.put("city", "");
        adParams.put("app", "sky");
        adParams.put("resolution", DeviceUtils.getDisplayPixelWidth(mContext) + "," + DeviceUtils.getDisplayPixelHeight(mContext));
        adParams.put("dpi", String.valueOf(DeviceUtils.getDensity(mContext)));
        adParams.put("channel", " ");
        adParams.put("section", " ");
        adParams.put("itemid", " ");
        adParams.put("topic", " ");
        adParams.put("source", "power");
        adParams.put("content_model", " ");
        adParams.put("director", " ");
        adParams.put("actor", " ");
        adParams.put("genre", " ");
        adParams.put("clipid", " ");
        adParams.put("length", " ");
        adParams.put("live_video", " ");
        adParams.put("vendor", " ");
        adParams.put("expense", " ");
        return adParams;
    }

    private List<AdElementEntity> getAdInfo(String result, String adPid) {
        List<AdElementEntity> adElementEntities = new ArrayList<>();
        try {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return adElementEntities;
    }

}
