package tv.ismar.app.ad;
import cn.ismartv.truetime.TrueTime;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
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
    private DateFormat dateFormat;

    private OnVideoPlayAdListener mOnVideoPlayAdListener;
    private OnAppStartAdListener mOnAppStartAdListener;

    public Advertisement(Context context) {
        mContext = context;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
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
                        if (mOnVideoPlayAdListener != null) {
                            if (adPid.equals(Advertisement.AD_MODE_ONPAUSE)) {
                                mOnVideoPlayAdListener.loadPauseAd(null);
                            } else {
                                mOnVideoPlayAdListener.loadVideoStartAd(null);
                            }
                        }

                        if (mApiVideoStartSubsc != null && !mApiVideoStartSubsc.isUnsubscribed()) {
                            mApiVideoStartSubsc.unsubscribe();
                        }

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
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

                        List<AdElementEntity> adElementEntityList = parseAdResult(result, adPid);
                        if (adPid.equals(Advertisement.AD_MODE_ONPAUSE)) {
                            mOnVideoPlayAdListener.loadPauseAd(adElementEntityList);
                        } else {
                            mOnVideoPlayAdListener.loadVideoStartAd(adElementEntityList);
                        }

                        if (mApiVideoStartSubsc != null && !mApiVideoStartSubsc.isUnsubscribed()) {
                            mApiVideoStartSubsc.unsubscribe();
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
                        Log.e(TAG, "fetchAppStartAd:" + e.getMessage());

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
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
                            Log.d(TAG, "fetchAppStartAd result null.");
                            mOnAppStartAdListener.loadAppStartAd(null);
                            return;
                        }

                        mOnAppStartAdListener.loadAppStartAd(parseAdResult(result, adPid));

                        if (mApiAppStartSubsc != null && !mApiAppStartSubsc.isUnsubscribed()) {
                            mApiAppStartSubsc.unsubscribe();
                        }
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
        adParams.put("channel", BaseActivity.baseChannel);
        adParams.put("section", BaseActivity.baseSection);
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

    private List<AdElementEntity> parseAdResult(String result, String adPid) {
//        result = "{\"retcode\":\"200\",\"retmsg\":\"\",\"ads\":{\"kaishi\":[{\"title\":\"power1\",\"media_id\":6,\"description\":\"\",\"media_url\":\"http://bootad.vdata.tvxio.com/topvideo/4953fb1c263fe7cf329a089c5530f72f.mp4?sn=sh_3xrdbg48\",\"tag\":\"\",\"coordinate\":{},\"monitor\":[],\"report_url\":\"\",\"md5\":\"\",\"media_type\":\"video\",\"media_size\":30,\"serial\":0,\"start\":0,\"end\":15,\"duration\":15,\"start_date\":\"2016-07-05\",\"end_date\":\"2016-12-11\",\"start_time\":\"00:00:00\",\"end_time\":\"23:00:00\",\"retcode\":\"200\",\"retmsg\":\"\"}]}}";
//        result = "{\"retcode\":\"200\",\"retmsg\":\"\",\"ads\":{\"kaishi\":[{\"title\":\"\\u538b\\u529b\\u6d4b\\u8bd53\",\"media_id\":14,\"description\":\"\",\"media_url\":\"http://124.42.65.66:8082/media/clover_image/xiayouqiaomu2_1280_720.png\",\"tag\":\"\",\"coordinate\":{},\"monitor\":[],\"report_url\":\"\",\"md5\":\"8aa51d011dd170e833559f504c7ddc73\",\"media_type\":\"image\",\"media_size\":0,\"serial\":0,\"start\":0,\"end\":5,\"duration\":5,\"start_date\":\"2016-08-24\",\"end_date\":\"2016-12-31\",\"start_time\":\"12:00:00\",\"end_time\":\"23:00:00\",\"retcode\":\"200\",\"retmsg\":\"\"}]}}";
        List<AdElementEntity> adElementEntities = new ArrayList<>();
        try {
            Log.i(TAG, "adPid:" + adPid + " parseAdResult:" + result);
            JSONObject jsonObject = new JSONObject(result);
            String retcode = jsonObject.getString("retcode");
//            int retcode = jsonObject.getInt("retcode");
            if (!Utils.isEmptyText(retcode) && retcode.equals("200")) {
                JSONObject body = jsonObject.getJSONObject("ads");
                JSONArray arrays = body.getJSONArray(adPid);
                for (int i = 0; i < arrays.length(); i++) {
                    JSONObject element = arrays.getJSONObject(i);
                    String elementRetCode = element.getString("retcode");
//                    int elementRetCode = element.getInt("retcode");
                    if (!Utils.isEmptyText(elementRetCode) && elementRetCode.equals("200")) {
                        AdElementEntity ad = new Gson().fromJson(element.toString(), AdElementEntity.class);
                        // 已经过期的数据需要过滤掉
                        try {
                            String start_date = ad.getStart_date() + " " + ad.getStart_time();
                            String end_date = ad.getEnd_date() + " " + ad.getEnd_time();
                            long date_start = dateFormat.parse(start_date).getTime();
                            long date_end = dateFormat.parse(end_date).getTime();
                            Date todayDate = TrueTime.now();
                            long todayDateTime = todayDate.getTime();
                            if(todayDateTime > date_start && todayDateTime < date_end){
                                adElementEntities.add(ad);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Ad dateTime parse exception.");
                            adElementEntities.add(ad);
                        }
                    }
                }

                if (!adElementEntities.isEmpty()) {
                    Collections.sort(adElementEntities, new Comparator<AdElementEntity>() {
                        @Override
                        public int compare(AdElementEntity lhs, AdElementEntity rhs) {
                            return rhs.getSerial() > lhs.getSerial() ? 1 : -1;
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return adElementEntities;
    }

}
