package tv.ismar.searchpage.core.handler;//package cn.ismartv.Jasmine.core.handler;
//
//import android.util.Log;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//
//import java.util.List;
//
//import cn.ismartv.injectdb.library.query.Select;
//import cn.ismartv.Jasmine.data.http.JsonRes;
//import cn.ismartv.Jasmine.data.http.JasmineResultEntity;
//import cn.ismartv.Jasmine.data.table.AppTable;
//
///**
// * Created by huaijie on 2016/1/30.
// */
//public class JsonDomainHandler {
//    private static final String TAG = "JsonDomainHandler";
//
//    private HandleCallback callback;
//    private AppHandleCallback appHandleCallback;
//    private MultiHandlerCallback multiHandlerCallback;
//    private WeatherHandlerCallback weatherHandlerCallback;
//    private TvInstructionCallback mTvInstructionCallback;
//
//    public JsonDomainHandler(String json, HandleCallback handleCallback, AppHandleCallback appHandleCallback, MultiHandlerCallback multiHandlerCallback,
//                             WeatherHandlerCallback weatherHandlerCallback, TvInstructionCallback tvInstructionCallback) {
//        this.mTvInstructionCallback = tvInstructionCallback;
//        this.multiHandlerCallback = multiHandlerCallback;
//        this.appHandleCallback = appHandleCallback;
//        this.callback = handleCallback;
//        this.weatherHandlerCallback = weatherHandlerCallback;
//        getElement(json);
//    }
//
//    private void getElement(String result) {
//        JasmineResultEntity[] JasmineResultEntity = new Gson().fromJson(result.toString(), JasmineResultEntity[].class);
//        for (JasmineResultEntity entity : JasmineResultEntity) {
//            JsonElement jsonElement = new JsonParser().parse(entity.getJson_res());
//            JsonRes jsonRes = new Gson().fromJson(jsonElement, JsonRes.class);
//            String rawText = jsonRes.getRaw_text();
////            List<WordFilterResult> filterResults = FilterUtil.filter(rawText);
////            if (!filterResults.isEmpty()) {
////            } else {
//            Object resultObject = jsonRes.getResults();
//            String json = new Gson().toJson(resultObject);
//            Log.i(TAG, json);
//            if (resultObject == null || new JsonParser().parse(json).getAsJsonArray().size() == 0) {
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("raw_text", rawText);
//                switch (rawText.replace("\"", "")) {
//                    case "设备关机":
//                    case "关机":
//                        new OtherTvInstructionHandler("shutdown", mTvInstructionCallback);
//                        return;
//                    case "取消静音":
//                        new OtherTvInstructionHandler("unmute", mTvInstructionCallback);
//                        return;
//
//                }
//                String appName = rawText.replace("\"", "");
//                List<AppTable> appTables = new Select().from(AppTable.class).where("app_name = ?", rawText.replace("\"", "")).execute();
//                if (appTables != null && !appTables.isEmpty()) {
//
//                    jsonObject.addProperty("domain", "app");
//                    jsonObject.addProperty("intent", "open");
//                    JsonObject myObject = new JsonObject();
//                    myObject.addProperty("appname", appName);
//                    jsonObject.add("object", myObject);
//                    new AppHandler(jsonObject, appHandleCallback);
//                    Log.d(TAG, jsonObject.toString());
//
//                } else {
//                    new DefaultHandler(jsonObject, callback);
//                }
//
//
//            } else if (new JsonParser().parse(json).getAsJsonArray().size() == 1) {
//                JsonArray jsonArray = new JsonParser().parse(json).getAsJsonArray();
//                JsonObject o = jsonArray.get(0).getAsJsonObject();
//                o.addProperty("raw_text", rawText);
//                switch (rawText.replace("\"", "")) {
//                    case "设备关机":
//                    case "关机":
//                        new OtherTvInstructionHandler("shutdown", mTvInstructionCallback);
//                        return;
//                    case "取消静音":
//                        new OtherTvInstructionHandler("unmute", mTvInstructionCallback);
//                        return;
//                }
//
//                String domain = o.get("domain").getAsString();
//                switch (domain) {
//                    case "app":
//                        new AppHandler(o, appHandleCallback);
//                        break;
//                    case "video":
//                    case "tv_show":
//                        new VideoHandler(o, callback);
//                        break;
//                    case "weather":
//                        new WeatherHandler(o, weatherHandlerCallback);
//                        break;
//                    case "tv_instruction":
//                        new TvInstructionHandler(o, mTvInstructionCallback);
//                        break;
//                    default:
//                        new VideoHandler(o, callback);
//                        break;
//                }
//
//            } else {
//                JsonArray array = new JsonParser().parse(json).getAsJsonArray();
//                new MultiHandler(array, rawText, multiHandlerCallback).start();
//            }
//        }
//    }
//}
