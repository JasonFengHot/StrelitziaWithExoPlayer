package tv.ismar.app.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/1/29.
 */
public class HotWords {

    /**
     */

    public String title;
    /**
     * vertical_url : null
     */

    public Object vertical_url;

    public static List<HotWords> arrayHotWordsFromData(String str) {

        Type listType = new TypeToken<ArrayList<HotWords>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
    }
}
