package tv.ismar.searchpage.core.handler;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import tv.ismar.searchpage.core.AppArrayList;
import tv.ismar.searchpage.core.event.AnswerAvailableEvent;
import tv.ismar.searchpage.core.http.HttpAPI;
import tv.ismar.searchpage.core.http.HttpManager;
import tv.ismar.searchpage.data.http.AppSearchObjectEntity;
import tv.ismar.searchpage.data.http.AppSearchRequestParams;
import tv.ismar.searchpage.data.http.AppSearchResponseEntity;
import tv.ismar.searchpage.data.table.AppTable;
import cn.ismartv.injectdb.library.query.Select;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

//import tv.ismar.searchpage.MainApplication;

/**
 * Created by huaijie on 1/4/16.
 */
public class AppHandler {
    private static final String TAG = "AppHandler";

    public AppHandler(final JsonObject jsonObject, final AppHandleCallback callback) {
        String appName = jsonObject.get("object").getAsJsonObject().get("appname").toString().replace("\"", "");
        final String intent = jsonObject.get("intent").toString();
        final List<AppTable> appTables = new Select().from(AppTable.class).where("app_name like ?", "%" + appName + "%").execute();

        Log.i(TAG, "json: " + jsonObject.toString());
        Retrofit retrofit = HttpManager.getInstance().resetAdapter_SKY;
        AppSearchRequestParams params = new AppSearchRequestParams();
        params.setKeyword(appName);
        params.setContent_type("app");
        params.setPage_count(300);
        params.setPage_no(1);
        retrofit.create(HttpAPI.AppSearch.class).doRequest(params).enqueue(new Callback<AppSearchResponseEntity>() {
            @Override
            public void onResponse(Call<AppSearchResponseEntity> call, Response<AppSearchResponseEntity> response) {
                Log.i(TAG, new Gson().toJson(response.body()));
                if (response.errorBody() == null) {

                    AppSearchResponseEntity appSearchResponseEntity = response.body();
                    AppArrayList appList = new AppArrayList();
                    for (AppTable appTable : appTables) {
                        Log.i(TAG, "local app:" + appTable.app_name + "-->" + appTable.app_package);
                        AppSearchObjectEntity appSearchObjectEntity = new AppSearchObjectEntity();
                        appSearchObjectEntity.setTitle(appTable.app_name);
                        appSearchObjectEntity.setCaption(appTable.app_package);
                        appSearchObjectEntity.setIsLocal(true);
                        appList.add(appSearchObjectEntity);
                    }

                    AppSearchResponseEntity.Facet[] facet = appSearchResponseEntity.getFacet();

                    if (facet != null) {
                        List<AppSearchObjectEntity> serverAppList = facet[0].getObjects();
                        for (AppSearchObjectEntity entity : serverAppList) {
                            AppTable table = new Select().from(AppTable.class).where("app_package = ?", entity.getCaption()).executeSingle();
                            if (table != null) {
                                entity.setIsLocal(true);
                            }
                            appList.add(entity);
                        }

                        appSearchResponseEntity.getFacet()[0].setObjects(appList);
                        appSearchResponseEntity.getFacet()[0].setTotal_count(appList.size());
                    }
                    callback.onAppHandleSuccess(appSearchResponseEntity, new Gson().toJson(jsonObject));


                } else {
                    //error
                    EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));
                }
            }

            @Override
            public void onFailure(Call<AppSearchResponseEntity> call, Throwable t) {
                EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));

            }

        });
    }


//    private void launchApp(String appPackage) {
//        Context context = MainApplication.getContext();
//        PackageManager packageManager = context.getPackageManager();
//        Intent intent = packageManager.getLaunchIntentForPackage(appPackage);
//        context.startActivity(intent);
//    }


}
