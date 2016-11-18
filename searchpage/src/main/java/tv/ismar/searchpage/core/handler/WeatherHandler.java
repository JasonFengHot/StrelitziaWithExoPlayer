package tv.ismar.searchpage.core.handler;

import com.google.gson.JsonObject;

import tv.ismar.searchpage.data.table.CityTable;
import cn.ismartv.injectdb.library.query.Select;

/**
 * Created by huaijie on 1/15/16.
 */
public class WeatherHandler {
    private static final String TAG = "WeatherHandler";

    public WeatherHandler(JsonObject jsonObject, WeatherHandlerCallback weatherHandlerCallback) {
        try {

            String city = jsonObject.getAsJsonObject("object").get("region").toString().replace("\"", "");
            CityTable cityTable = new Select().from(CityTable.class).where("city like ?", "%" + city + "%").executeSingle();
            weatherHandlerCallback.onWeatherHandle(cityTable);
        } catch (NullPointerException e) {
            weatherHandlerCallback.onWeatherHandle(null);
        }
    }
}
