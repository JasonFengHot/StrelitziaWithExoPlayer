package tv.ismar.player.presenter;

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
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.player.PlayerPageContract;
import tv.ismar.player.view.PlayerFragment;

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
    private Subscription mApiGetAdSubsc;
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
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
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
                        playerView.loadPlayerItem(itemEntity);
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

    @Override
    public void fetchAdvertisement(final ItemEntity itemEntity, final String adPid) {
        if (mApiGetAdSubsc != null && !mApiGetAdSubsc.isUnsubscribed()) {
            mApiGetAdSubsc.unsubscribe();
        }
        SkyService skyService = SkyService.ServiceManager.getAdService();
        mApiGetAdSubsc = skyService.fetchAdvertisement(getAdParam(itemEntity, adPid))
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
                            if (adPid.equals(PlayerFragment.AD_MODE_ONPAUSE)) {
                                playerView.loadPauseAd(null);
                            } else {
                                playerView.loadAdvertisement(null);
                            }
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
                        if (Utils.isEmptyText(result)) {
                            if (adPid.equals(PlayerFragment.AD_MODE_ONPAUSE)) {
                                playerView.loadPauseAd(null);
                            } else {
                                playerView.loadAdvertisement(null);
                            }
                            return;
                        }
                        List<AdElementEntity> adElementEntityList = getAdInfo(result, adPid);
                        if (adPid.equals(PlayerFragment.AD_MODE_ONPAUSE)) {
                            playerView.loadPauseAd(adElementEntityList);
                        } else {
                            playerView.loadAdvertisement(adElementEntityList);
                        }
                    }
                });
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
