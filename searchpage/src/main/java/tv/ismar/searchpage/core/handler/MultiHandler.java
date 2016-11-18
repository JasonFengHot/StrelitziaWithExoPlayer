package tv.ismar.searchpage.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tv.ismar.searchpage.core.AppArrayList;
import tv.ismar.searchpage.core.event.AnswerAvailableEvent;
import tv.ismar.searchpage.core.http.HttpAPI;
import tv.ismar.searchpage.core.http.HttpManager;
import tv.ismar.searchpage.data.http.AppSearchObjectEntity;
import tv.ismar.searchpage.data.http.AppSearchRequestParams;
import tv.ismar.searchpage.data.http.AppSearchResponseEntity;
import tv.ismar.searchpage.data.http.IndicatorResponseEntity;
import tv.ismar.searchpage.data.http.SemanticSearchRequestEntity;
import tv.ismar.searchpage.data.http.SemanticSearchResponseEntity;
import tv.ismar.searchpage.data.table.AppTable;
import cn.ismartv.injectdb.library.query.Select;
import retrofit2.Response;

//import tv.ismar.searchpage.AppConstant;

/**
 * Created by huaijie on 2016/1/30.
 */
public class MultiHandler extends Thread {
    private static final int HANDLE_SUCCESS = 0x0001;
    private JsonArray jsonArray;
    private String rawText;
    private MessageHandler messageHandler;
    private MultiHandlerCallback callback;

    public MultiHandler(JsonArray jsonArray, String rawText, MultiHandlerCallback callback) {
        messageHandler = new MessageHandler(this);
        this.jsonArray = jsonArray;
        this.rawText = rawText;
        this.callback = callback;
    }

    @Override
    public void run() {
        List<IndicatorResponseEntity> indicatorList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String domain = jsonObject.get("domain").getAsString();
            switch (domain) {
                case "app":
                    String appName = jsonObject.get("object").getAsJsonObject().get("appname").toString().replace("\"", "");
                    jsonObject.addProperty("raw_text", appName);
                    final List<AppTable> appTables = new Select().from(AppTable.class).where("app_name like ?", "%" + appName + "%").execute();
                    try {
                        AppSearchRequestParams params = new AppSearchRequestParams();
                        params.setKeyword(appName);
                        params.setContent_type("app");
                        params.setPage_count(300);
                        params.setPage_no(1);
                        Response<AppSearchResponseEntity> response = HttpManager.getInstance().resetAdapter_SKY.create(HttpAPI.AppSearch.class)
                                .doRequest(params)
                                .execute();
                        AppSearchResponseEntity responseEntity = response.body();

                        AppArrayList appList = new AppArrayList();
                        for (AppTable appTable : appTables) {

                            AppSearchObjectEntity appSearchObjectEntity = new AppSearchObjectEntity();
                            appSearchObjectEntity.setTitle(appTable.app_name);
                            appSearchObjectEntity.setCaption(appTable.app_package);
                            appSearchObjectEntity.setIsLocal(true);
                            appList.add(appSearchObjectEntity);
                        }


                        AppSearchResponseEntity.Facet[] facet = responseEntity.getFacet();

                        if (facet != null) {
                            List<AppSearchObjectEntity> serverAppList = facet[0].getObjects();
                            for (AppSearchObjectEntity entity : serverAppList) {
                                AppTable table = new Select().from(AppTable.class).where("app_package = ?", entity.getCaption()).executeSingle();
                                if (table != null) {
                                    entity.setIsLocal(true);
                                }
                                appList.add(entity);
                            }
                        }

                        IndicatorResponseEntity entity = new IndicatorResponseEntity();
                        entity.setType("app");
                        entity.setSearchData(appList);
                        entity.setSemantic(new Gson().toJson(jsonObject));

                        indicatorList.add(entity);


                    } catch (IOException e) {
                        EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));
                    }
                    break;
                case "video":
                case "tv_show":
                    jsonObject.addProperty("raw_text", rawText);
                    SemanticSearchRequestEntity requestEntity = new SemanticSearchRequestEntity();
                    requestEntity.setSemantic(jsonObject);
//                    requestEntity.setPage_on(AppConstant.DEFAULT_PAGE_NO);
//                    requestEntity.setPage_count(AppConstant.DEFAULT_PAGE_COUNT);
                    try {
                        Response<SemanticSearchResponseEntity> response = HttpManager.getInstance().resetAdapter_SKY.create(HttpAPI.SemanticSearch.class)
                                .doRequest(requestEntity).execute();
                        IndicatorResponseEntity entity = new IndicatorResponseEntity();
                        entity.setType("video");
                        entity.setSearchData(response.body());
                        entity.setSemantic(new Gson().toJson(jsonObject));
                        indicatorList.add(0, entity);
                    } catch (IOException e) {
                        EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));
                    }
                    break;
            }
        }
        //send message
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("list", indicatorList);
        hashMap.put("rawText", rawText);
        Message message = messageHandler.obtainMessage(HANDLE_SUCCESS, hashMap);
        messageHandler.sendMessage(message);
    }


    private class MessageHandler extends Handler {
        public WeakReference<MultiHandler> weakReference;

        public MessageHandler(MultiHandler handler) {
            super(Looper.getMainLooper());
            this.weakReference = new WeakReference<>(handler);
        }

        @Override
        public void handleMessage(Message msg) {
            MultiHandler handler = weakReference.get();
            if (handler != null) {
                switch (msg.what) {
                    case HANDLE_SUCCESS:
                        HashMap<String, Object> hashMap = (HashMap) msg.obj;
                        List<IndicatorResponseEntity> list = (List<IndicatorResponseEntity>) hashMap.get("list");
                        String rawText = (String) hashMap.get("rawText");
                        handler.callback.onMultiHandle(list, rawText);
                        break;
                }
            }
        }
    }
}
